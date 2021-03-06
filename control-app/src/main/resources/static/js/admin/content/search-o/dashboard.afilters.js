(function () {
    'use strict';

    dashboard.afilters = dashboard.afilters || {};

    dashboard.afilters.init = function () {

    };

    dashboard.afilters.loadApFiltersByReport = function () {


        let _html = "";
        let queryParams = new URLSearchParams(window.location.search);
        let url = dashboard.siteUrl + '/api/v1/getAccessPolicyOfReport';
        $.ajax({
            type:        "GET",
            url: 		  url,
            success: function(data){
                data.data.forEach(function(item) {
                    //console.log(item.access + " " + item.counter);
                    queryParams.set("ap", item.access);
                    queryParams.delete("skip");
                    _html +=  '<tr>';
                    if (item.access === "public") {
                      _html += '<td style="width: 92%" class="pl-0"><a  style="color: #005cbf" href="?' + queryParams + '">Δημόσια' + ' (' + item.counter + ')</a>';
                    }
                    else if (item.access === "private") {
                      _html += '<td style="width: 92%" class="pl-0"><a  style="color: #005cbf" href="?' + queryParams + '">Ιδιωτική' + ' (' + item.counter + ')</a>';
                    }
                });
                $("#table-apfilter").append(_html);
            },
            error: function (jqXHR, textStatus, errorThrown)  {

            }
        });

    }
})();
