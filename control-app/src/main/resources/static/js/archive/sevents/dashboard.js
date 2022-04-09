(function () {
    'use strict';

    window.dashboard = window.dashboard || {};

    dashboard.broker = $({});

    dashboard.siteurl = "";
    dashboard.dtLanguageGr = "";

    dashboard.init = function () {

        dashboard.siteurl = dashboard.broker.getRootSitePath();
        InitGlobalSettings();
        dashboard.sevents.init();

        function InitGlobalSettings() {

            alertify.defaults.transition = "slide";
            alertify.defaults.theme.ok = "btn btn-primary";
            alertify.defaults.theme.cancel = "btn btn-danger";
            alertify.defaults.theme.input = "form-control";
            alertify.set('notifier','position', 'top-right');
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