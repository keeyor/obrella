(function () {
    'use strict';

    window.dashboard = window.dashboard || {};
    dashboard.broker = $({});

    dashboard.siteUrl   = "";
    dashboard.app_path  = "";

    let LiveLectures_DT;
    let LiveScheduledEvents_DT;
    let LiveToday_DT;

    dashboard.init = function () {

        dashboard.siteUrl = dashboard.broker.getRootSitePath();
       // dashboard.departments.init();

        let today = moment().format("LL");
        $("#daily_date_now").html(today);

     };

    $(document).ready(function () {

        dashboard.init();
        define_events();
        init_controls();
        dashboard.broker.initTimeTableLectureDT_SERVER();
        dashboard.broker.initTimeTableTodayDT_SERVER();
    });

    //"ajax":  dashboard.siteUrl + '/api/v1/live/liveLectures',
    //
    dashboard.broker.initTimeTableLectureDT_SERVER = function() {

        let url = dashboard.siteUrl + '/api/v1/live/liveLectures';
        let container = $("#live-list-details");

        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.data, function (index, el) {
                    let access = el.access;
                    let live_url;
                    if (access === "open" || access === "password") {
                        live_url = "live_player";
                    }
                    else if (access === "sso") {
                        live_url = "cas/live_player";
                    }
                    let html = '';
                        html += '<div class="mt-2" style="font-weight: 500">' + (index+1) + '. Τμήμα ' + el.department.title
                             + ' - ' + el.supervisor.name
                             + '</div>';
                        html +=
                                '<div class="card-header row">';
                                    html += '<div class="col-2" style="display: flex;align-items: center">' +
                                                '<a class="text-decoration-none" target="_blank" href="' + live_url + '?id=' + el.id + '">' +
                                                '<img alt="live-broadcast status" src="' + dashboard.siteUrl + '/public/images/icons/IconLive25x25.png"></a>' +
                                            '</div>';
                                     html += '<div class="vr"></div>';
                                     html += '<div class="col-9">' +
                                            '<i class="icon-time-duration me-1"></i>' +  getStartTimeAndDuration(el.date, el.realDuration) + ': ' +
                                            '<a class="text-dark text-decoration-none blue-link-wcag-color" target="_blank" href="' + live_url + '?id=' + el.id + '">' + el.title + '</a>';

                                            html += '<div class="row  row-cols-auto mt-2" style="font-size: 0.9em">';
                                                if (access === "open") {
                                                    html += '<div class="col"><i class="fas fa-lock-open ms-lg-1 me-1"></i>Ελεύθερη</div>';
                                                }
                                                else if (access === "sso") {
                                                    html += '<div class="col">' +   '<img class="ms-lg-1 me-1" src="' + dashboard.siteUrl +
                                                                                    '/public/images/icons/IconMetadosiStatusUserName.png" width="20px" alt="">' + '' +
                                                                                    'Ιδρυματικός Λογαριασμός</div>';
                                                }
                                                else if (access === "password") {
                                                    html += '<div class="col"><i class="fas fa-lock  ms-lg-1 me-1"></i>Κωδικός Μετάδοσης</div>';
                                                }
                                                let recording = el.recording;
                                                if (recording) {
                                                    html += '<div class="col">' +   '<img class="me-1" width="16px"  src="' + dashboard.siteUrl +
                                                                                    '/public/images/icons/IconRecAuto.png" alt="" src="">' +
                                                                                    'Kαταγραφή</div>';
                                                }
                                                else {
                                                    html += '<div class="col">' +   '<img class="me-1" width="16px"  src="' + dashboard.siteUrl +
                                                                                    '/public/images/icons/IconRecOff.png" alt="" src="">' +
                                                                                    'Χωρίς Kαταγραφή</div>';
                                                }
                                                html += '<div class="col"><i class="fas fa-map-marked ms-lg-1 me-1"></i>' + el.classroomName + '</div>';
                                                html += '<div class="col"></div>';
                                            html += '</div>';
                                     html += '</div>';
                                    html += '</div>';
                                html += '</div>';


                    container.append(html);
                });
                if (data.data.length < 1) {
                    container.html("δεν βρέθηκαν μεταδόσεις σε εξέλιξη");
                }
             }
        });

    }


    dashboard.broker.initTimeTableTodayDT_SERVER = function() {

        let url = dashboard.siteUrl + '/api/v1/live/liveToday/c';
        let container = $("#live-dayList-details");

        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.data, function (index, el) {
                    let status = getPastPresentFuture(el.date,el.realDuration);

                    let access = el.access;
                    let live_url;
                    if (access === "open" || access === "password") {
                        live_url = "live_player";
                    }
                    else if (access === "sso") {
                        live_url = "cas/live_player";
                    }
                    let html = '';
                    html += '<div class="mt-2" style="font-weight: 500">' + (index+1) + '. Τμήμα ' + el.department.title
                        + ' - ' + el.supervisor.name
                        + '</div>';
                    html +=
                        '<div class="card-header row">';
                            html += '<div class="col-2" style="display: flex;align-items: center">';
                                if (status === "live") {
                                    html += '<a class="text-decoration-none" title="ζωντανή μετάδοση" target="_blank" href="' + live_url + '?id=' + el.id + '">';
                                    html += '<img alt="live-broadcast status" src="' + dashboard.siteUrl + '/public/images/icons/IconLive25x25.png"></a>';
                                }
                                else if (status === "past") {
                                    html += '<i class="fas fa-bell-slash mr-1 fa-2x text-muted" title="ολοκληρωμένη μετάδοση"></i>';
                                }
                                else if (status === "future") {
                                    html += '<i class="far fa-clock  fa-2x"  title="αργότερα σήμερα"></i>';
                                }
                        html += '</div>';
                        html += '<div class="vr"></div>';
                        html += '<div class="col-9">' +
                        '<i class="icon-time-duration me-1"></i>' +  getStartTimeAndDuration(el.date, el.realDuration) + ': ';
                        if (status === "live") {
                            html += '<a class="text-dark text-decoration-none blue-link-wcag-color" target="_blank" href="' + live_url + '?id=' + el.id + '">' + el.title + '</a>';
                        }
                        else if (status === "past" || status === "future") {
                            html += '<span class="text-dark text-decoration-none" style="font-weight: 500">' + el.title + '</span>';
                        }


                    let recording = el.recording;

                    html += '<div class="row  row-cols-auto mt-2" style="font-size: 0.9em">';
                    if (status === "live" || status === "future") {
                        if (access === "open") {
                            html += '<div class="col"><i class="fas fa-lock-open ms-lg-1 me-1"></i>Ελεύθερη</div>';
                        } else if (access === "sso") {
                            html += '<div class="col">' + '<img class="ms-lg-1 me-1" src="' + dashboard.siteUrl +
                                '/public/images/icons/IconMetadosiStatusUserName.png" width="20px" alt="">' + '' +
                                'Ιδρυματικός Λογαριασμός</div>';
                        } else if (access === "password") {
                            html += '<div class="col"><i class="fas fa-lock  ms-lg-1 me-1"></i>Κωδικός Μετάδοσης</div>';
                        }
                    }
                    else if (status === "past") {
                        let departmentId = el.department.id;
                        let courseId = el.course.id;
                        let publication = el.publication;
                        if (courseId !== undefined && recording && publication === "public") {
                            let date = moment.unix(el.date).format('YYYY-MM-DD');
                            let ret_lnk = '<a class="blue-link-wcag-color" title="αναζήτηση καταγραφής στις καταγεγραμμένες διαλέξεις"' +
                                ' target="_blank" ' +
                                ' href="' + 'https://dimos.med.uoa.gr/vod/search?d=' + departmentId + '&c=' + courseId + '&dt=' + date + '">' +
                                ' <i class="fas fa-search  ms-lg-1 me-1"></i>Αναζήτηση' +
                                ' </a>';
                            html += ret_lnk;
                        }
                    }
                    if (recording) {
                        html += '<div class="col">' +   '<img class="me-1" width="16px"  src="' + dashboard.siteUrl +
                            '/public/images/icons/IconRecAuto.png" alt="" src="">' +
                            'Kαταγραφή</div>';
                    }
                    else {
                        html += '<div class="col">' +   '<img class="me-1" width="16px"  src="' + dashboard.siteUrl +
                            '/public/images/icons/IconRecOff.png" alt="" src="">' +
                            'Χωρίς Kαταγραφή</div>';
                    }
                    html += '<div class="col"><i class="fas fa-map-marked ms-lg-1 me-1"></i>' + el.classroomName + '</div>';
                    html += '<div class="col"></div>';
                    html += '</div>';
                    html += '</div>';
                    html += '</div>';
                    html += '</div>';

                    container.append(html);
                });
                if (data.data.length < 1) {
                    container.html("δεν βρέθηκαν προγραμματισμένες μεταδόσεις για σήμερα");
                }
            }
        });

    }

    function getStartTimeAndDuration(data, realDuration) {
        if (data != null && data !== "") {
            let display_date = moment.unix(data).format('HH:mm');
            //duration
            let end_time;
            let display_duration = "";
            if (realDuration != null) {
                let duration_split = realDuration.split(":");
                let hours = parseInt(duration_split[0]);
                let minutes = parseInt(duration_split[1]);
                display_duration = hours + 'h ' + minutes + 'm';
                end_time = moment.unix(data).add(hours,'hours').add(minutes,'minutes');
            }
            return '<span style="font-weight: 470">' + display_date + ' - ' + end_time.format('HH:mm') + '</span>';
        }
        else {
            return '';
        }
    }

    function getPastPresentFuture(start_time, duration) {

        let start_time_moment = moment.unix(start_time);
        let end_time = getBroadcastsEndTime(start_time,duration);
        let now = moment();
        if (now.isAfter(end_time)) {
            return "past";
        }
        else if (now.isBefore(start_time_moment)) {
            return "future";
        }
        else if (now.isAfter(start_time_moment) && now.isBefore(end_time)) {
            return "live";
        }
    }
    
    
    dashboard.broker.reloadTimeTableLectureDT_SERVER = function() {
        dashboard.broker.initTimeTableLectureDT_SERVER();
    }
    dashboard.broker.reloadTimeTableTodayEventsDT_SERVER = function() {
        dashboard.broker.initTimeTableTodayDT_SERVER();
    }

    function getBroadcastsEndTime(start_time, duration) {
        let duration_split = duration.split(":");
        let hours = parseInt(duration_split[0]);
        let minutes = parseInt(duration_split[1]);
        let end_time = moment.unix(start_time).add(hours,'hours').add(minutes,'minutes');
        return end_time;
    }
    function set_display_results() {

        $("#stats-info-lectures").html("");
        let total = LiveLectures_DT.rows().count();
        if (total > 0) {
            $("#lecture_results").html("Βρέθηκαν " + total + " ζωντανές <b>Διαλέξεις</b> σε εξέλιξη");
            $("#nof-lectures").val(total);
        }
        else {
            $("#lecture_results").html("Δεν βρέθηκαν ζωντανές <b>Διαλέξεις</b> σε εξέλιξη");
            $("#nof-lectures").val(0);
        }
    }
    function set_display_results_events() {

        $("#stats-info-events").html("");
        let total = LiveScheduledEvents_DT.rows().count();
        if (total > 0) {
            $("#events_results").html("Βρέθηκαν " + total + " ζωντανές Εκδηλώσεις σε εξέλιξη");
            $("#nof-events").val(total);
        }
        else {
            $("#events_results").html("Δεν υπάρχουν ζωντανές <b>Εκδηλώσεις</b> σε εξέλιξη");
            $("#nof-events").val(0);
        }
        dashboard.wowza.loadStatistics();
    }
    function set_display_results_today() {

       /* $("#stats-info-events").html("");
        let total = LiveScheduledEvents_DT.rows().count();
        if (total > 0) {
            $("#events_results").html("Βρέθηκαν " + total + " ζωντανές εκδηλώσεις σε εξέλιξη");
            $("#nof-events").val(total);
        }
        else {
            $("#events_results").html("Δεν Βρέθηκαν ζωντανές εκδηλώσεις σε εξέλιξη");
            $("#nof-events").val(0);
        }
        dashboard.wowza.loadStatistics();*/
    }

    dashboard.broker.getRootSitePath = function () {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let applicationName = _location.substring(0, applicationNameIndex) + '/';
        let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

        return _location.substring(0, webFolderIndex);
    };

    function define_events() {
        let $body = $("body");

        $body.on('click','.stop_live', function(e){
            let data_id = $(this).data("target");
            let data_date = $(this).data("date");
            let _date = moment(data_date).format("YYYY-MM-DD");
            //let display_time = moment(data_date).format("HH:mm");
            let data_title =  $(this).data("title");

            let msg = '<div><b>Η μετάδοση</b> του Μαθήματος/Εκδήλωσης <b>"' + data_title + '"</b>' +
                ' θα σταματήσει <b>ΑΜΕΣΩΣ</b>. Είστε σίγουρος?' +
                '<br/><br/> Πληκτρολογήστε (προαιρετικά) την αιτία της ακύρωσης' +
                '</div>';
            alertify.prompt('Ακύρωση Μετάδοσης', msg,'',
                function (evt,value) {
                    postImmediateStreamStop(data_id,_date,value);
                },
                function () {
                }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
            e.preventDefault();
        });

        $(".password_protected").on('click',function(e){
            let _id = $(this).data("target");
            let _location = $(this).attr("href");
            alertify.prompt('Ακύρωση Μετάδοσης', 'Κωδικός Πρόσβασης','',
                function (evt,value) {
                    confirmPassword(_id,value,_location);
                },
                function () {
                }).set('labels', {ok: 'Οκ!', cancel: 'Ακύρωση'});
            e.preventDefault();
        });

        $("#refresh_table").on('click',function(e){
            $("#live-list-details").empty();
            $("#live-dayList-details").empty();
            dashboard.broker.reloadTimeTableLectureDT_SERVER();
            dashboard.broker.reloadTimeTableTodayEventsDT_SERVER();
            e.preventDefault();
        });

        $('#sort_select').on('select2:select', function (e) {
            let data = e.params.data;
            let id = data.id;
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.set("sort", id);
            queryParams.delete("skip");
            window.location.href = "live?" + queryParams;
        });

        $('#direction_select').on('select2:select', function (e) {
            let data = e.params.data;
            let id = data.id;
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.set("direction", id);
            queryParams.delete("skip");
            window.location.href = "live?" + queryParams;
        });

        $("#department-columns, #category-list").on('click','a.dropdown-toggle', function() {
            if (!$(this).next().hasClass('show')) {
                $(this).parents('.dropdown-menu').first().find('.show').removeClass('show');
            }
            var $subMenu = $(this).next('.dropdown-menu');
            $subMenu.toggleClass('show');

            $(this).parents('li.nav-item.dropdown.show').on('hidden.bs.dropdown', function() {
                $('.dropdown-submenu .show').removeClass('show');
            });
            return false;
        });

        $("#play_edited_video").on('click',function(e) {
            let info = $(this).data("info").split("::");
            let id = info[0];
            let title = info[1];
            $("#playEditedModalTitle").text(title);
            load_edited_video(id);
            e.preventDefault();
        })

    }
    function init_controls() {
        $("#aFilterAccessPolicy").select2({
            minimumResultsForSearch: -1 //hides the searchbox
        });
        $("#aFilterType").select2({
            minimumResultsForSearch: -1
        });
        $("#aFilterTags").select2({
            minimumResultsForSearch: -1
        });

        //init sort select2
        let $sort_select = $("#sort_select");
        $sort_select.select2({minimumResultsForSearch: Infinity});
        let sortby = $("#sortField").val();
        $sort_select.val(sortby).trigger("change");

        //init direction select2
        let $direction_select = $("#direction_select");
        $direction_select.select2({minimumResultsForSearch: Infinity});
        let sortDirection = $("#directionField").val();
        $direction_select.val(sortDirection).trigger("change");


        alertify.defaults.transition = "slide";
        alertify.defaults.theme.ok = "btn btn-primary";
        alertify.defaults.theme.cancel = "btn btn-danger";
        alertify.defaults.theme.input = "form-control";

        $("#last_updated").html(" ( τ.ε. " + moment(new Date()).format('HH:mm:ss') + " )");
    }


    function confirmPassword(id,passwd, location) {

        $.ajax({
            url: dashboard.siteUrl + '/api/v1/schedule_table/confirm_password/' + id + '/cd/' + passwd,
            type:"POST",
            contentType: "application/json; charset=utf-8",
            data: 		  JSON.stringify(passwd),
            async: true,
            success: function(data) {
                if (data === "1") {
                    window.location.href = location + "&access=restricted";
                }
                else {
                    alert("Λάθος Κωδικός");
                }
            },
            error : function(msg) {
                alertify.error("Σφάλμα: " + msg.responseText);
            }
        });
    }

    function postImmediateStreamStop(id,date,reason) {
        let cancellation = {
            title : reason,
            date : date
        };
        $.ajax({
            url: dashboard.siteUrl + '/api/v1/schedule_table/stream_cancellation/' + id,
            type:"POST",
            contentType: "application/json; charset=utf-8",
            data: 		  JSON.stringify(cancellation),
            async: true,
            success: function() {
                window.location.replace(window.location.href); //location.reload();
                alertify.notify("Η ακύρωση καταχωρήθηκε" , "success");
            },
            error : function(msg) {
                alertify.error("Σφάλμα: " + msg.responseText);
            }
        });
    }

    function checkTime(i) {
        if (i < 10) {
            i = "0" + i;
        }
        return i;
    }
    function startTime() {
        var today = new Date();
        var h = today.getHours();
        var m = today.getMinutes();
        var s = today.getSeconds();
        // add a zero in front of numbers<10
        m = checkTime(m);
        s = checkTime(s);
        document.getElementById('time').innerHTML = ' ' +  h + ":" + m + ":" + s;
        setTimeout(function() {
            startTime();
            if (s === "00") {
                $("#live-list-details").empty();
                $("#live-dayList-details").empty();
                dashboard.broker.reloadTimeTableLectureDT_SERVER();
                dashboard.broker.reloadTimeTableTodayEventsDT_SERVER();
            }
        }, 500);
    }
    startTime();
})();
