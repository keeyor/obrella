/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.evtab = dashboard.evtab || {};

    let TimetableE_DT;
    let first_run_e = 0;

    dashboard.evtab.initTimeTableLectureEDT = function() {

        TimetableE_DT = $("#table_timetable_events").DataTable( {
            "processing": true,
            "ajax": {
                contentType: "application/json; charset=utf-8",
                type : "POST",
                url: dashboard.siteurl + '/api/v1/timetable/dt/authorized/scheduler',
                data: function ( d ) {
                    d = constructScheduleQuery();
                    return JSON.stringify( d );
                }
            },
            order: [[5, 'desc']],
            language: dtLanguageGr,
            fixedHeader: true,
             "dom": '<"top"flB><p>rti<"bottom">p<"clear">',
            pagingType: "full_numbers",
            pageLength : 10,
            "columns": [
                {"data": "id"},
                {"data": "enabled"},
                {"data": "id"},
                {"data": "repeat"}, //3
                {"data": "dayOfWeek"}, //4
                {"data": "date"}, //5
                {"data": "startTime"}, //6
                {"data": "durationHours"}, //7
                {"data": "durationMinutes"}, //8
                {"data": "type"},   //9
                {"data": "scheduledEvent.title"},   //10
                {"data": "scheduledEvent.supervisor"},   //11
                {"data": "classroom.name"}, //12
                {"data": "broadcast"}, //13
                {"data": "access"}, //14
                {"data": "recording"}, //15
                {"data": "publication"}, //16
                {"data": "editor"}, //17
                {"data": "id"}, //18
                {"data": "id"}, //19
                {"data": "cancellations"},//20
                {"data": "date"} //21
            ],
            "aoColumnDefs": [
                {
                    "aTargets": [2,3,4,8,9,20,21],
                    "sortable": false,
                    "visible" : false,
                    "sWidth": "0px"
                },
                {
                    "aTargets": [1],
                    "mData": "enabled",
                    "sortable": false,
                    "className" : "dt-center",
                    "mRender": function (data,type,row) {
                        if (data) {
                            let repeat = row["repeat"];
                            let cancellations = row["cancellations"];
                            if (repeat === "onetime" && cancellations != null && cancellations.length >0) {
                                return '<i class="fas fa-circle" style="color:orangered"  title="Ανενεργό Πρόγραμμα"></i>';
                            }
                            return '<i class="fas fa-circle" style="color:greenyellow"  title="Ενεργό Πρόγραμμα"></i>';
                        }
                        else {
                            return '<i class="fas fa-circle" style="color:orangered"  title="Ανενεργό Πρόγραμμα"></i>';
                        }
                    }
                },
                {
                    "aTargets": [5],
                    "mData": "date",
                    "className" : "dt-center",
                    "mRender": function (data,type,row) {
                        let cancellations = row["cancellations"];
                        if (data != null && data !== "") {
                            let display_date = moment(data).format('ll');
                            let hiddenDataForSorting = '<span style="display:none">' + data + "-" + row.startTime + '</span>';
                            let cancellation_mark = "";
                            if (cancellations != null && cancellations.length >0) {
                                cancellation_mark = '<i class="fas fa-exclamation font-weight-bolder ml-1" style="color: red" title="Ακύρωση"></i>';
                            }
                            return hiddenDataForSorting + '<span>' + display_date + '</span>' + cancellation_mark;
                        }
                        else {
                            return "";
                        }
                    }
                },
                {
                    "aTargets": [6],
                    "mData": "startTime",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        return data;
                    }
                },
                {
                    "aTargets": [7],
                    "mData": "durationHours",
                    "className" : "dt-center",
                    "mRender": function (data,type,row) {
                        let val = data + " Ώ ";
                        if (row["durationMinutes"] !== 0) {
                            val +=row["durationMinutes"] + " λ"
                        }
                        return val;
                    }
                },
                {
                    "aTargets": [10],
                    "mData": "scheduledEvent.title",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        return '<span style="color: #003476;font-weight: 600">' + data + '</span>';
                    }
                },
                {
                    "aTargets": [11],
                    "mData": "scheduledEvent.supervisor",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data != null) {
                            return '<span style="font-weight: 500">' + data + '</span>';
                        }
                        else {
                            return '';
                        }
                    }
                },
                {
                    "aTargets": [12],
                    "mData": "classroom.name",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        return data;
                    }
                },
                {
                    "aTargets": [13],
                    "mData": "broadcast",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        let hiddenDataForSorting = '<span style="display:none;">' + data + '</span>';
                        if (data) {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl +   '/public/images/icons/IconMetadosiOn.png" width="20px" alt=""  />';
                        }
                        else {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl +   '/public/images/icons/IconMetadosiOff.png" width="20px" alt=""  />';
                        }
                    }
                },
                {
                    "aTargets": [14],
                    "mData": "access",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        let hiddenDataForSorting = '<span style="display:none">' + data + '</span>';
                        if (data === "open") {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl + '/public/images/icons/IconMetadosiStatusOpen.png" width="20px" alt=""  />';
                        } else if (data === "sso") {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl + '/public/images/icons/IconMetadosiStatusUserName.png" width="20px" alt=""  />';
                        } else if (data === "password") {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl + '/public/images/icons/IconMetadosiStatusPassword.png" width="20px" alt=""  />';
                        } else  {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl + '/public/images/icons/IconProsvasiNA.png" width="20px" alt=""  />';
                        }
                    }
                },
                {
                    "aTargets": [15],
                    "mData": "recording",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        let hiddenDataForSorting = '<span style="display:none;">' + data + '</span>';
                        if (data) {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl +   '/public/images/icons/IconRecAuto.png" width="20px" alt=""  />';
                        }
                        else {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl +   '/public/images/icons/IconRecOff.png" width="20px"alt=""  />';
                        }
                    }
                },
                {
                    "aTargets": [16],
                    "mData": "publication",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        let hiddenDataForSorting = '<span style="display:none">' + data + '</span>';
                        if (data === "public") {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl + '/public/images/icons/IconRecStatusPublic.png" width="20px" alt=""  />';
                        } else if (data === "private") {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl + '/public/images/icons/IconRecStatusPrivate.png" width="20px" alt=""  />';
                        } else  {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl + '/public/images/icons/IconProsvasiNA.png" width="20px" alt=""  />';
                        }
                    }
                },
                {
                    "aTargets": [17],
                    "mData": "editor",
                    "sortable": false,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        let editor_name = data.name.split(" ");
                        let badge = '<span title="' + editor_name + '" class="badge rounded-pill bg-warning">' + editor_name[0].charAt(0);
                        if (editor_name.length>1) {
                            badge += editor_name[1].charAt(0)
                        }
                        badge += '</span>';
                        return badge;
                    }
                },
                {
                    "aTargets": [18],
                    "mData": "id",
                    "sortable": false,
                    "mRender": function (data) {
                        return '<a role="button" class="btn btn-secondary btn-sm edit-schedule btn-pill blue-btn-wcag-bgnd-color text-white" title="επεξεργασία μετάδοσης" href="schedule?id=' + data + '"><i class="fas fa-pencil-alt"></i></a>';
                    }
                },
                {
                    "aTargets": [19],
                    "mData": "id",
                    "sortable": false,
                    "mRender": function (data) {
                        return '<a role="button" class="btn btn-sm btn-warning btn-pill edit-schedule" title="αντιγραφή μετάδοσης σε νέα" href="schedule?cloneId=' + data + '"><i class="far fa-clone"></i></a>';
                    }
                },
                {
                    "aTargets": [21],
                    "mData": "date",
                    "className" : "dt-center",
                    "mRender": function (data,type,row) {
                        let cancellations = row["cancellations"];
                        if (data != null && data !== "") {
                            return  moment(data).format('ll');
                        }
                        else {
                            return "";
                        }
                    }
                },
            ],
            buttons: [
                {
                    extend: 'pdf',
                    exportOptions: {
                        columns: [4,21,6,7,8,12],
                        stripHtml: true,
                    },
                    text:'<span title="Εξαγωγή σε PDF"><i class="fas fa-download"></i> PDF</span>',
                    className: 'ms-2 blue-btn-wcag-bgnd-color text-white',
                }
            ],
            "initComplete": set_display_results
        });
        TimetableE_DT.on( 'order.dt search.dt', function () {
            TimetableE_DT.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
                cell.innerHTML = i+1;
            } );
        }).draw();
        TimetableE_DT.on( 'page.dt', function () {
            set_display_results();
        } );
    }
    function  set_display_results() {

        //Enable Tooltips
        var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-toggle="tooltip"]'))
        var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new coreui.Tooltip(tooltipTriggerEl)
        });

        let filters = ["estatus"];

        for (let i = 0; i < filters.length; i++) {
            let $filter_name = "#" + filters[i] + "_filter_name";
            let $filter_clear = "#" + filters[i] + "_clear";
            let $filter_load = "#" + filters[i] + "_load";

            let filter_value = $($filter_name).val();
            if (filter_value !== '' && filter_value !== '-1') {
                $($filter_clear).show();
                $($filter_load).html("<span style='color:Dodgerblue;font-weight: bold'>" + filter_value + "</span>");
            }
        }
    }
    function constructScheduleQuery() {


        let estatus_filter;
        let type;
        let d;
        let timetable_events_search_history = $("#search_event_filters").val();

        type = "event";

        if (timetable_events_search_history === "" || first_run_e !== 0) {
            estatus_filter = $("#estatus_filter").val();
            if (estatus_filter == null || estatus_filter === "") {
                estatus_filter = "_all";
            }
            d = {
                type : type,
                status_type: estatus_filter
            }
        }
        else {
            let json_filters = JSON.parse(timetable_events_search_history);
            d = {
                type: json_filters.type,
                status_type: json_filters.status_type,
            }
            $("#estatus_filter_name").val(dashboard.broker.select_eStatus(json_filters.status_type));
        }

        setupAppliedFilters();
        setupDynamicFilters();

        first_run_e = 1;
        $("#search_event_filters").val(JSON.stringify(d));
        return d;
    }

    function  setupAppliedFilters() {

        let some_filters = 0;
        let statusFilterId = $("#estatus_filter").val();
        let statusFilterText = $("#estatus_filter_name").val();
        if (statusFilterId !== undefined && statusFilterId != null && statusFilterId !== '' && statusFilterId !== '_all') {
            $('#status-dd-header').html("<span class='fas fa-minus-circle'></span> | Κατάσταση " + statusFilterText);
            $("#status-filter-applied").show();
            some_filters = 1;
        }
        else {
            $("#status-filter-applied").hide();
        }

        if (some_filters === 1) {
            $("#none_event_filter").hide();
        }
        else {
            $("#none_event_filter").show();
        }
    }

    function setupDynamicFilters() {

        let some_filters = 0;

        $("#no_dyna_event_filters").show();
        $("#no_dyna_event_filters").html("Δημιουργία δυναμικών φίλτρων σε εξέλιξη. Παρακαλώ περιμένετε...");

        let status_filter_val = $("#estatus_filter").val();
        if (status_filter_val === "_all" || status_filter_val === "" ) {
            dashboard.type.loadEventStatusByReport();
            some_filters = 1;
        }
        else {
            $("#statusCanvasLink").hide();
        }

        if (some_filters === 0) {
            $("#no_dyna_event_filters").html("δεν βρέθηκαν επιπλέον κριτήρια");
        }
        else {
            $("#no_dyna_event_filters").hide();
        }

    }
    dashboard.evtab.reloadTimeTableLectureEDT = function() {
        TimetableE_DT.ajax.reload(set_display_results);
    }
})();