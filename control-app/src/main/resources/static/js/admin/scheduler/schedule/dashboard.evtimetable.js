(function () {
    'use strict';

    dashboard.evtimetable = dashboard.evtimetable || {};

    let ScheduleTable_DT;

    dashboard.evtimetable.buildScheduleTable = function () {

        let schedule_id     = $("#id").val();
        let $scheduleDtElem = $("#schedule_etable");

        ScheduleTable_DT = $scheduleDtElem.DataTable( {
            "processing": true,
            "ajax": {
                contentType: "application/json; charset=utf-8",
                type : "GET",
                url: dashboard.siteurl + '/api/v1/schedule_table/dt/' + schedule_id,
                dataSrc : function(jsonObj) {
                    return jsonObj.data.results;
                },
            },
            pageLength : 10,
            language: dtLanguageGr,
            "dom": '<"top"l>rti<"bottom">p<"clear">',
            "columns": [
                {"data": null},
                {"data": "enabled"},
                {"data": "id"},
                {"data": "repeat"}, //3
                {"data": "dayOfWeek"}, //4
                {"data": "date"}, //5
                {"data": "date"}, //6
                {"data": "startTime"}, //7
                {"data": "durationHours"}, //8
                {"data": "durationMinutes"}, //9
                {"data": "type"},   //10
                {"data": "department.title"}, //11
                {"data": "scheduledEvent.title"},   //12
                {"data": "classroom.name"}, //13
                {"data": "broadcast"}, //14
                {"data": "access"}, //15
                {"data": "recording"}, //16
                {"data": "publication"}, //17
                {"data": "id"}, //18
                {"data": "cancellation"} //19
            ],
            "aoColumnDefs": [
                {
                    "aTargets": [2,6,9,10,11,13,19],
                    "sortable": false,
                    "visible": false,
                    "sWidth": "0px"
                },
                {
                    "aTargets": [1],
                    "mData": "enabled",
                    "sortable": false,
                    "className" : "dt-center",
                    "mRender": function (data,type,row) {
                        if (data) {
                            return '<i class="fas fa-circle" style="color:greenyellow"   title="???????????????????????????? ????????????????"></i>';
                        }
                        else {
                            if (row["argia"] != null)  {
                                return '<i class="fas fa-circle" style="color:orangered"    title="??????????/??????????:' + row["argia"].name + '"></i>';
                            }
                            if (row["argia"] != null)  {
                                return '<i class="fas fa-circle" style="color:orangered"    title="??????????/??????????:' + row["argia"].name + '"></i>';
                            }
                            else if (row["cancellation"] !== null)  {
                                return '<i class="fas fa-circle" style="color:orangered"    title="??????????????:' + row["cancellation"].title + '"></i>';
                            }
                            else if (row["overlapInfo"] !== null)  {
                                return '<i class="fas fa-circle" style="color:orangered"    title="????????????????????????:' + row["overlapInfo"].title + '"></i>';
                            }
                            else {
                                return '<i class="fas fa-circle" style="color:orangered" title="??????????????"></i>'
                            }
                        }
                    }
                },
                {
                    "aTargets": [3],
                    "mData": "repeat",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        let hiddenDataForSorting = '<span style="display:none;">' + data + '</span>';
                        if (data === "regular") {
                            return hiddenDataForSorting +
                                '<img title="??????????????" src="' + dashboard.siteurl +   '/public/images/icons/IconEidosTaktiki.png" width="15px " alt=""  />';
                        }
                        else {
                            return hiddenDataForSorting +
                                '<img title="??????????????"  src="' + dashboard.siteurl +   '/public/images/icons/IconEidosEktakti.png" width="15px " alt=""  />';
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
                    "mData": "date",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data != null && data !== "") {
                            let display_date = moment(data).format('ll');
                            let hiddenDataForSorting = '<span style="display:none">' + data + '<br/></span>';
                            return hiddenDataForSorting + '<span  style="font-weight: 500">' + display_date + '</span>';
                        }
                        else {
                            return "";
                        }
                    }
                },
                {
                    "aTargets": [6], // only for PDF, EXCEL
                    "mData": "date",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data != null && data !== "") {
                            return moment(data).format('ll');
                        }
                        else {
                            return "";
                        }
                    }
                },
                {
                    "aTargets": [7],
                    "mData": "startTime",
                    "className" : "dt-center",
                    "mRender": function (data,type,row) {
                        return data;
                    }
                },
                {
                    "aTargets": [8],
                    "mData": "durationHours",
                    "className" : "dt-center",
                    "mRender": function (data,type,row) {
                        let val = data + " ?? ";
                        if (row["durationMinutes"] !== 0) {
                            val +=row["durationMinutes"] + " ??";
                        }
                        return val;
                    }
                },
                {
                    "aTargets": [12],
                    "mData": "scheduledEvent.title",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        return data;
                    }
                },
                {
                    "aTargets": [13],
                    "mData": "classroom.name",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        return data;
                    }
                },
                {
                    "aTargets": [14],
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
                    "aTargets": [15],
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
                    "aTargets": [16],
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
                    "aTargets": [17],
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
                },
                {
                    "aTargets": [18],
                    "mData": "id",
                    "sortable": false,
                    "mRender": function (data,type,row) {
                        let add_class = "";
                        if (_dateIsInThePast(row)) {
                            return '<i class="fas fa-clock fa-2x" title="???????? ??????????????????????" style="color: lightgrey"></i>';
                        }
                        if (row["cancellation"] == null && row["overlapInfo"] == null) {
                            if (_dateIsLive(row)) {
                                return '<span class="icon-live-lecture" style="color: red;font-size: 1.3em"></span> Live!';
                            }
                            else {
                                return '<a role="button" title="?????????????? ??????????????????" class="btn btn-secondary btn-sm cancel-scheduled ' + add_class + '" href="#" ' +
                                    'data-id="' + data + '" data-date="' + row["date"] + '" data-title="' + row["scheduledEvent"].title + '" data-type="event">' +
                                    '<i class="fas fa-ban" style="color:orangered"></i>' +
                                    '</a>';
                            }
                        }
                        if ( row["cancellation"] != null) {
                            return  '<a role="button" title="???????????????????????? ??????????????????" class="btn btn-secondary btn-sm un-cancel-scheduled ' + add_class + '" href="#" ' +
                                'data-id="' + data + '" data-date="' + row["date"]  + '" data-title="' + row["scheduledEvent"].title + '" data-type="event">' +
                                '<i style="color:green" class="fas fa-circle-notch"></i>' +
                                '</a>';
                        }
                        else {
                            return '';
                        }
                    }
                }
            ],
            buttons: [
                {extend: 'pdf',
                    exportOptions: {
                        columns: [ 1,5,7,8,9],
                        stripHtml: true,
                    },
                    title: EventPDFHeader(),
                    filename: $("#pdf_filename").val(),
                    customize: function (doc) {
                        doc.defaultStyle.fontSize = 8;
                        doc.styles.tableHeader.fontSize = 8;
                        doc.styles.title.fontSize = 9;
                        // Remove spaces around page title
                        doc.content[0].text = doc.content[0].text.trim();
                        doc.pageMargins = [60, 10, 60,10 ];
                        doc.content[1].table.widths =Array(doc.content[1].table.body[0].length + 1).join('*').split('');
                        doc.defaultStyle.alignment = 'center';
                        doc.styles.tableHeader.alignment = 'center';
                    },
                    text:'<span title="?????????????? ???? PDF" ><i class="fas fa-download"></i> PDF</span>',
                    className: 'ms-2 blue-btn-wcag-bgnd-color text-white mb-4',
                    orientation: 'portrait'
                }
            ],
