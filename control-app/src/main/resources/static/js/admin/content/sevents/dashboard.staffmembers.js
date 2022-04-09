(function () {
    'use strict';

    dashboard.staffmembers = dashboard.staffmembers || {};

    let StaffMembersDT;
    let cpage;

    dashboard.staffmembers.init = function () {
        cpage = $("#cpage").val();

        $("#staff_load").on('click',function(e){
            let department_filter = $("#department_filter").val();

            if ( ! $.fn.DataTable.isDataTable( '#StaffMembersSelectDataTable' ) ) {
                dashboard.staffmembers.InitAuthorizedInstitutionStaffMembers(department_filter);
            }
            else {
                dashboard.staffmembers.reloadInstitutionStaffMembers(department_filter);
            }

            $("#StaffMemberSelectModal").modal('show');
            e.preventDefault();
        });


        $("#applyStaffMemberFilter").on('click',function(){
            let StaffMembersDT = $("#StaffMembersSelectDataTable").DataTable();
            let nodes= StaffMembersDT.rows( { selected: true } ).data();
            let staffId, staffName;
            if (nodes.length>0) {
                for (let l = 0; l < nodes.length; l++) {
                    let _row = nodes[l];
                    staffId = _row.id;
                    staffName = _row.name;
                }
                $("#StaffMemberSelectModal").modal('hide');
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.set("s", staffId);
                location.href = cpage + "?" + queryParams;
            }
        });
    }
    dashboard.staffmembers.InitAuthorizedInstitutionStaffMembers = function (department_filter) {

        let $institutionStaffMembersDtElem = $("#StaffMembersSelectDataTable");
        let url;
        if (department_filter === undefined || department_filter === "") {
            url = dashboard.siteUrl + '/api/v2/dt/staff.web/authorized/content';
        }
        else {
            url =  dashboard.siteUrl + '/api/v2/dt/staff.web/authorized/content/d/' + department_filter;
        }
        StaffMembersDT = $institutionStaffMembersDtElem.DataTable({
            "ajax": url,
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
            "language":  dashboard.dtLanguageGr,
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

    dashboard.staffmembers.reloadInstitutionStaffMembers = function() {
        StaffMembersDT.ajax.reload();
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
                        '<a class="text-dark text-decoration-none" href="sevents?' + queryParams + '">' + element.name +' (' + element.counter + ')</a>' +
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

        /*     jQuery('#stSearchText').on( 'keyup', function () {
                 staffDT.search( this.value ).draw();
             } );
             $('#clear-st-search-text').on( 'click', function () {
                 $("#stSearchText").val('');
                 staffDT.search( '' ).draw();
             } );

             $(document).on('init.dt', function ( e, settings ) {
                 /!*var api = new $.fn.dataTable.Api(settings);
                 var state = api.state.loaded();
                 var searchText = state.search.search;
                 $("#crSearchText").val(searchText);*!/
                 $("#stSearchText").val('');
                 staffDT.search('').draw();
             });

             let url = siteUrl + '/api/v1/getStaffOfReport';
             let $staff_dt_el = $("#staff_rdt");
             staffId = $("#staffId").val();

             staffDT = $staff_dt_el.DataTable({
                 "ajax": url,
                 pagingType: "simple",
                 paging: false,
                 lengthChange: false,
                 order: [[2, 'asc']],
                 rowGroup: {
                     dataSrc: "department.title",
                     startRender: function ( rows, group ) {
                         return 'Τμήμα ' + group;
                     }
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
                         "sortable": false,
                         "mRender": function (data,type,row) {
                             let staffDtId = row["id"];
                             let queryParams = new URLSearchParams(window.location.search);
                             queryParams.set("s", staffDtId);
                             queryParams.delete("skip");
                             let add_class='';
                             if (staffDtId === staffId) { add_class = "active"; }
                             return '<a class="' + add_class + '" style="color: #005cbf" href="search?' + queryParams + '">' + data + ' (' + row["counter"] + ')</a>';
                         }
                     },
                     {
                         "aTargets": [1],
                         "mRender": function (data) {
                             return 'Τμήμα ' + data;
                         }
                     },
                     {
                         "aTargets": [3],
                         "sortable": false,
                         "className" :"text-right",
                         "visible": true
                     }
                 ],
                 "rowCallback": function( row, data ) {
                 },
                 "initComplete": function() {
                     if ( this.api().data().length === 0) {
                         $("#staff_filters_row").hide();
                     }
                 }
             });*/

    }

})();
