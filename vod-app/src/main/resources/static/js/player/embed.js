    const intro_url = "https://dimos.med.uoa.gr/delosrc/support/Intro_Video_cc.mp4";
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

    const mp4 = document.getElementById("mp4");
    const resource_id = document.getElementById("vid").value;
    const title_overlay = document.getElementById("title_overlay");
    title_overlay.style.display = 'none';
    let playlist_array;
    let video_playing_index;
    let video_url;
    init_();

    function init_() {
        let siteUrl = getRootSitePath();
        fetch(siteUrl + '/api/v1/emurl/' + resource_id)
            .then(function (response) {
                // Successful fetch return as json
                return response.json();
            })
            .then(function (data) {
                video_url = data.url;
                title_overlay.innerText = data.title + '\n' + data.subtitle;
                loadPlayerContent();
                video_playing_index = 0;
                play_();
            })
            .catch(function (error) {
                console.log(error);
            });
    }
    function play_() {
        init_events("events", media_events);
        mp4.setAttribute("src", playlist_array[video_playing_index].sources[0].file + "#t=0.5");
        //console.log("Now playing MP4:" + playlist_array[video_playing_index].sources[0].file );
        document._video = document.getElementById("player");
        document._video.load();
    }

    function init_events(id, arrayEventDef) {
        for (let key in arrayEventDef) {
            document._video = document.getElementById("player");
            document._video.addEventListener(key, capture, false);
        }
    }

    function capture(event) {
        media_events[event.type]++;
        if (event.type === "play") {
            if (video_playing_index === 1) {
                title_overlay.style.display = 'none';
            }
        }
        else if (event.type === "pause") {
            if (video_playing_index === 1) {
                title_overlay.style.display = 'block';
            }
        }
        else if (event.type === "ended") {
            console.log("ended");
            if (introPlaying()) {
                //console.log("intro ended");
                video_playing_index = 1;
                play_();
            }
        }
    }
    function loadPlayerContent() {
      playlist_array = [
                {
                    sources: [
                        { file: intro_url }
                    ]
                },
                {
                    sources: [
                        { file: video_url }
                    ]
                }
       ];
    }
    function introPlaying() {
        return video_playing_index === 0;
    }

    function getRootSitePath() {
        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let applicationName = _location.substring(0, applicationNameIndex) + '/';
        let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

        return _location.substring(0, webFolderIndex);
    }