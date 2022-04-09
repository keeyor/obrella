(function () {
    'use strict';

    dashboard.course = dashboard.course || {};

    let siteUrl;
    let coursesDT;
    let courseSelectDT;

    dashboard.course.loadCourseByReport = function () {

        siteUrl = dashboard.siteUrl;
        let $crSearchText = $("#crSearchText");

        $('#crSearchText').on( 'keyup', function () {
            coursesDT.search( this.value ).draw();
        } );

        $('#clear-cr-search-text').on( 'click', function () {

            let val = $crSearchText.val();
            if (val !== '') {
                $crSearchText.val('');
                coursesDT.search('').draw();
            }
        } );

        $(document).on('init.dt', function () {
            $crSearchText.val('');
            coursesDT.search('').draw();
        });

        let url = siteUrl + '/api/v1/getCoursesOfReport';
        let $courses_dt_el = $("#courses_rdt");

        coursesDT = $courses_dt_el.DataTable({
            "ajax": url,
            pagingType: "simple",
            paging: false,
            lengthChange: false,
            order: [[2, 'asc']],
            responsive: true,
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
                    responsivePriority: 1,
                    "mData": "text",
                    "sortable": false,
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
                    "sortable": false,
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
                    _html += '<td style="width: 92%" class="pl-0"><a href="search?' + queryParams + '">Διάλεξη</a></td>';
                    _html += '<td class="text-right">' + sum + '</td></tr>';
                    if (sum > 0) {
                        $("#table-typefilter").append(_html);
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
