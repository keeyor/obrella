(function () {
    'use strict';

    dashboard.staffmembers = dashboard.staffmembers || {};

    let $staff_el;

    dashboard.staffmembers.init = function () {
        $staff_el             = $("#supervisor_s2");
        InitContorls();
        InitEvents();
    };

    function InitContorls() {
        $staff_el.select2({
            placeholder: 'Επιλέξτε Υπ. Καθηγητή (προηγείται η επιλογή Μαθήματος)'
        })
    }
    function InitEvents() {
        $staff_el.on('select2:select', function (e) {
            let data = e.params.data;
            let sel_supervisor_id = data.id;
            $("#supervisor_id").val(sel_supervisor_id);

            let message = {msg: "StaffMember selected!"};
            dashboard.broker.trigger('afterSelect.staffMember', [message]);
        });
    }

    dashboard.staffmembers.getAuthorizedStaffMembersOfCourse = function (courseId, staffId) {

        $staff_el.empty();
        $.ajax({
            url:  dashboard.siteUrl + '/api/v1/s2/staff.web/authorized/course/' + courseId + '/content',
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

                $staff_el.val(staffId).trigger("change");

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
                return "<span style='font-weight: 600'>" + repo.text + "</span><span> - " + repo.subheader + "</span>";
            else
                return repo.text;
        }
    };

/*
    dashboard.staffmembers.select2staff = function (staffId, editorId) {

        // WHen reading Client List -->
        $.ajax({
            url:  dashboard.siteUrl + '/api/v1/s2/staff.web/user/' + editorId,
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

                $staff_el.val(staffId).trigger("change");

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
                return "<b>" + repo.text + "</b>";
            else
                return repo.text;
        }
    };
*/
})();
