(function () {
    'use strict';

    dashboard.departments = dashboard.departments || {};

    let $departments_el;
    dashboard.departments.init = function (staff_department_id) {
        $departments_el = $(".staff_department");
        $departments_el.select2({
            placeholder: 'Επιλέξτε Τμήμα'
        });

        $("#departments_s2").on('select2:select', function (e) {
            let data = e.params.data;
            let sel_department_Id = data.id;
            $("#staff_department_id").val(sel_department_Id);
            let message = {msg: "Department selected!", value: sel_department_Id, el_id: "departments_s2" };
            dashboard.broker.trigger('afterSelect.department', [message]);
        });
        $("#departments_s21").on('select2:select', function (e) {
            let data = e.params.data;
            let sel_department_Id = data.id;
            $("#staff_department_id").val(sel_department_Id);
            let message = {msg: "Department selected!", value: sel_department_Id, el_id: "departments_s21" };
            dashboard.broker.trigger('afterSelect.department', [message]);
        });
        $("#staff_department").on('select2:select', function (e) {
            let data = e.params.data;
            let sel_department_Id = data.id;
            $("#staff_department_id").val(sel_department_Id);
            let message = {msg: "Department selected!", value: sel_department_Id, el_id: "departments_s21" };
            dashboard.broker.trigger('afterSelect.department', [message]);
        });

        let $dep1 = $("#departments_s2");
        let $dep2 = $("#departments_s21");

        dashboard.departments.initializeDepartmentsList($dep1,staff_department_id);
        dashboard.departments.initializeDepartmentsList($dep2,staff_department_id);

        dashboard.departments.initializeDepartmentsList($("#staff_department"),staff_department_id);
    }

    dashboard.departments.initializeDepartmentsList = function ($dep,staff_department_id) {

        let schoolId = 'dummy';
        $.ajax({
            url: dashboard.siteurl + '/api/v1/s2/departments.web/school/' + schoolId,
            cache: false
        })
            .done(function( data ) {
                $dep.select2({
                    placeholder: 'Επιλέξτε Τμήμα',
                    width: 'style', // need to override the changed default
                    data : data.results,
                    dropdownParent: $dep.parent(),
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });

                $dep.val(staff_department_id).trigger("change");
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