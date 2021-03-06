(function () {
    'use strict';

    //PRESERVE TAB AFTER RELOAD
        $('a[data-coreui-toggle="pill"]').on('show.coreui.tab', function (e) {
            localStorage.setItem('activeAdminTab', $(e.target).attr('href'));
        });
        //LOAD ACTIVE TAB
        let activeTab = localStorage.getItem('activeAdminTab');
        if (activeTab) {
            let $pills = $('#v-pills-tab a');
            $pills.removeClass("active");
            $(".tab-content .tab-pane").removeClass("active");
            let $pill = $('#v-pills-tab a[href="' + activeTab + '"]');
            $pill.addClass("active");
            $(activeTab).addClass("show").addClass("active");
        }


        $('#managers_modal').on('show.coreui.modal', function () {
            loadHTMLBySiteAndCodeFromDB("admin","managers");
        });
        $('#updates_modal').on('show.coreui.modal', function () {
            loadHTMLBySiteAndCodeFromDB("admin","updates");
        });
        $('#about_modal').on('show.coreui.modal', function () {
            loadHTMLBySiteAndCodeFromDB("admin","about");
        });
        $('#terms_modal').on('show.coreui.modal', function () {
            loadHTMLBySiteAndCodeFromDB("admin","terms");
        });
        $('#faq_modal').on('show.coreui.modal', function () {
            loadHTMLBySiteAndCodeFromDB("admin","faq");
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