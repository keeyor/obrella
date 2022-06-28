(function () {
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

    dashboard.player.setAndShowPlayer = function() {
        let intro_url           = "";
        let video_url           = dashboard.baseUrl_mm + $("#file_folder").text() + "/" + $("#file_name").text();
        let play_intro          = "false";
        let global_vid_duration = $("#file_duration").text();
        let poster_image        = "";
        let mode                = "sync_off";
        dashboard.player.init(intro_url, video_url, play_intro, poster_image,global_vid_duration,mode);
        let rid = $("#rid").val();
        $("#download_video_link").attr("href",dashboard.siteUrl + "/admin/download-video?id=" + rid);
        //$("#download_video_link").attr("href",video_url);

        $("#player_container").show();

        $("#mm_delete").on("click",function(event){
            event.preventDefault();
            let message = {msg: "Remove Video!"};
            dashboard.broker.trigger('remove.mmEdit', [message]);
        })
    }

    dashboard.player.init = function(intro_url_, video_url_, play_intro_, poster_img_, duration_, sync_) {
        intro_url  = intro_url_;
        video_url  = video_url_;
        play_intro = play_intro_;
        poster_image  = poster_img_;
        global_vid_duration = duration_;
        mode = sync_;
        
        loadPlayerContent();
        //Not needed for this player:> go_to_time_func()
        dashboard.player.initializePlayer(0);
    };

    dashboard.player.initializePlayer = function(index) {

        video_playing_index= index;
        const mp4 = document.getElementById("mp4");
        const webm = document.getElementById("webm");
        const parent = mp4.parentNode;

        document._video = document.getElementById("player");

        if (playlist_array[video_playing_index].sources.length > 1 && playlist_array[video_playing_index].sources[1].file !== "-1") {
            webm.setAttribute("src", playlist_array[video_playing_index].sources[1].file);
            if (debug) console.log("Now playing WEBM:" + playlist_array[video_playing_index].sources[1].file);
        }
        else {
            if (webm) {
                parent.removeChild(webm);
            }
        }
        if (playlist_array[video_playing_index].sources[0].file !== "-1") {
            mp4.setAttribute("src", playlist_array[video_playing_index].sources[0].file + "#t=0.5");
            if (debug)   console.log("Now playing MP4:" + playlist_array[video_playing_index].sources[0].file );
        }

        define_actions();
        init_events("events", media_events);
        getVideo().load();
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
})();