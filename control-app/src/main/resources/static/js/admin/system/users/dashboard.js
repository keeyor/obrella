(function () {
    'use strict';

    window.dashboard = window.dashboard || {};

    dashboard.broker = $({});

    dashboard.siteurl = "";
    dashboard.study_code = "all";
    dashboard.add_action = "";
    dashboard.institutionName;
    dashboard.institutionId;

    dashboard.init = function () {

        dashboard.siteurl = dashboard.broker.getRootSitePath();

        dashboard.institutionName = $("#institutionTitle").val();
        dashboard.institutionId = $("#institutionIdentity").val();

        alertify.defaults.transition = "slide";
        alertify.defaults.theme.ok = "btn btn-primary";
        alertify.defaults.theme.cancel = "btn btn-danger";
        alertify.defaults.theme.input = "form-control";
        alertify.set('notifier','position', 'top-center');

        // Do this before you initialize any of your modals
        $.fn.modal.Constructor.prototype._enforceFocus = function() {};

        dashboard.users.init();
        loader.initialize();

        dashboard.broker.on("afterSelect.department afterInit.department", function (event, msg) {

            let sel_department_id = msg.value;
            $(".js-staffMembers-tags").empty();
            $("#courses_s2").empty();
            $("#events_s2").empty();
            dashboard.staffmembers.getStaffMembersOfDepartmentId(sel_department_id);
        });

        dashboard.broker.on("afterSelect.staffmember afterInit.staffmember", function (event, msg) {
            let sel_staff_id = msg.value;
            if (sel_staff_id != null && sel_staff_id !== "") {
                if (dashboard.add_action === "event") {
                    $("#events_s2").empty();
                    dashboard.events.getEventsByResponsiblePerson(sel_staff_id);
                }
                else if (dashboard.add_action === "course") {
                    $("#courses_s2").empty();
                    dashboard.courses.s2FillCoursesByTeachingStaff(sel_staff_id);
                }
            }
        });

    };

    $(document).ready(function () {
        dashboard.init();
    });

    dashboard.broker.getRootSitePath = function () {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let applicationName = _location.substring(0, applicationNameIndex) + '/';
        let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

        return _location.substring(0, webFolderIndex);
    };
})();