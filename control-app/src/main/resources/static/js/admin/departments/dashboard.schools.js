(function () {
    'use strict';

    dashboard.schools = dashboard.schools || {};

    let schoolsDT = null;

    dashboard.schools.selectedSchoolId = null;
    dashboard.schools.selectedRowIndex = null;
    dashboard.schools.selectedSchoolName = null;

    dashboard.schools.init = function () {

        dashboard.schools.selectedSchoolId = -1;
        dashboard.schools.selectedRowIndex = -1;
        dashboard.schools.selectedSchoolName = "all";

        let $schoolsDtElem = $("#schoolsDataTable");
        schoolsDT = $schoolsDtElem.DataTable({

            "ajax": dashboard.siteurl + '/api/v1/dt/schools.web',
            "columns": [
                {"data": "id"},
                {"data": "title"},
                {"data": "identity"},
                {"data": "id"}
            ],
            "language": {
                "search": "Αναζήτηση",
                "lengthMenu": "Εμφάνιση _MENU_ εγγραφών",
                "emptyTable": "Δεν βρέθηκαν εγγραφές",
                "zeroRecords": "Η αναζήτηση δεν βρήκε εγγραφές",
                "sInfo": "Εμφάνιση από _START_ έως _END_ από _TOTAL_",
                "infoEmpty": "Εμφάνιση από 0 σε 0 από 0 εγγραφές",
                "infoFiltered": "(Φίλτρο από _MAX_ συνολικές εγγραφές)",
                "loadingRecords": "Φόρτωση...",
                "processing": "Επεξεργασία...",
                "oPaginate": {
                    "sNext": "<i class='fas fa-angle-right'></i>",
                    "sPrevious": "<i class='fas fa-angle-left'></i>"
                },
                "aria": {
                    "sortAscending":  ": αύξουσα ταξινόμηση",
                    "sortDescending": ": φθίνουσα ταξινόμηση"
                }
            },
            order: [[1, 'asc']],
            stateSave: true,
            "aoColumnDefs": [
                {
                    "aTargets": [0],
                    "mData": "id",
                    "mRender": function () {
                        return '<i style="color:silver" class="fas fa-check-circle"></i>';
                    },
                    "sortable": false
                },
                {
                    "aTargets": [3],
                    "sortable": false,
                    "className": "text-right",
                    "mRender": function () {
                        return '<button type="button" class="btn btn-outline-secondary btn-sm"><i class="fas fa-pencil-alt"></i> </button>';
                    }
                }
            ]
        });


        RegisterEvents();

        function RegisterEvents() {

            $schoolsDtElem.on("click", "tbody button", function (e) {
                // get selected row index
                let table_cell = $(this).closest('td');
                let rowIdx = schoolsDT.cell(table_cell).index().row;

                setupScModal("edit", "Τροποποίηση Σχολής", rowIdx);
                e.stopPropagation(); // Important! otherwise the 'tbody td' click event below is executed too!!!
            });

            $schoolsDtElem.on("click", "tbody td", function (e) {
                // get selected row index
                let rowIdx = schoolsDT.cell(this).index().row;

                if (dashboard.schools.selectedRowIndex !== rowIdx) {
                   dashboard.schools.enableSchoolDtByRowIndex(rowIdx);
                } else {
                   dashboard.schools.clearSchoolsDtSelection();
                }
                let message = {msg: "School selection changed!"};
                dashboard.broker.trigger('schoolItemClick', [message]);
                e.preventDefault();
            });

            $("#addOrUpdateSchool").on("click", function () {

                let schoolId = $("#school_id").val();
                let school_identity = $("#school_identity").val();
                let school_title = $("#school_title").val();

                if (school_identity == null || school_identity === "" || school_title == null || school_title === "") {
                    alertify.alert("<i style='color:red' class='fas fa-exclamation-triangle'></i> Πρόβλημα", "Υπάρχουν παραλείψεις στη φόρμα. Διορθώστε τα κενά και προσπαθήστε πάλι");
                } else {
                    let schoolData = {
                        "id": schoolId,
                        "identity": school_identity,
                        "title": school_title,
                    };
                    postUpdate(schoolData);
                }
            });
            $("#deleteSchool").on("click", function (e) {

                let schoolId = $("#school_id").val();
                let school_title = $("#school_title").val();

                let msg = '<div class="font-weight-bold">Η Σχολή "' + school_title + '" Θα διαγραφεί! Είστε σίγουρος;</div>';
                msg += '<div>Προσοχή: Αν έχουν δηλωθεί Τμήματα για τη Σχολή, η διαγραφή θα ακυρωθεί'
                alertify.confirm('<i style="color: red" class="fas fa-trash-alt"></i> Διαγραφή Σχολής', msg,
                    function () {
                            postDelete(schoolId);
                    },
                    function () {
                    }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});

                e.preventDefault();
            });

            let $SchoolModalButton = $(".schoolModalButton");
            $SchoolModalButton.on("click", function () {
                setupScModal("add", "Προσθήκη νέας Σχολής", -1);
                return false;
            });
        }
        function setupScModal (action, title, rowIdx) {

            $("#dschool_edit_mode").val(action);
            $("#schoolModalLabel").html(title);

            if (rowIdx !== -1) {
                let row_data = schoolsDT.row( rowIdx ).data();
                $("#school_id").val(row_data.id);
                $("#school_title").val(row_data.title);
                $("#school_identity").val(row_data.identity);
                $("#sc_rowIdx_edited").val(rowIdx);
            }
            else {
                $("#school_id").val("");
                $("#school_title").val("");
                $("#school_identity").val("");
                $("#sc_rowIdx_edited").val("");
            }

            $('#schoolModal').modal('show');
        }

        function postUpdate(schoolData) {

            $.ajax({
                type:        "POST",
                url: 		  dashboard.siteurl + '/api/v1/school/save',
                contentType: "application/json; charset=utf-8",
                data: 		  JSON.stringify(schoolData),
                async:		  true,
                success: function(data){
                    //Highlight edited/added row after ajax refresh
                    schoolsDT.ajax.reload( function () {
                            dashboard.schools.enableSchoolDtBySchoolId(data);
                            let message = {msg: "School Edited"};
                            dashboard.broker.trigger("schoolItemClick", [message]);
                    });

                    $("#schoolModal").modal('hide');
                    alertify.notify("Η Σχολή αποθηκεύτηκε με επιτυχία", "success");
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
        }

        function postDelete(schoolId) {

            $.ajax({
                type:        "DELETE",
                url: 		  dashboard.siteurl + '/api/v1/school/delete/' + schoolId,
                contentType: "application/json; charset=utf-8",
                async:		  true,
                success: function(){
                    //Highlight edited/added row after ajax refresh
                    schoolsDT.ajax.reload( function () {
                        dashboard.schools.clearSchoolsDtSelection();
                        let message = {msg: "School Removed"};
                        dashboard.broker.trigger("schoolItemClick", [message]);
                    });
                    $("#schoolModal").modal('hide');
                    alertify.notify("Η Σχολή διαγράφηκε με επιτυχία", "success");
                },
                error: function (data)  {
                    let info = "Άγνωστο Σφάλμα";
                    let msg = data.responseText;
                    if (msg === "_FORBIDDEN") {
                        info = "<div class='mt-2'>Η Σχολή δεν μπορεί να διαγραφεί! Έχουν δηλωθεί Τμήματα</div>";
                    }
                    else if (msg === "_NOT_FOUND") {
                        info = "<div class='mt-2'>Η Σχολή δεν βρέθηκε</div>";
                    }
                    $("#schoolModal").modal('hide');
                    alertify.alert('Σφάλμα', '<i style="color: red" class="fas fa-exclamation-circle"></i> ' + msg + info);
                }
            });
        }
    };   //schools init

    dashboard.schools.initializeSchoolList = function (schoolId, $element) {
        $.ajax({
            url: dashboard.siteurl + '/api/v1/s2/schools.web',
            cache: false
        })
            .done(function( data ) {

                $element.select2({
                    placeholder: 'Επιλέξτε Σχολή',
                    width: 'style', // need to override the changed default
                    data : data.results,
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });

                if (schoolId !== "-1") {
                    $element.val(schoolId).trigger("change");
                }
                let message = {msg: "School afterInit!"};
                dashboard.broker.trigger('afterInit.School', [message]);
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
                return repo.text;
        }
    };
    dashboard.schools.clearSchoolsDtSelection = function () {

        //un-highlight selected rows
        $(schoolsDT.rows().nodes()).removeClass('row_selected');

        //disble first column icon
        schoolsDT.column(0, {order: 'applied'}).nodes().each(function (cell, i) {
            cell.innerHTML = '<i style="color:silver" class="fas fa-check-circle"></i>';
        });

        //disable edit buttons
        let lastColumnIndex = schoolsDT.columns().count();
        schoolsDT.column(lastColumnIndex - 1, {order: 'applied'}).nodes().each(function (cell, i) {
            cell.innerHTML = '<button type="button" class="btn btn-outline-secondary btn-sm" disabled><i class="fas fa-pencil-alt"></i> </button>';
        });

        dashboard.schools.selectedSchoolId = -1;
        dashboard.schools.selectedRowIndex = -1;
        dashboard.schools.selectedSchoolName = "all";
        // Display the rowIdx row in the table
       // schoolsDT.row(0).show().draw(false);
    };
    dashboard.schools.enableSchoolDtByRowIndex = function (rowIdx) {

        console.log("enable School:" + rowIdx);

        //Clear previous Selection
        dashboard.schools.clearSchoolsDtSelection();

        //highlight selected row
        $(schoolsDT.row(rowIdx).nodes()).addClass('row_selected');

        var node;
        //enable first column icon
        node = schoolsDT.cell(rowIdx, 0).node();
        node.innerHTML = '<i style="color:green" class="fas fa-check-circle"></i>';

        //enable edit button of selected row
        var lastColumnIndex = schoolsDT.columns().count();
        node = schoolsDT.cell(rowIdx, lastColumnIndex - 1).node();
        node.innerHTML = '<button type="button" class="btn btn-primary btn-sm"><i class="fas fa-pencil-alt"></i></button>';

        dashboard.schools.selectedRowIndex = rowIdx;
        dashboard.schools.selectedSchoolId = schoolsDT.cell(rowIdx, 0).data();
        dashboard.schools.selectedSchoolName = schoolsDT.cell(rowIdx, 1).data();
        // Display the rowIdx row in the table
        schoolsDT.row(rowIdx).show().draw(false);

    };
    dashboard.schools.enableSchoolDtBySchoolId = function (schoolId) {
        if (schoolId !== undefined && schoolId.toString() !== "-1") {
            schoolsDT.column(2, {order: 'applied'}).nodes().each(function (cell, i) {
                let indexSchoolId = schoolsDT.cell(i, 0).data();
                if (indexSchoolId === schoolId) {
                    dashboard.schools.enableSchoolDtByRowIndex(i);
                }
            });
        }
        else {
            dashboard.schools.clearSchoolsDtSelection();
        }
    }

})();
