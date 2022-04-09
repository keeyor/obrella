(function () {
    'use strict';

    dashboard.parousiasi = dashboard.parousiasi || {};

    let $presentation_status;
    let MEDIA_FOLDER;
    let inclMultimedia;
    let nocs;

    let $body;
    let $presentation_container;
    let $uploadLog;
    let $dropArea;
    let $browseButton;
    let $cancelButton;
    let $statusMessage;
    let $statusAnalMessage;
    let $progressBar;
    let $dropsq;
    
    let post_action;
    let post_data;

    let postProcessingUrl;
    let analyzeProcessingUrl;

    let resourceId;
    let upload_container;

    const status_interval = 500;
    let ajaxRefreshInterval;

    let $carouselSlides;
    let carouselSlides;

    let fileUploader;

    let mode;

    dashboard.parousiasi.init = function () {

        $presentation_container = $('#presentation-container');
        //let controlHtml = setControlHtml();
        //$presentation_container.append(controlHtml);

        inclMultimedia       = $("#inclMultimedia").val();
        if (inclMultimedia !== "1") {
            $presentation_container.find("#pr_syncPp").addClass("disabled");
        }

        $body = $("body");
        $uploadLog            = $presentation_container.find("#pr_uploadLog");
        $dropArea             = $presentation_container.find("#pr_dropArea");
        $browseButton         = $presentation_container.find("#pr_browseButton");
        $cancelButton         = $presentation_container.find("#pr_cancelButton");
        $statusMessage        = $presentation_container.find("#pr_statusMessage");
        $statusAnalMessage    = $presentation_container.find("#pr_statusAnalMessage");
        $progressBar          = $presentation_container.find("#pr_progressBar");
        $dropsq               = $presentation_container.find(".dropsq");

        postProcessingUrl     = dashboard.siteUrl + "/admin/multimediaUpload";
        analyzeProcessingUrl  = dashboard.siteUrl + "/admin/startSlidesProcessing";

        MEDIA_FOLDER          = $("#media_folder").val();

        upload_container      = "pr_uploadContainer";
        $carouselSlides       = $('#carouselSlides');
        carouselSlides        = "#carouselSlides";
        $presentation_status  = $("#status_pre");
        mode                  = "details";           //default show mode

        dashboard.parousiasi.uploadLog("hide");


        let inclPresentation = $("#inclPresentation").val();
        if (inclPresentation === "1") {
            dashboard.parousiasi.setMode("slides");
            dashboard.parousiasi.getResourceSlides(dashboard.rid,"details");
        }
        else {
            if (dashboard.rid !== "") {
                dashboard.parousiasi.setMode("upload");
                dashboard.parousiasi.initUpload(dashboard.rid,"_NONE","");
            }
            else {
                dashboard.parousiasi.setMode("disabled");
            }
        }
    };

    dashboard.parousiasi.setMode = function (display) {
        if (display === "upload") {
            $presentation_container.find("div.pr_uploadWarning").hide();
            $presentation_container.find("div.pr_outerUploadContainer").show();
            $presentation_container.find("div.pr_slideContainer").hide();
        }
        else if (display === "disabled") {
            $presentation_container.find("div.pr_uploadWarning" ).show();
            $presentation_container.find("div.pr_outerUploadContainer" ).hide();
            $presentation_container.find("div.pr_slideContainer").hide();
        }
        else if (display === "slides") {
            $presentation_container.find("div.pr_uploadWarning" ).hide();
            $presentation_container.find("div.pr_outerUploadContainer" ).hide();
            $presentation_container.find("div.pr_slideContainer").show();
        }
    };
    dashboard.parousiasi.initUpload = function (id, action, data) {
        resourceId = id;
        post_action = action;
        post_data   = data;
        fileUploader = InitializePPUploadControl(resourceId);
    };
    dashboard.parousiasi.getResourceSlides = function(id ,mode) {
        resourceId = id;
        $.ajax({
            url: dashboard.siteUrl +  '/api/v1/resource/slides/' + id,
            cache: false
        })
            .done(function( data ) {
                dashboard.parousiasi.showPresentation(id,data, mode);
            });
    };
    dashboard.parousiasi.uploadLog = function(display) {
        if (display === "show") {
            $uploadLog.show();
        }
        else if (display === "hide") {
            $uploadLog.hide();
        }
    };
    dashboard.parousiasi.started = function () {

        $progressBar.css('width', '0%').attr('aria-valuenow',0);
        setStatus("Upload Started");
        $dropArea.hide();
        $browseButton.prop("disabled", true);
        $cancelButton.prop("disabled", false);
    };
    dashboard.parousiasi.showPresentation = function (id, Slides, display_mode) {

        mode = display_mode;
        if (Slides) {
            $("#pr_slideCount").html('<i class="fab fa-slideshare"></i> &centerdot;' + Slides.length + " slides");

            if (display_mode === "details") {
                let innerHtml = imageGrid(Slides, dashboard.baseUrl_pp  + MEDIA_FOLDER + "/");
                $("#pr_imageGrid").html(innerHtml);
            }
            else if (display_mode === "compact") {
                let innerHtml = imageGridCompact(Slides, dashboard.baseUrl_pp  + MEDIA_FOLDER + "/");
                $("#pr_imageGrid").html(innerHtml);

                $carouselSlides.carousel({
                    interval: false
                   // ride: "false" //?
                });
                $('#carouselSlides').on("slide.coreui.carousel", function (e) {
                    $("#slideNo").html(parseInt(e.to) + 1);
                });
            }
        }

        $("#pr_slideContainer").show();

        $(document).on('click','.delete_slide_button', function(e) {
            e.preventDefault();
            OnSlideDelete(this);
        });
        $(document).on('click', '.edit_slide_button', function(e) {
            e.preventDefault();
            OnSlideEdit(this);
        });
        $(document).on('click', '.view_slide', function() {
            OnSlideView(this);
        });
        $(document).on('click','.hide_display_row' , function(e) {
            e.preventDefault();
            let ref_id = $(this).data("row");
            $('#' + ref_id ).hide();
        });

        $(".prSlideActions").on("click", "button", function(event) {

            let button_id = $(this).attr("id");

            if (button_id === "pr_removePp") {
                let message = {msg: "remove slides file!"};
                dashboard.broker.trigger('remove.ppEdit', [message])
            }
            if (button_id === "pr_compactPp") {
                if (mode !== "compact") {
                    mode = "compact";
                    $(this).addClass("btn-info");
                    $("#pr_detailsPp").removeClass("btn-info");

                    dashboard.parousiasi.getResourceSlides(id,"compact");
                }
            }
            if (button_id === "pr_detailsPp") {
                if (mode !== "details") {
                    mode = "details";
                    $(this).addClass("btn-info");
                    $("#pr_compactPp").removeClass("btn-info");
                    dashboard.parousiasi.getResourceSlides(id, "details");
                }
            }
            if (button_id === "pr_cancelButton") {
                fileUploader.stop();
                ClearPPUploadQueue(fileUploader,"cancel");
            }
            event.preventDefault();
        });
    };

    function  log() {
        let str = "";
        plupload.each(arguments, function(arg) {
            let row = "";

            if (typeof(arg) !== "string") {
                plupload.each(arg, function(value, key) {
                    // Convert items in File objects to human readable form
                    if (arg instanceof plupload.File) {
                        // Convert status to human readable
                        switch (value) {
                            case plupload.QUEUED:
                                value = 'QUEUED';
                                break;
                            case plupload.UPLOADING:
                                value = 'UPLOADING';
                                break;
                            case plupload.FAILED:
                                value = 'FAILED';
                                break;
                            case plupload.DONE:
                                value = 'DONE';
                                break;
                        }
                    }
                    if (typeof(value) !== "function") {
                        row += (row ? ', ' : '') + key + '=' + value;
                    }
                });
                str += row + " ";
            } else {
                str += arg + " ";
            }
        });

        $uploadLog.append(str + "\n");
        $uploadLog.scrollTop($uploadLog[0].scrollHeight);
    }
    function OnSlideEdit(element){

        let parent_row =  element.parentNode.parentNode;
        let img_node = parent_row.getElementsByTagName('img')[0];

        let img_pos_w = $(element).data('index');
        let res = img_pos_w.split(":");
        let img_pos = parseInt(res[1]);

        let title = language.u61;
        let message = language.u62;

        alertify.prompt()
            .setting ({
                'title'  : title,
                'message': message,
                'value'  : img_node.title,
                'onok'   : function(evt, value){ updateSlideTitle(img_pos, value);}
            }).show();
    }
    function OnSlideView(element) {
        let img_url =  $(element).data("url");
        InflateSlide(element, img_url);
    }
    function InflateSlide(element, img_url) {

        if (parseInt(nocs)>0)
        {
            let sfloat = nocs / 4;
            let thumbs = sfloat - (sfloat % 1);

            let init_imgUrl = dashboard.siteUrl + "/public/images/dotWhite.png";

            let parent_row = $(element).closest(".row");
            let next_display_row = parent_row.next(".display_row");
            let display_row_index = $(next_display_row[0]).data("index");

            for (let dt=0;dt<thumbs; dt++) {
                if (dt !== display_row_index ) {
                    $("#display_row" + dt).hide();
                }
            }

            let $display_pop = $("#pop" + display_row_index);
            let $display_row = $("#display_row" + display_row_index );
            let prev_imgUrl = $display_pop.prop("src");

            if (prev_imgUrl === img_url) {
                $display_row.hide();
                $display_pop.attr("src", init_imgUrl);
            }
            else {
                $display_row.show();
                $display_pop.attr("src", img_url);
            }

        } //if

    }
    function updateSlideTitle(position, title) {

        jQuery.support.cors = true;
        var json_string = '{"position":"' + position + '",' +
            '"title":"' + title + '"}';

        $.ajax({
            url: dashboard.siteUrl + "/api/v1/resource/update_slide_title/" + dashboard.rid + "/",
            type: "POST",
            data: json_string,
            contentType: "application/json; charset=utf-8",
            dataType: 'html',
            crossDomain: true,
            async: false,    	//Cross-domain requests and dataType: "jsonp" requests do not support synchronous operation
            cache: false,    	//This will force requested pages not to be cached by the browser
            processData: false,  //To avoid making query String instead of JSON
            success: function () {
                let div_node = document.getElementById("cnt:" + position);
                let img_node = div_node.getElementsByTagName('img')[0];
                img_node.setAttribute('title', title);
                //If compact mode
                img_node.innerHTML = (position + 1) + "." + title;
                let title_node = document.getElementById("tlt:" + position);
                let item_title_compact = title;
                if (title.length > 32) {
                    item_title_compact = title.substring(0, 29) + '...';
                }
                title_node.innerHTML = '<small>' + (position+1) + '.' + item_title_compact + '</small>';
                alertify.success("Ο τίτλος της διαφάνειας ενημερώθηκε");
            },
            error: function (jqXHR, textStatus, errorThrown) {
                alert('error trapped in error: UpdateSlideTitle');
                alert('msg = ' + errorThrown);
            }
        });
    }
    function OnSlideDelete(element) {

        let $msgline = $('#msgline');

        let sfloat = nocs / 4;
        let thumbs = sfloat - (sfloat % 1);

        if (nocs > 1){
            let parent_row =  element.parentNode.parentNode;
            let img_node = parent_row.getElementsByTagName('img')[0];

            let img_url =  $(img_node).data('id');
            let img_index = parent_row.id;

            alertify.confirm(language.u46, language.u30, function () {
                //close open pops
                for (let dt=0;dt<thumbs; dt++) {
                    $("#display_row" + dt).hide();
                }
                //
                DeleteSlideFromDb(img_url,img_index);
                nocs = parseInt(nocs -1);
                $('#nocs').val(nocs);
                if (nocs === 0) {
                    nocs = "-1";
                    $msgline.html(language.u54);
                }
                else {
                    $msgline.html(language.u55 + nocs + language.u56);
                }
            }, function(){});
        }
        else {
            alertify.notify(language.u57+ ". " + language.u58, "danger");
        }
    }

    function DeleteSlideFromDb(slideId,index) {

        jQuery.support.cors = true;

        var json_string = '{"slideId":"'  + slideId + '"}';

        $.ajax({
            url 		: dashboard.siteUrl + "/api/v1/resource/delete_slide/" + dashboard.rid + "/",
            type 		: "POST",
            data 		: json_string,
            contentType : "application/json; charset=utf-8",
            dataType 	: 'html',
            crossDomain : true,
            async: false,    	//Cross-domain requests and dataType: "jsonp" requests do not support synchronous operation
            cache: false,    	//This will force requested pages not to be cached by the browser
            processData:false,  //To avoid making query String instead of JSON
            success : function() {
                DeleteSlideFromUi(index);
            },
            error: function (jqXHR, textStatus, errorThrown)  {
                alert('error trapped in error: DeleteSlideFromDb');
                alert('msg = ' + errorThrown);
            }
        });

    }

    function DeleteSlideFromUi(index) {

        var mv_img,mv_title,mv_time;
        var from_thesi;

        from_thesi = document.getElementById(index);

        from_thesi.removeChild(from_thesi.getElementsByTagName('img')[0]);
        from_thesi.removeChild(from_thesi.getElementsByTagName('div')[0]);
        from_thesi.removeChild(from_thesi.getElementsByClassName('slidetag')[0]);


        let res = from_thesi.id.split(":");
        let startAt = parseInt(res[1]); // id dragged

        let endAt = 99;

        startAt = parseInt(startAt+1);
        for (var thesi= startAt; thesi<=endAt;thesi++ )
        {
            let mv_node = document.getElementById("cnt:"+thesi);
            if (mv_node !== null) {
                mv_img = mv_node.getElementsByTagName('img')[0];
                mv_title = mv_node.getElementsByTagName('div')[0];
                mv_time  = mv_node.getElementsByClassName('slidetag')[0];
                var par = parseInt(thesi)-1;
                let new_pos_node = document.getElementById("cnt:"+par);
                new_pos_node.appendChild(mv_title);
                new_pos_node.appendChild(mv_img);
                new_pos_node.appendChild(mv_time);
            }
            else {break;}
        }
        let last_index = thesi-1;

        let row_parent = document.getElementById("cnt:" + last_index).parentNode;
        let c = row_parent.childNodes.length;
        for (var ch=0; ch<=c-1;ch++) {
            if (row_parent.childNodes[ch].id === "cnt:" + last_index) {
                row_parent.removeChild(row_parent.childNodes[ch]);
                break;
            }
        }
        $("#pr_slideCount").html('<i class="fab fa-slideshare"></i> &centerdot;' + last_index + " slides");
        alertify.success("Η Διαφάνεια διαγράφηκε");

    }
    function InitializePPUploadControl(id) {

        InitUploadControls();

        let resourceId = id;
        let fileList = "pr_fileList";
        let post_processing_url = postProcessingUrl;
        let $upload_container = document.getElementById(upload_container);
        let m_folder = MEDIA_FOLDER;
        let ppFileUploader = new plupload.Uploader({
            runtimes : 'html5,flash,silverlight,html4',
            container : $upload_container,
            url :  post_processing_url,
            chunk_size : '1mb',
            unique_names : true,
            drop_element : "pr_dropArea",
            browse_button : "pr_browseButton",
            multipart_params : {
                m_folder : MEDIA_FOLDER
            },

            filters : {
                max_file_size : '100mb',
                // Specify what files to browse for
                mime_types: [
                    {title : "Image files", extensions : "jpg"},
                    {title : "Zip files", extensions : "zip"},
                    {title : "PDF files", extensions : "pdf"},
                    {title : "Powerpoint files", extensions : "ppt,pptx"}
                ]
            },

            // Flash settings
            flash_swf_url : dashboard.siteUrl + '/lib/plupload-2.3.6/js/Moxie.swf',

            // Silverlight settings
            silverlight_xap_url : dashboard.siteUrl + '/lib/plupload-2.3.6/js/Moxie.xap',

            // PreInit events, bound before any internal events
            preinit : {
                Init: function(up, info) {
                    log('[Init]', 'Info:', info, 'Features:', up.features);
                    //setStatus("'Ετοιμος!");
                },

                UploadFile: function(up, file) {
                    log('[UploadFile]', file);
                    setStatus("Μεταφόρτωση αρχείου: " + file.name);
                }
            },

            // Post init events, bound after the internal events
            init : {
                PostInit: function() {
                    // Called after initialization is finished and internal event handlers bound
                    log('[PostInit]');
                    document.getElementById(fileList).innerHTML = '';
                },

                Browse: function() {
                    // Called when file picker is clicked
                    log('[Browse]');
                },

                Refresh: function() {
                    // Called when the position or dimensions of the picker change
                    log('[Refresh]');
                },

                StateChanged: function(up) {
                    // Called when the state of the queue is changed
                    log('[StateChanged]', up.state === plupload.STARTED ? "STARTED" : "STOPPED");
                    let message = {msg: "Upload State Change!", value: up.state};
                    dashboard.broker.trigger('pp_upload_state.changed', [message]);
                },

                QueueChanged: function(up) {
                    // Called when queue is changed by adding or removing files
                    log('[QueueChanged]:' + up.total.queued);
                },

                OptionChanged: function(up, name, value, oldValue) {
                    // Called when one of the configuration options is changed
                    log('[OptionChanged]', 'Option Name: ', name, 'Value: ', value, 'Old Value: ', oldValue);
                },

                BeforeUpload: function(up, file) {
                    // Called right before the upload for a given file starts, can be used to cancel it if required
                    log('[BeforeUpload]', 'File: ', file);
                    $("#presentation_progress_panel").show();
                },

                UploadProgress: function(up, file) {
                    // Called while file is being uploaded
                    log('[UploadProgress]', 'File:', file, "Total:", up.total);
                    $progressBar.css('width', file.percent + '%').attr('aria-valuenow',file.percent);
                    setStatus("Μεταφόρτωση αρχείου: " + file.name + " - Ποσοστό ολοκλήρωσης: " + file.percent + "%");
                },

                FileFiltered: function(up, file) {
                    // Called when file successfully files all the filters
                    log('[FileFiltered]', 'File:', file);
                },

                /**
                 * @return {boolean}
                 */
                FilesAdded: function(up, files) {
                    // Called when files are added to queue
                    log('[FilesAdded]:' + up.total.queued );
                    let action = post_action;
                    log('[Action]:' + post_action );
                    let filename = files[0].name;
                    let fileExtension = filename.substr(filename.lastIndexOf('.')+1);

                    if (action === "REPLACE_IMAGE") {
                        if (up.total.queued === "1" && fileExtension.toLowerCase() === "jpg") {
                            ppFileUploader.start();
                            return false;
                        }
                        else {
                            post_action = "_NONE";                  //$post_action.val("_NONE");
                            post_data = "";                         //$post_action_data.val("");
                            ClearPPUploadQueue(ppFileUploader);
                        }
                    }
                    else if (action === "IMPORT_PRESENTATION") {
                        if (up.total.queued === "1" && fileExtension.toLowerCase() === "zip") {
                            ppFileUploader.start();
                            return false;
                        }
                        else {
                            post_action = "_NONE";              //$post_action.val("_NONE");
                            post_data = "";                     //$post_action_data.val("");
                            ClearPPUploadQueue(ppFileUploader);
                        }
                    }
                    else if (action === "_NONE") {
                        ppFileUploader.start();
                        return false;
                    }
                },

                FilesRemoved: function(up) {
                    // Called when files are removed from queue
                    log('[FilesRemoved]:' + up.total.queued);
                },

                FileUploaded: function(up, file, info) {
                    // Called when file has finished uploading
                    log('[FileUploaded] File:', file, "Info:", info);
                    if ( (info.response.trim() === "UPLOAD_OK")) {
                        //let ext = file.name.split('.').pop();
                        //pp_uploaded_filename = file.id + "." + ext;
                    }
                    else {
                        up.stop();
                        setStatus(info.response.trim());
                    }
                },
                ChunkUploaded: function(up, file, info) {
                    log('[ChunkUploaded] File:', file, "Info:", info);
                },

                UploadComplete: function(up) {
                    // Called when all files are either uploaded or failed
                    log('[UploadComplete]');
                    setStatus('Finished!');
                    if (up.total.uploaded > 0) {
                        //let post_action = $post_action.val();
                        //let action_data = $post_action_data.val();
                        ClearPPUploadQueue(ppFileUploader, "finished");
                        log('[Process] Folder:', m_folder, " Id:", resourceId, " action:", post_action, " data:", post_data);
                        doAjaxPPHandle( m_folder,resourceId,post_action,post_data);
                    }
                },

                Destroy: function() {
                    // Called when uploader is destroyed
                    log('[Destroy] ');
                },

                Error: function(up, args) {
                    // Called when error occurs
                    log('[Error] ', args);
                    ClearPPUploadQueue(ppFileUploader, "error");
                    setStatus("Πρόβλημα στο upload");
                }
            }
        });
        ppFileUploader.splice();
        ppFileUploader.init();
        return ppFileUploader;
    }
    function doAjaxPPHandle(m_folder,lecture_id,action,adata) {

        let analyze_processing_url = analyzeProcessingUrl;
        setIsRunning(true);
        //console.log("analyze:" + analyze_processing_url);
        let params = {
            folder	    :	m_folder,
            id          :   lecture_id,
            action 		:	action,
            actionData	:	adata,
            module      :   ""
        };
        $.ajax({
            type:        "POST",
            url: 		  analyze_processing_url,
            contentType: "application/json; charset=utf-8",
            data: 		  JSON.stringify(params),
            async:		  true,
            success: function(response){
                if (response.status === "SUCCESS") {
                    $dropArea.hide();
                    console.log("analysis finished:" + response.source);
                    let message = {msg: "Slides Analysis Succeded!", id: lecture_id, slides: response.result};
                    dashboard.broker.trigger('pp_upload_state.finished', [message]);
                    setStatus("");
                    $presentation_status.val("1");
                }
                else {
                    console.log("analysis error: SOURCE: " + response.source + " MSG: " +response.message);
                    if (response.message !== null) {
                        setStatus(response.status + ":" + response.message);
                    }
                    else {
                        setStatus(response.status);
                    }
                }
            },
            error: function (jqXHR, textStatus, errorThrown)  {
                setStatus(errorThrown);
            }
        });
    }
    function InitUploadControls(lecture_id) {

        resourceId = lecture_id;

        $dropArea.show();
        $browseButton.prop("disabled", false);
        $cancelButton.prop("disabled", true);

        $body.bind("dragenter", function(e){
            $dropsq.addClass("draggingFile");
            e.stopPropagation();
            e.preventDefault();
        });
        $body.bind("dragleave", function(){
            $dropsq.removeClass("draggingFile");
        });
        $body.bind("dragover", function(e){
            $dropsq.addClass("draggingFile");
            e.stopPropagation();
            e.preventDefault();
        });
        $body.bind("drop", function(e){
            e.stopPropagation();
            e.preventDefault();
            $dropsq.removeClass("draggingFile");
        });
    }

    function ClearPPUploadQueue(uploader, finito){

        let len = uploader.files.length;
        for (let i=len-1; i>=0;i--) {
            uploader.splice(i,1);
        }
        resetUploadArea(finito);
    }
    function resetUploadArea(finito) {
        
        $dropArea.show();

        if (finito === "finished") {
            setStatus("Η Μεταφόρτωση Ολοκληρώθηκε! ");
        }
        else if (finito === "cancel") {
            $progressBar.css('width', '0%').attr('aria-valuenow', 0);
            setStatus("Η μεταφόρτωση ακυρώθηκε από το χρήστη. Επιλέξτε αρχείο'!");
        }
        else {
            $progressBar.css('width', '0%').attr('aria-valuenow', 0);
            setStatus("Η μεταφόρτωση διεκόπη. Επιλέξτε αρχείο!");
        }
    }
    function setIsRunning(data) {
        if (data === true) {
            //console.log("Import process Is running:" + data);
            startStatusDaemon(status_interval);
        }
        else {
            //console.log("Import process NOT running:" + data);
            stopStatusDaemon();
        }
    }
    function startStatusDaemon(interval){
        ajaxRefreshInterval = setInterval( function () {
            getStatus();
        }, interval );
    }
    function stopStatusDaemon() {
        clearInterval(ajaxRefreshInterval);
    }
    function getStatus() {

        let analyze_processing_url = dashboard.siteUrl + "/status/process_status";

        $.ajax({
            type:        "GET",
            url: 		  analyze_processing_url,
            async:		  true,
            success: function(response){
               setAnalStatus(response);
                if (response.startsWith("Failed") || response.startsWith("Canceled")) {
                    setIsRunning(false);
                    $("#pr_progressBar_anal").css('width', '0%').attr('aria-valuenow',0);
                    $("#pr_progressBar_anal").html(0 + "%");
                }
                if (response.startsWith("Finished")) {
                    setIsRunning(false);
                    $("#pr_progressBar_anal").css('width', '100%').attr('aria-valuenow',100);
                    $("#pr_progressBar_anal").html(100 + "%");
                    $browseButton.prop("disabled", false);
                    $cancelButton.prop("disabled", true);
                }
                if (response.startsWith("Ανάγνωση")) {
                    $("#pr_progressBar_anal").css('width', '30%').attr('aria-valuenow',35);
                    $("#pr_progressBar_anal").html("Ανάγνωση Αρχείου: παρακαλώ περιμένετε!");
                }
                if (response.startsWith("Δημιουργία")) {
                    $("#pr_progressBar_anal").css('width', '70%').attr('aria-valuenow',70);
                    $("#pr_progressBar_anal").html("Δημιουργία Μικρογραφιών: παρακαλώ περιμένετε!");
                }
            },
            error: function (jqXHR, textStatus, errorThrown)  {
                setAnalStatus(errorThrown);
                setIsRunning(false);
                $browseButton.prop("disabled", false);
                $cancelButton.prop("disabled", true);
            }
        });
    }
    function setStatus(status_msg) {
        $statusMessage.html(status_msg);
    }
    function setAnalStatus(status_msg) {
        $statusAnalMessage.html(status_msg);
    }
    function setControlHtml() {

        return '<div id="pr_outerUploadContainer" class="form-group row pr_outerUploadContainer">'  +
                                     '<label for="pr_dropArea" class="col-sm-3 col-form-label"></label>' +
                                     '<div class="col-sm-9">' +
                                             '<div class="row uploadWrapper p-3">' +
                                                 '<div id="pr_dropArea" class="dropsq text-muted">Για να "ανεβάσετε" ένα αρχείο, σύρτε το εδώ ή κάντε κλίκ στο κουμπί \'Επιλέξτε Αρχείο\'</div>' +
                                             '</div>' +
                                             '<div class="progress" style="margin-right: 5px">' +
                                                    '<div class="progress-bar progress-bar-success" id="pr_progressBar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100"></div>' +
                                             '</div>' +
                                             '<div><span style="font-style: italic">Κατάσταση Μεταφόρτωσης: </span>' +
                                                   '<span id="pr_statusMessage" style="font-size:0.9em"></span>' +
                                             '</div>' +
                                             '<div><span style="font-style: italic">Κατάσταση Ανάλυση: </span>' +
                                                    '<span id="pr_statusAnalMessage" style="font-size:0.9em"></span>' +
                                             '</div>' +
                                             '<div>' +
                                             '<div class="form-group" id="pr_uploadContainer"></div>' +
                                             '<div id="pr_fileList"><small>Error: No runtime found!</small></div>' +
                                             '<button id="pr_browseButton" type="button" class="btn btn-brand btn-instagram btn-sm mr-1">' +
                                             '<i class="far fa-file-video"></i><span> Επιλέξτε Αρχείο</span>' +
                                             '</button>' +
                                             '<button id="pr_cancelButton" type="button" class="btn btn-brand btn-danger btn-sm"><i class="fas fa-ban"></i><span> Ακύρωση</span></button>' +
                                             '</div>' +
                                              '<pre id="pr_uploadLog" style="height: 300px; overflow: auto">upload -- log</pre>' +
                                             '<div class="mt-2" style="font-size: 0.9em; font-style: italic"><i class="fas fa-info"></i>' +
                                             ' Αποδεκτά αρχεία: Microsoft Powerpoint (PPT & PPTX), Adobe Acrobat (PDF), Συμπιεσμένο αρχείο ZIP με εικόνες JPG, Εικόνες JPG (πολλαπλές),' +
                                             ' Συμπιεσμένο αρχείο ZIP με εικόνες JPG και πληροφορίες συγχρονισμού (SlideRecorder)' +
                                             '</div>' +
                                     '</div>' +
                '</div>' +
               '<div id="pr_slideContainer" class="pr_slideContainer">' +
                            '<div class="prSlideActions p-0 mb-3" style="border-bottom:none">' +
                                '<span class="float-end">' +
                                    '<button id="pr_compactPp" class="btn btn-sm text-left me-1" title="Compact View"><i class="far fa-square"></i> Carousel</button>' +
                                    '<button id="pr_detailsPp" class="btn btn-sm text-left btn-info" title="Details View"><i class="fab fa-buromobelexperte"></i> Grid</button>' +
                                    '<button id="pr_exportPp" title="Εξαγωγή Εικόνων - download" class="btn-success btn btn-sm"><i class="fas fa-download"></i></button>' +
                                    '<a  href="' + dashboard.siteUrl + '/admin/video-sync?id=' + dashboard.rid + '" role="button" id="pr_syncPp" class="btn-instagram btn btn-sm me-1 ms-1"><i class="fas fa-sync-alt"></i> Συγχρονισμός</a>' +
                                    '<button id="pr_removePp" class="btn-danger btn btn-sm"><i class="fas fa-trash"></i> Διαγραφή</button>' +
                                '</span>' +
                            '</div>' +
                            '<div class="card-body text-left" id="slides-card">' +
                                '<span id="pr_imageGrid"></span>' +
                                '<footer class="blockquote-footer text-right">' +
                                    '<span id="pr_slideCount"></span> &centerdot;' +
                                '</footer>' +
                            '</div>' +
               '</div>';
    }
    function imageGrid(slides, basepath) {
        let html="";
        nocs = slides.length;
        let sfloat = nocs / 4;
        let thumbs = sfloat - (sfloat % 1);
        let thumb_rem = nocs % 4;
        let runLast=0;

        for (let jk=0; jk<=thumbs; jk++) {

            let rowId = "row:" + jk;

            html = html + '<div class="row"  id="' + rowId + '" ondragover="return false;" ondrop="return false;" style="margin-bottom:0;"> ';
            let cols=3;
            runLast=1;
            if (jk === thumbs) {
                if (thumb_rem<1)
                {runLast=0;}
                else
                {
                    runLast=1;
                    cols = nocs - (thumbs*4)-1;
                }
            }
            if (runLast===1) {
                for (let jk_row=0;jk_row<=cols;jk_row++) {
                    let item = slides[(4*jk)+jk_row];
                    let bordercolor = "#6CB33E";
                    let _time = item.time;
                    let _ftime;

                    let header_html = "";
                    let numbering_html = "";

                    let item_title = item.title;
                    let item_url = item.url;

                    let item_title_compact = item_title;
                    if (item_title === "") {
                        item_title_compact = "- - -";
                    }

                    if (_time=== "-1") {
                        bordercolor = "silver";
                        _ftime = "--- : --- : ---";
                        numbering_html = '<span class="numbering" id="num:' + parseInt((4*jk)+jk_row) + '">' + parseInt((4*jk)+jk_row+1) + '. </span>';
                        header_html =  '<div id="tlt:' + parseInt((4*jk)+jk_row) + '" class="slide_header text-truncate">';
                        header_html +=  '<small>' + numbering_html + item_title_compact + '</small>';
                        header_html += '</div>';
                    }
                    else {
                        _ftime = secondsTimeSpanToHMS(_time);
                        numbering_html = '<span class="numbering" id="num:' + parseInt((4*jk)+jk_row) + '">' + parseInt((4*jk)+jk_row+1) + '. </span>';
                        header_html =  '<div id="tlt:' + parseInt((4*jk)+jk_row) + '" class="slide_header_sync text-truncate">';
                        header_html +=  '<small>' + numbering_html + item_title_compact + '</small>';
                        header_html += '</div>';
                    }

                    html= html + '<div style="font-size: 1.0em" class="col-lg-4 col-md-3 slide_frame mb-2" id="cnt:' + parseInt((4*jk)+jk_row) + '"> ';
                    html= html + header_html;
                    html= html + '<img id="img:' + parseInt((4*jk)+jk_row) + '" class="view_slide draggable_image img-fluid" style="cursor: zoom-in; border:1px solid ' + bordercolor + '" ';
                    html= html + 'ondrop="drop(event)" draggable="true" ondragstart="drag(event)"';
                    html= html + 'alt="Slide.' + parseInt((4*jk)+jk_row) + '"';
                    html= html + 'data-index="' + parseInt((4*jk)+jk_row+1) + '"';
                    html= html + 'title="' + item_title + '"';
                    html= html + 'data-id="' + item_url +'"';
                    html= html + 'data-sync="' + _ftime +'"';
                    html= html + 'data-url="' + basepath + 'slides/' + item_url + '"';
                    html= html + 'src="' +  basepath + 'slides/small/' + item_url + '"> ';

                    html= html + '<div class="slidetag mt-1" id="slide_footer:' + parseInt((4*jk)+jk_row) + '"> ';
                    html= html + '<span data-index="cnt:' + parseInt((4*jk)+jk_row) + '" class="delete_slide_button"> ';
                    html= html + ' <a style="color: #517fa4" title="Διαγραφή διαφάνειας" href="#" ><i class="fas fa-ban"></i></a> ';
                    html= html + '</span> ';
/*                    html= html + '<span data-index="cnt:' + parseInt((4*jk)+jk_row) + '" class="replace_slide_button" draggable="false"> ';
                    html= html + ' <a style="color: #517fa4" title="Αντικατάσταση διαφάνειας" href="#" ><i class="fas fa-xs  fa-sync-alt"></i></a> ';
                    html= html + '</span> ';*/
                    html= html + '<span data-index="cnt:' + parseInt((4*jk)+jk_row) + '" class="edit_slide_button" draggable="false"> ';
                    html= html + ' <a  style="color: #517fa4" title="Επεξεργασία τίτλου" href="#" ><i class="fas fa-edit"></i></a> ';
                    html= html + '</span> ';
                    html= html + '<span class="slide_sync_time pl-3"> ';
                    html= html + ' <a style="color:#39f" title="Χρόνος συγχρονισμός"><i class="far fa-clock"></i> ' + _ftime + '</a>';
                    html= html + '</span> ';
                    html= html + '</div>';

                    html+= '</div>';

                }

            }
            html = html + '</div>';

            let hide_row_id = "display_row" + jk;
            let pop_row = "pop" + jk;

            html = html + '<div class="row display_row" data-index="' + jk + '" style="display:none" id="' + hide_row_id + '">' +
                '<div class="col-sm-12 text-center inflated_image">' +
                '<span class="float-right"><a href="#" class="hide_display_row" data-row="' + hide_row_id + '"><i class="far fa-window-close"></i></a></span>' +
                '<img class="img-fluid " alt="pop" id="' + pop_row + '" src="' + dashboard.siteUrl +  '/public/images/dotWhite.png"/>' +
                '</div>' +

                '</div>';

        }
        return html;
    }
    function imageGridCompact(slides, basepath) {

        let  html="";
        nocs = slides.length;

        html = html + '<div class="row text-center">';
            html = html + '<div class="col"></div>';
            html = html + '<div class="col-6 text-medium-emphasis" id="slide-controls" style="align-items: center">';
            html = html + ' Slide  <span id="slideNo">1</span> of ' + nocs + '';
            html = html + '</div>';
            html = html + '<div class="col"></div>';
        html = html + '</div>';

        html = html + '<div class="row">';
        html = html + '<div class="col"></div>';
        html = html + '<div class="col-6" style="padding: 0">';

        html = html + '<div id="carouselSlides" class="slide carousel" data-coreui-ride="carousel">';
        html = html + '<div class="carousel-inner" id="slide-inner">';
        for (let i=0; i < nocs; i++) {
            let item = slides[i];
            let item_title = item.title;
            let item_url = item.url;

            let item_title_compact = item_title;
            if (item_title.length > 32) {
                item_title_compact = item_title.substring(0,29) + '...';
            }
            if (item_title === "") {
                item_title_compact = "- - -";
            }
            if (i ===0) {
                html = html + '<div class="carousel-item active">';
            }
            else {
                html = html + '<div class="carousel-item">';
            }
            html = html +'<img style="border: 1px solid #ccc" class="d-block w-100" src="' + basepath + 'slides/' + item_url + '" alt="slide">';
            html = html +'</div>';
        }
        html = html +'</div>';
        html = html +'</div>';

        html = html + '<button type="button" class="carousel-control-prev" data-coreui-target="#carouselSlides" data-coreui-slide="prev">';
        html = html + '<i style="color: #bf0063" class="fas fa-2x fa-angle-left"></i>';
        html = html + '<span class="sr-only">Previous</span>';
        html = html + '</button>';
        html = html +'<button type="button" class="carousel-control-next" data-coreui-target="#carouselSlides" data-coreui-slide="next">';
        html = html +'<i style="color: #bf0063" class="fas fa-2x fa-angle-right"></i>';
        html = html +'<span class="sr-only">Next</span>';
        html = html +'</button>';

        html = html +'</div>';
        html = html + '<div class="col"></div>';
        html = html +'</div>';
        return html;
    }

    function secondsTimeSpanToHMS(s){

        if (!isNumeric(s)) {
            let split_time = s.indexOf(":");
            if (split_time > -1)
                return s;
        }
        let h=Math.floor(s/3600);
        s-=h*3600;
        let m=Math.floor(s/60);
        s-=m*60;
        return(h<10?'0'+h:h)+":"+(m<10?'0'+m:m)+":"+(s<10?'0'+s:s);
    }
    function isNumeric(n) {
        return !isNaN(parseFloat(n)) && isFinite(n);
    }

})();
