(function () {
    'use strict';

    dashboard.staffmembers = dashboard.staffmembers || {};

    let siteUrl;
    let staffDT;
    let staffName;
    let staffId;
    let StaffMembersDT;

    dashboard.staffmembers.init = function () {
        siteUrl = dashboard.siteUrl;
        //staffId = $("#staffId").val();
    };

    dashboard.staffmembers.loadStaffsDT = function () {

        let url;
        let departmentId = $("#departmentId").val();
        let courseId = $("#courseId").val();
        if (courseId === '' && departmentId !== '') {
            url = siteUrl + '/api/v1/dt/staff.web/teaching/department/' + departmentId;
        }
        else if (courseId !== '') {
            url = siteUrl + '/api/v1/dt/staff.web/course/' + courseId;
        }
        else {
            url = '';
        }

        let $staff_dt_el = $("#staff_dt");
        staffDT = $staff_dt_el.DataTable({
            "ajax": url,
            pagingType: "simple",
            lengthChange: false,
            stateSave: true,
            order: [[1, 'asc']],
            rowGroup: {
                dataSrc: "department.title"
            },
            "dom": '<"top"i>rt<"bottom">p<"clear">',
            "language": {
                "search": ""
            },
            "columns": [
                {"data": "id"},
                {"data": "department.title"},
                {"data": "name"}
            ],
            "aoColumnDefs": [
                    {
                        "aTargets": [0,1],
                        "sortable": false,
                        "visible" : false
                    },
                    {
                        "aTargets": [2],
                        "mData": "id",
                        "sortable": false,
                        "mRender": function (data,type,row) {
                            let staffDtId = row["id"];
                            let queryParams = new URLSearchParams(window.location.search);
                            queryParams.set("s", staffDtId);
                            queryParams.delete("skip");
                            let add_class='';
                            if (staffDtId === staffId) { add_class = "active"; }
                            return '<a class="dropdown-item text-wrap pl-0 ' + add_class + '" href="search?' + queryParams + '">' + data +'</a>';
                         }
                    },
            ],
            "rowCallback": function( row, data ) {
                if ( data.id === staffId ) {
                    staffName = data.name;
                }
            },
            "initComplete": function() {
                if (staffId !== '') {
                    $('#staff-dd-header').html("<b class='mr-2'>Καθηγητής:</b>" + staffName);
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.delete("s");
                    $("#clear-sm-filter").attr('href','search?' + queryParams);
                    $("#staff-filter").show();
                }
                else {
                    $('#staff-dd-header').html("");
                }
            }
        });

    }

    dashboard.staffmembers.loadStaffByReport = function () {

        let staffId = $("#staffId").val();
        let url = dashboard.siteUrl + '/api/v1/getStaffOfReport';

        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.data, function (index, element) {
                    let staffDtId = element.id;
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("s", staffDtId);
                    queryParams.delete("skip");
                    let html = '<li class="list-group-item">' +
                                    '<a class="text-dark text-decoration-none" href="search?' + queryParams + '">' + element.name +' (' + element.counter + ')</a>' +
                                    '<div class="font-sm text-muted"> Τμήμα ' + element.department.title + '</div>' +
                                '</li>';
                    $("#stFilters").append(html);
                });
                if (data.data.length < 1) {
                    $("#stFilters").hide();
                    $("#staffCanvasLink").hide();
                }
                else {
                    $("#staffCanvasLink").show();
                    $("#no_dyna_filters").hide();
                }
            }
        });
    }


    dashboard.staffmembers.loadStaffMembersInDataTable = function (departmentId) {

        if (departmentId == null || departmentId === "") { departmentId = "dummy";}
        let $institutionStaffMembersDtElem = $("#StaffMembersSelectDataTable");
        StaffMembersDT = $institutionStaffMembersDtElem.DataTable({
            "ajax": dashboard.siteUrl + '/api/v1/dt/staff.web/department/' + departmentId,
            "sDom": 'Zfrtip',
            "oListNav" : {
                sLetterClass : "btn abcdaire",
            },
            "columns": [
                {"data": "id"},
                {"data": "name"},
                {"data": "affiliation"},
                {"data": "department.title"}
            ],
            "language":  dtLanguageGr,
            select: {
                style: 'single',
                items: 'row',
                info: true
            },
            order: [[1, 'asc']],
            "pageLength": 10,
            "pagingType" : "full_numbers",
            "aoColumnDefs": [
                {
                    "aTargets": [0,2],
                    "mData": "id",
                    "visible": false,
                },
                {
                    "aTargets": [1],
                    "mData": "name",
                    "mRender": function (data) {
                        return '<b>' + data + '</b>';
                    }
                }
            ]
        });
    }; // Staff DataTable Init

    dashboard.staffmembers.reloadInstitutionStaffMembers = function() {
        StaffMembersDT.ajax.reload();
    }

    $("#example tbody tr").click( function( e ) {
        if ( $(this).hasClass('row_selected') ) {
            $(this).removeClass('row_selected');
        }
        else {
            oTable.$('tr.row_selected').removeClass('row_selected');
            $(this).addClass('row_selected');
        }
    });

    $("#StaffMembersSelectDataTable").on("click", "tbody tr", function (e) {

        let $searchSelectedStId = $("#searchSelectedStId");
        if ( $(this).hasClass('row_selected') ) {
            $(this).removeClass('row_selected');
            $searchSelectedStId.attr('disabled',true);
            $searchSelectedStId.html('Επιλέξτε Καθηγητή..');
        }
        else {
            StaffMembersDT.$('tr.row_selected').removeClass('row_selected');
            $(this).addClass('row_selected');
            $searchSelectedStId.attr('disabled',false);
            let data = StaffMembersDT.row(this).data()
            $searchSelectedStId.attr('title','Πατήστε για αναζήτηση με τον επιλεγμένο Καθηγητή');
            $searchSelectedStId.html(data.name + '<i class="fas fa-search ml-2"></i>');
        }
    });

    $("#StaffMembersSelectDataTable").on("dblclick", "tbody td", function (e) {

        let table_cell = $(this).closest('td');
        let rowIdx = StaffMembersDT.cell(table_cell).index().row;
        let row_data = StaffMembersDT.row( rowIdx ).data();
        location.href = "search?s=" + row_data.id;
        e.stopPropagation();
    });

    function ApplyStaffMemberFilter() {

        let nodes= StaffMembersDT.rows( { selected: true } ).data();
        let staffId;
        if (nodes.length>0) {
            for (let l = 0; l < nodes.length; l++) {
                let _row = nodes[l];
                staffId = _row.id;
            }
            $("#StaffMemberSelectModal").modal('hide');
            location.href = "search?s=" + staffId;
        }
    }
})();
