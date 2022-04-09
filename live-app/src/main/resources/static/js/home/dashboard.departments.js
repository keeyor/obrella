(function () {
    'use strict';

    dashboard.departments = dashboard.departments || {};

    let siteUrl;
    let $department_list_content;
    let $department_list_live;

    dashboard.departments.init = function () {
        siteUrl          = dashboard.siteUrl;
        dashboard.departments.loadDepartmentsOnSearchBar();
    };

    dashboard.departments.loadDepartmentsOnSearchBar = function () {

        $department_list_content = $('#department-columns-content');
        $department_list_live = $('#department-columns-live');
        siteUrl          = dashboard.siteUrl;
        let queryParams = new URLSearchParams(window.location.search);

        let url = siteUrl + '/api/v1/s2/departments.web/school/dummy';
        let html_live = '';
        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.results, function (index, element) {
                    html_live += '<li class="dropdown-submenu">';
                    html_live += '<a class="dropdown-item dropdown-toggle" style="font-size: 0.88em" href="#">' + element.text + '</a>';
                    html_live += '<ul class="dropdown-menu">';
                    $.each(element.children, function (index1, el) {
                        queryParams.set("d", el.id);
                        queryParams.delete("skip");
                        queryParams.delete("ca");
                        queryParams.delete("c");
                        queryParams.delete("s");
                        queryParams.delete("e");
                        queryParams.delete("ft");
                        html_live +=  '<li><a class="dropdown-item" style="font-size: 0.88em"  href="list?' + queryParams + '">' + el.text +'</a></li>';
                    });
                    html_live += '</ul></li>';
                });
                $department_list_live.append(html_live);
            }
        });
    }
})();
