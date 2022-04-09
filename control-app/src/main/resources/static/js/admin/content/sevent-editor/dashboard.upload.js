(function () {
    'use strict';

    dashboard.upload = dashboard.upload || {};

    let uploader;

    dashboard.upload.init = function () {

        let upload_base_path = $("#upload_base_path").val();

        $("#uploadfiles").on ('click',function(e) {
            uploader.start();
            e.preventDefault();
            return false;
        });

        $("#coursePhotoValidationBt").on ('click',function(e) {
             let photo_url = $("#course_photo").val();
             if (photo_url !== '') {

             }
             e.preventDefault();
        });

        uploader = new plupload.Uploader({
            runtimes : 'html5,flash,silverlight,html4',
            browse_button : 'pickfiles',
            container: document.getElementById('container'),
            url : dashboard.siteurl + '/secure/image_upload',
            flash_swf_url : dashboard.siteurl + '/res/plupload-2.3.6/js/Moxie.swf',
            silverlight_xap_url : dashboard.siteurl + '/res/plupload-2.3.6/js/Moxie.xap',
            chunk_size : '1mb',
            unique_names : true,
            multipart_params : {
                target : "SCHEDULED_EVENT",
                id: "dummy" // to be set before upload
            },
            headers: {
                "X-CSRF-TOKEN" : $("#_token").val()
            },
            filters : {
                max_file_size : '10mb',
                mime_types: [
                    {title : "Image files", extensions : "jpg"}
                ]
            },
            init: {
                PostInit: function() {
                    document.getElementById('filelist').innerHTML = '';
                    $("#uploadfiles").hide();
                    $("#uploadfiles_disabled").show();
                },
                FilesAdded: function(up, files) {
                    plupload.each(files, function(file) {
                        document.getElementById('filelist').innerHTML = '<div id="' + file.id + '">Όνομα αρχείου (μέγεθος): ' + file.name + ' (' + plupload.formatSize(file.size) + ')</div>';
                        document.getElementById('file_select_label').innerHTML = 'Πατήστε "Μεταφόρτωση αρχείου" για την προσθέσετε την εικόνα που έχετε επιλέξει στην Εκδήλωση ή "Επιλογή αρχείου" για αλλάξετε την επιλογή σας. Η (τυχόν) υπάρχουσα φωτογραφία θα διαγραφεί!'
                        $("#uploadfiles").show();
                        $("#uploadfiles_disabled").hide();
                    });
                },
                Error: function(up, err) {
                    document.getElementById('console').appendChild(document.createTextNode("\nError #" + err.code + ": " + err.message));
                },
                FileUploaded: function(up, file, info) {
                    if (info.response === "OK" || info.response === "" || info.response == null) {
                        let image_file_name = uploader.settings.multipart_params.id + ".jpg";
                        let image_folder_name = uploader.settings.multipart_params.id;
                        var d = new Date(); //add date to avoid image cache
                        $("#event_photo_url").attr("src", upload_base_path + "/" + image_folder_name  + "/" + image_file_name + "?ver=" + d.getTime());
                        $("#event_photo_rurl").val(image_file_name);
                        $("#_image_panel").show();
                        $("#event_photo").val(image_file_name);
                        $("#default_photo").hide();
                        document.getElementById('file_select_label').innerHTML = 'Η μεταφόρτωση της φωτογραφίας, ολοκληρώθηκε';
                        document.getElementById('filelist').innerHTML = '';
                        $("#uploadfiles").hide();
                        $("#uploadfiles_disabled").show();
                    }
                    else {
                        setStatus(info.response)
                    }
                }
            }
        }); // uploader

        uploader.init();
    }

    dashboard.upload.setEventId = function(id) {
        uploader.settings.multipart_params.id = id;
    }
    function setStatus(status_msg) {
        if (status_msg !== '') {
            $("#status").html(status_msg);
        }
    }



})();