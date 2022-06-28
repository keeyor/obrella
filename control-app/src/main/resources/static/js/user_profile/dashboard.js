(function () {
    'use strict';

    window.dashboard = window.dashboard || {};
    dashboard.broker = $({});

    dashboard.siteUrl   = "";
    dashboard.app_path  = "";

    dashboard.init = function () {

        dashboard.siteUrl = dashboard.broker.getRootSitePath();
        dashboard.departments.init();
    };

    $(document).ready(function () {

        dashboard.init();
        define_events();
        init_controls();
    });

    dashboard.broker.getRootSitePath = function () {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let applicationName = _location.substring(0, applicationNameIndex) + '/';
        let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

        return _location.substring(0, webFolderIndex);
    };

    function define_events() {
        $("#department-columns, #category-list").on('click','a.dropdown-toggle', function() {
            if (!$(this).next().hasClass('show')) {
                $(this).parents('.dropdown-menu').first().find('.show').removeClass('show');
            }
            var $subMenu = $(this).next('.dropdown-menu');
            $subMenu.toggleClass('show');

            $(this).parents('li.nav-item.dropdown.show').on('hidden.bs.dropdown', function() {
                $('.dropdown-submenu .show').removeClass('show');
            });
            return false;
        });
    }

    function init_controls() {

        let user_authorities = $("#user_authorities").text();
        if (user_authorities.includes("STAFFMEMBER")) {
            $("#edit_staff_link").show();
            dashboard.staffmembers.assignedCoursesDT();
            dashboard.staffmembers.assignedScheduledEventsDT();
        }
        if (user_authorities.includes("MANAGER") || user_authorities.includes("SUPPORT")) {
            $("#edit_manager_link").show();
            if (user_authorities.includes("SUPPORT")) {
                dashboard.users.assignedStaffCoursesDT();
                dashboard.users.assignedStaffEventsDT();
            }
        }

        alertify.defaults.transition = "slide";
        alertify.defaults.theme.ok = "btn blue-btn-wcag-bgnd-color text-white";
        alertify.defaults.theme.cancel = "btn red-btn-wcag-bgnd-color text-white";
        alertify.defaults.theme.input = "form-control";

        let msg_value = $("#msg_value").val();
        if (msg_value !== null && msg_value !== "") {
            $("#msg").show();
        }
        else {
            $("#msg").hide();
        }
    }
})();
