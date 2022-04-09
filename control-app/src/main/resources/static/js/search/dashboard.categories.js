(function () {
    'use strict';

    dashboard.categories = dashboard.categories || {};

    let $category_list;
    let filter_list = ['ca','d','c','e','s','ft','skip'];
    let this_filter = 'ca';

    dashboard.categories.init = function () {
        $category_list  = $('#category-list');//only for dropdown
        dashboard.categories.lnklist();
    };

    dashboard.categories.lnklist = function () {

        let html ='';
        let queryParams = new URLSearchParams(window.location.search);
        let url = dashboard.siteUrl + '/api/v1/s2/categories.web';
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

    dashboard.categories.loadCategoriesInColumns= function () {

        let $category_list  = $('#category-list-alt');
        let html ='';
        let queryParams = new URLSearchParams(window.location.search);
        let url = dashboard.siteUrl + '/api/v1/s2/categoriesAlt.web';
        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                let idx=0;
                $.each(data.results, function (index, element) {
                    if (index === 0) {
                        html += '<div class="col-lg-3 col-md-6 col-xs-12">';
                    }
                    if (index !== 0 && idx >= 18) {
                        html += '</div><div class="col-lg-3 col-md-6 col-xs-12">';
                        idx = 0;
                    }
                    html += '<h6>' +  element.text + '</h6>';
                    html += '<ul>';
                    $.each(element.children, function (index1, el) {
                            queryParams.set("ca", el.id);
                            queryParams.delete("skip");
                            queryParams.delete("c");
                            queryParams.delete("d");
                            queryParams.delete("s");
                            queryParams.delete("e");
                            queryParams.delete("ft");
                            html +=  '<li class="pl-2"><i style="font-size: 0.5em" class="far fa-circle fa-xs mr-1"></i><a style="color:#4f5d73" href="search?' + queryParams + '">' + el.text +'</a></li>';
                            idx++;
                    });
                    html += "</ul>";
                    idx++;
                });
                $category_list.append(html);
            }
        });
    }

})();
