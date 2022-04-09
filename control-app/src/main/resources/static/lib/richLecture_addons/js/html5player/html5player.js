let video_playing_index = 0 ;

let crossDomain_option = false;

let movie_h = 0;
let active_slide_h = 0;
let go_to_time_refresh = 1;
let go_to_slide_refresh = 1;

cuepoints_c = [];
let cuepoints_m = [];

cut_end_in_sec =[];

let clip_start = -1;
let clip_end = -1;

cuepointsx = [];
cuepointsx_m = [];

slide_at = [];
slide_titles = [];
cuepointsx_noslides = [];
slide_at_noslides = [];
slide_titles_noslides = [];

let jwplayer_captions = [];
let captions_default_lang = 1;

let info,actions,pp;
let basepath_subs = '';
let debug=0;
let sync_events_flag = 1;
let mode = "sync_on";

let subs_mode = "off";
let global_vid_duration = -1;
let clip_duration = -1;
let provider_type = "";
let video_url ="";
let video_url_webm ="";
let intro_url = "";
let intro_url_web = "";
let basepath = "";
let number_of_total_slides = 0;
let slide_to_be_loaded = "";
let video_aspectratio;
let overlay_activation_status;
let playlist_array = [];

let production_mode = 1;
let webRoot = "";
let presentation_file="presentation.xml";
let slides_file="slideSync.xml";
let cuts_file="cuts.xml";
let subs_file="subs.xml";
let clip_file = "clip.xml";

let subs_loaded = 0;
let cuts_loaded = 0;
let clip_loaded = 0;
let slides_loaded = 0;
let current_vid_sec = -1;

let lecture_presenter;
let title;
let date_txt;
let description,lecture_header,lecture_serie,lecture_duration,logo,lecture_real_duration;

var fullscreen;

function reformat_date(date_original){
    let date_splited = date_original.split("-");
    return (date_splited[2]+'-'+date_splited[1]+'-'+date_splited[0]);
}

function read_parameters_XML() {
    $.ajax({
        type: "GET",
        url: presentation_file,
        dataType: "xml",
        crossDomain : crossDomain_option,
        cache: false,    	//This will force requested pages not to be cached by the browser
        success: function(xml) {
            logo = $(xml).find('logo').text();
            title = $(xml).find('title:first').text();
            $(document).attr('title', title);
            date_txt = reformat_date($(xml).find('date').text());
            description = $(xml).find('description:first').text();
            provider_type = $(xml).find('provider').text();

            video_url = $(xml).find('resource[format=MP4]').text();
            video_url_webm = $(xml).find('resource[format=WEBM]').text();
            intro_url = $(xml).find('intro[format=MP4]').text();
            intro_url_webm = $(xml).find('intro[format=WEBM]').text();

            lecture_header = $(xml).find('header').text();

            lecture_serie = $(xml).find('serie').text();
            lecture_presenter = $(xml).find('presenter').text();
            lecture_duration = $(xml).find('duration').text();
            global_vid_duration = convert_hh_mm_ss_format(lecture_duration);

            lecture_real_duration = $(xml).find('real_duration').text();
            if (lecture_real_duration === '') {
                lecture_real_duration = lecture_duration;
            }

            video_aspectratio = $(xml).find('aspectratio').text();
            if(video_aspectratio === '0:0'){
                video_aspectratio = "4:3";//Default when rest call returns nothing specific
            }
            overlay_activation_status = $(xml).find('overlay').text();

        },
        complete: function(){
            read_subs_XML();

            let refreshIntervalIdForSubs = setInterval(function(){
                if(subs_loaded === 1){
                    clearInterval(refreshIntervalIdForSubs);
                    load_player();
                    if (debug) console.log("playlist_array length:" + playlist_array.length);
                }
            },200);
            //MG: wait for player to load...
            let refreshIntervalIdForLoadMetaData = setInterval(function(){
                if(playermetadataloaded === 1){
                    clearInterval(refreshIntervalIdForLoadMetaData);
                    if (!introPlaying()) {
                        fix_ui(title,date_txt,logo,lecture_real_duration, description,lecture_header,lecture_serie,lecture_presenter);
                        /*  move to script.js onloadmetadata  */
                        let refreshIntervalIdForPlayer = setInterval(function () {
                            if (playermetadataloaded === 1) {
                                clearInterval(refreshIntervalIdForPlayer);
                                if (clip_file === -1)
                                    read_cuts_XML();//First load player and then add cuts
                                else if (clip_file !== -1)
                                    read_clip_XML();//When cuts are loaded load clip
                            }
                        }, 200);

                        if (clip_file !== -1) {
                            let refreshIntervalIdForClip = setInterval(function () {
                                if (clip_loaded === 1) {
                                    clearInterval(refreshIntervalIdForClip);
                                    read_cuts_XML();//When clip is loaded load slides
                                }
                            }, 200);
                        }
                        let refreshIntervalIdForCuts = setInterval(function () {
                            if (cuts_loaded === 1) {
                                clearInterval(refreshIntervalIdForCuts);
                                read_slides_XML();
                            }
                        }, 200);
                    }
                }
            },200);

            let refreshIntervalIdEverythingReady = setInterval(function(){
                if(slides_loaded === 1){
                    clearInterval(refreshIntervalIdEverythingReady);
                    if(provider_type !== "audio" || playlist_array.length === 2)    //Audio has to be fully buffered and this is done via playlistitem even
                        $("#dvLoading").hide();
                }
            },200);


        }
    });
}

