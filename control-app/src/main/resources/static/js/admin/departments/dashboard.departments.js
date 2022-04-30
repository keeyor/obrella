(function () {
    'use strict';

    dashboard.departments = dashboard.departments || {};

    let departmentsDT = null;

    dashboard.departments.selectedDepartmentId = null;
    dashboard.departments.selectedRowIndex = null;
    dashboard.departments.selectedDepartmentName = null;

    dashboard.departments.init = function () {

        dashboard.departments.selectedDepartmentId = -1;
        dashboard.departments.selectedRowIndex = -1;
        dashboard.departments.selectedDepartmentName = "all";

        let $departmentsDtElem = $("#departmentsDataTable");

        departmentsDT = $departmentsDtElem.DataTable({
            "ajax":  dashboard.siteurl + '/api/v1/dt/departments.web',
            "columns": [
                {"data": "id"},
                {"data": "title"},
                {"data": "identity"},
                {"data": "url"},
                {"data": "logoUrl"},
                {"data": "password"},
                {"data": "schoolId"},
                {"data": "classrooms"},
                {"data": "id"}
            ],
            "language": dtLanguageGr,
            order: [[1, 'asc']],
            "aoColumnDefs": [
                {
                    "aTargets": [0],
                    "mData": "id",
                    "sortable": false,
                    "mRender": function () {
                            return '<i style="color:silver" class="fas fa-check-circle"></i>';
                    }
                },
                {
                    "aTargets": [3,4,5,6,7],
                    "visible": false,
                    "sWidth": "0px",
                },
                {
                    "aTargets": [8],
                    "mData": "id",
                    "sortable": false,
                    "className": "text-right",
                    "mRender": function () {
                            return '<button type="button" class="btn btn-outline-secondary btn-sm" disabled><i class="fas fa-pencil-alt"></i> </button>';
                    }
                }
            ]

        });

        RegisterEvents();

        function RegisterEvents() {

            $departmentsDtElem.on("click", "tbody button", function (e) {

                // get selected row index
                let table_cell = $(this).closest('td');
                let rowIdx = departmentsDT.cell(table_cell).index().row;

                console.log("Edit button on row:" + rowIdx);

                setupDpModal ("edit", "Τροποποίηση Τμήματος",rowIdx);
                e.stopPropagation(); // Important! otherwise the 'tbody td' click event below is executed too!!!
            });

            $departmentsDtElem.on("click", "tbody td", function (e) {

                // get selected row index
                let rowIdx = departmentsDT.cell(this).index().row;

                if (dashboard.departments.selectedRowIndex !== rowIdx) {
                    dashboard.departments.enableDepartmentDtByRowIndex(rowIdx);
                }
                else {
                    dashboard.departments.clearDepartmentsDtSelection();
                }

                let message = {msg: "Department selection changed!"};
                dashboard.broker.trigger('departmentItemClick', [message]);
                e.preventDefault();
            });

            $("#addOrUpdateDepartment").on("click", function() {

                let departmentId    = $("#department_id").val();
                let title           = $("#department_title").val();
                let identity        = $("#department_identity").val();
                let url             = $("#department_url").val();
                let logoUrl         = $("#department_logourl").val();
                let password        = $("#department_password").val();
                let schoolId        = $("#department_school").val();
                let institutionId   = $("#institution_id").val()

                if ((title == null || title === "") || (identity == null || identity === "") ||
                    (schoolId == null || schoolId === "") || (institutionId == null || institutionId === "")) {
                    alertify.alert("<i style='color:red' class='fas fa-exclamation-triangle'></i> Πρόβλημα","Υπάρχουν παραλείψεις στη φόρμα. Διορθώστε τα κενά και προσπαθήστε πάλι")
                }
                else {
                    let departmentData = {
                        "id": departmentId,
                        "title": title,
                        "identity": identity,
                        "url": url,
                        "logoUrl": logoUrl,
                        "password": password,
                        "schoolId": schoolId,
                        "institutionId": institutionId,
                        "classrooms": null
                    };
                    postUpdate(departmentData);
                }
            });

            $("#deleteDepartment").on("click", function (e) {

                let departmentId = $("#department_id").val();
                let department_title = $("#department_title").val();

                let msg = '<div class="font-weight-bold">Το Τμήμα "' + department_title + '" Θα διαγραφεί! Είστε σίγουρος;</div>';
                msg += '<div>Προσοχή: Αν έχουν δηλωθεί Μαθήματα, Προσωπικό ή Προγράμματα Σπουδών για το Τμήμα, η διαγραφή θα ακυρωθεί'
                alertify.confirm('<i style="color: red" class="fas fa-trash-alt"></i> Διαγραφή Τμήματος', msg,
                    function () {
                        postDelete(departmentId);
                    },
                    function () {
                    }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});

                e.preventDefault();
            });

            let $DepartmentModalButton = $(".departmentModalButton");
            $DepartmentModalButton.on("click", function() {
                setupDpModal ("add", "Προσθήκη νέου Τμήματος",-1);
                return false;
            });
        }
        function setupDpModal (action, title, rowIdx) {

            $("#department_edit_mode").val(action);
            $("#departmentModalLabel").html(title);

            let school_id;
            if (rowIdx !== -1) {
                let row_data = departmentsDT.row( rowIdx ).data();
                $("#department_id").val(row_data.id);
                $("#department_title").val(row_data.title);
                $("#department_identity").val(row_data.identity);
                $("#department_url").val(row_data.url);
                $("#department_password").val(row_data.password);
                school_id = row_data.schoolId;
                $("#dp_rowIdx_edited").val(rowIdx);
            }
            else {
                $("#department_id").val("");
                $("#department_title").val("");
                $("#department_identity").val("");
                $("#department_url").val("");
                $("#department_password").val("");
                school_id = dashboard.schools.selectedSchoolId;
                $("#dp_rowIdx_edited").val("");
            }
            //setup school list in modal
            let $department_school = $("#department_school");
            dashboard.schools.initializeSchoolList(school_id,$department_school);
            if (rowIdx !== -1) {
                $department_school.prop('disabled', true);
            }
            else {
                $department_school.prop('disabled', false);
            }
            //show modal
            $('#departmentModal').modal('show');
        }

        function postUpdate(departmentData) {

            $.ajax({
                type:        "POST",
                url: 		  dashboard.siteurl + '/api/v1/department/save',
                contentType: "application/json; charset=utf-8",
                data: 		  JSON.stringify(departmentData),
                async:		  true,
                success: function(data){
                    //Highlight edited/added row after ajax refresh
                    departmentsDT.ajax.reload( function () {
                            dashboard.departments.enableDepartmentDtByDepartmentId(data);
                            let message = {msg: "Department Edited"};
                            dashboard.broker.trigger("departmentItemClick", [message]);
                   });

                   $("#departmentModal").modal('hide');
                   alertify.notify("Το Τμήμα αποθηκεύτηκε με επιτυχία", "success");
                },
                error: function (data)  {
                    let info = "Άγνωστο Σφάλμα";
                    let msg = data.responseText;
                    if (msg === "_DUPLICATE_IDENTITY") {
                        info = "<div class='mt-2'>Τό σύμβολο χρησιμοποιείτε! Πληκτρολογήστε διαφορετικό σύμβολο και προσπαθήστε ξανά.</div>";
                    }
                    alertify.alert('Σφάλμα', '<i style="color: red" class="fas fa-exclamation-circle"></i> ' + msg + info);
                }
            });
        } //

        function postDelete(deparmentId) {

            $.ajax({
                type:        "DELETE",
                url: 		  dashboard.siteurl + '/api/v1/department/delete/' + deparmentId,
                contentType: "application/json; charset=utf-8",
                async:		  true,
                success: function(){
                    //Highlight edited/added row after ajax refresh
                    departmentsDT.ajax.reload( function () {
                        dashboard.departments.clearDepartmentsDtSelection();
                        let message = {msg: "Department Removed"};
                        dashboard.broker.trigger("departmentItemClick", [message]);
                    });
                    $("#departmentModal").modal('hide');
                    alertify.notify("Το Τμήμα διαγράφηκε με επιτυχία", "success");
                },
                error: function (data)  {
                    let info = "Άγνωστο Σφάλμα";
                    let msg = data.responseText;
                    if (msg === "_FORBIDDEN_STUDYPROGRAMS") {
                        info = "<div class='mt-2'>Το Τμήμα δεν μπορεί να διαγραφεί! Έχουν δηλωθεί Προγράμματα Σπουδών</div>";
                    }
                    else if (msg === "_FORBIDDEN_COURSES") {
                        info = "<div class='mt-2'>Το Τμήμα δεν μπορεί να διαγραφεί! Έχουν δηλωθεί Μαθήματα</div>";
                    }
                    else if (msg === "_FORBIDDEN_STAFF") {
                        info = "<div class='mt-2'>Το Τμήμα δεν μπορεί να διαγραφεί! Έχουν δηλωθεί Καθηγητές</div>";
                    }
                    else if (msg === "_NOT_FOUND") {
                        info = "<div class='mt-2'>Το Τμήμα δεν βρέθηκε</div>";
                    }
                    $("#departmentModal").modal('hide');
                    alertify.alert('Σφάλμα', '<i style="color: red" class="fas fa-exclamation-circle"></i> ' + msg + info);
                }
            });
        }

    };// departments Init

    dashboard.departments.initializeDepartmentsList = function (departmentId, $elem, schoolId, exceptionId) {

        // WHen reading Client List -->
        if (schoolId === '') { schoolId = 'dummy';}
        $.ajax({
            url: dashboard.siteurl + '/api/v1/s2/departments.web/school/' + schoolId,
            cache: false
        })
            .done(function( data ) {

                $elem.select2({
                    placeholder: 'Επιλέξτε Τμήμα',
                    dropdownParent: $elem.parent(),
                    width: 'style', // need to override the changed default
                    data : data.results,
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });

                $elem.val(departmentId).trigger("change");
                let message = {msg: "Department afterInit!"};
                dashboard.broker.trigger('afterInit.Department', [message]);
            });

        function formatRepo (repo) {

            if (repo.id === exceptionId) {
                return null;
            }

            if (repo.loading) {
                return repo.text;
            }

            var markup = "<div class='select2-result-repository clearfix'>" +
                "<div class='select2-result-repository__meta'>" +
                "<div class='select2-result-repository__title'>" + repo.text + "</div>";

            if (repo.children) {

            }
            else {
                markup += "<div class='select2-result-repository__statistics'>" +
                    "</div>" +
                    "</div></div>";
            }
            return markup;
        }

        function formatRepoSelection (repo) {
                return repo.text;
        }

    };
    dashboard.departments.clearDepartmentsDtSelection = function () {

        //un-highlight selected rows
        $(departmentsDT.rows().nodes()).removeClass('row_selected');

        //disαble first column icon
        departmentsDT.column(0, {order: 'applied'}).nodes().each(function (cell, i) {
            cell.innerHTML = '<i style="color:silver" class="fas fa-check-circle"></i>';
        });

        //disable edit buttons
        let lastColumnIndex = departmentsDT.columns().count();
        departmentsDT.column(lastColumnIndex - 1, {
            order: 'applied'
        }).nodes().each(function (cell, i) {
            cell.innerHTML = '<button type="button" class="btn btn-outline-secondary btn-sm" disabled><i class="fas fa-pencil-alt"></i> </button>';
        });

        dashboard.departments.selectedDepartmentId = -1;
        dashboard.departments.selectedRowIndex = -1;
        dashboard.departments.selectedDepartmentName = "all";
        // Display the rowIdx row in the table
        //departmentsDT.row(0).show().draw(false);
    };
    dashboard.departments.DtFillDepartmentsBySchool = function (schoolId) {

        console.log("Department DT re-filled");
        let ajaxUrl = dashboard.siteurl + '/api/v1/dt/departments.web';

        if (schoolId.toString() !== "-1") {
            ajaxUrl += "/school/" + schoolId.toString();
        }

        departmentsDT.ajax.url(ajaxUrl);
        departmentsDT.ajax.reload();
        dashboard.departments.updateHeader(schoolId);
        dashboard.departments.selectedDepartmentId = -1;
        dashboard.departments.selectedRowIndex = -1;
        dashboard.departments.selectedDepartmentName = "all";
    };
    dashboard.departments.enableDepartmentDtByRowIndex = function (rowIdx) {

        console.log("enable Department:" + rowIdx);

            //clear current selections
            dashboard.departments.clearDepartmentsDtSelection();
            //highlight selected row
            $(departmentsDT.row(rowIdx).nodes()).addClass('row_selected');

            let node;
            //enable first column icon
            node = departmentsDT.cell(rowIdx, 0).node();
            node.innerHTML = '<i style="color:green" class="fas fa-check-circle"></i>';

            //enable edit button of selected row
            var lastColumnIndex = departmentsDT.columns().count();
            node = departmentsDT.cell(rowIdx, lastColumnIndex - 1).node();
            node.innerHTML = '<button type="button" class="btn btn-primary btn-sm">' +
                '<i class="fas fa-pencil-alt"></i> ' +
                '</button>';

            dashboard.departments.selectedRowIndex = rowIdx;
            dashboard.departments.selectedDepartmentId = departmentsDT.cell(rowIdx, 0).data();
            dashboard.departments.selectedDepartmentName = departmentsDT.cell(rowIdx, 1).data();

            // Display the rowIdx row in the table
            departmentsDT.row(rowIdx).show().draw(false);

    };
    dashboard.departments.getSchoolIdFromRowIdx = function (rowIdx) {
        console.log("Get school from row:" + rowIdx);
        return departmentsDT.cell(rowIdx, 6).data();
    };
    dashboard.departments.updateHeader = function (schoolId) {

        let $schoolFilterName = $("#filteredschoolname");

        if (schoolId !== -1) {
            let dep_tail_html = " Σχολή " +   dashboard.schools.selectedSchoolName;//
            $schoolFilterName.html(dep_tail_html);
        }
        else {
            $schoolFilterName.html(dashboard.institutions.institutionName);
        }
    };
    dashboard.departments.enableDepartmentDtByDepartmentId = function (departmentId) {

        console.log("Search and enable department with id:" + departmentId);
        if (departmentId !== undefined && departmentId.toString() !== "-1") {
            departmentsDT.column(0, {order: 'applied'}).nodes().each(function (cell, i) {
                var indexDepartmentId = departmentsDT.cell(i, 0).data();
                if (indexDepartmentId === departmentId) {
                    dashboard.departments.enableDepartmentDtByRowIndex(i);
                }
            });
        }
        else {
            dashboard.departments.clearDepartmentsDtSelection();
        }
    }

    dashboard.departments.loadDepartmentsOnSearchBar = function ($elem) {

        $elem.on('click','a.dropdown-toggle', function() {
            if (!$(this).next().hasClass('show')) {
                $(this).parents('.dropdown-menu').first().find('.show').removeClass('show');
            }
            var $subMenu = $(this).next('.dropdown-menu');
            $subMenu.toggleClass('show');

            $(this).parents('li.nav-item.dropdown.show').on('hidden.bs.dropdown', function() {
                $('.dropdown-submenu .show').removeClass('show');
            });
            return false;
        });

        let siteUrl          = dashboard.siteurl;
        let queryParams = new URLSearchParams(window.location.search);

        let url = siteUrl + '/api/v2/s2/departments.web/authorized/data';
        let html = '';
        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.results, function (index, element) {
                    html += '<li class="dropdown-submenu">';
                    html += '<a class="dropdown-item dropdown-toggle" href="#">' + element.text + '</a>';
                    html += '<ul class="dropdown-menu">';
                    $.each(element.children, function (index1, el) {
                        queryParams.set("id", el.id);
                        html +=  '<li><a class="dropdown-item" href="?' + queryParams + '">' + el.text +'</a></li>';
                    });
                    html += '</ul></li>';
                });
                $elem.append(html);
            }
        });
    }
})();