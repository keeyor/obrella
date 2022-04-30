(function () {
    'use strict';

    dashboard.sevents = dashboard.sevents || {};


    let StaffMembersDT      = null;
    let InstitutionUnitsDT  = null;
    let AssignedUnitsDT     = null;

    let serialize_form;
    let upload_base_path;
    let $event_photo_url;
    let event_id;

    dashboard.sevents.init = function () {
        event_id = $("#sevent_id").val();
        InitControls();
        SetupAssignUnitsDataTable();
        SetupUnitsSelectModal();
        RegisterEvents();
        setupEventEditPanel ();
    };

    function DisplayStaffMembersSelectModal(departmentId) {
        if (departmentId === "") { departmentId = "dummy";}
        let $institutionStaffMembersDtElem = $("#rPersonSelectDataTable");
        StaffMembersDT = $institutionStaffMembersDtElem.DataTable({
            "ajax": dashboard.siteurl + '/api/v1/dt/staff.web/department/' + departmentId,
            "columns": [
                {"data": "id"},
                {"data": "name"},
                {"data": "affiliation"},
                {"data": "department.title"}
            ],
            dom: "ftp",
            "language":  dtLanguageGr,
            select: {
                style: 'single'
            },
            order: [[1, 'asc']],
            "pageLength": 10,
            "aoColumnDefs": [
                {
                    "aTargets": [0,2],
                    "mData": "id",
                    "visible": false,
                }
            ]
        });
    }
    function SetupUnitsSelectModal() {

        let $institutionUnitsDtElem = $("#runitSelectDataTable");
        InstitutionUnitsDT = $institutionUnitsDtElem.DataTable({
            "ajax": dashboard.siteurl + '/api/v1/dt/units.web',
            "columns": [
                {"data": "id"},
                {"data": "structureType"},
                {"data": "title"},
            ],
            "language":  dtLanguageGr,
            select: {
                style: 'multi'
            },
            dom: "ftp",
            order: [[1, 'asc']],
            "pageLength": 10,
            "aoColumnDefs": [
                {
                    "aTargets": [0],
                    "mData": "id",
                    "visible": false
                },
                {
                    "aTargets": [1],
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
    }
    function SetupAssignUnitsDataTable() {

        let $assignUnitsDtElement = $("#runitsAssignedDataTable");
        AssignedUnitsDT = $assignUnitsDtElement.DataTable({
            "columns": [
                {"data": "id"},
                {"data": "structureType"},
                {"data": "title"},
            ],
            "language":  dtLanguageGr,
            dom: 'rtip',
            order: [[1, 'asc']],
            "pageLength": 10,
            "aoColumnDefs": [
                {
                    "aTargets": [0,2],
                    "mData": "id",
                    "visible": false
                },
                {
                    "aTargets": [1],
                    "name": "structureType",
                    "render": function (data,type, row) {
                        if (data != null) {
                            if (data === 'INSTITUTION') {
                                return row.title;
                            }
                            else if (data === 'SCHOOL') {
                                return 'Σχολή ' + row.title;
                            }
                            else if (data === 'DEPARTMENT') {
                                return 'Τμήμα ' + row.title;
                            }
                        }
                        else {
                            return "-";
                        }
                    }
                },
            ]
        });

    }
    function LoadAllUnitsModal() {
        //reset DT page
        InstitutionUnitsDT.row(0).show().draw(false);
        //set modal title
        let sevent_title = $("#sevent_title").val();
        if (sevent_title.trim() === "") {
            sevent_title = "[Τίτλος]"
        }
        $("#runitSelectModalLabel").html('<div>' + sevent_title + '</div><small>Επιλογή Διοργανωτών</small>');
        $("#runitSelectModal").modal('show');
        //get already responsible units (ids)
        let nodes = AssignedUnitsDT.rows().data();
        let assignedIds =[];
        for (let l = 0; l < nodes.length; l++) {
            assignedIds.push(nodes[l].id);
        }
        InstitutionUnitsDT.rows().deselect();
        //select already responsible units to InstitutionUnitsDT
        InstitutionUnitsDT.column(0, {order: 'applied'}).nodes().each(function (cell, i) {
            let unitId = InstitutionUnitsDT.cell(i, 0).data();
            if (assignedIds.includes(unitId)) {
                InstitutionUnitsDT.row(i).select();
            }
        });
    }
    function InitControls() {

        upload_base_path = $("#upload_base_path").val();
        $event_photo_url = $("#event_photo_url");
        $("#sevent_isactive_toggle").bootstrapToggle({
            on: '<i class="fas fa-power-off"></i> Ενεργό',
            off: '<i class="fas fa-ban"></i> Ανενεργό',
            onstyle: "success",
            offstyle: "danger",
            size: "small"
        });
        $("#sevent_isfeatured_toggle").bootstrapToggle({
            on: '<i class="fas fa-power-off"></i> Ενεργό',
            off: '<i class="fas fa-ban"></i> Ανενεργό',
            onstyle: "success",
            offstyle: "danger",
            size: "small"
        });
        var d = new Date();
        var today = d.getDate()  + "/" + (d.getMonth()+1) + "/" + d.getFullYear();
        $("#daterange").daterangepicker({
            "showDropdowns": true,
            "locale": {
                "format": "DD/MM/YYYY",
                "separator": " - ",
                "applyLabel": "Εφαρμογή",
                "cancelLabel": "Ακύρωση",
                "fromLabel": "Από",
                "toLabel": "Έως",
                "customRangeLabel": "Custom",
                "weekLabel": "W",
                "daysOfWeek": [
                    "Κυ",
                    "Δε",
                    "Τρ",
                    "Τε",
                    "Πέ",
                    "Πα",
                    "Σά"
                ],
                "monthNames": [
                    "Ιανουάριος",
                    "Φεβρουάριος",
                    "Μάρτιος",
                    "Απρίλιος",
                    "Μάιος",
                    "Ιούνιος",
                    "Ιούλιος",
                    "Αύγουστος",
                    "Σεπτέμβριος",
                    "Οκτώβριος",
                    "Νοέμβριος",
                    "Δεκέμβριος"
                ],
                "firstDay": 1
            }
        }, function(start, end, label) {
            console.log('New date range selected: ' + start.format('DD-MM-YYYY') + ' to ' + end.format('DD-MM-YYYY') + ' (predefined range: ' + label + ')');
            let startDate =  moment(start.format('YYYY-MM-DD')).format('YYYY-MM-DDTHH:mm:ss[Z]');
            let endDate =   moment(end.format('YYYY-MM-DD')).format('YYYY-MM-DDTHH:mm:ss[Z]');
            $("#startDate").val(startDate);
            $("#endDate").val(endDate);
        });
        $("#sevent_type").select2({
            placeholder: "Επιλέξτε Τύπο (προηγείται η επιλογή κατηγορίας)",
            minimumResultsForSearch: -1
        });

        $("#rperson_assign_department").select2({
            placeholder: "Επιλέξτε Τμήμα"
        });
        $("#sevent_area").select2({
            placeholder: "Επιλέξτε Κατηγορία"
        });
        //Categories
        $(".js-category-tags").select2({
            placeholder: "Επιλέξτε Θεματικές Ενότητες",
            maximumSelectionLength: 3
        });
    }

    function RegisterEvents() {

        $("#deleteSevent").on("click", function (e) {

            e.preventDefault();
            let $event_form = $("#sevent_form");
            let msg = '<div>Προσοχή: Αν έχουν δημιουργηθεί Πoλυμέσα Εκδηλώσεων για την Προγραμματισμένη Εκδήλωση ή υπάρχουν Προγραμματισμένες Μεταδόσεις, η διαγραφή θα ακυρωθεί'

            alertify.confirm('Διαγραφή Εκδήλωσης', msg,
                function () {
                    $event_form.attr("action", "sevent-editor?action=delete");
                    $event_form.submit();
                },
                function () {
                }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
            e.preventDefault();
        });

        $("#sevent_area").on('select2:select', function (e) {
            let data = e.params.data;
            let area = data.id;
            let $element = $("#sevent_type");
            dashboard.sevents.getAndSetEventTypesByArea(area,$element,"");
        });

        $("#deletePhoto").on('click',function(e){
            let photo_url = $("#event_photo_rurl").val();
            if (photo_url !== "") {
                let msg = '<div class="font-weight-bold">Η Φωτογραφία θα διαγραφεί! Είστε σίγουρος?</div>';
                alertify.confirm('Προειδοποίηση', msg,
                    function () {
/*                        $("#event_photo_rurl").val("");
                        $event_photo_url.attr("src", "");
                        $("#_image_panel").hide();
                        $("#default_photo").show();
                        document.getElementById('file_select_label').innerHTML = "Η διαγραφή της φωτογραφίας, ολοκληρώθηκε";
                        document.getElementById('filelist').innerHTML = '';*/
                        let $event_form = $("#sevent_form");
                        $event_form.attr("action", "sevent-editor?action=deletePhoto");
                        $event_form.submit();
                    },
                    function () {
                        e.preventDefault();
                    }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
            }
        })
        $("#enlarge_image_link").on('click',function(e){
            let image_tag = $(this).children('img')[0];
            let image_src = image_tag.src;
            $("#modal_image_url").attr("src",image_src);
            let sevent_title = $("#sevent_title").val();
            if (sevent_title.trim() === "") {
                sevent_title = "[Τίτλος]"
            }
            $("#showImageModalLabel").html('<div>' + sevent_title + '</div><small>Φωτογραφία | Εικόνα Εκδήλωσης</small>');
            $("#showImageModal").modal('show');
            e.preventDefault();
        });
        $("#closeUpdateSevent").on('click',function(e){
            e.preventDefault();
            let end_serialize = $("#sevent_form").serialize();
            if (serialize_form !== end_serialize) {
                let msg = '<div class="font-weight-bold">Οι αλλαγές θα χαθούν! Είστε σίγουρος?</div>';
                alertify.confirm('Προειδοποίηση', msg,
                    function () {
                        window.location.href = e.currentTarget.href;
                    },
                    function () {
                        e.preventDefault();
                    }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
            }
            else {
                window.location.href = e.currentTarget.href;
            }
        });

         $(".selectRunitBt").on('click',function(e){
            LoadAllUnitsModal();
            e.preventDefault();
        })
        $(".selectRpersonBt").on("click",function(e) {
            loadInstitutionStaffMembersModal("");
            e.preventDefault();
        });
        $("#rperson_assign_department").on('select2:select', function (e) {
            $("#default_dp_id").val(e.params.data.id);
            //loadInstitutionStaffMembersModal(e.params.data.id);
            // initialize datatable if not initialized yet! else reload ajax
            if ( ! $.fn.DataTable.isDataTable( '#rPersonSelectDataTable' ) ) {
                DisplayStaffMembersSelectModal(e.params.data.id);
            }
            else {
                let ajax_url = dashboard.siteurl + '/api/v1/dt/staff.web/department/' + e.params.data.id
                StaffMembersDT.ajax.url(ajax_url);
                StaffMembersDT.ajax.reload();
            }
        });
        $("#addAssignedPerson").on('click',function(){
            let node = StaffMembersDT.rows( { selected: true } ).data();
            if (node.length>0) {
               /* let media_body = "<h6 class='mt-1'>" + node[0].name + " / " + "Τμήμα " + node[0].department.title + "</h6>"
                $("#sevent_rperson_text").html(media_body);*/
                let media_body = node[0].name + " / " + "Τμήμα " + node[0].department.title
                $("#sevent_rperson_text").val(media_body);
                $("#sevent_rpersonId").val(node[0].id);
                $("#rPersonSelectModal").modal('hide');
            }
        });
        $(".setMySelfBt").on('click',function(e) {
            setMySelfAsRP();
            e.preventDefault();
        });
        $("#addAssignedUnits").on('click',function(){
            let assigned_units_ids = [];
            let assigned_units_types = [];
            AssignedUnitsDT.clear().draw();
            //Update AssignedUnitsDT with user selections
            let nodes= InstitutionUnitsDT.rows( { selected: true } ).data();
            let html_units = '';
            if (nodes.length>0) {
                for (let l = 0; l < nodes.length; l++) {
                    let new_row = nodes[l];
                    AssignedUnitsDT.row.add( new_row ).draw();
                    let unit_line = "";
                    if (new_row.structureType === "SCHOOL") { unit_line += "Σχολή "}
                    else if (new_row.structureType === "DEPARTMENT") { unit_line += "Τμήμα "}
                    html_units += "<div class=\"bg-light border px-2\">" + unit_line + new_row.title + "</div>";
                    assigned_units_ids.push(new_row.id);
                    assigned_units_types.push(new_row.structureType);
                }
            }
            else {
                html_units += "<div class=\"h6 text-muted\">κάντε κλικ εδώ για να επιλέξτε διοργανωτές</div>"
            }
            $("#sevent_unitsText").html(html_units);
            $("#sevent_runits_ids").val(assigned_units_ids);
            $("#sevent_runits_types").val(assigned_units_types);
            $("#runitSelectModal").modal('hide');
        });
    }

    function loadInstitutionStaffMembersModal(departmentId) {
        let $rperson_assign_department = $("#rperson_assign_department");
        //department filter on Institution's StaffMembers Modal
        dashboard.departments.initializeDepartmentsList(departmentId, $rperson_assign_department);

        //departmentId = $("#default_dp_id").val();
        if (departmentId === "") { departmentId = "dummy";}
        // initialize datatable if not initialized yet! else reload ajax
        if ( ! $.fn.DataTable.isDataTable( '#rPersonSelectDataTable' ) ) {
            DisplayStaffMembersSelectModal(departmentId);
        }
        else {
            let ajax_url = dashboard.siteurl + '/api/v1/dt/staff.web/department/' + departmentId
            StaffMembersDT.ajax.url(ajax_url);
            StaffMembersDT.ajax.reload();
        }
        let sevent_title = $("#sevent_title").val();
        if (sevent_title.trim() === "") {
            sevent_title = "[Τίτλος]"
        }
        $("#rPersonSelectModalLabel").html('<div>' + sevent_title + '</div><small>Επιλογή Επιστημονικού Υπεύθυνου</small>');
        $("#rPersonSelectModal").modal('show');
    }

    function setMySelfAsRP() {
        let editor_id = $("#user_id").val();
        $.getJSON(dashboard.siteurl + '/api/v1/user/' + editor_id, function(data) {
            /*let media_body = "<h6 class='mt-1'>" + data.name + " / " + "Τμήμα " + data.department.title + "</h6>"
            $("#sevent_rperson_text").html(media_body);*/
            let media_body = data.name + " / " + "Τμήμα " + data.department.title;
            $("#sevent_rperson_text").val(media_body);
            $("#sevent_rpersonId").val(data.id);
        });
    }
    function setupEventEditPanel () {
        AssignedUnitsDT.clear().draw();
        $("#newSeventBt").hide();
        $("#file_select_label").html("");
        $("#filelist").html("");

        let dateModified    = $("#dateModified").val();
        let editor_name     = $("#editor_name").val();
        let event_id        = $("#sevent_id").val();
        let event_title     = $("#sevent_title").val();

        if (event_title === "") {
            event_title = "[Τίτλος]";
        }
        $("#seventModalLabel").html(event_title);
        //type
        let type = $("#sevent_type").val();
        if (type === null || type === "") {
            $("#sevent_type").val("other").trigger("change");
        }

        if (event_id !== "") {
            setPhotoUploadState("enable");
        }
        else {
            setPhotoUploadState("disable");
        }
        //Dates
        let startDate = $("#startDate").val();
        let $daterange = $("#daterange").data('daterangepicker');
        if (startDate == null || startDate === "") {
            let today = moment().format('YYYY-MM-DDTHH:mm:ss[Z]');
            $("#startDate").val(today);
            $("#endDate").val(today);
            let formatted_today = moment(today).format('DD-MM-YYYY');
            $daterange.setStartDate(formatted_today);
            $daterange.setEndDate(formatted_today);
        }
        else {
            //Start Date
            let endDate = $("#endDate").val();
            if (startDate != null && (endDate == null || endDate === "")) {
                let formatted_date = moment(startDate).format('DD-MM-YYYY');
                $daterange.setStartDate(formatted_date);
                $daterange.setEndDate(formatted_date);
            }
            else {
                let formatted_startDate = moment(startDate).format('DD-MM-YYYY');
                let formatted_endDate = moment(endDate).format('DD-MM-YYYY');

                $daterange.setStartDate(formatted_startDate);
                $daterange.setEndDate(formatted_endDate);
            }
        }
        if (dateModified !== null && dateModified !== "") {
                let formatted_date = moment(dateModified).format('LLL');
                $("#s_event_info").html("Τελευταία Ενημέρωση: " + formatted_date + " από '" + editor_name + "'");
        }
        else {
            $("#s_event_info").html("Επεξεργασία: " + $("#user_name").val());
        }

        //Set AssignedUnitsDT from Database (get values from ScheduledEventsDT - row)
         let html_units = "";
         let responsible_units_size = parseInt($("#sevent_runits_size").val());
         if (responsible_units_size > 0) {
                for (var i = 0, l = responsible_units_size; i < l; i++) {
                    let unit_id = $("#unitId__" + i).val();
                    let unit_st = $("#unitSt__" + i).val();
                    let unit_ti = $("#unitTi__" + i).val();
                    let new_row = {
                        "id" : unit_id,
                        "structureType" : unit_st,
                        "title" : unit_ti
                    }
                    AssignedUnitsDT.row.add( new_row ).draw();
                    let unit_line = "";
                    if (new_row.structureType === "SCHOOL") { unit_line += "Σχολή "}
                    else if (new_row.structureType === "DEPARTMENT") { unit_line += "Τμήμα "}
                    html_units += "<div class=\"bg-light border px-2\">" + unit_line + new_row.title + "</div>";
                }
         }
         else {
             html_units += "<div class=\"h6 text-muted\"> -- κάντε κλικ εδώ για να επιλέξτε διοργανωτές -- </div>"
         }
         $("#sevent_unitsText").html(html_units);

         //Set Responsible Person from Database (get values from ScheduledEventsDT - row)
        let rPerson_id = $("#sevent_rpersonId").val();
        let media_body = "";
        if (rPerson_id !== "") {
            let rPersonName = $("#sevent_rPersonName").val();
            let rPersonDepartmentName = $("#sevent_rpersonDepartmentName").val();
            //media_body += "<h6 class='mt-1'>" + rPersonName + " / " + "Τμήμα " + rPersonDepartmentName + "</h6>"
            media_body += rPersonName + " / " + "Τμήμα " + rPersonDepartmentName;
        }
        else {
          // media_body +=  "<div class=\"h6 text-muted\">  -- επιλέξτε επιστημονικό υπεύθυνο -- </div>";
            media_body +=  "-- κάντε κλικ εδώ για να επιλέξτε επιστημονικό υπεύθυνο -- ";
        }
       // $("#sevent_rperson_text").html(media_body);
        $("#sevent_rperson_text").val(media_body);
        // PHOTO HANDLE
        if (event_id !== "") {
            handlePhotoPanel(event_id);
            $("#deleteSevent").attr("disabled",false);
        }
        else {
            $event_photo_url.attr("src", "");
            $("#_image_panel").hide();``
            $("#default_photo").show();
            $("#deleteSevent").attr("disabled",true);
        }

        let area = $("#sevent_area").val();
        if (area !== "ea_uas") {
            $("#sevent_cat").select2({placeholder: "- δεν εφαρμόζεται -"});
            $("#sevent_cat").attr("disabled", true);
        }
        else {
            $("#sevent_cat").select2({ placeholder: "Επιλέξτε Θεματικές Περιοχές" });
            $("#sevent_cat").attr("disabled",false);
        }
        let event_type = $("#event_type").val();
        let $s2_element = $("#sevent_type");


        if (area !== null && area !== "") { //for all scheduledEvents that have no area
            dashboard.sevents.getAndSetEventTypesByArea(area, $s2_element, event_type);
        }

    }
    function setPhotoUploadState(state) {
        if (state === "enable") {
            $("#container").show();
            $("#photoUploadHelp_1").show();
            $("#photoUploadHelp_2").hide();
        }
        else {
            $("#container").hide();
            $("#photoUploadHelp_1").hide();
            $("#photoUploadHelp_2").show()
        }
    }
    function handlePhotoPanel(event_id) {
        let image_relative_url = $("#event_photo_rurl").val();
        let image_url = dashboard.hosturl + upload_base_path + event_id + "/" + image_relative_url;
        if (image_relative_url === "") {
            $event_photo_url.attr("src", "");
            $("#_image_panel").hide();
            $("#default_photo").show();
        }
        else {
            var image = new Image();
            image.src = image_url;
            image.onload = function () {
                var d = new Date();             //add date to avoid image cache
                $event_photo_url.attr("src", image_url + "?ver=" + d.getTime());
                $("#_image_panel").show();
                $("#default_photo").hide();
            }
            image.onerror = function () {
                $event_photo_url.attr("src", "");
                $("#_image_panel").hide();
                $("#default_photo").show();
            }
        }
        dashboard.upload.setEventId(event_id);
    }

    dashboard.sevents.getAndSetEventTypesByArea = function(area, $sel2_element, selected_value) {

        $sel2_element.empty();

        $.ajax({
            url:  dashboard.siteurl + '/api/v1/s2/event_types/' + area,
            cache: false
        })
            .done(function( data ) {
                $sel2_element.select2({
                    placeholder: 'Επιλέξτε Τύπο',
                    width: 'style', // need to override the changed default
                    data : data.results
                });
                if (selected_value != null && selected_value !== "") {
                    $sel2_element.val(selected_value).trigger("change");
                }
                else {
                    $sel2_element.val("").trigger("change");
                }
                if (area === "ea_uas") {
                    $("#sevent_cat").select2({ placeholder: "Επιλέξτε Θεματικές Περιοχές" });
                    $("#sevent_cat").attr("disabled",false);
                }
                else {
                    $("#sevent_cat").val("").trigger("change");
                    $("#sevent_cat").select2({ placeholder: "- δεν εφαρμόζεται -" });
                    $("#sevent_cat").attr("disabled",true);
                }

                //Serialize form to check for changes on submit
                serialize_form = $("#sevent_form").serialize();
            });
    }

})();