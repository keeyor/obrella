(function () {
    'use strict';

    dashboard.course = dashboard.course || {};

    let siteUrl;
    let coursesDT;
    let courseSelectDT;

    dashboard.course.loadCourseByReport = function () {

        let url = dashboard.siteUrl + '/api/v1/getCoursesOfReport';

        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.data, function (index, element) {
                    let courseDtId = element.id;
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("c", courseDtId);
                    queryParams.delete("skip");
                    let html =
                        '<a class="text-dark list-group-item list-group-item-action text-decoration-none" href="search?' + queryParams + '">' + element.title +' (' + element.counter + ')<br/>' +
                        '<div class="font-sm text-medium-emphasis"> Τμήμα ' + element.department.title + '</div></a>';
                    $("#coFilters").append(html);
                });
                if (data.data.length < 1) {
                    $("#coFilters").hide();
                    $("#courseCanvasLink").hide();
                }
                else {
                    $("#courseCanvasLink").show();
                    $("#no_dyna_filters").hide();
                }
            }
        });
    }

})();
