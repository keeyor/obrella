(function () {
    'use strict';

    dashboard.departments = dashboard.departments || {};

    let departmentsDT = null;

    dashboard.departments.selectedDepartmentId = null;
    dashboard.departments.selectedRowIndex = null;
    dashboard.departments.selectedDepartmentName = null;

    dashboard.departments.initializeDepartmentsList = function (departmentId, $elem) {

       // $elem.empty();

        $.ajax({
            url: dashboard.siteurl + '/api/v2/s2/departments.web/authorized/content', //departments.web/school/' + schoolId,
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
                //if (departmentId === "") {
                $elem.val(departmentId).trigger("change");
               // }
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