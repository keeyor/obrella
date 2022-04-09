(function () {
    'use strict';

    window.dashboard = window.dashboard || {};
    dashboard.broker = $({});

    dashboard.siteUrl   = "";
    dashboard.app_path  = "";


    dashboard.init = function () {
        dashboard.siteUrl = dashboard.broker.getRootSitePath();
    };

    $(document).ready(function () {
        dashboard.init();
        dashboard.live.init();
        let localeCode = $("#localeCode").val();
        moment.locale(localeCode);
        let today = moment().format("LL");
        $("#daily_date_now").html(today);

    });

    dashboard.broker.getRootSitePath = function () {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let applicationName = _location.substring(0, applicationNameIndex) + '/';
        let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

        return _location.substring(0, webFolderIndex);
    };

})();
