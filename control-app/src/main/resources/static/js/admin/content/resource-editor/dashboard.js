(function () {
    'use strict';

    window.dashboard = window.dashboard || {};
    dashboard.broker = $({});

    dashboard.siteUrl    = "";
    dashboard.rid        = "";
    dashboard.baseUrl_pp = "";
    dashboard.baseUrl_mm = "";

    let page_select = "";
    let serialize_form;

    $(document).ready(function () {

        dashboard.init();

        let resource_type = $("#type").val();
        dashboard.classrooms.init();

        if (resource_type === "COURSE") {
            dashboard.staffmembers.init();
            dashboard.courses.init();
            page_select = "lecture-editor";
        }
        else if (resource_type === "EVENT") {
            dashboard.events.init();
            dashboard.events.select2event();
            dashboard.classrooms.init();
            dashboard.classrooms.selectaAll2class();
            page_select = "event-editor";
        }

        dashboard.video.init();
        dashboard.parousiasi.init();


    });

    dashboard.init = function () {

        //PRESERVE TAB AFTER RELOAD
        // use HTML5 localStorage object to save some parameter for the current tab locally in the browser and get it back to make the last active tab selected on page reload.
        //ON CHANGE TAB EVENT
        $('a[data-coreui-toggle="tab"]').on('show.coreui.tab', function(e) {
            localStorage.setItem('activeTab', $(e.target).attr('href'));
            //console.log($(e.target).attr('href'));
        });
        //LOAD ACTIVE TAB
        var activeTab = localStorage.getItem('activeTab');
        if(activeTab){
            $('#myTab a[href="' + activeTab + '"]').tab('show');
        }

        dashboard.rid        =  $("#rid").val();
        dashboard.siteUrl    =  dashboard.broker.getRootSitePath();
        dashboard.baseUrl_pp =  $("#mediaBaseUrl").val();
        dashboard.baseUrl_mm =  $("#streamingBaseUrl").val();

        let   msg_val   = $("#msg_val").val();
        let   msg_type  =$("#msg_type").val();

        if (msg_val !== '') {
            let message = {msg: "instant message", type: msg_type, val: msg_val};
            dashboard.broker.showInstantMessage(message.type ,message.val);
        }

        init_controls();
        init_local_events();


        serialize_form = $("#lecture-form").serialize();
    }
    function init_controls() {

        var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-toggle="tooltip"]'))
        var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new coreui.Tooltip(tooltipTriggerEl)
        });

        alertify.defaults.transition = "slide";
        alertify.defaults.theme.ok = "btn blue-btn-wcag-bgnd-color text-white";
        alertify.defaults.theme.cancel = "btn red-btn-wcag-bgnd-color text-white";
        alertify.defaults.theme.input = "form-control";

        //DATE
        $("#_date_part").datepicker({
            format: "yyyy-mm-dd",
            todayBtn: false,
            language: "el",
            autoclose: true,
            todayHighlight: false
        });
        //TIME
        $("#resource_hour").select2({
            minimumResultsForSearch: -1
        });
        $("#resource_minutes").select2({
            minimumResultsForSearch: -1
        });
        let $date_element = $("#_date");
        let resource_datetime = $date_element.val();
        if (resource_datetime === "") {
            resource_datetime = new Date();
            $("#resource_hour").val(moment(resource_datetime).hours()).trigger("change");
            $("#resource_minutes").val(moment(resource_datetime).minutes()).trigger("change");
            $("#_date_part").val(moment(resource_datetime).format('YYYY-MM-DD'));
        }
        else {
            //use UTC here because time comes with fixed time zone from db
            $("#resource_hour").val(moment.utc(resource_datetime).hours()).trigger("change");
            $("#resource_minutes").val(moment.utc(resource_datetime).minutes()).trigger("change");
            $("#_date_part").val(moment.utc(resource_datetime).format('YYYY-MM-DD'));
        }
        let hour = $("#resource_hour").val();
        let minute = $("#resource_minutes").val();
        let datetime_selected = moment(resource_datetime).hour(hour).minute(minute).format('YYYY-MM-DD'); //moment(resource_datetime).hour(hour).minute(minute).format('YYYY-MM-DDTHH:mm:ss[Z]');

        $("#_date_part").datepicker('setDate',datetime_selected);

        let _form_datetime= moment(datetime_selected).hour(hour).minute(minute).format('YYYY-MM-DDTHH:mm:ss[Z]');
        $("#_date").val(_form_datetime);

        //partNumber
        $("input[type='number']").inputSpinner();

        //AcademicYear
        $("#resource_ay").select2({
            placeholder: 'Επιλέξτε ακαδημαϊκό έτος',
            minimumResultsForSearch: -1
        });
        //Periods
        $("#resource_pd").select2({
            minimumResultsForSearch: -1
        });
        //Categories
        $(".js-category-tags").select2({
            placeholder: 'Επιλέξτε έως 3 θεματικές περιοχές',
            maximumSelectionLength: 3
        });

        //Topics
        $("#resource_tpcs").select2({
            placeholder: '',
            tags: true
        });
        //Publication toggle
        $("#publication_toggle").bootstrapToggle({
            on: "Δημόσιο",
            off: "Ιδιωτικό",
            onstyle: "success",
            offstyle: "secondary",
            size: "small"
        });

        //PlayerOptions
        $("#playerOptions_LicenseIntro_toggle").bootstrapToggle({
            on: '<i class="fas fa-power-off"></i>',
            off: '<i class="fas fa-ban"></i>',
            onstyle: "success",
            offstyle: "danger",
            size: "small"
        });
        $("#playerOptions_overlay_toggle").bootstrapToggle({
            on: '<i class="fas fa-power-off"></i>',
            off: '<i class="fas fa-ban"></i>',
            onstyle: "success",
            offstyle: "danger",
            size: "small"
        });
        //Licenses
        $("#resource_license").select2({
            placeholder: 'Επιλέξτε άδεια χρήσης',
            minimumResultsForSearch: -1,
        });
        //Language
        $("#resource_languaqe").select2({
            minimumResultsForSearch: -1
        });
        //TAGS
        $("#tag_ResApp_select").select2({
            minimumResultsForSearch: -1
        });
        $("#tag_MultUp_select").select2({
            minimumResultsForSearch: -1
        });
        $("#tag_MetEdt_select").select2({
            minimumResultsForSearch: -1
        });
        $("#tag_MultEdt_select").select2({
            minimumResultsForSearch: -1
        });
        $("#tag_MultRed_select").select2({
            minimumResultsForSearch: -1
        });
        $("#tag_PreUp_select").select2({
            minimumResultsForSearch: -1
        });
        $("#tag_PreSyn_select").select2({
            minimumResultsForSearch: -1
        });

        let inclMultimedia = $("#inclMultimedia").val();

        if (inclMultimedia === '1') {
            $("#publication_toggle").bootstrapToggle('enable');
            //Access Policy
            let access_policy = $("#accessPolicy").val();
            if ((access_policy === "private" || access_policy === "") && dashboard.rid !== "") {
                $("#delete-button").attr("disabled", false);
                $("#publication_toggle").bootstrapToggle('off')
            } else {
                $("#publication_toggle").bootstrapToggle('on')
                $("#delete-button").attr("disabled", true);
            }
        }
        else {
            $("#publication_toggle").bootstrapToggle('off');
            $("#publication_toggle").bootstrapToggle('disable');
            $("#delete-button").attr("disabled", false);
        }
    }

    function init_local_events() {

        $("#_date_part").on('changeDate', function (selected) {
            let sel_Date = new Date(selected.date.valueOf());
            let hour = $("#resource_hour").val();
            let minute = $("#resource_minutes").val();
            let datetime_selected = moment(sel_Date).hour(hour).minute(minute).format('YYYY-MM-DDTHH:mm:ss[Z]');

            $("#_date").val(datetime_selected);
            let message = {msg: "Date selected!", value: datetime_selected};
            dashboard.broker.trigger('afterSelect.date', [message]);
        });
        $("#resource_hour").on('select2:select', function (e) {
            let data = e.params.data;
            let hour = data.id;
            let sel_Date = $("#_date_part").val();
            let minute = $("#resource_minutes").val();
            let datetime_selected = moment(sel_Date).hour(hour).minute(minute).format('YYYY-MM-DDTHH:mm:ss[Z]');
            $("#_date").val(datetime_selected);
        });
        $("#resource_minutes").on('select2:select', function (e) {
            let data = e.params.data;
            let minute = data.id;
            let hour = $("#resource_hour").val();
            let sel_Date = $("#_date_part").val();
            let datetime_selected = moment(sel_Date).hour(hour).minute(minute).format('YYYY-MM-DDTHH:mm:ss[Z]');
            $("#_date").val(datetime_selected);
        });

        $(".submit_form").on('click',function(e){
            //enable controls before submit
            $("#publication_toggle").bootstrapToggle('enable');
            $("#lecture-form").submit();
        });

        $("#resource_ay").on('select2:select', function (e) {
            let data = e.params.data;
            let sel_year_Id = data.id;
            //trigger change event
            let message = {msg: "Year selected!", value: sel_year_Id};
            dashboard.broker.trigger('afterSelect.year', [message]);
        });

        $('#publication_toggle').change(function(e) {
            if (checkSaveWithIgnore() === false) {
                alertify.alert("<i style='color:red' class='fas fa-exclamation-triangle'></i> Πρόβλημα", "Η φόρμα έχει τροποποιηθεί. Αποθηκεύστε τις αλλαγές και δοκιμάστε πάλι");
                e.preventDefault();
            }
            else {
                let $lecture_form = $("#lecture-form");
                let data = $(this).prop('checked');
                if (data) {
                    let msg = '<p>Η καταχώρηση θα δημοσιοποιηθεί. <b>Είστε σίγουρος;</b></p>';
                    alertify.confirm('Δημοσιοποίηση', msg,
                        function () {
                            $lecture_form.attr("action", page_select + "?action=publish");
                            $("#publication_toggle").bootstrapToggle('enable');
                            $lecture_form.submit();
                        },
                        function () {
                            $("#publication_toggle").prop('checked', false).change();
                        }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
                } else {
                    let msg = '<p>Η καταχώρηση θα αφαιρεθεί από τη δημόσια πρόσβαση. <b>Είστε σίγουρος;</b></p>';
                    alertify.confirm('Δημοσιοποίηση', msg,
                        function () {
                            $lecture_form.attr("action", page_select + "?action=unpublish");
                            $("#publication_toggle").bootstrapToggle('enable');
                            $lecture_form.submit();
                        },
                        function () {
                            $("#publication_toggle").prop('checked', true).change()
                        }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
                }
            }
            e.preventDefault();
        });

        $("#delete-button").on('click',function(e){
            e.preventDefault();
            let $lecture_form = $("#lecture-form");
            let msg = '<p> Η Διάλεξη/Εκδήλωση θα διαγραφεί. <b>Είστε σίγουρος;</b></p>';
            msg += '<div class="font-weight-bold text-center">ΠΡΟΣΟΧΗ<br/>(Αν Υπάρχουν) Θα διαγραφούν τα αρχεία Βίντεο/Ήχου και Παρουσίασης<br/>';
            msg += 'Πριν σβήσετε την καταχώρηση, βεβαιωθείτε ότι δεν είναι δημοσιευμένη σε τρίτο ιστότοπο (opencourses, youtube κτλ)</div>';

            alertify.confirm('Διαγραφή Καταχώρησης', msg,
                function () {
                    $lecture_form.attr("action", page_select + "?action=delete");
                    $lecture_form.submit();
                },
                function () {
                }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
        });
        $("#copy-button").on('click',function(e){
            e.preventDefault();
            if (checkSaveWithIgnore() === false) {
                alertify.alert("<i style='color:red' class='fas fa-exclamation-triangle'></i> Πρόβλημα", "Η φόρμα έχει τροποποιηθεί. Αποθηκεύστε τις αλλαγές και δοκιμάστε πάλι");
            }
            else {
                let $lecture_form = $("#lecture-form");
                let msg = '<p> Θα δημιουργηθεί νέα διάλεξη με ταυτόσημα "Στοιχεία Διάλεξης". Η νέα διάλεξη θα ανοίξει για επεξεργασία.<b> Είστε σίγουρος;</b></p>';
                alertify.confirm('Δημιουργία Καταχώρησης με Αντιγραφή', msg,
                    function () {
                        $lecture_form.attr("action", page_select + "?action=copy");
                        $("#publication_toggle").bootstrapToggle('enable');
                        $lecture_form.submit();
                    },
                    function () {
                    }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
            }

        });
        $("#clone-button").on('click',function(e){
            e.preventDefault();
            if (checkSaveWithIgnore() === false) {
                alertify.alert("<i style='color:red' class='fas fa-exclamation-triangle'></i> Πρόβλημα", "Η φόρμα έχει τροποποιηθεί. Αποθηκεύστε τις αλλαγές και δοκιμάστε πάλι");
            }
            else {
                let $lecture_form = $("#lecture-form");
                let msg = '<p> Θα δημιουργηθεί νέα διάλεξη ΣΤΗΝ ΙΔΙΑ ΣΕΙΡΑ με ταυτόσημα "Στοιχεία Διάλεξης". Η νέα διάλεξη θα ανοίξει για επεξεργασία.<b> Είστε σίγουρος;</b></p>';
                alertify.confirm('Δημιουργία Καταχώρησης στη Σειρά', msg,
                    function () {
                        $lecture_form.attr("action", page_select + "?action=clone");
                        $("#publication_toggle").bootstrapToggle('enable');
                        $lecture_form.submit();
                    },
                    function () {
                    }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
            }
        });

        $(".edit_video_link").on('click',function(e){
            if (checkSaveWithIgnore() === false) {
                alertify.alert("<i style='color:red' class='fas fa-exclamation-triangle'></i> Πρόβλημα", "Η φόρμα έχει τροποποιηθεί. Αποθηκεύστε τις αλλαγές και δοκιμάστε πάλι");
                e.preventDefault();
            }
        });
        $("#pr_syncPp").on('click',function(e){
            if (checkSaveWithIgnore() === false) {
                alertify.alert("<i style='color:red' class='fas fa-exclamation-triangle'></i> Πρόβλημα", "Η φόρμα έχει τροποποιηθεί. Αποθηκεύστε τις αλλαγές και δοκιμάστε πάλι");
                e.preventDefault();
            }
        });
        $(".related_link").on('click',function(e){
            if (checkSaveWithIgnore() === false) {
                alertify.alert("<i style='color:red' class='fas fa-exclamation-triangle'></i> Πρόβλημα", "Η φόρμα έχει τροποποιηθεί. Αποθηκεύστε τις αλλαγές και δοκιμάστε πάλι");
                e.preventDefault();
            }
        });
        $(".cancel_button").on('click',function(e){
            e.preventDefault();
            let end_serialize = $("#lecture-form").serialize();
            if (serialize_form !== end_serialize) {
                let msg = '<div class="font-weight-bold">Οι αλλαγές θα χαθούν! Είστε σίγουρος?</div>';
                alertify.confirm('Προειδοποίηση', msg,
                    function () {
                        window.location.href = e.currentTarget.href;
                    },
                    function () {
                        e.preventDefault();
                    }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
            }
            else {
                window.location.href = e.currentTarget.href;
            }
        });

        function checkSave() {
            let end_serialize = $("#lecture-form").serialize();
            if (serialize_form !== end_serialize) {
                return false;
            }
            else {
                return true;
            }
        }

        function checkSaveWithIgnore() {
            let serial_form_without_ignored = "";
            let ser_array = serialize_form.split("&");
            $.each(ser_array, function (i, input) {
                let _name = input.split("=")[0];
                if (_name !== "status.inclMultimedia" && _name !== "status.inclPresentation") {
                    serial_form_without_ignored += input;
                }
            });

            let end_serialize = $("#lecture-form").serialize();
            let end_form_without_ignored = "";
            let end_array = end_serialize.split("&");
            $.each(end_array, function (i, input) {
                let _name = input.split("=")[0];
                if (_name !== "status.inclMultimedia" && _name !== "status.inclPresentation") {
                    end_form_without_ignored += input;
                }
            });

            return serial_form_without_ignored === end_form_without_ignored;

        }
    }


    //Global Events
    dashboard.broker.on("afterSelect.staffMember afterInit.staffMember", function () {
        dashboard.classrooms.select2class();
    });

    dashboard.broker.on("afterSelect.course afterInit.course", function (event, msg) {

        let sel_supervisor_id = $("#supervisor_id").val();
        let sel_course_id = msg.value;
        //console.log("selected courseId: " + sel_course_id);
        if (sel_course_id != null && sel_course_id !== "") {
            dashboard.staffmembers.getAuthorizedStaffMembersOfCourse(sel_course_id, sel_supervisor_id);
            $("#course_info_button").show();
            //fill categories from course info if NOT filled by user
            let $sel2_element = $("#event_cats");
                let course_categories = $sel2_element.val();
            if (course_categories.length === 0) {
                dashboard.courses.getAndSetCourseCategories(sel_course_id, $sel2_element);
            }
            //set period based on course, date, year
            setPeriodBySelections();
        }
        else {
            $("#course_info_button").hide();
        }
    });

    dashboard.broker.on("afterSelect.date", function (event, msg) {
        setPeriodBySelections();
    });
    dashboard.broker.on("afterSelect.year", function (event, msg) {
        setPeriodBySelections();
    });

    dashboard.broker.on("upload_state.changed", function (event, msg) {

        // console.info("upload_state.changed:" + msg.value);
        if (msg.value === plupload.STARTED)    {
            dashboard.video.started();
        }
        else if (msg.value === plupload.STOPPED)  {
            //do nothing
        }
    });

    dashboard.broker.on("upload_state.finished", function () {

        dashboard.video.upload_hide();
        dashboard.player.setAndShowPlayer();
        //console.log(msg.filename);
        //Language
        $("#resource_languaqe").select2({});
        //PlayerOptions
        //PlayerOptions
        $("#playerOptions_LicenseIntro_toggle").bootstrapToggle({
            size: 'small',
            onstyle: 'info'
        });
        $("#playerOptions_overlay_toggle").bootstrapToggle({
            on: '<i class="fas fa-power-off"></i>',
            off: '<i class="fas fa-ban"></i>',
            onstyle: "success",
            offstyle: "danger",
            size: "small"
        });
        $("#inclMultimedia").val(1);
        //enable play button (top)
        $("#play-d").removeClass("disabled").removeAttr("aria-disabled");
        $(".save_warning").hide();
    });

    dashboard.broker.on("remove.mmEdit", function (event) {
        let $lecture_form = $("#lecture-form");
        if (event.type === "remove") {
            let msg = '<p>Το αρχείο βίντεο της καταχώρησης θα διαγραφεί (δεν υπάρχει τρόπος επαναφοράς). <b>Είστε σίγουρος;</b></p>';
            alertify.confirm('Διαγραφή Αρχείου', msg,
                function () {
                    $lecture_form.attr("action", page_select + "?action=mm_delete");
                    $lecture_form.submit();
                },
                function () {
                }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
        }
    });

    dashboard.broker.on("pp_upload_state.changed", function (event, msg) {

        // console.info("upload_state.changed:" + msg.value);
        if (msg.value === plupload.STARTED)    {
            dashboard.parousiasi.started();
        }
        else if (msg.value === plupload.STOPPED)  {
            // do nothing
        }
    });

    dashboard.broker.on("pp_upload_state.finished", function (event, msg) {
        dashboard.parousiasi.setMode("slides");
        dashboard.parousiasi.getResourceSlides(msg.id,"details");
        $("#inclPresentation").val("1");
        $(".save_warning").hide();
    });

    dashboard.broker.on("remove.ppEdit", function (event) {
        let $lecture_form = $("#lecture-form");
        if (event.type === "remove") {
            let msg = '<p>Το αρχείο παρουσίασης της καταχώρησης θα διαγραφεί (δεν υπάρχει τρόπος επαναφοράς). <b>Είστε σίγουρος;</b></p>';
            alertify.confirm('Διαγραφή Αρχείου', msg,
                function () {
                    $lecture_form.attr("action", page_select + "?action=delete_pp");
                    $lecture_form.submit();
                },
                function () {
                }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
        }
    });

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

    dashboard.broker.getRootSitePath = function () {
        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let applicationName = _location.substring(0, applicationNameIndex) + '/';
        let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

        return _location.substring(0, webFolderIndex);
    };

    function setPeriodBySelections() {
        let resource_date   = $("#_date_part").val();
        let course_id       = $("#courses_s2").val();
        let academic_year   = $("#resource_ay").val();
        let _$period_el     = $("#resource_pd");
        if (resource_date !== "" && course_id !== "" && course_id != null && academic_year !== "" && _$period_el != null) {
            let _date_moment = moment(resource_date).unix();
                dashboard.courses.getAndSetPeriodNameByCourseAndDate(course_id,_date_moment,academic_year,_$period_el);
        }
    }
})();