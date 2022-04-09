(function () {
    'use strict';

    window.dashboard = window.dashboard || {};

    dashboard.broker = $({});

    dashboard.siteurl = "";
    dashboard.study_code = "all";
    dashboard.add_action = "";
    dashboard.institutionName;
    dashboard.institutionId;

    $(document).ready(function () {
        dashboard.init();
    });

    dashboard.init = function () {

        dashboard.siteurl = dashboard.broker.getRootSitePath();

        dashboard.institutionName = $("#institutionTitle").val();
        dashboard.institutionId = $("#institutionIdentity").val();

        alertify.defaults.transition = "slide";
        alertify.defaults.theme.ok = "btn btn-primary";
        alertify.defaults.theme.cancel = "btn btn-danger";
        alertify.defaults.theme.input = "form-control";
        alertify.set('notifier','position', 'top-center');

        // Do this before you initialize any of your modals
        $.fn.modal.Constructor.prototype._enforceFocus = function() {};

        let   msg_val   = $("#msg_val").val();
        let   msg_type  =$("#msg_type").val();

        if (msg_val !== '') {
            let message = {msg: "instant message", type: msg_type, val: msg_val};
            dashboard.broker.showInstantMessage(message.type ,message.val);
        }

        dashboard.users.init();
        let staff_department_id = $("#staff_department_id").val()
        dashboard.departments.init(staff_department_id);
        dashboard.courses.init();
        dashboard.staffmembers.init();
        dashboard.events.init();

        dashboard.broker.on("afterSelect.department afterInit.department", function (event, msg) {

            let sel_department_id = msg.value;
            $(".js-staffMembers-tags").empty();
            $("#courses_s2").empty();
            $("#events_s2").empty();
            dashboard.staffmembers.getStaffMembersOfDepartmentId(sel_department_id);
        });

        dashboard.broker.on("afterSelect.staffmember afterInit.staffmember", function (event, msg) {
            let sel_staff_id = msg.value;
            if (sel_staff_id != null && sel_staff_id !== "") {
                if (dashboard.add_action === "event") {
                    $("#events_s2").empty();
                    dashboard.events.getEventsByResponsiblePerson(sel_staff_id);
                }
                else if (dashboard.add_action === "course") {
                    $("#courses_s2").empty();
                    dashboard.courses.s2FillCoursesByTeachingStaff(sel_staff_id);
                }
            }
        });

        setEvents();
    };

    function setEvents() {
        //* UNIT UPDATE EVENTS - LEAVE IT IN dashboard.js to prevent double posts - possible bug of boostrap-toggle
        let $body = $('#unitRightsDataTable');
        $body.on('change', '.unit_content_toggle', function() {
            let el = $(this);
            let table_cell = el.closest('td');
            let contentManager = el.prop('checked');
            let row_data = dashboard.users.getUnitRightsDTRowData(table_cell);
            let dataManager = row_data.dataManager;
            let scheduleManager = row_data.scheduleManager;
            if (contentManager === false && dataManager === false && scheduleManager === false) {
                alertify.alert("Δεν μπορείτε να αφαιρέσετε όλα δικαιώματα. Εναλλακτικά διαγράψτε το δικαίωμα");
                el.bootstrapToggle('on');
            }
            else  {
                setUnitPermission(el,row_data,"content");
            }
        });

        $body.on('change', '.unit_data_toggle', function() {
            let el = $(this);
            let table_cell = el.closest('td');
            let dataManager = el.prop('checked');
            let row_data = dashboard.users.getUnitRightsDTRowData(table_cell);
            let contentManager = row_data.contentManager;
            let scheduleManager = row_data.scheduleManager;
            if (contentManager === false && dataManager === false && scheduleManager === false) {
                alertify.alert("Δεν μπορείτε να αφαιρέσετε όλα δικαιώματα. Εναλλακτικά διαγράψτε το δικαίωμα");
                el.bootstrapToggle('on');
            }
            else  {
                setUnitPermission(el,row_data,"data");
            }
        });
        $body.on('change', '.unit_schedule_toggle', function() {
            let el = $(this);
            let table_cell = el.closest('td');
            let scheduleManager = el.prop('checked');
            let row_data = dashboard.users.getUnitRightsDTRowData(table_cell);
            let contentManager = row_data.contentManager;
            let dataManager = row_data.dataManager;
            if (contentManager === false && dataManager === false && scheduleManager === false) {
                alertify.alert("Δεν μπορείτε να αφαιρέσετε όλα δικαιώματα. Εναλλακτικά διαγράψτε το δικαίωμα");
                el.bootstrapToggle('on');
            }
            else  {
                setUnitPermission(el,row_data,"schedule");
            }
        });



        //COURSE RIGHTS UPDATE

        let $staffRightsDataTable =  $('#staffRightsDataTable');
        $staffRightsDataTable.on('change', '.course_content_toggle', function(e) {
            let el = $(this);
            let table_cell = el.closest('td');
            let contentManager = el.prop('checked');
            let row_data = dashboard.users.getStaffCoursesDTRowData(table_cell);
            let scheduleManager = row_data.scheduleManager;
            if (contentManager === false && scheduleManager === false) {
                alertify.alert("Δεν μπορείτε να αφαιρέσετε και τα δύο δικαιώματα. Εναλλακτικά διαγράψτε το δικαίωμα");
                el.bootstrapToggle('on');
            }
            else  {
                setCoursePermission(el, row_data, "content");
            }
        });
        $staffRightsDataTable.on('change', '.course_schedule_toggle', function(e) {
            let el = $(this);
            let table_cell = el.closest('td');
            let row_data = dashboard.users.getStaffCoursesDTRowData(table_cell);
            let contentManager = row_data.contentManager;
            let scheduleManager = el.prop('checked');
            if (contentManager === false && scheduleManager === false) {
                alertify.alert("Δεν μπορείτε να αφαιρέσετε και τα δύο δικαιώματα. Εναλλακτικά διαγράψτε το δικαίωμα");
                el.bootstrapToggle('on');
            }
            else  {
                setCoursePermission(el, row_data, "schedule");
            }
        });


        //COURSE RIGHTS UPDATE
        let $staffEventRightsDataTable =  $('#staffEventRightsDataTable');
        $staffEventRightsDataTable.on('change', '.event_content_toggle', function() {
            let el = $(this);
            let table_cell = el.closest('td');
            setEventPermission(el,table_cell,"content");
        });
        $staffEventRightsDataTable.on('change', '.event_schedule_toggle', function() {
            let el = $(this);
            let table_cell = el.closest('td');
            setEventPermission(el,table_cell,"schedule");
        });
    }
    function setUnitPermission(el,row_data, type) {
        let unit_id = row_data.unitId;
        let unit_type = row_data.unitType;
        let contentManager;
        let dataManager;
        let scheduleManager;
        if (type === "content") {
            contentManager = el.prop('checked');
            dataManager = row_data.dataManager;
            scheduleManager = row_data.scheduleManager;
        }
        else if (type === "data") {
            contentManager = row_data.contentManager;
            dataManager = el.prop('checked');
            scheduleManager = row_data.scheduleManager;
        }
        else {
            contentManager = row_data.contentManager;
            dataManager = row_data.dataManager;
            scheduleManager = el.prop('checked');
        }
        let staff_id = $("#staff_id").val();
        let unitPermission = {
            "unitId": unit_id,
            "unitType": unit_type,
            "contentManager": contentManager,
            "dataManager": dataManager,
            "scheduleManager":scheduleManager
        }
        //Check just in case to prevent data corruption
        if (unit_id !== undefined && unit_type !== undefined && contentManager !== undefined && dataManager !==undefined && scheduleManager !== undefined) {
            postUpdateStaffUnit(staff_id, unitPermission);
        }
    }
    function postUpdateStaffUnit(staff_id, unitPermission) {
        console.log("post unit update ajax");
        $.ajax({
            type:        "POST",
            url: 		  dashboard.siteurl + '/api/v1/managers/assign_unit_update/' + staff_id,
            contentType: "application/json; charset=utf-8",
            data: 		  JSON.stringify(unitPermission),
            async:		  true,
            success: function(){
                dashboard.users.reloadAssignedUnitsTable(staff_id,"Τα δικαιώματα μονάδων ενημερώθηκαν...");
            },
            error: function ()  {
                alertify.alert('Error-Update-AssignStaffUnit-Error');
            }
        });
    }

    function setEventPermission(el,table_cell, type) {
        let row_data = dashboard.users.getStaffEventDTRowData(table_cell);
        let event_id = row_data.eventId;
        let staffMemberId = row_data.staffMemberId
        let contentManager;
        let scheduleManager;
        if (type === "content") {
            contentManager = el.prop('checked');
            scheduleManager = row_data.scheduleManager;
        }
        else if (type === "schedule") {
            contentManager = row_data.contentManager;
            scheduleManager = el.prop('checked');
        }
        let staff_id = $("#staff_id").val();
        let eventPermission = {
            "staffMemberId": staffMemberId,
            "eventId": event_id,
            "contentManager": contentManager,
            "scheduleManager":scheduleManager
        }
        //Check just in case to prevent data corruption
        if (staffMemberId !== undefined && event_id !== undefined && contentManager !== undefined && scheduleManager !== undefined) {
            postUpdateAssignRightToEventStaff(staff_id, eventPermission);
        }

        function postUpdateAssignRightToEventStaff(staff_id, eventPermission) {
            $.ajax({
                type:        "POST",
                url: 		  dashboard.siteurl + '/api/v1/managers/assign_event_update/' + staff_id,
                contentType: "application/json; charset=utf-8",
                data: 		  JSON.stringify(eventPermission),
                async:		  true,
                success: function(){
                    dashboard.users.reloadAssignedEventTable(staff_id,"Τα δικαιώματα εκδηλώσεων ενημερώθηκαν...")
                },
                error: function ()  {
                    alertify.alert('Error-Update-AssignStaffEvent-Error');
                }
            });
        }
    }
    function setCoursePermission(el,row_data, type) {
        let course_id = row_data.courseId;
        let staffMemberId = row_data.staffMemberId
        let contentManager;
        let scheduleManager;
        if (type === "content") {
            contentManager = el.prop('checked');
            scheduleManager = row_data.scheduleManager;
        }
        else if (type === "schedule") {
            contentManager = row_data.contentManager;
            scheduleManager = el.prop('checked');
        }

        let staff_id = $("#staff_id").val();
        let coursePermission = {
            "staffMemberId": staffMemberId,
            "courseId": course_id,
            "contentManager": contentManager,
            "scheduleManager": scheduleManager
        }
        //Check just in case to prevent data corruption
        if (staffMemberId !== undefined && course_id !== undefined && contentManager !== undefined && scheduleManager !== undefined) {
            postUpdateStaffCourse(staff_id, coursePermission);
        }

        function postUpdateStaffCourse(staff_id, coursePermission) {
            $.ajax({
                type: "POST",
                url: dashboard.siteurl + '/api/v1/managers/assign_course_update/' + staff_id,
                contentType: "application/json; charset=utf-8",
                data: JSON.stringify(coursePermission),
                async: true,
                success: function () {
                    dashboard.users.reloadAssignedStaffTable(staff_id, "Τα δικαιώματα μαθημάτων ενημερώθηκαν...");
                },
                error: function () {
                    alertify.alert('Error-Update-AssignStaffCourse-Error');
                }
            });
        }

    }

    dashboard.broker.getRootSitePath = function () {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let applicationName = _location.substring(0, applicationNameIndex) + '/';
        let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

        return _location.substring(0, webFolderIndex);
    };
    dashboard.broker.showInstantMessage = function(type, val) {

        //Override alertify defaults
        alertify.set('notifier','position', 'top-center');

        switch (type) {
            case "alert-success":
                alertify.success(val);
                break;
            case "alert-danger":
                alertify.error(val);
                break;
            case "alert-warning":
                alertify.warning(val);
                break;
            case "alert-info":
                alertify.info(val);
                break;
        }
    };
})();