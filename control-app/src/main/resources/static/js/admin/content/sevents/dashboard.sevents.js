(function () {
    'use strict';

    dashboard.sevents = dashboard.sevents || {};

    dashboard.sevents.loadAreasByReport = function () {

        let url = dashboard.siteUrl + '/api/v1/getAreasOfReport';

        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.data, function (index, element) {
                    let eventDtId = element.id;
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("ea", eventDtId);
                    queryParams.delete("skip");
                    let html = '<a class="list-group-item list-group-item-action text-dark text-decoration-none" href="sevents?' + queryParams + '">' + element.text +' (' + element.counter + ')</a>';
                    $("#areaFilters").append(html);
                });
                if (data.data.length < 1) {
                    $("#areaFilters").hide();
                    $("#areaCanvasLink").hide();
                }
                else {
                    $("#areaCanvasLink").show();
                    $("#no_dyna_filters").hide();
                }
            }
        });
    }

    dashboard.sevents.loadTypesByReport = function () {


        let url = dashboard.siteUrl + '/api/v1/getTypesOfReport';

        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.data, function (index, element) {
                    let eventDtId = element.id;
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("et", eventDtId);
                    queryParams.delete("skip");
                    let html = '<a class="list-group-item list-group-item-action text-dark text-decoration-none" href="sevents?' + queryParams + '">' + element.text +' (' + element.counter + ')</a>' +
                    '</li>';
                    $("#typeFilters").append(html);
                });
                if (data.data.length < 1) {
                    $("#typeFilters").hide();
                    $("#typeCanvasLink").hide();
                }
                else {
                    $("#typeCanvasLink").show();
                    $("#no_dyna_filters").hide();
                }
            }
        });
    }

    dashboard.sevents.loadCategoriesByReport = function () {


        let url = dashboard.siteUrl + '/api/v1/getEventCategoriesOfReport';

        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.data, function (index, element) {
                    let eventDtId = element.id;
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("ec", eventDtId);
                    queryParams.delete("skip");
                    let html = '<a class="list-group-item list-group-item-action text-dark text-decoration-none" href="sevents?' + queryParams + '">' + element.text +' (' + element.counter + ')</a>';
                    $("#catFilters").append(html);
                });
                if (data.data.length < 1) {
                    $("#catFilters").hide();
                    $("#catCanvasLink").hide();
                }
                else {
                    $("#catCanvasLink").show();
                    $("#no_dyna_filters").hide();
                }
            }
        });
    }

})();