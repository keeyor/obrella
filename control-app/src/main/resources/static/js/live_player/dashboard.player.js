$(function () {
    'use strict';

    dashboard.player = dashboard.player || {};

    dashboard.player.actions;

    let debug = 0;
    let playlist_array;
    let global_vid_duration;
    let intro_url;
    let video_url;
    let play_intro;
    let poster_image;
    let mode;
    let tt;
    let ts;
    let video_playing_index;
    let hls;

    const media_events = [];

    media_events["loadstart"] = 0;
    media_events["progress"] = 0;
    media_events["suspend"] = 0;
    media_events["abort"] = 0;
    media_events["error"] = 0;
    media_events["emptied"] = 0;
    media_events["stalled"] = 0;
    media_events["loadedmetadata"] = 0;
    media_events["loadeddata"] = 0;
    media_events["canplay"] = 0;
    media_events["canplaythrough"] = 0;
    media_events["playing"] = 0;
    media_events["waiting"] = 0;
    media_events["seeking"] = 0;
    media_events["seeked"] = 0;
    media_events["ended"] = 0;
    media_events["durationchange"] = 0;
    media_events["timeupdate"] = 0;
    media_events["play"] = 0;
    media_events["pause"] = 0;
    media_events["ratechange"] = 0;
    media_events["resize"] = 0;
    media_events["volumechange"] = 0;

    let cuepoints_m = [];
    let cuepointsx_m = [];

    dashboard.player.init = function(intro_url_, video_url_, play_intro_, poster_img_, duration_, sync_) {
        intro_url  = intro_url_;
        video_url  = video_url_;
        play_intro = play_intro_;
        poster_image  = poster_img_;
        global_vid_duration = duration_;
        mode = sync_;

        loadPlayerContent();
        $("#player_footer").hide();
        dashboard.player.initializePlayer(0);
    };

    dashboard.player.initializePlayer = function(index) {

        video_playing_index= index;
        const mp4 = document.getElementById("mp4");
        const webm = document.getElementById("webm");
        const parent = mp4.parentNode;

        document._video = document.getElementById("player");
        let video_source;
        if (playlist_array[video_playing_index].sources.length > 1 && playlist_array[video_playing_index].sources[1].file !== "-1") {
            video_source = playlist_array[video_playing_index].sources[1].file;
            webm.setAttribute("src", video_source);
            if (debug) console.log("Now playing WEBM:" + video_source);
        }
        else {
            if (webm) {
                parent.removeChild(webm);
            }
        }
        if (playlist_array[video_playing_index].sources[0].file !== "-1") {
            video_source = playlist_array[video_playing_index].sources[0].file;
            mp4.setAttribute("src", video_source);
            if (debug)   console.log("Now playing MP4:" + video_source );
        }

        define_actions();
        init_events("events", media_events);

        hls = new Hls({
            lowLatencyMode: true
        });
        hls.loadSource(video_source);
        hls.attachMedia( document._video);
        hls.on(Hls.Events.MANIFEST_PARSED,function(event,data) {
            console.log("manifest loaded, found " + data.levels.length + " quality level");
            document._video.play();
            isPlaying = true;
        });
        hls.on(Hls.Events.ERROR, function (event, data) {
            if (data.fatal) {
                switch (data.type) {
                    case Hls.ErrorTypes.NETWORK_ERROR:
                        // try to recover network error
                        console.log('Player Error: fatal network error encountered, try to recover');
                        hls.startLoad();
                        play_error();
                        break;
                    case Hls.ErrorTypes.MEDIA_ERROR:
                        console.log('PLayer Error: fatal media error encountered, try to recover');
                        hls.startLoad();
                        play_error();
                        break;
                    default:
                        console.log("Player Error: cannot recover...")
                        play_error();
                        hls.destroy();
                        break;
                }
            }
        });
    }

    dashboard.player.play = function() {
        getVideo().play();
    }

    dashboard.player.setVideoTime = function(time) {
        getVideo().currentTime = time;
    }

    dashboard.player.getVideoTime = function() {
        return getVideo().currentTime;
    }

    dashboard.player.addTrimOrCutPoint = function(begin_sec, end_sec, label) {
        cuepoints_m.addCue(new window.VTTCue(begin_sec, end_sec, label));
    }
    dashboard.player.addSlidePoint = function(slide_begin, slide_end, jsonData) {
        cuepointsx_m.addCue(new window.VTTCue(slide_begin, slide_end, jsonData));
    }

    dashboard.player.getSlidePoint = function(k) {
        return cuepointsx_m.cues[k];
    }

    dashboard.player.stopVideo = function() {
        stop_play();
    }

    dashboard.broker.getRootSitePath = function () {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let applicationName = _location.substring(0, applicationNameIndex) + '/';
        let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

        return _location.substring(0, webFolderIndex);
    };

    function capture(event) {
        media_events[event.type]++;

        let curr_time;
        if (event.type === "play") {
            if (debug) console.log("player resumed");
            $('#playPauseVideoBt').html('<i class="fas fa-pause"></i>');
        } else if (event.type === "pause") {
            if (debug) console.log("player paused");
            $('#playPauseVideoBt').html('<i class="fas fa-play"></i>');
        }
        else if (event.type === "seeked") {
            if (!introPlaying()) {
                if (debug) console.log("intro not playing. Seeked");
                curr_time = Math.round(getVideo().currentTime) * 1000;
                curr_time += 1;
                if (debug) console.log("onSeek:" + curr_time);

                let message = {msg: "Video Seeked!", value: curr_time};
                dashboard.broker.trigger('seeked.video', [message]);
            }
        } else if (event.type === "loadedmetadata") {

            if (debug) console.log("loadedmetadata");

            if (introPlaying()) {
                let message = {msg: "Intro Loaded", value: ''};
                dashboard.broker.trigger('intro_loaded.video', [message]);
            }
            else {
                if (debug)  console.log("intro not playing. Add cues");
                //Note: We have to add a cue_points array to video and THEN fill it with cue points
                cuepoints_m = getVideo().addTextTrack("metadata", "CutsAndTrims", "en");
                cuepoints_m.mode = "showing";

                cuepointsx_m = getVideo().addTextTrack("metadata", "SlideSync", "en");
                cuepointsx_m.mode = "showing";

                let message = {msg: "Video Loaded!", value: ''};
                dashboard.broker.trigger('loaded.video', [message]);

                define_cuepoint_events();

            }
            //set default volume values
            getVideo().volume = 0.5;
            $(".show_current_vol").html(Math.round(getVideo().volume*10) + "");

        } else if (event.type === "oncanplay") {
            if (debug) console.log("player ready");
        }
        else if (event.type === "ended") {
            if (debug) console.log("ended");

            if (introPlaying()) {
                let message = {msg: "Intro Ended!", value: ''};
                dashboard.broker.trigger('intro_ended.video', [message]);
            }
            else {
                console.log("video ended");
            }
        }
        else if (event.type === "volumechange") {
            if (debug) console.log("volumechange");
            if (getVideo().muted) {
                $('.player_mute').html('<i class="fas fa-volume-mute"></i>');
            }
            else {
                $('.player_mute').html('<i class="fas fa-volume-off"></i>');
            }
        }
    }


    function define_cuepoint_events() {

        getVideo().textTracks.addEventListener('addtrack', function () {

            let trackIndex = 0; //Cuts Tracks
            tt = getVideo().textTracks[trackIndex];

            tt.addEventListener('cuechange', function () {
                if (tt.activeCues[0] !== undefined) {
                    if (debug) {
                        console.log(" Cut visited..Seek to:" + tt.activeCues[0].endTime);
                    }
                    getVideo().currentTime = tt.activeCues[0].endTime;
                }
            }); //end oncuechange

            trackIndex = 1; //Slides Track
            ts = getVideo().textTracks[trackIndex]
            ts.addEventListener('cuechange', function () {
                if (ts.activeCues[0] !== undefined) {
                    if (debug) {
                        console.log("Slide Cue Point:" + ts.activeCues[0].startTime);
                    }
                    let jsonData = ts.activeCues[0].text;
                    let message = {msg: "Slide cuepoint !", value: jsonData};
                    dashboard.broker.trigger('slide_cuepoint_reached.video', [message]);
                }
            }); //end oncuechange
        });
    }

    function loadPlayerContent() {

        if (play_intro !== "true"){
            playlist_array = [
                {
                    sources: [
                        { file: video_url }
                    ],
                    image: poster_image
                }
            ];
        }else{
            playlist_array = [
                {
                    sources: [
                        { file: intro_url }
                    ]
                },
                {
                    sources: [
                        { file: video_url }
                    ],
                    image: poster_image
                }
            ];
        }
    }

    function define_actions() {
        dashboard.player.actions = {
            play: function() {
                toggle_play();
            },
            stop: function() {
                stop_play();
            },
            rewind: function() {
                rewind_play();
            },
            forward: function() {
                forward_play();
            },
            mute: function() {
                toggle_mute();
            },
            volup: function() {
                volume_up();
            },
            voldown: function() {
                volume_down();
            },
            fullscreen: function() {
                full_screen();
            }
        }
    }

    function go_to_time_func() {
        setInterval(function() {
            if (getVideo() !== undefined) {
                let time_now = getVideo().currentTime;
                let hms = secondsTimeSpanToHMS(Math.round(time_now))
                if (!introPlaying()) {
                    $(".show_current_time").html(hms + " / " + secondsTimeSpanToHMS(global_vid_duration));
                }
                else {
                    $(".show_current_time").html(hms + " / " + getVideo().duration + " <span class='text-muted'> - CC License</span>");
                }
            }
        }, 1000);
    }

    function introPlaying() {
        return playlist_array.length === 2 && video_playing_index === 0;
    }

    function getVideo() {
        return document._video;
    }

    function init_events(id, arrayEventDef) {

        for (let key in arrayEventDef) {
            document._video.addEventListener(key, capture, false);
        }
    }

    function toggle_mute() {
        if (getVideo().muted) {
            if (debug) console.log("un-mute");
            $('.player_mute').html('<i class="fas fa-volume-up"></i>');
            getVideo().muted = false;
            $(".show_current_vol").html(Math.round(getVideo().volume*10));
        }
        else {
            if (debug) console.log("mute");
            $('.player_mute').html('<i class="fas fa-volume-mute"></i>');
            getVideo().muted = true;
            $(".show_current_vol").html(Math.round(getVideo().volume*10));
        }

    }

    function volume_down() {
        $(".vup").attr('disabled',false);
        if  (getVideo().volume >= 0.2) {
            getVideo().volume -= 0.1;
            $(".vdown").attr('disabled',false);
        }
        else {
            getVideo().volume = 0;
            getVideo().muted = true;
            $(".vdown").attr('disabled',true);
        }
        $(".show_current_vol").html(Math.round(getVideo().volume*10));
        if (debug) console.log("volume down:" + getVideo().volume);
    }

    function volume_up() {
        getVideo().muted = false;
        $(".vdown").attr('disabled',false);
        if  (getVideo().volume <= 0.9) {
            getVideo().volume += 0.1;
            $(".vup").attr('disabled',false);
        }
        else {
            getVideo().volume = 1;
            $(".vup").attr('disabled',true);
        }
        $(".show_current_vol").html(Math.round(getVideo().volume*10));

        if (debug) console.log("volume up:"+ getVideo().volume);
        $('.player_mute').html('<i class="fas fa-volume-up"></i>');
    }

    function toggle_play() {
        if (document._video.paused === false) {
            getVideo().pause();
            if (debug) console.log("video paused");

        }
        else {
            getVideo().play();
            if (debug) console.log("video resumed");

        }
    }

    function stop_play() {
        getVideo().pause();
        getVideo().currentTime = 0;
        $('#playPauseVideoBt').html('<i class="fas fa-play"></i>');
        hls.stopLoad();
         let img_src = dashboard.siteUrl + "/public/images/images_player/live_ended-el.jpg";
        let live_ended_image_url = '<img class="p-0 m-0" style="width: 100%" alt="The live stream has finished" src="' + img_src + '"/>';
        $("#video_cont").html(live_ended_image_url);
    }

    function play_error() {
        getVideo().pause();
        getVideo().currentTime = 0;
        $('#playPauseVideoBt').html('<i class="fas fa-play"></i>');
        hls.stopLoad();
        let img_src = dashboard.siteUrl + "/public/images/images_player/network_error-el-1.jpg";
        let live_ended_image_url = '<img class="p-0 m-0" style="width: 100%" alt="The live stream has finished" src="' + img_src + '"/>';
        $("#video_cont").html(live_ended_image_url);
    }

    function rewind_play() {
        if (getVideo().currentTime >=5) {
            getVideo().currentTime -= 5;
        }
        else {
            getVideo().currentTime = 0;
        }

    }

    function forward_play() {
        getVideo().currentTime += 5;
        if(getVideo().currentTime >= global_vid_duration || document._video.paused) {
            getVideo().pause();
            getVideo().currentTime = 0;
        }
    }

    function full_screen() {
        toggleFullScreen();
    }

    function toggleFullScreen() {
        var player = document.querySelector('#player');
        if (!document.mozFullScreen && !document.webkitIsFullScreen) {
            if (player.mozRequestFullScreen) {
                player.mozRequestFullScreen();
            }
            else {
                player.webkitRequestFullScreen();
            }
        } else {
            if (document.mozCancelFullScreen) {
                document.mozCancelFullScreen();
            } else {
                document.webkitCancelFullScreen();
            }
        }
    }
    function secondsTimeSpanToHMS(s) {
        let h = Math.floor(s/3600); //Get whole hours
        s -= h*3600;
        let m = Math.floor(s/60); //Get remaining minutes
        s -= m*60;
        return (h < 10 ? '0'+h : h)+":"+(m < 10 ? '0'+m : m)+":"+(s < 10 ? '0'+s : s); //zero padding on minutes and seconds
    }
});