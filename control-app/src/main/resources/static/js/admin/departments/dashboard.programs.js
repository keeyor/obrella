(function () {
    'use strict';

    dashboard.programs = dashboard.programs || {};

    var programsDT = null;
    let study_filter = "_all";
    let serialize_form;

    dashboard.programs.selectedProgramId = null;
    dashboard.programs.selectedRowIndex = null;
    dashboard.programs.selectedProgramName = null;

    dashboard.programs.selectedStudies = null;

    dashboard.programs.init = function () {

        InitControls();

        //Init Study Filter
        dashboard.programs.selectedStudies = "all";
        dashboard.programs.setStudyFilter(dashboard.programs.selectedStudies);

        dashboard.programs.selectedProgramId = -1;
        dashboard.programs.selectedRowIndex = -1;
        dashboard.programs.selectedProgramName = "all";

        let $programsDtElem = $("#programsDataTable");

        programsDT = $programsDtElem.DataTable({
            "ajax":  dashboard.siteurl + '/api/v1/dt/programs.web/school/_all/department/' + dashboard.departments.selectedDepartmentId + '/study/_all',
            "columns": [
                {"data": null},
                {"data": "id"},
                {"data": "title"},
                {"data": "study"},
                {"data": "studyTitle"},
                {"data": "schoolId"},
                {"data": "departmentId"},
                {"data": "departmentName"},
                {"data": "id"}
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
                    "aTargets": [1],
                    "visible": false
                },
                {
                    "aTargets": [2],
                    "mRender": function (data) {
                        return '<span class="pb-0 mb-0" style="color: #003476;font-weight: 500">' + data + '</span>';
                    }
                },
                {
                    "aTargets": [4],
                    "mRender": function (data) {
                        return '<span style="font-weight: 500">' + data + '</span>';
                    }
                },
                {
                    "aTargets": [3,5,6,7],
                    "visible": false,
                    "sWidth": "0px",
                },
                {
                    "aTargets": [8],
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
        programsDT.on( 'order.dt search.dt', function () {
            programsDT.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
                cell.innerHTML = i+1;
            } );
        } ).draw();
        function  set_display_results() {
            $("#count_programs_results").html("" + programsDT.rows().count() + "");
        }

        RegisterEvents();

        function InitControls() {
            $.fn.modal.Constructor.prototype._enforceFocus = function() {}; // !Important: otherwise select2 is disabled in modals
            $("#program_study_filter").select2({});
            $("#program_study").select2();
        }
        function RegisterEvents() {

            $("#program_study_filter").on('select2:select', function (e) {
                study_filter = e.params.data.id;
                dashboard.programs.FillProgramsDataTable();
            });

            $programsDtElem.on("dblclick", "tbody td", function (e) {
                // get selected row index
                let table_cell = $(this).closest('td');
                let rowIdx = programsDT.cell(table_cell).index().row;

                setupPrModal ("edit", "Επεξεργασία Προγράμματος Σπουδών",rowIdx);
                e.stopPropagation();
            });

            $programsDtElem.on("click", "tbody button", function () {

                // get selected row index
                let table_cell = $(this).closest('td');
                let rowIdx = programsDT.cell(table_cell).index().row;
                setupPrModal ("edit", "Επεξεργασία Προγράμματος Σπουδών",rowIdx);
            });

            $("#addOrUpdateProgram").on("click", function() {

                let $program_study = $("#program_study");
                let $program_department = $("#program_department");

                let programId      = $("#program_id").val();
                let title          = $("#program_title").val();
                let study          = $program_study.val();
                let studyTitle     = $program_study.select2("data")[0].text;
                let schoolId       = "_set";
                let departmentId   = $program_department.val();
                let departmentName = $program_department.select2("data")[0].text;

                if ((departmentId == null || departmentId === "") || (title == null || title === "") || (study == null || study === "")) {
                    alertify.alert("<i style='color:red' class='fas fa-exclamation-triangle'></i> Πρόβλημα","Υπάρχουν παραλείψεις στη φόρμα. Διορθώστε τα κενά και προσπαθήστε πάλι")
                }
                else {
                    let programData = {
                        "id": programId,
                        "title": title,
                        "study" : study,
                        "studyTitle" : studyTitle,
                        "schoolId": schoolId,
                        "departmentId": departmentId,
                        "departmentName": departmentName,
                    };
                    postUpdate(programData);
                }
            });

            $("#deleteStudyProgram").on("click", function (e) {

                let programId = $("#program_id").val();
                let program_title = $("#program_title").val();

                let msg = '<div class="font-weight-bold">Το Πρόγραμμα Σπουδών "' + program_title + '" Θα διαγραφεί! Είστε σίγουρος;</div>';
                msg += '<div>Προσοχή: Αν έχουν δηλωθεί Μαθήματα για το Πρόγραμμα Σπουδών, η διαγραφή θα ακυρωθεί'
                alertify.confirm('<i style="color: red" class="fas fa-trash-alt"></i> Διαγραφή Προγράμματος Σπουδών', msg,
                    function () {
                        postDelete([programId]);
                    },
                    function () {
                    }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});

                e.preventDefault();
            });
            $("#closeUpdateProgram").on('click',function(){
                let end_serialize = $("#programs_form").serialize();
                if (serialize_form !== end_serialize) {
                    closeEditDialogWarning();
                }
                else {
                    unloadEditForm();
                }
            });
            $("#newProgramBt").on("click", function() {
                setupPrModal ("add", "Νέο Πρόγραμμα Σπουδών",-1);
            });
        }
        function unloadEditForm() {
            $("#programs-tab").html("Προγράμματα Σπουδών");
            $("#programs_edit_card").hide();
            $("#programs_card").show();
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
        function setupPrModal (action, title, rowIdx) {
            $("#program_edit_mode").val(action);
            let department_id;
            let $program_title = $("#program_title");

            if (rowIdx !== -1) {
                let row_data = programsDT.row( rowIdx ).data();
                $("#programModalLabel").html('<div style="font-size: 1.4em">' + row_data.title + '</div><small>' + title + '</small>');

                $("#program_id").val(row_data.id);
                $program_title.val(row_data.title);
                $("#program_study").val(row_data.study);

                department_id = row_data.departmentId;
                $("#pr_rowIdx_edited").val(rowIdx);
                $("#deleteStudyProgram").show();
              }
            else {
                    $("#programModalLabel").html(title);
                    $("#program_id").val("");
                    $program_title.val("");
                    $("#program_study").val("post").trigger("change");

                    department_id = dashboard.departments.selectedDepartmentId;
                    $("#pr_rowIdx_edited").val("");
                    $("#deleteStudyProgram").hide();
            }

            let $program_department = $("#program_department");
            dashboard.departments.initializeDepartmentsList(department_id,$program_department,'');
            if (rowIdx !== -1) {
                $program_department.prop('disabled', true);
            }
            else {
                $program_department.prop('disabled', false);
            }
            //$('#programModal').modal('show');
            $("#programs-tab").html("Επεξεργασία Προγράμματος Σπουδών [<b>" + $program_title.val() + "</b>]");
            $("#programs_card").hide();
            $("#programs_edit_card").show();
            serialize_form = $("#programs_form").serialize();
        }
        function postUpdate(programData) {
            $.ajax({
                type:        "POST",
                url: 		  dashboard.siteurl + '/api/v1/programs/save',
                contentType: "application/json; charset=utf-8",
                data: 		  JSON.stringify(programData),
                async:		  true,
                success: function(){
                    //Highlight edited/added row
                    programsDT.ajax.reload(function () {
                        serialize_form = $("#programs_form").serialize();
                        alertify.notify("Το Πρόγραμμα Σπουδών αποθηκεύτηκε με επιτυχία", "success");

                    })

                },
                error: function ()  {
                    alertify.alert('Error-Update-Program');
                }
            });
        } //postUpdate
        function postDelete(programId) {

            $.ajax({
                type:        "DELETE",
                url: 		  dashboard.siteurl + '/api/v1/programs/delete/' + programId,
                contentType: "application/json; charset=utf-8",
                async:		  true,
                success: function(){
                    //Highlight edited/added row after ajax refresh
                    programsDT.ajax.reload( function () {
                        dashboard.programs.clearProgramsDtSelection();
                        let message = {msg: "Study Program Removed"};
                        dashboard.broker.trigger("programItemClick", [message]);
                    });
                    unloadEditForm();
                    $("#programModal").modal('hide');
                    alertify.notify("Το πρόγραμμα Σπουδών διαγράφηκε με επιτυχία", "success");
                },
                error: function (data)  {
                    let info = "Άγνωστο Σφάλμα";
                    let msg = data.responseText;
                    if (msg === "_FORBIDDEN_COURSES") {
                        info = "<div class='mt-2'>Το πρόγραμμα Σπουδών δεν μπορεί να διαγραφεί! Έχουν δηλωθεί Μαθήματα</div>";
                    }
                    else if (msg === "_NOT_FOUND") {
                        info = "<div class='mt-2'>Το πρόγραμμα Σπουδών δεν βρέθηκε</div>";
                    }
                    $("#programModal").modal('hide');
                    alertify.alert('Σφάλμα', '<i style="color: red" class="fas fa-exclamation-circle"></i> ' + msg + info);
                }
            });
        }
    };// programs DataTable Init

    dashboard.programs.clearProgramsDtSelection = function () {

        //un-highlight selected rows
        $(programsDT.rows().nodes()).removeClass('row_selected');

        dashboard.programs.selectedProgramId = -1;
        dashboard.programs.selectedRowIndex = -1;
        dashboard.programs.selectedProgramName = "all";

        // Display the rowIdx row in the table
        //programsDT.row(0).show().draw(false);

    };
    dashboard.programs.FillProgramsDataTable = function () {

        let schoolId = "_all";
        let departmentId = dashboard.departments.selectedDepartmentId;
        let studyId = $("#program_study_filter").val();

        let ajaxUrl = dashboard.siteurl + '/api/v1/dt/programs.web/school/' + schoolId + '/department/' + departmentId + '/study/' + studyId;

        programsDT.ajax.url(ajaxUrl);
        programsDT.ajax.reload( function() {
            dashboard.programs.updateProgramHeader();
        });
    };
    dashboard.programs.updateProgramHeader = function () {

        let $depFilterName = $("#filtereddpname01");
        let course_tail_html= " Τμήμα " + dashboard.departments.selectedDepartmentName;
        if (study_filter !== "_all") {
            course_tail_html += " <i class=\"fas fa-angle-double-right\"></i> "
            if (study_filter === "under") { course_tail_html += " Προπτυχιακές Σπουδές"}
            else if (study_filter === "post") { course_tail_html += " Μεταπτυχιακές Σπουδές"}
            else if (study_filter === "master") { course_tail_html += " Ξενόγλωσσα Προγράμματα Σπουδών"}
            else if (study_filter === "cont") { course_tail_html += " Επιμόρφωση & Δια Βίου Μάθηση"}
        }
        $depFilterName.html(course_tail_html);
    };
    dashboard.programs.enableProgramDtByRowIndex = function (rowIdx) {

            //clear current selections
            dashboard.programs.clearProgramsDtSelection();

            //highlight selected row
            $(programsDT.row(rowIdx).nodes()).addClass('row_selected');

    };
    dashboard.programs.enableProgramDtByProgramId = function (programId) {

        console.log("Search and enable program with id:" + programId);
        if (programId !== undefined && programId.toString() !== "-1") {
            programsDT.column(0, {order: 'applied'}).nodes().each(function (cell, i) {
                var indexProgramId = programsDT.cell(i, 0).data();
                if (indexProgramId === programId) {
                    dashboard.programs.enableProgramDtByRowIndex(i);
                    console.log("enable program in row:" + i);
                }
            });
        }
        else {
            dashboard.programs.clearProgramsDtSelection();
        }
    };
    dashboard.programs.getProgramList = function (programId, departmentId, $element) {

        $element.empty(); //!important
        if (departmentId === "") departmentId = "_all";
        $.ajax({
            url: dashboard.siteurl + '/api/v1/s2/programs.web/department/' + departmentId,
            async: false, // wait till finish, in order to get serialize form correctly!
            cache: false
        })
            .done(function( data ) {

                $element.select2({
                    width: 'style', // need to override the changed default
                    data : data.results,
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });
                if (programId == null || programId === "") { programId = "program_default";}
                $element.val(programId).trigger("change");
                let message = {msg: "Program afterInit!"};
                dashboard.broker.trigger('afterInit.Program', [message]);
            });

        function formatRepo (repo) {
            if (repo.loading) {
                return repo.text;
            }

            let markup = "<div class='select2-result-repository clearfix'>" +
                "<div class='select2-result-repository__meta'>" +
                "<div class='select2-result-repository__title'> " + repo.text + "</div>";

            if (repo.children) {}
            else {
                markup += "<div class='select2-result-repository__statistics'>" +
                    "<div class='select2-result-repository__stargazers' style='font-size: 0.9em'></div>" +
                    "</div>" +
                    "</div></div>";
            }
            return markup;
        }

        function formatRepoSelection (repo) {
                return repo.text;
        }
    };
    dashboard.programs.setStudyFilter = function (study) {

        console.log("Study Filter:" +  study);
        $(".study_filter").removeClass("active");
        if (study === "_all") { study="all";}
        $("#study_" + study).addClass("active");
        dashboard.programs.selectedStudies = study;
    };
})();