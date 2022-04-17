$(function () {
    'use strict';

    window.dashboard = window.dashboard || {};
    dashboard.broker = $({});
    dashboard.siteUrl   = "";

    let debug = 1;
    let hasSlides;
    let showSlides = "true";
    let mode;
    let intro_url;
    let video_url;
    let play_intro;
    let global_vid_duration;
    let poster_image;
    let actions;

    let cuepoints_c = [];
    let cut_end_in_sec =[];
    let cuepoints_x = [];
    let layout = 2;

    $(document).ready(function () {

        dashboard.init();

        dashboard.broker.on('seeked.video', function (event, msg) {
            let curr_time = msg.value;
            for (let i = 0; i < cuepoints_c.length; i++) {
                if (cuepoints_c[i] <= curr_time) {
                    const end_c = cut_end_in_sec[cuepoints_c[i]] * 1000;
                    const diff = curr_time - end_c;
                    if (diff <= 0) {
                        if (debug) console.log("on Seek of end-cut current time at " + curr_time + " cut_period " + i + ":" + cuepoints_c[i] + " - " + end_c);
                        if ((end_c / 1000) < global_vid_duration) {
                            dashboard.player.setVideoTime(end_c / 1000.0);
                        } else {
                            dashboard.player.play();
                        }
                        break;
                    } else {
                        if (debug) console.log("no cuts:" + i + " " + cuepoints_c[i] + " " + end_c + " |curr: " + curr_time + " dif: " + diff);
                    }
                }
            }
        });

        dashboard.broker.on('loaded.video', function () {

            set_on_play_controls();
            read_cuts_cuepoints_func();
            read_slide_cuepoints_func();

            let showOverlay = $("#showOverlay").val();
            if (showOverlay === "true") {
                let overlay_1 = document.getElementById('overlay_1');
                if (overlay_1 != null) {
                    setTimeout(function () {
                        overlay_1.style.visibility = 'visible';
                    }, 2000);
                    setTimeout(function () {
                        overlay_1.style.visibility = 'hidden';
                    }, 10000);
                }
                let overlay_2 = document.getElementById('overlay_2');
                if (overlay_2 != null) {
                    setTimeout(function () {
                        overlay_2.style.visibility = 'visible';
                    }, 2000);
                    setTimeout(function () {
                        overlay_2.style.visibility = 'hidden';
                    }, 10000);
                }
            }
        });

        dashboard.broker.on('intro_ended.video', function () {
            let video_playing_index = 1;
            dashboard.player.initializePlayer(video_playing_index);
        });

        dashboard.broker.on('slide_cuepoint_reached.video', function (event,msg) {
            if (mode === "sync_on") {
                let jsonData = JSON.parse(msg.value);
                let slideIndexToShow = parseInt(jsonData.index);
                if (debug)  console.log("cuechange: GoTo Slide:" + slideIndexToShow);
                actions.go_to_slide(slideIndexToShow);
            }
        });
    });


    dashboard.init = function () {

        dashboard.siteUrl = dashboard.broker.getRootSitePath();

        intro_url    = $("#intro_url").val();
        video_url    = $("#video_url").val();
        play_intro   = $("#showIntro").val();
        global_vid_duration = convert_hh_mm_ss_format($("#duration").val());
        poster_image = "";
        mode         = "sync_on";

        dashboard.player.init(intro_url, video_url, play_intro, poster_image,global_vid_duration,mode);

        hasSlides = $("#hasSlides").val();
        if (hasSlides === "true" && showSlides === "true") {
            showPresentation();
            $("#layout_button").show();

        }
        else {
            hidePresentation();
            $("#layout_button").hide();
            $("#right-box").hide();
        }
        define_menu_actions();
        define_menu_events();

        $("#copy-url").on('click', function(e){
            copyUrl();
            e.preventDefault();
        });
        $("#copy-embed").on('click', function(e){
            copyEmbedUrl();
            e.preventDefault();
        });
    }

    dashboard.broker.getRootSitePath = function () {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let applicationName = _location.substring(0, applicationNameIndex) + '/';
        let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

        return _location.substring(0, webFolderIndex);
    };

    function showPresentation() {

        $('#carouselExampleCaptions').carousel({ interval : false});
        $("#slide_no").prop("disabled", false);
        $("[name='sync_mode']").bootstrapSwitch('disabled', false);

        //events
        $(".go_to_slide").on('click',function(e) {
            let csv_data  = $(this).data("slide");
            let data = csv_data.split("#");
            let index = parseInt(data[0])
            let time = parseInt(data[1]);
            actions.go_to_slide(index);
            if(mode === "sync_on" && time !== -1) {
                if (debug) console.log("Slide Clicked. Sync Video to:" + (cuepoints_x[index] / 1000));
                dashboard.player.setVideoTime(cuepoints_x[index] / 1000);
            }
            else if (time === -1) {
                if (debug) console.log("slide not synced");
            }
            e.preventDefault();
        });

        $("#layout_button").on('click',function() {

            if ( layout === 1) {
                $("#video_wrapper").prependTo("#right-box");
                $("#video_wrapper").css("width","100%");
                $("#presentation_wrapper").prependTo("#left-box");
                $("#presentation_wrapper").css("width","100%");
                $("#presentation_wrapper").css("margin","0 auto");
                layout = 2
            }
            else {
                $("#video_wrapper").prependTo("#left-box");
                $("#video_wrapper").css("width","100%");
                $("#video_wrapper").css("margin","0 auto");
                $("#presentation_wrapper").prependTo("#right-box");
                $("#presentation_wrapper").css("width","100%");

                layout = 1;
            }
        });
    }

    function hidePresentation() {
        $("#slide_no").prop("disabled", true);
        $("[name='sync_mode']").bootstrapSwitch('disabled', true);
    }

    function define_menu_events() {

        $('#sync_mode_btn').on('switchChange.bootstrapSwitch', function (event, state) {

            let video_current_time = dashboard.player.getVideoTime();
            if(state === true){
                mode = 'sync_on';
                let time_to_go =  Math.round(video_current_time)*1000;
                let total_num_of_slides = cuepoints_x.length;
                let k = -1 ;
                for (let i = 0; i < total_num_of_slides; i++) {
                    if (time_to_go >= cuepoints_x[total_num_of_slides-1]){
                        k = total_num_of_slides - 1;
                        break;
                    }else if (cuepoints_x[i] <= time_to_go  && time_to_go < cuepoints_x[i+1]){
                        k = i;
                        break;
                    }
                }
                let slidepoint_k = dashboard.player.getSlidePoint(k);
                let jsonData = JSON.parse(slidepoint_k.text);
                let slideIndexToShow = parseInt(jsonData.index);
                actions.go_to_slide(slideIndexToShow);
            }else {
                mode = 'sync_off';
            }
            dashboard.player.mode = mode;
        });
    }

    function define_menu_actions(){
        actions = {
            go_to_slide: function(go_to_slide){
                $(".carousel").carousel(go_to_slide);
                $( ".go_to_slide" ).each(function( i ) {
                    if (i === go_to_slide) {
                        $(this).attr("style","background-color: green;color:white");
                    }
                    else {
                        $(this).attr("style","background-color: #337ab7;color:white");
                    }
                });
            }
        }
    }

    function read_cuts_cuepoints_func(){

        if (debug) console.log("READ CUTS");
        //trim_start
        let $trim_start = $("#trim_start");

        if ($trim_start !== undefined) {
            let cvs_data  = $trim_start.data("trim");
            if (cvs_data !== undefined) {
                let data = cvs_data.split("#");
                let begin_sec = convert_hh_mm_ss_format(data[0]);
                let end_sec = convert_hh_mm_ss_format(data[1]);

                cut_end_in_sec[begin_sec * 1000] = end_sec;
                cuepoints_c.push(begin_sec * 1000);

                dashboard.player.addTrimOrCutPoint(begin_sec, end_sec, "trim start");
                if (debug) console.log("TRIM START ADD:" + begin_sec + "->" + end_sec);
            }
        }

        let $trim_finish = $("#trim_end");

        if ($trim_finish !== undefined) {
            let cvs_data = $trim_finish.data("trim");
            if (cvs_data !== undefined) {
                let data = cvs_data.split("#");
                let begin_sec = convert_hh_mm_ss_format(data[0]);
                let end_sec = convert_hh_mm_ss_format(data[1]);

                cut_end_in_sec[begin_sec * 1000] = end_sec;
                cuepoints_c.push(begin_sec * 1000);

                dashboard.player.addTrimOrCutPoint(begin_sec, end_sec, "trim finish");
                if (debug) console.log("TRIM FINISH ADD:" + begin_sec + "->" + end_sec);
            }
        }

        $( ".cut_space" ).each(function() {
            let csv_data  = $(this).data("cut");
            if (csv_data !== undefined) {
                let data = csv_data.split("#");
                let begin_sec = convert_hh_mm_ss_format(data[0]);
                let end_sec = convert_hh_mm_ss_format(data[1]);

                cut_end_in_sec[begin_sec * 1000] = end_sec;
                cuepoints_c.push(begin_sec * 1000);

                dashboard.player.addTrimOrCutPoint(begin_sec, end_sec, "new cut");
                if (debug) console.log("CUT ADD:" + begin_sec + "->" + end_sec);
            }
        });
    }

    function read_slide_cuepoints_func() {

        let increment 	 = 0;
        let slideIndexes = [];

        $( ".go_to_slide" ).each(function() {
            let csv_data  = $(this).data("slide");
            let data  	  = csv_data.split("#");
            let index	  = parseInt(data[0]);
            let time	  = data[1];

            if (time !== "-1") {
                let times = time.split(',');
                for (let times_iteration = 0; times_iteration < times.length; times_iteration++){
                    time = convert_hh_mm_ss_format(times[times_iteration]);
                    if( time < global_vid_duration){    		//Exclude slides that are synced outside video duration - only when sync is done with slides recorder
                        cuepoints_x[increment]  = time*1000;
                        slideIndexes[increment] = index;
                        increment++;
                    }
                }
            }
        });

        for (let s = 0; s < cuepoints_x.length; s++) {
            let slide_begin = cuepoints_x[s] / 1000;
            let slide_end;
            if (s < (cuepoints_x.length - 1)) {
                slide_end = (cuepoints_x[s + 1] / 1000) - 1;
            } else {
                slide_end = global_vid_duration;
            }
            let jsonData = '{ "index" : "' + slideIndexes[s] + '"}';
            dashboard.player.addSlidePoint(slide_begin, slide_end,jsonData);
        }

        let $sync_switch = $("[name='sync_mode']");
        if(increment === 0){	// number of synced Slides
            mode = "sync_off";
            $sync_switch.bootstrapSwitch('disabled', true);
        }
        else {
            mode = "sync_on";
            $sync_switch.bootstrapSwitch('disabled', false);
            $sync_switch.bootstrapSwitch('state', true, true);
        }
        dashboard.player.mode = mode;
    }

    function convert_hh_mm_ss_format(input_time) {
        let broken_time = input_time.split(':');
        let hours = 0;
        let minutes = 0;
        let seconds = 0;
        switch(broken_time.length){
            case 1:
                seconds = parseFloat(broken_time[0]);
                break;
            case 2:
                minutes = parseFloat(broken_time[0]);
                seconds = parseFloat(broken_time[1]);
                break;
            case 3:
                hours = parseFloat(broken_time[0]);
                minutes = parseFloat(broken_time[1]);
                seconds = parseFloat(broken_time[2]);
                break;
        }
        return Math.round(seconds+60*minutes+3600*hours);
    }

    function set_on_play_controls() {

        let $sync_mode_sync = $("[name='sync_mode']");
        let $playPauseVideoBt = $("#playPauseVideoBt");

        if(hasSlides === "true" && showSlides === "true"){
                    mode = "sync_on";
                    $sync_mode_sync.bootstrapSwitch('disabled', false);
                    $sync_mode_sync.bootstrapSwitch('state', true, true);
        }
        else {
            mode = "sync_off";
        }

        $playPauseVideoBt.focus();
        dashboard.player.mode = mode;
    }

    function copyUrl() {
        let input = $("#copy_url_input");
        input.val(window.location);
        copyToClipboard(input, "Η διεύθυνση έχει αντιγραφεί στο πρόχειρο!");
    }
    function copyEmbedUrl() {
        let input = $("#embed_code_input");
        let resource_id = $("#resourceId").val();
        let text = '<iframe width="560" height="315" src="' + dashboard.siteUrl + '/api/v1/embed/' + resource_id + '" frameBorder="0"></iframe>';
        input.val(text);
        copyToClipboard(input, "Η κώδικας ενσωμάτωσης έχει αντιγραφεί στο πρόχειρο!");
    }

    function copyToClipboardFF(text) {
        window.prompt ("Copy to clipboard: Ctrl C, Enter", text);
    }

    function copyToClipboard(input, message) {

        var success   = true,
            range     = document.createRange(),
            selection;

        // For IE.
        if (window.clipboardData) {
            window.clipboardData.setData("Text", input.val());
        } else {
            // Create a temporary element off screen.
            var tmpElem = $('<div>');
            tmpElem.css({
                position: "absolute",
                left:     "-1000px",
                top:      "-1000px",
            });
            // Add the input value to the temp element.
            tmpElem.text(input.val());
            $("body").append(tmpElem);
            // Select temp element.
            range.selectNodeContents(tmpElem.get(0));
            selection = window.getSelection ();
            selection.removeAllRanges ();
            selection.addRange (range);
            // Lets copy.
            try {
                success = document.execCommand ("copy", false, null);
            }
            catch (e) {
                copyToClipboardFF(input.val());
            }
            if (success) {
                alert (message);
                tmpElem.remove();
            }
        }
    }
});