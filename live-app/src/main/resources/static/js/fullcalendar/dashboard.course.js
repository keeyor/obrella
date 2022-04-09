(function () {
    'use strict';

    dashboard.course = dashboard.course || {};

    let coursesDT;

    dashboard.course.loadCourseByReport = function () {

        let $courses_dt_el = $("#courses_rdt");

        coursesDT = $courses_dt_el.DataTable({
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
                {"data": "title"},
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
                    "mData": "text",
                    "mRender": function (data,type,row) {
                        let courseDtId = row["id"];
                        let queryParams = new URLSearchParams(window.location.search);
                        queryParams.set("c", courseDtId);
                        queryParams.delete("e");
                        queryParams.set("cv", dashboard.calendar.view);
                        queryParams.set("sd",$("#start_date").val());
                        queryParams.set("ed",$("#end_date").val());
                        return '<a style="color: #005cbf" href="calendar?' + queryParams + '">' + data +'</a>';
                    }
                }
            ],
            "initComplete": function() {
                if ( this.api().data().length === 0) {
                    $("#courses_filter_row").hide();
                }
            }
        });
    }
    dashboard.course.addRowToReportTable = function(row) {
        $("#courses_rdt").DataTable().row.add(row).draw();
        $("#courses_filter_row").show();
    }
    dashboard.course.clearTable = function() {
        $("#courses_rdt").DataTable().clear().draw();
    }

})();
