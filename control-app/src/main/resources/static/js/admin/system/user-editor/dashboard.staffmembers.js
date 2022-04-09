(function () {
    'use strict';

    dashboard.staffmembers = dashboard.staffmembers || {};

    let $staff_el;

    dashboard.staffmembers.init = function () {
        $staff_el = $(".js-staffMembers-tags");
        $staff_el.select2({
            placeholder: 'Επιλέξτε Τμήμα -> Καθηγητή'
        });

        $staff_el.on('select2:select', function (e) {
            let data = e.params.data;
            let sel_department_Id = data.id;
            let message = {msg: "StaffMember selected!", value: sel_department_Id};
            dashboard.broker.trigger('afterSelect.staffmember', [message]);
        });
    };

    dashboard.staffmembers.getStaffMembersOfDepartmentId = function (departmentId) {

        $.ajax({
            url:  dashboard.siteurl + '/api/v1/s2/staff.web/department/' + departmentId,
            cache: false
        })
            .done(function( data ) {
                $staff_el.select2({
                    placeholder: 'Επιλέξτε τον Υπεύθυνο Καθηγητή',
                    width: 'style', // need to override the changed default
                    data : data.results,
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });

                $staff_el.val("").trigger("change");
            });

        function formatRepo (repo) {
            if (repo.loading) {
                return repo.text;
            }

            let markup = "<div class='select2-result-repository clearfix'>" +
                "<div class='select2-result-repository__meta'>" +
                "<div class='select2-result-repository__title'><i class=\"fas fa-user-tie\"></i> " + repo.text + "</div>";

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
