(function () {
    'use strict';

    dashboard.sevents = dashboard.sevents || {};

    dashboard.sevents.loadEventsByReport = function () {

        let url = dashboard.siteUrl + '/api/v1/getEventsOfReport';

        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.data, function (index, element) {
                    let eventDtId = element.id;
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("e", eventDtId);
                    queryParams.delete("skip");
                    let html = '<a class="list-group-item list-group-item-action  text-dark text-decoration-none" href="search?' + queryParams + '">' + element.title +' (' + element.counter + ')</a>';
                    $("#evFilters").append(html);
                });
                if (data.data.length < 1) {
                    $("#evFilters").hide();
                    $("#eventCanvasLink").hide();
                }
                else {
                    $("#eventCanvasLink").show();
                    $("#no_dyna_filters").hide();
                }
            }
        });
    }
})();
