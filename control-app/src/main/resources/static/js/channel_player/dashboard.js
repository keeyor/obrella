$(function () {
    'use strict';

    window.dashboard = window.dashboard || {};
    dashboard.broker = $({});
    dashboard.siteUrl   = "";

    let debug = 0;
    let mode;
    let intro_url;
    let video_url;
    let poster_image;
    let daily_schedule;

    $(document).ready(function () {

        dashboard.init();
        dashboard.broker.on('loaded.video', function () {
            set_on_play_controls();
        });

        $("#refresh_page").on('click',function(){
            dashboard.broker.getChannelTimeTable();
        })
    });

    dashboard.init = function () {
        dashboard.siteUrl = dashboard.broker.getRootSitePath();
        intro_url    = "false";
        video_url    = $("#video_url").val();
        poster_image = "";
        mode         = "sync_on";
        dashboard.player.init(video_url);
        dashboard.broker.getChannelTimeTable();
        startTime();
    }

    dashboard.broker.getChannelTimeTable = function () {

        let _url = dashboard.siteUrl + "/api/v1/channel/timetable/today";
        let now_epoch = moment().unix();
        $.ajax({
            type:        "GET",
            url: 		  _url,
            async:		  true,
            success: function(data) {
                let html = "", html_past = "", html_future = "", html_live="", html_today="";
                daily_schedule = data;
                if (data.length === 0) {
                    html = ' <div>Δεν βρέθηκαν προγραμματισμένες μεταδόσεις</div>';
                }
                else {
                    data.forEach(function (entry, index) {

                        if (now_epoch > entry.endTime) {
                            html_today += getMediaHtml(entry, index, 'past');
                        } else if (now_epoch > entry.startTime && now_epoch < entry.endTime) {
                            html_live += getMediaHtml(entry, index, 'live');
                            html_today += getMediaHtml(entry, index, 'live');
                        } else {
                            html_future += getMediaHtml(entry, index, 'future');
                            html_today += getMediaHtml(entry, index, 'future');
                        }
                    });
                    if (html_live === "")   { html_live += ' <div class="text-center">Δεν βρέθηκαν ζωντανές μεταδόσεις</div> '; }
                    if (html_past === "")   { html_past += ' <div class="text-center">Δεν βρέθηκαν προγραμματισμένες μεταδόσεις</div> '; }
                    if (html_future === "") { html_future += ' <div class="text-center">Δεν βρέθηκαν προγραμματισμένες μεταδόσεις</div> '; }
                    if (html_today=== "")   { html_today += ' <div class="text-center">Δεν βρέθηκαν προγραμματισμένες μεταδόσεις</div> '; }

                }
                $("#channel_schedule_live").html(html_live);
                $("#channel_schedule_today").html(html_today);
                $("#channel_schedule_future").html(html_future);
            },
            error: function ()  {
                console.log("error getChannelTimeTable");
            }
        });
    };

    function getMediaHtml(entry, index, type) {
        let html = '<div class="media mb-3">';
        html += '<svg class="bd-placeholder-img mr-3" width="100" height="80" ' +
            'xmlns="http://www.w3.org/2000/svg" aria-label="Placeholder: 100x80" ' +
            'preserveAspectRatio="xMidYMid slice" role="img"><title>Placeholder</title> ' +
            '<rect width="100%" height="100%" fill="#868e96"/> ' +
            '<text x="30%" y="50%" fill="#dee2e6" dy=".3em">100x80</text> ' +
            '</svg>';
        html += '<div class="media-body">';
        html +=  '<h6  id="entry_' + index + '">' + entry.title + '</h6>';
        html += '<span>Ωρα Έναρξης: ' + moment.unix(entry.startTime).format('LLL') + '</span>';
        html += '<span> - Τοποθεσία: ' + entry.description + '</span>';
        if (type === "past")
        html += '<div class="text-right">πληροφορίες - σελίδα εκδήλωσης - περιεχόμενο</div>';
        if (type === "future")
            html += '<div class="text-right">πληροφορίες - σελίδα εκδήλωσης</div>';
        if (type === "live")
            html += '<div class="text-right">αναπαραγωγή</div>';
        html += '</div>';
        html += '</div>';

        return html;
    }

    function set_on_play_controls() {

        let $playPauseVideoBt = $("#playPauseVideoBt");
        $playPauseVideoBt.focus();
        dashboard.player.mode = "sync_off";
    }

    function startTime() {
        let today = new Date();
        let h = today.getHours();
        let m = today.getMinutes();
        let s = today.getSeconds();
        // add a zero in front of numbers<10
        m = checkTime(m);
        s = checkTime(s);
        document.getElementById('time').innerHTML = moment(new Date()).format('LL') + ' ' +  h + ":" + m + ":" + s;
        let overlay = document.getElementById('channel_overlay');
        setTimeout(function() {
            startTime();
            let now_epoch = moment().unix();
            daily_schedule.forEach(function (entry, index) {

                if (now_epoch === entry.startTime) {
                    overlay.innerHTML= "Ζωντανή Μετάδοση: σύνδεση με " + entry.description + "<br/><small>Αν δεν συνδεθείτε αυτόματα μετά από 1΄, ανανεώστε τη σελίδα (F5)</small>";
                    overlay.style.visibility = 'visible';
                    dashboard.player.detachMedia();
                }
                if (now_epoch === entry.startTime+10) {
                    setTitleInfo(entry);
                    overlay.style.visibility = 'hidden';
                    overlay.innerHTML= "";
                    dashboard.player.initializePlayer();
                }
                if (now_epoch > entry.startTime && now_epoch < entry.endTime) {
                   $("#channel_schedule li:nth-child(" + (index+1) +")").css("font-weight","bold");
                   setTitleInfo(entry);
                }
                else if (now_epoch === entry.endTime) {
                    unsetTitleInfo();
                    dashboard.player.detachMedia();
                    overlay.innerHTML= "Η ζωντανή μετάδοση ολοκληρώθηκε... Επιστροφή σε Μαγνητοσκοπημένο Πρόγραμμα";
                    overlay.style.visibility = 'visible';
                    setTimeout(function () {
                            overlay.innerHTML= "";
                            overlay.style.visibility = 'hidden';
                            dashboard.player.initializePlayer();
                    }, 5000);
                }
                else if (now_epoch > entry.endTime) {
                  $("#channel_schedule li:nth-child(" + (index+1) +")").css("font-weight","normal");
                  unsetTitleInfo();
                }
            });
        }, 500);
    }

    function checkTime(i) {
        if (i < 10) {
            i = "0" + i;
        }
        return i;
    }

    function setTitleInfo(entry) {
        $("#current_video_title").html('<span><i class="icon-live-lecture" style="color: red"></i> ' + entry.title + '</span>');
        $("#current_video_description").text(entry.description);
    }
    function unsetTitleInfo() {
        $("#current_video_title").html('<span>ΕΚΠΑ: Κανάλι Εκδηλώσεων</span>');
        $("#current_video_description").text('[ Μαγνητοσκοπημένο Πρόγραμμα ]');
    }

});