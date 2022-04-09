(function () {
    'use strict';

    window.dashboard = window.dashboard || {};

    dashboard.broker = $({});

    dashboard.siteurl = "";

    dashboard.init = function () {

        dashboard.siteurl = dashboard.broker.getRootSitePath();
        dashboard.schools.init();
        dashboard.departments.init();
        dashboard.institutions.init();


        alertify.defaults.transition = "slide";
        alertify.defaults.theme.ok = "btn btn-primary";
        alertify.defaults.theme.cancel = "btn btn-danger";
        alertify.defaults.theme.input = "form-control";
    };

    $(document).ready(function () {

        dashboard.init();

        dashboard.broker.on('schoolItemClick', function (event, message) {

            //console.log(message.msg + dashboard.schools.selectedSchoolId);
            let selectedSchoolId = dashboard.schools.selectedSchoolId;

            //update departments DT
            dashboard.departments.DtFillDepartmentsBySchool(selectedSchoolId);
        });

        dashboard.broker.on('departmentItemClick', function (event, message) {

           // console.log(message.msg + dashboard.departments.selectedDepartmentId);
        });

    });

    dashboard.broker.getRootSitePath = function () {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let applicationName = _location.substring(0, applicationNameIndex) + '/';
        let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

        return _location.substring(0, webFolderIndex);
    };
})();