function load_player(){
    let poster_image = '';
    if(provider_type === 'audio' || provider_type === 'rtmp-audio'){
        poster_image = webRoot + "/resources/richLecture_addons/css/images_player/AudioOnly.jpg";
        jwplayer_captions = []; //JWPlayer does not support subs when MP3 is streamed
    }

    if(intro_url !== "-1000"){ // disable intro playing for now! 22-11-2020 - if(intro_url === "-1"){
        if(video_url_webm !== "-1"){
            playlist_array = [
                {
                    sources: [
                        { file: video_url },
                        { file: video_url_webm }
                    ],
                    tracks:jwplayer_captions,
                    image: poster_image
                }
            ];
        }else{
            playlist_array = [
                {
                    sources: [
                        { file: video_url }
                    ],
                    tracks:jwplayer_captions,
                    image: poster_image
                }
            ];
        }
    }else{
        if(intro_url_webm !== "-1" && video_url_webm !== "-1"){
            playlist_array = [
                {
                    sources: [
                        { file: intro_url },
                        { file: intro_url_webm }
                    ]
                }
                ,
                {
                    sources: [
                        { file: video_url },
                        { file: video_url_webm }
                    ],
                    tracks:jwplayer_captions,
                    image: poster_image
                }
            ];
        }else{
            playlist_array = [
                {
                    sources: [
                        { file: intro_url }
                    ]
                }
                ,
                {
                    sources: [
                        { file: video_url }
                    ],
                    tracks: jwplayer_captions,
                    image: poster_image
                }
            ];
        }
    }

    init_HTML5();

    let lecture_presenter_tmp = lecture_serie;

    if(lecture_presenter !== -1)
        lecture_presenter_tmp = lecture_presenter_tmp+ '<br/>' + lecture_presenter.replace(',', ', ');

    /*    MG:FORNOW pp.onReady(function(){
          if ($("#movie > #player").length){
            $('#movie > #player').append('<div id="jwplayer_overlay">'+lecture_presenter_tmp+'<\/div>');
            $('#jwplayer_overlay').css('width',$('#movie > #player').width()-20+'px');
          }else{
            $('#movie > #player_wrapper').append('<div id="jwplayer_overlay">'+lecture_presenter_tmp+'<\/div>');
            $('#jwplayer_overlay').css('width',$('#movie > #player_wrapper').width()-20+'px');
          }
        });*/

    /*   MG FORNOW  pp.onPlay(function(event){
            if(pp.getPlaylistIndex() === 0 && playlist_array.length === 2){
                //pp.setControls(false);
            }else{
                //pp.setControls(true);
                /!*Overlay additions*!/
                if(overlay_activation_status === 1){
                    setTimeout(function(){   //Add a time delay in order for getPosition() to work properly
                        let playing_first_secs = 0;

                        if(typeof cut_end_in_sec[0] !== 'undefined') {
                            playing_first_secs = cut_end_in_sec[0];
                        }
                        //console.log(parseInt(pp.getPosition() - playing_first_secs,10));
                        if(parseInt(pp.getPosition() - playing_first_secs,10) < 10 && pp.getState() === "PLAYING" ){   //This is in order to get rid of start trims of clip start
                            $('#jwplayer_overlay').fadeIn();
                            setTimeout(function(){
                                $('#jwplayer_overlay').fadeOut();
                            }, 10000);
                        }
                    }, 1000);
                }
            }
        });*/
    /*
    pp.onComplete(function(event){
        if(pp.getPlaylistIndex() == 0 && playlist_array.length == 2){
            pp.setControls(true);
        }
    });
    */
    /*    pp.onPlaylistItem(function(event){
            if(event.index === 0 && playlist_array.length === 2){
                mode = 'sync_off';
                $("[name='sync_mode']").bootstrapSwitch('disabled', true);
                $('button').prop('disabled', true);//Disable all buttons when intro video is playing
                $('input').prop('disabled', true);//Disable all inputs when intro video is playing
            }else{
                if(jwplayer_captions.length !== 0)
                        pp.setCurrentCaptions(captions_default_lang);

                if(provider_type === "audio"){
                    $("#dvLoading").show();
                    video_is_buffered_counter = 0;
                    let video_is_buffered = setInterval(function(){
                        if(pp.getBuffer() === 100){
                            clearTimeout(video_is_buffered);
                            $("#dvLoading").hide();

                            if(playlist_array.length === 2){
                                pp.play(true);
                            }
                        }else{
                            if(playlist_array.length === 1 && video_is_buffered_counter === 0){
                                pp.play(true);
                                video_is_buffered_counter = 1;
                            }
                            pp.pause(true);
                        }
                    },100);
                }

                if(sync_events_flag === 1){
                    if($("[name='sync_mode']").length !== 0){ //If we are not in only video mode (no slides available)
                        mode = 'sync_on';
                        $("[name='sync_mode']").bootstrapSwitch('disabled', false);
                        $("[name='sync_mode']").bootstrapSwitch('state', true, true);
                    }
                }
                $('button').prop('disabled', false);//Enable all buttons when recture video is playing
                $('input').prop('disabled', false);//Enable all inputs when recture video is playing
            }
        });*/
}

function read_subs_XML(){
    $.ajax({
        type: "GET",
        url: subs_file,
        crossDomain : crossDomain_option,
        cache: false,    	//This will force requested pages not to be cached by the browser
        dataType: "xml",
        success: function(xml_for_subs) {
            basepath_subs = $(xml_for_subs).find('basepath').text();
            let subs_counter = 1;
            $(xml_for_subs).find("sub").each(function() {
                //let description_sub = $(this).find('description').text();
                let url_sub = $(this).find('url').text();
                let url_sub_split = url_sub.split(".");
                let url_sub_split2 = url_sub_split[0].split("-");
                let url_sub_lang = url_sub_split2[0].replace("sub_","");
                jwplayer_captions.push(new Object({file:basepath_subs+url_sub,label:url_sub_lang.toUpperCase()}));
                if(url_sub_lang === language.selectedLang)
                    captions_default_lang = subs_counter;
                subs_counter++;
            });
            subs_loaded = 1;
        }
    });
}

