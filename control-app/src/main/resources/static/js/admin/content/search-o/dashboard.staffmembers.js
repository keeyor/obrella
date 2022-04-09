(function () {
    'use strict';

    dashboard.staffmembers = dashboard.staffmembers || {};

    let siteUrl;
    let StaffMembersDT;
    let staffReportDT;
    let staffId;


    dashboard.staffmembers.init = function () {
        $("#staff_list").on('click',function(e){
            if ( ! $.fn.DataTable.isDataTable( '#StaffMembersSelectDataTable' ) ) {
                dashboard.staffmembers.InitAuthorizedInstitutionStaffMembers();
            }
            else {
                dashboard.staffmembers.reloadInstitutionStaffMembers();
            }

            $("#StaffMemberSelectModal").modal('show');
            e.preventDefault();
        });

        $("#applyStaffMemberFilter").on('click',function(){
            let StaffMembersDT = $("#StaffMembersSelectDataTable").DataTable();
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
        });
    }

    dashboard.staffmembers.InitAuthorizedInstitutionStaffMembers = function () {

        let $institutionStaffMembersDtElem = $("#StaffMembersSelectDataTable");
        StaffMembersDT = $institutionStaffMembersDtElem.DataTable({
            "ajax": dashboard.siteUrl + '/api/v2/dt/staff.web/authorized/content',
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
    }; // Staff DataTable Init

    dashboard.staffmembers.reloadInstitutionStaffMembers = function() {
        StaffMembersDT.ajax.reload();
    }

    dashboard.staffmembers.loadStaffByReport = function () {

        siteUrl = dashboard.siteUrl;

        $('#stSearchText').on( 'keyup', function () {
            staffReportDT.search( this.value ).draw();
        } );
        $('#clear-st-search-text').on( 'click', function () {
            $("#stSearchText").val('');
            staffReportDT.search( '' ).draw();
        } );

        $(document).on('init.dt', function ( e, settings ) {
            /*var api = new $.fn.dataTable.Api(settings);
            var state = api.state.loaded();
            var searchText = state.search.search;
            $("#crSearchText").val(searchText);*/
            $("#stSearchText").val('');
            staffReportDT.search('').draw();
        });

        let url = siteUrl + '/api/v1/getStaffOfReport';
        let $staff_dt_el = $("#staff_rdt");
        staffId = $("#staffId").val();

        staffReportDT = $staff_dt_el.DataTable({
            "ajax": url,
            pagingType: "simple",
            paging: false,
            lengthChange: false,
            order: [[2, 'asc']],
            rowGroup: {
                dataSrc: "department.title"
            },
            "dom": '<"top">rt<"bottom">ip<"clear">',
            "language": {
                "search": "",
                "sInfo": "Εμφάνιση από _START_ έως _END_ από _TOTAL_",
                "oPaginate": {
                    "sNext": "<i class='fas fa-angle-right'></i>",
                    "sPrevious": "<i class='fas fa-angle-left'></i>"
                }
            },
            "columns": [
                {"data": "id"},
                {"data": "department.title"},
                {"data": "name"},
                {"data": "counter"}
            ],
            "aoColumnDefs": [
                {
                    "aTargets": [0,1,3],
                    "sortable": false,
                    "visible" : false
                },
                {
                    "aTargets": [2],
                    "mData": "id",
                    "mRender": function (data,type,row) {
                        let staffDtId = row["id"];
                        let queryParams = new URLSearchParams(window.location.search);
                        queryParams.set("s", staffDtId);
                        queryParams.delete("skip");
                        let add_class='';
                        if (staffDtId === staffId) { add_class = "active"; }
                        return '<a class="' + add_class + '" style="color: #005cbf" href="search?' + queryParams + '">' + data + ' (' + row["counter"] + ')</a>';
                    }
                }
            ],
            "rowCallback": function( row, data ) {
            },
            "initComplete": function() {
                if ( this.api().data().length === 0) {
                    $("#staff_filters_row").hide();
                }
            }
        });

    }
})();
