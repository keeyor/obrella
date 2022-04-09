(function () {
    'use strict';

    dashboard.departments = dashboard.departments || {};

    let $department_list;
    let departmentId;
    let departmentTitle;
    let departmentsDT;

    dashboard.departments.init = function () {
        $department_list = $('#department-list');//only for dropdown
        dashboard.departments.loadDepartmentsOnSearchBar();
    };

    dashboard.departments.loadDepartmentsOnSearchBar = function () {

        $department_list = $('#department-columns');
        let queryParams = new URLSearchParams(window.location.search);

        let url = dashboard.siteUrl + '/api/v1/s2/departments.web/school/dummy';
        let html = '';
        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.results, function (index, element) {
                    html += '<li class="dropdown-submenu">';
                    html += '<a class="dropdown-item dropdown-toggle" href="#">' + element.text + '</a>';
                    html += '<ul class="dropdown-menu">';
                    $.each(element.children, function (index1, el) {
                        queryParams.set("d", el.id);
                        queryParams.delete("skip");
                        queryParams.delete("ca");
                        queryParams.delete("c");
                        queryParams.delete("s");
                        queryParams.delete("e");
                        queryParams.delete("ft");
                        html +=  '<li><a class="dropdown-item" href="search?' + queryParams + '">' + el.text +'</a></li>';
                    });
                    html += '</ul></li>';
                });
                $department_list.append(html);
            }
        });
    }

    dashboard.departments.loadDepartmentsByReport = function () {

        $('#depSearchText').on( 'keyup', function () {
            departmentsDT.search( this.value ).draw();
        } );
        $('#clear-dp-search-text').on( 'click', function () {
            $("#depSearchText").val('');
            departmentsDT.search( '' ).draw();
        } );

        $(document).on('init.dt', function ( e, settings ) {
            /*var api = new $.fn.dataTable.Api(settings);
            var state = api.state.loaded();
            var searchText = state.search.search;
            $("#crSearchText").val(searchText);*/
            $("#depSearchText").val('');
            departmentsDT.search('').draw();
        });

        let url = dashboard.siteUrl + '/api/v1/getDepartmentsOfReport';
        let $departments_dt_el = $("#departments_rdt");
        departmentId = $("#departmentId").val();

        departmentsDT = $departments_dt_el.DataTable({
            "ajax": url,
            pagingType: "simple",
            paging: false,
            lengthChange: false,
            order: [[1, 'asc']],
            responsive: true,
            "dom": '<"top">rt<"bottom">ip<"clear">',
            "language": dtLanguageGr,
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
                    "mData": "id",
                    "sortable": false,
                    "mRender": function (data,type,row) {
                        let depDtId = row["id"];
                        let queryParams = new URLSearchParams(window.location.search);
                        queryParams.set("d", depDtId);
                        queryParams.delete("skip");
                        return '<a style="color: #005cbf" href="search?' + queryParams + '">' + data +' (' + row["counter"] + ')</a>';
                    }
                },
                {
                    "aTargets": [2],
                    "sortable": false,
                    "className" :"text-right"
                }
            ],
            "rowCallback": function( row, data ) {
                if ( data.id === departmentId ) {
                    departmentTitle = data.title;
                }
            },
            "initComplete": function() {
                if ( this.api().data().length === 0) {
                    $("#departments_filter_row").hide();
                }
            }
        });
    }

    dashboard.departments.loadDepartmentsInColumns = function () {

        let $department_list = $('#department-columns-alt');
        let queryParams = new URLSearchParams(window.location.search);

        let url = dashboard.siteUrl + '/api/v1/s2/departments.web/school/dummy';
        let html = '';
        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                let idx =0 ;
                $.each(data.results, function (index, element) {
                    if (index === 0) {
                        html += '<div class="col-lg-3 col-md-12 col-xs-12">';
                    }
                    if (index !== 0 && (index === 5 || index === 9)) {
                        html += '</div><div class="col-lg-3 col-md-12 col-xs-12">';
                        idx = 0;
                    }
                    html += '<h6>' +  element.text + '</h6>';
                    html += '<ul>';
                    $.each(element.children, function (index1, el) {
                        queryParams.set("d", el.id);
                        queryParams.delete("skip");
                        queryParams.delete("ca");
                        queryParams.delete("c");
                        queryParams.delete("s");
                        queryParams.delete("e");
                        queryParams.delete("ft");
                        html +=  '<li class="pl-2"><i style="font-size: 0.5em" class="far fa-circle fa-xs mr-1"></i><a style="color:#4f5d73" href="search?' + queryParams + '">' + el.text +'</a></li>';
                        idx++;
                    });
                    html += "</ul>";
                    idx++;
                });
                $department_list.append(html);
            }
        });
    }

})();
