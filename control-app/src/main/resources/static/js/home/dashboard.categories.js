(function () {
    'use strict';

    dashboard.categories = dashboard.categories || {};

    let siteUrl;
    let $category_list;
    let filter_list = ['ca','d','c','e','s','ft','skip'];
    let this_filter = 'ca';

    dashboard.categories.init = function () {
        $category_list  = $('#category-list');
        siteUrl = dashboard.siteUrl;
        dashboard.categories.lnklist();
    };

    dashboard.categories.lnklist = function () {

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
                            let filter_name =  category.split(":")[1];
                            let filter_code =  category.split(":")[0];
                            filter_list.forEach(function(value){
                                if (value === this_filter) {
                                    queryParams.set(value, filter_code);
                                }
                                else {
                                    queryParams.delete(value);
                                }
                            });
                            html +=  '<li><a class="dropdown-item" href="search?' + queryParams + '">' + filter_name +'</a></li>';
                      });
                    html += '</ul></li>';
                });
                $category_list.append(html);
            }
        });
    }

})();
