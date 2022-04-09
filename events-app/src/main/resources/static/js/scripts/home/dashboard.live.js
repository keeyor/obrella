(function () {
    'use strict';

    dashboard.live = dashboard.live || {};

    let LiveScheduledEvents_DT;

    dashboard.live.init = function () {
        dashboard.live.loadLiveScheduledEventsMediaObject();
        dashboard.live.loadNextBroadcastsExcludingLiveMediaObject();

        $("#refresh_table").on('click',function(e){
            dashboard.live.loadLiveScheduledEventsMediaObject();
            dashboard.live.loadNextBroadcastsExcludingLiveMediaObject();
            e.preventDefault();
        });
     };

    dashboard.live.loadLiveScheduledEventsMediaObject = function() {
        //$("#LiveBroadcastsV4").html("");
        $.ajax({
            url: dashboard.siteUrl + '/api/v1/timetable_daterange/users/live',
            cache: false
        })
            .done(function( data ) {
                let html = '';
                let live_events = data.searchResultList;
                let live_counter = 0
                live_events.forEach(function(event) {
                    live_counter++
                    html +=
                        '<li class="media">\n' +
                        '  <div class=" mr-3" style="width: 64px;height: 64px"><span class="icon-live-lecture" style="color: red;font-size: 2em"></span></div>' +
                        '\n' +
                        '  <div class="media-body">\n' +
                        '    <h6 class="mt-0 mb-1">';
                        if (event.accessPolicy === 'open') {
                            html += '    <a target="_blank" class=" text-danger font-weight-bolder" href="./live_player?id=' + event.streamName + '">' + event.title + '</a></h6>\n' +
                            '    Ζωντανή Μετάδοση :: ' + event.classroomName + '\n';
                        }
                        else if (event.accessPolicy === 'sso') {
                            html += '    <a target="_blank" class=" text-danger font-weight-bolder" href="./cas/live_player?id=' + event.streamName + '">' + event.title + '</a></h6>\n' +
                            '    Ζωντανή Μετάδοση :: ' + event.classroomName + ' <b> - πρόσβαση με λογαριασμό ιδρύματος - </b>' + '\n';
                        }
                        else if (event.accessPolicy === 'password') {
                            html += '    <a target="_blank" class=" text-danger font-weight-bolder" href="./live_player?id=' + event.streamName + '">' + event.title + '</a></h6>\n' +
                                '    Ζωντανή Μετάδοση :: ' + event.classroomName + ' <b> - απαιτείται κωδικός πρόσβασης - </b>' + '\n';
                        }
                            if (event.youTubeBroadcast != null && event.youTubeBroadcast.broadcast) {
                                html += '<p class="text-muted">Πραγματοποιείτε παράλληλη μετάδοση στο YouTube: ';
                                html += '<i class="fab fa-youtube mr-2" style="color:red;"></i>' +
                                    '<a href="https://www.youtube.com/watch?v=' + event.youTubeBroadcast.broadcastId + '" target="_blank">Σύνδεσμος Μετάδοσης (not valid for demo!)</a></p>';
                            }
                            html += '</div>\n' +
                        '</li>'
                });
                html += '';
                if (live_counter > 0) {
                    html = "<div class='mb-2'><b>" + live_counter + "</b> ζωντανές μεταδόσεις σε εξέλιξη" + "</div>" + html;
                    $("#LiveBroadcastsV4").html(html);
                }
                else {
                    $("#LiveBroadcastsV4").html("Δεν υπάρχουν σε εξέλιξη ζωντανές μεταδόσεις");
                }
            });
    }
    dashboard.live.loadNextBroadcastsExcludingLiveMediaObject = function() {
        //$("#nextAndLiveBroadcastsV4").html("");
        $.ajax({
            url: dashboard.siteUrl + '/api/v1/timetable_daterange/users/next',
            cache: false
        })
            .done(function( data ) {
                let html = '';
                //data.forEach(function(event, index) {
                for (const [index, event] of data.entries()) {
                    html +=
                        '<li class="media mt-3">\n';
                            let supportWebDir = $("#sSupportWedDir").val();
                            let event_default_img = supportWebDir + "event-default.png";
                            html += '<img class="bd-placeholder-img mr-3 mb-2" src="' + event_default_img + '" alt="..." src="" style="height: 32px;width: 32px;">';
                            html += '\n' +
                            '  <div class="media-body">\n' +
                            '    <h6 class="mt-0 mb-1">' + event.scheduledEvent.title + '</h6>\n' +
                            '    Προγραμματισμένη Εκδήλωση :: ' + moment(event.date).format('LL')  + ' Ώρα: '  + event.startTime  + '\n';
                                if (event.youTubeBroadcast != null && event.youTubeBroadcast.broadcast) {
                                    let startTime = event.startTime;
                                    let broadcastHour = startTime.split(":")[0];
                                    let broadcastMinute = startTime.split(":")[1];
                                    let broadcast_datetime = moment(event.date).add(broadcastHour,'hours').add(broadcastMinute,'minutes');
                                    let publish_link_datetime = moment(broadcast_datetime).subtract(24,'hours');
                                    html += '<p class="text-muted">' + '<i class="fab fa-youtube mr-2" style="color:red"></i>'
                                         + 'θα πραγματοποιηθεί παράλληλη μετάδοση στο <b>YouTube</b>. ';
                                    if (publish_link_datetime.isAfter(broadcast_datetime)) {
                                        html += '<a href="https://www.youtube.com/watch?v=' + event.youTubeBroadcast.broadcastId + '" target="_blank">Σύνδεσμος Μετάδοσης</a> (not valid for demo!)</p>';
                                    }
                                    else {
                                        html +=  'Ο σύνδεσμος θα δημοσιευτεί 24 ώρες πριν την έναρξη</p>';
                                    }
                                }
                        html += '</div>\n' +
                        '</li>';
                    if (index === 1) {
                        break;
                    }
                }
                html +=
                    '<li class="media mt-3">\n' +
                    '  <div class=" mr-3 mb-2" style="width: 32px;height: 32px"><span class="icon-scheduled" style="font-size: 2em"></span></div>' +
                    '\n' +
                    '  <div class="media-body">\n' +
                    '    <h6 class="mt-0 mb-1">' +
                    '    <a target="_blank" href="calendar"  class="blue-link-wcag-color">Ημερολόγιο Μεταδόσεων</a></h6>\n' +
                    '    Συμβουλευτείτε το αναλυτικό ημερολόγιο μεταδόσεων '  + '\n' +
                    '  </div>\n' +
                    '</li>'
                html += '';
                $("#nextAndLiveBroadcastsV4").html(html);
            });
    }
    dashboard.live.initTimeTableScheduledEventsDT_SERVER = function() {

        LiveScheduledEvents_DT = $("#liveScheduledEventsDataTable").DataTable( {
            "processing": true,
            "ajax":  dashboard.siteUrl + '/api/v1/live/liveScheduledEvents',
            pageLength : 25,
            order: [[2, 'asc']],
            dom: "t",
            "columns": [
                {"data": null}, //index :: 0
                {"data": "id"}, //1
                {"data": "title"}, //2
                {"data": "date"},
                {"data": null},
                {"data": "classroomName"} //5
            ],
            "aoColumnDefs": [
                {
                    "aTargets": [0,1,5],
                    "visible": false
                },
                {
                    "aTargets": [3],
                    "mData": "date",
                    responsivePriority: 1,
                    "className" : "",
                    "mRender": function (data,type,row) {
                        if (data != null && data !== "") {
                            let display_date = moment.unix(data.epochSecond).format('HH:mm');
                            //duration
                            let display_duration = "";
                            let realDuration = row["realDuration"];
                            if (realDuration != null) {
                                let duration_split = realDuration.split(":");
                                let hours = parseInt(duration_split[0]);
                                let minutes = parseInt(duration_split[1]);
                                display_duration = hours + 'h ' + minutes + 'm';
                            }
                            let hiddenDataForSorting = '<span style="display:none">' + data.epochSecond + '</span>';
                            return hiddenDataForSorting + '<span>' + display_date + '  (' + display_duration + ')</span>';
                        }
                        else {
                            return "";
                        }
                    }
                },
                {
                    "aTargets": [4],
                    "mData": "date",
                    responsivePriority: 1,
                    "className" : "",
                    "mRender": function (data,type,row) {
                        if (data != null && data !== "") {
                            let start_time = row["date"];
                            let duration_split = row["realDuration"].split(":");
                            let hours = parseInt(duration_split[0]);
                            let minutes = parseInt(duration_split[1]);
                            let end_time = moment.unix(start_time.epochSecond).add(hours,'hours').add(minutes,'minutes');
                            return end_time.format('HH:mm');
                        }
                        else {
                            return "";
                        }
                    }
                },
                {
                    "aTargets": [2],
                    "mData": "title",
                    responsivePriority: 1,
                    "className" : "dt-nowrap",
                    "mRender": function (data,type,row) {
                        let broadcast = row["broadcast"];
                        let ret = "";
                        if (broadcast) {
                            let id = row["id"];
                            if (row["access"] === 'open') {
                                ret += '<a  role="button" title="Μετάβαση στη ζωντανή μετάδοση" class="btn btn-sm text-white btn-danger play_live" ' +
                                    ' target="_blank"  href="' + dashboard.siteUrl + '/live_player?id=' + id + '"> ' +
                                    ' <i class="fas fa-play"></i> ' + data +
                                    '</a>'
                            } else if (row["access"] === 'sso') {
                                ret += '<a  role="button" title="Μετάβαση στη ζωντανή μετάδοση" class="btn text-white btn-danger play_live" ' +
                                    ' target="_blank"  href="' + dashboard.siteUrl + '/cas/live_player?id=' + id + '"> ' +
                                    ' <i class="fas fa-play"></i> ' + data +
                                    '</a>'
                            } else if (row["access"] === 'password') {
                                ret += '<a  role="button" title="Μετάβαση στη μετάδοση" class="btn text-white btn-danger play_live" ' +
                                    ' target="_blank"  href="' + dashboard.siteUrl + '/secure/live_player?id=' + id + '"> ' +
                                    ' <i class="fas fa-play"></i> ' + data +
                                    '</a>'
                            }
                        }
                        else {
                            ret += data;
                        }
                        return ret;
                    }
                }
            ],
            "initComplete": function(settings, json) {
                set_display_results_events(json);
            }
        });
        LiveScheduledEvents_DT.on( 'order.dt search.dt', function () {
            LiveScheduledEvents_DT.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
                cell.innerHTML = i+1;
            } );
        }).draw();
        LiveScheduledEvents_DT.on( 'page.dt', function (json) {
            set_display_results_events(json);
        } );
    }

    dashboard.live.reloadTimeTableScheduledEventsDT_SERVER = function() {
        LiveScheduledEvents_DT.ajax.reload(set_display_results_events);
    }

    function getBroadcastsEndTime(start_time, duration) {
        let duration_split = duration.split(":");
        let hours = parseInt(duration_split[0]);
        let minutes = parseInt(duration_split[1]);
        let end_time = moment.unix(start_time.epochSecond).add(hours,'hours').add(minutes,'minutes');
        return end_time;
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

    }

    dashboard.live.getRootSitePath = function () {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let applicationName = _location.substring(0, applicationNameIndex) + '/';
        let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

        return _location.substring(0, webFolderIndex);
    };

    function define_events() {
        let $body = $("body");

        $("#refresh_table").on('click',function(e){
            dashboard.live.reloadTimeTableScheduledEventsDT_SERVER();
            e.preventDefault();
        });
    }
    function init_controls() {

        alertify.defaults.transition = "slide";
        alertify.defaults.theme.ok = "btn btn-primary";
        alertify.defaults.theme.cancel = "btn btn-danger";
        alertify.defaults.theme.input = "form-control";

        $("#last_updated").html(" ( τ.ε. " + moment(new Date()).format('HH:mm:ss') + " )");
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
                dashboard.live.loadLiveScheduledEventsMediaObject();
                dashboard.live.loadNextBroadcastsExcludingLiveMediaObject();
            }
        }, 500);
    }
    startTime();
})();
