var crossDomain_option = false;

var movie_h = 0;
var active_slide_h = 0;
var go_to_time_refresh = 1;
var go_to_slide_refresh = 1;

cuepoints_c = [];
cut_end_in_sec =[];

var clip_start = -1;
var clip_end = -1;

cuepointsx = [];
slide_at = [];
slide_titles = [];
cuepointsx_noslides = [];
slide_at_noslides = [];
slide_titles_noslides = [];

var jwplayer_captions = [];
var captions_default_lang = 1;

var info,actions,pp;
var basepath_subs = '';
var debug=0;
var sync_events_flag = 1;
var mode = "sync_on";

var subs_mode = "off";
var global_vid_duration = -1;
var clip_duration = -1;
var provider_type = "";
var video_url ="";
var video_url_webm ="";
var intro_url = "";
var intro_url_web = "";
var basepath = "";
var number_of_total_slides = 0;
var slide_to_be_loaded = "";
var video_aspectratio;
var overlay_activation_status;
var playlist_array = [];

var production_mode = 1;
var webRoot = "";
var presentation_file="presentation.xml";
var slides_file="slideSync.xml";
var cuts_file="cuts.xml";
var subs_file="subs.xml";
var clip_file = "clip.xml";

var subs_loaded = 0;
var cuts_loaded = 0;
var clip_loaded = 0;
var slides_loaded = 0;
var current_vid_sec = -1;

function reformat_date(date_original){
    var date_splited = date_original.split("-");
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
            //video_url = $(xml).find('resource').text();
            //video_url_webm = $(xml).find('resource_webm').text();
            //intro_url = $(xml).find('intro').text();            
            //intro_url_webm = $(xml).find('intro_webm').text(); 
            video_url = $(xml).find('resource[format=MP4]').text();
            video_url_webm = $(xml).find('resource[format=WEBM]').text();
            intro_url = $(xml).find('intro[format=MP4]').text();
            intro_url_webm = $(xml).find('intro[format=WEBM]').text();

            lecture_header = $(xml).find('header').text();
            if(lecture_header.substring(0,6) == "event:"){
                lecture_header = lecture_header.substring(6,lecture_header.length);
                $('#lecture_department').html(language.p1+"<span><\/span>");
                $('#lecture_serie').html(language.p2+"<span><\/span>");
            }
            lecture_serie = $(xml).find('serie').text();
            lecture_presenter = $(xml).find('presenter').text();
            lecture_duration = $(xml).find('duration').text();
            global_vid_duration = convert_hh_mm_ss_format(lecture_duration);

            lecture_real_duration = $(xml).find('real_duration').text();

            video_aspectratio = $(xml).find('aspectratio').text();
            if(video_aspectratio == '0:0'){
                video_aspectratio = "4:3";//Default when rest call returns nothing specific                
            }
            overlay_activation_status = $(xml).find('overlay').text();

            $('<div class="header-item"><\/div>').html(title +' | '+date_txt).prependTo($('.header'));
            $('<div class="header-logo"><img alt="Institution logo" src="'+logo+'"><\/img><\/div>').prependTo($('.header'));

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
            if(lecture_presenter == -1)
                $('#lecture_presenter').remove();
            else
                $('#lecture_presenter span').html(lecture_presenter);

            $(".icon-info").parent().click(function(){
                $('#show_lecture_info').dialog( "open" );
            });

        },
        complete: function(){
            read_subs_XML();

            var refreshIntervalIdForSubs = setInterval(function(){
                if(subs_loaded == 1){
                    clearInterval(refreshIntervalIdForSubs);
                    load_player();
                }
            },200);
            var refreshIntervalIdForPlayer = setInterval(function(){
                if(jwplayer("player").getState() == 'IDLE' || jwplayer("player").getState() == 'BUFFERING' || jwplayer("player").getState() == 'PLAYING' || jwplayer("player").getState() == 'PAUSED'){
                    clearInterval(refreshIntervalIdForPlayer);
                    if(clip_file == -1)
                        read_cuts_XML();//First load player and then add cuts
                    else
                        read_clip_XML();//When cuts are loaded load clip
                }
            },200);
            if(clip_file != -1){
                var refreshIntervalIdForClip = setInterval(function(){
                    if(clip_loaded == 1){
                        clearInterval(refreshIntervalIdForClip);
                        read_cuts_XML();//When clip is loaded load slides
                    }
                },200);
            }
            var refreshIntervalIdForCuts = setInterval(function(){
                if(cuts_loaded == 1){
                    clearInterval(refreshIntervalIdForCuts);
                    read_slides_XML();
                }
            },200);
            var refreshIntervalIdEverythingReady = setInterval(function(){
                if(slides_loaded == 1){
                    clearInterval(refreshIntervalIdEverythingReady);
                    if(provider_type != "audio" || playlist_array.length == 2)    //Audio has to be fully buffered and this is done via playlistitem even
                        $("#dvLoading").hide();

                    /*Overlay additions*/
                    if ($("#movie > #player").length){
                        $('#jwplayer_overlay').css('width',$('#movie > #player').width()-20+'px');
                    }else{
                        $('#jwplayer_overlay').css('width',$('#movie > #player_wrapper').width()-20+'px');
                    }
                }
            },200);


        }
    });
}

