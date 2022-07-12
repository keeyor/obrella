(function () {
    'use strict';

        $('#about_modal').on('show.coreui.modal', function (e) {
            loadHTMLBySiteAndCodeFromDB("live","about");
        });
        $('#terms_modal').on('show.coreui.modal', function (e) {
            loadHTMLBySiteAndCodeFromDB("live","terms");
        });
        $('#faq_modal').on('show.coreui.modal', function (e) {
            loadHTMLBySiteAndCodeFromDB("live","faq");
        });

        function loadHTMLBySiteAndCodeFromDB(site, code) {

            let _location = getRootSitePath();

            $.ajax({
                type:        "GET",
                url: 		  _location + '/api/v1/text/' + site + '/code/' + code,
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

      function getRootSitePath() {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let applicationName = _location.substring(0, applicationNameIndex) + '/';
        let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

        _location = _location.substring(0, webFolderIndex);

        return _location;
    }
})();