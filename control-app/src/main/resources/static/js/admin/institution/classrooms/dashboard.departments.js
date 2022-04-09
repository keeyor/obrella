(function () {
    'use strict';

    dashboard.departments = dashboard.departments || {};

    let $departments_el;
    dashboard.departments.init = function (course_department_id) {
        $departments_el = $(".course_department");

        $departments_el.on('select2:select', function (e) {
            var $select = $(this);
            if (e.val === '') { // Assume only groups have an empty id
                e.preventDefault();
                $select.select2('data', $select.select2('data').concat(e.choice.children));
                $select.select2('close');
            }
        });
 /*        $departments_el.on('select2:select', function (e) {
            let data = e.params.data;
            let sel_department_Id = data.id;
            $("#staff_department_id").val(sel_department_Id);
            let message = {msg: "Department selected!", value: sel_department_Id};
            dashboard.broker.trigger('afterSelect.department', [message]);
        });*/
        //dashboard.departments.initializeDepartmentsList(course_department_id);
        dashboard.departments.initializeUnitsList();

        $(document).on("click", ".select2-results__group", function(){

            var groupName = $(this).text();
            var options = $("#departments_s2 option");

            $.each(options, function(key, value){
                let $el = $(value).closest("optgroup")[0];
                if ($el !== undefined) {
                    if ($el.label === groupName && $(value).val() !== "") {
                        $(value).prop("selected","selected");
                     }
                }

            });

            $("#departments_s2").trigger("change");
            $("#departments_s2").select2('close');

        });
    }

    dashboard.departments.initializeUnitsList = function () {

        let schoolId = 'dummy';
        $.ajax({
            url: dashboard.siteurl + '/api/v1/s2/departments.web/school/' + schoolId, //'/api/v1/s2/units.web',
            cache: false
        })
            .done(function( data ) {
                $departments_el.select2({
                    placeholder: 'Επιλέξτε Μονάδες',
                    width: 'style', // need to override the changed default
                    data : data.results,
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });

                $departments_el.val("").trigger("change");
                let message = {msg: "Department afterInit!"};
                dashboard.broker.trigger('afterInit.Department', [message]);
            })


        function formatRepo (repo) {
            if (repo.loading) {
                return repo.text;
            }3

            var markup = "<div class='select2-result-repository clearfix'>" +
                "<div class='select2-result-repository__meta'>" +
                "<div class='select2-result-repository__title'>" + repo.text + "</div>";

            if (repo.children) {

            }
            else {
                markup += "<div class='select2-result-repository__statistics'>" +
                    "</div>" +
                    "</div></div>";
            }
            return markup;
        }

        function formatRepoSelection (repo) {
                if (repo.subheader === "school" && !repo.text.includes("Σχολή")) {
                    return "Σχολή " + repo.text;
                }
                else if (repo.subheader === "department"  && !repo.text.includes("Τμήμα")) {
                    return "Τμήμα " + repo.text;
                }
                else {
                    return repo.text;
                }
        }

    };

    dashboard.departments.initializeDepartmentsList = function (course_department_id) {

        let schoolId = 'dummy';
        $.ajax({
            url: dashboard.siteurl + '/api/v1/s2/departments.web/school/' + schoolId,
            cache: false
        })
            .done(function( data ) {
                $departments_el.select2({
                    placeholder: 'Επιλέξτε Τμήμα',
                    width: 'style', // need to override the changed default
                    data : data.results,
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });

                $departments_el.val(course_department_id).trigger("change");
                let message = {msg: "Department afterInit!"};
                dashboard.broker.trigger('afterInit.Department', [message]);
            });

        function formatRepo (repo) {
            if (repo.loading) {
                return repo.text;
            }3

            var markup = "<div class='select2-result-repository clearfix'>" +
                "<div class='select2-result-repository__meta'>" +
                "<div class='select2-result-repository__title'>" + repo.text + "</div>";

            if (repo.children) {

            }
            else {
                markup += "<div class='select2-result-repository__statistics'>" +
                    "</div>" +
                    "</div></div>";
            }
            return markup;
        }

        function formatRepoSelection (repo) {
            if (!repo.text.startsWith("Επιλέξτε"))
                return "<b>" + repo.text + "</b>";
            else
                return repo.text;
        }

    };
})();