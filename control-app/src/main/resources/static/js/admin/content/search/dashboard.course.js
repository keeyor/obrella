(function () {
    'use strict';

    dashboard.course = dashboard.course || {};

    let siteUrl;
    let coursesDT;
    let courseSelectDT;

    dashboard.course.loadCourseByReport = function () {

        let url = dashboard.siteUrl + '/api/v1/getCoursesOfReport';

        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.data, function (index, element) {
                    let courseDtId = element.id;
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("c", courseDtId);
                    queryParams.delete("skip");
                    let html = '<li class="list-group-item">' +
                        '<a class="text-dark text-decoration-none" href="search?' + queryParams + '">' + element.title +' (' + element.counter + ')</a>' +
                        '<div class="font-sm text-muted"> Τμήμα ' + element.department.title + '</div>' +
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

    dashboard.course.loadCoursesInDataTable = function () {

        let $staffCourses = $("#CourseSelectDataTable");
        courseSelectDT = $staffCourses.DataTable({
            "ajax": dashboard.siteUrl + '/api/v1/dt/courses.web/department/dummy',
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
                style: 'single',
                items: 'row',
                info: true
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
    dashboard.course.reloadInstitutionCourses = function() {
        courseSelectDT.ajax.reload();
    }
    $("#CourseSelectDataTable").on("click", "tbody tr", function (e) {
        let $searchSelectedCourseId =  $("#searchSelectedCourseId");
        if ( $(this).hasClass('row_selected') ) {
            $(this).removeClass('row_selected');
            $searchSelectedCourseId.attr('disabled',true);
            $searchSelectedCourseId.html('Επιλέξτε Μάθημα..');
        }
        else {
            courseSelectDT.$('tr.row_selected').removeClass('row_selected');
            $(this).addClass('row_selected');
            $searchSelectedCourseId.attr('disabled',false);
            let data = courseSelectDT.row(this).data()
            $searchSelectedCourseId.attr('title','Πατήστε για αναζήτηση με το επιλεγμένο Μάθημα');
            $searchSelectedCourseId.html(data.title + '<i class="fas fa-search ml-2"></i>');
        }
    });

    $("#CourseSelectDataTable").on("dblclick", "tbody td", function (e) {

        let table_cell = $(this).closest('td');
        let rowIdx = courseSelectDT.cell(table_cell).index().row;
        let row_data = courseSelectDT.row( rowIdx ).data();
        location.href = "search?s=" + row_data.id;
        e.stopPropagation();
    });

    function ApplyCourseFilter() {
        let nodes= courseSelectDT.rows( { selected: true } ).data();
        let courseId;
        if (nodes.length>0) {
            for (let l = 0; l < nodes.length; l++) {
                let _row = nodes[l];
                courseId = _row.id;
            }
            location.href = "search?c=" + courseId;
        }
    }
})();
