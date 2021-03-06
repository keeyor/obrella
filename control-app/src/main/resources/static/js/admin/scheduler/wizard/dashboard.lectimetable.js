(function () {
    'use strict';

    dashboard.lectimetable = dashboard.lectimetable || {};

    let ScheduleTable_DT;

    dashboard.lectimetable.buildScheduleTable = function () {


        let schedule_id = $("#id").val();
        let $scheduleDtElem = $("#schedule_table");

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
            fixedHeader: true,
            language: dtLanguageGr,
            pagingType: "full_numbers",
            "dom": '<"top"flB><p>rti<"bottom">p<"clear">',
            "columns": [
                {"data": null},
                {"data": "enabled"},
                {"data": "id"},
                {"data": "repeat"}, //3
                {"data": "period"}, //4
                {"data": "dayOfWeek"}, //5
                {"data": "date"}, //6
                {"data": "date"}, //7
                {"data": "startTime"}, //8
                {"data": "durationHours"}, //9
                {"data": "durationMinutes"}, //10
                {"data": "type"},   //11
                {"data": "department.title"}, //12
                {"data": "course.title"},   //13
                {"data": "supervisor.name"}, //14
                {"data": "classroom.name"}, //15
                {"data": "broadcast"}, //16
                {"data": "access"}, //17
                {"data": "recording"}, //18
                {"data": "publication"}, //19
                {"data": "id"},
                {"data": "fromDate"}, //21
                {"data": "toDate"}, //22
                {"data": "argia"}, //23
                {"data": "cancellation"}, //24
                {"data": "overlapInfo"} //25
            ],
            "aoColumnDefs": [
                {
                    "aTargets": [2,7,10,11,12,13,14, 15,16,17,18,19,21,22,23,24,25],
                    "sortable": false,
                    "visible": false,
                    "sWidth": "0px"
                },
                {
                    "aTargets": [1],
                    "mData": "enabled",
                    "sortable": false,
                    "className" : "dt-center",
                    "sWidth": "10px",
                    "mRender": function (data,type,row) {
                        if (data) {
                            return '<i class="fas fa-circle" style="color:greenyellow"   title="???????????????????????????? ????????????????"></i>';
                        }
                        else {
                            if (row["argia"] != null)  {
                                return '<i class="fas fa-circle" style="color:orangered"    title="??????????/??????????:' + row.argia.name + '"></i>';
                            }
                            else if (row["cancellation"] != null)  {
                                return '<i class="fas fa-circle" style="color:orangered"    title="??????????????:' + row.cancellation.title + '"></i>';
                            }
                            else if (row["overlapInfo"] != null)  {
                                return '<i class="fas fa-circle" style="color:orangered"    title="????????????????????????:' + row.overlapInfo.title + '"></i>';
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
                    "mData": "period",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        return dashboard.broker.selectPeriod(data);
                    }
                },
                {
                    "aTargets": [5],
                    "mData": "dayOfWeek" ,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        return dashboard.broker.selectDayOfWeek(data);
                    }
                },
                {
                    "aTargets": [6],
                    "mData": "date",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data != null && data !== "") {
                            let display_date = moment(data).format('DD MMMM YYYY');
                            let hiddenDataForSorting = '<span style="display:none">' + data + '<br/></span>';
                            return hiddenDataForSorting + '<b>' + display_date + '</b>';
                        }
                        else {
                            return "";
                        }
                    }
                },
                {
                    "aTargets": [7], // only for PDF, EXCEL
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
                    "aTargets": [8],
                    "mData": "startTime",
                    "className" : "dt-center",
                    "mRender": function (data,type,row) {
                            return data;
                    }
                },
                {
                    "aTargets": [9],
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
                    "aTargets": [13],
                    "mData": "course.title",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        return data;
                    }
                },
                {
                    "aTargets": [14],
                    "mData": "supervisor.name",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        return data;
                    }
                },
                {
                    "aTargets": [15],
                    "mData": "classroom.name",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        return data;
                    }
                },
                {
                    "aTargets": [16],
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
                    "aTargets": [17],
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
                    "aTargets": [18],
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
                    "aTargets": [19],
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
                    "aTargets": [20],
                    "mData": "id",
                    "sortable": false,
                    "mRender": function (data,type,row) {
                        let add_class = "";
                        if (_dateIsInThePast(row)) {
                           // add_class = "disabled";
                            return '';
                        }
                        if ( row["argia"] == null && row["cancellation"] == null && row["overlapInfo"] == null) {
                            return  '<a role="button" title="?????????????? ????????????" class="btn btn-secondary btn-sm cancel-scheduled ' + add_class + '" href="#" ' +
                                'data-id="' + data + '" data-date="' + row["date"] + '" data-title="' + row["course"].title + '" data-type="lecture">' +
                                '<i style="color:orangered" class="fas fa-ban" style="color:orangered"></i>' +
                                '</a>';
                        }
                        else if ( row["cancellation"] != null) {
                            return  '<a role="button" title="???????????????????????? ????????????" class="btn btn-secondary btn-sm un-cancel-scheduled ' + add_class + '" href="#" ' +
                                'data-id="' + data + '" data-date="' + row["date"] + '" data-title="' + row["course"].title + '" data-type="lecture">' +
                                '<i style="color:green" class="fas fa-circle-notch"></i>' +
                                '</a>';
                        }
                        else if ( row["overlap"] != null) {
                            return  "";
                        }
                        else {
                            return "";
                        }
                    }
                }
            ],
            buttons: [
                {extend: 'pdf',
                    exportOptions: {
                        columns: [ 5,7,8,9,13],
                        stripHtml: true,
                    },
                    title: LecturePDFHeader(),
                    filename: 'openDelos-schedule',
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
                    text:'<span title="?????????????? ???? PDF"><i class="fas fa-download"></i> PDF</span>',
                    className: 'ml-2 blue-btn-wcag-bgnd-color text-white mb-4',
                    orientation: 'portrait'
                }
            ],
            "rowCallback": function( row, data ) {
                if (_dateIsInThePast(data)) {
                    dashboard.canDelete = false;
                }
            },
            "initComplete": function(settings, json) {
                set_display_results(json);
            }

        });
        ScheduleTable_DT.on( 'order.dt search.dt', function () {
            ScheduleTable_DT.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
                cell.innerHTML = i+1;
            } );
        }).draw();
        ScheduleTable_DT.on( 'page.dt', function (json) {
            set_display_results();
        } );
    };

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
       // console.log("Now date-time:" + date_now_m.format('lll'));
        // Compare
        if (row_date_m.isBefore(date_now_m)) {
            isInThePast = true;
        }
        return isInThePast;
    }

    function  set_display_results(json) {

        let _message_pauses_html = "";
        let _message_cancellations_html = "";
        let _message_overlaps_html = "";

        //Set Messages if one or more Cancelled,Paused or Overlaps exist in data
        if (json !== undefined) {
            if (json.data.message_pauses !== "") {
                _message_pauses_html = json.data.message_pauses;
            }
            if (json.data.message_cancellations !== "") {
                _message_cancellations_html += json.data.message_cancellations;
            }
            if (json.data.message_overlaps !== "") {
                _message_overlaps_html += json.data.message_overlaps;
            }
        }
        $("#timetable_msg_pauses").html( _message_pauses_html);
        $("#timetable_msg_cancellations").html( _message_cancellations_html);
        $("#timetable_msg_overlaps").html( _message_overlaps_html);


        //Set Period (Effective) Dates
        let row_0_data = ScheduleTable_DT.row(0).data();
        if (row_0_data != null && row_0_data["repeat"] === "regular") {
            let m_form_date = moment(row_0_data["fromDate"]).format('LL');
            let m_to_date = moment(row_0_data["toDate"]).format('LL');
            $("#_effective_dates").html("( " + m_form_date + " - " + m_to_date + " )");
        }
        //Check and Mark Live.. ( if Live => disable edit )
        if ( json !== undefined) {
            json.data.results.forEach(function (row_data, index) {
                let date = row_data["date"];
                let startTime = row_data["startTime"];
                let hour = parseInt(startTime.substring(0, 2));
                let minute = parseInt(startTime.substring(3, 5));
                let startDateTime = moment(date).add(hour, 'hours').add(minute, 'minutes');

                let durationHours = parseInt(row_data["durationHours"]);
                let durationMinutes = parseInt(row_data["durationMinutes"]);
                let endDateTime = moment(date).add(hour, 'hours').add(minute, 'minutes').add(durationHours, 'hours').add(durationMinutes, 'minutes');

                if (moment().isBetween(startDateTime, endDateTime, '[]') && row_data.enabled !== false) {
                    $(".cancel-scheduled").hide();
                    $("#save-button").attr('disabled', true);
                    $("._fixed_error_msg").html("?????????? ???? ?????????????? ?????????????? ???????????????? ?????? ?????? ???????????????????? ????????????????????????????. ?? ?????????????????????? ???????? ?????????????????? ??????????????????????????????!");
                    $("#deleteSchedule").prop("disabled",true);
                    $("#enable-co-button").prop("disabled",true);
                    $("#disable-co-button").prop("disabled",true);
                }
            })
        }
        loader.hideLoader();
    }

    function LecturePDFHeader() {

        let html="";
        html += "?????????????????? ????????????????????/????????????????????\n\n" +
                "????????????: " + $("#_course_title").text() + " , ?????????? " + $("#_department_title").text() + "\n" +
                "?????????????????? ??????????????????: " + $("#_staff_name").text() + "\n" +
                "????????????????: " + $("#_period_title").text() + " " + $("#_effective_dates").text() + " - ???????????????????? ????????: " + $("#resource_ay").text().replace(/\s+/g,' ').trim() + "\n" +
                "??????????????: " + $("#_classroom_name").text() + "\n" +
                $("#_broadcast_info").text().replace(/\s+/g,' ').trim()  + "\n" +
                $("#_recording_info").text().replace(/\s+/g,' ').trim()  + "\n" +
                $("#_publication_info").text().replace(/\s+/g,' ').trim();
        return html;
    }

    dashboard.lectimetable.rebuildScheduleTable = function () {
        loader.showLoader();
        ScheduleTable_DT.ajax.reload( function ( json ) {
            set_display_results(json);
        } );
    };
})();
