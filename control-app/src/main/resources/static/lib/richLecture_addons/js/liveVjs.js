const crossDomain_option = false;
cuepointsx = [];
slide_at = [];
slide_titles = [];
let info, actions;
let video_url_primary = "";
let video_url_ipcamera = "";
let video_url_camcast = "";
let video_url_screencast = "";
let secondary_resource = "";
let basepath = "";
let number_of_total_slides = 0;
let webRoot = "";
let presentation_file = "";
let slides_file = "";
let slides_loaded = 0;
let isPlaying = false;
let isSecPlaying = false;

let ajaxRefreshInterval;
let vid_lecture_id;
let hls;
let hls_sec;

$(document).ready(function() {
    webRoot = getRootWebSitePath();
    presentation_file = webRoot + "/scheduler/service/presentation/";
    slides_file = webRoot + "/scheduler/service/slides/";

    vid_lecture_id = window.location.search.replace("?id=", "");
    presentation_file = presentation_file + vid_lecture_id;
    slides_file = slides_file + vid_lecture_id;
    read_parameters_XML();
    /* disable right click: not used for now!! */
    /*$(this).bind('contextmenu', function() {
        return false;
    });*/

    $('.carousel').carousel({
        interval: false
    });
    $(".carousel-indicators").on('click', '.list-inline-item', function() {
        $(".list-inline-item").removeClass("active");
        $(this).addClass("active");
    });
    $(".carousel-control-next").on('click', function(){
       let $active_elem = $('.carousel-item.active');
       $(".list-inline-item").removeClass("active");
       let act_slide = $active_elem.data("slide-number") + 1;
       let max_items =  $('.carousel-indicators li').length-1;
       if (act_slide > max_items) {
           act_slide = 0;
       }
        let $active_carousel_indicator = $('.carousel-indicators li:nth-child('+ (act_slide+1) + ')');
        $active_carousel_indicator.addClass("active");
        $active_carousel_indicator.focus();
        let thumbnail_width = $(".img-thumbnail").first().width();
        //console.log(thumbnail_width);
        $('.carousel-indicators').scrollLeft(act_slide* thumbnail_width);
    });
    $(".carousel-control-prev").on('click', function(){
        let $active_elem = $('.carousel-item.active');
        $(".list-inline-item").removeClass("active");
        let act_slide = $active_elem.data("slide-number")-1;
        let max_items =  $('.carousel-indicators li').length;
        if (act_slide < 0) {
            act_slide = max_items-1;
        }
        let $active_carousel_indicator =  $('.carousel-indicators li:nth-child('+ (act_slide+1) + ')');
        $active_carousel_indicator.addClass("active");
        $active_carousel_indicator.focus();
        let thumbnail_width = $(".img-thumbnail").first().width();
        //console.log(thumbnail_width);
        $('.carousel-indicators').scrollLeft(act_slide* thumbnail_width);
    });

    let roomCode = $("#roomCode").val();
    let streamfile = $("#streamfile").val();


    let liveStreamConnectionsInterval = setInterval(function() {
        getLiveStreamConnections(roomCode, streamfile);
    }, 10000);
    if (roomCode === '' || streamfile === '') {
        clearInterval(liveStreamConnectionsInterval);
    }
});

function getOpenDelosLiveSlide(sessionId) {

    var element = $("#slide_container");
    let resources_base_url = $("#resources_base_url").val();

    $.ajax({
        url: webRoot + '/services/api/v1/resource/slide_exists?filename=currentSlide.jpg&resourceId=' + sessionId,//  $(this).attr('src'),
        type: 'get',
        //async: false,
        error:function(response){
        },
        success: function(data) {
            if (data === "1") {
                $(element).attr('src', resources_base_url + "/_Repo/" + sessionId + "/slides/currentSlide.jpg?t=" + new Date().getTime());
            }
            else {
                $(element).attr('src', webRoot + "/resources/images/deloslogo.png");
            }
        }
    });
}

function startLiveSlidesBroadcast(sessionId) {
    ajaxRefreshInterval = setInterval(function(){ getOpenDelosLiveSlide(sessionId); }, 2000);
}
function stopLiveSlidesBroadcast() {
    clearInterval(ajaxRefreshInterval);
}

