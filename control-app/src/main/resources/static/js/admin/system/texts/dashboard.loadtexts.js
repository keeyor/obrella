(function () {
    'use strict';

    dashboard.loadtexts = dashboard.loadtexts || {}

    dashboard.loadtexts.init = function () {
        RegisterEvents();
    };

    function RegisterEvents() {
        $('#about_modal').on('show.coreui.modal', function (e) {
            loadHTMLBySiteAndCodeFromDB("admin","about");
        })
    }
    function loadHTMLBySiteAndCodeFromDB(site, code) {
        $.ajax({
            type:        "GET",
            url: 		  dashboard.siteurl + '/api/v1/text/' + site + '/code/' + code,
            contentType: "application/json; charset=utf-8",
            async:		  true,
            success: function(data){
                $("#" + code + "-modal-html").html(data.content);
            },
            error: function ()  {
                return "error";
            }
        });
    }
})();