function read_clip_XML(){

    $.ajax({
        type: "GET",
        url: clip_file,
        dataType: "xml",
        crossDomain : crossDomain_option,
        cache: false,    	//This will force requested pages not to be cached by the browser
        success: function(xml) {
            title = $(xml).find('title:first').text();
            description = $(xml).find('description').text();
            overlay_activation_status = $(xml).find('overlay').text();
            lecture_presenter = $(xml).find('presenter').text();

            $(document).attr('title', title);
            $('.header-item').html(title +' | '+date_txt);

            if($(xml).find('clip_starttime').text() !== -1){
                clip_start = convert_hh_mm_ss_format($(xml).find('clip_starttime').text());
            }

            if($(xml).find('clip_endtime').text() !== -1){
                clip_end =  convert_hh_mm_ss_format($(xml).find('clip_endtime').text());
            }

            if($(xml).find('clip_duration').text() !== -1){
                $('#lecture_duration span').html($(xml).find('clip_duration').text());
                clip_duration = convert_hh_mm_ss_format($(xml).find('clip_duration').text());
            }

            $('#lecture_title span').html(title);
            $('#lecture_description span').html(description);

            let lecture_presenter_tmp = lecture_serie;

            if(lecture_presenter === -1)
                $('#lecture_presenter').remove();
            else{
                $('#lecture_presenter span').html(lecture_presenter);
                lecture_presenter_tmp = lecture_presenter_tmp+ '<br/>' + lecture_presenter.replace(',', ', ');
            }

            $('#jwplayer_overlay').html(lecture_presenter_tmp);

            //TODO - When clip duration is 0 then do not load anything!!!

            clip_loaded = 1;
        }
    });
}

function read_cuts_XML(){

    if (debug) console.log("READ CUTS");
    $.ajax({
        type: "GET",
        url: cuts_file,
        crossDomain : crossDomain_option,
        cache: false,    	//This will force requested pages not to be cached by the browser
        dataType: "xml",
        success: function(xml_for_trims_cuts) {
            let xml_clips = $(xml_for_trims_cuts).find("clips");
            let xml_trims = $(xml_for_trims_cuts).find("trims");

            end_sec = -1;
            xml_trims.find("start").each(function() {
                begin_sec = $(this).find('begin').text();
                end_sec = $(this).find('end').text();
                begin_sec=convert_hh_mm_ss_format(begin_sec);
                end_sec=convert_hh_mm_ss_format(end_sec);
                cut_end_in_sec[begin_sec*1000]=end_sec;
                cuepoints_c.push(begin_sec*1000);

                if (!introPlaying()) {
                    cuepoints_m.addCue(new window.VTTCue(begin_sec, end_sec, 'trim start'));
                    if (debug) console.log("TRIM START ADD:" + begin_sec + "->" + end_sec);
                }
            });
            if(clip_start > end_sec && clip_start !== 0 && clip_start !== -1){    //Enable clip start
                cut_end_in_sec[0]=clip_start;
                if(end_sec === -1)
                    cuepoints_c.push(0);
            }

            begin_sec = global_vid_duration;
            xml_trims.find("finish").each(function() {
                begin_sec = $(this).find('begin').text();
                end_sec = $(this).find('end').text();
                begin_sec=convert_hh_mm_ss_format(begin_sec);
                end_sec=convert_hh_mm_ss_format(end_sec);
                cut_end_in_sec[begin_sec*1000]=end_sec;
                cuepoints_c.push(begin_sec*1000);
                if (!introPlaying()) {
                    cuepoints_m.addCue(new window.VTTCue(begin_sec, end_sec, 'trim finish'));
                    if (debug) console.log("TRIM FINISH ADD:" + begin_sec + "->" + end_sec);
                }
            });

            if(clip_end < begin_sec && clip_end !== -1){    //Enable clip end
                if(begin_sec !== global_vid_duration){
                    cut_end_in_sec.splice(cut_end_in_sec.indexOf(end_sec), 1);
                    cuepoints_c.splice(cuepoints_c.indexOf(begin_sec*1000), 1);
                }
                cut_end_in_sec[clip_end*1000]=global_vid_duration;
                cuepoints_c.push(clip_end*1000);
            }

            xml_clips.find("cut").each(function() {
                begin_sec = $(this).find('begin').text();
                end_sec = $(this).find('end').text();
                begin_sec=convert_hh_mm_ss_format(begin_sec);
                end_sec=convert_hh_mm_ss_format(end_sec);

                if((end_sec <= clip_start && clip_start !== -1)||(begin_sec >= clip_end && clip_end !== -1)){
                    //console.log('oloklhro entos clip trims - OK');
                }else if(begin_sec < clip_start && end_sec > clip_start && clip_start !== -1){
                    //console.log('meros entos clip start - OK');
                    cut_end_in_sec[0]=end_sec;
                }else if(begin_sec < clip_end && end_sec > clip_end && clip_end !== -1){
                    //console.log('meros entos clip end - OK');
                    cuepoints_c.splice(cuepoints_c.indexOf(cut_end_in_sec.indexOf(global_vid_duration)), 1);
                    cut_end_in_sec.splice(cut_end_in_sec.indexOf(global_vid_duration), 1);
                    cut_end_in_sec[begin_sec*1000]=global_vid_duration;
                    cuepoints_c.push(begin_sec*1000);
                }else{
                    //console.log('create cut');
                    cut_end_in_sec[begin_sec*1000]=end_sec;
                    cuepoints_c.push(begin_sec*1000);
                    if (!introPlaying()) {
                        cuepoints_m.addCue(new window.VTTCue(begin_sec, end_sec, 'new cut'));
                        if (debug) console.log("CUT ADD:" + begin_sec + "->" + end_sec);
                    }
                }
            });

            cuts_loaded = 1;
        }
    });
}