function read_parameters_XML() {

    let $time_left_badge = $("#time_left_badge");

    $.ajax({
        type: "GET",
        url: presentation_file,
        dataType: "xml",
        crossDomain: crossDomain_option,
        cache: false,
        success: function(xml) {
            let timeleft = $(xml).find('real_duration').text();
            var refreshIntervalTimeLeft;
            
            if ($(xml).find('header').text().substring(0, 6) !== "event:") {
                refreshIntervalTimeLeft = setInterval(function() {
                    timeleft = timeleft - 1;
                    if (timeleft <= 0) {
                        clearInterval(refreshIntervalTimeLeft);
                        StopLiveFeed();
                        this.stopLiveSlidesBroadcast();
                        $time_left_badge.html(Seconds2HHMMSS(timeleft));
                    }
                    else {
                        $time_left_badge.html(Seconds2HHMMSS(timeleft));
                    }
                }, 1000);
            } else {
                refreshIntervalTimeLeft = setInterval(function() {
                    timeleft = timeleft - 1;
                    if (timeleft <= 0) {
                        $.ajax({
                            type: "GET",
                            url: presentation_file,
                            dataType: "xml",
                            crossDomain: crossDomain_option,
                            cache: false,
                            success: function(xml_final_check) {
                                timeleft = $(xml_final_check).find('real_duration').text();
                                if (timeleft <= 0) {
                                    clearInterval(refreshIntervalTimeLeft);
                                    StopLiveFeed();
                                    this.stopLiveSlidesBroadcast();
                                    $time_left_badge.html(Seconds2HHMMSS(timeleft));
                                }
                            }
                        });
                    }
                    else {
                        $time_left_badge.html(Seconds2HHMMSS(timeleft));
                    }
                }, 1000);
            }
            let logo                = $(xml).find('logo').text();
            let date_txt            = reformat_datetime($(xml).find('date').text());
            let lecture_serie       = $(xml).find('serie').text();
            let duration            = $(xml).find('duration').text();
            let description         = $(xml).find('description:first').text();
            let lecture_header      = $(xml).find('header').text();
            let slides_uploaded     = $(xml).find('slide');
            let live_slides         = $(xml).find('live_slides');
            let lecture_presenter   = $(xml).find('presenter').text();
            video_url_ipcamera      = $(xml).find('resource[format=ipcamera]').text();
            video_url_camcast       = $(xml).find('resource[format=camcast]').text();

            $(document).attr('title', lecture_serie);
            $(".header-item").text(lecture_serie + ' | ' + date_txt);
            $(".header-logo").html('<img class="header-logo" alt="Institution logo" src="' + logo + '"/>');


            if (video_url_ipcamera === "-1") {
                video_url_primary = video_url_camcast;
            } else {
                video_url_primary = video_url_ipcamera;
            }
            video_url_screencast = $(xml).find('resource[format=screencast]').text();

            if (video_url_screencast === "live_slides") {// if (live_slides === "1") {
                secondary_resource = "live_slides";
            }
            else {
                if (slides_uploaded.length) {
                    secondary_resource = "slides";
                } else if (video_url_screencast !== "-1") {
                    secondary_resource = "screencast";
                } else
                    secondary_resource = "none";
            }

            let $video_panel = $("#primary_video_panel");

            if (secondary_resource !== "slides") {
                $("#myCarousel").remove();
            }
            if (secondary_resource !== "screencast") {
                $("#secondary_video_container").remove();
            }
          /*
            let $aside_1 = $(".aside-1");
            let $player  = $("#player");
            if (secondary_resource === "none") {
                $(".aside-2").css("flex", "0 0 0");
                $aside_1.css("flex", "3 1");
                $player.removeClass("w-100");
                //$player.css("width","50%");
                $player.css("margin", "0 auto");

                $aside_1.css("align-items","center");
                $aside_1.css("justify-content","center");
            }*/


            if (secondary_resource !== "live_slides") {
                $("#live_slides_panel").remove();
            }
            else {
                startLiveSlidesBroadcast(vid_lecture_id);
            }


            if (lecture_header.substring(0, 6) === "event:") {
                lecture_header = lecture_header.substring(6, lecture_header.length);
                $('#lecture_department').html(language.p1 + "<span><\/span>");
                $('#lecture_serie').html(language.p2 + "<span><\/span>");
            }


            // Fill info modal fields-properties
            $('#lecture_date span').html(date_txt);
            $('#lecture_duration span').html(duration);
            $('#lecture_description span').html(description);
            $('#lecture_department span').html(lecture_header);
            $('#lecture_serie span').html(lecture_serie);
            if (lecture_presenter === -1)
                $('#lecture_presenter').remove();
            else
                $('#lecture_presenter span').html(lecture_presenter);
        },
        complete: function() {

            if (video_url_primary.indexOf("camcast") !== -1) {
                $("#buttonGo").show();
                load_player_primary(video_url_primary);
            }
            else {
                $("#buttonGo").hide();
                load_player_primary();
            }
                    if (secondary_resource === "slides") {
                        read_slides_XML();
                    } else if (secondary_resource === "screencast") {
                        load_secondary_player();
                     } else {
                        slides_loaded = 1;
                     }
       }
    });
}
 
 function load_player_primary(camcast) {

	 let video_source = video_url_primary;
	 if (camcast !== undefined) {
	     video_source = video_source +  "_720p/playlist.m3u8"; //"_360p/playlist.m3u8";
     }
	 console.log("Primary Video Source:" + video_source);

     try {
         hls.destroy();
     }
     catch (ex) {

     }
     let video;
     if (Hls.isSupported()) {
         video = document.getElementById('player');
         if(Hls.isSupported()) {
             hls = new Hls();
             hls.loadSource(video_source);
             hls.attachMedia(video);
             hls.on(Hls.Events.MANIFEST_PARSED,function() {
                 console.log("manifest loaded, found " + data.levels.length + " quality level");
                 video.play();
                 isPlaying = true;
             });
             hls.on(Hls.Events.ERROR, function (event, data) {
                 if (data.fatal) {
                     let $sdpDataTag = $("#sdpDataTag");
                     switch(data.type) {
                         case Hls.ErrorTypes.NETWORK_ERROR:
                             // try to recover network error
                             console.log("fatal network error encountered, try to recover");
                             $sdpDataTag.html("<small>fatal network error encountered, trying to recover</small>");
                             hls.startLoad();
                             break;
                         case Hls.ErrorTypes.MEDIA_ERROR:
                             console.log("fatal media error encountered, try to recover");
                             $sdpDataTag.html("<small>fatal media error encountered, trying to recover</small>");
                             hls.recoverMediaError();
                             break;
                         default:
                             // cannot recover
                             hls.destroy();
                             StopLiveFeed();
                             this.stopLiveSlidesBroadcast();
                             break;
                     }
                 }
             });
         }
         else if (video.canPlayType('application/vnd.apple.mpegurl')) {
             console.log("Sec option");
             video.src = video_source;
             video.addEventListener('loadedmetadata',function() {
                 video.play();
                 isPlaying = true;
             });
         }
      }
}

