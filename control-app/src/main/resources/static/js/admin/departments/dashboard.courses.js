(function () {
    'use strict';

    dashboard.courses = dashboard.courses || {};

    let coursesDT = null;
    let study_filter = "_all";
    let serialize_form;

    dashboard.courses.selectedCourseId = null;
    dashboard.courses.selectedRowIndex = null;
    dashboard.courses.selectedCourseName = null;

    dashboard.courses.init = function () {

       InitControls();

       dashboard.courses.selectedCourseId = -1;
       dashboard.courses.selectedRowIndex = -1;
       dashboard.courses.selectedCourseName = "all";

       let $coursesDtElem = $("#coursesDataTable");
       coursesDT = $coursesDtElem.DataTable({
            "ajax": dashboard.siteurl + '/api/v1/dt/courses.web/school/_all/department/' + dashboard.departments.selectedDepartmentId + '/study/_all/program/_all',
            "columns": [
                {
                    "className":      'details-control',
                    "orderable":      false,
                    "data":           null,
                    "defaultContent": ''
                },
                {"data": null},
                {"data": "id"},
                {"data": "title"}, //3
                {"data": "teaching"},
                {"data": "department.title"},
                {"data": "scopeId"},
                {"data": "lmsReferences"}, //7
                {"data": "study"},
                {"data": "studyTitle"},
                {"data": "studyProgramId"},
                {"data": "studyProgramTitle"},
                {"data": "department.id"},
                {"data": null},
                {"data": "categories"}, //14
                {"data": "departmentsRelated"}, //15
            ],
            "language": dtLanguageGr,
             order: [[3, 'asc']],
            "dom": '<"top"fl><p>rt<"bottom">p<"clear">',
            "pageLength": 25,
            "pagingType": "full_numbers",
            "aoColumnDefs": [
                {
                    "aTargets": [1],
                    "sortable": false,
                     "sWidth": "20px"
                },
                {
                    "aTargets": [2,5,8,10,12,14,15],
                    "visible": false
                },
                {
                    "aTargets": [3],
                    "mRender": function (data) {
                        return '<h6 class="pb-0 mb-0" style="color: #003476">' + data + '</h6>';
                    }
                },
                {
                    "aTargets": [7],
                    "mRender": function (data) {
                        let dlist = '';
                        $.each(data, function (index, el) {
                            if (el.lmsCode !== '') {
                                dlist += (index > 0 ? ', ' : '') + el.lmsId + '=<b>' + el.lmsCode + '</b>';
                            }
                        });
                        return dlist;
                    }
                },
                {
                    "aTargets": [13],
                    "mData": "id",
                    "sortable": false,
                    "className": "text-right",
                    "mRender": function () {
                        return '<button type="button" title="επεξεργασία στοιχείων" class="btn btn-pill blue-btn-wcag-bgnd-color btn-sm"><i class="fas fa-edit text-white"></i></button>';
                    }
                }
            ],
           "initComplete": set_display_results,
           "rowCallback": function( row, data ) {
               if (data.teaching === 0) {
                   $('td:eq(0)', row).removeClass("details-control");
               }
           },
        });
        coursesDT.on( 'order.dt search.dt', function () {
            coursesDT.column(1, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
                cell.innerHTML = i+1;
            } );
        } ).draw();

        function  set_display_results() {
            $("#count_results").html("" + coursesDT.rows().count() + "");
        }

        $coursesDtElem.on("click", "tbody td", function (e) {
            // get selected row index
            let rowIdx = coursesDT.cell(this).index().row;
            let title =  coursesDT.cell(rowIdx, 2).data();
            $("#rtitle").html(title);
            let $info_box = $("#info_box");
            $info_box.hide();
            var y = e.pageY;
            $info_box.css({'top' : (y-200) + 'px'});
            //$info_box.show();
            e.preventDefault();
        });



       RegisterEvents();

       function RegisterEvents() {

           $coursesDtElem.on("click", "td.details-control", function () {

               var tr = $(this).closest('tr'),
                   row = coursesDT.row(tr);
               if (tr.hasClass('shown')) {
                   $('div.childWrap', row.child()).slideUp( function () {
                       tr.removeClass('shown');
                       row.child().remove();
                   } );
               }
               else {
                   $.when(getStaffTeachingCourse(row.data().id)).then(function (response) {
                       row.child(renderCourseChild(response), 'no-padding').show();
                       tr.addClass('shown');
                       $('div.childWrap', row.child()).slideDown();
                   });
               }
           });

           function getStaffTeachingCourse(id) {
               return $.ajax({
                   url: dashboard.siteurl + '/api/v1/dt/staff.web/course/' + id,
                   type: "GET"
               });
           }
           function renderCourseChild(data) {
               var wrapper = $('<div style="padding:5px 0" class="childWrap"></div>'),
                   result = [];

               $.each(data.data, function (i, v) {
                   let table_row_html = "<tr><td></td><td></td><td></td>";
                   table_row_html += "<td><b>" + v.name + "</b></td>";
                   table_row_html += "<td>" + v.department.title + "</td>";
                   table_row_html += "</tr>";
                   result.push(table_row_html);
               });

               let cTable = '<table class="child-table" style="width: 100%">' +
                   '<thead><tr>' +
                   '<th colspan="2" style="text-align: right">' +
                   '<i class=\"fas fa-level-up-alt fa-rotate-90\"></i>' +
                   '</th>' +
                   '<th>Διδάσκοντες</th><th>Όνομα</th><th>Τμήμα</th></tr></thead>' +
                   '<tbody>' + result.join('') + '</tbody></table>';
               wrapper.append(cTable);

               return wrapper;
           }

            $("#course_study_filter").on('select2:select', function (e) {
               study_filter = e.params.data.id;
               dashboard.courses.FillCoursesDataTable();
            });

            $coursesDtElem.on("dblclick", "tbody td", function (e) {
                // get selected row index
                let table_cell = $(this).closest('td');
                let rowIdx = coursesDT.cell(table_cell).index().row;
                setupCoModal ("edit", "Επεξεργασία Μαθήματος",rowIdx);
                serialize_form = $("#course_form").serialize();
                e.stopPropagation();
            });
            $coursesDtElem.on("click", "tbody button", function () {
                // get selected row index
                let table_cell = $(this).closest('td');
                let rowIdx = coursesDT.cell(table_cell).index().row;
                setupCoModal ("edit", "Επεξεργασία Μαθήματος",rowIdx);
                serialize_form = $("#course_form").serialize();
                console.log("Start:" + serialize_form);
            });

            $("#addOrUpdateCourse").on("click", function() {

                loader.showLoader();

                let courseId        = $("#course_id").val();
                let title           = $("#course_title").val();
                let categories      = $("#course_categories").val();
                let scopeId         = $("#course_scopeId").val();
                let study           = "";
                let studyTitle      = "";
                let studyProgramId  = $("#course_programId").val();
                let studyProgramTitle  = "";
                let departmentId    = $("#course_department").val();
                let department = {
                    id : departmentId,
                    title: "set"
                }
                let rel_values = $("#course_reldep").select2('data');
                let rd_array = [];
                rel_values.forEach(function(dep) {
                    let department = {
                        id: dep.id,
                        title: dep.text,
                        structureType: "DEPARTMENT"
                    }
                    rd_array.push(department);
                });

                //check for errors
                if (title == null || title ==="" || studyProgramId == null || studyProgramId === "" || departmentId == null || departmentId === "") {
                    alertify.alert("<i style='color:red' class='fas fa-exclamation-triangle'></i> Πρόβλημα","Υπάρχουν παραλείψεις στη φόρμα. Διορθώστε τα κενά και προσπαθήστε πάλι");
                    loader.hideLoader();
                }
                else
                {
                    //gather user defined lms codes
                    let lms_array = [];
                    let lms_code_fields = $(".js-lms-tags");
                    lms_code_fields.each(function( ) {
                        let lmsId = $(this).data("target");
                        let lms_codes_array = $(this).val();
                        lms_codes_array.forEach(function(code) {
                            let lms_item = {
                                lmsId: lmsId,
                                lmsCode : code
                            }
                            lms_array.push(lms_item);
                        })
                    });
                    let courseData = {
                        "id"             : courseId,
                        "title"          : title,
                        "categories"     : categories,
                        "department"     : department,
                        "departmentsRelated" : rd_array,
                        "scopeId"        : scopeId,
                        "lmsReferences"  : lms_array,
                        "study"          : study,
                        "studyTitle"     : studyTitle,
                        "studyProgramId" : studyProgramId,
                        "studyProgramTitle" : studyProgramTitle,
                    };
                   postUpdate(courseData);
                }
            });
            $("#deleteCourse").on("click", function (e) {

               let courseId = $("#course_id").val();
               let course_title = $("#course_title").val();

               let msg = '<div class="font-weight-bold">Το Μάθημα "' + course_title + '" Θα διαγραφεί! Είστε σίγουρος;</div>';
               msg += '<div>Προσοχή: Αν έχουν δημιουργηθεί διαλέξεις για το μάθημα ή έχουν προγραμματιστεί διαλέξεις, η διαγραφή θα ακυρωθεί'
               alertify.confirm('<i style="color: red" class="fas fa-trash-alt"></i> Διαγραφή Μαθήματος', msg,
                   function () {
                       loader.showLoader();
                       postDelete(courseId);
                   },
                   function () {
                   }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
               e.preventDefault();
            });
            $("#course_department").on('select2:select', function (e) {
                let data = e.params.data;
                let $course_program_Id = $("#course_programId");
                dashboard.programs.getProgramList("program_default",data.id,$course_program_Id);
            })
            $("#newCourseBt").on("click", function() {
                setupCoModal ("add", "Νέο Μάθημα",-1);
            });
            $("#closeUpdateCourse").on('click',function(){
                let end_serialize = $("#course_form").serialize();
                console.log("END:" + end_serialize);
                if (serialize_form !== end_serialize) {
                    closeEditDialogWarning();
                }
                else {
                    unloadEditForm();
                }
            });
       }
       function unloadEditForm() {
            $("#courses-tab").html("Μαθήματα");
            $("#courses_edit_card").hide();
            $("#courses_card").show();
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
            $("#course_edit_mode").val(action);

            let $course_program_Id  = $("#course_programId");
            let $course_department  = $("#course_department");
            let $course_title       = $("#course_title");

            if (rowIdx !== -1) {
                $(".title_change_warning").show();
                let row_data = coursesDT.row( rowIdx ).data();

                $("#courseModalLabel").html('<div style="font-size: 1.4em">' + row_data.title + '</div><small>' + title + '</small>');
                $("#course_id").val(row_data.id);
                $course_title.val(row_data.title);
                $("#course_categories").val(row_data.categories).trigger("change");
                $("#course_scopeId").val(row_data.scopeId);

                let department_title = row_data.department.title;
                let department_id = row_data.department.id;
                $course_department.val(department_id);
                $("#course_department_title").val(department_title);

                let programId = row_data.studyProgramId;
                dashboard.programs.getProgramList(programId,department_id,$course_program_Id);

                //If multi-department course (exception: department_id already);
                let $rel_deps = $("#course_reldep");
                let depList = row_data.departmentsRelated;
                let depIds = [];
                $.each(depList, function (index, el) {
                    depIds.push(el.id);
                });
                //NOTE: selected, element, school, exception (remove from list)
                dashboard.departments.initializeDepartmentsList(depIds,$rel_deps,"", department_id);
                //TODO: Pre-Selected selected relative departments (if any)

                //load LMSs saved in DB
                let lms_references = row_data.lmsReferences;
                LoadLmsReferencesForCourse(lms_references);
                $("#cs_rowIdx_edited").val(rowIdx);
                $("#deleteCourse").show();
            }
            else {
                $("#courseModalLabel").html(title);
                $(".title_change_warning").hide();

                $("#course_id").val("");
                $course_title.val("");
                $("#course_scopeId").val("");
                $("#course_categories").val("").trigger("change");

                let department_title = dashboard.departments.selectedDepartmentName
                let department_id = dashboard.departments.selectedDepartmentId;
                $course_department.val(department_id);
                $("#course_department_title").val(department_title);

                let programId = dashboard.programs.selectedProgramId;
                dashboard.programs.getProgramList(programId,department_id,$course_program_Id);

                //If multi-department course (exception: department_id already);
                let $rel_deps = $("#course_reldep");
                dashboard.departments.initializeDepartmentsList("",$rel_deps,"", department_id);

                if (programId == null || programId === -1 || programId === "-1") {programId = "program_default"}
                $course_program_Id.val(programId).trigger("change");

                //Empty select2 fields before loading modal...
                for (let l=0; l<5; l++) {
                    let $s2_el = $("#lms\\.entry\\[" + l + "\\]");
                    $s2_el.empty();
                }
                $("#cs_rowIdx_edited").val("");
                $("#deleteCourse").hide();
            }
            $course_department.prop('disabled', true);
            $("#courses-tab").html("Επεξεργασία Μαθήματος [<b>" + $course_title.val() + "</b>]");
            $("#courses_card").hide();
            $("#courses_edit_card").show();

       }
       function postUpdate(courseData) {
            $.ajax({
                type:        "POST",
                url: 		  dashboard.siteurl + '/api/v1/courses/save',
                contentType: "application/json; charset=utf-8",
                data: 		  JSON.stringify(courseData),
                async:		  true,
                success: function(data){
                    //Highlight edited/added row after ajax refresh
                    coursesDT.ajax.reload( function () {
                            dashboard.courses.enableCourseDtByCourseId(data);
                            let message = {msg: "Course Edited"};
                            dashboard.broker.trigger("courseItemClick", [message]);
                            loader.hideLoader();
                    });
                    $("#course_id").val(data); //!important
                    $("#courseModalLabel").text($("#course_title").val());
                    serialize_form = $("#course_form").serialize();
                    alertify.notify("Το Μάθημα αποθηκεύτηκε με επιτυχία", "success");
                },
                error: function ()  {
                    loader.hideLoader();
                    alertify.alert('Error-Update-Course');
                }
            });
        } //postUpdate
       function postDelete(courseId) {

            $.ajax({
                type:        "DELETE",
                url: 		  dashboard.siteurl + '/api/v1/courses/delete/' + courseId,
                contentType: "application/json; charset=utf-8",
                async:		  true,
                success: function(){
                    //Highlight edited/added row after ajax refresh
                    coursesDT.ajax.reload( function () {
                        dashboard.courses.clearCoursesDtSelection();
                        let message = {msg: "Course Removed"};
                        dashboard.broker.trigger("courseItemClick", [message]);
                        loader.hideLoader();
                    });
                    $("#courseModal").modal('hide');
                    alertify.notify("Το Μάθημα διαγράφηκε με επιτυχία", "success");
                },
                error: function (data)  {
                    let info = "Άγνωστο Σφάλμα";
                    let msg = data.responseText;
                    if (msg === "_FORBIDDEN_LECTURES") {
                        info = "<div class='mt-2'>Το Μάθημα δεν μπορεί να διαγραφεί! Βρέθηκαν Διαλέξεις</div>";
                    }
                    else if (msg === "_FORBIDDEN_SCHEDULER") {
                        info = "<div class='mt-2'>Το Μάθημα δεν μπορεί να διαγραφεί! Βρέθηκαν προγραμματισμένες Διαλέξεις</div>";
                    }
                    else if (msg === "_NOT_FOUND") {
                        info = "<div class='mt-2'>Το Μάθημα δεν βρέθηκε</div>";
                    }
                   // $("#courseModal").modal('hide');
                    loader.hideLoader();
                    alertify.alert('Σφάλμα', '<i style="color: red" class="fas fa-exclamation-circle"></i> ' + msg + info);
                }
            });
        }
       function InitControls() {
           //Categories
           $(".js-category-tags").select2({
               maximumSelectionLength: 3
           });
            //lms select2 fields on Course Modal
            $(".js-lms-tags").select2({
                tags: true,
                multiple: true,
                maximumSelectionLength: 5
            });
           $("#course_study_filter").select2({});

           let $course_program_Id  = $("#course_programId");
           let programId = "";
           let department_id = "";
           dashboard.programs.getProgramList(programId,department_id,$course_program_Id);
        }

       function LoadLmsReferencesForCourse(lmsReferences) {
           for (let l=0; l<5; l++) {
               let $s2_el  = $("#lms\\.entry\\[" + l + "\\]");
               $s2_el.empty();
               let lms_id = $s2_el.data("target");
               lmsReferences.forEach(function(item) {
                   let lms_url = item.lmsId;
                   let lms_codes= item.lmsCode;
                   if (lms_id === lms_url) {
                       if (lms_codes !== "") {
                           let newOption = new Option(lms_codes, lms_codes, false, true);
                           $s2_el.append(newOption).trigger('change');
                       }
                   }
               });
           }
       }
    }; // courses DataTable Init

    dashboard.courses.enableCourseDtByRowIndex = function (rowIdx) {

            //clear current selections
            dashboard.courses.clearCoursesDtSelection();

            //highlight selected row
            $(coursesDT.row(rowIdx).nodes()).addClass('row_selected');

            dashboard.courses.selectedRowIndex = rowIdx;
            dashboard.courses.selectedCourseId = coursesDT.cell(rowIdx, 0).data();
            dashboard.courses.selectedCourseName = coursesDT.cell(rowIdx, 1).data();
    };

    dashboard.courses.enableCourseDtByCourseId = function (courseId) {
        console.log("Search and enable course with id:" + courseId);
        if (courseId !== undefined && courseId.toString() !== "-1") {
            coursesDT.column(0, {order: 'applied'}).nodes().each(function (cell, i) {
                var indexCourseId = coursesDT.cell(i, 0).data();
                if (indexCourseId === courseId) {
                    dashboard.courses.enableCourseDtByRowIndex(i);
                    console.log("enable department in row:" + i);
                }
            });
        }
        else {
            dashboard.courses.clearCoursesDtSelection();
        }
    };

    dashboard.courses.clearCoursesDtSelection = function () {

        //un-highlight selected rows
        $(coursesDT.rows().nodes()).removeClass('row_selected');

        dashboard.courses.selectedCourseId = -1;
        dashboard.courses.selectedRowIndex = -1;
        dashboard.courses.selectedCourseName = "all";
    };

    dashboard.courses.FillCoursesDataTable = function () {

        let schoolId = "_all";
        let departmentId = dashboard.departments.selectedDepartmentId;
        let studyId = $("#course_study_filter").val();
        let programId = "_all";
        let ajaxUrl = dashboard.siteurl + '/api/v1/dt/courses.web/school/' + schoolId + '/department/' + departmentId + '/study/' + studyId + '/program/' + programId;

        coursesDT.ajax.url(ajaxUrl);
        coursesDT.ajax.reload( function() {
            dashboard.courses.updateCoursesHeader();
        });

    };

    dashboard.courses.updateCoursesHeader = function () {

        $("#count_results").html("" + coursesDT.rows().count() + "");
        let $depFilterName = $("#filtereddpname02");
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

    dashboard.courses.getDepartmentIdFromRowIdx = function (rowIdx) {
        console.log("Get department from row:" + rowIdx);
        return coursesDT.cell(rowIdx, 9).data();        // return departmentId
    };

    dashboard.courses.getStudyList = function (studyId, $element) {

        $element.select2({
            placeholder: 'Επιλέξτε Σπουδές'
        });

        $element.val(studyId).trigger("change");
        let message = {msg: "Study afterInit!"};
        dashboard.broker.trigger('afterInit.Study', [message]);
    };

    dashboard.courses.getPeriodList = function (periodId, $element) {

        // WHen reading Client List -->
        $.ajax({
            url: dashboard.app_path + '/api/v1/filters/PeriodList.web.s3',
            cache: false
        })
            .done(function( data ) {

                $element.select2({
                    placeholder: 'Select Period',
                    width: 'style', // need to override the changed default
                    data : data.results,
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });

                $element.val(periodId).trigger("change");
                let message = {msg: "Period afterInit!"};
                dashboard.broker.trigger('afterInit.Period', [message]);
            });

        function formatRepo (repo) {
            if (repo.loading) {
                return repo.text;
            }

            return   "<div class='select2-result-repository clearfix'>" +
                "<div class='select2-result-repository__meta'>" +
                "<div class=''>" + repo.text + "</div>" +
                "</div>" +"" +
                "</div>";
        }

        function formatRepoSelection (repo) {
            if (!repo.text.startsWith("Select"))
                return repo.text;
            else
                return repo.text;
        }
    };
    dashboard.courses.getCourseInfo= function(courseId) {

        $.ajax({
            url:  dashboard.siteUrl + '/api/v1/course/' + courseId,
            cache: false
        })
            .done(function( data ) {
                let course_info_html="";
                course_info_html += "<table style='border: none;width: 100%'>";
                course_info_html += "<tr><th style='width: 40%;font-weight: 550;margin-bottom: 5px;vertical-align: top'>Τίτλος</th><td>" + data.title + "</td></tr>";
                course_info_html += "<tr><th style='width: 40%;font-weight: 550;margin-bottom: 5px;vertical-align: top'>Τμήμα</th><td>" + data.department.title + "</td></tr>";
                course_info_html += "<tr><th style='width: 40%;font-weight: 550;margin-bottom: 5px;vertical-align: top'>Εξάμηνο</th><td>" + semester[data.semester] + "</td></tr>";
                course_info_html += "<tr><th style='width: 40%;font-weight: 550;margin-bottom: 5px;vertical-align: top'>Κωδικός Γραμματείας</th><td>";
                if (data.scopeId !== "") {
                    course_info_html += data.scopeId + "</td></tr>";
                }
                else {
                    course_info_html += " - ";
                }
                course_info_html += "<tr><th style='width: 40%;font-weight: 550;margin-bottom: 5px;vertical-align: top'>Κωδικοί LMS</th><td>";
                let lms_codes="";
                $.each(data.lmsReferences, function (index, el) {
                    lms_codes += (index>0 ? ', ' : '') + el.lmsId + ' [' + el.lmsCode + ']';
                });
                course_info_html += lms_codes + "</td></tr>";
                course_info_html += "</table>";
                $("#course_info_panel").html(course_info_html);
            });
    }
})();