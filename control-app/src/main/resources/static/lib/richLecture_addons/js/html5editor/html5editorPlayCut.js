cuepoints_for_cutted_section = [];
var pp_cut;
var webRoot = "";
let video_url;
let video_duration;
let video_start_sec;
let video_end_sec;
let provider_type;
let debug = 0;
let cuepoints_m = [];
let url_params;
let seek_times = [];


function secondsTimeSpanToHMS(s) {
    var h = Math.floor(s / 3600);
    s -= h * 3600;
    var m = Math.floor(s / 60);
    s -= m * 60;
    return (h < 10 ? '0' + h : h) + ":" + (m < 10 ? '0' + m : m) + ":" + (s < 10 ? '0' + s : s);
}

function getURLParameters(url) {
    var result = {};
    var searchIndex = url.indexOf("?");
    if (searchIndex == -1)
        return result;
    var sPageURL = url.substring(searchIndex + 1);
    var sURLVariables = sPageURL.split('&');
    for (var i = 0; i < sURLVariables.length; i++) {
        var sParameterName = sURLVariables[i].split('=');
        result[sParameterName[0]] = sParameterName[1];
    }
    return result;
}

function getRootWebSitePath() {
    var _location = document.location.toString();
    var applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
    var applicationName = _location.substring(0, applicationNameIndex) + '/';
    var webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);
    var webFolderFullPath = _location.substring(0, webFolderIndex);
    return webFolderFullPath;
}

function load_player(video_url, video_duration, provider_type) {

    init_HTML5_modal();
    getVideo().load();
}
$(document).ready(function() {
    webRoot = getRootWebSitePath();
    url_params = getURLParameters(document.URL);
    video_url = url_params['id'];
    video_duration = url_params['duration'];
    video_start_sec = url_params['start'];
    video_end_sec = url_params['end'];
    provider_type = url_params['provider'];
    $(".cutted_pediod_time").html(secondsTimeSpanToHMS(video_start_sec) + ' - ' + secondsTimeSpanToHMS(video_end_sec));


    load_player(video_url, video_duration, provider_type);
});
function loadVideoWithCuttedParts() {

    if (video_start_sec == 0) {

        //cuepoints_for_cutted_section.push(video_end_sec * 1000);
        cuepoints_m.addCue(new window.VTTCue(video_end_sec, video_duration,'cut/trim tart'));
        seek_times[video_end_sec * 1000] = video_duration * 1000;

    } else {
        cuepoints_m.addCue(new window.VTTCue(0, video_start_sec, 'cut/trim tart'));
        cuepoints_m.addCue(new window.VTTCue(video_end_sec, video_duration,'cut/trim tart'));
        //cuepoints_for_cutted_section.push(0);
        seek_times[0] = video_start_sec * 1000;
        //cuepoints_for_cutted_section.push(video_end_sec * 1000);
        seek_times[video_end_sec * 1000] = video_duration * 1000;

    }
}