function load_secondary_player() {

     let video_source = video_url_screencast;
     console.log("Secondary Video Source:" + video_source);

    let sec_video;
    if (Hls.isSupported()) {
        sec_video = document.getElementById('sec_player');
        if(Hls.isSupported()) {
            hls_sec = new Hls();
            hls_sec.loadSource(video_source);
            hls_sec.attachMedia(sec_video);
            hls_sec.on(Hls.Events.MANIFEST_PARSED,function() {
                console.log("sec manifest loaded, found " + data.levels.length + " quality level");
                sec_video.play();
                isSecPlaying = true;
            });
            hls_sec.on(Hls.Events.ERROR, function (event, data) {
                if (data.fatal) {
                    switch(data.type) {
                        case Hls.ErrorTypes.NETWORK_ERROR:
                            // try to recover network error
                            console.log("sec fatal network error encountered, try to recover");
                            hls_sec.startLoad();
                            break;
                        case Hls.ErrorTypes.MEDIA_ERROR:
                            console.log("sec fatal media error encountered, try to recover");
                            hls_sec.recoverMediaError();
                            break;
                        default:
                            // cannot recover
                            hls_sec.destroy();
                            StopSecLiveFeed();
                            break;
                    }
                }
            });
        }
        else if (sec_video.canPlayType('application/vnd.apple.mpegurl')) {
            console.log("Sec option");
            sec_video.src = video_source;
            sec_video.addEventListener('loadedmetadata',function() {
                sec_video.play();
                isSecPlaying = true;
            });
        }
    }
}

