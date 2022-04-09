(function () {
    'use strict';

    window.dashboard = window.dashboard || {};

    dashboard.broker = $({});

    dashboard.siteurl = "";
    dashboard.study_code = "all";

    dashboard.init = function () {

        dashboard.siteurl = dashboard.broker.getRootSitePath();
        dashboard.departments.loadDepartmentsOnSearchBar($("#department-columns"));
        dashboard.institutions.institutionName = $("#institutionTitle").val();
        dashboard.institutions.institutionId = $("#institutionIdentity").val();

        alertify.defaults.transition = "slide";
        alertify.defaults.theme.ok = "btn btn-primary";
        alertify.defaults.theme.cancel = "btn btn-danger";
        alertify.defaults.theme.input = "form-control";
        alertify.set('notifier','position', 'top-center');


        let _init_dp_id     = $("#department_id").val();
        let _init_dp_title  = $("#department_name").val();
        let _init_sc_id     = $("#school_id").val();
        let _init_sc_title  = $("#school_name").val();

        if (_init_dp_id !== undefined && _init_dp_id != null && _init_dp_id !== "") {
            $("#select_department_info").css("display","none");
            $("#newStaffBt").prop("disabled",false);
            $("#myTab").show();
            $("#myTabContent").show();
            dashboard.departments.selectedDepartmentId = _init_dp_id;
            dashboard.departments.selectedDepartmentName = _init_dp_title;
            dashboard.schools.selectedSchoolId = _init_sc_id;
            dashboard.schools.selectedSchoolName = _init_sc_title;
            dashboard.staffmembers.initDT();
            $("#staff_card").show();
        }
        else {
            $("#staff_card").hide();
            $("#newStaffBt").prop("disabled",true);
        }

        InitEvents();
        loader.initialize();

        function InitEvents() {

            $(".toggle-card").on('click',function(e) {
                 let target = $(this).data('target');
                 let el = document.getElementById(target);
                 if (el.style.display === "none") {
                     el.style.display = "block";
                     $(this).removeClass("btn-outline-dark");
                     $(this).addClass("btn-outline-primary");
                 }
                 else {
                     el.style.display = "none";
                     $(this).removeClass("btn-outline-primary");
                     $(this).addClass("btn-outline-dark");
                 }
            });

        }
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