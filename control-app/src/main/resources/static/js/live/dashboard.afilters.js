(function () {
    'use strict';

    dashboard.afilters = dashboard.afilters || {};

    dashboard.afilters.init = function () {

    };

    dashboard.afilters.loadApFiltersByReport = function () {

        let public_html = "";
        let _html = "";
        let queryParams = new URLSearchParams(window.location.search);
        let url = dashboard.siteUrl + '/api/v1/getAccessPolicyOfReport';
        $.ajax({
            type:        "GET",
            url: 		  url,
            success: function(data){
                data.data.forEach(function(item) {
                    console.log(item.access + " " + item.counter);
                    queryParams.set("ap", item.access);
                    queryParams.delete("skip");
                    _html +=  '<tr>';
                    if (item.access === "public") {
                      _html += '<td style="width: 92%" class="pl-0"><a href="live?' + queryParams + '">Δημόσια</a></td>';
                    }
                    else if (item.access === "private") {
                      _html += '<td style="width: 92%" class="pl-0"><a href="live?' + queryParams + '">Ιδιωτική</a></td>';
                    }
                    else if (item.access === "open") {
                        _html += '<td style="width: 92%" class="pl-0"><a href="live?' + queryParams + '">Ανοικτή</a></td>';
                    }
                    else if (item.access === "sso") {
                        _html += '<td style="width: 92%" class="pl-0"><a href="live?' + queryParams + '">Ιδρυματικός Λογαριασμός</a></td>';
                    }
                    else if (item.access === "password") {
                        _html += '<td style="width: 92%" class="pl-0"><a href="live?' + queryParams + '">Κωδικός Πρόσβασης</a></td>';
                    }
                     _html += '<td class="text-right">' + item.counter + '</td></tr>';
                });
                $("#table-apfilter").append(_html);
            },
            error: function (jqXHR, textStatus, errorThrown)  {

            }
        });

    }
})();