function read_slides_XML(){

    if (debug) console.log("read slides");
    let increament = 0;
    let increament_noslides = 0;
    let slides_xml_exists = 0;

    $.ajax({
        type: "GET",
        url: slides_file,
        crossDomain : crossDomain_option,
        dataType: "xml",
        cache: false,    	//This will force requested pages not to be cached by the browser
        success: function(xml) {
            basepath = $(xml).find('basepath').text();
            $(xml).find('slide').each(function(){
                slides_xml_exists = 1;
                let url = $(this).find('url').text();
                url = basepath + url;
                //let last_slash_pos = url.lastIndexOf('/');
                //url_for_scroll_bar = url.substr(0, last_slash_pos + 1) + "small/" + url.substr(last_slash_pos + 1);
                let time = $(this).find('time').text();
                let slide_title = $(this).find('title').text();
                if(!slide_title.length) {
                    slide_title = language.s4; // "Χωρίς τίτλο"
                }
                if(time !== "-1"){
                    times = time.split(',');
                    for(let times_iteration = 0; times_iteration < times.length; times_iteration++){

                        time=convert_hh_mm_ss_format(times[times_iteration]);

                        if( time < global_vid_duration){    //Exclude slides that are synced outside video duration - only when sync is done with slides recorder
                            cuepointsx.push(time*1000);
                            slide_at[time*1000]=url;
                            slide_titles[time*1000] = slide_title;
                            increament++;
                        }
                    }
                }else{
                    cuepointsx_noslides.push(increament_noslides*1000);
                    slide_at_noslides[increament_noslides*1000]=url;
                    slide_titles_noslides[increament_noslides*1000] = slide_title;
                    increament_noslides++;
                }
            });

            if (!introPlaying()) {
                for (let s = 0; s < cuepointsx.length; s++) {
                    let time = cuepointsx[s] / 1000;
                    let slide_begin = time;
                    let slide_end;
                    if (s < (cuepointsx.length - 1)) {
                        slide_end = (cuepointsx[s + 1] / 1000) - 1;
                    } else {
                        slide_end = global_vid_duration;
                    }
                    let url = slide_at[time * 1000];
                    let slide_title = slide_titles[time * 1000];
                    let jsonData = '{ "url" : "' + url + '", "title" : "' + slide_title + '", "pos" : ' + s + '}';
                    cuepointsx_m.addCue(new window.VTTCue(slide_begin, slide_end, jsonData));
                }

                if (debug) console.log("SLIDE CUE");
                //print cuepoints_m after update
                if (cuepointsx_m.cues !== undefined) {
                    for (let cm = 0; cm < cuepointsx_m.cues.length; cm++) {
                        let mCue = cuepointsx_m.cues[cm];
                        if (debug) console.log("cue:" + cm + "   start:" + mCue.startTime + "(" + secondsTimeSpanToHMS(mCue.startTime) + ")" +
                            "     end:" + mCue.endTime + "(" + secondsTimeSpanToHMS(mCue.endTime) + ")");

                    }
                }
            }
            if(slides_xml_exists === 0){

                $("#resize_icon").remove();
                $("#active_slide").remove();
                $(".right_sidebar").remove();
                $(".main_content").width("100%");
                $("#slides_exist").remove();


                /*$(".main_content").css('position','absolute');
                $(".main_content").css('display','block');
                $(".main_content").css('top','10%');
                $(".main_content").css('left','20%');*/
                $(".main_content").css('display','block');
                $(".main_content").css('width', '100%');
                $(".main_content").css('margin', '0 auto');

                $("#movie").css('display','block');
                $("#movie").css('width', '80%');
                $("#movie").css('margin', '0 auto');
               // $("#movie").css('max-width', '1280px');
                $("#movie").css('max-height', '720px');

                mode = "sync_off";
                slides_loaded = 1;
                return false;
            }
/*            else { //MG
                $("#movie").css('position','relative');
                $("#movie").css('top','35%');
                $("#movie").css('vertical-align','top');
            }*/

            if(increament === 0){//If there are no slides synced disable sync mode
                $("[name='sync_mode']").bootstrapSwitch('disabled', false);
                $("[name='sync_mode']").bootstrapSwitch('toggleState');
                $("[name='sync_mode']").bootstrapSwitch('disabled', true);
                sync_events_flag = 0;

                increament = increament_noslides;
                cuepointsx = cuepointsx_noslides;
                slide_at = slide_at_noslides;
                slide_titles = slide_titles_noslides;
            }else{
                if(clip_duration === 0){
                    increament = 0;
                    cuepointsx = [];
                }else{
                    cuepointsx = cuepointsx.sort(function(a,b){
                        return a-b;
                    });

                    for(let counter_x = 0; counter_x < cuepointsx.length; counter_x++){
                        for(let counter_c = 0; counter_c < cuepoints_c.length; counter_c++){

                            if(counter_x === cuepointsx.length-1){
                                if(cuepointsx[counter_x] >= cuepoints_c[counter_c] && global_vid_duration === cut_end_in_sec[cuepoints_c[counter_c]]){
                                    cuepointsx.splice(counter_x, 1);
                                    increament--;
                                    counter_x--;
                                }
                            }else if(cuepointsx[counter_x] >= cuepoints_c[counter_c] && cuepointsx[counter_x+1] <= cut_end_in_sec[cuepoints_c[counter_c]]*1000){
                                cuepointsx.splice(counter_x, 1);
                                increament--;
                                counter_x--;
                            }
                        }
                    }
                }
            }

            if(cuepointsx.length === 0){ //If all slides are synced in cutted periods get rid of them
                $("#resize_icon").remove();
                $("#active_slide").remove();
                $(".right_sidebar").remove();
                $(".main_content").width("100%");
                $("#slides_exist").remove();
                mode = "sync_off";
                slides_loaded = 1;
                return false;
            }

            for(let j = 0; j < cuepointsx.length; j++){

                let last_slash_pos = slide_at[cuepointsx[j]].lastIndexOf('/');
                url_for_scroll_bar = slide_at[cuepointsx[j]].substr(0, last_slash_pos + 1) + "small/" + slide_at[cuepointsx[j]].substr(last_slash_pos + 1);

                $('<div class="scroll-content-item" id="scroll-content-item-'+cuepointsx[j]/1000+'"><\/div>').html(
                    //'<img src="'+url_for_scroll_bar+'" title="'+(j+1)+' '+slide_titles[cuepointsx[j]]+'" ><div class="scroll-content-img-number">'+(j+1)+'<\/div>'
                    '<img alt="'+(j+1)+' '+slide_titles[cuepointsx[j]]+'" src="'+url_for_scroll_bar+'" title="'+(j+1)+' '+slide_titles[cuepointsx[j]]+'" ><div class="scroll-content-img-number">'+(j+1)+'<\/div><div class="scroll-content-img-title">'+slide_titles[cuepointsx[j]]+'<\/div>'
                ).appendTo($('.scroll-content'));
            }

            $('.scroll-content').disableSelection();
            number_of_total_slides = increament;

            $(".scroll-pane").animate({scrollTop: 0});
            $(".go_to_slide").val("00");
            $('#num_of_slides').html(number_of_total_slides);

            scroll_area_img_height = 0;
            $(".scroll-content").children().each(function(){
                scroll_area_img_height = scroll_area_img_height + $(this).outerHeight();
            });
            $(".scroll-content").css("height",scroll_area_img_height+"px");

            $(".scroll-content-item").click(function() {
                let url = $(this).find("img").attr("src");
                url =  url.replace("small/", "");
                if(mode === "sync_off"){
                    let wrap = $("#active_slide img");
                    if( wrap.attr("src") !== url){
                        wrap.css("opacity",0.3);
                        wrap.attr("src", url).fadeTo("normal",1);

                        slide_to_be_loaded = $(".scroll-content-item").index( this );
                        set_active_img($(this));
                    }
                }else { //when sync_on
                    let wrap = $("#active_slide img");
                    if( wrap.attr("src") !== url){
                        wrap.css("opacity",0.3);
                        wrap.attr("src", url).fadeTo("normal",1);

                        slide_to_be_loaded = $(".scroll-content-item").index( this );
                        set_active_img($(this));
                    }
                    //if below not needed (?)
                    //if(pp.getPlaylistIndex() === 1 || playlist_array.length === 1){
                    let current_slide_time =  $(this).attr("id").replace("scroll-content-item-", "");
                    getVideo().currentTime = current_slide_time; //pp.seek(current_slide_time);
                    // }
                }
            });//.filter(":first").click();

            slides_loaded = 1;
        }
    });
}

