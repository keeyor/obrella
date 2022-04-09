(function () {
    'use strict';

    dashboard.course = dashboard.course || {};

    let coursesReportDT;
    let courseSelectDT;

    dashboard.course.init = function () {

        $("#course_list").on('click',function(e){
            if ( ! $.fn.DataTable.isDataTable( '#CourseSelectDataTable' ) ) {
                dashboard.course.InitAuthorizedInstitutionCourses();
            }
            else {
                dashboard.course.reloadInstitutionCourses();
            }

            $("#CourseSelectModal").modal('show');
            e.preventDefault();
        });

        $("#applyCourseFilter").on('click',function(){
            let courseSelectDT = $("#CourseSelectDataTable").DataTable();
            let nodes= courseSelectDT.rows( { selected: true } ).data();
            let courseId;
            if (nodes.length>0) {
                for (let l = 0; l < nodes.length; l++) {
                    let _row = nodes[l];
                    courseId = _row.id;
                }
                $("#CourseSelectModal").modal('hide');
                location.href = "search?c=" + courseId;
            }
        });
    };

    dashboard.course.InitAuthorizedInstitutionCourses = function () {

        let $staffCourses = $("#CourseSelectDataTable");
        courseSelectDT = $staffCourses.DataTable({
            "ajax": dashboard.siteUrl + '/api/v2/dt/courses.web/authorized/content',
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

    dashboard.course.getCourseInfo = function(courseId) {

        $.ajax({
            url:  dashboard.siteUrl + '/api/v1/course/' + courseId,
            cache: false
        })
            .done(function( data ) {
                let course_info_html="";
                course_info_html += "<table style='border: none;width: 100%'>";
                course_info_html += "<tr><th style='width: 40%;font-weight: 550;margin-bottom: 5px;vertical-align: top'>Τίτλος</th><td>" + data.title + "</td></tr>";
                course_info_html += "<tr><th style='width: 40%;font-weight: 550;margin-bottom: 5px;vertical-align: top'>Τμήμα</th><td>" + data.department.title + "</td></tr>";
                course_info_html += "<tr><th style='width: 40%;font-weight: 550;margin-bottom: 5px;vertical-align: top'>Πρόγραμμα Σπουδών</th><td>" + data.studyProgramTitle + "</td></tr>";
                course_info_html += "<tr><th style='width: 40%;font-weight: 550;margin-bottom: 5px;vertical-align: top'>Κωδικός Γραμματείας</th><td>";
                if (data.scopeId !== "") {
                    course_info_html += data.scopeId + "</td></tr>";
                }
                else {
                    course_info_html += " - ";
                }
                course_info_html += "<tr><th style='width: 40%;font-weight: 550;margin-bottom: 5px;vertical-align: top'>Κωδικοί LMS</th><td>";
                let lms_codes="";
                $.each(data.lmsReferences, function (index, el) {
                    lms_codes += (index>0 ? ', ' : '') + el.lmsId + ' [' + el.lmsCode + ']';
                });
                course_info_html += lms_codes + "</td></tr>";
                course_info_html += "</table>";
                $("#course_info_panel").html(course_info_html);
            });
    }
   /* dashboard.course.loadCoursesOnSearchBar = function () {

        $course_list = $('#course-columns');
        let queryParams = new URLSearchParams(window.location.search);

        let url = dashboard.siteUrl + '/api/v1/s2/courses.web/authorized/content';
        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.results, function (index, el) {
                        queryParams.set("c", el.id);
                        queryParams.delete("skip");
                        queryParams.delete("up");
                        queryParams.delete("d");
                        queryParams.delete("s");
                        queryParams.delete("e");
                        queryParams.delete("ft");
                        $course_list.append('<li><a class="dropdown-item"  href="?' + queryParams + '">' + el.text + '</a></li>');
                });
            }
        });
    }*/

    dashboard.course.loadCourseByReport = function () {

        $('#crSearchText').on( 'keyup', function () {
            coursesReportDT.search( this.value ).draw();
        } );

        $('#clear-cr-search-text').on( 'click', function () {
            let val = $("#crSearchText").val();
            if (val !== '') {
                $("#crSearchText").val('');
                coursesReportDT.search('').draw();
            }
        } );

        $(document).on('init.dt', function ( e, settings ) {
            /*var api = new $.fn.dataTable.Api(settings);
            var state = api.state.loaded();
            var searchText = state.search.search;
            $("#crSearchText").val(searchText);*/
            $("#crSearchText").val('');
            coursesReportDT.search('').draw();
        });

        let url = dashboard.siteUrl + '/api/v1/getCoursesOfReport';
        let $courses_dt_el = $("#courses_rdt");

        coursesReportDT = $courses_dt_el.DataTable({
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
                        queryParams.delete("skip");
                        return '<a style="color: #005cbf" href="search?' + queryParams + '">' + data + ' (' + row["counter"] + ')</a>';
                    }
                },
                {
                    "aTargets": [3],
                    "className" :"text-right"
                }
            ],
            "rowCallback": function( row, data ) {

            },
            "initComplete": function() {
                if ( this.api().data().length === 0) {
                    $("#courses_filter_row").hide();
                }
                    var sum = coursesReportDT.column(3).data().sum();
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("rt", "c");
                    queryParams.delete("skip");
                    let _html = '<tr>';
                    _html += '<td style="width: 92%" class="pl-0"><a  style="color: #005cbf" href="?' + queryParams + '">Διάλεξη' + ' (' + sum + ')</a>';
                    if (sum > 0) {
                        $("#table-typefilter").append(_html);
                    }
            }
        });
    }
})();
