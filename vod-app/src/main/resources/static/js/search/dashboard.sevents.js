(function () {
    'use strict';

    dashboard.sevents = dashboard.sevents || {};

    let siteUrl;
    let $event_list;
    let EventsDT;

    dashboard.sevents.init = function () {
        siteUrl = dashboard.siteUrl;
        $event_list = $('#events-list');
        //dashboard.sevents.loadScheduledEvents();

    };

    dashboard.sevents.loadEventsByReport = function () {

        let sum = 0;
        siteUrl = dashboard.siteUrl;

        $('#evSearchText').on( 'keyup', function () {
            eventsDT.search( this.value ).draw();
        } );
        $('#clear-ev-search-text').on( 'click', function () {
            $("#evSearchText").val('');
            eventsDT.search( '' ).draw();
        } );

        $(document).on('init.dt', function ( e, settings ) {
            /*var api = new $.fn.dataTable.Api(settings);
            var state = api.state.loaded();
            var searchText = state.search.search;
            $("#crSearchText").val(searchText);*/
            $("#evSearchText").val('');
            eventsDT.search('').draw();
        });

        let url = siteUrl + '/api/v1/getEventsOfReport';
        let queryParams = new URLSearchParams(window.location.search);
        let $events_dt_el = $("#events_rdt");

        let eventsDT = $events_dt_el.DataTable({
            "ajax": url,
            lengthChange: false,
            paging: false,
            pagingType: "simple",
            order: [[1, 'asc']],
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
                    "visible" : true,
                    "sortable": false,
                    "mRender": function (data,type,row) {
                        queryParams.set("e", row["id"]);
                        return '<a class="" style="color: #005cbf" href="search?' + queryParams + '">' + data + ' (' + row["counter"] + ')</a>';
                    }
                },
                {
                    "aTargets": [2],
                    "sortable": false,
                    "className" :"text-right"
                }
            ],
            "rowCallback": function( row, data ) {
                //sum = sum + parseInt(data.counter);
            },
            "initComplete": function() {
                if ( this.api().data().length === 0) {
                    $("#events_filter_row").hide();
                }
                var sum = eventsDT.column(2).data().sum();
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.set("rt", "e");
                queryParams.delete("skip");
                let _html =  '<tr>';
                _html += '<td style="width: 92%" class="pl-0"><a href="search?' + queryParams + '">Εκδήλωση</a></td>';
                _html += '<td class="text-right">' + sum + '</td></tr>';
                if (sum > 0) {
                    $("#table-typefilter").append(_html);
                }
            }
        });
    }

    dashboard.sevents.InitInstitutionEvents = function () {

        let $institutionEventsDtElem = $("#EventSelectDataTable");
        EventsDT = $institutionEventsDtElem.DataTable({
            "ajax": dashboard.siteUrl + '/api/v1/dt/scheduledEvents.web',
            "sDom": 'Zfrtip',
            "oListNav" : {
                sLetterClass : "btn abcdaire",
            },
            "columns": [
                {"data": "id"},
                {"data": "title"},
                {"data": "startDate"}
            ],
            "language":  dtLanguageGr,
            select: {
                style: 'single'
            },
            order: [[2, 'desc']],
            "pageLength": 10,
            "pagingType" : "full_numbers",
            "aoColumnDefs": [
                {
                    "aTargets": [0],
                    "mData": "id",
                    "visible": false,
                },
                {
                    "aTargets": [2],
                    "name": "startDate",
                    "render": function (data) {
                        let ret = "<div> "
                        if (data != null) {
                            let epochDate = data;
                            let formatted_date = moment.unix(epochDate).format('YYYY-MM-DD');
                            let display_date = moment.unix(epochDate).format('DD MMM YYYY');
                            ret += '<span style="display:none;">' + formatted_date + '</span>' + display_date  + '<input type="hidden" value="' + epochDate + '">';
                        }
                        ret += "</div>";
                        return ret;
                    }
                }
            ]
        });
    }; // Events DataTable Init

    dashboard.sevents.reloadInstitutionEvents = function() {
        EventsDT.ajax.reload();
    }
})();
