(function () {
    'use strict';

    dashboard.staffmembers = dashboard.staffmembers || {};

    dashboard.staffmembers.loadStaffByReport = function () {

        let url = dashboard.siteUrl + '/apiw/v1/getStaffOfReport';

        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.data, function (index, element) {
                    let staffDtId = element.id;
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("s", staffDtId);
                    queryParams.delete("skip");
                    let html = '<a class="list-group-item list-group-item-action text-dark text-decoration-none" href="search?' + queryParams + '">' + element.name + '<br/>' +
                        '<div class="font-sm text-medium-emphasis"> Τμήμα ' + element.department.title + '</div></a>';
                    $("#stFilters").append(html);
                });
                if (data.data.length < 1) {
                    $("#stFilters").hide();
                    $("#staffCanvasLink").hide();
                }
                else {
                    $("#staffCanvasLink").show();
                    $("#no_dyna_filters").hide();
                }
            }
        });
    }

})();
