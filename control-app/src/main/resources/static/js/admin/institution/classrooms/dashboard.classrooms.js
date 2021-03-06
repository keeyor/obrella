(function () {
    'use strict';

    dashboard.classrooms = dashboard.classrooms || {};

    let roomsDT;
    let no_devices;
    let $classroom_device_list;
    let serialize_form;

    dashboard.classrooms.initDT = function () {

        let $roomsDtElem = $("#roomsDataTable");
        roomsDT = $roomsDtElem.DataTable({
            "ajax": dashboard.siteurl + '/api/v1/dt/rooms.web',
            "columns": [
                {"data": null},
                {"data": "id"},
                {"data": "name"}, //2
                {"data": "code"}, //3
                {"data": "availableTo"}, //4
                {"data": "usage"}, //5
                {"data": "description"}, //6
                {"data": "calendar"}, //7
                {"data": "id"} //8
            ],
            "language": dtLanguageGr,
            order: [[2, 'asc']],
            "dom": '<"top"fl><p>rt<"bottom">p<"clear">',
            "pageLength": 25,
            "saveState": true,
            "pagingType": "full_numbers",
            "aoColumnDefs": [
                {
                    "aTargets": [1,6],
                    "visible": false,
                },
                {
                    "aTargets": [2],
                    "mRender": function (data,type, row) {
                        let ret = '<div class="pb-0 mb-0" style="color: #003476;font-weight: 500">' + data + '</div>';
                        ret += row["description"];
                        return ret;
                    }
                },
                {
                    "aTargets": [4],
                    "className": "dt-center",
                    "mRender": function (data) {
                        if (data != null) {
                            return data.length;
                        }
                        else {
                            return "-";
                        }
                    }
                },
                {
                    "aTargets": [5],
                    "className": "dt-center",
                    "mData": "usage",
                    "mRender": function (data) {
                        if (data != null) {
                            if (data === "events") {
                                return "?????????? ????????????????????";
                            }
                            else if (data === "lectures") {
                                return "?????????????? ??????????????????????";
                            }
                            else  {
                                return "???????????? ??????????";
                            }
                        }
                        else {
                            return "-";
                        }
                    }
                },
                {
                    "aTargets": [7],
                    "mData": "calendar",
                    "className": "dt-center",
                    "mRender": function (data) {
                        let hiddenDataForSorting = '<span style="display:none">' + data + '</span>';
                        if (data === "true") {
                            return hiddenDataForSorting + '<i class="fas fa-circle" style="color:green" title="???????????? ??????????????/??????????"></i>';
                        }
                        else {
                            return hiddenDataForSorting + '<i class="fas fa-circle" style="color:red" title="???????????????? ??????????????/??????????"></i>';
                        }
                    }
                },
                {
                    "aTargets": [8],
                    "mData": "id",
                    "sortable": false,
                    "className": "text-right",
                    "mRender": function () {
                        return '<button type="button" class="btn blue-btn-wcag-bgnd-color btn-sm btn-pill text-white"><i class="fas fa-edit"></i> </button>';
                    }
                }
            ],
            "initComplete": set_display_results,
        });
        roomsDT.on( 'order.dt search.dt', function () {
            roomsDT.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
                cell.innerHTML = i+1;
            } );
        } ).draw();
        function  set_display_results() {
            $("#count_results").html("" + roomsDT.rows().count() + "");
        }

        InitControls();
        RegisterEvents();

        function InitControls() {
            $("#room_calendar_toggle").bootstrapToggle({
                size: 'small',
                onstyle: 'success',
                offstyle: 'secondary'
            });
        }
        function RegisterEvents() {

            $("input[name=device_url]").change(function(e) {
                let el = $(this);
                if (el.val() !== "") {
                    //console.log("enable calendar toggle");
                    $("#room_calendar_toggle").bootstrapToggle('enable');
                }
            });

            $roomsDtElem.on("dblclick", "tbody td", function (e) {
                let table_cell = $(this).closest('td');
                let rowIdx = roomsDT.cell(table_cell).index().row;
                setupRoomEditForm("edit", "?????????????????????? ????????????????/??????????", rowIdx);
                e.stopPropagation();
            });
            $roomsDtElem.on("click", "tbody button", function () {
                let table_cell = $(this).closest('td');
                let rowIdx = roomsDT.cell(table_cell).index().row;
                setupRoomEditForm("edit", "?????????????????????? ???????????????? ??????????",rowIdx);
            });
            $("#closeUpdateRoom").on('click',function(e){
                let end_serialize = $("#room_form").serialize();
                if (serialize_form !== end_serialize) {
                    closeEditDialogWarning();
                }
                else {
                    unloadEditForm();
                }
            });
            $("#newRoomBt").on("click", function(e) {
                setupRoomEditForm ("add", "-- ?????? ??????????????/?????????? --",-1);
                e.preventDefault();
            });
            $("#deleteRoomBt").on("click", function(e) {
                let room_name = $("#room_name").val();
                let roomId = $("#room_id").val();

                let msg = '<div class="font-weight-bold">?? ??????????????/?????????? "' + room_name + '" ???? ??????????????????! ?????????? ????????????????;</div>';
                msg += '<div>??????????????: ???? ?????????? ???????????????? ?????????????????? ?? ?????????????????????????????????? ???????????????????? ?????? ?????????????????????? ?????? ????????, ?? ???????????????? ???? ????????????????'
                alertify.confirm('<i style="color: red" class="fas fa-trash-alt"></i> ???????????????? ????????????????/??????????', msg,
                    function () {
                         postRoomDelete(roomId);
                    },
                    function () {
                    }).set('labels', {ok: '??????!', cancel: '??????????????'});
                e.preventDefault();
            });

            $("#addOrUpdateRoom").on("click", function() {


                loader.showLoader();
                let devices = [];
                let type            = $("#device_type").val();
                let descr           = $("#device_descr").val();
                let streamAccessUrl = $("#device_url").val().trim();
                let ip              = $("#device_ip").val();
                let mac             = $("#device_mac").val();
                let socket          = $("#device_socket").val();
                let device = {
                    type: type,
                    description: descr,
                    streamAccessUrl: streamAccessUrl,
                    ipAddress: ip,
                    macAddress: mac,
                    socket: socket
                }
                devices.push(device);

                let roomId      = $("#room_id").val();
                let availableTo = $("#departments_s2").val();
                let name        = $("#room_name").val();
                let code        = $("#room_code").val();
                let description = $("#room_descr").val();
                let usage       = $("#room_usage").val();
                let calendar    = $("#room_calendar_toggle").prop('checked');

                let roomData = {
                    id: roomId,
                    name: name,
                    code: code,
                    availableTo: availableTo,
                    description: description,
                    usage: usage,
                    calendar: calendar,
                    devices: devices
                }
                //check for errors
                if ( (streamAccessUrl === null || streamAccessUrl === "") && calendar === true) {
                    alertify.alert("<i style='color:red' class='fas fa-exclamation-triangle'></i> ????????????????","???????????? ????????????????????: ???? ?????????? StreamURL/StreamKey ?????? ???????????? ???? ?????????? ????????!");
                    loader.hideLoader();
                }
                else if (name == null || name ==="" || code == null || code === "") {
                    alertify.alert("<i style='color:red' class='fas fa-exclamation-triangle'></i> ????????????????","???????????????? ?????????????????????? ?????? ??????????. ?????????????????? ???? ???????? ?????? ?????????????????????? ????????");
                    loader.hideLoader();
                }
                else
                {
                    postRoomUpdate(roomData);
                }
            });

        }

        function postRoomUpdate(roomData) {
            $.ajax({
                type:        "POST",
                url: 		  dashboard.siteurl + '/api/v1/room/save',
                contentType: "application/json; charset=utf-8",
                data: 		  JSON.stringify(roomData),
                async:		  true,
                success: function(data){
                    if ( data === "-1") {
                        loader.hideLoader();
                        alertify.error("?? ?????????????? ????????????????/?????????? ?????????????? ??????. ????????????????, ???????????????????????????? ???????? ?????? ????????????", 0);
                    }
                    else {
                        roomsDT.ajax.reload();
                        $("#room_id").val(data);
                        $("#editRoomLabel").html('<div style="font-size: 1.4em">' + roomData.name + '</div><small>?????????????????? ??????????????????</small>');
                        loader.hideLoader();
                        alertify.notify("?? ??????????????/?????????? ???????????????????????? ???? ????????????????", "success");
                        let device = roomData.devices[0];
                        if (device.type != null && device.type !== "" && device.streamAccessUrl != null && device.streamAccessUrl !== "") {
                            $('#room_calendar_toggle').bootstrapToggle('enable');
                        }
                        else {
                            $('#room_calendar_toggle').bootstrapToggle('off');
                            $('#room_calendar_toggle').bootstrapToggle('disable');
                        }
                        $("#deleteRoomBt").show();
                        serialize_form = $("#room_form").serialize();
                    }
                },
                error: function ()  {
                    loader.hideLoader();
                    alertify.alert('Error-Update-Room');
                }
            });
        }
        function postRoomDelete(roomId) {
            $.ajax({
                type:        "DELETE",
                url: 		  dashboard.siteurl + '/api/v1/room/delete/' + roomId,
                contentType: "application/json; charset=utf-8",
                async:		  true,
                success: function(){
                    unloadEditForm();
                    roomsDT.ajax.reload();
                    alertify.notify("?? ??????????????/?????????? ????????????????????", "success");
                },
                error: function (data)  {
                    let info = "?????????????? ????????????";
                    let msg = data.responseText;
                    if (msg === "_FORBIDDEN_LECTURES") {
                        info = "<div class='mt-2'>?? ??????????????/?????????? ?????? ???????????? ???? ??????????????????! ???????????????? ?????????????????? ?????? ?????????????????????? ?????? ????????<br/>" +
                            "</div>";
                    }
                    else if (msg === "_FORBIDDEN_SCHEDULER") {
                        info = "<div class='mt-2'>?? ??????????????/?????????? ?????? ???????????? ???? ??????????????????! ???????????????? ???????????????????? ?????? ?????????????????????? ?????? ????????<br/>" +
                        "</div>";
                    }
                    else if (msg === "_NOT_FOUND") {
                        info = "<div class='mt-2'>?? ??????????????/?????????? ?????? ??????????????</div>";
                    }
                    // $("#courseModal").modal('hide');
                    alertify.alert('????????????', '<i style="color: red" class="fas fa-exclamation-circle"></i> ' + msg + info);
                }
            });
        }                        //postDeviceDelete



        function unloadEditForm() {
            $("#rooms-tab").html("????????????????/??????????");
            $("#rooms_edit_card").hide();
            $("#rooms_card").show();
        }

        function closeEditDialogWarning() {
            let msg = '<div class="font-weight-bold">???? ?????????????? ???? ????????????! ?????????? ?????????????????</div>';
            alertify.confirm('??????????????????????????', msg,
                function () {
                    unloadEditForm();
                },
                function () {
            }).set('labels', {ok: '??????!', cancel: '??????????????'});
        }

        function setupRoomEditForm(action, title, rowIdx) {
            no_devices = 0;
            let $calendar_toggle = $('#room_calendar_toggle');
            $calendar_toggle.bootstrapToggle('off');

            if (rowIdx !== -1) {
                let row_data = roomsDT.row( rowIdx ).data();
                fillClassroomForm(row_data);
            }
            else {
                fillClassroomForm();
            }
            $("#rooms_card").hide();
            $("#rooms_edit_card").show();

            serialize_form = $("#room_form").serialize();
        }

        function fillClassroomForm(data) {

            let $room_name = $("#room_name");
            let $calendar_toggle = $('#room_calendar_toggle');
            $calendar_toggle.bootstrapToggle('off');

            if (data !== undefined && data !== null) {
                $("#editRoomLabel").html('<div style="font-size: 1.4em">' + data.name + '</div><small>?????????????????? ??????????????????</small>');
                $("#room_id").val(data.id);
                $room_name.val(data.name);
                $("#room_code").val(data.code);
                $("#room_descr").val(data.description);
                $("#room_usage").val(data.usage);
                let departmentId = data.availableTo;
                if (departmentId == null) { departmentId = "";}
                $("#departments_s2").val(departmentId).trigger("change");
                let device = data.devices[0];

                if (data.calendar === "true") {
                    $calendar_toggle.bootstrapToggle('on');
                }
                else {
                    $calendar_toggle.bootstrapToggle('off');
                }
                fillDeviceForm(device);
                $("#deleteRoomBt").show();
            }
            else {
                $room_name.val("");
                $("#room_id").val("");
                $("#room_code").val("");
                $("#room_descr").val("");
                $("#room_usage").val("both");
                $(".room_devices_warning").show();
                $(".room_devices").hide();
                $("#editRoomLabel").html('<div style="font-size: 1.4em">??????????????/??????????</div><small>?????? ????????????</small>');
                fillDeviceForm();
                //$calendar_toggle.bootstrapToggle('disable');
                $("#deleteRoomBt").hide();
                $("#departments_s2").val("").trigger("change");
            }
        }


        function fillDeviceForm(device) {
            let can_enable_room = false;
            if (device !== undefined && device !== null) {
                $("#device_type").val(device.type);
                if (device.description !== null && device.description !== "null") {
                    $("#device_descr").val(device.description);
                } else {
                    $("#device_descr").val("");
                }
                if (device.streamAccessUrl !== null && device.streamAccessUrl !== "null") {
                    $("#device_url").val(device.streamAccessUrl);
                } else {
                    $("#device_url").val("");
                }
                if (device.ipAddress !== null && device.ipAddress !== "null") {
                    $("#device_ip").val(device.ipAddress);
                } else {
                    $("#device_ip").val("");
                }
                if (device.macAddress !== null && device.macAddress !== "null") {
                    $("#device_mac").val(device.macAddress);
                } else {
                    $("#device_mac").val("");
                }
                if (device.socket !== null && device.socket !== "null") {
                    $("#device_socket").val(device.socket);
                } else {
                    $("#device_socket").val("");
                }
                if (device.streamAccessUrl !== null && device.streamAccessUrl !== "") {
                    can_enable_room = true;
                }
            }
            else {
                $("#device_type").val("ipcamera");
                $("#device_descr").val("");
                $("#device_url").val("");
                $("#device_ip").val("");
                $("#device_mac").val("");
                $("#device_socket").val("");
            }
            return can_enable_room;
        }
    }; // Staff DataTable Init
 })();
