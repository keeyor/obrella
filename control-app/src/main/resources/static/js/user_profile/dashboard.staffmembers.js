(function () {
    'use strict';

    dashboard.staffmembers = dashboard.staffmembers || {};


    let staffCoursesDT;
    let staffEventsDT;

    dashboard.staffmembers.init = function () {};

    dashboard.staffmembers.assignedCoursesDT = function () {

        let $staffCourses = $("#staffCoursesDataTable");
        let $staff_id = $("#staff_id");

        staffCoursesDT = $staffCourses.DataTable({
            "ajax": dashboard.siteUrl + '/api/v1/dt/courses.web/staff/' + $staff_id.val(),
            "columns": [
                {"data": null},
                {"data": "title"},
                {"data": "department.title"},
                {"data": "studyProgramTitle"},
                {"data": "lmsReferences"},
                {"data": "supportedBy"}
            ],
            dom: 'rtip',
            "language": dtLanguageGr,
            order: [[1, 'asc']],
            "pageLength": 10,
            "aoColumnDefs": [
                {
                    "aTargets": [1],
                    "mRender": function (data) {
                        return '<span style = "color: #003476;font-weight: 500">' + data + '</span>';
                    }
                },
                {
                    "aTargets": [4],
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
                    "aTargets": [5],
                    "mRender": function (data) {
                        let table_row_html = "";
                        $.each(data, function (i,st) {
                                table_row_html += st.name + "<br/>";
                        });
                        return table_row_html;
                        }
                },
            ]
        });
        staffCoursesDT.on( 'order.dt search.dt', function () {
            staffCoursesDT.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
                cell.innerHTML = i+1;
            } );
        } ).draw();
    }

    dashboard.staffmembers.assignedScheduledEventsDT = function () {

        let $staffEvents = $("#staffEventsDataTable");
        let $staff_id = $("#staff_id");

        staffEventsDT = $staffEvents.DataTable({
            "ajax": dashboard.siteUrl + '/api/v1/dt/events.web/staff/' + $staff_id.val(),
            "columns": [
                {"data": null},
                {"data": "title"},
                {"data": "startDate"},
                {"data": "endDate"},
                {"data": "isActive"}
            ],
            dom: 'rtip',
            "language": dtLanguageGr,
            order: [[1, 'asc']],
            "pageLength": 10,
            "aoColumnDefs": [
                {
                    "aTargets": [1],
                    "mRender": function (data) {
                        return '<span style = "color: #003476;font-weight: 500">' + data + '</span>';
                    }
                },
                {
                    "aTargets": [2],
                    "mRender": function (data) {
                        return moment.unix(data).format("LL");
                    }
                },
                {
                    "aTargets": [3],
                    "mRender": function (data) {
                        if (data != null) {
                            return moment.unix(data).format("LL");
                        }
                        else {
                            return '';
                        }
                    }
                },
                {
                    "aTargets": [4],
                    "mRender": function (data) {
                        if (data) {
                            return '<i class="fas fa-power-off" style="color: green"></i> Ενεργό';
                        }
                        else {
                            return '<i class="fas fa-power-off" style="color: red"></i> Ανενεργό';
                        }
                    }
                }
            ]
        });
        staffEventsDT.on( 'order.dt search.dt', function () {
            staffEventsDT.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
                cell.innerHTML = i+1;
            } );
        } ).draw();
    }

})();
