(function () {
    'use strict';

    dashboard.staffmembers = dashboard.staffmembers || {};

    let staffDT;
    let staffCoursesDT;
    let courseSelectDT;
    let serialize_form;
    let $staffDtElem;
    dashboard.staffmembers.init = function () {};

    dashboard.staffmembers.assignedCoursesDT = function () {

        let $staffCourses = $("#staffCoursesDataTable");
        let $staff_id = $("#staff_id");
        let $course_assign_department = $("#course_assign_department");

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
                        return '<span style = "color: #003476">' + data + '</span>';
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

})();
