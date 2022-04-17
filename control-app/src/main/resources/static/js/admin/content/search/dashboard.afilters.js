(function () {
    'use strict';

    dashboard.afilters = dashboard.afilters || {};

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
                        html += '<a class="list-group-item list-group-item-action  text-dark text-decoration-none" href="search?' + queryParams + '"> Δημόσιο (' + element.counter + ')</a>';
                    }
                    else {
                        html += '<a class="list-group-item list-group-item-action text-dark text-decoration-none" href="search?' + queryParams + '"> Ιδιωτικό (' + element.counter + ')</a>';
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

    dashboard.afilters.loadResourceTypeCounterFiltersByReport = function () {

        let url = dashboard.siteUrl + '/api/v1/getResourceTypeCounterOfReport';

        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.data, function (index, element) {
                    let type = element.type;
                    if (type === "lecture") type = "c";
                    else type = "e";
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("rt", type);
                    queryParams.delete("skip");
                    let html = "";
                    if (type === "c") {
                        html += '<a class="list-group-item list-group-item-action  text-dark text-decoration-none" href="search?' + queryParams + '"> Διάλεξη (' + element.counter + ')</a>';
                    }
                    else {
                        html += '<a class="list-group-item list-group-item-action  text-dark text-decoration-none" href="search?' + queryParams + '"> Πολυμέσο Εκδήλωσης (' + element.counter + ')</a>';
                    }
                    $("#rtFilters").append(html);
                });
                if (data.data.length < 1) {
                    $("#rtFilters").hide();
                    $("#rtCanvasLink").hide();
                }
                else {
                    $("#rtCanvasLink").show();
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
                    html += '<a class="list-group-item list-group-item-action text-dark text-decoration-none" href="search?' + queryParams + '"> Απαιτείται Αποδοχή</a>';
                    access = "MetEdt";
                    queryParams.set("t", access);
                    queryParams.delete("skip");
                    html += '<a class="list-group-item list-group-item-action text-dark text-decoration-none" href="search?' + queryParams + '"> Απαιτείται Επεξεργασία Βίντεο</a>';
                    access = "PreUp";
                    queryParams.set("t", access);
                    queryParams.delete("skip");
                    html += '<a class="list-group-item list-group-item-action text-dark text-decoration-none" href="search?' + queryParams + '"> Απαιτείται Μεταφόρτωση Παρουσίασης</a>';
                    access = "MultUp";
                    queryParams.set("t", access);
                    queryParams.delete("skip");
                    html += '<a class="list-group-item list-group-item-action text-dark text-decoration-none" href="search?' + queryParams + '"> Απαιτείται Μεταφόρτωση Βίντεο</a>';
                    access = "MultEdt";
                    queryParams.set("t", access);
                    queryParams.delete("skip");
                    html += '<a class="list-group-item list-group-item-action text-dark text-decoration-none" href="search?' + queryParams + '"> Απαιτείται Επεξεργασία Βίντεο</a>';
                    access = "MultRed";
                    queryParams.set("t", access);
                    queryParams.delete("skip");
                    html += '<a class="list-group-item list-group-item-action text-dark text-decoration-none" href="search?' + queryParams + '"> Απαιτείται Πραγματική Κοπή</a>';
                    access = "PreSyn";
                    queryParams.set("t", access);
                    queryParams.delete("skip");
                    html += '<a class="list-group-item list-group-item-action text-dark text-decoration-none" href="search?' + queryParams + '"> Απαιτείται Συγχρονισμός</a>';

                    $("#tagFilters").append(html);
                    $("#tagCanvasLink").show();

            }


})();
