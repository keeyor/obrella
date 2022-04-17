(function () {
    'use strict';

    dashboard.users = dashboard.users || {};

    let staffDT;
    let unitRightsDT;
    let staffRightsDT;
    let staffEventRightsDT;
    let serialize_form;
    let InstitutionUnitsDT;

    dashboard.users.init = function () {
        dashboard.users.initEditor();
        dashboard.users.InitInstitutionUnits();
    };

    dashboard.users.initEditor = function () {
        InitControls();
        RegisterEvents();
    }; // Staff DataTable Init

    function InitControls() {
        let staff_id = $("#staff_id").val();
        if (staff_id === "") {
            $("#staffModalLabel").html('<div>Νέος Διαχειριστής</div><br/>');
            $(".rights_note").hide();
            $(".rights_assign_warning").show();
        }
        else {
            let staff_name = $("#staff_name").val();

            let user_authorities;
            user_authorities = $("#staff_authorities").val();
            if (user_authorities.includes("STAFFMEMBER")) { $("#r_isStaffMember").val("true");}
            else { $("#r_isStaffMember").val("false"); }

            $("#staffModalLabel").html('<div style="font-size: 1.4em">' + staff_name + '</div><br/>');
            $(".rights_note").show();
            $(".rights_assign_warning").hide();

            let manager_type = $("#staff_role").val();
            $("#r_manager_type").val(manager_type);

            hideShowPanel(manager_type);

            if ( ! $.fn.DataTable.isDataTable( '#staffRightsDataTable' ) ) {
                dashboard.users.assignedUnitsDT();
            }
            else {
                dashboard.users.reloadAssignedUnitsTable(staff_id);
            }
            if ( ! $.fn.DataTable.isDataTable( '#staffRightsDataTable' ) ) {
                dashboard.users.assignedStaffCoursesDT();
            }
            else {
                dashboard.users.reloadAssignedStaffTable(staff_id);
            }
            if ( ! $.fn.DataTable.isDataTable( '#staffEventRightsDataTable' ) ) {
                dashboard.users.assignedStaffEventsDT();
            }
            else {
                dashboard.users.reloadAssignedEventTable(staff_id);
            }
            //hide Delete Button for myself
            if (staff_id === $("#signInUserid").val()) {
                $("#deleteManagerBt").attr("disabled",true);
            }
            else {
                $("#deleteManagerBt").attr("disabled",false);
            }

            $("#staff_rights_card").show();
        }

        $("#staff_password_1").passwordify().focus();
        $("#staff_password_2").passwordify().focus();
        $("#staff_password_1").passwordify({
            maxLength: 12
        })
        $("#staff_password_2").passwordify({
            maxLength: 12
        })
        $("#userType_select").select2({
            minimumResultsForSearch: -1 //hides the searchbox
        });

        $("#staff_enabled").bootstrapToggle({
            on: '<i class="fas fa-power-off"></i>',
            off: '<i class="fas fa-ban"></i>',
            onstyle: "success",
            offstyle: "danger",
            size: "small"
        });
        serialize_form = $("#staff_form").serialize();
    }
    function RegisterEvents() {

        $('#staff_password_1').keyup(function() {
            $("password_error_match").hide();
        });
        $('#staff_password_2').keyup(function() {
            $("password_error_match").hide();
        });
        $("#submit_button").on('click',function(e){
            let staff_password1_val = $("#staff_password_1").data("val");
            let staff_password2_val = $("#staff_password_2").data("val");
            if (staff_password1_val !== staff_password2_val) {
                $("#password_error_match").show();
                e.preventDefault();
            }
            else {
                $("#user_password").val(staff_password1_val);
            }
        });

        $(".close_editor").on('click',function(e){
            let end_serialize = $("#staff_form").serialize();
            let loc = $(this).attr("href");
            if (serialize_form !== end_serialize) {
                closeEditDialogWarning(loc);
                e.preventDefault();
            }
            else {
                window.location = loc;
            }
        });

        $('input[type=radio][name="managerOptions"]').on('change',function() {
            let selected_value = $("input[name='managerOptions']:checked").val();
            let previous_value = $("#r_manager_type").val();
            let manager_name = $("#staff_name").val();
            if (selected_value !== previous_value) {
                showTypeChangeWarning(manager_name, previous_value, selected_value);
            }
        });

        $("#addAssignedUnits").on('click',function(){
            //Update unitRightsDT with user selections
            let nodes= InstitutionUnitsDT.rows( { selected: true } ).data();
            if (nodes.length>0) {
                for (let l = 0; l < nodes.length; l++) {
                    let n_id = nodes[l].id;
                    let n_title= nodes[l].title;
                    let n_st = nodes[l].structureType;
                    let new_assign_row = {
                        "unitType": n_st,
                        "unitId" : n_id,
                        "unitTitle" : n_title,
                        "contentManager": true,
                        "dataManager": true,
                        "scheduleManager":true
                    }
                    unitRightsDT.row.add( new_assign_row ).draw();
                    let staff_id = $("#staff_id").val();
                    let unitPermission = {
                        "unitId": n_id,
                        "unitType": n_st,
                        "contentManager": true,
                        "dataManager": true,
                        "scheduleManager":true
                    }
                    postAssignUnit(staff_id,unitPermission);
                }
            }
            $("#runitSelectModal").modal('hide');
        });

        $("#reveal_passfield").click(function(e) {
            let x = document.getElementById("staff_password_1");
            if (x.type === "password") {
                x.type = "text";
            } else {
                x.type = "password";
            }
            let y = document.getElementById("staff_password_2");
            if (y.type === "password") {
                y.type = "text";
            } else {
                y.type = "password";
            }
            e.preventDefault();
        });

        $("#deleteStaffBt").on('click',function() {
            let staff_name = $("#staff_name").val();
            let staff_id = $("#staff_id").val();

            let msg = '<div class="font-weight-bold">Το Διαχειριστικό Δικαίωμα του "' + staff_name + '" θα ανακληθεί!". Είστε σίγουρος;</div>';
                msg += '<div class="mb-2" style="font-size: 0.9em"><br/><i class="fas fa-info-circle"></i> Υπενθύμιση:'
                msg += '<br/><span style="color:red;font-weight: bold">O "' + staff_name + '" ανήκει στο διδακτικό προσωπικό του Ιδρύματος. ' +
                    'Η ενέργεια αφορά την ανάκληση του Διαχειριστικού Δικαιώματος και ΔΕΝ ΑΦΑΙΡΕΙ την ιδιότητα του ΔΙΔΑΣΚΟΝΤΑ</span>';
                msg += '</div>';

            alertify.confirm('<i style="color: red" class="fas fa-trash-alt"></i> Ανάκληση Διαχειριστή', msg,
                function () {
                    postRecallOrDeleteManager(staff_id);
                },
                function () {
                }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
        });

        $("#deleteManagerBt").on('click',function() {
            let staff_name = $("#staff_name").val();
            let staff_id = $("#staff_id").val();

            let msg = '<div class="font-weight-bold">O χρήστης "' + staff_name + '" θα διαγραφεί!". Είστε σίγουρος;</div>';
                msg += '<div class="mb-2" style="font-size: 0.9em"><br/><i class="fas fa-info-circle"></i> Προσοχή:'
                msg += '<br/><span style="color:red;font-weight: bold">O χρήστης "' + staff_name + '" ΔΕΝ ΑΝΗΚΕΙ ΣΤΟ ΔΙΔΑΚΤΙΚΟ ΠΡΟΣΩΠΙΚΟ ΤΟΥ ΙΔΡΥΜΑΤΟΣ. ' +
                    'H ενέργεια ΘΑ ΔΙΑΓΡΑΨΕΙ ΟΡΙΣΤΙΚΑ το χρήστη από το σύστημα</span>';
                msg += '<br/>Προσοχή: Αν έχουν υπάρχουν διαλέξεις ή εκδηλώσεις που αναφέρονται στο χρήστη, η διαγραφή θα ακυρωθεί';
                msg += '</div>';

            alertify.confirm('<i style="color: red" class="fas fa-trash-alt"></i> Οριστική Διαγραφή Διαχειριστή', msg,
                function () {
                    postRecallOrDeleteManager(staff_id);
                },
                function () {
                }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
        });

        $("#addAssignedStaffCourses").on('click',function() {
            let rights_course_id = $("#courses_s2").val();
            let rights_staff_id = $("#supervisor_s2").val();
            let rights_content  = $('#staff_content_toggle').is(":checked");
            let rights_scheduler = $('#staff_schedule_toggle').is(":checked");

            // console.log(rights_course_id + " " + rights_staff_id + " " + rights_content + " " + rights_scheduler);
            if (rights_course_id == null || rights_staff_id == null || rights_course_id === '' || rights_staff_id === '' ||
                (rights_content === false && rights_scheduler === false)) {
                alertify.error("Το δικαίωμα δεν αποθηκεύτηκε. Υπάρχουν παραλείψεις στη φόρμα εισαγωγής");
            }
            else {
                let staff_id = $("#staff_id").val();
                let coursePermission = {
                    "staffMemberId": rights_staff_id,
                    "courseId": rights_course_id,
                    "contentManager": rights_content,
                    "scheduleManager":rights_scheduler
                }
                postAssignRightToCourseStaff(staff_id, coursePermission);
            }
        });
        $("#addAssignedStaffEvents").on('click',function() {
            let rights_events_id = $("#events_s2").val();
            let rights_staff_id = $("#supervisor_s21").val();
            let rights_content  = $('#event_content_toggle').is(":checked");
            let rights_scheduler = $('#event_schedule_toggle').is(":checked");

            //console.log(rights_events_id + " " + rights_staff_id + " " + rights_content + " " + rights_scheduler);
            if (rights_events_id == null || rights_staff_id == null || rights_events_id === '' || rights_staff_id === '' ||
                (rights_content === false && rights_scheduler === false)) {
                alertify.error("Το δικαίωμα δεν αποθηκεύτηκε. Υπάρχουν παραλείψεις στη φόρμα εισαγωγής");
            }
            else {
                let staff_id = $("#staff_id").val();
                let eventPermission = {
                    "staffMemberId": rights_staff_id,
                    "eventId": rights_events_id,
                    "contentManager": rights_content,
                    "scheduleManager":rights_scheduler
                }
                postAssignRightToEventStaff(staff_id, eventPermission);
            }
        });
    }
    function closeEditDialogWarning(loc) {
        let msg = '<div class="font-weight-bold">Οι αλλαγές θα χαθούν! Είστε σίγουρος?</div>';
        alertify.confirm('Προειδοποίηση', msg,
            function () {
                window.location = loc;
            },
            function () {
            }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
    }

    function postRecallOrDeleteManager(staffId) {

        $.ajax({
            type:        "DELETE",
            url: 		  dashboard.siteurl + '/api/v1/manager/delete/' + staffId,
            contentType: "application/json; charset=utf-8",
            async:		  true,
            success: function(){
                let info = "O χρήστης διαγράφηκε από διαχειριστής με επιτυχία";
                alertify.alert('Επιτυχής Διαγραφή!', info, function(){
                    window.location = "users";
                });
            },
            error: function (data)  {
                let info = "Άγνωστο Σφάλμα";
                let msg = data.responseText;
                if (msg === "_FORBIDDEN_LECTURES") {
                    info = "<div class='mt-2'>O χρήστης δεν μπορεί να διαγραφεί! Βρέθηκαν Διαλέξεις ή/και Εκδηλώσεις που αναφέρονται στον επιλεγμένο " +
                        "χρήστη</div>";
                }
                else if (msg === "_FORBIDDEN_SCHEDULER") {
                    info = "<div class='mt-2'>O χρήστης δεν μπορεί να διαγραφεί! Βρέθηκαν Προγραμματισμένες Μεταδόσεις που αναφέρονται στον επιλεγμένο " +
                        "χρήστη</div>";
                }
                else if (msg === "_FORBIDDEN_SCHEDULED_EVENTS") {
                    info = "<div class='mt-2'>O χρήστης δεν μπορεί να διαγραφεί! Βρέθηκαν Εκδηλώσεις που αναφέρονται στον επιλεγμένο " +
                        "χρήστη</div>";
                }
                else if (msg === "_NOT_FOUND") {
                    info = "<div class='mt-2'>O χρήστης δεν βρέθηκε</div>";
                }
                alertify.alert('Σφάλμα', '<i style="color: red" class="fas fa-exclamation-circle"></i> ' + msg + info);
            }
        });
    }
    function hideShowPanel(manager_type) {

        let $unit_panel     = $(".unit_panel");
        let $course_panel   = $(".course_panel");
        let $admin_panel    = $(".admin_panel");
        let $event_panel    = $(".event_panel");
        $unit_panel.show();
        $course_panel.show();
        if (manager_type === "SA") {$("#saRadio").prop("checked",true);
            $unit_panel.hide();
            $course_panel.hide();
            $event_panel.hide();
            $admin_panel.show();
        }
        if (manager_type === "MANAGER" || manager_type === "INSTITUTION_MANAGER") {
            $("#managerRadio").prop("checked",true);
            $course_panel.hide();
            $event_panel.hide();
            $admin_panel.hide();
        }
        if (manager_type === "SUPPORT" || manager_type === "NOT_SET") {
            $("#supportRadio").prop("checked",true);
            $unit_panel.hide();
            $admin_panel.hide();
            $event_panel.show();
        }
    }
    function showTypeChangeWarning(name ,previous_value, selected_value) {

        let staff_id = $("#staff_id").val();
        let msg = '<div class="font-weight-bold;font-size: 1.2em">ΠΡΟΣΟΧΗ<br/>Ο Διαχειριστικός Ρόλος του/της: "' + name + '" Θα αλλάξει' +
            '<br/>Όλα τα ήδη εκχωρημένα δικαιώματα του χρήστη Θα ΔΙΑΓΡΑΦΟΥΝ!<br/><br/>Είστε σίγουρος;</span></div>';
        msg += '<div class="text-high-emphasis">Πατήστε "Ακύρωση" για ανάκληση της ενέργειας</div>';

        alertify.confirm('<i style="color: red" class="fas fa-user-shield"></i> Τροποποίηση Διαχειριστικού Ρόλου', msg,
            function () {
                $("#r_manager_type").val(selected_value);
                postChangeRole(staff_id, selected_value);
            },
            function () {
                $("input[name='managerOptions'][value=" + previous_value + "]").prop('checked', true);
                hideShowPanel(previous_value);

            }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
    }
    function postAssignUnit(staff_Id,unitPermission) {
        $.ajax({
            type:        "POST",
            url: 		  dashboard.siteurl + '/api/v1/managers/assign_unit/' + staff_Id,
            contentType: "application/json; charset=utf-8",
            data: 		  JSON.stringify(unitPermission),
            async:		  true,
            success: function(){
                unitRightsDT.ajax.url = dashboard.siteurl + '/api/v1/dt/managers/assigned_units/' + staff_Id;
                unitRightsDT.ajax.reload( function () {
                    dashboard.users.refreshTableResults();
                    $("#runitSelectModal").modal('hide');
                    alertify.notify("Τα Δικαιώματα Μονάδων ενημερώθηκαν με επιτυχία", "success");
                });
            },
            error: function ()  {
                $("#runitSelectModal").modal('hide');
                alertify.alert('Error-Update-AssignUnit-Error');
            }
        });
    }
    function postChangeRole(staff_Id,new_role) {
        $.ajax({
            type:        "POST",
            url: 		  dashboard.siteurl + '/api/v1/managers/change_role/' + staff_Id,
            contentType: "application/json; charset=utf-8",
            data: 		  new_role,
            async:		  true,
            success: function(){
                hideShowPanel(new_role);
                dashboard.users.reloadAssignedUnitsTable(staff_Id, "Ο ρόλος άλλαξε");
                dashboard.users.reloadAssignedStaffTable(staff_Id);
            },
            error: function ()  {
                alertify.alert('Error-Update-ChangeRole-Error');
            }
        });
    }
    function postAssignRightToCourseStaff(staff_id, coursePermission) {
        $.ajax({
            type:        "POST",
            url: 		  dashboard.siteurl + '/api/v1/managers/assign_course/' + staff_id,
            contentType: "application/json; charset=utf-8",
            data: 		  JSON.stringify(coursePermission),
            async:		  true,
            success: function(){
                staffRightsDT.ajax.url = dashboard.siteurl + '/api/v1/dt/managers/assigned_courses/' + staff_id
                staffRightsDT.ajax.reload( function () {
                    dashboard.users.refreshTableResults();
                    $("#staffCourseSelectModal").modal('hide');
                    alertify.notify("Τα Δικαιώματα (Μαθημάτων) του Χρήστη ενημερώθηκαν με επιτυχία", "success");
                });
            },
            error: function ()  {
                $("#staffCourseSelectModal").modal('hide');
                alertify.alert('Error-Update-AssignStaffCourse-Error');
            }
        });
    }
    function postAssignRightToEventStaff(staff_id, eventPermission) {
        $.ajax({
            type:        "POST",
            url: 		  dashboard.siteurl + '/api/v1/managers/assign_event/' + staff_id,
            contentType: "application/json; charset=utf-8",
            data: 		  JSON.stringify(eventPermission),
            async:		  true,
            success: function(){
                staffEventRightsDT.ajax.url = dashboard.siteurl + '/api/v1/dt/managers/assigned_events/' + staff_id
                staffEventRightsDT.ajax.reload( function () {
                    dashboard.users.refreshTableResults();
                    $("#staffEventSelectModal").modal('hide');
                    alertify.notify("Τα Δικαιώματα (Εκδηλώσεων) του Χρήστη ενημερώθηκαν με επιτυχία", "success");
                });
            },
            error: function ()  {
                $("#staffEventSelectModal").modal('hide');
                alertify.alert('Error-Update-AssignStaffEvent-Error');
            }
        });
    }

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
                        text: '<i class="fas fa-plus-circle"></i> Προσθήκη Μονάδας', className: 'blue-btn-wcag-bgnd-color text-white my-2', attr: {id: 'openUnitsSelectModal'},
                        action: function () {
                            openUnitsSelectModal();
                        }
                    },
                ],
                dom: {
                    button: {
                        className: 'btn btn-sm openUnitsSelectModal'
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
                        return  '<button type="button" title="αφαίρεση δικαιώματος" class="btn remove_assigned_unit"><i class="fas fa-trash-alt" style="color:red;"></i> </button>';
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
    dashboard.users.getUnitRightsDTRowData = function(cell) {
        let rowIdx = unitRightsDT.cell(cell).index().row;
        return unitRightsDT.row( rowIdx ).data();
    }
    dashboard.users.assignedStaffCoursesDT = function () {

        let $staffRights = $("#staffRightsDataTable");
        let $staff_id = $("#staff_id");

        staffRightsDT = $staffRights.DataTable({
            "ajax": dashboard.siteurl + '/api/v1/dt/managers/assigned_courses/' + $staff_id.val(),
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
            dom: 'Brtip',
            buttons: {
                buttons: [
                    {
                        text: '<i class="fas fa-plus-circle"></i> Προσθήκη Μαθήματος', className: 'blue-btn-wcag-bgnd-color text-white my-2', attr: {id: 'openStaffCourseSelectModal'},
                        action: function () {
                            openStaffCourseSelectModal();
                        }
                    },
                ],
                dom: {
                    button: {
                        className: 'btn openStaffCourseSelectModal'
                    }
                }
            },
            "language": dtLanguageGr,
            order: [[2, 'asc']],
            "pageLength": 10,
            "aoColumnDefs": [
                {
                    "aTargets": [1,3],
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
                        return  '<button type="button" title="αφαίρεση δικαιώματος" class="btn _remove_right"><i class="fas fa-trash-alt" style="color:red;"></i> </button>';
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

        function openStaffCourseSelectModal() {

            let staff_name = $("#staff_name").val();
            dashboard.add_action = "course";
            $("#departments_s2").val("").trigger("change");
            $("#supervisor_s2").val("").trigger("change");
            $("#courses_s2").val("").trigger("change");
            $("#staffCourseSelectModalLabel").html('<div style="font-size: 1.4em">' + staff_name + '</div><small>ΠΑΡΑΧΩΡΗΣΗ ΔΙΚΑΙΩΜΑΤΟΣ: Καθηγητής+Μάθημα</small>');
            $("#staffCourseSelectModal").modal('show');
        }

        function postUnAssignStaffCourse(staffId, courseId, staffMemberId) {
            $.ajax({
                type:        "POST",
                url: 		  dashboard.siteurl + '/api/v1/managers/unassign_course/' + staffId + '/course/' + courseId + '/sm/' + staffMemberId,
                contentType: "application/json; charset=utf-8",
                async:		  true,
                success: function(){
                    staffRightsDT.ajax.url = dashboard.siteurl + '/api/v1/dt/managers/assigned_courses/' + staffId;
                    staffRightsDT.ajax.reload( function () {
                        dashboard.users.refreshTableResults();
                        alertify.notify("Το Μάθημα αφαιρέθηκε με επιτυχία", "success");
                    });
                },
                error: function ()  {
                    alertify.alert('Error-UnAssign-Course-Error');
                }
            });
        }

    }
    dashboard.users.getStaffCoursesDTRowData = function(cell) {
        let rowIdx = staffRightsDT.cell(cell).index().row;
        return staffRightsDT.row( rowIdx ).data();
    }
    dashboard.users.assignedStaffEventsDT = function () {

        let $staffEventRights = $("#staffEventRightsDataTable");
        let $staff_id = $("#staff_id");

        staffEventRightsDT = $staffEventRights.DataTable({
            "ajax": dashboard.siteurl + '/api/v1/dt/managers/assigned_events/' + $staff_id.val(),
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
            dom: 'Brtip',
            buttons: {
                buttons: [
                    {
                        text: '<i class="fas fa-plus-circle"></i> Προσθήκη Εκδήλωσης', className: 'blue-btn-wcag-bgnd-color text-white my-2', attr: {id: 'openStaffEventSelectModal'},
                        action: function () {
                            openStaffEventSelectModal();
                        }
                    },
                ],
                dom: {
                    button: {
                        className: 'btn openStaffEventSelectModal'
                    }
                }
            },
            "language":dtLanguageGr,
            order: [[4, 'asc']],
            "pageLength": 10,
            "aoColumnDefs": [
                {
                    "aTargets": [1,3],
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
                        return  '<button type="button" title="αφαίρεση δικαιώματος" class="btn _remove_right"><i class="fas fa-trash-alt" style="color:red;"></i> </button>';
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
    dashboard.users.getStaffEventDTRowData = function(cell) {
        let rowIdx = staffEventRightsDT.cell(cell).index().row;
        return staffEventRightsDT.row( rowIdx ).data();
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
            "language":  dashboard.dtLanguageGr,
            select: {
                style: 'single'
            },
            order: [[1, 'asc']],
            "pageLength": 10,
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
    dashboard.users.reloadAssignedUnitsTable = function(staffId, msg) {
        unitRightsDT.ajax.reload( function () {
            dashboard.users.refreshTableResults();
            if (msg != null) {
                alertify.notify(msg, "success");
            }
        });
    }
    dashboard.users.reloadAssignedStaffTable = function(staffId, msg) {
        staffRightsDT.ajax.reload( function () {
            dashboard.users.refreshTableResults();
            if (msg != null) {
                alertify.notify(msg, "success");
            }
        });
    }
    dashboard.users.reloadAssignedEventTable = function(staffId, msg) {
        staffEventRightsDT.ajax.reload( function () {
            dashboard.users.refreshTableResults();
            if (msg != null) {
                alertify.notify(msg, "success");
            }
        });
    }
    dashboard.users.refreshTableResults  = function() {
            $('._toggle').bootstrapToggle({
                onstyle: 'success',
                offstyle: 'danger',
                size: "small"
            });
    }
})();
