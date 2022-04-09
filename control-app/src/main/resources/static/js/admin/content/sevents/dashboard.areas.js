(function () {
    'use strict';

    dashboard.areas = dashboard.areas || {};

    let siteUrl;
    let $area_list;
    let $etype_list;

    dashboard.areas.init = function () {
        siteUrl          = dashboard.siteUrl;
        dashboard.areas.loadAreasOnSearchBar();
    };

    dashboard.areas.loadAreasOnSearchBar = function () {

        $area_list = $('#area-columns');
        let queryParams = new URLSearchParams(window.location.search);

        let url = siteUrl + '/api/v1/s2/areas.web';
        let html = '';
        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {

                $.each(data.results, function (index, el) {
                        queryParams.set("ea", el.id);
                        queryParams.delete("skip");
                        queryParams.delete("ca");
                        queryParams.delete("up");
                        queryParams.delete("c");
                        queryParams.delete("s");
                        queryParams.delete("e");
                        queryParams.delete("ft");
                        html +=  '<li><a class="dropdown-item filter-item" href="?' + queryParams + '">' + el.text +'</a></li>';
                });

                $area_list.append(html);
            }
        });
    }

    dashboard.areas.loadEventTypesOnSearchBar = function (area) {

        $etype_list = $('#etype-columns');
        let queryParams = new URLSearchParams(window.location.search);

        let url = siteUrl + '/api/v1/s2/eventarea.web/' + area;
        let html = '';
        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {

                $.each(data.results, function (index, el) {
                    queryParams.set("et", el.id);
                    queryParams.delete("skip");
                    queryParams.delete("ca");
                    queryParams.delete("up");
                    queryParams.delete("c");
                    queryParams.delete("s");
                    queryParams.delete("e");
                    queryParams.delete("ft");
                    html +=  '<li><a class="dropdown-item filter-item" href="?' + queryParams + '">' + el.text +'</a></li>';
                });

                $etype_list.append(html);
            }
        });
    }


})();
