(function () {
    'use strict';

    dashboard.type = dashboard.type || {};


    dashboard.type.init = function () {
    };

    dashboard.type.loadTypeByReport = function() {

        let html = '<li class="list-group-item">' +
            '<a class="text-dark text-decoration-none filter-item" href="#" data-filter="repeat" data-target="regular">Τακτική</a>' +
            '</li>';
             html += '<li class="list-group-item">' +
            '<a class="text-dark text-decoration-none filter-item" href="#" data-filter="repeat" data-target="onetime">Έκτακτη</a>' +
            '</li>';

        $("#repeatFilters").html(html);
        $("#repeatCanvasLink").show();

    }

    dashboard.type.loadPeriodByReport = function() {

        let html = '<li class="list-group-item">' +
            '<a class="text-dark text-decoration-none filter-item" href="#" data-filter="period" data-target="winter">Χειμερινό Εξάμηνο</a>' +
            '</li>';
        html += '<li class="list-group-item">' +
            '<a class="text-dark text-decoration-none filter-item" href="#" data-filter="period" data-target="intervening">Ενδιάμεση Περίοδος</a>' +
            '</li>';
        html += '<li class="list-group-item">' +
            '<a class="text-dark text-decoration-none filter-item" href="#" data-filter="period" data-target="spring">Εαρινό Εξάμηνο</a>' +
            '</li>';
        html += '<li class="list-group-item">' +
            '<a class="text-dark text-decoration-none filter-item" href="#" data-filter="period" data-target="summer">Καλοκαιρινή Περίοδος</a>' +
            '</li>';

        $("#periodFilters").html(html);
        $("#periodCanvasLink").show();

    }

    dashboard.type.loadDaysOfWeekByReport = function() {

        let queryParams = new URLSearchParams(window.location.search);
        let html = '<li class="list-group-item">' +
            '<a class="text-dark text-decoration-none filter-item" href="#" data-filter="dow" data-target="MONDAY">Δευτέρα</a>' +
            '</li>';
        queryParams.set("dow", "TUESDAY");
        queryParams.delete("skip");
        html += '<li class="list-group-item">' +
            '<a class="text-dark text-decoration-none filter-item" href="#" data-filter="dow" data-target="TUESDAY">Τρίτη</a>' +
            '</li>';
        queryParams.set("dow", "WEDNESDAY");
        queryParams.delete("skip");
        html += '<li class="list-group-item">' +
            '<a class="text-dark text-decoration-none filter-item" href="#" data-filter="dow" data-target="WEDNESDAY">Τετάρτη</a>' +
            '</li>';
        queryParams.set("dow", "THURSDAY");
        queryParams.delete("skip");
        html += '<li class="list-group-item">' +
            '<a class="text-dark text-decoration-none filter-item" href="#" data-filter="dow" data-target="THURSDAY">Πέμπτη</a>' +
            '</li>';
        queryParams.set("dow", "FRIDAY");
        queryParams.delete("skip");
        html += '<li class="list-group-item">' +
            '<a class="text-dark text-decoration-none filter-item" href="#" data-filter="dow" data-target="FRIDAY">Παρασκευή</a>' +
            '</li>';
        queryParams.set("dow", "SATURDAY");
        queryParams.delete("skip");
        html += '<li class="list-group-item">' +
            '<a class="text-dark text-decoration-none filter-item" href="#" data-filter="dow" data-target="SATURDAY">Σαββάτο</a>' +
            '</li>';
        queryParams.set("dow", "SUNDAY");
        queryParams.delete("skip");
        html += '<li class="list-group-item">' +
            '<a class="text-dark text-decoration-none filter-item" href="#" data-filter="dow" data-target="SUNDAY">Κυριακή</a>' +
            '</li>';

        $("#dowFilters").html(html);
        $("#dowCanvasLink").show();

    }

    dashboard.type.loadEventStatusByReport = function() {

        let html = '<li class="list-group-item">' +
            '<a class="text-dark text-decoration-none filter-item" href="#" data-filter="estatus" data-target="past">Ολοκληρωμένες</a>' +
            '</li>';
        html += '<li class="list-group-item">' +
            '<a class="text-dark text-decoration-none filter-item" href="#" data-filter="estatus" data-target="today">Σημερινές</a>' +
            '</li>';
        html += '<li class="list-group-item">' +
            '<a class="text-dark text-decoration-none filter-item" href="#" data-filter="estatus" data-target="future">Μελλοντικές</a>' +
            '</li>';

        $("#statusFilters").html(html);
        $("#statusCanvasLink").show();

    }

})();