function set_active_img(item){
    if(go_to_slide_refresh === 1)
        $(".go_to_slide").val($(item).find($('.scroll-content-img-number')).html());

    go_slider_to = $(".scroll-pane").scrollTop() + $(item).offset().top - ($(".header").height() + parseInt($("#contentwrapper").css("marginTop").replace("px",''),10) + parseInt($(item).css("marginBottom").replace("px",''),10) + 2 * parseInt($(item).css("marginTop").replace("px",'')) + item.prev().height());

    $(".scroll-content-item").removeClass("active");
    $(item).addClass("active");
    $(".scroll-pane").animate({scrollTop: go_slider_to});
    //$(".scroll-pane").scrollTop(go_slider_to);
}

function convert_hh_mm_ss_format(input_time) {
    broken_time = input_time.split(':');
    hours = minutes = seconds = 0;
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

function make_main_content_resizable() {

    $("#active_slide").resizable({
        handles: {"e": $('#resize_icon')},
        aspectRatio: true,
        alsoResizeReverse: '#movie',
        maxWidth: 800,
        minWidth: 200,
        start: function(){
            movie_h = $('#movie').css("height");
            active_slide_h = $('#active_slide').css("height");
        },
        resize: function(){
            $('#movie').css("height",movie_h);
            $('#active_slide').css("height",active_slide_h);
            $("#next_slide").css("left",($("#active_slide").width()-45)+"px");
        }
    });

    $("#movie").resizable({
        handles: {"e": '', "w": ''},
        aspectRatio: true,
        maxWidth: 720,
        minWidth: 200
    });
}

function make_components_responsive(){
    $("#next_slide").css("left",($("#active_slide").width()-45)+"px");

    $(window ).resize(function() {
        let resizeTimer;
        if (resizeTimer) {
            clearTimeout(resizeTimer);   // clear any previous pending timer
        }

        resizeTimer = setTimeout(function() {
            resizeTimer = null;
            // It will only be called when there's been a pause in resize events
            $('#movie').css("height",movie_h);
            $('#active_slide').css("height","100%");
            $("#next_slide").css("left",($("#active_slide").width()-45)+"px");

            scroll_area_img_height = 0;
            $(".scroll-content").children().each(function(){
                scroll_area_img_height = scroll_area_img_height + $(this).outerHeight();
            });
            $(".scroll-content").height(scroll_area_img_height);

            $(".header-item").css("width",$(".header").width()-800);

            if( $("#contentwrapper").css("width").replace("px",'') === $("#contentwrapper").css("maxWidth").replace("px",'')){
                $("#contentwrapper").addClass("contentwrapper_max");
                let dynamic_left_distance = $( window ).width() - $("#contentwrapper").css("maxWidth").replace("px",'');
                $("#contentwrapper").css('left',dynamic_left_distance/2 - 15);
            }else{
                $("#contentwrapper").removeClass("contentwrapper_max");
                $("#contentwrapper").css('left',0);
            }
        }, 500);
    });
}

function scroll_bar_mousewheel(){
    $(".scroll-pane").mousewheel();
}

function load_menu_events(){

    let $contentwrapper = $("#contentwrapper");
    if( $contentwrapper.css("width").replace("px",'') === $contentwrapper.css("maxWidth").replace("px",'')){
        $contentwrapper.addClass("contentwrapper_max");
        let dynamic_left_distance = $( window ).width() - $contentwrapper.css("maxWidth").replace("px",'');
        $contentwrapper.css('left',dynamic_left_distance/2 - 15);
    }else{
        $contentwrapper.removeClass("contentwrapper_max");
        $contentwrapper.css('left',0);
    }

    $("[name='sync_mode']").bootstrapSwitch();

    $("[name='sync_mode']").on('switchChange', function () {
        if($(this).prop('checked')){
            mode = 'sync_on';
            time_to_go =  Math.round(getVideo().currentTime)*1000; //Math.round(pp.getPosition())*1000;


            total_num_of_slides = cuepointsx.length ;
            let k = -1 ;
            for (let i = 0; i < total_num_of_slides; i++) {
                if (time_to_go >= cuepointsx[total_num_of_slides-1]){
                    k = total_num_of_slides - 1;
                    break;
                }else if (cuepointsx[i] <= time_to_go  && time_to_go < cuepointsx[i+1]){
                    k = i;
                    break;
                }
            }
            slide_to_be_loaded = $(".scroll-content-item img").index( $('#scroll-content-item-'+cuepointsx[k]/1000+' img') );
            let url = slide_at[cuepointsx[k]];
            let wrap = $("#active_slide img");
            if( wrap.attr("src") !== url){
                wrap.css("opacity",0.3);
                wrap.attr("src", url).fadeTo("normal",1);
                set_active_img($('#scroll-content-item-'+cuepointsx[k]/1000));
            }
        }else
            mode = 'sync_off';
    });

    $('#slidesButton').click(function(){
        $(this).addClass('btn-primary');
        $("#titlesButton").removeClass('btn-primary');
        $("#titlesButton").addClass('btn-default');

        $(".scroll-content-item img").show();
        $(".scroll-content-img-title").hide();

        scroll_area_img_height = 0;
        $(".scroll-content").children().each(function(){
            scroll_area_img_height = scroll_area_img_height + $(this).outerHeight();
        });
        $(".scroll-content").height(scroll_area_img_height);

        set_active_img($("#"+$(".scroll-content-item").filter(".active").attr("id")));
    });

    $('#titlesButton').click(function(){
        $(this).addClass('btn-primary');
        $("#slidesButton").removeClass('btn-primary');
        $("#slidesButton").addClass('btn-default');

        $(".scroll-content-item img").hide();
        $(".scroll-content-img-title").show();

        scroll_area_img_height = 0;
        $(".scroll-content").children().each(function(){
            scroll_area_img_height = scroll_area_img_height + $(this).outerHeight();
        });
        $(".scroll-content").height(scroll_area_img_height);

        set_active_img($("#"+$(".scroll-content-item").filter(".active").attr("id")));
    });

    $("[name='sync_mode']").bootstrapSwitch('disabled', true);
    $('button').prop('disabled', true);//Disable all buttons when intro video is playing
    $('input').prop('disabled', true);//Disable all inputs when intro video is playing

    $("#active_slide").disableSelection();
    $('#previous_slide').disableSelection();
    $('#next_slide').disableSelection();

    $("#active_slide").mouseenter(function(){
        $('#previous_slide').show();
        $('#next_slide').show();
    });

    $("#active_slide").mouseleave(function(){
        $('#previous_slide').hide();
        $('#next_slide').hide();
    });

    $('#previous_slide').click(function(){
        $(".scroll-content-item").filter(".active").prev().click();
    });
    $('#next_slide').click(function(){
        $(".scroll-content-item").filter(".active").next().click();
    });



}

function define_actions(){
    actions = {
        go_to_time: function(){
            let go_to_time = $(".go_to_time").val();
            let go_to_time_sec = convert_hh_mm_ss_format(go_to_time);
            let broken_time=go_to_time.split(':');

            if(go_to_time.length === 8 && go_to_time_sec < global_vid_duration && broken_time.length === 3)
                if(broken_time[0].length === 2 && broken_time[1].length === 2 && broken_time[2].length === 2 && $.isNumeric( broken_time[0] )  && $.isNumeric( broken_time[1] ) && $.isNumeric( broken_time[2] ))
                    if(broken_time[0] < 60 && broken_time[1] < 60 && broken_time[2] < 60 )
                        getVideo().currentTime = go_to_time_sec; //pp.seek(go_to_time_sec);
        },
        go_to_slide: function(){
            initial_slide_num = $('.scroll-content-item.active').children('.scroll-content-img-number').html();
            let go_to_slide = $(".go_to_slide").val();
            if($.isNumeric(go_to_slide)){
                if(go_to_slide > 0 && go_to_slide <= number_of_total_slides){
                    //Bug fix of Beta 0.9 - Start
                    if(mode === "sync_on")
                        getVideo().currentTime = cuepointsx[go_to_slide-1]/1000;//       pp.seek(cuepointsx[go_to_slide-1]/1000);
                    else{
                        $(".scroll-content-item").eq( go_to_slide-1 ).click();
                    }
                    //Bug fix of Beta 0.9 - End
                }else{
                    $(".go_to_slide").val(initial_slide_num);
                }
            }else{
                $(".go_to_slide").val(initial_slide_num);
            }
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




function go_to_time_func(){
    $( ".go_to_time" ).click(function() {
        go_to_time_refresh = 0;
    });

    $( ".go_to_time" ).focus(function() {
        go_to_time_refresh = 0;
    });

    $( ".go_to_time" ).select(function() {
        go_to_time_refresh = 0;
    });

    $( ".go_to_time" ).focusout(function() {
        go_to_time_refresh = 1;
    });

    $('.go_to_time').bind('keyup', function(e) {
        if(e.keyCode===13){//When enter key in pressed
            actions.go_to_time();
            go_to_time_refresh = 1;
        }else if (e.keyCode===27){
            go_to_time_refresh = 1;
        }else{
            go_to_time_refresh = 0;
        }
    });

    setInterval(function() {

            if (go_to_time_refresh === 1 && getVideo() !== undefined) {
                time_now = getVideo().currentTime; //jwplayer("player").getPosition();
                let hms = secondsTimeSpanToHMS(Math.round(time_now))
                $(".go_to_time").val(hms);
                if (!introPlaying()) {
                    $(".show_current_time").html(hms + " / " + secondsTimeSpanToHMS(Math.round(global_vid_duration)));
                }
                else {
                    $(".show_current_time").html(hms + " / " + getVideo().duration + " <span class='text-muted'> - CC License </span>");
                }
            }

    }, 1000);

}

function go_to_slide_func(){
    $( ".go_to_slide" ).click(function() {
        go_to_slide_refresh = 0;
    });

    $( ".go_to_slide" ).focus(function() {
        go_to_slide_refresh = 0;
    });

    $( ".go_to_slide" ).select(function() {
        go_to_slide_refresh = 0;
    });

    $( ".go_to_slide" ).focusout(function() {
        go_to_slide_refresh = 1;
        initial_slide_num = $('.scroll-content-item.active').children('.scroll-content-img-number').html();
        $(".go_to_slide").val(initial_slide_num);
    });

    $('.go_to_slide').bind('keyup', function(e) {
        if(e.keyCode===13){//When enter key in pressed
            actions.go_to_slide();
            go_to_slide_refresh = 1;
        }else if (e.keyCode===27){
            go_to_slide_refresh = 1;
            initial_slide_num = $('.scroll-content-item.active').children('.scroll-content-img-number').html();
            $(".go_to_slide").val(initial_slide_num);
        }else{
            go_to_slide_refresh = 0;
        }
    });
}

function getRootWebSitePath()
{
    let _location = document.location.toString();
    let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
    let applicationName = _location.substring(0, applicationNameIndex) + '/';
    let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);
    let webFolderFullPath = _location.substring(0, webFolderIndex);

    return webFolderFullPath;
}

function introPlaying() {

    if (playlist_array.length === 2 && video_playing_index === 0) {
        return true;
    }
    else {
        return false;
    }
}

$(document).ready(function(){
    webRoot = getRootWebSitePath();

    fullscreen = document.getElementById('fs');

    info = document.getElementById("info");

     presentation_file=webRoot+"/services/presentationplayer/";
     slides_file=webRoot+"/services/slides/";
     cuts_file=webRoot+"/services/cuts/";
     subs_file=webRoot+"/services/subs/";
     clip_file=webRoot+"/services/clip/";

    if(production_mode === 1){

        vid_lecture_params = window.location.search.replace( "?", "" );
        vid_lecture_id_args = vid_lecture_params.split('&');

        if(vid_lecture_id_args.length === 1){
            vid_lecture_id = window.location.search.replace( "?rid=", "" );
            presentation_file = presentation_file + vid_lecture_id;
            slides_file = slides_file + vid_lecture_id;
            cuts_file = cuts_file + vid_lecture_id;
            subs_file = subs_file + vid_lecture_id;
            clip_file = -1;
        }else{
            vid_lecture_id = vid_lecture_id_args[0].replace( "rid=", "" );
            presentation_file = presentation_file + vid_lecture_id;
            cuts_file = cuts_file + vid_lecture_id;
            subs_file = subs_file + vid_lecture_id;
            slides_file = slides_file + vid_lecture_id;
            clip_lecture_id = vid_lecture_id_args[1].replace( "clip=", "" );
            clip_file = clip_file + clip_lecture_id;
        }
    }

    //console.log("load menu events");
    load_menu_events();
    //console.log("define_actions");
    define_actions();
    //console.log("go_to_time_func");
    go_to_time_func();
    //console.log("go_to_slide_func");
    go_to_slide_func();

    read_parameters_XML();

    //disable context-menu (right click)
    $(this).bind('contextmenu',function(e){return false;});

    scroll_bar_mousewheel();
    make_main_content_resizable();
    make_components_responsive();


    setInterval(function() {
        if (!introPlaying() && getVideo() !== undefined) {
            time_now = getVideo().currentTime;
            $(".go_to_field").html(secondsTimeSpanToHMS(Math.round(time_now)));
        }
    }, 1000);

     //$("#player").removeAttr("controls");
     document.getElementById("playPauseVideoBt").focus();

$( "#contentwrapper" ).focusin(function() {
        $(".video-controls").css("border", "1px solid silver");
    });
    $( "#contentwrapper" ).focusout(function() {
        $(".video-controls").css("border", "0");
    });
   $("#contentwrapper").on('keydown',function(e) {

       if (!introPlaying()) {
           if (debug) console.log('You downed a key!' + e.which);
           switch (e.which) {
               case 40:
                   volume_down();
                   break;
               case 38:
                   volume_up();
                   break;
               case 77:
                   toggle_mute();
                   break;
               case 70:
                   toggleFullScreen();
                   break;
               case 37:
                   rewind_play();
                   break;
               case 39:
                   forward_play();
                   break;
               default:
                   return; // exit this handler for other keys
           }
           e.preventDefault(); // prevent the default action (scroll / move caret)
       }
    });
});

function onPlay() {

    $(".global_time").html(secondsTimeSpanToHMS(global_vid_duration));

    if(introPlaying()){
        mode = 'sync_off';
        $("[name='sync_mode']").bootstrapSwitch('disabled', true);
        $('button').prop('disabled', true);//Disable all buttons when intro video is playing
        $('input').prop('disabled', true);//Disable all inputs when intro video is playing
        $("#playPauseVideoBt").prop('disabled', false);
    }else{

        /*        if(jwplayer_captions.length !== 0)
                    pp.setCurrentCaptions(captions_default_lang);*/

        /*        if(provider_type === "audio"){
                    $("#dvLoading").show();
                    video_is_buffered_counter = 0;
                    let video_is_buffered = setInterval(function(){
                        if(pp.getBuffer() === 100){
                            clearTimeout(video_is_buffered);
                            $("#dvLoading").hide();

                            if(playlist_array.length === 2){
                                pp.play(true);
                            }
                        }else{
                            if(playlist_array.length === 1 && video_is_buffered_counter === 0){
                                pp.play(true);
                                video_is_buffered_counter = 1;
                            }
                            pp.pause(true);
                        }
                    },100);
                }*/

        if(sync_events_flag === 1){
            if($("[name='sync_mode']").length !== 0){ //If we are not in only video mode (no slides available)
                mode = 'sync_on';
                $("[name='sync_mode']").bootstrapSwitch('disabled', false);
                $("[name='sync_mode']").bootstrapSwitch('state', true, true);
            }
        }
        $('button').prop('disabled', false);//Enable all buttons when lecture video is playing
        $('input').prop('disabled', false);//Enable all inputs when lecture video is playing

    }
    $("#playPauseVideoBt").focus();

}
/* I THINK YOU DO NOT NEED IT. IT's HANLDED IN SEEKED EVENT */
function onSeek() {

    //curr_time = Math.round(pp.getPosition())*1000;
    curr_time = getVideo().currentTime*1000; //Math.round(event.offset)*1000;
    curr_time += 1; //Go to 1 millisecond forward otherwise you will fall to a loop because of cut point -end point- and seek to the same time

    for (i = 0; i < cuepoints_c.length; i++) {
        if (cuepoints_c[i] <= curr_time)  {
            let end_c = cut_end_in_sec[cuepoints_c[i]]*1000;
            let diff = curr_time - end_c;

            if (diff <= 0) {
                if((end_c/1000) < global_vid_duration){
                    let waitForNextSeek = setInterval(function(){
                        if (getVideo().paused === false) {          //if(pp.getState() === "PLAYING"){
                            clearTimeout(waitForNextSeek);
                            getVideo().currentTime = end_c / 1000 ; //pp.seek(end_c/1000.0);
                        }
                    },100);
                    return false;
                }else{
                    let waitForStop = setInterval(function(){
                        if (getVideo().paused === false || getVideo().readyState === 1 ) { //if(pp.getState() === "PLAYING" || pp.getState() === "IDLE"){
                            clearTimeout(waitForStop);
                            getVideo().paused = true;                                       //pp.stop();
                        }
                    },100);
                    return false;
                }
            }
        }
    }

}
function fix_ui(title,date_txt,logo,lecture_real_duration, description,lecture_header,lecture_serie,lecture_presenter) {
    $('<div class="header-item"><\/div>').html(title +' | '+ date_txt).prependTo($('.header'));
    $('<div class="header-logo"><img alt="Institution logo" src="'+logo+'"><\/img><\/div>').prependTo($('.header'));

    if(lecture_header.substring(0,6) === "event:"){
        lecture_header = lecture_header.substring(6,lecture_header.length);
        $('#lecture_department').html(language.p1+"<span><\/span>");
        $('#lecture_serie').html(language.p2+"<span><\/span>");
    }

    $(".header-item").css("width",$(".header").width()-800);

    $( "#show_lecture_info" ).dialog({
        autoOpen: false,
        modal: true,
        width:'500',
        resizable: false,
        draggable: false,
        position:{
            my: "center",
            at: "center",
            of: window
        },
        buttons:{
            Close: function() {    $( this ).dialog( "close" );    }
        }

    });

    $(".ui-dialog-titlebar-close").remove();
    $('#lecture_title span').html(title);
    $('#lecture_date span').html(date_txt);
    $('#lecture_duration span').html(lecture_real_duration);
    $('#lecture_description span').html(description);
    $('#lecture_department span').html(lecture_header);
    $('#lecture_serie span').html(lecture_serie);
    if(lecture_presenter === -1)
        $('#lecture_presenter').remove();
    else
        $('#lecture_presenter span').html(lecture_presenter);

    $(".icon-info").parent().click(function(){
        $('#show_lecture_info').dialog( "open" );
    });

}