(function () {
    'use strict';

    dashboard.departments = dashboard.departments || {};

    dashboard.departments.loadDepartmentsByReport = function () {

        let url = dashboard.siteUrl + '/api/v1/getDepartmentsOfReport';

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
                    let html = '<a class="list-group-item list-group-item-action text-dark text-decoration-none" href="search?' + queryParams + '">' + element.title +' (' + element.counter + ')</a>';
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
