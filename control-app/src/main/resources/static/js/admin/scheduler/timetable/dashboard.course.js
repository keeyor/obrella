(function () {
    'use strict';

    dashboard.course = dashboard.course || {};

    let coursesReportDT;
    let courseSelectDT;

    dashboard.course.init = function () {

        $("#course_load").on('click',function(e){

            let department_filter = $("#department_filter").val();

            if ( ! $.fn.DataTable.isDataTable( '#CourseSelectDataTable' ) ) {
                dashboard.course.InitAuthorizedInstitutionCourses(department_filter);
            }
            else {
                dashboard.course.reloadInstitutionCourses(department_filter);
            }

            $("#CourseSelectModal").modal('show');
            e.preventDefault();
        });

        $("#applyCourseFilter").on('click',function(){
            let courseSelectDT = $("#CourseSelectDataTable").DataTable();
            let nodes= courseSelectDT.rows( { selected: true } ).data();
            let courseId, courseTitle;
            if (nodes.length>0) {
                for (let l = 0; l < nodes.length; l++) {
                    let _row = nodes[l];
                    courseId = _row.id;
                    courseTitle = _row.title
                }
                $("#CourseSelectModal").modal('hide');
                let message = {msg: "Filter:" + "course" + " Selected!", filter: "course", id: courseId, value: courseTitle};
                dashboard.broker.trigger('filter.select', [message]);
            }
        });
    };

    dashboard.course.loadCourseByReport = function(department_filter) {

        let url;
        if (department_filter === undefined || department_filter === "") {
            url = dashboard.siteurl + '/api/v2/dt/courses.web/authorized/scheduler';
        }
        else {
            url =  dashboard.siteurl + '/api/v2/dt/courses.web/authorized/scheduler/d/' + department_filter;
        }
        $("#coFilters").html("");
        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.data, function (index, el) {
                    let html = '<li class="list-group-item">' +
                        '<a class="text-dark text-decoration-none filter-item" href="#" data-filter="course" data-target="' + el.id + '">' + el.title + '</a>' +
                        '</li>';
                    $("#coFilters").append(html);
                });
                if (data.data.length < 1) {
                    $("#coFilters").hide();
                    $("#courseCanvasLink").hide();
                }
                else {
                    $("#courseCanvasLink").show();
                    $("#no_dyna_filters").hide();
                }
            }
        });

    }

    dashboard.course.InitAuthorizedInstitutionCourses = function (department_filter) {

        let $staffCourses = $("#CourseSelectDataTable");

        let url;
        if (department_filter === undefined || department_filter === "") {
            url = dashboard.siteurl + '/api/v2/dt/courses.web/authorized/scheduler';
        }
        else {
            url =  dashboard.siteurl + '/api/v2/dt/courses.web/authorized/scheduler/d/' + department_filter;
        }

        courseSelectDT = $staffCourses.DataTable({
            "ajax": url,
            "sDom": 'Zfrtip',
            "oListNav" : {
                sLetterClass : "btn abcdaire",
            },
            "columns": [
                {"data": "id"},
                {"data": "title"},
                {"data": "department.title"}
            ],
            select: {
                style: 'single'
            },
            "language": dtLanguageGr,
            order: [[1, 'asc']],
            "pageLength": 10,
            "pagingType" : "full_numbers",
            "aoColumnDefs": [
                {
                    "aTargets": [0],
                    "mData": "id",
                    "visible": false
                }
            ]
        });
    }
    dashboard.course.reloadInstitutionCourses = function(department_filter) {
        let url;
        if (department_filter === undefined || department_filter === "") {
            url = dashboard.siteurl + '/api/v2/dt/courses.web/authorized/scheduler';
        }
        else {
            url =  dashboard.siteurl + '/api/v2/dt/courses.web/authorized/scheduler/d/' + department_filter;
        }
        courseSelectDT.ajax.url(url);
        courseSelectDT.ajax.reload();
    }

})();
