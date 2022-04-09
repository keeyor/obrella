(function () {
    'use strict';

    dashboard.afilters = dashboard.afilters || {};

    dashboard.afilters.init = function () {

    };

    dashboard.afilters.loadApFiltersByReport = function () {

        let url = dashboard.siteUrl + '/api/v1/getAccessPolicyOfReport';

        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.data, function (index, element) {
                    let access = element.access;
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("ap", access);
                    queryParams.delete("skip");
                    let html = "";
                    if (access === "public") {
                        html += '<li class="list-group-item">' +
                            '<a class="text-dark text-decoration-none" href="search?' + queryParams + '"> Δημόσιο (' + element.counter + ')</a>' +
                            '</li>';
                    }
                    else {
                        html += '<li class="list-group-item">' +
                            '<a class="text-dark text-decoration-none" href="search?' + queryParams + '"> Ιδιωτικό (' + element.counter + ')</a>' +
                            '</li>';
                    }
                    $("#apFilters").append(html);
                });
                if (data.data.length < 1) {
                    $("#apFilters").hide();
                    $("#apCanvasLink").hide();
                }
                else {
                    $("#apCanvasLink").show();
                    $("#no_dyna_filters").hide();
                }
            }
        });
    }

    dashboard.afilters.loadTags = function () {
                let html = "";

                    let queryParams = new URLSearchParams(window.location.search);
                    let access = "ResApp";
                    queryParams.set("t", access);
                    queryParams.delete("skip");
                    html += '<li class="list-group-item">' +
                            '<a class="text-dark text-decoration-none" href="search?' + queryParams + '"> Απαιτείται Αποδοχή</a>' +
                            '</li>';
                    access = "MetEdt";
                    queryParams.set("t", access);
                    queryParams.delete("skip");
                    html += '<li class="list-group-item">' +
                        '<a class="text-dark text-decoration-none" href="search?' + queryParams + '"> Απαιτείται Επεξεργασία Βίντεο</a>' +
                            '</li>';
                    access = "PreUp";
                    queryParams.set("t", access);
                    queryParams.delete("skip");
                    html += '<li class="list-group-item">' +
                        '<a class="text-dark text-decoration-none" href="search?' + queryParams + '"> Απαιτείται Μεταφόρτωση Παρουσίασης</a>' +
                        '</li>';
                    access = "MultUp";
                    queryParams.set("t", access);
                    queryParams.delete("skip");
                    html += '<li class="list-group-item">' +
                        '<a class="text-dark text-decoration-none" href="search?' + queryParams + '"> Απαιτείται Μεταφόρτωση Βίντεο</a>' +
                        '</li>';

                    access = "MultEdt";
                    queryParams.set("t", access);
                    queryParams.delete("skip");
                    html += '<li class="list-group-item">' +
                        '<a class="text-dark text-decoration-none" href="search?' + queryParams + '"> Απαιτείται Επεξεργασία Βίντεο</a>' +
                        '</li>';
                    access = "MultRed";
                    queryParams.set("t", access);
                    queryParams.delete("skip");
                    html += '<li class="list-group-item">' +
                        '<a class="text-dark text-decoration-none" href="search?' + queryParams + '"> Απαιτείται Πραγματική Κοπή</a>' +
                        '</li>';
                    access = "PreSyn";
                    queryParams.set("t", access);
                    queryParams.delete("skip");
                    html += '<li class="list-group-item">' +
                        '<a class="text-dark text-decoration-none" href="search?' + queryParams + '"> Απαιτείται Συγχρονισμός</a>' +
                        '</li>';

                    $("#tagFilters").append(html);
                    $("#tagCanvasLink").show();

            }


})();
