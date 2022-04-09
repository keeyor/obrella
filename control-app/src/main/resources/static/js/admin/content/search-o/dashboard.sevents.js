(function () {
    'use strict';

    dashboard.sevents = dashboard.sevents || {};

    let siteUrl;
    let EventsDT;
    let EventsReportDT;

    dashboard.sevents.init = function () {
        siteUrl = dashboard.siteUrl;
        //dashboard.sevents.loadScheduledEvents();

        $("#event_list").on('click',function(e){
            if ( ! $.fn.DataTable.isDataTable( '#EventSelectDataTable' ) ) {
                dashboard.sevents.InitInstitutionEvents();
            }
            else {
                dashboard.sevents.reloadInstitutionEvents();
            }
            $("#EventSelectModal").modal('show');
            e.preventDefault();
        });

        $("#applyEventFilter").on('click',function(){
            let eventSelectDT = $("#EventSelectDataTable").DataTable();
            let nodes= eventSelectDT.rows( { selected: true } ).data();
            let eventId;
            if (nodes.length>0) {
                for (let l = 0; l < nodes.length; l++) {
                    let _row = nodes[l];
                    eventId = _row.id;
                }
                $("#EventSelectModal").modal('hide');
                location.href = "search?e=" + eventId;
            }
        });
    };

    dashboard.sevents.InitInstitutionEvents = function () {

        let $institutionEventsDtElem = $("#EventSelectDataTable");
        let url = siteUrl + '/api/v2/dt/scheduledEvents.web/authorized/content/_all';
        EventsDT = $institutionEventsDtElem.DataTable({
            "ajax": url,
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
            pagingType: "full_numbers",

            "pageLength": 10,
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
                            let display_date = moment.unix(epochDate).format('LL');
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


/*    dashboard.sevents.loadScheduledEvents = function () {

        let eventId = $("#eventId").val();
        let eventTitle = '';
        let url = siteUrl + '/api/v2/s2/scheduledEvents.web/authorized/content/_all';
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
                    $('#events-list').append('<li><a class="dropdown-item text-wrap" href="?' + queryParams + '">' + el.text +'</a></li>');
                });
            }
        });
    }*/

    dashboard.sevents.loadEventsByReport = function () {

        let sum = 0;
        siteUrl = dashboard.siteUrl;

        $('#evSearchText').on( 'keyup', function () {
            EventsReportDT.search( this.value ).draw();
        } );
        $('#clear-ev-search-text').on( 'click', function () {
            $("#evSearchText").val('');
            EventsReportDT.search( '' ).draw();
        } );

        $(document).on('init.dt', function ( e, settings ) {
            /*var api = new $.fn.dataTable.Api(settings);
            var state = api.state.loaded();
            var searchText = state.search.search;
            $("#crSearchText").val(searchText);*/
            $("#evSearchText").val('');
            EventsReportDT.search('').draw();
        });

        let url = siteUrl + '/api/v1/getEventsOfReport';
        let queryParams = new URLSearchParams(window.location.search);
        let $events_dt_el = $("#events_rdt");

        let EventsReportDT = $events_dt_el.DataTable({
            "ajax": url,
            pagingType: "simple",
            paging: false,
            lengthChange: false,
            order: [[1, 'asc']],
            "dom": '<"top">rt<"bottom">ip<"clear">',
            "language": {
                "search": "",
                "sInfo": "Εμφάνιση από _START_ έως _END_ από _TOTAL_",
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
                        return '<a class="" style="color: #005cbf" href="search?' + queryParams + '">' + data + ' (' + row["counter"] + ')</a>';
                    }
                }
            ],
            "rowCallback": function( row, data ) {
                //sum = sum + parseInt(data.counter);
            },
            "initComplete": function() {
                if ( this.api().data().length === 0) {
                    $("#events_filter_row").hide();
                }
                var sum = EventsReportDT.column(2).data().sum();
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.set("rt", "e");
                queryParams.delete("skip");
                let _html =  '<tr>';
                _html += '<td style="width: 92%" class="pl-0"><a  style="color: #005cbf" href="?' + queryParams + '">Πολυμέσο Εκδήλωσης' + ' (' + sum + ')</a>';
                if (sum > 0) {
                    $("#table-typefilter").append(_html);
                }
            }
        });
    }
})();
