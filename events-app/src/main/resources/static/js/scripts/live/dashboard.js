(function () {
    'use strict';

    window.dashboard = window.dashboard || {};
    dashboard.broker = $({});

    dashboard.siteUrl   = "";
    dashboard.app_path  = "";

    let LiveLectures_DT;
    let LiveScheduledEvents_DT;
    let LiveToday_DT;

    let live_stream_counter = 0;

    dashboard.init = function () {

        dashboard.siteUrl = dashboard.broker.getRootSitePath();
        //dashboard.departments.init();

        let today = moment().format("LL");
        $("#daily_date_now").html(today);

     };

    $(document).ready(function () {

        dashboard.init();
        define_events();
        init_controls();
        //dashboard.broker.initTimeTableLectureDT_SERVER();
        dashboard.broker.initTimeTableScheduledEventsDT_SERVER();
       // dashboard.broker.initTimeTableTodayDT_SERVER();
    });

    dashboard.broker.initTimeTableLectureDT_SERVER = function() {

    LiveLectures_DT = $("#liveLecturesDataTable").DataTable( {
            "processing": true,
            "ajax":  dashboard.siteUrl + '/liveLectures',
            pageLength : 25,
            order: [[2, 'asc']],
            "language": dtLanguageGr,
            "columns": [
                {"data": null}, //index :: 0
                {"data": "id"},
                {"data": "date"},
                {"data": "realDuration"},
                {"data": null},
                {"data": "title"}, //5
                {"data": "supervisor"},
                {"data": "department"},
                {"data": "classroomName"},//8
                {"data": "broadcast"},
                {"data": "access"},
                {"data": "recording"},
                {"data": "publication"}
            ],
            "aoColumnDefs": [
                {
                    "aTargets": [1,3],
                    "visible": false
                },
                {
                    "aTargets": [2],
                    "mData": "date",
                    responsivePriority: 1,
                    "className" : "dt-center",
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
                    "aTargets": [3],
                    "mData": "realDuration",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data != null && data !== "") {
                            let duration_split = data.split(":");
                            let hours = parseInt(duration_split[0]);
                            let minutes = parseInt(duration_split[1]);
                            return hours + 'h ' + minutes + 'm';
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
                    "className" : "dt-center",
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
                    "aTargets": [5],
                    "mData": "title",
                    responsivePriority: 1,
                    "className" : "dt-center nowrap",
                    "mRender": function (data,type,row) {
                        let broadcast = row["broadcast"];
                        let ret = "";
                        if (broadcast) {
                            let id = row["id"];
                            if (row["access"] === 'open') {
                                ret += '<a  role="button" title="Μετάβαση στη ζωντανή μετάδοση" class="btn  text-white btn-danger play_live" ' +
                                    ' target="_blank"  href="' + dashboard.siteUrl + '/live_player?id=' + id + '"> ' +
                                    ' <i class="fas fa-play mr-1"></i> ' + data +
                                    '</a>'
                            } else if (row["access"] === 'sso') {
                                ret += '<a  role="button" title="Μετάβαση στη ζωντανή μετάδοση" class="btn text-white btn-danger  play_live" ' +
                                    ' target="_blank"  href="' + dashboard.siteUrl + '/cas/live_player?id=' + id + '"> ' +
                                    ' <i class="fas fa-play mr-1"></i> ' + data +
                                    '</a>'
                            } else if (row["access"] === 'password') {
                                ret += '<a  role="button" title="Μετάβαση στη μετάδοση" class="btn  text-white btn-danger  play_live" ' +
                                    ' target="_blank"  href="' + dashboard.siteUrl + '/secure/live_player?id=' + id + '"> ' +
                                    ' <i class="fas fa-play mr-1"></i> ' + data +
                                    '</a>'
                            }
                        }
                        else {
                            ret += data;
                        }
                        return ret;
                    }
                },
                {
                    "aTargets": [6],
                    "mData": "supervisor",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        return '<span style="font-weight: 500">' + data.name + '</span>';
                    }
                },
                {
                    "aTargets": [7],
                    "mData": "department",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        return data.title;
                    }
                },
                {
                    "aTargets": [9],
                    "mData": "broadcast",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data) {
                            return '<img width="16px"  src="' + dashboard.siteUrl + '/public/images/icons/IconMetadosiOn.png" alt="" src="">';
                        }
                        else {
                            return '<img width="16px"  src="' + dashboard.siteUrl + '/public/images/icons/IconMetadosiOff.png" alt="" src="">';
                        }
                    }
                },
                {
                    "aTargets": [10],
                    "mData": "access",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data !== "closed") {
                            if (data === 'open') {
                                return '<i class="fas fa-lock-open"></i>';
                            } else if (data === 'sso') {
                                return '<img src="' + dashboard.siteUrl + '/public/images/icons/IconMetadosiStatusUserName.png" width="20px" alt="">';
                            } else if (data === 'password') {
                                return '<i class="fas fa-lock"></i>';
                            }
                        }
                        else return 'NA';
                    }
                },
                {
                    "aTargets": [11],
                    "mData": "recording",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data) {
                            return '<img width="16px"  src="' + dashboard.siteUrl + '/public/images/icons/IconRecAuto.png" alt="" src="">';
                        }
                        else    {
                            return '<img width="16px"  src="' + dashboard.siteUrl + '/public/images/icons/IconRecOff.png" alt="" src="">';
                        }
                    }
                },
                {
                    "aTargets": [12],
                    "mData": "publication",
                    responsivePriority: 1,
                    "className" : "dt-center pt-1",
                    "mRender": function (data,type,row) {
                        let recording = row["recording"];
                        if (recording) {
                            if (data === 'public') {
                                return '<img width="16px"  src="' + dashboard.siteUrl + '/public/images/icons/IconRecStatusPublic.png" alt="" >';
                            } else {
                                return '<img width="16px"  src="' + dashboard.siteUrl + '/public/images/icons/IconRecStatusPrivate.png" alt="" >';
                            }
                        }
                        else {
                            return 'NA';
                        }
                    }
                }
            ],
            "initComplete": function(settings, json) {
                set_display_results(json);
            }
        });
        LiveLectures_DT.on( 'order.dt search.dt', function () {
            LiveLectures_DT.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
                cell.innerHTML = i+1;
            } );
        }).draw();
        LiveLectures_DT.on( 'page.dt', function (json) {
            set_display_results(json);
        } );
    }

    dashboard.broker.initTimeTableScheduledEventsDT_SERVER = function() {

        LiveScheduledEvents_DT = $("#liveScheduledEventsDataTable").DataTable( {
            "processing": true,
            "ajax":  dashboard.siteUrl + '/liveScheduledEvents',
            pageLength : 25,
            order: [[2, 'asc']],
            "language": dtLanguageGr,
            "columns": [
                {"data": null}, //index :: 0
                {"data": "id"}, //1
                {"data": "date"},
                {"data": "realDuration"},
                {"data": null},
                {"data": "title"}, //5
                {"data": "event"},//6
                {"data": "classroomName"},//7
                {"data": "broadcast"}, //8
                {"data": "access"},//9
                {"data": "recording"}, //10
                {"data": "publication"} //11
            ],
            "aoColumnDefs": [
                {
                    "aTargets": [1,3,6],
                    "visible": false
                },
                {
                    "aTargets": [2],
                    "mData": "date",
                    responsivePriority: 1,
                    "className" : "dt-center",
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
                    "aTargets": [3],
                    "mData": "realDuration",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data != null && data !== "") {
                            let duration_split = data.split(":");
                            let hours = parseInt(duration_split[0]);
                            let minutes = parseInt(duration_split[1]);
                            return hours + 'h ' + minutes + 'm';
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
                    "className" : "dt-center",
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
                    "aTargets": [5],
                    "mData": "title",
                    responsivePriority: 1,
                    "className" : "dt-center dt-nowrap",
                    "mRender": function (data,type,row) {
                        let broadcast = row["broadcast"];
                        let ret = "";
                        if (broadcast) {
                            let id = row["id"];
                            if (row["access"] === 'open') {
                                ret += '<a  role="button" title="Μετάβαση στη ζωντανή μετάδοση" class="btn text-white btn-danger play_live" ' +
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
                },
                {
                    "aTargets": [8],
                    "mData": "broadcast",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data) {
                            return '<img width="16px"  src="' + dashboard.siteUrl + '/public/images/icons/IconMetadosiOn.png" alt="" src="">';
                        }
                        else {
                            return '<img width="16px"  src="' + dashboard.siteUrl + '/public/images/icons/IconMetadosiOff.png" alt="" src="">';
                        }
                    }
                },
                {
                    "aTargets": [9],
                    "mData": "access",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data !== "closed") {
                            if (data === 'open') {
                                return '<i class="fas fa-lock-open"></i>';
                            } else if (data === 'sso') {
                                return '<img src="' + dashboard.siteUrl + '/public/images/icons/IconMetadosiStatusUserName.png" width="20px" alt="">';
                            } else if (data === 'password') {
                                return '<i class="fas fa-lock"></i>';
                            }
                        }
                        else return 'NA';
                    }
                },
                {
                    "aTargets": [10],
                    "mData": "recording",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data) {
                            return '<img width="16px"  src="' + dashboard.siteUrl + '/public/images/icons/IconRecAuto.png" alt="" src="">';
                        }
                        else    {
                            return '<img width="16px"  src="' + dashboard.siteUrl + '/public/images/icons/IconRecOff.png" alt="" src="">';
                        }
                    }
                },
                {
                    "aTargets": [11],
                    "mData": "publication",
                    responsivePriority: 1,
                    "className" : "dt-center pt-1",
                    "mRender": function (data,type,row) {
                        let recording = row["recording"];
                        if (recording) {
                            if (data === 'public') {
                                return '<img width="16px"  src="' + dashboard.siteUrl + '/public/images/icons/IconRecStatusPublic.png" alt="" >';
                            } else {
                                return '<img width="16px"  src="' + dashboard.siteUrl + '/public/images/icons/IconRecStatusPrivate.png" alt="" >';
                            }
                        }
                        else {
                            return 'NA';
                        }
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

    dashboard.broker.initTimeTableTodayDT_SERVER = function() {

        LiveToday_DT = $("#liveTodayDataTable").DataTable( {
            "processing": true,
            "ajax":  dashboard.siteUrl + '/liveToday',
            pageLength : 25,
            order: [[2, 'asc']],
            "language": dtLanguageGr,
            "columns": [
                {"data": null}, //index :: 0
                {"data": "id"}, //1
                {"data":  null}, //2
                {"data": "date"},
                {"data": "realDuration"},
                {"data": null}, //5
                {"data": "type"}, //6
                {"data": "title"}, //7
                {"data": null}, //8
                {"data": "supervisor"}, //9
                {"data": "department"}, //10
                {"data": "classroomName"},//11
                {"data": "broadcast"}, //12
                {"data": "access"},//13
                {"data": "recording"}, //14
                {"data": "publication"} //15
            ],
            "aoColumnDefs": [
                {
                    "aTargets": [1,2, 4,6],
                    "visible": false
                },
                {
                    "aTargets": [2],
                    responsivePriority: 1,
                    "className": "dt-center dt-nowrap",
                    "mRender": function (data, type, row) {
                        let start_time = row["date"];
                        let start_time_moment = moment.unix(start_time.epochSecond);
                        let duration = row["realDuration"];
                        let end_time = getBroadcastsEndTime(start_time,duration);
                        let now = moment();
                        if (now.isAfter(end_time)) {
                            return '<i class="fas fa-flag-checkered mr-1"></i>';
                        }
                        else if (now.isBefore(start_time_moment)) {
                            return '<i class="far fa-clock mr-1"></i>';
                        }
                        else if (now.isAfter(start_time_moment) && now.isBefore(end_time)) {
                            return '<span class="icon-live-lecture" style="color: red;width:16px"></span>';
                        }
                    }
                },
                {
                    "aTargets": [3],
                    "mData": "date",
                    responsivePriority: 1,
                    "className" : "dt-center",
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
                    "mData": "realDuration",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data != null && data !== "") {
                            let duration_split = data.split(":");
                            let hours = parseInt(duration_split[0]);
                            let minutes = parseInt(duration_split[1]);
                            return hours + 'h ' + minutes + 'm';
                        }
                        else {
                            return "";
                        }
                    }
                },
                {
                    "aTargets": [5],
                    "mData": "date",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data,type,row) {
                        if (data != null && data !== "") {
                            let start_time = row["date"];
                            let duration = row["realDuration"];
                            let end_time = getBroadcastsEndTime(start_time,duration);
                            return end_time.format('HH:mm');
                        }
                        else {
                            return "";
                        }
                    }
                },
                {
                    "aTargets": [6],
                    "mData": "type",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data === "COURSE") {
                            return 'Διάλεξη Μαθήματος';
                        }
                        else {
                            return 'Εκδήλωση';
                        }
                    }
                },
                {
                    "aTargets": [7],
                    "mData": "title",
                    responsivePriority: 1,
                    "className" : "dt-center nowrap",
                    "mRender": function (data) {
                          return data;
                    }
                },
                {
                    "aTargets": [8],
                    "mData": "title",
                    "sWidth" : "20px",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data,type,row) {
                        let start_time = row["date"];
                        let start_time_moment = moment.unix(start_time.epochSecond);
                        let duration = row["realDuration"];
                        let end_time = getBroadcastsEndTime(start_time,duration);
                        let now = moment();
                        if (now.isAfter(end_time)) {
                            //passed
                            let recording = row["recording"];
                            let publication = row["publication"];
                            let ret_lnk= '';
                            if (recording && publication === "public") {
                                let type = row["type"];
                                let date = moment.unix(start_time.epochSecond).format('YYYY-MM-DD');
                                if (type === "COURSE") {
                                    let departmentId = row["department"].id;
                                    let courseId = row["course"].id;
                                    ret_lnk = '<a role="button" title="Αναζήτηση Καταγραφής" class="btn btn-sm  blue-btn-wcag-bgnd-color play_vod mr-1" ' +
                                        ' style="color: whitesmoke;font-size: 0.9em;" target="_blank" ' +
                                        ' href="' + dashboard.siteUrl + '/search?d=' + departmentId + '&c=' + courseId + '&dt=' + date + '">' +
                                        ' <i class="fas fa-search"></i>' +
                                        ' </a>';
                                } else if (type === "EVENT") {
                                    let eventId = row["event"].id;
                                    ret_lnk = '<a role="button" title="Αναζήτηση Καταγραφής" class="btn btn-sm blue-btn-wcag-bgnd-color play_vod mr-1" ' +
                                        ' style="color: whitesmoke;font-size: 0.9em;" target="_blank" ' +
                                        ' href="' + dashboard.siteUrl + '/search?e=' + eventId + '&dt=' + date + '">' +
                                        ' <i class="fas fa-search"></i>' +
                                        ' </a>';
                                }
                            }
                            if (ret_lnk === "") return '<button class="btn-default btn-sm" disabled title="Αναζήτηση Καταγραφής">NA</button>';
                            return ret_lnk;
                        }
                        else if (now.isBefore(start_time_moment)) {
                            return "";
                        }
                        else if (now.isAfter(start_time_moment) && now.isBefore(end_time)) {
                            let broadcast = row["broadcast"];
                            let ret = "";
                            if (broadcast) {
                                let id = row["id"];
                                if (row["access"] === 'open') {
                                    ret += '<a  role="button" title="Μετάβαση στη ζωντανή μετάδοση" class="btn btn-sm  text-white btn-danger play_live" ' +
                                        ' target="_blank"  href="' + dashboard.siteUrl + '/live_player?id=' + id + '"> ' +
                                        ' <i class="fas fa-play"></i> '
                                        '</a>';
                                } else if (row["access"] === 'sso') {
                                    ret += '<a  role="button" title="Μετάβαση στη ζωντανή μετάδοση" class="btn  btn-sm text-white btn-danger  play_live" ' +
                                        ' target="_blank"  href="' + dashboard.siteUrl + '/cas/live_player?id=' + id + '"> ' +
                                        ' <i class="fas fa-play"></i> '
                                        '</a>';
                                } else if (row["access"] === 'password') {
                                    ret += '<a  role="button" title="Μετάβαση στη μετάδοση" class="btn  btn-sm   text-white btn-danger  play_live" ' +
                                        ' target="_blank"  href="' + dashboard.siteUrl + '/secure/live_player?id=' + id + '"> ' +
                                        ' <i class="fas fa-play"></i> '
                                        '</a>';
                                }
                            } else {
                                ret += ""
                            }
                            return ret;
                        }
                    }
                },
                {
                    "aTargets": [9],
                    "mData": "supervisor",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data) {
                            return data.name;
                        }
                        else {
                            'NA';
                        }
                    }
                },
                {
                    "aTargets": [10],
                    "mData": "department",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data,type,row) {
                        if (data != null) {
                            return data.title;
                        }
                        else {
                            return row["supervisor"].department.title;
                        }
                    }
                },
                {
                    "aTargets": [12],
                    "mData": "broadcast",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data) {
                            return '<img width="16px"  src="' + dashboard.siteUrl + '/public/images/icons/IconMetadosiOn.png" alt="" src="">';
                        }
                        else {
                            return '<img width="16px"  src="' + dashboard.siteUrl + '/public/images/icons/IconMetadosiOff.png" alt="" src="">';
                        }
                    }
                },
                {
                    "aTargets": [13],
                    "mData": "access",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data !== "closed") {
                            if (data === 'open') {
                                return '<i class="fas fa-lock-open"></i>';
                            } else if (data === 'sso') {
                                return '<img src="' + dashboard.siteUrl + '/public/images/icons/IconMetadosiStatusUserName.png" width="20px" alt="">';
                            } else if (data === 'password') {
                                return '<i class="fas fa-lock"></i>';
                            }
                        }
                        else return 'NA';
                    }
                },
                {
                    "aTargets": [14],
                    "mData": "recording",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data) {
                            return '<img width="16px"  src="' + dashboard.siteUrl + '/public/images/icons/IconRecAuto.png" alt="" src="">';
                        }
                        else    {
                            return '<img width="16px"  src="' + dashboard.siteUrl + '/public/images/icons/IconRecOff.png" alt="" src="">';
                        }
                    }
                },
                {
                    "aTargets": [15],
                    "mData": "publication",
                    responsivePriority: 1,
                    "className" : "dt-center pt-1",
                    "mRender": function (data,type,row) {
                        let recording = row["recording"];
                        if (recording) {
                            if (data === 'public') {
                                return '<img width="16px"  src="' + dashboard.siteUrl + '/public/images/icons/IconRecStatusPublic.png" alt="" >';
                            } else {
                                return '<img width="16px"  src="' + dashboard.siteUrl + '/public/images/icons/IconRecStatusPrivate.png" alt="" >';
                            }
                        }
                        else {
                            return 'NA';
                        }
                    }
                }
            ],
        });
        LiveToday_DT.on( 'order.dt search.dt', function () {
            LiveToday_DT.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
                cell.innerHTML = i+1;
            } );
        }).draw();
    }

    dashboard.broker.reloadTimeTableLectureDT_SERVER = function() {
        LiveLectures_DT.ajax.reload(set_display_results);
    }
    dashboard.broker.reloadTimeTableScheduledEventsDT_SERVER = function() {
        LiveScheduledEvents_DT.ajax.reload(set_display_results_events);
    }
    dashboard.broker.reloadTimeTableTodayEventsDT_SERVER = function() {
        LiveToday_DT.ajax.reload();
    }

    function getBroadcastsEndTime(start_time, duration) {
        let duration_split = duration.split(":");
        let hours = parseInt(duration_split[0]);
        let minutes = parseInt(duration_split[1]);
        let end_time = moment.unix(start_time.epochSecond).add(hours,'hours').add(minutes,'minutes');
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

        $("#password_protected").on('click',function(e){
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
            //location.reload();
            //dashboard.broker.reloadTimeTableLectureDT_SERVER();
            dashboard.broker.reloadTimeTableScheduledEventsDT_SERVER();
            //dashboard.broker.reloadTimeTableTodayEventsDT_SERVER();
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
                //location.reload();
                //dashboard.broker.reloadTimeTableLectureDT_SERVER();
                dashboard.broker.reloadTimeTableScheduledEventsDT_SERVER();
               // dashboard.broker.reloadTimeTableTodayEventsDT_SERVER();
            }
        }, 500);
    }
    startTime();
})();
