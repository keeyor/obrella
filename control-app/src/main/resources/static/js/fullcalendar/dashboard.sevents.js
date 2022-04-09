(function () {
    'use strict';

    dashboard.sevents = dashboard.sevents || {};

    dashboard.sevents.init = function () {
    };


    dashboard.sevents.loadEventsByReport = function () {

        let $events_dt_el = $("#events_rdt");

        let eventsDT = $events_dt_el.DataTable({
            lengthChange: false,
            pagingType: "simple",
            paging: false,
            ordering: false,
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
                {"data": "title"},
                {"data": "counter"}
            ],
            "aoColumnDefs": [
                {
                    "aTargets": [0,2],
                    "sortable": false,
                    "visible" : false
                },
                {
                    "aTargets": [1],
                    "visible" : true,
                    "mRender": function (data,type,row) {
                        let queryParams = new URLSearchParams(window.location.search);
                        queryParams.set("e", row["id"]);
                        queryParams.delete("c", row["id"]);
                        queryParams.set("cv", dashboard.calendar.view);
                        return '<a style="color: #005cbf" href="calendar?' + queryParams + '">' + data +'</a>';
                    }
                },
                {
                    "aTargets": [2],
                    "className" :"text-right"
                }
            ]
        });
    }
    dashboard.sevents.addRowToReportTable = function(row) {
        $("#events_rdt").DataTable().row.add(row).draw();
    }
    dashboard.sevents.clearTable = function() {
        $("#events_rdt").DataTable().clear().draw();
    }
})();
