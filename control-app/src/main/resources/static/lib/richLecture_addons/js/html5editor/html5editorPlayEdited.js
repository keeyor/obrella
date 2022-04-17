
let re_response= "-1";
let siteRoot;
let _video_edited;

function RealEditDaemon(rid) {
    $("#real_time_edit_rid").val(rid);
    siteRoot = webRoot;
    let timeout =  setInterval(function() {
        getRealEditStatus();
        if (re_response === "FLRE" || re_response === "Canceled" || re_response === "DARE") {
            clearInterval(timeout);
            if (re_response === "FLRE") {
                alertify.alert("Η διαδικασία δημιουργίας του νέου βίντεο, απέτυχε. Μπορείτε όμως να δοκιμάσετε ξανά!");
                $("#play_edited_modal").modal("hide");
            }
            else if (re_response === "Canceled") {
                alertify.alert("Η κοπή ακυρώθηκε από τον χρήστη");
                $("#play_edited_modal").modal("hide");
            }
            else if (re_response === "DARE") {
                alertify.notify("Το νέο βίντεο δημιουργήθηκε. Παρακαλώ ελέξτε αν το αποτέλεσμα σας ικανοποιεί", "success");
                getEditedVideo(rid);
            }
        }
    }, 1000);
}

function getEditedVideo(rid) {
    $("#wait_realedit_modal").modal('hide');
    load_edited_video(rid);
}

function getRealEditStatus() {

    realediting_status = siteRoot + "/api/v1/realediting/status";
    $.ajax({
        type:        "GET",
        url: 		  realediting_status,
        async:		  true,
        success: function(response){
            re_response=  response;
        },
        error: function (jqXHR, textStatus, errorThrown)  {
            re_response = errorThrown;
        }
    });
}

function load_edited_video(rid) {
    $("#play_edited_modal").modal('show');
    if (siteRoot === undefined || siteRoot  === null) {
        siteRoot = getRootWebSitePath();
    }
    $("#real_time_edit_rid").val(rid);
    read_resource_realedit_presentation(rid);
}

function read_resource_realedit_presentation(rid) {

    $.ajax({
        type: "GET",
        url: siteRoot + "/api/v1/resource/real_edited_presentation/" + rid,
        cache: false,
        dataType: "json"
    })
        .done (function(data) {
            video_url       = data.video_url;
            video_duration  = data.duration;
            provider_type   = data.provider;
        })
        .always(function(){
            init_HTML5_modal(video_url);
        });
}

function post_reject_realediting(rid) {
    $.ajax({
        type: "POST",
        url: siteRoot + "/api/v1/realediting/reject/" + rid,
        contentType: "application/json; charset=utf-8",
        dataType: "html",
        cache: false,
        processData: false,
        success: function (res) {
            $("#play_edited_modal").modal("hide");
            if (res !== "ERROR") {
                 alertify.notify("Το αποτέλεσμα της κοπής απορρίφθηκε. Μπορείτε να δοκιμάσετε από την αρχή.","success");
            } else {
                alert("Κατά την απόρριψη του τελικού αποτελέσματος προέκυψε κάποιο σφάλμα. Παρακαλώ ξεκινήστε τη διαδικασία από την αρχή!!");
            }
        },
        error: function () {
                alert("Server_ERROR: Κατά την απόρριψη του τελικού αποτελέσματος προέκυψε κάποιο σφάλμα. Παρακαλώ ξεκινήστε τη διαδικασία από την αρχή!!");
        }
    });

}
function post_accept_realediting(rid) {

    $.ajax({
        type: "POST",
        url: siteRoot + "/api/v1/realediting/approve/" + rid,
        contentType: "application/json; charset=utf-8",
        dataType: "html",
        cache: false,
        processData: false,
        success: function (res) {
            $("#play_edited_modal").modal("hide");
            if (res !== "ERROR") {
                alert("Το αποτέλεσμα της κοπής ενσωματώθηκε στην καταχώρηση με επιτυχία");
            } else {
                alert("Κατά την αποδοχή του τελικού αποτελέσματος προέκυψε κάποιο σφάλμα. Παρακαλώ ξεκινήστε τη διαδικασία από την αρχή!!");
            }
        },
        error: function () {
            alert("Server_ERROR: Κατά την αποδοχή του τελικού αποτελέσματος προέκυψε κάποιο σφάλμα. Παρακαλώ ξεκινήστε τη διαδικασία από την αρχή!!");
        }
    });

}

function init_HTML5_modal(video_url) {

    _video_edited = document.getElementById("player_edited");
    const mp4 = document.getElementById("mp4-re");
    _video_edited.setAttribute("poster", siteRoot + '/lib/richLecture_addons/css/images_editor/pause-large.png');
    mp4.setAttribute("src", video_url);
    _video_edited.load();
}

function getRootWebSitePath() {
    const _location = document.location.toString();
    const applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
    const applicationName = _location.substring(0, applicationNameIndex) + '/';
    const webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

    return _location.substring(0, webFolderIndex);
}

$("#play_edited_modal").on('hidden.coreui.modal', function (e) {
     location.reload();
})

$("#real_reject_bt").on('click',function(){

    let rid = $("#real_time_edit_rid").val();
    let msg = '<p>Το βίντεο που παρήχθηκε από την διαδικασία της πραγματικής κοπής θα διαγραφεί. <b>Είστε σίγουρος;</b></p>';
    alertify.confirm('Απόρριψη Κοπής', msg,
        function () {
            post_reject_realediting(rid);
            $("#accept_reject_button").hide();
        },
        function () {}
    );
})
$("#real_approve_bt").on('click',function(){
    let rid = $("#real_time_edit_rid").val();
    let msg = '<p>Το βίντεο που παρήχθηκε από την διαδικασία της πραγματικής κοπής θα αντικαταστήσει το υπάρχον. <b>Είστε σίγουρος;</b></p>';
    alertify.confirm('Αποδοχή Κοπής', msg,
        function () {
            post_accept_realediting(rid);
            $("#accept_reject_button").hide();
        },
        function () {
        });
});

$("#play_edited_video").on('click',function(e) {
    let info = $(this).data("info").split("::");
    let id = info[0];
    let title = info[1];
    $("#playEditedModalTitle").text(title);
    load_edited_video(id);
    e.preventDefault();
})


