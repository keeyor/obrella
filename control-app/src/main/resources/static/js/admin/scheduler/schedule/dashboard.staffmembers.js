(function () {
    'use strict';

    dashboard.staffmembers = dashboard.staffmembers || {};

    let $staff_el;
    let $staff_s2;

    dashboard.staffmembers.init = function () {
        $staff_el             = $("#_supervisor");
        $staff_s2             = $("#supervisor_s2");

        let sel_supervisor_id = $staff_el.val();
        let editor_id         = $("#editor_id").val();

        //dashboard.staffmembers.select2staff(sel_supervisor_id, editor_id);

        $staff_s2.on('select2:select', function (e) {
            let data = e.params.data;
            $("#_supervisor").val(data.id);

            let message = {msg: "StaffMember selected!" };
            dashboard.broker.trigger('afterSelect.staffMember', [message]);
        });

    };

    dashboard.staffmembers.getAuthorizedStaffMembersOfCourse = function (courseId, staffId) {

        $staff_s2.empty();
        $.ajax({
            url:  dashboard.siteurl + '/api/v1/s2/staff.web/authorized/course/' + courseId + '/scheduler',
            cache: false
        })
            .done(function( data ) {

                $staff_s2.select2({
                    placeholder: 'Επιλέξτε τον Υπεύθυνο Καθηγητή',
                    width: 'style', // need to override the changed default
                    data : data.results,
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });

                $staff_s2.val(staffId).trigger("change");

                let message = {msg: "StaffMember afterInit!", value: staffId};
                dashboard.broker.trigger('afterInit.staffMember', [message]);
            });

        function formatRepo (repo) {
            if (repo.loading) {
                return repo.text;
            }

            let markup = "<div class='select2-result-repository clearfix'>" +
                "<div class='select2-result-repository__meta'>" +
                "<div class='select2-result-repository__title' style='font-weight: 500'>" + repo.text + "</div>";

            if (repo.children) {

            }
            else {
                markup += "<div class='select2-result-repository__statistics'>" +
                    "<div class='select2-result-repository__stargazers'>" + repo.subheader + " </div>" +
                    "</div>" +
                    "</div></div>";
            }
            return markup;
        }

        function formatRepoSelection (repo) {
            if (!repo.text.startsWith("Επιλέξτε"))
                return "<span  style=\"font-weight: 500\">" + repo.text + "</span>";
            else
                return repo.text;
        }
    };
    dashboard.staffmembers.select2staff = function (staffId, editorId) {

        $staff_s2.empty();
        // WHen reading Client List -->
        $.ajax({
            url:  dashboard.siteurl + '/api/v1/s2/staff.web/user/' + editorId,
            cache: false
        })
        .done(function( data ) {

                $staff_s2.select2({
                    placeholder: 'Επιλέξτε τον Υπεύθυνο Καθηγητή',
                    width: 'style', // need to override the changed default
                    data : data.results,
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });

                $staff_s2.val(staffId).trigger("change");

                let message = {msg: "StaffMember afterInit!", value: staffId};
                dashboard.broker.trigger('afterInit.staffMember', [message]);
        });

        function formatRepo (repo) {
            if (repo.loading) {
                return repo.text;
            }

            let markup = "<div class='select2-result-repository clearfix'>" +
                "<div class='select2-result-repository__meta'>" +
                "<div class='select2-result-repository__title'><i class=\"fas fa-user-tie\"></i> " + repo.text + "</div>";

            if (repo.children) {

            }
            else {
                markup += "<div class='select2-result-repository__statistics'>" +
                    "<div class='select2-result-repository__stargazers' style='font-size: 0.9em'>" + repo.subheader + " </div>" +
                    "</div>" +
                    "</div></div>";
            }
            return markup;
        }

        function formatRepoSelection (repo) {
            if (!repo.text.startsWith("Επιλέξτε"))
                return "<span  style=\"font-weight: 500\">" + repo.text + "</span>";
            else
                return repo.text;
        }
    };

})();
