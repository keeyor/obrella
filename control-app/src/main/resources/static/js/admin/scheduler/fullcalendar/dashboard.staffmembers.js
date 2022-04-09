(function () {
    'use strict';

    dashboard.staffmembers = dashboard.staffmembers || {};

    let siteUrl;
    let $staff_list;
    let staffDT;
    let staffName;
    let staffId;

    dashboard.staffmembers.init = function () {
    };


    dashboard.staffmembers.loadStaffByReport = function () {

        let $staff_dt_el = $("#staff_rdt");

        staffDT = $staff_dt_el.DataTable({
            pagingType: "simple",
            paging: false,
            lengthChange: false,
            ordering: false,
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
                    "mRender": function (data,type,row) {
                        let staffDtId = row["id"];
                        let queryParams = new URLSearchParams(window.location.search);
                        queryParams.set("s", staffDtId);
                        queryParams.delete("e");
                        queryParams.set("cv", dashboard.calendar.view);
                        return '<a style="color: #005cbf" href="calendar?' + queryParams + '">' + data +'</a>';
                    }
                },
                {
                    "aTargets": [3],
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
        });
    }
    dashboard.staffmembers.addRowToReportTable = function(row) {
        $("#staff_rdt").DataTable().row.add(row).draw();
        $("#staff_filters_row").show();
    }
    dashboard.staffmembers.clearTable = function() {
        $("#staff_rdt").DataTable().clear().draw();
    }
})();
