/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.calendar = dashboard.calendar || {};

    let TimetableE_DT;

    dashboard.calendar.initTimeTableLectureEDT = function() {

        TimetableE_DT = $("#table_timetable_events").DataTable( {
            "processing": true,
            "ajax": {
                contentType: "application/json; charset=utf-8",
                type : "POST",
                url: dashboard.siteurl + '/api/v1/timetable_daterange/dt',
                data: function ( d ) {
                    d = constructScheduleQuery();
                    return JSON.stringify( d );
                }
            },
            order: [[7, 'asc']],
            language: dashboard.dtLanguageGr,
            fixedHeader: true,
            pageLength: 50,
            dom: 'lBfrtip',
            "columns": [
                {"data": null},
                {"data": "enabled"},
                {"data": "id"},
                {"data": "repeat"}, //3
                {"data": "dayOfWeek"}, //4
                {"data": "period"}, //5
                {"data": "type"}, //6
                {"data": "date"}, //7
                {"data": "date"}, //8
                {"data": "startTime"}, //9
                {"data": "durationHours"}, //10
                {"data": "durationMinutes"}, //11
                {"data": "type"},   //12
                {"data": "scheduledEvent.title"},   //13
                {"data": "classroom.name"}, //14
                {"data": "broadcast"}, //15
                {"data": "access"}, //16
                {"data": "recording"}, //17
                {"data": "publication"}, //18
                {"data": "supervisor.name"} //19
            ],
            "aoColumnDefs": [
                {
                    "aTargets": [1,2,8,11,12,19],
                    "sortable": false,
                    "visible": false,
                    "sWidth": "0px"
                },
                {
                    "aTargets": [1],
                    "mData": "enabled",
                    "sortable": false,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data) {
                            return '<i class="fas fa-circle" style="color:greenyellow"></i>';
                        }
                        else {
                            return '<i class="fas fa-circle" style="color:orangered"></i>';
                        }
                    }
                },
                {
                    "aTargets": [3],
                    "mData": "repeat",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        let hiddenDataForSorting = '<span style="display:none">' + data + '</span>';
                        if (data === "regular") {
                            return hiddenDataForSorting + '<img title="τακτική" src="' + dashboard.siteurl
                                +   '/public/images/icons/IconEidosTaktiki.png" width="15px " alt="τακτική"  />';
                        }
                        else {
                            return hiddenDataForSorting + '<img title="έκτακτη"  src="' + dashboard.siteurl
                                +   '/public/images/icons/IconEidosEktakti.png" width="15px " alt="έκτακτη"  />';
                        }
                    }
                },
                {
                    "aTargets": [4],
                    "mData": "dayOfWeek" ,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        return dashboard.broker.selectDayOfWeek(data);
                    }
                },
                {
                    "aTargets": [5],
                    "mData": "period" ,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data!= null) {
                            return dashboard.broker.selectPeriod(data);
                        }
                        else {
                            return "";
                        }
                    }
                },
                {
                    "aTargets": [6],
                    "mData": "type" ,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data === "lecture") {
                            return "Διάλεξη";
                        }
                        else {
                            return "<span style='color: maroon'>Εκδήλωση</span>";
                        }
                    }
                },
                {
                    "aTargets": [7],
                    "mData": "date",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data != null && data !== "") {
                            let display_date = moment(data).format('ll');
                            let hiddenDataForSorting = '<span style="display:none">' + data + '</span>';
                            return hiddenDataForSorting + '<span>' + display_date + '</span>';
                        }
                        else {
                            return "";
                        }
                    }
                },
                {
                    "aTargets": [9],
                    "mData": "startTime",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        return data;
                    }
                },
                {
                    "aTargets": [10],
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
                    "aTargets": [13],
                    "mData": "scheduledEvent.title",
                    "className" : "dt-center",
                    "mRender": function (data,type,row) {
                        if (data !== "") {
                            return "<span style='color: maroon'>" + data + "</span>";
                        }
                        else {
                            return row["course"].title;
                        }
                    }
                },
                {
                    "aTargets": [14],
                    "mData": "classroom.name",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        return data;
                    }
                },
                {
                    "aTargets": [15],
                    "mData": "broadcast",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        let hiddenDataForSorting = '<span style="display:none;">' + data + '</span>';
                        if (data) {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl +   '/public/images/icons/IconMetadosiOn.png" width="15px " alt=""  />';
                        }
                        else {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl +   '/public/images/icons/IconMetadosiOff.png" width="15px " alt=""  />';
                        }
                    }
                },
                {
                    "aTargets": [16],
                    "mData": "access",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        let hiddenDataForSorting = '<span style="display:none">' + data + '</span>';
                        if (data === "open") {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl + '/public/images/icons/IconMetadosiStatusOpen.png" width="15px " alt=""  />';
                        } else if (data === "sso") {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl + '/public/images/icons/IconMetadosiStatusUserName.png" width="15px " alt=""  />';
                        } else if (data === "password") {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl + '/public/images/icons/IconMetadosiStatusPassword.png" width="15px " alt=""  />';
                        } else  {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl + '/public/images/icons/IconProsvasiNA.png" width="15px " alt=""  />';
                        }
                    }
                },
                {
                    "aTargets": [17],
                    "mData": "recording",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        let hiddenDataForSorting = '<span style="display:none;">' + data + '</span>';
                        if (data) {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl +   '/public/images/icons/IconRecAuto.png" width="15px " alt=""  />';
                        }
                        else {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl +   '/public/images/icons/IconRecOff.png" width="15px " alt=""  />';
                        }
                    }
                },
                {
                    "aTargets": [18],
                    "mData": "publication",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        let hiddenDataForSorting = '<span style="display:none">' + data + '</span>';
                        if (data === "public") {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl + '/public/images/icons/IconRecStatusPublic.png" width="15px " alt=""  />';
                        } else if (data === "private") {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl + '/public/images/icons/IconRecStatusPrivate.png" width="15px " alt=""  />';
                        } else  {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl + '/public/images/icons/IconProsvasiNA.png" width="15px " alt=""  />';
                        }
                    }
                }
            ],
            buttons: [
                {extend: 'excel',
                    exportOptions: {
                        columns: [ 4,8,9,13,14,15,17],
                        stripHtml: true,
                        orientation: 'landscape'
                    },
                    title:SchedulerPDFHeader,
                    filename: 'openDelos-live',
                    text:'excel'
                },
                {extend: 'pdf',
                    exportOptions: {
                        columns: [ 4,8,9,13,14,15,17],
                        stripHtml: true,
                    },
                    title:SchedulerPDFHeader,
                    filename: 'openDelos-live',
                    text:'PDF',
                    orientation: 'landscape'
                }
            ],
            "initComplete" : enableNavigation
        });
        TimetableE_DT.on( 'order.dt search.dt', function () {
            TimetableE_DT.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
                cell.innerHTML = i+1;
            } );
        }).draw();
    }

    function constructScheduleQuery() {
        let year
        let school_filter;
        let department_filter;
        let classroom_filter;
        let editor_filter;
        let repeat_filter;
        let dow_filter;
        let type;
        let period_filter
        let d;
        let enabled;

        let fromDate = $("#_date_from").val();
        let toDate = $("#_date_to").val();
        department_filter = $("#department_filter").val();
        if (department_filter == null || department_filter === "") {
            department_filter = "_all";
        }
        if (department_filter == null || department_filter === "") {
            department_filter = "_all";
        }
        classroom_filter = $("#classroom_filter").val();
        if (classroom_filter == null || classroom_filter === "") {
            classroom_filter = "_all";
        }
        editor_filter = $("#editor_filter").val();
        if (editor_filter == null || editor_filter === "") {
            editor_filter = "_all";
            $(".resp_editor").html("").hide();
        }
        else {
            let html = "Υπεύθυνος Καθηγητής: <span style='color: blue;font-weight: bolder'>" + $("#editor_name").val();
            $(".resp_editor").html(html).show();
        }

            year =  dashboard.selected_year;
            school_filter = "_all";
            repeat_filter = "_all";
            dow_filter = "_all";
            period_filter = "_all";
            type = "_all";
            enabled = "true";

            d = {
                year: year,
                type : type,
                repeat: repeat_filter,
                dayOfWeek : dow_filter,
                departmentId : department_filter,
                classroomId: classroom_filter,
                supervisorId: editor_filter,
                schoolId: school_filter,
                period: period_filter,
                fromDate: fromDate,
                toDate: toDate,
                enabled: enabled  //!Important. We care only for enabled entries
            }

        return d;
    }

    dashboard.calendar.reloadTimeTableLectureEDT = function() {

        loader.showLoader();
        $(".btn").attr("disabled",true);
        $("#department_load").addClass("disabled");
        $("#classroom_load").addClass("disabled");
        $("#editor_load").addClass("disabled");

        TimetableE_DT.ajax.reload( function() {
            enableNavigation();
        });

    }

    function enableNavigation() {

        let filters = ["department", "classroom", "editor"];

        for (let i = 0; i < filters.length; i++) {
            let $filter_name = "#" + filters[i] + "_filter_name";
            let $filter_clear = "#" + filters[i] + "_clear";
            let $filter_load = "#" + filters[i] + "_load";

            let filter_value = $($filter_name).val();
            if (filter_value !== '' && filter_value !== '-1') {
                $($filter_clear).show();
                let filter_text = dashboard.broker.getTextForFilter(filters[i]);
                $($filter_load).html("<span style='color:Dodgerblue;font-weight: bold'> " + filter_text + " "  + filter_value + "</span>");
            }
        }
        $(".btn").attr("disabled",false);
        $("#department_load").removeClass("disabled");
        $("#classroom_load").removeClass("disabled");
        $("#editor_load").removeClass("disabled");

        loader.hideLoader();
    }

    function SchedulerPDFHeader() {

        let html="";
        html += "Πρόγραμμα Μεταδόσεων/Καταγραφών\n" +
             $("#date_range_header").text();
        return html;
    }

})();