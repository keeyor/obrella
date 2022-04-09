(function () {
    'use strict';

    dashboard.departments = dashboard.departments || {};

    let siteUrl;
    let $department_list;
    let departmentId;
    let departmentTitle;
    let departmentsDT;

    dashboard.departments.init = function () {
        siteUrl          = dashboard.siteUrl;
        $department_list = $('#department-list');
        dashboard.departments.loadDepartmentsOnSearchBar();
    };

    dashboard.departments.loadDepartmentsOnSearchBar = function () {

        $department_list = $('#department-columns');
        siteUrl          = dashboard.siteUrl;
        let queryParams = new URLSearchParams(window.location.search);

        let url = siteUrl + '/api/v1/s2/departments.web/school/dummy';
        let html = '';
        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                let selected_department_id = $("#department_id").val();
                if (selected_department_id !== '' && selected_department_id !== undefined) {
                    html += '<li><a class="dropdown-item" href="daily">Όλα τα Τμήματα</a></li>';
                }
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
                        html +=  '<li><a class="dropdown-item" href="daily?' + queryParams + '">' + el.text +'</a></li>';
                    });
                    html += '</ul></li>';
                });
                $department_list.append(html);
            }
        });
    }

    dashboard.departments.loadDepartmentsByReport = function () {

        siteUrl = dashboard.siteUrl;

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

        let url = siteUrl + '/api/v1/getDepartmentsOfReport';
        let $departments_dt_el = $("#departments_rdt");
        departmentId = $("#departmentId").val();

        departmentsDT = $departments_dt_el.DataTable({
            "ajax": url,
            pagingType: "simple",
            paging: false,
            lengthChange: false,
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
                    "mData": "id",
                    "mRender": function (data,type,row) {
                        let depDtId = row["id"];
                        let queryParams = new URLSearchParams(window.location.search);
                        queryParams.set("d", depDtId);
                        queryParams.delete("skip");
                        return '<a style="color: #005cbf" href="daily?' + queryParams + '">' + data +' (' + row["counter"] + ')</a>';
                    }
                },
                {
                    "aTargets": [2],
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

})();
