(function () {
    'use strict';

    dashboard.course = dashboard.course || {};

    let siteUrl;
    let $course_list;
    let coursesDT;

    let courseTitle;
    let courseId;
    let staffId;
    let departmentId;

    dashboard.course.init = function () {
        siteUrl = dashboard.siteUrl;
        $course_list = $('#course-list');
        $("#course-dd").attr("disabled", true);
        courseId = $("#courseId").val();
        //dashboard.course.loadCourseByDepartmentOrStaffDT();
    };
    dashboard.course.loadCourseByDepartmentOrStaffDT = function () {

        let url;
        staffId = $("#staffId").val();
        departmentId = $("#departmentId").val();
        if (staffId !== '' && departmentId !== '') {
            url = siteUrl + '/api/v1/dt/courses.web/staff/' + staffId + '/department/' + departmentId;
        }
        else if (staffId === '') {
            url = siteUrl + '/api/v1/dt/courses.web/department/' + departmentId;
        }

        let $courses_dt_el = $("#courses_dt");
        coursesDT = $courses_dt_el.DataTable({
            "ajax": url,
            pagingType: "simple",
            lengthChange: false,
            stateSave: true,
            order: [[1, 'asc']],
            rowGroup: {
                dataSrc: "department.title",
            },
            "dom": '<"top"i>rt<"bottom">p<"clear">',
            "language": {
                "search": ""
            },
            "columns": [
                {"data": "id"},
                {"data": "department.title"},
                {"data": "title"}
            ],
            "aoColumnDefs": [
                    {
                        "aTargets": [0,1],
                        "sortable": false,
                        "visible" : false
                    },
                    {
                        "aTargets": [2],
                        "mData": "id",
                        "sortable": false,
                        "mRender": function (data,type,row) {
                            let courseDtId = row["id"];
                            let queryParams = new URLSearchParams(window.location.search);
                            queryParams.set("c", courseDtId);
                            queryParams.delete("skip");
                            if ($("#staffId").val() === '')
                            {
                                queryParams.delete("s");
                            }
                            let add_class='';
                            if (courseDtId === courseId) { add_class = "active"; }
                            return '<a class="dropdown-item text-wrap pl-0 ' + add_class + '" href="daily?' + queryParams + '">' + data +'</a>';
                         }
                    },
            ],
            "rowCallback": function( row, data ) {
                if ( data.id === courseId ) {
                    courseTitle = data.title;
                }
            },
            "initComplete": function() {
                if (courseId !== '') {
                    $('#course-dd-header').html("<b class='mr-2'>Μάθημα:</b>" + courseTitle);
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.delete("c");
                    queryParams.delete("s");
                    $("#clear-co-filter").attr('href','daily?' + queryParams);
                    $("#course-filter").show();
                }
                else {
                    $('#course-dd-header').html("");
                }
            }
        });
    }
    dashboard.course.loadCourseByDepartment = function () {

        let departmentId = $("#departmentId").val();
        if (departmentId == null || departmentId === '') {
            departmentId = 'dummy';
        }
        let url = siteUrl + '/api/v1/s2/courses.web/department/' + departmentId;
        $('#course-list').html("");
        $('#course-dd-header').html("Μάθημα");
        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.results, function (index, element) {
                   $course_list.append('<div><a class="gray-accesskeys-link-wcag-color" href="#">' + element.text +'</a></div>');
                   // $course_list.append('<br>' + element.text);
                });
                $("#course-dd").attr("disabled", false);
            }
        });

    }
    dashboard.course.loadCourseByReport = function () {

        let sum = 0;
        siteUrl = dashboard.siteUrl;

        $('#crSearchText').on( 'keyup', function () {
            coursesDT.search( this.value ).draw();
        } );

        $('#clear-cr-search-text').on( 'click', function () {
            let val = $("#crSearchText").val();
            if (val !== '') {
                $("#crSearchText").val('');
                coursesDT.search('').draw();
            }
        } );

        $(document).on('init.dt', function ( e, settings ) {
            /*var api = new $.fn.dataTable.Api(settings);
            var state = api.state.loaded();
            var searchText = state.search.search;
            $("#crSearchText").val(searchText);*/
            $("#crSearchText").val('');
            coursesDT.search('').draw();
        });

        let url = siteUrl + '/api/v1/getCoursesOfReport';
        let $courses_dt_el = $("#courses_rdt");
        courseId = $("#courseId").val();

        coursesDT = $courses_dt_el.DataTable({
            "ajax": url,
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
                        queryParams.delete("skip");
                        return '<a style="color: #005cbf" href="daily?' + queryParams + '">' + data +'</a>';
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
                    var sum = coursesDT.column(3).data().sum();
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("rt", "c");
                    queryParams.delete("skip");
                    let _html = '<tr>';
                    _html += '<td style="width: 92%" class="pl-0"><a href="daily?' + queryParams + '">Διάλεξη</a></td>';
                    _html += '<td class="text-right">' + sum + '</td></tr>';
                    if (sum > 0) {
                        $("#table-typefilter").append(_html);
                    }
            }
        });
    }

})();
