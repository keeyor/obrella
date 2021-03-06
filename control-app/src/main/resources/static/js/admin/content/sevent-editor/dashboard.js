(function () {
    'use strict';

    window.dashboard = window.dashboard || {};

    dashboard.broker = $({});

    dashboard.siteurl = "";
    dashboard.hostUrl = "";
    dashboard.app_path = "";
    dashboard.dtLanguageGr = "";

    dashboard.init = function () {

        $('select:not(.normal)').each(function () {
            $(this).select2({
                dropdownParent: $(this).parent()
            });
        });

        dashboard.siteurl = dashboard.broker.getRootSitePath();
        dashboard.hosturl = dashboard.broker.getHostURL();
        dashboard.app_path = "";

        let   msg_val   = $("#msg_val").val();
        let   msg_type  = $("#msg_type").val();

        if (msg_val !== '') {
            let message = {msg: "instant message", type: msg_type, val: msg_val};
            dashboard.broker.showInstantMessage(message.type ,message.val);
        }

        InitGlobalSettings();


        function InitGlobalSettings() {

            alertify.defaults.transition = "slide";
            alertify.defaults.theme.ok = "btn blue-btn-wcag-bgnd-color text-white";
            alertify.defaults.theme.cancel = "btn red-btn-wcag-bgnd-color text-white";
            alertify.defaults.theme.input = "form-control";
            alertify.set('notifier','position', 'top-center');
        }
    };

    $(document).ready(function () {

        //load specific tab when returning
        let url = document.location.toString();
        if (url.match('#')) {
            $('.nav-tabs a[href="#' + url.split('#')[1] + '"]').tab('show');
        }
        //Change hash for page-reload
        $('.nav-tabs a[href="#' + url.split('#')[1] + '"]').on('shown', function (e) {
            window.location.hash = e.target.hash;
        });

        dashboard.init();
        dashboard.upload.init();
        dashboard.sevents.init();

    });

    dashboard.broker.getRootSitePath = function () {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let applicationName = _location.substring(0, applicationNameIndex) + '/';
        let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

        return _location.substring(0, webFolderIndex);
    };

    dashboard.broker.getHostURL = function () {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        return _location.substring(0, applicationNameIndex);
    };


    dashboard.broker.showInstantMessage = function(type, val) {

        //Override alertify defaults
        alertify.set('notifier','position', 'top-center');

        switch (type) {
            case "alert-success":
                alertify.success(val);
                break;
            case "alert-danger":
                //alertify.error(val);
                alertify.alert()
                    .setting({
                        'title' : '<i class="fas fa-exclamation-circle me-1" style="color: red"></i>????????????????',
                        'label':'OK',
                        'message': val
                    }).show();
                break;
            case "alert-warning":
                alertify.warning(val);
                break;
            case "alert-info":
                alertify.info(val);
                break;
        }
    };
})();