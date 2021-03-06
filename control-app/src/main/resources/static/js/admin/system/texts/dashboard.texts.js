(function () {
    'use strict';

    dashboard.texts = dashboard.texts || {};

    let quill;
    let load_text;
    let changes;
    let $active_site;
    let $active_code;

    dashboard.texts.init = function () {

        load_text = true;
        changes = false;

        $active_site = $("#active_site");
        $active_code = $("#active_code");

        $("#av_texts").select2({
            placeholder: 'Επιλέξτε κείμενο από τη διαθέσιμη λίστα',
            allowClear: true
        });

        quill = new Quill('#quill-container', {
            modules: {
                toolbar: '#toolbar-container'
            },
            placeholder: 'Επιλέξτε ένα από τα Διαθέσιμα Κείμενα για επεξεργασία...',
            theme: 'snow'
        });

        // Enable all tooltips
        $('[data-toggle="tooltip"]').tooltip();

        RegisterEvents();
    };

    function RegisterEvents() {

        let $av_texts_s2 = $("#av_texts");
        quill.on('text-change', function() {
            if (load_text === false) {
                changes = true;
                $("#addOrUpdateText").prop("disabled", false);
            }
            else {
                changes = false;
                load_text = false;
            }
        });

        $av_texts_s2.on("select2:selecting", function(event){
            let data = event.params.args.data;
            if (changes) {
                let msg = '<div class="p-2" style="font-weight: 500;background-color: #fdf59a">ΠΡΟΣΟΧΗ<br/>';
                msg += 'Έχετε προβεί σε τροποποιήσεις στο κείμενο, οι οποίες δεν έχουν αποθηκευτεί! Αν συνεχίσετε, οι αλλαγές θα χαθούν...</div>';
                msg += '<div style="font-weight: bold" class="text-high-emphasis mt-2">Είστε σίγουρος; Πατήστε "Όχι" για ακύρωση της ενέργειας και επιστροφή στο επιλεγμένο κείμενο</div>';

                alertify.confirm('<i style="color: red" class="fas fa-exclamation-triangle"></i> Τροποποίηση κειμένου', msg,
                    function () {
                        $("#av_texts").val(data.id).trigger("change");
                        loadText(data.id);
                    },
                    function () {
                    }).set('labels', {ok: 'Ναί!', cancel: 'Όχι'});
            }
            else {
                loadText(data.id);
            }
        });

        $av_texts_s2.on("select2:clear", function(e){
            quill.root.innerHTML = "";
            $("#edit_site_title").html("");
            $("#edit_doc_title").html("...");
            changes = false;
            $("#addOrUpdateText").prop("disabled",true);
        });

        $("#addOrUpdateText").on("click", function() {

            let active_site      = $active_site.val();
            let active_code      = $active_code.val();

            if (active_site !== null && active_site !== "" && active_code !== null && active_code !== "") {
                let quillHtml = quill.root.innerHTML.trim();
                postUpdate(active_site, active_code,quillHtml);
            }
        });

        $('#about_modal').on('show.coreui.modal', function (e) {
             let contentHTML = fetchHTMLBySiteAndCodeFromDB("admin","about");
        })
    }

    function loadText(text_id) {
        quill.root.innerHTML = "";
        if (text_id !== "") {
            $("#edit_site_title").html("παρακαλώ περιμένετε...");
            $("#edit_doc_title").html("");
            
            let site = text_id.split("-")[0];
            let code = text_id.split("-")[1];

            $active_site.val(site);
            $active_code.val(code);

            fetchTextBySiteAndCodeFromDB($(this), site, code);
        }
        else {
            $("#edit_site_title").html("");
            $("#edit_doc_title").html("...");
        }
    }
    function fetchTextBySiteAndCodeFromDB(element, site, code) {
        $.ajax({
            type:        "GET",
            url: 		  dashboard.siteurl + '/api/v1/text/' + site + '/code/' + code,
            contentType: "application/json; charset=utf-8",
            async:		  true,
            success: function(data){

                    let color;
                    let icon;
                    let site_title = $("#site-" + site).attr("label");

                    if (data.site === "admin") {
                        color = "#006A9B";
                        //icon = '<span class="icon-main-menu-contentm mx-2" style="color: #006A9B;font-size: 1.2em"></span>';
                    }
                    else if (data.site === "vod") {
                        color = "darkgreen";
                       // icon = '<span class="icon-on-demand mx-2" style="color: #006A9B"></span>';
                    }
                    else if (data.site === "live") {
                        color = "red";
                       // icon = '<span class="icon-live-lecture mx-2" style="color: red"></span>';
                    }
                    else {
                        color = "darkorange";
                       // icon = '<span class="fas fa-circle mx-2" style="color: darkorange"></span>';
                    }

                    $("#edit_site_title").html(site_title);
                    $("#edit_doc_title").css("color" , color).html("<i class=\"fas fa-angle-double-right mx-2\"></i>"  + data.title);

                    quill.root.innerHTML = data.content;
                    load_text = true;
                    $("#addOrUpdateText").prop("disabled", true);
            },
            error: function ()  {
                alertify.alert('Error-FETCH-TEXT');
            }
        });
    }

    function postUpdate(site, code, content) {
        $.ajax({
            type:        "POST",
            url: 		  dashboard.siteurl + '/api/v1/text/save/' + site + '/code/' + code,
            contentType: "application/json; charset=utf-8",
            data: 		  content,
            async:		  true,
            success: function(){
                alertify.notify("Το κείμενο αποθηκεύτηκε με επιτυχία", "success");
                changes = false;
                $("#addOrUpdateText").prop("disabled",true);
            },
            error: function ()  {
                alertify.alert('Error-Update-Message');
            }
        });
    }

    function fetchHTMLBySiteAndCodeFromDB(site, code) {
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
                alertify.alert('Error-FETCH-TEXT');
            }
        });
    }
})();