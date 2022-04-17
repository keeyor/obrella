(function () {
    'use strict';

    dashboard.staffmembers = dashboard.staffmembers || {};

    let staffDT;
    let staffCoursesDT;
    let courseSelectDT;
    let serialize_form;
    let $staffDtElem;
    dashboard.staffmembers.init = function () {};

    dashboard.staffmembers.initDT = function () {

        $staffDtElem = $("#staffDataTable");
        staffDT = $staffDtElem.DataTable({
            "ajax": dashboard.siteurl + '/api/v1/dt/staff.web/department/' + dashboard.departments.selectedDepartmentId,
            "columns": [
                {
                    "className":      'details-control',
                    "orderable":      false,
                    "data":           null,
                    "defaultContent": ''
                },
                {"data": null},
                {"data": "id"},
                {"data": "uid"},//3
                {"data": "name"}, //4
                {"data": "altName"}, //5
                {"data": "courses"}, //6
                {"data": "affiliation"}, //6
                {"data": "email"}, //7
                {"data": "id"}, //8
                {"data": "authorities"} //9
            ],
            "language": dtLanguageGr,
            order: [[3, 'asc']],
            "pageLength": 25,
            "pagingType": "full_numbers",
            "dom": '<"top"flp>rt<"bottom">p<"clear">',
            fixedHeader: true,
            "aoColumnDefs": [
                {
                    "aTargets": [1],
                    "sortable": false
                },
                {
                    "aTargets": [2,3,5,7,10],
                    "mData": "id",
                    "visible": false,
                },
                {
                    "aTargets": [4],
                    "mRender": function (data,type,row) {
                        return '<span class="pb-0 mb-0 me-1" style="color: #003476;font-weight: 500">' + data + '</span>' + row["affiliation"];
                    }
                },
                {
                    "aTargets": [6],
                    "mRender": function (data) {
                        return data.length;
                    }
                },
                {
                    "aTargets": [7],
                    "mRender": function (data) {
                        return '<b>' + data + '</b>';
                    }
                },
                {
                    "aTargets": [9],
                    "mData": "id",
                    "sortable": false,
                    "className": "text-right",
                    "mRender": function () {
                        return '<button type="button" title="επεξεργασία στοιχείων" class="btn btn-pill blue-btn-wcag-bgnd-color btn-sm"><i class="fas fa-edit text-white"></i></button>';
                    }
                }
            ],
            "initComplete": set_display_results,
        });

        staffDT.on( 'order.dt search.dt', function () {
            staffDT.column(1, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
                cell.innerHTML = i+1;
            } );
        } ).draw();

        $("#staff_manager_toggle").bootstrapToggle({
            on: '<i class="fas fa-power-off"></i>',
            off: '<i class="fas fa-ban"></i>',
            onstyle: "success",
            offstyle: "danger",
            size: "small"
        });

        function  set_display_results() {
            $("#count_staff_results").html("" + staffDT.rows().count() + "");
        }

        RegisterEvents();

        function RegisterEvents() {

            $staffDtElem.on("click", "td.details-control", function () {

                var tr = $(this).closest('tr'),
                    row = staffDT.row(tr);
                if (tr.hasClass('shown')) {
                    $('div.childWrap', row.child()).slideUp( function () {
                        tr.removeClass('shown');
                        row.child().remove();
                    } );
                }
                else {
                        $.when(getStaffAssignedCourses(row.data().id)).then(function (response) {
                            row.child(renderStaffChild(response), 'no-padding').show();
                            tr.addClass('shown');
                            $('div.childWrap', row.child()).slideDown();
                        });
                }
            });

            function getStaffAssignedCourses(id) {
                return $.ajax({
                    url: dashboard.siteurl + '/api/v1/dt/courses.web/staff/' + id,
                    type: "GET"
                });
            }
            function renderStaffChild(data) {
                var wrapper = $('<div style="padding:5px 0" class="childWrap"></div>'),
                    result = [];

                $.each(data.data, function (i, v) {
                    let table_row_html = "<tr><td></td><td></td><td></td>";
                    table_row_html += "<td><b>" + v.title + "</b></td>";
                    table_row_html += "<td>" + v.department.title + "</td>";
                    table_row_html += "<td>" + v.studyProgramTitle + "</td>";
                    table_row_html += "<td>";
                    $.each(v.supportedBy, function (j, sb) {
                        table_row_html += sb.name + "<br/>";
                    });
                    table_row_html += "</td>";
                    table_row_html += "</tr>";
                    result.push(table_row_html);
                });

                let cTable = '<table class="child-table" style="width: 100%">' +
                    '<thead><tr>' +
                    '<th colspan="2" style="text-align: right">' +
                        '<i class=\"fas fa-level-up-alt fa-rotate-90\"></i>' +
                    '</th>' +
                    '<th>Μαθήματα</th><th>Τίτλος</th><th>Τμήμα</th><th>Πρόγραμμα Σπουδών</th><th>Υποστήριξη</th></tr></thead>' +
                    '<tbody>' + result.join('') + '</tbody></table>';
                wrapper.append(cTable);

                return wrapper;
            }

            $staffDtElem.on("dblclick", "tbody td", function (e) {
                // get selected row index
                let table_cell = $(this).closest('td');
                let rowIdx = staffDT.cell(table_cell).index().row;

                setupCoModal ("edit", "Επεξεργασία Μέλους Προσωπικού",rowIdx);
                e.stopPropagation();
            });

            $staffDtElem.on("click", "tbody button", function () {

                // get selected row index
                let table_cell = $(this).closest('td');
                let rowIdx = staffDT.cell(table_cell).index().row;

                setupCoModal ("edit", "Επεξεργασία Μέλους Προσωπικού",rowIdx);
            });

            $("#newStaffBt").on("click", function() {
                setupCoModal ("add", "Νέο Μέλος",-1);
                return false;
            });

            $("#addOrUpdateStaff").on("click", function() {

                loader.showLoader();
                let staff_id        = $("#staff_id").val();
                let staff_uid       = $("#staff_uid").val();
                let staff_name      = $("#staff_name").val();
                let staff_altname   = $("#staff_altname").val();
                let staff_aff       = $("#staff_aff").val();
                let staff_sp_id     = dashboard.departments.selectedDepartmentId;
                let staff_email     = $("#staff_email").val();

                let staff_authorities = [];
                staff_authorities.push("STAFFMEMBER");
                let is_manager = $("#staff_manager_toggle").prop('checked');
                if (is_manager) {
                    staff_authorities.push("MANAGER");
                }
                if (staff_name == null || staff_name === "" || staff_uid == null || staff_uid === "" || staff_email == null || staff_email === "" || staff_aff === "") {
                    alertify.alert("<i style='color:red' class='fas fa-exclamation-triangle'></i> Πρόβλημα", "Υπάρχουν παραλείψεις στη φόρμα. Διορθώστε τα κενά και προσπαθήστε πάλι");
                } else {
                    let staffData = {
                        "id": staff_id,
                        "uid" : staff_uid,
                        "name": staff_name,
                        "altName": staff_altname,
                        "affiliation": staff_aff,
                        "authorities" : staff_authorities,
                        "email" : staff_email,
                        "department" : {
                            id : staff_sp_id,
                            title: "set"
                        }
                    };
                    postUpdate(staffData);
                }
            });

            $("#deleteStaffBt").on("click", function(e) {
                let staff_name = $("#staff_name").val();
                let staffId = $("#staff_id").val();

                let msg = '<div class="font-weight-bold">Το μέλος του Προσωπικού "' + staff_name + '" Θα διαγραφεί! Είστε σίγουρος;</div>';
                msg += '<div>Προσοχή: Αν έχουν υπάρχουν διαλέξεις ή εκδηλώσεις που αναφέρονται στο μέλος, η διαγραφή θα ακυρωθεί'
                alertify.confirm('<i style="color: red" class="fas fa-trash-alt"></i> Διαγραφή μέλους Προσωπικού', msg,
                    function () {
                        postDelete(staffId);
                    },
                    function () {
                    }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
                e.preventDefault();
            });
            $("#closeUpdateStaff").on('click',function(){
                    let end_serialize = $("#staff_form").serialize();
                    if (serialize_form !== end_serialize) {
                        closeEditDialogWarning();
                    }
                    else {
                        unloadEditForm();
                    }
            });
            $("#addAssignedCourses").on('click',function(){
                let nodes= courseSelectDT.rows( { selected: true } ).data();
                if (nodes.length>0) {
                    let courseIds =[]
                    let staff_id = $("#staff_id").val();
                    for (let l = 0; l < nodes.length; l++) {
                        courseIds.push(nodes[l].id);
                    }
                    postAssignedCourses(staff_id, courseIds);
                }
            })
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
        function setupCoModal (action, title, rowIdx) {
            $("#staff_edit_mode").val(action);

            let $staff_id = $("#staff_id");
            let $staff_name = $("#staff_name");

            if (rowIdx !== -1) {
                let row_data = staffDT.row( rowIdx ).data();
                $("#staffModalLabel").html('<div style="font-size: 1.4em">' + row_data.name + '</div><small>' + title + '</small>');
                $staff_id.val(row_data.id);
                $staff_name.val(row_data.name);
                $("#staff_altname").val(row_data.altName);
                $("#staff_aff").val(row_data.affiliation);
                $("#staff_uid").val(row_data.uid);
                $("#staff_department").val(dashboard.departments.selectedDepartmentName);
                $("#staff_email").val(row_data.email);
                $("#st_rowIdx_edited").val(rowIdx);

                let user_authorities = [];
                user_authorities = row_data.authorities;
                if (user_authorities.includes("MANAGER")) {
                    $("#staff_manager_toggle").bootstrapToggle('on');
                    $("#staff_manager_toggle").bootstrapToggle('disable');
                    $(".manager_warning").show();
                    $(".no_manager_info").hide();
                }
                else {
                    $("#staff_manager_toggle").bootstrapToggle('enable');
                    $("#staff_manager_toggle").bootstrapToggle('off');
                    $('.manager_warning').hide();
                    $(".no_manager_info").show();
                }
                dashboard.staffmembers.assignedCoursesDT();

                //hide Delete Button for myself
                if (row_data.id === $("#signInUserid").val()) {
                    $("#deleteStaffBt").attr("disabled",true);
                }
                else {
                    $("#deleteStaffBt").attr("disabled",false);
                }

                $(".course_assign").show();
                $(".course_assign_warning").hide();
            }
            else {
                    $("#staffModalLabel").html(title);
                    $staff_id.val("");
                    $staff_name.val("");
                    $("#staff_altname").val("");
                    $("#staff_aff").val("");
                    $("#staff_department").val(dashboard.departments.selectedDepartmentName);
                    $("#staff_email").val("");
                    $("#staff_uid").val("");
                    $("#staff_manager_toggle").bootstrapToggle('enable');
                    $("#staff_manager_toggle").bootstrapToggle('off');
                    $('.manager_warning').hide();
                    $(".no_manager_info").show();
                    $("#st_rowIdx_edited").val("");
                    $(".course_assign").hide();
                    $(".course_assign_warning").show();
            }
            $("#staff-tab").html("Επεξεργασία Μέλους Προσωπικού " + "[<b>" + $staff_name.val() + "</b>]");
            $("#staff_card").hide();
            $("#staff_edit_card").show();
            serialize_form = $("#staff_form").serialize();
        }

        function postUpdate(staffData) {
            $.ajax({
                type:        "POST",
                url: 		  dashboard.siteurl + '/api/v1/staff/save',
                contentType: "application/json; charset=utf-8",
                data: 		  JSON.stringify(staffData),
                async:		  true,
                success: function(data){
                    staffDT.ajax.reload( function () {
                        loader.hideLoader();
                    });
                    $("#staff_id").val(data);
                    if ( ! $.fn.DataTable.isDataTable( '#staffCoursesDataTable' ) ) {
                        $(".course_assign").show();
                        $(".course_assign_warning").hide();
                        dashboard.staffmembers.assignedCoursesDT();
                    }
                    else {
                        staffCoursesDT.ajax.url(dashboard.siteurl + '/api/v1/dt/courses.web/staff/' + data);
                        staffCoursesDT.ajax.reload();
                    }
                    //set staff_manager_toggle_state
                    if ($("#staff_manager_toggle").prop('checked')) {
                        $("#staff_manager_toggle").bootstrapToggle('on');
                        $("#staff_manager_toggle").bootstrapToggle('disable');
                        $(".manager_warning").show();
                        $(".no_manager_info").hide();
                    }
                    alertify.notify("Το μέλος του Προσωπικού ενημερώθηκε με επιτυχία", "success");
                    serialize_form = $("#staff_form").serialize();
                },
                error: function ()  {
                    loader.hideLoader();
                    alertify.alert('Error-Update-Staff');
                }
            });
        }
        function postDelete(staffId) {

            $.ajax({
                type:        "DELETE",
                url: 		  dashboard.siteurl + '/api/v1/staff/delete/' + staffId,
                contentType: "application/json; charset=utf-8",
                async:		  true,
                success: function(){
                    unloadEditForm();
                    staffDT.ajax.reload( function () {
                        loader.hideLoader();
                    });
                    alertify.notify("Το μέλος του Προσωπικού διαγράφηκε με επιτυχία", "success");
                },
                error: function (data)  {
                    let info = "Άγνωστο Σφάλμα";
                    let msg = data.responseText;
                    if (msg === "_FORBIDDEN_LECTURES") {
                        info = "<div class='mt-2'>To μέλος του Προσωπικού δεν μπορεί να διαγραφεί! Βρέθηκαν Διαλέξεις ή/και Εκδηλώσεις που αναφέρονται στο επιλεγμένο " +
                            "μέλος του Προσωπικού</div>";
                    }
                    else if (msg === "_FORBIDDEN_SCHEDULER") {
                        info = "<div class='mt-2'>To μέλος του Προσωπικού δεν μπορεί να διαγραφεί! Βρέθηκαν Προγραμματισμένες Μεταδόσεις που αναφέρονται στο επιλεγμένο " +
                            "μέλος του Προσωπικού</div>";
                    }
                    else if (msg === "_FORBIDDEN_SCHEDULED_EVENTS") {
                        info = "<div class='mt-2'>To μέλος του Προσωπικού δεν μπορεί να διαγραφεί! Βρέθηκαν Εκδηλώσεις που αναφέρονται στο επιλεγμένο " +
                            "μέλος του Προσωπικού</div>";
                    }
                    else if (msg === "_NOT_FOUND") {
                        info = "<div class='mt-2'>Το μέλος του Προσωπικού δεν βρέθηκε</div>";
                    }
                    $("#courseModal").modal('hide');
                    loader.hideLoader();
                    alertify.alert('Σφάλμα', '<i style="color: red" class="fas fa-exclamation-circle"></i> ' + msg + info);
                }
            });
        }
        function unloadEditForm() {
            if (staffCoursesDT !== undefined) {
                staffCoursesDT.destroy();
            }
            $("#staff-tab").html("Προσωπικό");
            $("#staff_edit_card").hide();
            $("#staff_card").show();
        }
        function postAssignedCourses(staff_Id,courseIds) {
            $.ajax({
                type:        "POST",
                url: 		  dashboard.siteurl + '/api/v1/staff/assign_courses/' + staff_Id,
                contentType: "application/json; charset=utf-8",
                data: 		  JSON.stringify(courseIds),
                async:		  true,
                success: function(){
                    staffCoursesDT.ajax.url = dashboard.siteurl + '/api/v1/dt/staff.web/department/' + dashboard.departments.selectedDepartmentId
                    staffCoursesDT.ajax.reload( function () {
                        $("#courseSelectModal").modal('hide');
                        alertify.notify("Τα Διδασκόμενα Μαθήματα ενημερώθηκαν με επιτυχία", "success");
                    });
                },
                error: function ()  {
                    $("#courseSelectModal").modal('hide');
                    alertify.alert('Error-Update-Courses-Error');
                }
            });
        } //

    }; // Staff DataTable Init

    dashboard.staffmembers.assignedCoursesDT = function () {

        let $staffCourses = $("#staffCoursesDataTable");
        let $staff_id = $("#staff_id");
        let $course_assign_department = $("#course_assign_department");

        staffCoursesDT = $staffCourses.DataTable({
            "ajax": dashboard.siteurl + '/api/v1/dt/courses.web/staff/' + $staff_id.val(),
            "columns": [
                {"data": "id"},
                {"data": "title"},
                {"data": "department.title"},
                {"data": "studyProgramTitle"},
                {"data": "id"}
            ],
            dom: 'Bfrtip',
            buttons: {
                buttons: [
                    {
                        text: '<span class="fas fa-plus mr-1"></span> προσθήκη μαθήματος', className: 'blue-btn-wcag-bgnd-color text-white', attr: {id: 'openCoursesSelectModal'},
                        action: function () {
                            loadCourseSelectModal();
                        }
                    },
                ],
                dom: {
                    button: {
                        className: 'btn btn-sm openCoursesSelectModal'
                    }
                }
            },
            "language": dtLanguageGr,
            order: [[1, 'asc']],
            "pageLength": 10,
            "aoColumnDefs": [
                {
                    "aTargets": [0],
                    "mData": "id",
                    "visible": false
                },
                {
                    "aTargets": [4],
                    "mData": "id",
                    "sortable": false,
                    "className": "text-right",
                    "mRender": function () {
                        return '<button type="button" title="αφαίρεση μαθήματος" class="btn btn-sm"><i class="fas fa-minus"></i> </button>';
                    }
                }
            ]

        });

        function loadCourseSelectModal() {
            dashboard.departments.initializeDepartmentsList(dashboard.departments.selectedDepartmentId,$course_assign_department,'');
            $("#default_dp_id").val(dashboard.departments.selectedDepartmentId);
            // initialize datatable if not initialized yet! else reload ajax
            if ( ! $.fn.DataTable.isDataTable( '#courseSelectDataTable' ) ) {
                dashboard.staffmembers.courseSelectsDT();
            }
            else {
                let staff_id = $staff_id.val();
                let default_dp_id = dashboard.departments.selectedDepartmentId
                reloadCourseSelectDepartmentInModal(staff_id,default_dp_id)
            }
            $("#courseSelectModalLabel").html('<div style="font-size: 1.4em">' + $("#staff_name").val() + '</div><small>Προσθήκη στη λίστα των Διδασκόμενων Μαθημάτων </small>');
            $("#courseSelectModal").modal('show');
        }
        function reloadCourseSelectDepartmentInModal(staff_id, _dp_id) {
            if (_dp_id === "") {_dp_id = "_all"}
            courseSelectDT.ajax.url(dashboard.siteurl + '/api/v1/dt/courses.web/staff/u/' + staff_id + '/dp/' + _dp_id);
            courseSelectDT.ajax.reload();
        }
        function postUnAssignCourse(staff_Id,courseId) {
            $.ajax({
                type:        "POST",
                url: 		  dashboard.siteurl + '/api/v1/staff/unassign_course/' + staff_Id,
                contentType: "application/json; charset=utf-8",
                data: 		  courseId,
                async:		  true,
                success: function(){
                    staffCoursesDT.ajax.url = dashboard.siteurl + '/api/v1/dt/staff.web/department/' + dashboard.departments.selectedDepartmentId
                    staffCoursesDT.ajax.reload( function () {
                        alertify.notify("Το Μάθημα αφαιρέθηκε με επιτυχία", "success");
                    });
                },
                error: function ()  {
                    $("#courseSelectModal").modal('hide');
                    alertify.alert('Error-UnAssign-Courses-Error');
                }
            });
        }

        $course_assign_department.on('select2:select', function (e) {
            let selected_dp_id  = e.params.data.id;
            let staff_id = $("#staff_id").val();
            reloadCourseSelectDepartmentInModal(staff_id,selected_dp_id);
        });
        $staffCourses.on("click", "tbody button", function (e) {
            // get selected row index
            let table_cell = $(this).closest('td');
            let rowIdx = staffCoursesDT.cell(table_cell).index().row;
            let row_data = staffCoursesDT.row( rowIdx ).data();
            let staff_id = $("#staff_id").val();
            postUnAssignCourse(staff_id,row_data.id);
            e.stopPropagation();
        });
    }

    dashboard.staffmembers.courseSelectsDT = function () {

        let staff_id = $("#staff_id").val();
        let default_dp_id = $("#default_dp_id").val();
        if (default_dp_id === "") { default_dp_id = "_all"}
        let $staffCourses = $("#courseSelectDataTable");
        courseSelectDT = $staffCourses.DataTable({
            "ajax": dashboard.siteurl + '/api/v1/dt/courses.web/staff/u/' + staff_id + '/dp/' + default_dp_id,
            "columns": [
                {"data": "id"},
                {"data": "title"},
                {"data": "department.title"},
                {"data": "studyProgramTitle"}
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
                    "aTargets": [0,3],
                    "mData": "id",
                    "visible": false
                },
                {
                    "aTargets": [1],
                    "mRender": function (data,type, row) {
                        let ret = '<h6 class="pb-0 mb-0" style="color: #003476">' + data + '</h6>';
                        ret += row["studyProgramTitle"];
                        return ret;
                    }
                },
            ]
        });
    }


})();
