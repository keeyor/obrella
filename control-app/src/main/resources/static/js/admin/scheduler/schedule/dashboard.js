(function () {
    'use strict';

    window.dashboard = window.dashboard || {};
    dashboard.broker = $({});

    dashboard.siteurl = "";
    dashboard.dtLanguageGr = "";
    dashboard.canDelete = true;

    let serialize_form;

    dashboard.init = function () {

        //display post errors
        let  msg_val = $("#msg_val").val();
        if (msg_val !== '') {
            try {
                let jsonMsg = JSON.parse(msg_val);
                let html = jsonMsg.msg + ": τίτλος=" + jsonMsg.title + ", ημέρα=" + dashboard.broker.selectDayOfWeek(jsonMsg.dayOfWeek) + ", ώρα εκκίνησης=" + jsonMsg.startTime +
                    ", ώρα λήξης=" + jsonMsg.endTime;
                $("._fixed_error_msg").html('<i class="fas fa-exclamation-triangle"></i> ' + html);
            }
            catch(e) {
                let   msg_type  =$("#msg_type").val();
                let message = {msg: "instant message", type: msg_type, val: msg_val};
                dashboard.broker.showInstantMessage(message.type ,message.val);
            }
        }
        else {
            $("._fixed_error_msg").html('');
        }

        dashboard.siteurl = dashboard.broker.getRootSitePath();
        InitGlobalSettings();
        InitControls();
        //Authorization Error
        let authorize = $("#authorized").val();
        if (authorize === "false") {
            $(".cancel-scheduled").hide();
            $("#save-button").attr('disabled', true);
            $("._fixed_error_msg").html("ΠΡΟΣΟΧΗ: Δεν έχετε δικαίωμα Τροποποίησης της Καταχώρησης. Η αποθήκευση έχει απενεργοποιηθεί!");
        }
        else {
            InitEvents();
            loader.initialize();
        }
        function InitControls() {

            let schedule_id         = $("#id").val();
            let $schedule_type      = $("#_type");
            let schedule_type       = $schedule_type.val();
            let $repeat_type        = $("#_repeat");
            let repeat_type         = $repeat_type.val();
            let $broadcast          = $("#_broadcast");
            let $access             = $("#_access");
            let $record             = $("#_record");
            let $publish            = $("#_publish");
            let $password           = $("#_password");
            let $reveal_passfield   = $("#reveal_passfield");


            $("#supervisor_s2").select2({
                placeholder: "Επιλέξτε Διδάσκων (-ουσα) - προηγείται η επιλογή Μαθήματος"
            });

            $("#classrooms_s2").select2({
                placeholder: "Επιλέξτε Χώρο | Αίθουσα - προηγείται η επιλογή Μαθήματος & Διδάσκοντα"
            });

            $("#events_s2").select2({
                placeholder: "Επιλέξτε Εκδήλωση"
            });
            $("#events_channel").select2({
                minimumResultsForSearch: -1,
            });
            //_type: lecture | event
            $schedule_type.select2({
                minimumResultsForSearch: -1,
            });
            //_repeat (regular or extra
            $repeat_type.select2({
                minimumResultsForSearch: -1,
            });
            //AcademicYear
            $("#resource_ay").select2({
                placeholder: 'Επιλέξτε ακαδημαϊκό έτος',
                minimumResultsForSearch: -1
            });
            //Periods
            $("#resource_pd").select2({
                placeholder: 'Επιλέξτε περίοδο',
                minimumResultsForSearch: -1
            });

            $("#_dayOfWeek").select2({
                placeholder: 'Επιλέξτε Ημέρα της Εβδομάδας',
                minimumResultsForSearch: -1
            });
            $("#resource_hour").select2({
                minimumResultsForSearch: -1
            });
            $("#resource_minutes").select2({
                minimumResultsForSearch: -1
            });

            //duration spinners init
            $("input[type='number']").inputSpinner();

            $("#_date").datepicker({
                format: "yyyy-mm-dd",
                todayBtn: false,
                language: "el",
                autoclose: true,
                todayHighlight: false
            });

            $broadcast.select2({
                minimumResultsForSearch: -1,
            });
            $access.select2({
                minimumResultsForSearch: -1,
            });
            $record.select2({
                minimumResultsForSearch: -1,
            });
            $publish.select2({
                minimumResultsForSearch: -1,
            });


            if (schedule_type  == null || schedule_type  === "lecture") {
                $(".event_panel").hide();
                $(".lecture_panel").show();
            }
            else {
                $(".event_panel").show();
                $(".lecture_panel").hide();
            }

            if (schedule_id == null || schedule_id === "") {
                $("#schedule_panel").hide();
            }
            else {
                // fix startTime

                let startTime = $("#_timeOfDay").val();
                let resource_hour = startTime.split(":")[0];
                if (resource_hour.startsWith("0")) { resource_hour = resource_hour.charAt(1); }
                let resource_minutes = startTime.split(":")[1];
                if (resource_minutes.startsWith("0")) { resource_minutes = resource_minutes.charAt(1);}

                $("#resource_hour").val(resource_hour).trigger("change");
                $("#resource_minutes").val(resource_minutes).trigger("change");

                loader.showLoader();
                $("#schedule_panel").show();
                if (schedule_type  == null || schedule_type  === "lecture") {
                    dashboard.lectimetable.buildScheduleTable();
                }
                else {
                    dashboard.evtimetable.buildScheduleTable();
                }
            }

            if ($broadcast.val() === "0") {
                $access.val("open").trigger("change");
                $access.attr("disabled", true);
            }
            else {
                $access.attr("disabled", false);
            }
            if ($record.val() === "0") {
                $publish.attr("disabled", true);
            }
            else {
                $publish.attr("disabled", false);
            }
            if ($access.val() !== "password") {
                $password.attr("disabled", true);
                $reveal_passfield.attr("disabled", true);
            }
            else {
                $password.attr("disabled", false);
                $reveal_passfield.attr("disabled", false);
                let x = document.getElementById("_password");
                if (x.type === "password") {
                    x.type = "text";
                } else {
                    x.type = "password";
                }
            }

            if (schedule_id !== null && schedule_id !== "") {
                $schedule_type.attr("disabled", true);
                $repeat_type.attr("disabled", true);
            }
            else {
                $schedule_type.attr("disabled", false);
            }
            if (schedule_type === "lecture" && repeat_type === "regular") {
                $(".dayOfWeek").show();
                $(".dayOfMonth").hide();
                if ($("#_active").val() === "true") {
                    $("#disable-co-button").show();
                    $("#enable-co-button").hide();
                    $("._fixed_warn_msg").html("");
                }
                else {
                    $("#disable-co-button").hide();
                    $("#enable-co-button").show();
                    $("._fixed_warn_msg").html("ΠΡΟΣΟΧΗ: Το επιλεγμένο Πρόγραμμα Μεταδόσεων είναι απενεργοποιημένο!");
                }
            }
            else {
                $(".dayOfWeek").hide();
                $(".dayOfMonth").show();
                $("#disable-co-button").hide();
                $("#enable-co-button").hide();
                $("._fixed_warn_msg").html("");
                $repeat_type.attr("disabled", true);
            }
        }

        function InitEvents() {



            $("#resource_hour").on('select2:select', function (e) {
                let data = e.params.data;
                let resource_hour = data.id;
                if (resource_hour < 10) { resource_hour = "0" + resource_hour;}
                let resource_minutes = $("#resource_minutes").val();
                if (resource_minutes < 10) { resource_minutes = "0" + resource_minutes;}
                $("#_timeOfDay").val(resource_hour + ":" + resource_minutes);
            });
            $("#resource_minutes").on('select2:select', function (e) {
                let data = e.params.data;
                let resource_minutes = data.id;
                if (resource_minutes < 10) { resource_minutes = "0" + resource_minutes;}
                let resource_hour = $("#resource_hour").val();
                if (resource_hour < 10) { resource_hour = "0" + resource_hour;}
                $("#_timeOfDay").val(resource_hour + ":" + resource_minutes);
            });

            $("#save-button").on('click',function (e){

                //enable disabled fields before submitting !important
                $("#_repeat").attr("disabled", false);
                $("#_publish").attr("disabled", false);
                $("#_access").attr("disabled", false);

                $("#schedule-form").submit();
                e.preventDefault();
            });

            $("#copy-url").on('click', function(e){
                copyUrl();
                e.preventDefault();
            });

            $('#openYouTubeDialog').on('click',function(e){
                let end_serialize = $("#schedule-form").serialize();
                if (serialize_form !== end_serialize) {
                    alertify.alert("Σφάλμα","Πρέπει να αποθηκεύσετε την καταγραφή προτού τροποποιήσετε τις επιλογές δημοσίευσης");
                }
                else {
                    $('#youtube_publish_modal').modal('show');
                }
                e.preventDefault();
            });

            $('#unsetYouTubePublication').on('click',function(e){
                let end_serialize = $("#schedule-form").serialize();
                if (serialize_form !== end_serialize) {
                    alertify.alert("Σφάλμα","Πρέπει να αποθηκεύσετε την καταγραφή προτού τροποποιήσετε τις επιλογές δημοσίευσης");
                }
                else {
                    let schedule_id = $(this).data("target");
                    let broadcast_id = $(this).data("bid");
                    $.ajax({
                        url: dashboard.siteurl + '/api/youtube/unsetBroadcast/' + schedule_id + '/' + broadcast_id,
                        type: "POST",
                        success: function () {
                            alertify.alert('Προγραμματισμός', 'Η δημοσίευση στο YouTube ακυρώθηκε με επιτυχία!', function () {
                                location.reload();
                            });
                        },
                        error: function () {
                            alertify.alert('Σφάλμα', 'Η ακύρωση της δημοσίευσης στο YouTube απέτυχε.', function () {
                                location.reload();
                            });
                        }
                    });
                }
                e.preventDefault();
            });

            $('#youtube_publish_modal').on('show.coreui.modal', function (event) {
                let scheduledId = $("#id").val();
                $.ajax({
                    url: dashboard.siteurl + '/api/youtube/setBroadcast',
                    type:"POST",
                    contentType: "application/json; charset=utf-8",
                    data: scheduledId,
                    success: function(data) {

                        if (data.startsWith("http")) {
                            window.location.href = data;
                        }
                        else {
                            $("#waiting-gif").hide();
                            $("#broadcast-link-text").html("Κωδικός YouTube:" + data);
                            $("#broadcast-link").attr("href", "https://www.youtube.com/watch?v=" + data);
                            $("#broadcast-status").text("Μπορείτε να κλείσετε αυτό το παράθυρο");
                            $("#link_panel").show();
                        }
                    },
                    error : function(msg) {
                        $("#broadcast-status").text("Σφάλμα: " + msg.responseText);
                    }
                });
            });
            $('#youtube_publish_modal').on('hidden.coreui.modal', function () {
                    location.reload();
            })

            $("#_type").on('select2:select', function (e) {
                let $repeat_select = $("#_repeat");
                let data = e.params.data;
                if (data.id === "event") {
                    $repeat_select.attr("disabled",true);
                    $(".event_panel").show();
                    $(".lecture_panel").hide();
                    $repeat_select.val("onetime").trigger("change");
                }
                else {
                     $repeat_select.attr("disabled",false);
                    $(".event_panel").hide();
                    $(".lecture_panel").show();
                    $repeat_select.val("regular").trigger("change");
                }
            });

            $("#resource_pd").on('select2:select', function () {
                    let courseId = $("#courses_s2").val();
                    let _type = $("#_type").val();
                    if (courseId != null && courseId !== "" &&  _type === "lecture" ) {
                        getSelectedPeriodBoundaries(courseId);
                    }
            });

            $("#_repeat").on('change', function () {
                let val = $("#_repeat").val();
                if (val === "regular") {
                    $(".dayOfWeek").show();
                    $(".dayOfMonth").hide();
                }
                else {
                    $(".dayOfWeek").hide();
                    $(".dayOfMonth").show();
                }
            });

            $("#reveal_passfield").click(function(e) {
                let x = document.getElementById("_password");
                if (x.type === "password") {
                    x.type = "text";
                } else {
                    x.type = "password";
                }
                e.preventDefault();
            });

            $("#_broadcast").on('change', function () {

                let $access = $("#_access");
                let val =  $("#_broadcast").val();
                if (val === "1") {
                    $access.attr("disabled", false);
                }
                else {
                    $access.val("open").trigger("change");
                    $access.attr("disabled", true);
                }
            });

            $("#_access").on('select2:select', function (e) {
                let data = e.params.data;
                if (data.id === "password") {
                    $("#_password").attr("disabled", false);
                    $("#reveal_passfield").attr("disabled", false);
                }
                else {
                    $("#_password").attr("disabled", true);
                    $("#reveal_passfield").attr("disabled", true);
                }
            });

            $("#_record").on('change', function () {
                let val =  $("#_record").val();
                if (val === "1") {
                    $("#_publish").attr("disabled", false);
                }
                else {
                    $("#_publish").attr("disabled", true);
                }
            });

            serialize_form = $("#schedule-form").serialize();
            $(".cancelResource_top").on('click',function(e){
                let end_serialize = $("#schedule-form").serialize();
                if (serialize_form !== end_serialize) {
                    let default_href = $(this).attr("href");
                    closeEditDialogWarning(default_href);
                    e.preventDefault();
                }
            });

            let $body = $("body");
            $body.on('click','.cancel-scheduled', function(e){
                let data_id = $(this).data("id");
                let data_date = $(this).data("date");
                let data_title =  $(this).data("title");
                let data_type = $(this).data("type");
                let display_date = moment(data_date).format('ll');
                let msg = '<div>Η Προγραμματισμένη Μετάδοση για τις <b>' + display_date + '</b> του Μαθήματος/Εκδήλωσης <b>"' + data_title + '"</b> θα ακυρωθεί. Είστε σίγουρος?' +
                          '<br/><br/> (προαιρετικά) Πληκτρολογήστε την αιτία της ακύρωσης' +
                          '</div>';
                          alertify.prompt('Προειδοποίηση', msg,'',
                            function (evt,value) {
                                postCancellation(data_id,data_date,value,data_type);
                            },
                            function () {
                          }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
                e.preventDefault();
            });
            $body.on('click','.un-cancel-scheduled', function(e){
                let data_id = $(this).data("id");
                let data_date = $(this).data("date");
                let data_title =  $(this).data("title");
                let data_type = $(this).data("type");
                let display_date = moment(data_date).format('ll');
                let msg = '<div>Η ημερομηνία μετάδοσης <b>' + display_date + '</b> του Μαθήματος/Εκδήλωσης <b>"' + data_title + '"</b> θα ενεργοποιηθεί. Είστε σίγουρος?' +
                          '<br/><span style="color:red">ΠΡΟΣΟΧΗ: Η ενεργοποίηση ενδέχεται να αποτύχει αν υπάρχει άλλη μετάδοση την ίδια ημέρα,ώρα και χώρο</span>' +
                          '</div>';
                          alertify.confirm('Προειδοποίηση', msg,
                                function() {
                                    postUnsetCancellation(data_id,data_date,data_type);
                                },
                                function () {
                          }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
                e.preventDefault();
            });

            $("#disable-co-button").on('click',function(){
                let schedule_id = $("#id").val();
                let course_title = $("#courses_s2").select2('data')[0].text;
                let semester     = $("#resource_pd").select2('data')[0].text;
                let dayOfWeek    = $("#_dayOfWeek").select2('data')[0].text;
                let classroom    = $("#classrooms_s2").select2('data')[0].text;
                let startTime    = $("#_timeOfDay").val();

                let msg =   '<div>Όλες οι <b>μελλοντικές</b> μεταδόσεις του Μαθήματος <b>"' + course_title +
                            '</b>" για την περίοδο "' + semester + '", που είναι προγραμματισμένες για κάθε <b>' + dayOfWeek +
                            '</b> στις <b> ' + startTime +' </b>, στο χώρο <b>' +  classroom  + '</b> θα <b>ακυρωθούν</b>. Είστε σίγουρος?' +
                            '<br/><br/> (προαιρετικά) Πληκτρολογήστε την αιτία της ακύρωσης' +
                            '</div>';

                alertify.prompt('Προειδοποίηση', msg,'',
                    function (evt,value) {
                        postCancelRemaining(schedule_id,value);
                    },
                    function () {
                    }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
            });
            $("#enable-co-button").on('click',function() {
                let schedule_id = $("#id").val();
                let course_title = $("#courses_s2").select2('data')[0].text;
                let semester     = $("#resource_pd").select2('data')[0].text;
                let dayOfWeek    = $("#_dayOfWeek").select2('data')[0].text;
                let classroom    = $("#classrooms_s2").select2('data')[0].text;
                let startTime    = $("#_timeOfDay").val();

                let msg =   '<div>Όλες οι <b>μελλοντικές</b> μεταδόσεις του Μαθήματος <b>"' + course_title +
                            '</b>" για την περίοδο "' + semester + '", που είναι προγραμματισμένες για κάθε <b>' + dayOfWeek +
                            '</b> στις <b> ' + startTime +' </b>, στο χώρο <b>' +  classroom  + '</b> θα <b>ενεργοποιηθούν</b>. Είστε σίγουρος?' +
                            '</div>';

                alertify.confirm('Προειδοποίηση', msg,
                    function () {
                        unsetRemaining(schedule_id);
                    },
                    function () {
                    }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
            });

            $("#deleteSchedule").on('click',function(e){

                let schedule_id = $("#id").val();
                if (schedule_id === "") {
                    alertify.notify("Η καταχώρηση δεν έχει αποθηκευτεί!" , "warning");
                }
                else {
                    let data_type = $("#_type").val();
                    let classroom = $("#classrooms_s2").select2('data')[0].text;
                    let startTime = $("#_timeOfDay").val();
                    let title, _date, semester, repeat;
                    if (data_type === "lecture") {
                        title = $("#courses_s2").select2('data')[0].text;
                        semester = $("#resource_pd").select2('data')[0].text;
                        repeat = $("#_repeat").val();
                        if (repeat === 'onetime') {
                            repeat = 'Έκτακτη';
                            _date = $("#_date").val();
                        } else {
                            repeat = 'Τακτική';
                            _date = $("#_dayOfWeek").select2('data')[0].text;
                        }
                    } else {
                        title = $("#events_s2").select2('data')[0].text;
                        _date = $("#_date").val();
                    }

                    let deleteWarning = '<span class="fas fa-trash mb-2"></span> Είστε σίγουρος ότι θέλετε να διαγράψετε τη μετάδοση?';
                    if (data_type === "lecture") {
                        deleteWarning +=    '<table style="width:100%"><tr><td style="width: 20%" rowspan="7"><i class="icon-main-menu-scheduler fa-5x"></i></td></tr>' +
                                            '<tr><td style="vertical-align: top">Μάθημα</td><td>' + title + '</td></tr>' +
                                            '<tr><td>Τύπος</td><td>' + repeat + '</td></tr>' +
                                            '<tr><td>Περίοδος</td><td>' + semester + '</td></tr>';
                        if (repeat === 'onetime') {
                            deleteWarning += '<tr><td>Ημέρα</td><td>' + _date + '</td></tr>';
                        } else {
                            deleteWarning += '<tr><td>Ημερομηνία</td><td>' + _date + '</td></tr>';
                        }
                    } else {
                        deleteWarning +=    '<table style="width:100%"><tr><td style="width: 20%" rowspan="5"><i class="icon-main-menu-scheduler fa-5x"></i></td></tr>' +
                                            '<tr><td style="vertical-align: top">Εκδήλωση</td><td>' + title + '</td></tr>' +
                                            '<tr><td>Ημερομηνία</td><td>' + _date + '</td></tr>';
                    }
                    deleteWarning +=    '<tr><td>Ώρα</td><td>' + startTime + '</td></tr>' +
                                        '<tr><td>Χώρος</td><td>' + classroom + '</td></tr>' +
                                        '</table>';

                    let warnHeader = 'Διαγραφή Μετάδοσης';
                    alertify.confirm(warnHeader, deleteWarning,
                        function () {
                            postDelete();
                        },
                        function () {
                        }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
                }
                e.preventDefault();
            });
        }

        function closeEditDialogWarning(default_href) {
            if (dashboard.canDelete === true) {
                let msg = '<div class="font-weight-bold">Οι αλλαγές θα χαθούν! Είστε σίγουρος?</div>';
                alertify.confirm('Προειδοποίηση', msg,
                    function () {
                        window.location = default_href;
                    },
                    function () {
                    }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
            }
            else {
                window.location = default_href;
            }
        }

        function copyUrl() {
            let input = $("#copy_url_input");
            copyToClipboard(input, "Η διεύθυνση έχει αντιγραφεί στο πρόχειρο!");
        }

        function copyToClipboardFF(text) {
            window.prompt ("Copy to clipboard: Ctrl C, Enter", text);
        }

        function copyToClipboard(input, message) {

            var success   = true,
                range     = document.createRange(),
                selection;

            // For IE.
            if (window.clipboardData) {
                window.clipboardData.setData("Text", input.val());
            } else {
                // Create a temporary element off screen.
                var tmpElem = $('<div>');
                tmpElem.css({
                    position: "absolute",
                    left:     "-1000px",
                    top:      "-1000px",
                });
                // Add the input value to the temp element.
                tmpElem.text(input.val());
                $("body").append(tmpElem);
                // Select temp element.
                range.selectNodeContents(tmpElem.get(0));
                selection = window.getSelection ();
                selection.removeAllRanges ();
                selection.addRange (range);
                // Lets copy.
                try {
                    success = document.execCommand ("copy", false, null);
                }
                catch (e) {
                    copyToClipboardFF(input.val());
                }
                if (success) {
                    alert (message);
                    tmpElem.remove();
                }
            }
        }

        function InitGlobalSettings() {

            alertify.defaults.transition = "slide";
            alertify.defaults.theme.ok = "btn blue-btn-wcag-bgnd-color text-white";
            alertify.defaults.theme.cancel = "btn red-btn-wcag-bgnd-color text-white";
            alertify.defaults.theme.input = "form-control";
            alertify.set('notifier','position', 'top-center');
        }

        function postCancellation(id,date,reason,type) {
            let cancellation = {
                title : reason,
                date : date
            };
            $.ajax({
                    url: dashboard.siteurl + '/api/v1/schedule_table/set_cancellation/' + id,
                    type:"POST",
                    contentType: "application/json; charset=utf-8",
                    data: 		  JSON.stringify(cancellation),
                    async: true,
                    success: function() {
                        alertify.notify("Η ακύρωση καταχωρήθηκε" , "success");
                        if (type === "lecture") {
                                dashboard.lectimetable.rebuildScheduleTable();
                        }
                        else if (type === "event") {
                                dashboard.evtimetable.rebuildScheduleTable();
                        }
                    },
                    error : function(msg) {
                        alertify.error("Σφάλμα: " + msg.responseText);
                    }
            });
        }

        function postCancelRemaining(id ,reason) {
            if (reason === "") { reason = "Δεν ορίστηκε αιτία";}
            $.ajax({
                url: dashboard.siteurl + '/api/v1/schedule_table/cancel_remaining/' + id,
                type:"POST",
                contentType: "application/json; charset=utf-8",
                data: reason,
                async: true,
                success: function() {
                    alertify.notify("Η απενεργοποίηση καταχωρήθηκε" , "success");
                    $("._fixed_warn_msg").html("ΠΡΟΣΟΧΗ: Τό επιλεγμένο Πρόγραμμα Μεταδόσεων είναι απενεργοποιημένο!");
                    dashboard.lectimetable.rebuildScheduleTable();
                    $("#disable-co-button").hide();
                    $("#enable-co-button").show();
                },
                error : function(msg) {
                    alertify.error("Σφάλμα: " + msg.responseText);
                }
            });
        }
        function unsetRemaining(id) {
            $.ajax({
                url: dashboard.siteurl + '/api/v1/schedule_table/unset_remaining/' + id,
                type:"POST",
                contentType: "application/json; charset=utf-8",
                async: true,
                success: function() {
                    alertify.notify("Η ενεργοποίηση καταχωρήθηκε" , "success");
                    $("._fixed_warn_msg").html("");
                    dashboard.lectimetable.rebuildScheduleTable();
                    $("#disable-co-button").show();
                    $("#enable-co-button").hide();
                },
                error : function(msg) {
                    alertify.error("Σφάλμα: " + msg.responseText);
                }
            });
        }
        function postUnsetCancellation(id,date,type) {
            let cancellation = {
                title : "",
                date : date
            };
            $.ajax({
                url: dashboard.siteurl + '/api/v1/schedule_table/unset_cancellation/' + id,
                type:"POST",
                contentType: "application/json; charset=utf-8",
                data: 		  JSON.stringify(cancellation),
                async: true,
                success: function() {
                    alertify.notify("Η μετάδοση/καταγραφή ενεργοποιήθηκε" , "success");
                    if (type === "lecture") {
                        dashboard.lectimetable.rebuildScheduleTable();
                    }
                    else if (type === "event") {
                        dashboard.evtimetable.rebuildScheduleTable();
                    }
                },
                error : function(msg) {
                    alertify.error("Σφάλμα: " + msg.responseText);
                }
            });
        }

        function postDelete() {
              //#disable _type in order to post the value
              $("#_type").attr("disabled",false);
              let schedule_form = $("#schedule-form");
              schedule_form.attr("action", "schedule?action=delete");
              schedule_form.submit()
        }

        //Period depends on department or studyProgram which can be found from course info
        function getSelectedPeriodBoundaries(courseId) {

            let academic_year = $("#resource_ay").val();
            let period_name =  $("#resource_pd").val();


            $.ajax({
                url: dashboard.siteurl + '/api/v1/course/' + courseId + '/period/' + period_name + '/year/' + academic_year,
                type:"GET",
                contentType: "application/json; charset=utf-8",
                async: true,
                success: function(data) {
                    displayPeriodInfo(data);
                },
                error : function(msg) {
                    alertify.error("Σφάλμα: " + msg.responseText);
                }
            });
        }

        function displayPeriodInfo(period) {

            if (period != null) {
                let startDate = moment(period.startDate).format('LL');
                let endDate = moment(period.endDate).format('LL');
                $("#period_info").html("Η επιλεγμένη περίοδος, αφορά το διάστημα μεταξύ <b>" + startDate + "</b> και <b>" + endDate + "</b>");
            }
            else {
                $("#period_info").html("Άγνωστη περίοδος");
            }
        }

        dashboard.broker.on("afterSelect.staffMember", function () {
            dashboard.classrooms.select2class();
        });

        dashboard.broker.on("afterSelect.course afterInit.course", function (event, msg) {

            let sel_supervisor_id = $("#_supervisor").val();
            let sel_course_id = msg.value;
            let _type = $("#_type").val();
            //console.log("selected courseId: " + sel_course_id);
            if (sel_course_id != null && sel_course_id !== "" && _type === "lecture") {
                dashboard.staffmembers.getAuthorizedStaffMembersOfCourse(sel_course_id, sel_supervisor_id);
                getSelectedPeriodBoundaries(sel_course_id);
            }
        });

        dashboard.broker.on("afterSelect.event", function () {
            let sel_class_id = $("#_classroom").val();
            dashboard.classrooms.fillAllClassSelect(sel_class_id);
        });
        dashboard.broker.on("afterInit.course", function () {
        });
    };

    $(document).ready(function () {

        dashboard.init();
        dashboard.staffmembers.init();
        dashboard.courses.init();
        dashboard.classrooms.init();
        dashboard.events.init();
    });

    dashboard.broker.getRootSitePath = function () {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let applicationName = _location.substring(0, applicationNameIndex) + '/';
        let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

        return _location.substring(0, webFolderIndex);
    };

    dashboard.broker.showInstantMessage = function(type, val) {

        //Override alertify defaults
        alertify.set('notifier','position', 'top-center');

        switch (type) {
            case "alert-success":
                alertify.success(val);
                break;
            case "alert-danger":
                alertify.error(val);
                break;
            case "alert-warning":
                alertify.warning(val);
                break;
            case "alert-info":
                alertify.info(val);
                break;
        }
    };
    dashboard.broker.selectPeriod = function(data) {

        if (data === "winter") {
            return language.winter;
        }
        else if (data === "intervening") {
            return language.intervening;
        }
        else if (data === "spring") {
            return language.spring;
        }
        else if (data === "summer") {
            return language.summer;
        }
    };
    dashboard.broker.selectDayOfWeek = function(data) {

        if (data === "MONDAY") {
            return language.MONDAY;
        }
        else if (data === "TUESDAY") {
            return language.TUESDAY;
        }
        else if (data === "WEDNESDAY") {
            return language.WEDNESDAY;
        }
        else if (data === "THURSDAY") {
            return language.THURSDAY;
        }
        else if (data === "FRIDAY") {
            return language.FRIDAY;
        }
        else if (data === "SATURDAY") {
            return language.SATURDAY;
        }
        else if (data === "SUNDAY") {
            return language.SUNDAY;
        }
    };

})();