(function () {
    'use strict';

    dashboard.users = dashboard.users || {};

    let unitRightsDT;
    let staffRightsDT;
    let staffEventRightsDT;
    let InstitutionUnitsDT;

    dashboard.users.init = function () {

    };

    dashboard.users.assignedUnitsDT = function () {

        let $unitRights = $("#unitRightsDataTable");
        let $staff_id = $("#staff_id");

        unitRightsDT = $unitRights.DataTable({
            "ajax": dashboard.siteurl + '/api/v1/dt/managers/assigned_units/' + $staff_id.val(),
            "columns": [
                {"data": null}, //index column
                {"data": "unitType"},
                {"data": "unitId"},
                {"data": "unitTitle"},
                {"data": "contentManager"},//4
                {"data": "dataManager"}, //5
                {"data": "scheduleManager"}, //6
                {"data": "unitId"}
            ],
            dom: 'Brtip',
            buttons: {
                buttons: [
                    {
                        text: '<i class="fas fa-plus-circle"></i> Ανάθεση Μονάδας', className: 'my-2', attr: {id: 'openUnitsSelectModal'},
                        action: function () {
                            openUnitsSelectModal();
                        }
                    },
                ],
                dom: {
                    button: {
                        className: 'btn btn-sm green-btn-wcag-bgnd-color text-white openUnitsSelectModal'
                    }
                }
            },
            "language": dtLanguageGr,
            order: [[3, 'asc']],
            "pageLength": 10,
            "aoColumnDefs": [
                {
                    "aTargets": [2],
                    "mData": "id",
                    "visible": false
                },
                {
                    "aTargets": [1],
                    "mData": "unitType",
                    "mRender": function (data) {
                        if (data === "SCHOOL") return "Σχολή";
                        else if (data === "INSTITUTION") return "Ίδρυμα";
                        else if (data === "DEPARTMENT") return "Τμήμα";
                        else return 'ΑΛΛΟ';
                    }
                },
                {
                    "aTargets": [4],
                    "className": "text-center",
                    "mRender": function (data) {
                        let hiddenDataForSorting = '<span style="display:none">' + data + '<br/></span>';
                        if (data === true) {
                            return hiddenDataForSorting + "<input type=\"checkbox\" checked class=\"unit_content_toggle _toggle \">";
                        }
                        else {
                            return hiddenDataForSorting + "<input type=\"checkbox\"  class=\"unit_content_toggle _toggle \">";
                        }
                    }
                },
                {
                    "aTargets": [5],
                    "className": "text-center",
                    "mRender": function (data) {
                        let hiddenDataForSorting = '<span style="display:none">' + data + '<br/></span>';
                        if (data === true) {
                            return hiddenDataForSorting + "<input type=\"checkbox\" checked class=\"unit_data_toggle _toggle \">";
                        }
                        else {
                            return hiddenDataForSorting + "<input type=\"checkbox\"  class=\"unit_data_toggle _toggle \">";
                        }
                    }
                },
                {
                    "aTargets": [6],
                    "className": "text-center",
                    "mRender": function (data) {
                        let hiddenDataForSorting = '<span style="display:none">' + data + '<br/></span>';
                        if (data === true) {
                            return hiddenDataForSorting + "<input type=\"checkbox\" checked class=\"unit_schedule_toggle _toggle \">";
                        }
                        else {
                            return hiddenDataForSorting + "<input type=\"checkbox\"  class=\"unit_schedule_toggle _toggle \">";
                        }
                    }
                },
                {
                    "aTargets": [7],
                    "mData": "id",
                    "sortable": false,
                    "className": "text-right",
                    "mRender": function () {
                        return  '<button type="button" title="αφαίρεση μονάδας" class="btn text-white btn-sm btn-danger remove_assigned_unit"><i class="fas fa-trash-alt"></i> </button>';
                    }
                }
            ],
            "initComplete": dashboard.users.refreshTableResults
        });
        unitRightsDT.on( 'order.dt search.dt', function () {
            unitRightsDT.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
                cell.innerHTML = i+1;
            } );
        } ).draw();

        function openUnitsSelectModal() {
            if ( ! $.fn.DataTable.isDataTable( '#runitSelectDataTable' ) ) {
                dashboard.users.InitInstitutionUnits();
            }
            else {
                 InstitutionUnitsDT.ajax.reload(function(){
                     //reset DT page
                     InstitutionUnitsDT.row(0).show().draw(false);
                     //get already responsible units (ids)
                     let nodes = unitRightsDT.rows().data();
                     let assignedIds =[];
                     for (let l = 0; l < nodes.length; l++) {
                         assignedIds.push(nodes[l].unitId);
                     }
                     //remove all if institution is assigned
                     if (assignedIds.includes(dashboard.institutionId)) {
                         InstitutionUnitsDT.rows().remove().draw();
                         alertify.notify("Όλα τα διαθέσιμα δικαιώματα έχουν εκχωρηθεί", "success");
                     }
                     else {
                         //remove institution selection (row 0) from table, if at least one unit is assigned
                         if (assignedIds.length > 0) {
                             InstitutionUnitsDT.row(0).remove().draw();
                         }
                         InstitutionUnitsDT.rows().deselect();
                         //select already responsible units to InstitutionUnitsDT
                         InstitutionUnitsDT.column(0, {order: 'applied'}).nodes().each(function (cell, i) {
                             let unitId = InstitutionUnitsDT.cell(i, 0).data();
                             let parentId = InstitutionUnitsDT.cell(i, 1).data();
                             if (assignedIds.includes(unitId) || assignedIds.includes(parentId)) {
                                 let ov_row = InstitutionUnitsDT.row(i).node();
                                 $(ov_row).addClass("overlap");
                                 //console.log("removing row on id:" + InstitutionUnitsDT.cell(i,3).data() + " id:" + unitId);
                             }
                         });
                        //console.log( 'Removing ' + InstitutionUnitsDT.rows('.overlap').data().length + ' row(s) with ovelap School/Department' );
                         InstitutionUnitsDT.rows('.overlap').remove().draw();
                         let staff_name = $("#staff_name").val();
                         //set modal title
                         $("#runitSelectModalLabel").html('<div style="font-size: 1.4em">' + staff_name + '</div><small>ΠΑΡΑΧΩΡΗΣΗ ΔΙΚΑΙΩΜΑΤΟΣ: Μονάδα</small>');
                         $("#runitSelectModal").modal('show');
                     }
                 });
            }
        }

        $unitRights.on("click", "tbody button", function (e) {
            // get selected row index
            let table_cell = $(this).closest('td');
            let rowIdx = unitRightsDT.cell(table_cell).index().row;
            let row_data = unitRightsDT.row( rowIdx ).data();
            let staff_id = $("#staff_id").val();
            let staff_name = $("#staff_name").val();
            if ($(this).hasClass('remove_assigned_unit')) {
                let msg = '<div class="font-weight-bold">Η "' + row_data.unitTitle + '" θα αφαιρεθεί από τα δικαιώματα του χρήστη: "' + staff_name + '". Είστε σίγουρος;</div>';
                alertify.confirm('<i style="color: red" class="fas fa-trash-alt"></i> Διαγραφή Μονάδας', msg,
                    function () {
                        postUnAssignUnit(staff_id,row_data.unitId);
                    },
                    function () {
                    }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
            }
            e.preventDefault();
        });

        function postUnAssignUnit(staff_Id,unitId) {
            $.ajax({
                type:        "POST",
                url: 		  dashboard.siteurl + '/api/v1/managers/unassign_unit/' + staff_Id + '/unit/' + unitId,
                contentType: "application/json; charset=utf-8",
                async:		  true,
                success: function(){
                    dashboard.users.reloadAssignedUnitsTable(staff_Id,"Η Μονάδα αφαιρέθηκε με επιτυχία");
                },
                error: function ()  {
                    alertify.alert('Error-UnAssign-Unit-Error');
                }
            });
        }

    }

    dashboard.users.assignedStaffCoursesDT = function () {

        let $staffRights = $("#staffRightsDataTable");
        let user_id = $("#userId").val();

        staffRightsDT = $staffRights.DataTable({
            "ajax": dashboard.siteUrl+ '/api/v1/dt/managers/assigned_courses/' + user_id,
            "columns": [
                {"data": null}, //index column
                {"data": "staffMemberId"}, //1
                {"data": "staffMemberName"},
                {"data": "courseId"}, //3
                {"data": "courseTitle"},
                {"data": "contentManager"}, //5
                {"data": "scheduleManager"}, //6
                {"data": "staffMemberId"}
            ],
            "language": dtLanguageGr,
            order: [[2, 'asc']],
            "pageLength": 10,
            "aoColumnDefs": [
                {
                    "aTargets": [1,3,7],
                    "mData": "id",
                    "visible": false
                },
                {
                    "aTargets": [5],
                    "className": "text-center align-top",
                    "mRender": function (data,type,row) {
                        let hiddenDataForSorting = '<span style="display:none">' + data + '</span>';
                        if (data === true) {
                            return hiddenDataForSorting + "<input type=\"checkbox\" placeholder='Ενεργοποίηση/Απενεργοποίηση δικαιώματος' checked class=\"course_content_toggle _toggle \" data-id=\"" + row.id + "\">";
                        }
                        else {
                            return hiddenDataForSorting + "<input type=\"checkbox\" title='Ενεργοποίηση/Απενεργοποίηση δικαιώματος' class=\"course_content_toggle _toggle \">";
                        }
                    }
                },
                {
                    "aTargets": [6],
                    "className": "text-center align-top",
                    "mRender": function (data,type,row) {
                        let hiddenDataForSorting = '<span style="display:none">' + data + '</span>';
                        if (data === true) {
                            return hiddenDataForSorting + "<input type=\"checkbox\" title='Ενεργοποίηση/Απενεργοποίηση δικαιώματος' checked class=\"course_schedule_toggle _toggle  \" data-id=\"" + row.id + "\">";
                        }
                        else {
                            return hiddenDataForSorting + "<input type=\"checkbox\" title='Ενεργοποίηση/Απενεργοποίηση δικαιώματος' class=\"course_schedule_toggle _toggle \">";
                        }
                    }
                },
                {
                    "aTargets": [7],
                    "mData": "id",
                    "sortable": false,
                    "className": "text-right align-top",
                    "mRender": function () {
                        return  '<button type="button" title="αφαίρεση μαθήματος+καθηγητή" class="btn btn-sm text-white btn-danger _remove_right"><i class="fas fa-trash-alt"></i></button>';
                    }
                }
            ],
            "initComplete": dashboard.users.refreshTableResults
        });

        staffRightsDT.on( 'order.dt search.dt', function () {
            staffRightsDT.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
                cell.innerHTML = i+1;
            } );
        } ).draw();

        $staffRights.on("click", "tbody button", function (e) {
            // get selected row index
            let table_cell = $(this).closest('td');
            let rowIdx = staffRightsDT.cell(table_cell).index().row;
            let row_data = staffRightsDT.row(rowIdx).data();
            let staff_id = $("#staff_id").val();
            if ($(this).hasClass("content_toggle")) {
            }
            else if ($(this).hasClass("_remove_right")) {
                let msg = '<div class="font-weight-bold">To επιλεγμένο δικαίωμα θα αφαιρεθεί από τα δικαιώματα του χρήστη. Είστε σίγουρος;</div>';
                alertify.confirm('<i style="color: red" class="fas fa-trash-alt"></i> Διαγραφή Δικαιώματος', msg,
                    function () {
                        dashboard.users.refreshTableResults();
                        postUnAssignStaffCourse(staff_id, row_data.courseId, row_data.staffMemberId);
                        e.stopPropagation();
                    },
                    function () {
                    }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
            }
        });


    }

    dashboard.users.assignedStaffEventsDT = function () {

        let $staffEventRights = $("#staffEventRightsDataTable");
        let user_id = $("#userId").val();

        staffEventRightsDT = $staffEventRights.DataTable({
            "ajax": dashboard.siteUrl + '/api/v1/dt/managers/assigned_events/' + user_id,
            "columns": [
                {"data": null}, //index column
                {"data": "staffMemberId"}, //1
                {"data": "staffMemberName"},
                {"data": "eventId"}, //3
                {"data": "eventTitle"},
                {"data": "contentManager"}, //5
                {"data": "scheduleManager"}, //6
                {"data": "staffMemberId"}
            ],
            "language":dtLanguageGr,
            order: [[4, 'asc']],
            "pageLength": 10,
            "aoColumnDefs": [
                {
                    "aTargets": [1,3,7],
                    "mData": "id",
                    "visible": false
                },
                {
                    "aTargets": [5],
                    "className": "text-center",
                    "mRender": function (data,type,row) {
                        let hiddenDataForSorting = '<span style="display:none">' + data + '<br/></span>';
                        if (data === true) {
                            return hiddenDataForSorting + "<input type=\"checkbox\" title='Ενεργοποίηση/Απενεργοποίηση δικαιώματος' checked class=\"event_content_toggle _toggle \" data-id=\"" + row.id + "\">";
                        }
                        else {
                            return hiddenDataForSorting + "<input type=\"checkbox\"  title='Ενεργοποίηση/Απενεργοποίηση δικαιώματος' class=\"event_content_toggle _toggle\">";
                        }
                    }
                },
                {
                    "aTargets": [6],
                    "className": "text-center",
                    "mRender": function (data,type,row) {
                        let hiddenDataForSorting = '<span style="display:none">' + data + '<br/></span>';
                        if (data === true) {
                            return hiddenDataForSorting + "<input type=\"checkbox\" title='Ενεργοποίηση/Απενεργοποίηση δικαιώματος' checked class=\"event_schedule_toggle _toggle\" data-id=\"" + row.id + "\">";
                        }
                        else {
                            return hiddenDataForSorting + "<input type=\"checkbox\" title='Ενεργοποίηση/Απενεργοποίηση δικαιώματος' class=\"event_schedule_toggle _toggle\">";
                        }
                    }
                },
                {
                    "aTargets": [7],
                    "mData": "id",
                    "sortable": false,
                    "className": "text-right",
                    "mRender": function () {
                        return  '<button type="button" title="αφαίρεση εκδήλωσης+καθηγητή" class="btn btn-sm btn-danger text-white _remove_right"><i class="fas fa-trash-alt"></i> </button>';
                    }
                }
            ],
            "initComplete": dashboard.users.refreshTableResults
        });
        staffEventRightsDT.on( 'order.dt search.dt', function () {
            staffEventRightsDT.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
                cell.innerHTML = i+1;
            } );
        } ).draw();

        staffEventRightsDT.on("click", "tbody button", function (e) {
            // get selected row index
            let table_cell = $(this).closest('td');
            let rowIdx = staffEventRightsDT.cell(table_cell).index().row;
            let row_data = staffEventRightsDT.row( rowIdx ).data();
            let staff_id = $("#staff_id").val();
            postUnAssignStaffEvent(staff_id,row_data.eventId,row_data.staffMemberId);
            e.stopPropagation();
        });

        function openStaffEventSelectModal() {

            let staff_name = $("#staff_name").val();
            dashboard.add_action = "event";
            $("#departments_s21").val("").trigger("change");
            $("#supervisor_s21").val("").trigger("change");
            $("#events_s2").val("").trigger("change");
            $("#staffEventSelectModalLabel").html('<div style="font-size: 1.4em">' + staff_name + '</div><small>ΠΑΡΑΧΩΡΗΣΗ ΔΙΚΑΙΩΜΑΤΟΣ: Καθηγητής+Εκδήλωση</small>');
            $("#staffEventSelectModal").modal('show');
        }

        function postUnAssignStaffEvent(staffId, eventId, staffMemberId) {
            $.ajax({
                type:        "POST",
                url: 		  dashboard.siteurl + '/api/v1/managers/unassign_event/' + staffId + '/event/' + eventId + '/sm/' + staffMemberId,
                contentType: "application/json; charset=utf-8",
                async:		  true,
                success: function(){
                    staffEventRightsDT.ajax.url = dashboard.siteurl + '/api/v1/dt/managers/assigned_events/' + staffId;
                    staffEventRightsDT.ajax.reload( function () {
                        dashboard.users.refreshTableResults();
                        alertify.notify("Η Εκδήλωση αφαιρέθηκε με επιτυχία", "success");
                    });
                },
                error: function ()  {
                    alertify.alert('Error-UnAssign-Event-Error');
                }
            });
        }
    }

    dashboard.users.InitInstitutionUnits = function () {
        let $institutionUnitsDtElem = $("#runitSelectDataTable");
        InstitutionUnitsDT = $institutionUnitsDtElem.DataTable({
            "ajax": dashboard.siteurl + '/api/v1/dt/units-inherited.web',
            "columns": [
                {"data": "id"},
                {"data": "parentId"},
                {"data": "structureType"},
                {"data": "title"},
            ],
            "language":  dtLanguageGr,
            select: {
                style: 'single'
            },
            order: [[1, 'asc']],
            "pageLength": 10,
            "lengthChange"  : false,
            "aoColumnDefs": [
                {
                    "aTargets": [0,1],
                    "mData": "id",
                    "visible": false
                },
                {
                    "aTargets": [2],
                    "name": "structureType",
                    "render": function (data) {
                        if (data != null) {
                            if (data === 'INSTITUTION') {
                                return 'Ίδρυμα';
                            }
                            else if (data === 'SCHOOL') {
                                return 'Σχολή';
                            }
                            else if (data === 'DEPARTMENT') {
                                return 'Τμήμα';
                            }
                        }
                        else {
                            return "-";
                        }
                    }
                },
            ]
        });
    };

})();
