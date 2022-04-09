(function () {
    'use strict';

    dashboard.sevents = dashboard.sevents || {};

    let siteUrl;
    let $event_list;

    dashboard.sevents.init = function () {
        siteUrl = dashboard.siteUrl;
        $event_list = $('#events-list');
        dashboard.sevents.loadScheduledEvents();

    };

    dashboard.sevents.loadScheduledEvents = function () {

        let eventId = $("#eventId").val();
        let eventTitle = '';
        let url = siteUrl + '/api/v1/s2/scheduledEvents.web';
        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.results, function (index, el) {
                    if (el.id === eventId) {
                        eventTitle = el.text;
                    }
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("e", el.id);
                    queryParams.delete("d");
                    queryParams.delete("c");
                    queryParams.delete("s");
                    queryParams.delete("ft");
                    queryParams.delete("skip");
                    $('#events-list').append('<li><a class="dropdown-item text-wrap" href="daily?' + queryParams + '">' + el.text +'</a></li>');
                });
            }
        });

    }
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
            pagingType: "simple",
            paging: false,
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
                    "visible" : true,
                    "mRender": function (data,type,row) {
                        queryParams.set("e", row["id"]);
                        return '<a class="" style="color: #005cbf" href="daily?' + queryParams + '">' + data + ' (' + row["counter"] + ')</a>';
                    }
                },
                {
                    "aTargets": [2],
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
                _html += '<td style="width: 92%" class="pl-0"><a href="daily?' + queryParams + '">Εκδήλωση</a></td>';
                _html += '<td class="text-right">' + sum + '</td></tr>';
                if (sum > 0) {
                    $("#table-typefilter").append(_html);
                }
            }
        });
    }
})();