/*            "rowCallback": function( row, data ) {
                if (_dateIsInThePast(data)) {
                    dashboard.canDelete = false;
                }
            },*/
            "initComplete": function(settings, json) {
                set_display_results(json);
            }
        });
        ScheduleTable_DT.on( 'order.dt search.dt', function () {
            ScheduleTable_DT.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
                cell.innerHTML = i+1;
            } );
        }).draw();
        ScheduleTable_DT.on( 'page.dt', function () {
            set_display_results();
        } );
    };

    function _dateIsLive(row_data) {
        let isLive = false;
        let date = row_data["date"];
        let startTime = row_data["startTime"];
        let hour = parseInt(startTime.substring(0, 2));
        let minute = parseInt(startTime.substring(3, 5));
        let startDateTime = moment(date).add(hour, 'hours').add(minute, 'minutes');

        let durationHours = parseInt(row_data["durationHours"]);
        let durationMinutes = parseInt(row_data["durationMinutes"]);
        let endDateTime = moment(date).add(hour, 'hours').add(minute, 'minutes').add(durationHours, 'hours').add(durationMinutes, 'minutes');

        let enabled = row_data["enabled"];
        if (moment().isBetween(startDateTime, endDateTime, '[]') && enabled !== false) {
            isLive = true;
        }
        return isLive;
    }

    function _dateIsInThePast(row_data) {
        let isInThePast = false;
        let row_date = row_data.date;
        //DateTime of scheduled
        let startHour = row_data.startTime.substring(0,2);
        let startMinutes= row_data.startTime.substring(3,5);
        let durationHours = parseInt(row_data.durationHours);
        let durationMinutes  = parseInt(row_data.durationMinutes);
        let row_date_m = moment(row_date).add(parseInt(startHour),'h').add(durationHours,'h').add(parseInt(startMinutes),'m').add(durationMinutes,'m');
       // console.log("Schedule date-time:" + row_date_m.format('lll'));
        //DateTime now!
        let date_now = new Date();
        let date_now_m = moment(date_now);
      //  console.log("Now date-time:" + date_now_m.format('lll'));
        // Compare
        if (row_date_m.isBefore(date_now_m)) {
            isInThePast = true;
        }
        return isInThePast;
    }
    function _temp_printDateTimeOfRow(date,time) {

       console.log("date:" + date);
       console.log("time:" + time)
    }
    function  set_display_results(json) {

        let _message_cancellations_html = "";
        let _message_roomInactive_html = "";
        let _message_scheduledEventInactive_html = ""
        if (json !== undefined) {
            if (json.data.message_cancellations !== "") {
                _message_cancellations_html += '<i class="fas fa-info"></i> ' + json.data.message_cancellations;
            }
            if (json.data.message_roomInactive !== "") {
                _message_roomInactive_html += '<i class="fas fa-exclamation-triangle"></i> ' + json.data.message_roomInactive;
            }
            if (json.data.message_scheduleEventInactive !== "") {
                _message_scheduledEventInactive_html += '<i class="fas fa-exclamation-triangle"></i> ' + json.data.message_scheduleEventInactive;
            }
        }
        $("#timetable_msg_cancellations").html( _message_cancellations_html);
        $("#timetable_msg_InactiveClassroom").html(_message_roomInactive_html);
        $("#timetable_msg_InactiveScheduledEvent").html(_message_scheduledEventInactive_html);

        let page = ScheduleTable_DT.page.len();
        let info = ScheduleTable_DT.page.info();
        let total = ScheduleTable_DT.rows().count();
        let page_start = (info.page * page + 1);
        let page_end = page_start + (page-1);
        if (page_end > total) {
            page_end = total;
        }
        if (total === 0) {
            $("#count_results").html('???????????????? ?????? 0 ???? 0 ?????? 0 ????????????????');
        }
        else {
            $("#count_results").html('???????????????? ?????? ' + page_start + ' ???? ' + page_end + ' ?????? ' + total + ' ????????????????');
        }
        //Set Delete Button Status
        //Enable 8 row for disabling schedules on the run
        // if (dashboard.canDelete) {
        // }
        // else {
        //     $("#deleteSchedule").prop("disabled",true);
        //     $("#save-button").prop("disabled",true);
        //     $("#schedule_etable").find(".cancel-scheduled").addClass("disabled");
        //     $("#schedule_etable").find(".un-cancel-scheduled").addClass("disabled");
        //     document.getElementById('active_select').switchButton('disable');
        //     $("._fixed_error_msg").html("???? ???????????????????? ?????????????????? ???????????????????? ???????? ??????????????????????. ?????? ???????????????????????? ??????????????????????????")
        // }

        let row_0_data = ScheduleTable_DT.row(0).data();
        if (row_0_data != null && row_0_data.repeat === "regular") {
            let m_form_date = moment(row_0_data["fromDate"]).format('LL');
            let m_to_date = moment(row_0_data.toDate).format('LL');
            $("#effective_dates").html("( " + m_form_date + " - " + m_to_date + " )");
        }
        //Check and Mark Live.. ( if Live => disable edit )
        if ( json !== undefined) {
            let all_in_the_past = true;
            json.data.results.forEach(function (row_data, index) {
                let date = row_data["date"];
                let startTime = row_data["startTime"];
                let hour = parseInt(startTime.substring(0, 2));
                let minute = parseInt(startTime.substring(3, 5));
                let startDateTime = moment(date).add(hour, 'hours').add(minute, 'minutes');

                let durationHours = parseInt(row_data["durationHours"]);
                let durationMinutes = parseInt(row_data["durationMinutes"]);
                let endDateTime = moment(date).add(hour, 'hours').add(minute, 'minutes').add(durationHours, 'hours').add(durationMinutes, 'minutes');

                let enabled = row_data["enabled"];
                if (moment().isBetween(startDateTime, endDateTime, '[]') && enabled !== false) {
                    if (_message_roomInactive_html === "" && _message_scheduledEventInactive_html === "") {
                        $("._fixed_error_msg").html("?????????? ???? ?????????????? ?????????????? ???????????????? ?????? ?????? ???????????????????? ????????????????????????????. ?? ?????????????????????? ???????? ?????????????????? ??????????????????????????????!");
                        let allow_delete = false;
                        disableEditing(msg,allow_delete);
                    }
                }
                if (all_in_the_past === true) {
                    if (!(_dateIsInThePast(row_data))) {
                        all_in_the_past = false;
                    }
                }
            });
            if (all_in_the_past) {
                let msg = "???????????????????????? ????????????????. ?? ?????????????????????? ???????? ??????????????????????????????!";
                let allow_delete = true;
                disableEditing(msg,allow_delete);

                //Hide warnings. It does not make sense!
                $("#timetable_msg_cancellations").hide();
                $("#timetable_msg_InactiveClassroom").hide();
                $("#timetable_msg_InactiveScheduledEvent").hide();
            }
        }
        loader.hideLoader();
    }

    function disableEditing(msg, allow_delete) {
        dashboard.canEdit = false;
        $("#save-button").attr('disabled', true);
        $("._fixed_warn_msg").html(msg);
        if (allow_delete) {
            $("#deleteSchedule").prop("disabled", false);
        }
        else {
            $("#deleteSchedule").prop("disabled", true);
        }
        $("#enable-co-button").prop("disabled",true);
        $("#disable-co-button").prop("disabled",true);
    }

    function EventPDFHeader() {

        let html="";
        html += "?????????????????? ????????????????????/????????????????????\n" +
            "????????????????: " + $("#_event_title").text() + "\n";
        return html;
    }

    dashboard.evtimetable.rebuildScheduleTable = function () {
        ScheduleTable_DT.ajax.reload( function ( json ) {
            set_display_results(json);
        } );
    };
})();
