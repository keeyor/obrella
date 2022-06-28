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
            $("#pr_exportPp").attr("href",dashboard.siteUrl + "/admin/download-presentation?id=" + dashboard.rid);
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

        $(".delete_slide_button").on('click', function(e) {

            if (nocs > 1) {
                let img_pos_w = $("#inflated_slide_number").val();
                let img_pos = parseInt(img_pos_w);
                let slide_id = $("#inflated_slide_id").val();
                alertify.confirm(language.u46, language.u30, function () {
                    DeleteSlideFromDb(slide_id, img_pos);
                }, function () {
                });
            }
            else {
                alertify.alert("Σφάλμα" , language.u57+ ". " + language.u58);
            }
            e.preventDefault();
        });

        $(".save_slide_button").on('click', function(e) {
            let img_pos_w = $("#inflated_slide_number").val();
            let img_pos = parseInt(img_pos_w);
            let slide_title = $("#_edit_slide_title").val();
            updateSlideTitle(img_pos,slide_title);
            e.preventDefault();
        });

        $("#inflate_next_slide").on('click', function(e) {
            let img_no  =  $("#inflated_slide_number").val();
            let next_img = parseInt(img_no) + 1;
            if (next_img < nocs) {
                $("#inflated_slide_number").val(next_img);
                let $next_element = $("#image_" + next_img);
                OnSlideView($next_element);
            }
            e.preventDefault();
        });

        $("#inflate_first_slide").on('click', function(e) {
            let prev_img = 0;
            $("#inflated_slide_number").val(prev_img);
            let $previous_element = $("#image_" + prev_img);
            OnSlideView($previous_element);

            e.preventDefault();
        });
        $("#inflate_last_slide").on('click', function(e) {
            let prev_img = nocs-1;
            $("#inflated_slide_number").val(prev_img);
            let $previous_element = $("#image_" + prev_img);
            OnSlideView($previous_element);

            e.preventDefault();
        });

        $("#inflate_prev_slide").on('click', function(e) {
            let img_no  =  $("#inflated_slide_number").val();
            let prev_img = parseInt(img_no) - 1;
            if (prev_img >= 0) {
                $("#inflated_slide_number").val(prev_img);
                let $previous_element = $("#image_" + prev_img);
                OnSlideView($previous_element);
            }
            e.preventDefault();
        });

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

        $(document).on('click', '.view_slide', function() {
            OnSlideView(this);
        });

        $(".prSlideActions").on("click", "button", function(event) {

            let button_id = $(this).attr("id");

            if (button_id === "pr_removePp") {
                let message = {msg: "remove slides file!"};
                dashboard.broker.trigger('remove.ppEdit', [message])
            }
/*            if (button_id === "pr_compactPp") {
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
            }*/
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

    function OnSlideEdit(){

/*        let parent_row =  element.closest(".card"); //".parentNode.parentNode;
        let img_node = parent_row.getElementsByTagName('img')[0];

        let img_pos_w = parent_row.id; // $(element).data('index');
        let res = img_pos_w.split(":");
        let img_pos = parseInt(res[1]);*/

        let img_pos_w = $("#inflated_slide_number").val();
        let img_pos = parseInt(img_pos_w);
        let slide_title = $("#inflated_slide_title").val();
        slide_title = slide_title.replace(/\n/g, " ");  //replace new line with space ( it happens from ppt title extraction )

        let title = language.u61;
        let message = language.u62;

        alertify.prompt()
            .setting ({
                'title'  : title,
                'message': message,
                'value'  : slide_title, //img_node.title,
                'onok'   : function(evt, value){ updateSlideTitle(img_pos, value);}
            }).show();
    }

    function OnSlideView(element) {
        let img_url =  $(element).data("url");
        let img_no  =  $(element).data("no");
        let slide_title = $(element).data("title");
        let slide_id = $(element).data("id");

        let current_slide_number = parseInt(img_no) + 1;
        $(".current_slide_number_label").html(" (#" + current_slide_number + ")");
        $("#_edit_slide_title").val(slide_title);
        $("#inflated_slide_number").val(img_no);
        $("#inflated_slide_id").val(slide_id);
        $("#canvas_slide_image").attr("src", img_url);

        let $offcanvas = $("#offcanvasSlide");
        if ( $offcanvas.css('display') === 'none' || $offcanvas.css("visibility") === "hidden"){
            // 'element' is hidden
            $offcanvas.offcanvas('show');
        }

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
                $("#image_" + position).data('title', title);
                //If compact mode
                img_node.innerHTML = (position + 1) + "." + title;
                let title_node = document.getElementById("tlt:" + position);
                let item_title_compact = title;
                if (title.length > 32) {
                    item_title_compact = title.substring(0, 29) + '...';
                }
                title_node.title = title;
                title_node.innerHTML = '<small>' + (position+1) + '.' + item_title_compact + '</small>';
                alertify.success("Ο τίτλος της διαφάνειας ενημερώθηκε");
            },
            error: function (jqXHR, textStatus, errorThrown) {
                alert('error trapped in error: UpdateSlideTitle');
                alert('msg = ' + errorThrown);
            }
        });
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
                dashboard.parousiasi.getResourceSlides(dashboard.rid,"details"); //DeleteSlideFromUi(index);
                $("#offcanvasSlide").offcanvas('hide');
            },
            error: function (jqXHR, textStatus, errorThrown)  {
                alert('error trapped in error: DeleteSlideFromDb');
                alert('msg = ' + errorThrown);
            }
        });

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

        html = html + '<div class="row justify-content-center" style="--cui-gutter-x: 0"> ';

        for (let no=0; no<nocs; no++) {
            html = html + '<div class="card mx-2 my-2" style="width: 16rem" id="cnt:' + parseInt(no) + '">';
                    html = html + '<div class="card-header">';
                    let item = slides[no];
                    let bordercolor = "#6CB33E";
                    let _time = item.time;
                    let _ftime;

                    let header_html = "";
                    let numbering_html = "";

                    let item_title = item.title;
                    let item_url = item.url;

                    let item_title_compact = item_title;
                    if (item_title === "") {
                        item_title_compact = "[χωρίς τίτλο]";
                    }
                    if (_time=== "-1") {
                        bordercolor = "silver";
                        _ftime = "--- : --- : ---";
                        numbering_html = '<span class="numbering">' + parseInt(no+1) + '. </span>';
                        header_html =  '<div id="tlt:' + parseInt(no) + '" class="slide_header text-truncate" style="font-weight: 500" title="' + item_title_compact + '">';
                        header_html +=  '<small>' + numbering_html + item_title_compact + '</small>';
                        header_html += '</div>';
                    }
                    else {
                        _ftime = secondsTimeSpanToHMS(_time);
                        numbering_html = '<span class="numbering">' + parseInt(no+1) + '. </span>';
                        header_html =  '<div id="tlt:' + parseInt(no) + '" class="slide_header_sync text-truncate">';
                        header_html +=  '<small>' + numbering_html + item_title_compact + '</small>';
                        header_html += '</div>';
                    }
                    html= html + header_html;
                html+= '</div>'; // card-header
                html = html + '<div class="card-body">';
                        html= html + '<img id="image_' + no + '" class="view_slide img-fluid" style="cursor: zoom-in; border:1px solid ' + bordercolor + '" ';
                        //html= html + 'ondrop="drop(event)" draggable="true" ondragstart="drag(event)"';
                        html= html + 'title="' + item_title + '"';
                        html= html + 'data-id="' + item_url +'"';
                        html= html + 'data-title="' + item_title +'"';
                        html= html + 'data-sync="' + _ftime +'"';
                        html= html + 'data-url="' + basepath + 'slides/' + item_url + '"';
                        html= html + 'data-no="' + no + '"';
                        html= html + 'src="' +  basepath + 'slides/small/' + item_url + '"> ';
                html+= '</div>'; // card-body
                html = html + '<div class="card-footer" style="background-color:  #d5f1de"  title="Χρόνος εμφάνισης (συγχρονισμός με βίντεο)">';
                        html= html + '<div class="slidetag mt-1"> ';
                            html= html + '<span class="slide_sync_time pl-3"> ';
                            html= html + ' <a style="color:#39f"><i class="far fa-clock"></i> ' + _ftime + '</a>';
                            html= html + '</span> ';
                        html= html + '</div>';
                html+= '</div>'; // card-footer
            html+= '</div>'; // card
        }

        html+= '</div>'; // row

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