function load_player(){
    var poster_image = '';
    if(provider_type == 'audio' || provider_type == 'rtmp-audio'){
        poster_image = webRoot + "/resources/richLecture_addons/css/images_player/AudioOnly.jpg";
        jwplayer_captions = []; //JWPlayer does not support subs when MP3 is streamed
    }

    if(intro_url == -1){
        if(video_url_webm != -1){
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
        if(intro_url_webm != -1 && video_url_webm != -1){
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
                    tracks:jwplayer_captions,
                    image: poster_image
                }
            ];
        }
    }

    jwplayer("player").setup({
        width: "100%",
        aspectratio: video_aspectratio,
        playlist: playlist_array,
        startparam: "start" /*JW docs ara mistaken...start parameter is the right for mod_h264 on apache instread of starttime*/
    });
    pp = jwplayer("player");

    var lecture_presenter_tmp = lecture_serie;

    if(lecture_presenter != -1)
        lecture_presenter_tmp = lecture_presenter_tmp+ '<br/>' + lecture_presenter.replace(',', ', ');

    pp.onReady(function(){
        if ($("#movie > #player").length){
            $('#movie > #player').append('<div id="jwplayer_overlay">'+lecture_presenter_tmp+'<\/div>');
            $('#jwplayer_overlay').css('width',$('#movie > #player').width()-20+'px');
        }else{
            $('#movie > #player_wrapper').append('<div id="jwplayer_overlay">'+lecture_presenter_tmp+'<\/div>');
            $('#jwplayer_overlay').css('width',$('#movie > #player_wrapper').width()-20+'px');
        }
    });

    pp.onPlay(function(event){
        if(pp.getPlaylistIndex() == 0 && playlist_array.length == 2){
            //pp.setControls(false);
        }else{
            //pp.setControls(true);
            /*Overlay additions*/
            if(overlay_activation_status == 1){
                setTimeout(function(){   //Add a time delay in order for getPosition() to work properly
                    var playing_first_secs = 0;

                    if(typeof cut_end_in_sec[0] !== 'undefined') {
                        playing_first_secs = cut_end_in_sec[0];
                    }
                    //console.log(parseInt(pp.getPosition() - playing_first_secs,10));
                    if(parseInt(pp.getPosition() - playing_first_secs,10) < 10 && pp.getState() == "PLAYING" ){   //This is in order to get rid of start trims of clip start
                        $('#jwplayer_overlay').fadeIn();
                        setTimeout(function(){
                            $('#jwplayer_overlay').fadeOut();
                        }, 10000);
                    }
                }, 1000);
            }
        }
    });
    /*
    pp.onComplete(function(event){
        if(pp.getPlaylistIndex() == 0 && playlist_array.length == 2){
            pp.setControls(true);
        }
    });
    */
    pp.onPlaylistItem(function(event){
        if(event.index == 0 && playlist_array.length == 2){
            mode = 'sync_off';
            $("[name='sync_mode']").bootstrapSwitch('disabled', true);
            $('button').prop('disabled', true);//Disable all buttons when intro video is playing
            $('input').prop('disabled', true);//Disable all inputs when intro video is playing
        }else{
            if(jwplayer_captions.length != 0)
                pp.setCurrentCaptions(captions_default_lang);

            if(provider_type == "audio"){
                $("#dvLoading").show();
                video_is_buffered_counter = 0;
                var video_is_buffered = setInterval(function(){
                    if(pp.getBuffer() == 100){
                        clearTimeout(video_is_buffered);
                        $("#dvLoading").hide();

                        if(playlist_array.length == 2){
                            pp.play(true);
                        }
                    }else{
                        if(playlist_array.length == 1 && video_is_buffered_counter == 0){
                            pp.play(true);
                            video_is_buffered_counter = 1;
                        }
                        pp.pause(true);
                    }
                },100);
            }

            if(sync_events_flag == 1){
                if($("[name='sync_mode']").length != 0){ //If we are not in only video mode (no slides available)
                    mode = 'sync_on';
                    $("[name='sync_mode']").bootstrapSwitch('disabled', false);
                    $("[name='sync_mode']").bootstrapSwitch('state', true, true);
                }
            }
            $('button').prop('disabled', false);//Enable all buttons when recture video is playing
            $('input').prop('disabled', false);//Enable all inputs when recture video is playing
        }
    });
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
            var subs_counter = 1;
            $(xml_for_subs).find("sub").each(function() {
                //var description_sub = $(this).find('description').text();
                var url_sub = $(this).find('url').text();
                var url_sub_split = url_sub.split(".");
                var url_sub_split2 = url_sub_split[0].split("-");
                var url_sub_lang = url_sub_split2[0].replace("sub_","");
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

            if($(xml).find('clip_starttime').text() != -1){
                clip_start = convert_hh_mm_ss_format($(xml).find('clip_starttime').text());
            }

            if($(xml).find('clip_endtime').text() != -1){
                clip_end =  convert_hh_mm_ss_format($(xml).find('clip_endtime').text());
            }

            if($(xml).find('clip_duration').text() != -1){
                $('#lecture_duration span').html($(xml).find('clip_duration').text());
                clip_duration = convert_hh_mm_ss_format($(xml).find('clip_duration').text());
            }

            $('#lecture_title span').html(title);
            $('#lecture_description span').html(description);

            var lecture_presenter_tmp = lecture_serie;

            if(lecture_presenter == -1)
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
    $.ajax({
        type: "GET",
        url: cuts_file,
        crossDomain : crossDomain_option,
        cache: false,    	//This will force requested pages not to be cached by the browser
        dataType: "xml",
        success: function(xml_for_trims_cuts) {
            var xml_clips = $(xml_for_trims_cuts).find("clips");
            var xml_trims = $(xml_for_trims_cuts).find("trims");

            end_sec = -1;
            xml_trims.find("start").each(function() {
                begin_sec = $(this).find('begin').text();
                end_sec = $(this).find('end').text();
                begin_sec=convert_hh_mm_ss_format(begin_sec);
                end_sec=convert_hh_mm_ss_format(end_sec);
                cut_end_in_sec[begin_sec*1000]=end_sec;
                cuepoints_c.push(begin_sec*1000);
            });
            if(clip_start > end_sec && clip_start != 0 && clip_start != -1){    //Enable clip start
                cut_end_in_sec[0]=clip_start;
                if(end_sec == -1)
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
            });
            if(clip_end < begin_sec && clip_end != -1){    //Enable clip end
                if(begin_sec != global_vid_duration){
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

                if((end_sec <= clip_start && clip_start != -1)||(begin_sec >= clip_end && clip_end != -1)){
                    //console.log('oloklhro entos clip trims - OK');
                }else if(begin_sec < clip_start && end_sec > clip_start && clip_start != -1){
                    //console.log('meros entos clip start - OK');
                    cut_end_in_sec[0]=end_sec;
                }else if(begin_sec < clip_end && end_sec > clip_end && clip_end != -1){
                    //console.log('meros entos clip end - OK');
                    cuepoints_c.splice(cuepoints_c.indexOf(cut_end_in_sec.indexOf(global_vid_duration)), 1);
                    cut_end_in_sec.splice(cut_end_in_sec.indexOf(global_vid_duration), 1);
                    cut_end_in_sec[begin_sec*1000]=global_vid_duration;
                    cuepoints_c.push(begin_sec*1000);
                }else{
                    //console.log('create cut');
                    cut_end_in_sec[begin_sec*1000]=end_sec;
                    cuepoints_c.push(begin_sec*1000);
                }
            });

            pp.onTime(function(event,k){
                for(k = 0; k < cuepoints_c.length; k++){
                    if((pp.getPlaylistIndex() == 1 || playlist_array.length == 1)&& parseInt(event.position) == parseInt(cuepoints_c[k]/1000)){
                        var new_time =  cut_end_in_sec[cuepoints_c[k]] ;
                        if(new_time == global_vid_duration)
                            pp.stop();
                        else
                            pp.seek(new_time);
                    }
                }
            });

            cuts_loaded = 1;
        }
    });
}

