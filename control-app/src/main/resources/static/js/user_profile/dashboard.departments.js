(function () {
    'use strict';

    dashboard.departments = dashboard.departments || {};

    let siteUrl;
    let $department_select;

    dashboard.departments.init = function () {
        siteUrl          = dashboard.siteUrl;
        $department_select = $("#_assign_department");

        dashboard.departments.initializeDepartmentsList('',$department_select,'');

        $department_select.on('select2:select', function (e) {
            let data = e.params.data;
            let sel_dep_Id = data.id;
            if (sel_dep_Id !== "") {
                $("#submit_bt").attr("disabled",false);
            }
        });
    };

    dashboard.departments.initializeDepartmentsList = function (departmentId, $elem, schoolId) {

        // WHen reading Client List -->
        if (schoolId === '') { schoolId = 'dummy';}
        $.ajax({
            url: dashboard.siteUrl + '/api/v1/s2/departments.web/school/' + schoolId,
            cache: false
        })
            .done(function( data ) {

                $elem.select2({
                    placeholder: 'Επιλέξτε Τμήμα',
                    width: 'style', // need to override the changed default
                    data : data.results,
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });

                $elem.val(departmentId).trigger("change");
                if (departmentId === "") {
                    $("#submit_bt").attr("disabled",true);
                }
                let message = {msg: "Department afterInit!"};
                dashboard.broker.trigger('afterInit.Department', [message]);
            });

        function formatRepo (repo) {
            if (repo.loading) {
                return repo.text;
            }

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
            return repo.text;
        }

    };
})();
