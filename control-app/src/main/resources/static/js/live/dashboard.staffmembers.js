(function () {
    'use strict';

    dashboard.staffmembers = dashboard.staffmembers || {};

    let siteUrl;
    let $staff_list;
    let staffDT;
    let staffName;
    let staffId;

    dashboard.staffmembers.init = function () {
        siteUrl = dashboard.siteUrl;
        $staff_list = $('#staff-list');
        $("#staff-dd").attr("disabled", true);
        staffId = $("#staffId").val();
        dashboard.staffmembers.loadStaffsDT();

    };

    dashboard.staffmembers.loadStaffsDT = function () {

        let url;
        let departmentId = $("#departmentId").val();
        let courseId = $("#courseId").val();
        if (courseId === '' && departmentId !== '') {
            url = siteUrl + '/api/v1/dt/staff.web/teaching/department/' + departmentId;
        }
        else if (courseId !== '') {
            url = siteUrl + '/api/v1/dt/staff.web/course/' + courseId;
        }
        else {
            url = '';
        }

        let $staff_dt_el = $("#staff_dt");
        staffDT = $staff_dt_el.DataTable({
            "ajax": url,
            pagingType: "simple",
            lengthChange: false,
            stateSave: true,
            order: [[1, 'asc']],
            rowGroup: {
                dataSrc: "department.title"
            },
            "dom": '<"top"i>rt<"bottom">p<"clear">',
            "language": {
                "search": ""
            },
            "columns": [
                {"data": "id"},
                {"data": "department.title"},
                {"data": "name"}
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
                            let staffDtId = row["id"];
                            let queryParams = new URLSearchParams(window.location.search);
                            queryParams.set("s", staffDtId);
                            queryParams.delete("skip");
                            let add_class='';
                            if (staffDtId === staffId) { add_class = "active"; }
                            return '<a class="dropdown-item text-wrap pl-0 ' + add_class + '" href="live?' + queryParams + '">' + data +'</a>';
                         }
                    },
            ],
            "rowCallback": function( row, data ) {
                if ( data.id === staffId ) {
                    staffName = data.name;
                }
            },
            "initComplete": function() {
                if (staffId !== '') {
                    $('#staff-dd-header').html("<b class='mr-2'>Καθηγητής:</b>" + staffName);
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.delete("s");
                    $("#clear-sm-filter").attr('href','live?' + queryParams);
                    $("#staff-filter").show();
                }
                else {
                    $('#staff-dd-header').html("");
                }
            }
        });

    }

    dashboard.staffmembers.loadStaffByReport = function () {

        siteUrl = dashboard.siteUrl;


        $('#stSearchText').on( 'keyup', function () {
            staffDT.search( this.value ).draw();
        } );
        $('#clear-st-search-text').on( 'click', function () {
            $("#stSearchText").val('');
            staffDT.search( '' ).draw();
        } );

        $(document).on('init.dt', function ( e, settings ) {
            /*var api = new $.fn.dataTable.Api(settings);
            var state = api.state.loaded();
            var searchText = state.search.search;
            $("#crSearchText").val(searchText);*/
            $("#stSearchText").val('');
            staffDT.search('').draw();
        });

        let url = siteUrl + '/api/v1/getStaffOfReport';
        let $staff_dt_el = $("#staff_rdt");
        staffId = $("#staffId").val();

        staffDT = $staff_dt_el.DataTable({
            "ajax": url,
            pagingType: "simple",
            lengthChange: false,
            paging: false,
            rowGroup: {
                dataSrc: "department.title",
                startRender: function ( rows, group ) {
                    return 'Τμήμα ' + group;
                }
            },
            "ordering": false,
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
                {"data": "name"},
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
                    "mData": "id",
                    "mRender": function (data,type,row) {
                        let staffDtId = row["id"];
                        let queryParams = new URLSearchParams(window.location.search);
                        queryParams.set("s", staffDtId);
                        queryParams.delete("skip");
                        let add_class='';
                        if (staffDtId === staffId) { add_class = "active"; }
                        return '<a class="' + add_class + '" style="color: #005cbf" href="live?' + queryParams + '">' + data + '</a>';
                    }
                },
                {
                    "aTargets": [3],
                    "className" :"text-right",
                    "visible": true
                }
            ],
            "rowCallback": function( row, data ) {
            },
            "initComplete": function() {
                if ( this.api().data().length === 0) {
                    $("#staff_filters_row").hide();
                }
            }
        });

    }
})();