function read_slides_XML(){
    var increament = 0;
    var increament_noslides = 0;
    var slides_xml_exists = 0;

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
                var url = $(this).find('url').text();
                url = basepath + url;
                //var last_slash_pos = url.lastIndexOf('/');
                //url_for_scroll_bar = url.substr(0, last_slash_pos + 1) + "small/" + url.substr(last_slash_pos + 1);
                var time = $(this).find('time').text();
                var slide_title = $(this).find('title').text();
                if(!slide_title.length)
                    slide_title = language.s4;

                if(time != -1){
                    times = time.split(',');
                    for(times_iteration = 0; times_iteration < times.length; times_iteration++){

                        time=convert_hh_mm_ss_format(times[times_iteration]);

                        if( time < global_vid_duration){//Exclude slides that are synced outside video duration - only when sync is done with slides recorder
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

            if(slides_xml_exists == 0){

                $("#resize_icon").remove();
                $("#active_slide").remove();
                $(".right_sidebar").remove();
                $(".main_content").width("100%");
                $("#slides_exist").remove();
                mode = "sync_off";

                pp.onSeek(function(event){
                    if(pp.getPlaylistIndex() == 1 || playlist_array.length == 1){
                        //curr_time = Math.round(pp.getPosition())*1000;
                        curr_time = Math.round(event.offset)*1000;
                        curr_time += 1; //Go to 1 millisecond forward otherwise you will fall to a loop because of cut point -end point- and seek to the same time

                        for (i = 0; i < cuepoints_c.length; i++) {
                            if (cuepoints_c[i] <= curr_time)  {
                                var end_c = cut_end_in_sec[cuepoints_c[i]]*1000;
                                var diff = curr_time - end_c;

                                if (diff <= 0) {
                                    if((end_c/1000) < global_vid_duration){
                                        var waitForNextSeek = setInterval(function(){
                                            if(pp.getState() == "PLAYING"){
                                                clearTimeout(waitForNextSeek);
                                                pp.seek(end_c/1000.0);
                                            }
                                        },100);
                                        /*
                                        setTimeout(function () {
                                          pp.seek(end_c/1000.0);
                                        },100);
                                        */
                                        return false;
                                    }else{
                                        var waitForStop = setInterval(function(){
                                            if(pp.getState() == "PLAYING" || pp.getState() == "IDLE"){
                                                clearTimeout(waitForStop);
                                                pp.stop();
                                            }
                                        },100);
                                        /*
                                        setTimeout(function () {
                                          pp.stop();
                                        },100);
                                        */
                                        //pp.stop();
                                        return false;
                                    }
                                }
                            }
                        }


                    }
                });


                slides_loaded = 1;
                return false;
            }

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
                if(clip_duration == 0){
                    increament = 0;
                    cuepointsx = [];
                }else{
                    cuepointsx = cuepointsx.sort(function(a,b){
                        return a-b;
                    });

                    for(counter_x = 0; counter_x < cuepointsx.length; counter_x++){
                        for(counter_c = 0; counter_c < cuepoints_c.length; counter_c++){

                            if(counter_x == cuepointsx.length-1){
                                if(cuepointsx[counter_x] >= cuepoints_c[counter_c] && global_vid_duration == cut_end_in_sec[cuepoints_c[counter_c]]){
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

            if(cuepointsx.length == 0){ //If all slides are synced in cutted periods get rid of them
                $("#resize_icon").remove();
                $("#active_slide").remove();
                $(".right_sidebar").remove();
                $(".main_content").width("100%");
                $("#slides_exist").remove();
                mode = "sync_off";


                pp.onSeek(function(event){
                    if(pp.getPlaylistIndex() == 1 || playlist_array.length == 1){
                        //curr_time = Math.round(pp.getPosition())*1000;
                        curr_time = Math.round(event.offset)*1000;
                        curr_time += 1; //Go to 1 millisecond forward otherwise you will fall to a loop because of cut point -end point- and seek to the same time

                        for (i = 0; i < cuepoints_c.length; i++) {
                            if (cuepoints_c[i] <= curr_time)  {
                                var end_c = cut_end_in_sec[cuepoints_c[i]]*1000;
                                var diff = curr_time - end_c;

                                if (diff <= 0) {
                                    if((end_c/1000) < global_vid_duration){
                                        var waitForNextSeek = setInterval(function(){
                                            if(pp.getState() == "PLAYING"){
                                                clearTimeout(waitForNextSeek);
                                                pp.seek(end_c/1000.0);
                                            }
                                        },100);
                                        /*
                                        setTimeout(function () {
                                          pp.seek(end_c/1000.0);
                                        },100);
                                        */
                                        return false;
                                    }else{
                                        var waitForStop = setInterval(function(){
                                            if(pp.getState() == "PLAYING" || pp.getState() == "IDLE"){
                                                clearTimeout(waitForStop);
                                                pp.stop();
                                            }
                                        },100);
                                        /*
                                        setTimeout(function () {
                                          pp.stop();
                                        },100);
                                        */
                                        //pp.stop();
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                });
                slides_loaded = 1;
                return false;
            }

            for(j = 0; j < cuepointsx.length; j++){

                var last_slash_pos = slide_at[cuepointsx[j]].lastIndexOf('/');
                url_for_scroll_bar = slide_at[cuepointsx[j]].substr(0, last_slash_pos + 1) + "small/" + slide_at[cuepointsx[j]].substr(last_slash_pos + 1);

                $('<div class="scroll-content-item" id="scroll-content-item-'+cuepointsx[j]/1000+'"><\/div>').html(
                    //'<img src="'+url_for_scroll_bar+'" title="'+(j+1)+' '+slide_titles[cuepointsx[j]]+'" ><div class="scroll-content-img-number">'+(j+1)+'<\/div>'
                    '<img alt="'+(j+1)+' '+slide_titles[cuepointsx[j]]+'" src="'+url_for_scroll_bar+'" title="'+(j+1)+' '+slide_titles[cuepointsx[j]]+'" ><div class="scroll-content-img-number">'+(j+1)+'<\/div><div class="scroll-content-img-title">'+slide_titles[cuepointsx[j]]+'<\/div>'
                ).appendTo($('.scroll-content'));
            };

            pp.onTime(function(event,k){

                if (parseInt(event.position) != current_vid_sec)
                    current_vid_sec = parseInt(event.position);
                else
                    return false;

                for(k = 0; k < cuepointsx.length; k++){
                    if(mode == "sync_on" && (pp.getPlaylistIndex() == 1 || playlist_array.length == 1) && parseInt(event.position) == parseInt(cuepointsx[k]/1000)){
                        var url_seek = slide_at[cuepointsx[k]];
                        var wrap = $("#active_slide img");
                        if( wrap.attr("src") != url_seek){
                            wrap.css("opacity",0.3);
                            wrap.attr("src", url_seek).fadeTo("normal",1);

                            slide_to_be_loaded = $(".scroll-content-item").index( $('#scroll-content-item-'+cuepointsx[k]/1000) );
                            set_active_img($('#scroll-content-item-'+cuepointsx[k]/1000));
                        }
                    }
                }

                if(mode == "sync_on" && (pp.getPlaylistIndex() == 1 || playlist_array.length == 1) && parseInt(event.position) == 0 && cuepoints_c[0] != 0 && (typeof  slide_at[0] === 'undefined')){
                    $(".scroll-content-item").removeClass("active");
                    var wrap = $("#active_slide img");
                    wrap.css("opacity",0.3);
                    wrap.attr("src", webRoot + "/resources/richLecture_addons/css/images_player/blank.gif").fadeTo("normal",1);
                    $(".scroll-pane").animate({scrollTop: 0});
                    $(".go_to_slide").val("00");
                }

            });

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
                var url = $(this).find("img").attr("src");
                url =  url.replace("small/", "");
                if(mode == "sync_off"){
                    var wrap = $("#active_slide img");
                    if( wrap.attr("src") != url){
                        wrap.css("opacity",0.3);
                        wrap.attr("src", url).fadeTo("normal",1);

                        slide_to_be_loaded = $(".scroll-content-item").index( this );
                        set_active_img($(this));
                    }
                }else{
                    var wrap = $("#active_slide img");
                    if( wrap.attr("src") != url){
                        wrap.css("opacity",0.3);
                        wrap.attr("src", url).fadeTo("normal",1);

                        slide_to_be_loaded = $(".scroll-content-item").index( this );
                        set_active_img($(this));
                    }
                    if(pp.getPlaylistIndex() == 1 || playlist_array.length == 1){
                        var current_slide_time =  $(this).attr("id").replace("scroll-content-item-", "");
                        pp.seek(current_slide_time);
                    }
                }
            });//.filter(":first").click();

            pp.onSeek(function(event){
                if(pp.getPlaylistIndex() == 1 || playlist_array.length == 1){
                    curr_time = Math.round(event.offset)*1000;
                    //curr_time = Math.round(pp.getPosition())*1000;
                    curr_time += 1; //Go to 1 millisecond forward otherwise you will fall to a loop because of cut point -end point- and seek to the same time

                    time_to_go = curr_time;

                    for (i = 0; i < cuepoints_c.length; i++) {
                        if (cuepoints_c[i] <= curr_time)  {
                            var end_c = cut_end_in_sec[cuepoints_c[i]]*1000;
                            var diff = curr_time - end_c;

                            if (diff <= 0) {
                                if((end_c/1000) < global_vid_duration){

                                    var waitForNextSeek = setInterval(function(){
                                        if(pp.getState() == "PLAYING"){
                                            clearTimeout(waitForNextSeek);
                                            pp.seek(end_c/1000.0);
                                        }
                                    },100);
                                    /*
                                    setTimeout(function () {
                                      pp.seek(end_c/1000.0);
                                    },100);
                                    */
                                    return false;
                                }else{
                                    var waitForStop = setInterval(function(){
                                        if(pp.getState() == "PLAYING" || pp.getState() == "IDLE"){
                                            clearTimeout(waitForStop);
                                            pp.stop();
                                        }
                                    },100);
                                    /*
                                    setTimeout(function () {
                                      pp.stop();
                                    },100);
                                    */
                                    //pp.stop();
                                    time_to_go = 0;//global_vid_duration*1000;
                                }
                                break; //We do not need to look all other cut point if we find one matcing (consider that cut point should not overlap)
                            }
                        }
                    }

                    if(mode == "sync_on"){
                        total_num_of_slides = cuepointsx.length ;
                        var k = -1 ;
                        for (var i = 0; i < total_num_of_slides; i++) {
                            if (time_to_go >= cuepointsx[total_num_of_slides-1]){
                                k = total_num_of_slides - 1;
                                break;
                            }else if (cuepointsx[i] <= time_to_go  && time_to_go < cuepointsx[i+1]){
                                k = i;
                                break;
                            }
                        }

                        if(k == -1){
                            $(".scroll-content-item").removeClass("active");
                            var wrap = $("#active_slide img");
                            wrap.css("opacity",0.3);
                            wrap.attr("src", webRoot + "/resources/richLecture_addons/css/images_player/blank.gif").fadeTo("normal",1);
                            $(".scroll-pane").animate({scrollTop: 0});
                            $(".go_to_slide").val("00");
                        }else{
                            slide_to_be_loaded = $(".scroll-content-item").index( $('#scroll-content-item-'+cuepointsx[k]/1000) );
                            var url = slide_at[cuepointsx[k]];
                            var wrap = $("#active_slide img");
                            if( wrap.attr("src") != url){
                                wrap.css("opacity",0.3);
                                wrap.attr("src", url).fadeTo("normal",1);
                                set_active_img($('#scroll-content-item-'+cuepointsx[k]/1000));
                            }
                        }
                    }

                }
            });
            slides_loaded = 1;
        }
    });
}

function set_active_img(item){
    if(go_to_slide_refresh == 1)
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
    var h = Math.floor(s/3600); //Get whole hours
    s -= h*3600;
    var m = Math.floor(s/60); //Get remaining minutes
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
            /*Overlay additions*/
            if ($("#movie > #player").length){
                $('#jwplayer_overlay').css('width',$('#movie > #player').width()-20+'px');
            }else{
                $('#jwplayer_overlay').css('width',$('#movie > #player_wrapper').width()-20+'px');
            }
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
        var resizeTimer;
        if (resizeTimer) {
            clearTimeout(resizeTimer);   // clear any previous pending timer
        }

        resizeTimer = setTimeout(function() {
            resizeTimer = null;
            // It will only be called when there's been a pause in resize events
            $('#movie').css("height","100%");
            $('#active_slide').css("height","100%");
            $("#next_slide").css("left",($("#active_slide").width()-45)+"px");
            /*Overlay additions*/
            if ($("#movie > #player").length){
                $('#jwplayer_overlay').css('width',$('#movie > #player').width()-20+'px');
            }else{
                $('#jwplayer_overlay').css('width',$('#movie > #player_wrapper').width()-20+'px');
            }

            scroll_area_img_height = 0;
            $(".scroll-content").children().each(function(){
                scroll_area_img_height = scroll_area_img_height + $(this).outerHeight();
            });
            $(".scroll-content").height(scroll_area_img_height);

            $(".header-item").css("width",$(".header").width()-800);

            if( $("#contentwrapper").css("width").replace("px",'') == $("#contentwrapper").css("maxWidth").replace("px",'')){
                $("#contentwrapper").addClass("contentwrapper_max");
                var dynamic_left_distance = $( window ).width() - $("#contentwrapper").css("maxWidth").replace("px",'');
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

    if( $("#contentwrapper").css("width").replace("px",'') == $("#contentwrapper").css("maxWidth").replace("px",'')){
        $("#contentwrapper").addClass("contentwrapper_max");
        var dynamic_left_distance = $( window ).width() - $("#contentwrapper").css("maxWidth").replace("px",'');
        $("#contentwrapper").css('left',dynamic_left_distance/2 - 15);
    }else{
        $("#contentwrapper").removeClass("contentwrapper_max");
        $("#contentwrapper").css('left',0);
    }

    $("[name='sync_mode']").bootstrapSwitch();

    $("[name='sync_mode'][name='sync_mode']").on('switchChange', function () {
        if($(this).prop('checked')){
            mode = 'sync_on';
            time_to_go = Math.round(pp.getPosition())*1000;

            total_num_of_slides = cuepointsx.length ;
            var k = -1 ;
            for (var i = 0; i < total_num_of_slides; i++) {
                if (time_to_go >= cuepointsx[total_num_of_slides-1]){
                    k = total_num_of_slides - 1;
                    break;
                }else if (cuepointsx[i] <= time_to_go  && time_to_go < cuepointsx[i+1]){
                    k = i;
                    break;
                }
            }
            slide_to_be_loaded = $(".scroll-content-item img").index( $('#scroll-content-item-'+cuepointsx[k]/1000+' img') );
            var url = slide_at[cuepointsx[k]];
            var wrap = $("#active_slide img");
            if( wrap.attr("src") != url){
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
            var go_to_time = $(".go_to_time").val();
            var go_to_time_sec = convert_hh_mm_ss_format(go_to_time);
            var broken_time=go_to_time.split(':');

            if(go_to_time.length == 8 && go_to_time_sec < global_vid_duration && broken_time.length == 3)
                if(broken_time[0].length == 2 && broken_time[1].length == 2 && broken_time[2].length == 2 && $.isNumeric( broken_time[0] )  && $.isNumeric( broken_time[1] ) && $.isNumeric( broken_time[2] ))
                    if(broken_time[0] < 60 && broken_time[1] < 60 && broken_time[2] < 60 )
                        pp.seek(go_to_time_sec);
        },
        go_to_slide: function(){
            initial_slide_num = $('.scroll-content-item.active').children('.scroll-content-img-number').html();
            var go_to_slide = $(".go_to_slide").val();
            if($.isNumeric(go_to_slide)){
                if(go_to_slide > 0 && go_to_slide <= number_of_total_slides){
                    //Bug fix of Beta 0.9 - Start
                    if(mode == "sync_on")
                        pp.seek(cuepointsx[go_to_slide-1]/1000);
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
        if(e.keyCode==13){//When enter key in pressed
            actions.go_to_time();
            go_to_time_refresh = 1;
        }else if (e.keyCode==27){
            go_to_time_refresh = 1;
        }else{
            go_to_time_refresh = 0;
        }
    });

    setInterval(function() {
        if (go_to_time_refresh == 1){
            time_now = jwplayer("player").getPosition();
            $(".go_to_time").val(secondsTimeSpanToHMS(Math.round(time_now)));
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
        if(e.keyCode==13){//When enter key in pressed
            actions.go_to_slide();
            go_to_slide_refresh = 1;
        }else if (e.keyCode==27){
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
    var _location = document.location.toString();
    var applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
    var applicationName = _location.substring(0, applicationNameIndex) + '/';
    var webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);
    var webFolderFullPath = _location.substring(0, webFolderIndex);

    return webFolderFullPath;
}


$(document).ready(function(){
    webRoot = getRootWebSitePath();

    if(production_mode  == 0){
        presentation_file="presentation.xml";
        slides_file="slideSync.xml";
        cuts_file="cuts.xml";
        subs_file="subs.xml";
        clip_file="clip.xml";
    }else{
        if(webRoot == "http://localhost/rlecture" || webRoot == "http://195.134.115.253:8080/rlecture" || webRoot == "http://node-241.med.uoa.gr:8080/rlecture"){
            presentation_file="http://195.134.115.253:8080/dmms/services/presentation/";
            slides_file="http://195.134.115.253:8080/dmms/services/slides/";
            cuts_file="http://195.134.115.253:8080/dmms/services/cuts/";
            subs_file="http://195.134.115.253:8080/dmms/services/subs/";
            clip_file="http://195.134.115.253:8080/dmms/services/clip/";
        }else{
            presentation_file=webRoot+"/services/presentationplayer/";
            slides_file=webRoot+"/services/slides/";
            cuts_file=webRoot+"/services/cuts/";
            subs_file=webRoot+"/services/subs/";
            clip_file=webRoot+"/services/clip/";
        }
    }

/*    load_menu_events();
    define_actions();
    go_to_time_func();
    go_to_slide_func();*/

    if(production_mode == 1){

        vid_lecture_params = window.location.search.replace( "?", "" );
        vid_lecture_id_args = vid_lecture_params.split('&');

        if(vid_lecture_id_args.length == 1){
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
    read_parameters_XML();
    $(this).bind('contextmenu',function(e){return false;});
    scroll_bar_mousewheel();
    make_main_content_resizable();
    make_components_responsive();
});

