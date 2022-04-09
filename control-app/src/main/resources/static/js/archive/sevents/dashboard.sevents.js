(function () {
    'use strict';

    dashboard.sevents = dashboard.sevents || {};

    let  ScheduledEventsDT = null;
    let  StaffMembersDT = null;
    let  InstitutionUnitsDT = null;
    let  AssignedUnitsDT = null;
    let  serialize_form;

    dashboard.sevents.init = function () {

        InitControls();

        let url = dashboard.siteurl + '/api/v1/dt/sevents.web';
        let $scheduledEventsDtElem = $("#seventsDataTable");
        ScheduledEventsDT = $scheduledEventsDtElem.DataTable({
            "ajax":  url,
            "columns": [
                {"data": "id"}, //0
                {"data": "photo"}, //1
                {"data": "startDate"}, //2
                {"data": "endDate"}, //3
                {"data": "id"}, //4
                {"data": "title"}, //5
                {"data": null},
                {"data": "type"},
                {"data": "responsibleUnit"}, //8
                {"data": "responsiblePerson"},
                {"data": "place"},
                {"data": "dateModified"},//11
                {"data": "id"}, //12
                {"data": "editor"}, //13
            ],
            "language":  dtLanguageGr,
            order : [[2, 'desc']],
            "pagingType": "full_numbers",
            "pageLength": 50,
            select: {
                style:    'multi',
                selector: 'td:first-child'
            },
            "aoColumnDefs": [
                {
                    "aTargets": [0,5,6,7,9,10,11,12],
                    "visible": false,
                },
                {
                    "aTargets": [0],
                    "sortable": false,
                    "mRender": function (data) {
                        return '<input type="checkbox" name="id[]" value="' + data + '">'
                    }
                },
                {
                    "aTargets": [1],
                    "mData": "photo", //replace with photo
                    "className": "border_left",
                    "mRender": function (data) {
                        if (data == null) {
                            let html = '<svg class="bd-placeholder-img mr-3" width="100" height="80" ' +
                                'xmlns="http://www.w3.org/2000/svg" aria-label="Placeholder: 100x80" ' +
                                'preserveAspectRatio="xMidYMid slice" role="img"><title>Placeholder</title> ' +
                                '<rect width="100%" height="100%" fill="#868e96"/> ' +
                                '<text x="30%" y="50%" fill="#dee2e6" dy=".3em">100x80</text> ' +
                                '</svg>';
                            return html;
                        }
                    }
                },
                {
                    "aTargets": [2],
                    "className": "border_left",
                    "mData": "startDate",
                    "mRender": function (data, type, row) {
                        let ret = "";

                        if (data !== undefined && data != null ) {
                            let epochDate = data;
                            let formatted_date = moment.unix(epochDate).format('YYYY-MM-DD');
                            let view_date = moment.unix(epochDate).format('LL');
                            ret += '<span style="display:none;' + '">' + formatted_date + '</span>' + view_date;
                        }
                        else {
                            let formatted_date = moment('1900-01-01').format('YYYY-MM-DD');
                            ret+= '<span style="display:none;' + '">' + formatted_date + '</span>' + 'μή έγκυρη ημερ.';
                        }
                        return ret;
                    }
                },
                {
                    "aTargets": [3],
                    "className": "border_left",
                    "mData": "endDate",
                    "mRender": function (data, type, row) {
                        let ret = "";
                        if (data !== undefined && data !== null ) {
                            if (data === row.startDate) {
                                return '';
                            }
                            let epochDate = data;
                            let formatted_date = moment.unix(epochDate).format('YYYY-MM-DD');
                            let view_date = moment.unix(epochDate).format('LL');
                            ret += '<span style="display:none;' + '">' + formatted_date + '</span>' + view_date;
                        }
                        else {
                            let formatted_date = moment('1900-01-01').format('YYYY-MM-DD');
                            ret+= '<span style="display:none;' + '">' + formatted_date + '</span>' + '';
                        }
                        return ret;
                    }
                },
                {
                    "aTargets": [8],
                    "className": "border_left",
                    "mData": "responsibleUnit",
                    "mRender": function (data) {
                        let ret = "";
                        if (data !== null) {
                            let runits = "<div class='col-12 text-muted'>";
                            data.forEach(function(runit,index) {
                                if (index > 0) {
                                    runits += ", ";
                                }
                                if (runit.structureType === "DEPARTMENT") {
                                    runits += "Τμήμα " + runit.title;
                                }
                                else if (runit.structureType === "SCHOOL") {
                                    runits += "Σχολή " + runit.title;
                                }
                                else {
                                    runits += runit.title;
                                }
                            });
                            runits += '</div>';
                            ret += runits;
                        }
                        return ret;
                    }
                },
                {
                    "aTargets": [4],
                    "className": "border_left",
                    "mRender": function (data, type, row, meta) {
                        let ret = "<div class=\"row my-0\">";
                        ret +=  '<div class="col-12">' +
                                     '<span style="font-weight: bolder"></span>' +
                                     '<strong><span style="color:#006A9B">' + (meta.row + 1) + '. ' + row.title + '</span></strong>' +
                                '</div>';

                        if (row.responsiblePerson !== null) {
                            let rPers = "<div class='col-12 text-muted'>";
                                rPers +=  row.responsiblePerson.name + ", " + row.responsiblePerson.affiliation;
                                rPers += "</div>";
                            ret += rPers;
                        }
                        ret += "</div>";
                        return ret;
                    }
                },
                {
                    "aTargets": [13],
                    "mData": "editor",
                    "className": "border_left",
                    "sortable": false,
                    "render": function (data) {
                        return "<span class='fas fa-play'></span>";
                    }
                },

            ],
            "initComplete": set_display_results,
        });
        function  set_display_results() {
            $("#count_results").html("" + ScheduledEventsDT.rows().count() + "");
        }
/*        ScheduledEventsDT.on( 'order.dt search.dt', function () {
            ScheduledEventsDT.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
                cell.innerHTML = i+1;
            } );
        } ).draw();*/

        $('#table-select-all').on('click', function(){
            // Get all rows with search applied
            var rows = ScheduledEventsDT.rows({ 'search': 'applied' }).nodes();
            // Check/uncheck checkboxes for all rows in the table
            $('input[type="checkbox"]', rows).prop('checked', this.checked);
        });

        // Handle click on checkbox to set state of "Select all" control
        $('#seventsDataTable tbody').on('change', 'input[type="checkbox"]', function(){
            // If checkbox is not checked
            if(!this.checked){
                var el = $('#table-select-all').get(0);
                // If "Select all" control is checked and has 'indeterminate' property
                if(el && el.checked && ('indeterminate' in el)){
                    // Set visual state of "Select all" control
                    // as 'indeterminate'
                    el.indeterminate = true;
                }
            }
        });

        RegisterEvents();

        function InitControls() {
            $("#sevent_isactive_toggle").bootstrapToggle({
                on: '<i class="fas fa-power-off"></i>',
                off: '<i class="fas fa-ban"></i>',
                onstyle: "success",
                offstyle: "danger",
                size: "small"
            });
            //date time
            $("#sevent_startDate").flatpickr({
                locale: "gr",
                dateFormat: 'Z',
                altInput: true,
                altFormat: 'Y-m-d',
                defaultDate: new Date()
            });
            $(".sevent_endDate").flatpickr({
                locale: "gr",
                dateFormat: 'Z',
                altInput: true,
                altFormat: 'Y-m-d',
                defaultDate: new Date()
            });
            $("#sevent_type").select2({
                minimumResultsForSearch: -1
            });
      /*      $("#rperson_assign_department").select2({
                placeholder: "Επιλέξτε Τμήμα"
            });*/
            dashboard.sevents.InitAssignedUnits();
            dashboard.sevents.InitInstitutionUnits();
        }

        function RegisterEvents() {
            $scheduledEventsDtElem.on("dblclick", "tbody td", function (e) {
                // get selected row index
                let table_cell = $(this).closest('td');
                let rowIdx = ScheduledEventsDT.cell(table_cell).index().row;

                setupEventEditPanel("edit", "`Επεξεργα`σία Προγραμματισμένης Εκδήλωσης",rowIdx);
                e.stopPropagation();
            });
            $scheduledEventsDtElem.on("click", "tbody button", function () {

                // get selected row index
                let table_cell = $(this).closest('td');
                let rowIdx = ScheduledEventsDT.cell(table_cell).index().row;
                setupEventEditPanel ("edit", "Επεξεργασία Προγραμματισμένης Εκδήλωσης",rowIdx);
            });

            $("#addOrUpdateSevent").on("click", function() {
                let event_id        = $("#sevent_id").val();
                let event_title     = $("#sevent_title").val();
                let event_startDate = $("#sevent_startDate").val();
                if (event_startDate != null && event_startDate !== "") {
                    event_startDate = moment(event_startDate).format('YYYY-MM-DDTHH:mm:ss[Z]');
                }
                else {
                    event_startDate = null;
                }
                let event_endDate   = $("#sevent_endDate").val();
                if (event_endDate != null && event_endDate !== "") {
                    event_endDate = moment(event_endDate).format('YYYY-MM-DDTHH:mm:ss[Z]');
                }
                else {
                    event_endDate = null;
                }
                let event_type      = $("#sevent_type").val();
                let event_place     = $("#sevent_place").val();
                let event_isActive  = $("#sevent_isactive_toggle").prop('checked');
                let sevent_rPerson  = $("#sevent_rpersonId").val();

                //Add Responsible Units to AssignedUnitsDT
                let nodes = AssignedUnitsDT .rows().data();
                let sevent_runits =[];
                if (nodes.length>0) {
                    for (let l = 0; l < nodes.length; l++) {
                        sevent_runits.push(nodes[l]);
                    }
                }
                //Check Form for Errors
                if (event_title == null || event_title === "" || event_startDate == null || event_startDate === "" || event_type == null || event_type === "" ||
                    sevent_rPerson == null || sevent_rPerson === "" || sevent_runits.length === 0) {
                    alertify.alert("<i style='color:red' class='fas fa-exclamation-triangle'></i> Πρόβλημα","Υπάρχουν παραλείψεις στη φόρμα. Διορθώστε τα κενά και προσπαθήστε πάλι")
                }
                else {
                    let sEventData = {
                        "id": event_id,
                        "title" : event_title,
                        "type": event_type,
                        "startDate":event_startDate,
                        "endDate": event_endDate,
                        "place" : event_place,
                        "isActive":event_isActive,
                        "responsiblePerson" : {
                            "id" :sevent_rPerson
                        },
                        "editor" : {
                            "id" : $("#editor_id").val()
                        },
                        "responsibleUnit" : sevent_runits,
                        "editor.id" : "set"
                    };
                    postUpdate(sEventData);
                }
            });
            $("#deleteSevent").on("click", function (e) {

                let sevent_id = $("#sevent_id").val();
                let sevent_title = $("#sevent_title").val();

                let msg = '<div class="font-weight-bold">Η Προγραμματισμένη Εκδήλωση "' + sevent_title + '" Θα διαγραφεί! Είστε σίγουρος;</div>';
                msg += '<div>Προσοχή: Αν έχουν δημιουργηθεί Πoλυμέσα Εκδηλώσεων για την Προγραμματισμένη Εκδήλωση ή υπάρχουν Προγραμματισμένες Εκδηλώσεις, η διαγραφή θα ακυρωθεί'
                alertify.confirm('<i style="color: red" class="fas fa-trash-alt"></i> Διαγραφή Προγραμματισμένης Εκδήλωσης', msg,
                    function () {
                        postDelete(sevent_id);
                    },
                    function () {
                    }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
                e.preventDefault();
            });

            $("#closeUpdateSevent").on('click',function(){
                let end_serialize = $("#sevent_form").serialize();
                if (serialize_form !== end_serialize) {
                    closeEditDialogWarning();
                }
                else {
                    unloadEditForm();
                }
            });
            $("#newSeventBt").on("click", function() {
                setupEventEditPanel ("add", "Δημιουργία Προγραμματισμένης Εκδήλωσης",-1);
            });
            $("#selectRpersonBt").on("click",function(e) {
                loadInstitutionStaffMembersModal("");
                e.preventDefault();
            });
            $("#rperson_assign_department").on('select2:select', function (e) {
                $("#default_dp_id").val(e.params.data.id);
                //loadInstitutionStaffMembersModal(e.params.data.id);
                // initialize datatable if not initialized yet! else reload ajax
                if ( ! $.fn.DataTable.isDataTable( '#rPersonSelectDataTable' ) ) {
                    dashboard.sevents.InitInstitutionStaffMembers(e.params.data.id);
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
                    $("#sevent_rperson").val(node[0].name + " (Τμήμα: " + node[0].department.title + ")");//single select
                    $("#sevent_rpersonId").val(node[0].id);
                    $("#rPersonSelectModal").modal('hide');
                }
            });
            $("#setMySelfBt").on('click',function(e) {
                setMySelfAsRP();
                e.preventDefault();
            });
            $("#toggleStartDate").on('click',function(e){
                $("#sevent_startDate").flatpickr().toggle();
                e.preventDefault();
            });
            $("#toggleEndDate").on('click',function(e){
                $("#sevent_endDate").flatpickr().toggle();
                e.preventDefault();
            });
            $("#clearEndDate").on('click',function(e){
                $("#sevent_endDate").flatpickr().clear();
                e.preventDefault();
            });
            $("#addAssignedUnits").on('click',function(){
                AssignedUnitsDT.clear().draw();
                //Update AssignedUnitsDT with user selections
                let nodes= InstitutionUnitsDT.rows( { selected: true } ).data();
                if (nodes.length>0) {
                    for (let l = 0; l < nodes.length; l++) {
                        let new_row = nodes[l];
                        AssignedUnitsDT.row.add( new_row ).draw();
                    }
                }
                $("#runitSelectModal").modal('hide');
            })
        }
        function setupEventEditPanel (action, title, rowIdx) {

            AssignedUnitsDT.clear().draw();

            if (rowIdx !== -1) {
                let row_data = ScheduledEventsDT.row( rowIdx ).data();
                if (row_data.dateModified != null) {
                    let epochDate = row_data.dateModified;
                    let formatted_date = moment.unix(epochDate).format('LLL');
                    $("#s_event_info").html("Τελευταία Ενημέρωση: " + formatted_date + " (" + row_data.editor.name + ")");
                }
                $("#sevent_id").val(row_data.id);
                $("#sevent_title").val(row_data.title);
                $("#sevent_type").val(row_data.type).trigger("change");
                $("#seventModalLabel").html(row_data.title);
                //Start Date
                if (row_data.startDate != null) {
                    let epochDate = row_data.startDate;
                    let formatted_date = moment.unix(epochDate).format('YYYY-MM-DD');
                    $("#sevent_startDate").val(formatted_date);
                }
                else {
                    $("#sevent_startDate").flatpickr().clear();
                }
                //End Date
                if (row_data.endDate != null) {
                    let epochDate = row_data.startDate;
                    let formatted_date = moment.unix(epochDate).format('YYYY-MM-DD');
                    $("#sevent_endDate").val(formatted_date);
                }
                else {
                    $("#sevent_endDate").flatpickr().clear();
                }
                //Set AssignedUnitsDT from Database (get values from ScheduledEventsDT - row)
                if ( row_data.responsibleUnit != null ) {
                    row_data.responsibleUnit.forEach(function(runit) {
                        let new_row = {
                            "id" : runit.id,
                            "structureType" : runit.structureType,
                            "title" : runit.title
                        }
                        AssignedUnitsDT.row.add( new_row ).draw();
                    })
                }
                //Set REsponsible Person from Database (get values from ScheduledEventsDT - row)
                if (row_data.responsiblePerson != null) {
                    let node = row_data.responsiblePerson
                    $("#sevent_rperson").val(node.name + " (Τμήμα: " + node.department.title + ")");
                    $("#sevent_rpersonId").val(node.id);
                }
                $("#sevent_place").val(row_data.place);
                if (row_data.isActive) {
                    $('#sevent_isactive_toggle').bootstrapToggle('on');
                }
                $("#deleteSevent").show();
                $("#st_rowIdx_edited").val(rowIdx);
            }
            else {
                $("#seventModalLabel").html(title);
                $("#s_event_info").html("Επεξεργασία: " + $("#editor_name").val());
                $('#sevent_isactive_toggle').bootstrapToggle('off');
                $("#sevent_id").val("");
                $("#sevent_title").val("");
                $("#sevent_type").val("other").trigger("change");
                $("#sevent_startDate").flatpickr().clear();
                $("#sevent_endDate").flatpickr().clear();

                let isEditorStaffMember = $("#isStaffMember").val();
                if (isEditorStaffMember === "true") {
                    setMySelfAsRP();
                }
                else {
                    $("#sevent_rperson").html("");
                    $("#sevent_rpersonId").val("");
                }
                $("#sevent_place").val("");
                $("#deleteSevent").hide();
                $("#st_rowIdx_edited").val("");
            }
            $("#sevents_card").hide();
            $("#sevents_edit_card").show();

            //Serialize form to check for changes on submit
            serialize_form = $("#sevent_form").serialize();
        }

        function loadInstitutionStaffMembersModal(departmentId) {
            let $rperson_assign_department = $("#rperson_assign_department");
            //department filter on Institution's StaffMembers Modal
            dashboard.departments.initializeDepartmentsList(departmentId, $rperson_assign_department);

            //departmentId = $("#default_dp_id").val();
            if (departmentId === "") { departmentId = "dummy";}
            // initialize datatable if not initialized yet! else reload ajax
            if ( ! $.fn.DataTable.isDataTable( '#rPersonSelectDataTable' ) ) {
                dashboard.sevents.InitInstitutionStaffMembers(departmentId);
            }
            else {
                let ajax_url = dashboard.siteurl + '/api/v1/dt/staff.web/department/' + departmentId
                StaffMembersDT.ajax.url(ajax_url);
                StaffMembersDT.ajax.reload();
            }
            let sevent_title = $("#sevent_title").val();
            $("#rPersonSelectModalLabel").html('<div style="font-size: 1.4em">' + sevent_title + '</div><small>Επιλογή Επιστημονικού Υπεύθυνου</small>');
            $("#rPersonSelectModal").modal('show');
        }
        function postUpdate(sEventData) {
            $.ajax({
                type:        "POST",
                url: 		  dashboard.siteurl + '/api/v1/sevent/save',
                contentType: "application/json; charset=utf-8",
                data: 		  JSON.stringify(sEventData),
                async:		  true,
                success: function(data){
                    ScheduledEventsDT.ajax.reload();
                    $("#sevent_id").val(data);
                    alertify.notify("Η Προγραμματισμένη Εκδήλωση αποθηκεύτηκε με επιτυχία", "success");
                    //re-Calculate form serialization
                    serialize_form = $("#sevent_form").serialize();
                },
                error: function ()  {
                    alertify.alert('Error-Update-Sevent');
                }
            });
        } //postUpdate
        function postDelete(seventId) {

            $.ajax({
                type:        "DELETE",
                url: 		  dashboard.siteurl + '/api/v1/sevent/delete/' + seventId,
                contentType: "application/json; charset=utf-8",
                async:		  true,
                success: function(){
                    unloadEditForm();
                    ScheduledEventsDT.ajax.reload();
                    alertify.notify("Η Προγραμματισμένη Εκδήλωση διαγράφηκε με επιτυχία", "success");
                },
                error: function (data)  {
                    let info = "Άγνωστο Σφάλμα";
                    let msg = data.responseText;
                    if (msg === "_FORBIDDEN_LECTURES") {
                        info = "<div class='mt-2'>Η Προγραμματισμένη Εκδήλωση δεν μπορεί να διαγραφεί! Βρέθηκαν Διαλέξεις</div>";
                    }
                    else if (msg === "_FORBIDDEN_SCHEDULER") {
                        info = "<div class='mt-2'>Το Προγραμματισμένη Εκδήλωση δεν μπορεί να διαγραφεί! Βρέθηκαν προγραμματισμένες Εκδηλώσεις</div>";
                    }
                    else if (msg === "_NOT_FOUND") {
                        info = "<div class='mt-2'>Η Προγραμματισμένη Εκδήλωση δεν βρέθηκε</div>";
                    }
                    $("#courseModal").modal('hide');
                    alertify.alert('Σφάλμα', '<i style="color: red" class="fas fa-exclamation-circle"></i> ' + msg + info);
                }
            });
        }
        function unloadEditForm() {
            $("#sevents_edit_card").hide();
            $("#sevents_card").show();
        }
        function closeEditDialogWarning() {
            let msg = '<div class="font-weight-bold">Οι αλλαγές θα χαθούν! Είστε σίγουρος?</div>';
            alertify.confirm('Προειδοποίηση', msg,
                function () {
                    unloadEditForm();
                },
                function () {
                }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
        }
        function setMySelfAsRP() {
            let editor_id = $("#editor_id").val();
            $.getJSON(dashboard.siteurl + '/api/v1/user/' + editor_id, function(data) {
                $("#sevent_rperson").val(data.name + " (Τμήμα: " + data.department.title + ")");
                $("#sevent_rpersonId").val(data.id);
            });
        }
    };

    dashboard.sevents.InitInstitutionStaffMembers = function (departmentId) {

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
            "language":  dtLanguageGr,
            select: {
                style: 'single'
            },
            order: [[1, 'asc']],
            "pageLength": 10,
            "aoColumnDefs": [
                {
                    "aTargets": [0],
                    "mData": "id",
                    "visible": false,
                }
            ]
        });
    }; // Staff DataTable Init

    dashboard.sevents.InitInstitutionUnits = function () {

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
    };

    dashboard.sevents.InitAssignedUnits = function () {

        let $assignUnitsDtElement = $("#runitsAssignedDataTable");
        AssignedUnitsDT = $assignUnitsDtElement.DataTable({
            "columns": [
                {"data": "id"},
                {"data": "structureType"},
                {"data": "title"},
            ],
            "language":  dtLanguageGr,
            dom: 'Brti',
            buttons: {
                buttons: [
                    {
                        text: '<i class="fas fa-hand-pointer"></i> Επιλογή Διοργανωτών...', className: 'btn btn-sm blue-btn-wcag-bgnd-color text-white', attr: {id: 'selectRunitBt'},
                        action: function () {
                            LoadAllUnitsModal();
                        }
                    },
                ],
                dom: {
                    button: {
                        className: 'btn btn-sm mb-2'
                    }
                }
            },
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
        function LoadAllUnitsModal() {
            //reset DT page
            InstitutionUnitsDT.row(0).show().draw(false);
            //set modal title
            let sevent_title = $("#sevent_title").val();
            $("#runitSelectModalLabel").html('<div style="font-size: 1.4em">' + sevent_title + '</div><small>Επιλογή Διοργανωτών Εκδήλωσης</small>');
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
    };
})();