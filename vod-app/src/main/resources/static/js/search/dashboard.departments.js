(function () {
    'use strict';

    dashboard.departments = dashboard.departments || {};

    let $department_list;
    let departmentId;
    let departmentTitle;
    let departmentsDT;

    dashboard.departments.init = function () {
        $department_list = $('#department-list');//only for dropdown
        dashboard.departments.loadDepartmentsOnSearchBar();
    };

    dashboard.departments.loadDepartmentsOnSearchBar = function () {

        $department_list = $('#department-columns');
        let queryParams = new URLSearchParams(window.location.search);

        let url = dashboard.siteUrl + '/apiw/v1/s2/departments.web/school/dummy';
        let html = '';
        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.results, function (index, element) {
                    html += '<li class="dropdown-submenu">';
                    html += '<a class="dropdown-item dropdown-toggle" href="#">' + element.text + '</a>';
                    html += '<ul class="dropdown-menu">';
                    $.each(element.children, function (index1, el) {
                        queryParams.set("d", el.id);
                        queryParams.delete("skip");
                        queryParams.delete("ca");
                        queryParams.delete("c");
                        queryParams.delete("s");
                        queryParams.delete("e");
                        queryParams.delete("ft");
                        html +=  '<li><a class="dropdown-item" href="search?' + queryParams + '">' + el.text +'</a></li>';
                    });
                    html += '</ul></li>';
                });
                $department_list.append(html);
            }
        });
    }

    dashboard.departments.loadDepartmentsByReport = function () {

        let url = dashboard.siteUrl + '/apiw/v1/getDepartmentsOfReport';
        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.data, function (index, element) {
                    let depDtId = element.id;
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("d", depDtId);
                    queryParams.delete("skip");
                    let html = '<a class="list-group-item list-group-item-action text-dark text-decoration-none" href="search?' + queryParams + '">' + element.title + '</a>';
                    $("#dpFilters").append(html);
                });
                if (data.data.length < 1) {
                    $("#dpFilters").hide();
                    $("#depCanvasLink").hide();
                }
                else {
                    $("#depCanvasLink").show();
                    $("#no_dyna_filters").hide();
                }
            }
        });

    }


})();
