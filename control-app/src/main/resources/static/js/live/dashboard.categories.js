(function () {
    'use strict';

    dashboard.categories = dashboard.categories || {};

    let siteUrl;
    let $category_list;
    let $category_code;
    let $category_title;
    let $category_dd_header;
    let $category_filter_link;
    let $category_filter_alert;

    dashboard.categories.init = function () {
        $category_list          = $('#category-list');
        $category_code          = $("#categoryCode");
        $category_title         = $("#categoryTitle");
        $category_dd_header     = $('#category-dd-header');
        $category_filter_link   = $("#clear-ca-filter");
        $category_filter_alert  = $("#category-filter");
        siteUrl = dashboard.siteUrl;
        dashboard.categories.loadcategories();
    };

    dashboard.categories.loadcategories = function () {

        let html ='';
        let queryParams = new URLSearchParams(window.location.search);
        let url = siteUrl + '/api/v1/s2/categories.web';
        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.results, function (index, element) {
                      let index_name = index.split(":")[1];
                      html += '<li class="dropdown-submenu">';
                      html += '<a class="dropdown-item dropdown-toggle" href="#">' + index_name + '</a>';
                      html += '<ul class="dropdown-menu">';
                      $.each(element, function (index, category) {
                            let cat_name =  category.split(":")[1];
                            let cat_code =  category.split(":")[0];
                            queryParams.set("ca", cat_code);
                            queryParams.delete("d");
                            queryParams.delete("c");
                            queryParams.delete("e");
                            queryParams.delete("s");
                            queryParams.delete("ft");
                            queryParams.delete("skip");
                            html +=  '<li><a class="dropdown-item" style="font-size: 0.8em" href="search?' + queryParams + '">' + cat_name +'</a></li>';;
                      });
                    html += '</ul></li>';
                });
                $category_list.append(html);
            }
        });
    }

})();
