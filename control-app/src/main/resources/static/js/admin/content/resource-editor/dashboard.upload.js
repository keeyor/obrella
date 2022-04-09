(function () {
    'use strict';

    dashboard.upload = dashboard.upload || {};

    let m_folder;

    let postProcessingUrl;
    let analyzeProcessingUrl;

    let dropArea;
    let browseButton;
    let upload_container;
    let fileList;

    let $upload_log;
    let $dropArea;
    let $browseFileButton;
    let $cancelUploadButton;
    let $statusMessage;
    let $progress_bar;

    let $post_action;
    let $post_action_data;

    let $outer_upload_container;


    let fileUploader;
    let mm_uploaded_filename;

    dashboard.upload.init = function () {

        let d = new Date();
        let month = d.getMonth() +1;
        let year  = d.getFullYear();
        m_folder                = month + "-" + year + "/" + dashboard.rid;


        postProcessingUrl       = dashboard.siteUrl + "/admin/multimediaUpload";
        analyzeProcessingUrl    = dashboard.siteUrl + "/admin/startMultimediaProcessing";

        dropArea                = "mm_dropArea";
        browseButton            = "mm_BrowseFileButton";
        upload_container        = "mm_upload_container";
        fileList                = "mm_fileList";

        $upload_log             = $("#mm_uploadlog");
        $dropArea               = $("#mm_DropArea");
        $browseFileButton       = $("#mm_BrowseFileButton");
        $cancelUploadButton     = $("#mm_CancelUploadButton");
        $statusMessage          = $("#mm_statusMessage");
        $progress_bar           = $("#mm_progress_bar");

        $post_action            = $("#mm_UploadAction");
        $post_action_data       = $("#mm_UploadData");

        $outer_upload_container = $("#mm_outer_upload_container");

    };

    dashboard.upload.show = function () {

        let has_vd = $("#has_vd").val();
        if (dashboard.rid !== "" && has_vd === "-1") {
            $(".save_warning").hide();
            $outer_upload_container.show();
            InitControls();
            fileUploader = InitializeUploadControl();
        }
        else {
            $outer_upload_container.hide();
            $(".save_warning").show();
        }
        $cancelUploadButton.on("click", function() {
            fileUploader.stop();
            ClearUploadQueue(fileUploader,"cancel");
        });
        $("#delete_mm").on("click",function(event){
            event.preventDefault();
            let message = {msg: "Remove Video!"};
            dashboard.broker.trigger('remove.mmEdit', [message]);
        })
    };

    dashboard.upload.hide = function () {
        $(".save_warning").hide();
        $outer_upload_container.hide();
    };

    dashboard.upload.started = function () {

        $progress_bar.css('width', '0%').attr('aria-valuenow',0);
        dashboard.upload.setStatus("Upload Started");
        $dropArea.hide();
        $browseFileButton.prop("disabled", true);
        $cancelUploadButton.prop("disabled", false);
    };

    dashboard.upload.stopped = function () {

       // setTimeout(function(){
       //     ClearUploadQueue(fileUploader);
       //     console.log(fileUploader.files.length)},500);
    };

    dashboard.upload.resetUploadArea = function(finito) {

        $dropArea.show();

        if (finito === "finished") {
            dashboard.upload.setStatus("Complete!");
        }
        else if (finito === "cancel") {
            $progress_bar.css('width', '0%').attr('aria-valuenow', 0);
            dashboard.upload.setStatus("(Upload Cancelled). Ready!");
        }
        else {
            $progress_bar.css('width', '0%').attr('aria-valuenow', 0);
            dashboard.upload.setStatus("(Upload Stopped). Ready!");
        }

        $browseFileButton.prop("disabled", false);
        $cancelUploadButton.prop("disabled", true);
    };

    function InitializeUploadControl() {

        let post_processing_url = postProcessingUrl;
        let $upload_container = document.getElementById(upload_container);

        var fileUploader = new plupload.Uploader({
            runtimes : 'html5,flash,silverlight,html4',
            container : $upload_container,
            url :  post_processing_url,
            chunk_size : '20mb',
            unique_names : true,
            drop_element : dropArea,
            browse_button : browseButton,
            multipart_params : {
                m_folder : m_folder
            },
            filters : {
                max_file_size : '750mb',
                // Specify what files to browse for
                mime_types: [
                    {title : "MP4 Video files", extensions : "mp4"},
                    {title : "MP3 Audio files", extensions : "mp3"}
                ]
            },

            // Flash settings
            flash_swf_url : dashboard.siteUrl + '/lib/plupload-2.3.6/js/Moxie.swf',

            // Silverlight settings
            silverlight_xap_url : dashboard.siteUrl + '/lib/plupload-2.3.6/js/Moxie.xap',

            // PreInit events, bound before any internal events
            preinit : {
                Init: function(up, info) {
                    dashboard.upload.log('[Init]', 'Info:', info, 'Features:', up.features);
                    dashboard.upload.setStatus("Ready!");
                },

                UploadFile: function(up, file) {
                    dashboard.upload.log('[UploadFile]', file);
                    dashboard.upload.setStatus("Uploading file: " + file.name);
                }
            },

            // Post init events, bound after the internal events
            init : {
                PostInit: function() {
                    // Called after initialization is finished and internal event handlers bound
                    dashboard.upload.log('[PostInit]');
                    document.getElementById(fileList).innerHTML = '';
                },

                Browse: function() {
                    // Called when file picker is clicked
                    dashboard.upload.log('[Browse]');
                },

                Refresh: function() {
                    // Called when the position or dimensions of the picker change
                    dashboard.upload.log('[Refresh]');
                },

                StateChanged: function(up) {
                    // Called when the state of the queue is changed
                    dashboard.upload.log('[StateChanged]', up.state === plupload.STARTED ? "STARTED" : "STOPPED");
                    let message = {msg: "Upload State Change!", value: up.state};
                    dashboard.broker.trigger('upload_state.changed', [message]);
                },

                QueueChanged: function(up) {
                    // Called when queue is changed by adding or removing files
                    dashboard.upload.log('[QueueChanged]:' + up.total.queued);
                },

                OptionChanged: function(up, name, value, oldValue) {
                    // Called when one of the configuration options is changed
                    dashboard.upload.log('[OptionChanged]', 'Option Name: ', name, 'Value: ', value, 'Old Value: ', oldValue);
                },

                BeforeUpload: function(up, file) {
                    // Called right before the upload for a given file starts, can be used to cancel it if required
                    dashboard.upload.log('[BeforeUpload]', 'File: ', file);
                },

                UploadProgress: function(up, file) {
                    // Called while file is being uploaded
                    dashboard.upload.log('[UploadProgress]', 'File:', file, "Total:", up.total);
                    $progress_bar.css('width', file.percent + '%').attr('aria-valuenow',file.percent);
                    dashboard.upload.setStatus("Uploading file: " + file.name + " - Percent completed: " + file.percent + "%");
                },

                FileFiltered: function(up, file) {
                    // Called when file successfully files all the filters
                    dashboard.upload.log('[FileFiltered]', 'File:', file);
                },

                /**
                 * @return {boolean}
                 */
                FilesAdded: function(up) {
                    // Called when files are added to queue
                    dashboard.upload.log('[FilesAdded]:' + up.total.queued );
                    fileUploader.start();
                    return false;
                },

                FilesRemoved: function(up) {
                    // Called when files are removed from queue
                    dashboard.upload.log('[FilesRemoved]:' + up.total.queued);
                },

                FileUploaded: function(up, file, info) {
                    // Called when file has finished uploading
                    dashboard.upload.log('[FileUploaded] File:', file, "Info:", info);
                    if ( (info.response.trim() === "UPLOAD_OK")) {
                        let ext = file.name.split('.').pop();
                        mm_uploaded_filename = file.id + "." + ext;
                    }
                    else {
                        up.stop();
                        dashboard.upload.setStatus(info.response.trim());
                    }
                },
                ChunkUploaded: function(up, file, info) {
                    dashboard.upload.log('[ChunkUploaded] File:', file, "Info:", info);
                },

                UploadComplete: function(up) {
                    // Called when all files are either uploaded or failed
                    dashboard.upload.log('[UploadComplete]');
                    dashboard.upload.setStatus('Finished!');
                    if (up.total.uploaded > 0) {
                        let post_action = $post_action.val();
                        let action_data = $post_action_data.val();
                        ClearUploadQueue(fileUploader, "finished");
                        doAjaxHandle(m_folder,dashboard.rid,post_action,action_data,mm_uploaded_filename);
                    }
                },

                Destroy: function() {
                    // Called when uploader is destroyed
                    dashboard.upload.log('[Destroy] ');
                },

                Error: function(up, args) {
                    // Called when error occurs
                    dashboard.upload.log('[Error] ', args);
                    ClearUploadQueue(fileUploader, "error");
                    dashboard.upload.setStatus("Error in upload");
                }
            }
        });
        fileUploader.splice();
        fileUploader.init();
        return fileUploader;
    }

    function doAjaxHandle(m_folder,resource_id,action,actionData, filename) {
        // get the form values

        let analyze_processing_url =  analyzeProcessingUrl;

        console.log("analyze:" + analyze_processing_url);
        let params = {
            folder	    :	m_folder,
            id          :   resource_id,
            action 		:	action,
            actionData  :   actionData,
            fileName	:	filename
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
                    console.log("loading player for file:" + filename);
                    dashboard.upload.setStatus("");
                    fillVideoFileProperties(response.result);

                    let message = {msg: "Upload Succeded!", filename: filename, id: m_folder};
                    dashboard.broker.trigger('upload_state.finished', [message]);
                    //dashboard.upload.hide();
                }
                else {
                    if (response.message !== null) {
                        dashboard.upload.setStatus(response.status + ":" + response.message);
                    }
                    else {
                        dashboard.upload.setStatus(response.status);
                    }
                }
            },
            error: function (jqXHR, textStatus, errorThrown)  {
                dashboard.upload.setStatus(errorThrown);
            }
        });
    }


    dashboard.upload.setStatus = function (status_msg) {
        $statusMessage.html(status_msg);
    };

    dashboard.upload.log = function () {
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

        $upload_log.append(str + "\n");
        $upload_log.scrollTop($upload_log[0].scrollHeight);
    };

    function ClearUploadQueue(uploader, finito){

        let len = uploader.files.length;
        for (let i=len-1; i>=0;i--) {
            uploader.splice(i,1);
        }
        dashboard.upload.resetUploadArea(finito);
    }

    function InitControls() {

        mm_uploaded_filename = "";

        let $body = $("body");

         $upload_log.hide();

        $dropArea.show();
        $browseFileButton.prop("disabled", false);
        $cancelUploadButton.prop("disabled", true);

        $body.bind("dragenter", function(e){
            $(".dropsq").addClass("draggingFile");
            e.stopPropagation();
            e.preventDefault();
        });
        $body.bind("dragleave", function(){
            $(".dropsq").removeClass("draggingFile");
        });
        $body.bind("dragover", function(e){
            $(".dropsq").addClass("draggingFile");
            e.stopPropagation();
            e.preventDefault();
        });
        $body.bind("drop", function(e){
            e.stopPropagation();
            e.preventDefault();
            $(".dropsq").removeClass("draggingFile");
        });

    }

    function fillVideoFileProperties(ap) {

        let $video_format = $("#file_format");
        let $video_resolution = $("#video_res");
        let $video_aspectRatio = $("#video_ar");
        let $video_duration = $("#video_dur");
        let $file_name = $("#file_name");
        let $folder = $("#file_folder");
        let $size = $("#file_size");

        $video_format.text(ap.format);
        $video_resolution.text(ap.resolution);
        $video_aspectRatio.text(ap.aspectRatio);
        $video_duration.text(ap.duration);
        $file_name.text( ap.fileName);
        $folder.text(ap.folder);
        $size.text(ap.filesize + " bytes");
    }
})();
