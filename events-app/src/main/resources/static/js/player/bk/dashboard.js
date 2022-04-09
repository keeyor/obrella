let actions;
let layout = 1;
let playlist_array =[];
let video_playing_index = 0;
let video_url     = "";
let intro_url     = "";
let hasSlides     = "false";
let poster_image  = "";
let showIntro     = "false";
let mode          = "sync_on";
let global_vid_duration;
let sync_events_flag = 1;

let clip_start = -1;
let clip_end = -1;

/* CUTS */
cuepoints_c = [];
cut_end_in_sec =[];
let cuepoints_m = [];

let cuepoints_x = [];
let slide_at = [];
let slide_titles = [];

let cuepoints_noslides = [];
let slide_at_noslides = [];
let slide_titles_noslides = [];

    $(document).ready(function () {
        init();
    });

    function init() {
        global_vid_duration = convert_hh_mm_ss_format($("#duration").val());
        //init controls
        $("[data-toggle='switch']").bootstrapSwitch();

        video_url = $("#video_url").val();
        intro_url = $("#intro_url").val();
        hasSlides = $("#hasSlides").val();
        showIntro = $("#showIntro").val();

        if (hasSlides === "true") {
            showPresentation();
        }
        else {
            hidePresentation();
        }

        define_actions();
        define_events();
        loadPlayerContent();
        go_to_time_func();
        init_HTML5(playlist_array);
    }

    function getRootSitePath() {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let applicationName = _location.substring(0, applicationNameIndex) + '/';
        let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

        return _location.substring(0, webFolderIndex);
    }

    function loadPlayerContent() {
                    if (showIntro !== "true"){
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
            console.log("Slide Clicked. GoToSlide:" + index);
            actions.go_to_slide(index);
            if(mode === "sync_on" && time !== -1) {
                console.log("Slide Clicked. Sync Video to:" + (cuepoints_x[index] / 1000));
                getVideo().currentTime = cuepoints_x[index] / 1000;
            }
            else if (time === -1) {
                console.log("slide not synced");
            }
            e.preventDefault();
        });
        $("#layout_button").on('click',function(e) {
            if ( layout === 1) {
                $("#video_wrapper").appendTo("#right-box");
                $("#presentation_wrapper").appendTo("#left-box");
                layout = 2
                $("#layout_button").text("Layout B");
            }
            else {
                $("#video_wrapper").appendTo("#left-box");
                $("#presentation_wrapper").appendTo("#right-box");
                layout = 1;
                $("#layout_button").text("Layout A");
            }
        });

    }

    function hidePresentation() {
        $("#slide_no").prop("disabled", true);
        $("[name='sync_mode']").bootstrapSwitch('disabled', true);
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

    function define_actions(){
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
            },
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

    function define_events() {
        $('#sync_mode_btn').on('switchChange.bootstrapSwitch', function (event, state) {
        //$("[name='sync_mode']").on('switchChange', function () {
            if(state === true){
                mode = 'sync_on';
                let time_to_go =  Math.round(getVideo().currentTime)*1000; //Math.round(pp.getPosition())*1000;
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
                let jsonData = JSON.parse(cuepointsx_m.cues[k].text);
                let slideIndexToShow = parseInt(jsonData.index);
                actions.go_to_slide(slideIndexToShow);
            }else
                mode = 'sync_off';
        });
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
    function secondsTimeSpanToHMS(s) {
        let h = Math.floor(s/3600); //Get whole hours
        s -= h*3600;
        let m = Math.floor(s/60); //Get remaining minutes
        s -= m*60;
        return (h < 10 ? '0'+h : h)+":"+(m < 10 ? '0'+m : m)+":"+(s < 10 ? '0'+s : s); //zero padding on minutes and seconds
    }

    function introPlaying() {
        return playlist_array.length === 2 && video_playing_index === 0;
    }

    function onPlay_controls() {

        let $sync_mode_sync = $("[name='sync_mode']");
        let $playPauseVideoBt = $("#playPauseVideoBt");

        if(introPlaying()){
            mode = 'sync_off';
            $sync_mode_sync.bootstrapSwitch('disabled', true);
            $playPauseVideoBt.prop('disabled', false);
        }else{
            if(sync_events_flag === 1){
                if($sync_mode_sync.length !== 0){ //If we are not in only video mode (no slides available)
                    mode = 'sync_on';
                    $sync_mode_sync.bootstrapSwitch('disabled', false);
                    $sync_mode_sync.bootstrapSwitch('state', true, true);
                }
            }
        }
        $playPauseVideoBt.focus();
    }


