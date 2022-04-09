$(function () {
    'use strict';

    dashboard.player = dashboard.player || {};

    dashboard.player.actions;

    let debug = 1;
    let video_url;
    let hls;
    let video;

    dashboard.player.init = function(video_url_) {
        video_url  = video_url_;
        dashboard.player.initializePlayer();
    };

    dashboard.player.initializePlayer = function() {

        video = document.getElementById("player");
        if (hls !== undefined) {
            hls = null;
        }
        if (Hls.isSupported()) {
            hls = new Hls({
                debug: false,
                enableWorker: true,
/*                liveBackBufferLength: 15,
                backBufferLength: 15,
                liveMaxBackBufferLength: 15,
                maxBufferSize: 0,
                maxBufferLength: 10,
                liveSyncDurationCount: 1,*/
            });
            hls.loadSource(video_url);
            hls.attachMedia(video);
            hls.on(Hls.Events.MEDIA_ATTACHED, function () {
                if (debug) console.log('video and hls.js are now bound together !');
                hls.on(Hls.Events.MANIFEST_PARSED, function (event, data) {
                    console.log('manifest loaded, found ' + data.levels.length + ' quality level');
                    video.play();
                });
            });
            hls.on(Hls.Events.MEDIA_DETACHED, function () {
                if (debug) console.log('video and hls.js are now detached !');
            });
            hls.on(Hls.Events.DESTROYING, function () {
                hls = null;
            });
            hls.on(Hls.Events.ERROR, function (event, data) {
                if (data.fatal) {
                    switch (data.type) {
                        case "networkError":
                            // try to recover network error
                            console.log('Player Error: fatal network error encountered, try to recover');
                            hls.startLoad();
                            break;
                        case "mediaError":
                            console.log('Player Error: fatal media error encountered, try to recover');
                            document._video.currentTime = 0;
                            hls.recoverMediaError();
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
        else if (document._video.canPlayType('application/vnd.apple.mpegurl')) {
            video.src = video_url;
        }
    }

    dashboard.player.setLevel = function() {
        video.currentTime = 0;
        hls.currentLevel = -1;
    }
    dashboard.player.destroyPlayer = function() {
        hls.destroy();
    }

    dashboard.player.detachMedia = function() {
        hls.detachMedia();
    }

    dashboard.player.play = function() {
        video.play();
    }

    dashboard.player.setVideoTime = function(time) {
        video.currentTime = time;
    }

    dashboard.player.getVideoTime = function() {
        return video.currentTime;
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


    function play_error() {
        video.pause();
        video.currentTime = 0;
        $('#playPauseVideoBt').html('<i class="fas fa-play"></i>');
        hls.stopLoad();
        let img_src = dashboard.siteUrl + "/public/images/images_player/network_error-el-1.jpg";
        let live_ended_image_url = '<img class="p-0 m-0" style="width: 100%" alt="The live stream has finished" src="' + img_src + '"/>';
        $("#video_cont").html(live_ended_image_url);
    }

    function stop_play() {
        video.pause();
        video.currentTime = 0;
        hls.stopLoad();
         let img_src = dashboard.siteUrl + "/public/images/images_player/live_ended-el.jpg";
        let live_ended_image_url = '<img class="p-0 m-0" style="width: 100%" alt="The live stream has finished" src="' + img_src + '"/>';
        $("#video_cont").html(live_ended_image_url);
    }

});