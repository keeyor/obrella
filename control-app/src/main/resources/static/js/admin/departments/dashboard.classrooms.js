(function () {
    'use strict';

    dashboard.classrooms = dashboard.classrooms || {};

    let roomsDT;
    let no_devices;
    let $classroom_device_list;
    let serialize_form;
    let roomsSelectDT;

    dashboard.classrooms.initDT = function () {

        let $roomsDtElem = $("#roomsDataTable");
        roomsDT = $roomsDtElem.DataTable({
            "ajax": dashboard.siteurl + '/api/v1/dt/class.web/department/' + dashboard.departments.selectedDepartmentId,
            "columns": [
                {"data": null},
                {"data": "id"},
                {"data": "name"},
                {"data": "code"},
                {"data": "description"},//4
                {"data": "calendar"},
                {"data": "id"},
            ],
            "language": dtLanguageGr,
            order: [[2, 'asc']],
            "dom": '<"top"fl><p>rt<"bottom">p<"clear">',
            "pageLength": 25,
            "pagingType": "full_numbers",
            "aoColumnDefs": [
                {
                    "aTargets": [0],
                    "sortable": false,
                    "sWidth": "20px"
                },
                {
                    "aTargets": [1,4],
                    "mData": "id",
                    "visible": false,
                },
                {
                    "aTargets": [2],
                    "mRender": function (data,type,row) {
                        return '<h6 class="pb-0 mb-0" style="color: #003476">' + data + '</h6>'
                               + row["description"];
                    }
                },
                {
                    "aTargets": [4],
                    "mRender": function (data) {
                        return '<b>' + data + '</b>';
                    }
                },
                {
                    "aTargets": [5],
                    "mData": "calendar",
                    "mRender": function (data) {
                        if (data === "true") {
                            return '<i class="fas fa-circle" style="color:green" title="ενεργή αίθουσα/χώρος"></i>';
                        }
                        else {
                            return '<i class="fas fa-circle" style="color:red" title="ανενεργή αίθουσα/χώρος"></i>';
                        }
                    }
                },
                {
                    "aTargets": [6],
                    "mData": "id",
                    "sortable": false,
                    "className": "text-right",
                    "mRender": function () {
                        return '<button type="button" title="αφαίρεση αίθουσας" class="btn btn-sm"><i class="fas fa-minus-circle"></i> </button>';
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
            $("#count_room_results").html("" + roomsDT.rows().count() + "");
        }

        InitControls();
        RegisterEvents();

        function InitControls() {
            $classroom_device_list = $("#classroom_device_list");
            $("#room_calendar_toggle").bootstrapToggle({
                size: 'small',
                onstyle: 'primary',
                offstyle: 'light'
            });
        }
        function RegisterEvents() {
            $("#newRoomBt").on("click", function(e) {
                loadRoomSelectModal();
                e.preventDefault();
            });

            $("#addAssignedRooms").on('click',function(){
                let nodes= roomsSelectDT.rows( { selected: true } ).data();
                if (nodes.length>0) {
                    let roomsIds =[]
                    let department_id = dashboard.departments.selectedDepartmentId;
                    for (let l = 0; l < nodes.length; l++) {
                        roomsIds.push(nodes[l].id);
                    }
                    postNewlyAssignedRooms(department_id, roomsIds);
                }
            })
            $roomsDtElem.on("click", "tbody button", function (e) {
                // get selected row index
                let table_cell = $(this).closest('td');
                let rowIdx = roomsDT.cell(table_cell).index().row;
                let row_data = roomsDT.row( rowIdx ).data();
                let msg = '<div>Ο χώρος θα αφαιρεθεί από τη λίστα των διαθέσιμων χώρων διδασκαλίας του τμήματος! Είστε σίγουρος;</div>';
                alertify.confirm('<i style="color: red" class="fas fa-trash-alt"></i> Αφαίρεση Χώρου Διδασκαλίας', msg,
                    function () {
                        postUnAssignRoom(dashboard.departments.selectedDepartmentId,row_data.id);
                    },
                    function () {
                    }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
                e.stopPropagation();
            });
        }
        function loadRoomSelectModal() {
            // initialize datatable if not initialized yet! else reload ajax
            if ( ! $.fn.DataTable.isDataTable( '#roomSelectDataTable' ) ) {
                dashboard.classrooms.initRoomsSelectsDT();
            }
            else {
                reloadRoomSelectDepartmentInModal();
            }
            $("#default_dp_id").val(dashboard.departments.selectedDepartmentId);
            $("#roomSelectModalLabel").html('<div style="font-size: 1.4em">' + $("#department_name").val() + '</div><small>Προσθήκη στη λίστα των Αιθουσών </small>');
            $("#roomSelectModal").modal('show');
        }
        function reloadRoomSelectDepartmentInModal() {
            roomsSelectDT.ajax.url(dashboard.siteurl + '/api/v1/dt/class.web/u/department/' + dashboard.departments.selectedDepartmentId);
            roomsSelectDT.ajax.reload();
        }
        function postNewlyAssignedRooms(department_id, roomsIds) {
            $.ajax({
                type:        "POST",
                url: 		  dashboard.siteurl + '/api/v1/department/assign_rooms/' + department_id,
                contentType: "application/json; charset=utf-8",
                data: 		  JSON.stringify(roomsIds),
                async:		  true,
                success: function(){
                    roomsDT.ajax.url = dashboard.siteurl + '/api/v1/dt/class.web/department/' + department_id;
                    roomsDT.ajax.reload( function () {
                        $("#roomSelectModal").modal('hide');
                        alertify.notify("Οι Διαθέσιμες Αίθουσες ενημερώθηκαν με επιτυχία", "success");
                    });
                },
                error: function ()  {
                    $("#roomSelectModal").modal('hide');
                    alertify.error('Error-Update-Rooms-Error');
                }
            });
        }
        function postUnAssignRoom(department_id,roomId) {
            $.ajax({
                type:        "POST",
                url: 		  dashboard.siteurl + '/api/v1/department/unassign_room/' + department_id,
                contentType: "application/json; charset=utf-8",
                data: 		  roomId,
                async:		  true,
                success: function(){
                    roomsDT.ajax.url = dashboard.siteurl + '/api/v1/dt/class.web/department/' + department_id;
                    roomsDT.ajax.reload( function () {
                        alertify.notify("Η Άίθουσα αφαιρέθηκε με επιτυχία", "success");
                    });
                },
                error: function ()  {
                    $("#courseSelectModal").modal('hide');
                    alertify.eror('Error-UnAssign-Room-Error');
                }
            });
        } //postUpdate

    }; // Staff DataTable Init

    dashboard.classrooms.initRoomsSelectsDT = function () {

        roomsSelectDT = $("#roomSelectDataTable").DataTable({
            "ajax": dashboard.siteurl + '/api/v1/dt/class.web/u/department/' + dashboard.departments.selectedDepartmentId,
            "columns": [
                {"data": "id"},
                {"data": "name"},
                {"data": "code"},
                {"data": "description"}
            ],
            select: {
                style: 'multi'
            },
            "language": dtLanguageGr,
             order: [[1, 'asc']],
            "pageLength": 10,
            "pagingType" : "full_numbers",
            "bLengthChange" : false,
            "aoColumnDefs": [
                {
                    "aTargets": [0,2,3],
                    "mData": "id",
                    "visible": false
                },
                {
                    "aTargets": [1],
                    "mRender": function (data,type, row) {
                        let ret = '<h6 class="pb-0 mb-0" style="color: #003476">' + data + '</h6>';
                        ret += row["description"];
                        return ret;
                    }
                },
            ]
        });
    }
 })();
