(function () {
    'use strict';


    window.dashboard = window.dashboard || {};
    dashboard.broker = $({});

    let myCaptcha;

    dashboard.init = function () {

        let feedback_id = $("#feedback_id").val();
        if (feedback_id !== undefined && feedback_id !== "") {
            $("#msg_text").html("Η γνώμη σας καταχωρήθηκε. Ευχαριστούμε πολύ!");
            $("#feedback_card").hide();
        }
        else {
            let submitted = $("#submitted").val();
            let msg_type = $("#msg_type").val();
            if (msg_type !== undefined && msg_type === "alert-danger") {
                $("#msg_text").addClass("red-btn-wcag-bgnd-color").addClass("text-white");
            }
            if (submitted === "true") {
                $("#feedback_card").hide();
            } else {
                $("#feedback_card").show();
            }
        }
        initCaptcha();
    };

    $("#submit_form_button").on('click',function(e) {
        myCaptcha.validate();
        e.preventDefault();
    });

    function initCaptcha() {
       myCaptcha = new jCaptcha({
            el: '.jCaptcha',
            canvasClass: 'jCaptchaCanvas',
            canvasStyle: {
                // required properties for captcha stylings:
                width: 100,
                height: 15,
                textBaseline: 'top',
                font: '15px Arial',
                textAlign: 'left',
                fillStyle: '#000'
            },
            // set callback function for success and error messages:
            callback: ( response, $captchaInputElement, numberOfTries ) => {
                if ( response === 'success' ) {
                    $("#feedback-form").submit();
                }
                if ( response === 'error' ) {
                    $("#msg_text").addClass("red-btn-wcag-bgnd-color").addClass("text-white");
                    $("#msg_text").html("Το αποτέλεσμα της αριθμητικής πράξης είναι λάθος. Παρακαλώ, προσπαθήστε ξανά.");
                    if (numberOfTries === 3) {
                        $("#msg_text").addClass("red-btn-wcag-bgnd-color").addClass("text-white");
                        $("#msg_text").html("3 λαθεμένες προσπάθειες. Η φόρμα έχει απενεργοποιηθεί!");
                        $("#submit_form_button").addClass("disabled");
                        $("#submit_form_button").hide();
                        $("#feedback_card").hide();
                    }
                }
            }
        });
    }
    $(document).ready(function () {
        dashboard.init();
    });

    dashboard.broker.getRootSitePath = function () {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let applicationName = _location.substring(0, applicationNameIndex) + '/';
        let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

        return _location.substring(0, webFolderIndex);
    };

})();