function read_slides_XML() {
    let increment = 0;

    $.ajax({
        type: "GET",
        url: slides_file,
        crossDomain: crossDomain_option,
        dataType: "xml",
        cache: false,
        success: function(xml) {
            basepath = $(xml).find('basepath').text();
            $(xml).find('slide').each(function() {
                var url = $(this).find('url').text();
                url = basepath + url;
                var slide_title = $(this).find('title').text();
                if (!slide_title.length)
                    slide_title = language.s4;
                cuepointsx.push(increment * 1000);
                slide_at[increment * 1000] = url;
                slide_titles[increment * 1000] = slide_title;
                increment++;
            });
            for (j = 0; j < cuepointsx.length; j++) {
                var last_slash_pos = slide_at[cuepointsx[j]].lastIndexOf('/');
                let url_for_scroll_bar = slide_at[cuepointsx[j]].substr(0, last_slash_pos + 1) + "small/" + slide_at[cuepointsx[j]].substr(last_slash_pos + 1);

                let ci_class = "carousel-item";
                if ( j === 0) {
                    ci_class = "carousel-item active";
                }
                let img_html = '<img src="' + slide_at[j * 1000] + '" title="' + (j + 1) + '. ' + slide_titles[cuepointsx[j]] + '"   alt="">';
                $('<div class="' + ci_class + '" data-slide-number="' + j + '"></div>').html(img_html).appendTo($('.carousel-inner'));

                let figure_html =  '<figure class="figure" style="font-size: smaller;font-weight: normal;margin:0"><img class="img-thumbnail img-fluid" src="' + url_for_scroll_bar + '" title="' + (j + 1) + '. ' + slide_titles[cuepointsx[j]] + '" style="max-width: 100%;min-width:100px;height: auto"  alt=""/><figcaption class="figure-caption">no.' + (j+1) + '</figcaption></figure> </a>';

                let li_class= "list-inline-item";
                if (j === 0) {
                    figure_html = '<a id="carousel-selector-' + j + '">' + figure_html + '</a>';
                    li_class = "list-inline-item active";
                }
                $('<li class="' + li_class + '" data-slide-to="' + j + '" data-target="#myCarousel">' +
                        figure_html + '</li>').appendTo($('.carousel-indicators'));
            }

            number_of_total_slides = increment;
            scroll_area_img_height = 0;
            slides_loaded = 1;
        }
    });
}

function getRootWebSitePath() {
    var _location = document.location.toString();
    var applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
    var applicationName = _location.substring(0, applicationNameIndex) + '/';
    var webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);
    return _location.substring(0, webFolderIndex);
}


/**
 * @return {string}
 */
function Seconds2HHMMSS(timeInSeconds) {
    let sec_num = parseInt(timeInSeconds, 10); // don't forget the second param
    let hours   = Math.floor(sec_num / 3600);
    let minutes = Math.floor((sec_num - (hours * 3600)) / 60);
    let seconds = sec_num - (hours * 3600) - (minutes * 60);

    if (hours   < 10) {hours   = "0"+hours;}
    if (minutes < 10) {minutes = "0"+minutes;}
    if (seconds < 10) {seconds = "0"+seconds;}

    return hours + ':' + minutes + ':' + seconds;
}
function reformat_datetime(datetime_original) {
    let datetime_split = datetime_original.split(" ");
    let date_split = datetime_split[0].split("-");
    return (date_split[2] + '-' + date_split[1] + '-' + date_split[0] + ' ' + datetime_split[1]);
}

function StopLiveFeed() {
    if (video_url_primary.indexOf("camcast") === -1) {
        hls.destroy();
    }
        $("#player").remove();
        $("#info_button").remove();
        let $prepend_image_div = '<div style="border: #0b6da8 1px solid;width: 603px;height: 341px;display: block;margin:0 auto 0 auto"></div>';
        let live_ended_image_url = '<img id="stream_ended" alt="The live stream has finished" src="' + webRoot + '/resources/richLecture_addons/css/images_player/live_ended-' + language.selectedLang + '.jpg"/>';
        $($prepend_image_div).html(live_ended_image_url).prependTo($("#primary_video_container"));
        $("#buttonGo").hide();
        console.log("feed ended");

}
function StopSecLiveFeed() {
    if (video_url_primary.indexOf("camcast") === -1) {
        hls_sec.destroy();
    }
    $("#sec_player").remove();
    let $prepend_image_div = '<div style="border: #0b6da8 1px solid;width: 603px;height: 341px;display: block;margin:0 auto 0 auto"></div>';
    let live_ended_image_url = '<img id="stream_ended" alt="The live stream has finished" src="' + webRoot + '/resources/richLecture_addons/css/images_player/live_ended-' + language.selectedLang + '.jpg"/>';
    $($prepend_image_div).html(live_ended_image_url).prependTo($("#secondary_video_container"));
    console.log("feed ended");
}

function getLiveStreamConnections(roomCode, streamfile) {

    //console.log("ROOM CODE:" + roomCode);
    jQuery.support.cors = true;
    let service_url =   webRoot + '/services/wowza/room/' + roomCode +'/stream/' + streamfile + '/streamLiveConnections';

    $.ajax({
        type : "GET",
        url : service_url,
        contentType : "application/json; charset=utf-8",
        dataType : "json",
        crossDomain : true,
        success : function(data) {
            //console.log(data);
            $("#viewers_badge").html(data.totalConnections);
        },
        error : function() {
            $("#viewers_badge").html("!");
        }

    });
}