(function () {
    'use strict';

    window.dashboard = window.dashboard || {};
    dashboard.broker = $({});

    dashboard.siteUrl   = "";
    dashboard.app_path  = "";

    dashboard.init = function () {

        dashboard.siteUrl = dashboard.broker.getRootSitePath();
        dashboard.departments.init();

        $("#refresh_table").on('click',function(e){
            dashboard.broker.getLiveNowLectures();
            dashboard.broker.getScheduledForTodayLectures();
            e.preventDefault();
        });
     };

    $(document).ready(function () {

        $("#department-columns-live").on('click','a.dropdown-toggle', function() {
            if (!$(this).next().hasClass('show')) {
                $(this).parents('.dropdown-menu').first().find('.show').removeClass('show');
            }
            var $subMenu = $(this).next('.dropdown-menu');
            $subMenu.toggleClass('show');

            $(this).parents('li.nav-item.dropdown.show').on('hidden.bs.dropdown', function() {
                $('.dropdown-submenu .show').removeClass('show');
            });
            return false;
        });
        let localeCode = $("#localeCode").val();
        moment.locale(localeCode);
        let today = moment().format("LL");
        $("#daily_date_now").html(today);
        dashboard.init();
    });

    dashboard.broker.getLiveNowLectures = function() {
        $.ajax({
            url: dashboard.siteUrl + '/api/v1/live/liveNowLecturesCounter',
            cache: false
        })
        .done(function( data ) {
            $("#live_counter").text(data);
        });
    }
    dashboard.broker.getScheduledForTodayLectures = function() {
        $.ajax({
            url: dashboard.siteUrl + '/api/v1/live/ScheduledLecturesForTodayCounter',
            cache: false
        })
            .done(function( data ) {
                $("#scheduled_today").text(data);
            });
    }

    dashboard.broker.getRootSitePath = function () {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let applicationName = _location.substring(0, applicationNameIndex) + '/';
        let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

        return _location.substring(0, webFolderIndex);
    };
    function checkTime(i) {
        if (i < 10) {
            i = "0" + i;
        }
        return i;
    }
    function startTime() {
        var today = new Date();
        var h = today.getHours();
        var m = today.getMinutes();
        var s = today.getSeconds();
        // add a zero in front of numbers<10
        m = checkTime(m);
        s = checkTime(s);
        document.getElementById('time').innerHTML = ' ' +  h + ":" + m + ":" + s;
        setTimeout(function() {
            startTime();
            if (s === "00") {
                dashboard.broker.getLiveNowLectures();
                dashboard.broker.getScheduledForTodayLectures();
            }
        }, 500);
    }
  //  startTime();

})();
