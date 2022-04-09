(function () {
    'use strict';

    dashboard.departments = dashboard.departments || {};

    let $departments_el;
    dashboard.departments.init = function (staff_department_id) {
        $departments_el = $(".staff_department");
        $departments_el.select2({
            placeholder: 'Επιλέξτε Τμήμα'
        });
        $departments_el.on('select2:select', function (e) {
            let data = e.params.data;
            let sel_department_Id = data.id;
            $("#staff_department_id").val(sel_department_Id);
            let message = {msg: "Department selected!", value: sel_department_Id};
            dashboard.broker.trigger('afterSelect.department', [message]);
        });
        dashboard.departments.initializeDepartmentsList(staff_department_id);
    }
    dashboard.departments.initializeDepartmentsList = function (staff_department_id) {

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

                $departments_el.val(staff_department_id).trigger("change");
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