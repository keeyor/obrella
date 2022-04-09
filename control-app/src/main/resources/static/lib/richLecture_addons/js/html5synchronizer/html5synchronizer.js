var crossDomain_option = false;

var info,actions,pp;
var global_vid_duration = -1;
var timeline_width=0;
var slides_counter = 0;
var time_now = 0;
var timeline_progress_bar_update_flag = 1;
var lock_flag = 0;
var go_to_time_refresh = 1;
var indicator_every_no_secs = 900; /*Show time every x sec in timeline bar*/
var provider_type = "";
var video_url ="";
var basepath = "";
var number_of_total_slides = 0;
var width_of_presentation_of_synced_slides = 0;
cuepointsx = [];
cuepointsx_m = [];
//slide = [];
slide_at = [];
seek_time = [];
slide_order = [];
var debug=0;
var production_mode = 1;
var mode = "edit";
var edit_marker_on = 0;
var webRoot = "";
var presentation_file="";
var slides_file="";
var num_of_slides=0 ;
var slides_loaded = 0;

function addToMarkerListSelection(begin_sec,title_on_load,next_to_div,div_order){

    div_txt = '<div class="marker_selections" id="marker_selection_'+begin_sec+'"><\/div>';
    
    var active_slide_title_div;
    if(title_on_load !== null){
        title_on_load_tooltip = title_on_load;
        title_on_load = truncate(title_on_load,25,'...');
        active_slide_title_div = '<div class="active_slide_title_div" id="active_slide_title_div_'+begin_sec+'" title="'+title_on_load_tooltip+'">'+title_on_load+'<\/div>';
    }else{
        title_on_load_tooltip = title_on_load;
        title_on_load = truncate($(".active img").attr("title"),25,'...');
        active_slide_title_div = '<div class="active_slide_title_div" id="active_slide_title_div_'+begin_sec+'" title="'+title_on_load_tooltip+'">'+title_on_load+'<\/div>';
    }
    
    // var active_slide_time_div = '<div class="active_slide_time_div">'+secondsTimeSpanToHMS(begin_sec)+'<\/div>';
    var active_slide_time_div = '<input type="text" id="active_slide_time_div_'+ begin_sec+ '" disabled value="'+secondsTimeSpanToHMS(begin_sec)+'" class="active_slide_time_div"><\/input>';
    
    button_for_edit = '<button type="button" title="'+language.s1+'" id="edit_row_from_marker_list_'+begin_sec+'" class="edit_row_from_marker_list" onclick="actions.edit_slide_period('+begin_sec+',event)"></button>';
    button_for_goto = '<button type="button" title="'+language.s2+'" id="goto_row_from_marker_list_'+begin_sec+'" class="goto_row_from_marker_list" onclick="actions.seek('+begin_sec+')"></button>';
    button_for_delete = '<button type="button" title="'+language.s3+'" id="remove_row_from_marker_list_'+begin_sec+'" class="remove_row_from_marker_list" onclick="actions.remove_slide_period('+begin_sec+')"></button>';
    
    if(begin_sec >= global_vid_duration)
        buttons_marker_div = '<div class="buttons_marker_div">'+button_for_edit+'<button type="button" class="goto_row_from_marker_list_inactive"></button>'+button_for_delete+'<\/div>';
    else
        buttons_marker_div = '<div class="buttons_marker_div">'+button_for_edit+button_for_goto+button_for_delete+'<\/div>';
    
    if (div_order === 'after'){
            $(div_txt).html(active_slide_title_div + active_slide_time_div + buttons_marker_div+'<div style="clear:both"><\/div>').hide().insertAfter($(next_to_div)).show('slow');
    }else if(div_order === 'before'){
            $(div_txt).html(active_slide_title_div + active_slide_time_div + buttons_marker_div+'<div style="clear:both"><\/div>').hide().insertBefore($(next_to_div)).show('slow');
    }else{
            $(div_txt).html(active_slide_title_div + active_slide_time_div + buttons_marker_div+'<div style="clear:both"><\/div>').hide().appendTo($('#marker_list_content')).show('slow');
    }
   
    $(".edit_row_from_marker_list").hover(function(){$(this).css('opacity',1);},function(){$(this).css('opacity',0.4);});
    $(".goto_row_from_marker_list").hover(function(){$(this).css('opacity',1);},function(){$(this).css('opacity',0.4);});
    $(".remove_row_from_marker_list").hover(function(){$(this).css('opacity',1);},function(){$(this).css('opacity',0.4);});

    let $market_selection_begin_sec = $('#marker_selection_'+begin_sec);
    if(begin_sec >= global_vid_duration){
        $market_selection_begin_sec.addClass('marker_selection_outside');
        $market_selection_begin_sec.mouseenter(function(){
            $(this).addClass("marker_selection_outside_highlight");
        });
        $market_selection_begin_sec.mouseleave(function(){
            $(".marker_selections").removeClass("marker_selection_outside_highlight");
        });   
    }else{

        $market_selection_begin_sec.mouseenter(function(){
            timeline_bar_mention_start  = Math.round(((begin_sec)/global_vid_duration)*timeline_width);
            if(cuepointsx.indexOf(begin_sec*1000) === cuepointsx.length-1 || (cuepointsx.indexOf(begin_sec*1000) < cuepointsx.length-1 && cuepointsx[cuepointsx.indexOf(begin_sec*1000)+1] >= global_vid_duration*1000) )
                timeline_bar_mention_end = timeline_width;
            else
                timeline_bar_mention_end  = Math.round(((parseInt(cuepointsx[cuepointsx.indexOf(begin_sec*1000)+1]/1000))/global_vid_duration)*timeline_width);     

            new_width = timeline_bar_mention_end   - timeline_bar_mention_start;
            $(".marker_selections").removeClass("highlight_marker_row");
            $('.marked_area').width("2px");
            $('#timeline_bar_'+begin_sec).width(new_width+"px");
            $(this).addClass("highlight_marker_row");
        });
        $market_selection_begin_sec.mouseleave(function(){
            $(".marker_selections").removeClass("highlight_marker_row");
            $('.marked_area').width("2px");
        });       
    }
}

function removeMarkerListSelection(begin_sec){
        $('#marker_selection_'+begin_sec).fadeOut('slow', function(){ $(this).remove(); });
}

function addSlideOnTimelineBar(begin_sec){
        if(begin_sec > global_vid_duration)
            return false;
        timeline_bar_start = Math.round(((begin_sec)/global_vid_duration)*timeline_width);
        
        if(begin_sec-5>0)
            $('<div class="marked_area" id="timeline_bar_'+begin_sec+'" title="'+secondsTimeSpanToHMS(begin_sec)+'"><\/div>').animate({'left':timeline_bar_start+"px",'width':"2px"}).appendTo($('.timeline'));
        else
            $('<div class="marked_area" id="timeline_bar_'+begin_sec+'" title="'+secondsTimeSpanToHMS(begin_sec)+'"><\/div>').animate({'left':timeline_bar_start+"px",'width':"2px"}).appendTo($('.timeline'));
}

function removeSlideOnTimelineBar(begin_sec){
        if(begin_sec > global_vid_duration)
            return false;
        $('#timeline_bar_'+begin_sec).hide('slow', function(){ $(this).remove(); });
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

function read_slides_XML(data){
    var increament = 0;



            basepath = data.basepath + "slides/";
            let slides = data.slides;

            slides.forEach(function(item) {
                                var url = item.url;
                                url = basepath + url;
                                var last_slash_pos = url.lastIndexOf('/');
                                url_for_scroll_bar = url.substr(0, last_slash_pos + 1) + "small/" + url.substr(last_slash_pos + 1);
                                var time = item.time;
                                var slide_title = item.title;
                                if(!slide_title.length) {
                                    slide_title = language.s4;
                                }
                                $('<div class="scroll-content-item"><\/div>').html('<img alt="" src="'+url_for_scroll_bar+'" title="'
                                                                                    +(increament+1)+' '+slide_title+'" ><div class="scroll-content-img-number">'
                                                                                    +(increament+1)+'<\/div>').appendTo($('.scroll-content'));
                                slide_order[increament]=url;
                                times = time.split(',');
                                
                                for(let times_iteration = 0; times_iteration < times.length; times_iteration++){
                                    time=convert_hh_mm_ss_format(times[times_iteration]);
                                    if(time !== -1){
                                        cuepointsx.push(time*1000);
                                        if(seek_time[url] >= 0 ){
                                            if(seek_time[url] > time*1000){
                                                seek_time[url] = time*1000;
                                            }
                                        }else
                                            seek_time[url] = time*1000;                                           
                                       
                                        slide_at[time*1000]=url;

                                        min_diff = 36000000;
                                        min_diff_index = -1;
                                        time_to_inverstigate = time * 1000;
                                        for(i=0; i<cuepointsx.length; i++){
                                            cut_diff = Math.abs(time_to_inverstigate - cuepointsx[i]);
                                            if (cut_diff >0 && cut_diff < min_diff){
                                                min_diff = cut_diff;
                                                min_diff_index  = i;
                                                if(time_to_inverstigate > cuepointsx[i])
                                                    min_diff_order = 'after';
                                                else
                                                    min_diff_order = 'before';
                                            }
                                        }
                                        if(min_diff_index >=0)
                                            addToMarkerListSelection(time,(increament+1)+' '+slide_title,'#marker_selection_'+parseInt(cuepointsx[min_diff_index]/1000,10),min_diff_order);
                                        else
                                            addToMarkerListSelection(time,(increament+1)+' '+slide_title);

                                        addSlideOnTimelineBar(time);

                                        cuepointsx = cuepointsx.sort(function(a,b){
                                                        return a-b;
                                        });

                                        $('.scroll-content-img-number:eq('+increament+')').addClass("slide_synced");
                                    }
                                }
                increament++;
				num_of_slides++;

			});
            //TODO: BE CAREFUL-> CUEPOINTS FROM NICKOS ARE SORTED!!! MY CUEPOINTS DO NOT NEED SORT BECAUSE THEY HAVE position == AUTO !!!
            // ---------------- ADD CUE POINTS --------------------
            for (let s=0; s< cuepointsx.length; s++) {
                let slide_begin = parseInt((cuepointsx[s]/1000).toString(),10);
                let slide_end;
                if (s < (cuepointsx.length - 1)) {
                    slide_end = parseInt((cuepointsx[s+1]/1000).toString(),10);
                } else {
                    slide_end = global_vid_duration;
                }
                let time = cuepointsx[s];
                let url = slide_at[time];
                cuepointsx_m.addCue(new window.VTTCue(slide_begin, slide_end, url));
            }

            if (debug) {
                console.log("SLIDE CUE");
                //print cuepoints_m after update
                if (cuepointsx_m.cues !== undefined) {
                    for (let cm = 0; cm < cuepointsx_m.cues.length; cm++) {
                        let mCue = cuepointsx_m.cues[cm];
                        console.log("cue:" + cm + "   start:" + mCue.startTime + "(" + secondsTimeSpanToHMS(mCue.startTime) + ")" +
                            "     end:" + mCue.endTime + "(" + secondsTimeSpanToHMS(mCue.endTime) + " text: " + mCue.text + " )");

                    }
                }
            }

           let $presentation_of_work_done = $(".presentation_of_work_done");
           width_of_presentation_of_synced_slides = $presentation_of_work_done.width() + parseInt($presentation_of_work_done.css("paddingLeft"), 10) + parseInt($presentation_of_work_done.css("paddingRight"), 10);
           number_of_total_slides = increament;
           $(".number_of_total_slides").html(number_of_total_slides);

           refresh_presentation_of_synced_slides();
                        
           var img_div;
			
			img_div = $(".scroll-content-item");
			total_img_width = img_div.width();
			total_img_width += parseInt(img_div.css("paddingLeft"), 10) + parseInt(img_div.css("paddingRight"), 10);//Total Padding Width
			total_img_width += parseInt(img_div.css("marginLeft"), 10) + parseInt(img_div.css("marginRight"), 10);//Total Margin Width
                    
			scroll_area_img_width = total_img_width * num_of_slides;
			scroll_area_img_width += 4; //2*2px for border size when img is active

			$(".scroll-content").css("width",scroll_area_img_width+"px");
			
			if (debug) {
                info.innerHTML = "Scroll are for thumbenails has width " + scroll_area_img_width + "px";
            }
			$(".scroll-content-item img").click(function() {
                            var url = $(this).attr("src");
                            url =  url.replace("small/", "");
                            //DISABLED ALL BUTTONS ON MARKER EDIT
                            if(edit_marker_on === 0){
                                if(mode === "edit"){
                                    var wrap = $("#active_slide img");
                                    wrap.css("opacity",0.3);
                                    wrap.attr("src", url).fadeTo("normal",1);
                                    //Activate selected image - Added by Nickorfas
                                    slide_to_be_loaded = $(".scroll-content-item img").index( this );
                                    set_active_img($(this));
                                }else{
                                    if($.inArray(url, slide_at) >= 0){
                                        //Activate selected image - Added by Nickorfas
                                        slide_to_be_loaded = $(".scroll-content-item img").index( this );
                                        set_active_img($(this));
                                        var current_slide =  $(this).attr("src");
                                        current_slide = current_slide.replace("small/", "");  //Added by Nickorfas
                                        getVideo().currentTime = seek_time[current_slide] / 1000.0;
                                    }

                                }
                            }
			}).filter(":first").click();

            $(".scroll-content-item img").dblclick(function() {

                            var url = $(this).attr("src");
                            url =  url.replace("small/", "");
                            //DISABLED ALL BUTTONS ON MARKER EDIT
                            if(edit_marker_on === 0 && mode === "edit"){
                                var wrap = $("#active_slide img");
                                wrap.css("opacity",0.3);
                                wrap.attr("src", url).fadeTo("normal",1); 

                                //Activate selected image - Added by Nickorfas
                                slide_to_be_loaded = $(".scroll-content-item img").index( this );
                                set_active_img($(this));
                                actions.synchronize();                                    
                            }
			});
            slides_loaded = 1;

}

function refresh_presentation_of_synced_slides(){  
        setTimeout(function(){
                num_of_syncs = $(".slide_synced").length; //.size();
                $(".number_of_synced_slides").html(num_of_syncs);
                var width_bar_of_synced_slides = parseInt((num_of_syncs/number_of_total_slides)* width_of_presentation_of_synced_slides,10);
                $(".presentation_of_work_done_background").width(width_bar_of_synced_slides);

                num_of_syncs_outside_vid = $(".marker_selection_outside").length; //.size();
                if(num_of_syncs_outside_vid > 0) {
                    $(".number_of_synced_slides_outside_vid").html('(<img alt="" src="' + webRoot + '/lib/richLecture_addons/css/images_synchronizer/AttentionIcon.png ' +
                        +' class="outside_slides_attention_mark"/>' + num_of_syncs_outside_vid + ')');
                }
                else {
                    $(".number_of_synced_slides_outside_vid").html('');
                }
        },1000);
}

function truncate(str, maxLength, suffix) {
    if(str.length > maxLength)
    {
        str = str.substring(0, maxLength + 1); 
        str = str.substring(0, Math.min(str.length, str.lastIndexOf(" ")));
        str = str + suffix;
    }
    return str;
}

function reformat_date(date_original){
    var date_splited = date_original.split("-");
    return (date_splited[2]+'-'+date_splited[1]+'-'+date_splited[0]);    
}

function read_parameters_XML() {
    $(".scroll-region").hide();
    $.ajax({
        type: "GET",
        url: presentation_file,
        cache: false,
        dataType: "json"
    })
        .done (function(data) {
            let logo = data.logo;
            let title = data.title
            $(document).attr('title', title);
            let date_txt = moment.utc(data.date).format("LLL");
            provider_type = data.provider;
            video_url = data.video_url;
            global_vid_duration = convert_hh_mm_ss_format(data.duration);

            let $header = $('.header');
            $('<div class="header-item"><\/div>').html(title + ' | ' + date_txt).prependTo($header);
            $('<div class="header-logo-item-separator"><\/div>').prependTo($header);
            $('<div class="header-logo"><img alt="" src="' + logo + '" /><\/div>').prependTo($header);
        })
        .always (function(data){
            init_HTML5(); //load_player();
            timeline_progress_bar();
            read_slides_XML(data);

            var refreshIntervalIdEverythingReady = setInterval(function(){
                if(slides_loaded === 1){
                    clearInterval(refreshIntervalIdEverythingReady);
                    $(".scroll-region").show();
                    $("#dvLoading").hide();
                }
            },200);
        });
}
function scroll(){
        $(".scroll-fwd").on("click", function(){
                var go_slider_to;
                var move_width = parseInt($('.scroll-pane').width()/total_img_width,10);
                move_width *= total_img_width;

                var curr_scroll_position = $('.scroll-pane').scrollLeft();

                go_slider_to = curr_scroll_position + move_width;
                $(".scroll-pane").stop();
                $(".scroll-pane").animate({scrollLeft: go_slider_to});
        });
        $(".scroll-bwd").on("click", function(){
                var go_slider_to;
                var move_width = parseInt($('.scroll-pane').width()/total_img_width,10);
                move_width *= total_img_width;

                var curr_scroll_position = $('.scroll-pane').scrollLeft();
                
                go_slider_to = curr_scroll_position - move_width;
                $(".scroll-pane").stop();
                $(".scroll-pane").animate({scrollLeft: go_slider_to});
        });
}

function set_active_img(img){
	var go_slider_to = (slide_to_be_loaded * total_img_width)-total_img_width;
	$(".scroll-content-item").removeClass("active");
	$(img).parent().addClass("active");
        setTimeout(function(){$(".scroll-pane").animate({scrollLeft: go_slider_to});},500);	
}

function define_actions(){
    actions = {
           mute: function() {
               if(getVideo().muted){ //if(pp.getStatus().muted){
               $('.player_mute').css( 'background','url('+webRoot + '/lib/richLecture_addons/css/images_synchronizer/unmute-large.png) no-repeat');
               getVideo().muted = false; //pp.unmute();
               getVideo().volume = 0.5 ;//pp.setVolume(50);
            }else{
               $('.player_mute').css( 'background','url('+webRoot + '/lib/richLecture_addons/css/images_synchronizer/mute-large.png) no-repeat');
                   getVideo().muted = true; //pp.mute();
            }
        },
          voldown: function() {
               curr_vol = getVideo().volume; //pp.getVolume();
               if(curr_vol-0.1 <= 0){
                   getVideo().volume = 0; //pp.setVolume(0);
               }else
                   getVideo().volume = curr_vol-0.1; //pp.setVolume(curr_vol-10);
        },
          volup: function() {
               curr_vol = getVideo().volume; //pp.getVolume();
               if(curr_vol+0.1 > 1)
                   getVideo().volume = 1; //pp.setVolume(100);
               else
                   getVideo().volume = curr_vol + 0.1; //pp.setVolume(curr_vol+10);
        },        
           play: function() {
            toggle_play();
        },
           stop: function() {
               getVideo().paused = true; //pp.stop();
               $('.player_play').css( 'background','url('+webRoot + '/lib/richLecture_addons/css/images_synchronizer/play-large.png) no-repeat');
        },
           seek: function(second) {
            if(mode === "edit"){
                var url = slide_at[second *1000];
                slide_to_be_loaded = slide_order.indexOf(url);
                $(".scroll-content-item img:eq("+slide_to_be_loaded+")").click();    
            }
                
            if(second-5 > 0)
                getVideo().currentTime = second -5; //pp.seek(second-5);
            else
                getVideo().currentTime = second; //pp.seek(second);
            
        },
           go_to_time: function(){
               var go_to_time = $(".go_to_field").val();
               var go_to_time_sec = convert_hh_mm_ss_format(go_to_time); 
               var broken_time=go_to_time.split(':'); 
               
               if(go_to_time.length == 8 && go_to_time_sec < global_vid_duration && broken_time.length == 3)
                    if(broken_time[0].length == 2 && broken_time[1].length == 2 && broken_time[2].length == 2 && $.isNumeric( broken_time[0] )  && $.isNumeric( broken_time[1] ) && $.isNumeric( broken_time[2] ))
                        if(broken_time[0] < 60 && broken_time[1] < 60 && broken_time[2] < 60 )
                            getVideo().currentTime = go_to_time_sec; // pp.seek(go_to_time_sec);
               
        },
           synchronize: function(begin_sec){                 
                var new_sync_based_on_video_time = 0;
                if(begin_sec == null){
                    begin_sec = Math.round(getVideo().currentTime); //Math.round(pp.getTime());
                    new_sync_based_on_video_time = 1;
                }
                time_to_inverstigate = begin_sec * 1000;
                if(cuepointsx.indexOf(begin_sec*1000) != -1){
                    alert(language.s5);
                    return false;
                }
                var value_on_slide_order_array = $('.active .scroll-content-img-number').html()-1;
                var active_slide_url = slide_order[value_on_slide_order_array];
                
                cuepointsx.push(begin_sec*1000);							
                //seek_time[active_slide_url] = begin_sec*1000;
                if(seek_time[active_slide_url] >= 0 && seek_time[active_slide_url] != null){
                    if(seek_time[active_slide_url] > begin_sec*1000){
                        seek_time[active_slide_url] = begin_sec*1000;
                    }
                }else
                    seek_time[active_slide_url] = begin_sec*1000;    

                slide_at[begin_sec*1000]=active_slide_url;

                //MG ------------------------ ADD CUEPOINT
                //TODO: ADD and Sort. We don't know the end Time until sorted. Place at end of array, then sort, then update endTime
                cuepointsx_m.addCue(new window.VTTCue(begin_sec, global_vid_duration, active_slide_url));

                //cuepointsx_m.cues.sort(sortCuePointsByStartTime);

                //find new cuepoint && update endTime
                   let targetCueIndex;
                   let found=false;
                   let mCue
                    if (cuepointsx_m.cues !== undefined) {
                        for (let s = 0; s < cuepointsx_m.cues.length; s++) {
                            mCue = cuepointsx_m.cues[s];
                            if (mCue.startTime === begin_sec) {
                                found = true;
                                targetCueIndex = s;
                                break;
                            }
                        }
                    }
                   if (found) {
                       if (targetCueIndex < cuepointsx_m.cues.length-1) { //not last -> update endTime to statTime of Next
                           let nextCuePoint = cuepointsx_m.cues[targetCueIndex+1];
                           //merge if same with next
                           if (nextCuePoint.text !== mCue.text) {
                               mCue.endTime = nextCuePoint.startTime;
                           }
                           else {
                               mCue.endTime = nextCuePoint.endTime;
                              // DO NOT REMOVE: NICKOS IS REMOVING IT BELOW::: cuepointsx_m.removeCue(nextCuePoint);
                           }
                       }
                       if (targetCueIndex >0) { //not first -> update previous endTime to new cue's startTime
                           let previousCuePoint = cuepointsx_m.cues[targetCueIndex-1];
                           //merge if same with previous
                           if (previousCuePoint.text !== mCue.text) {
                               previousCuePoint.endTime = mCue.startTime;
                           }
                           else {
                               previousCuePoint.endTime = mCue.endTime;
                               cuepointsx_m.removeCue(mCue);
                           }
                       }
                   }
                   if (debug) {
                       console.log("SLIDE CUE AFTER SYNC OF:" + targetCueIndex);
                       if (cuepointsx_m.cues !== undefined) {
                           for (let cm = 0; cm < cuepointsx_m.cues.length; cm++) {
                               let mCue = cuepointsx_m.cues[cm];
                               console.log("cue:" + cm + "   start:" + mCue.startTime + "(" + secondsTimeSpanToHMS(mCue.startTime) + ")" +
                                   "     end:" + mCue.endTime + "(" + secondsTimeSpanToHMS(mCue.endTime) + " text: " + mCue.text + " )");

                           }
                       }
                   }

                // MG _END
                cuepointsx = cuepointsx.sort(function(a,b){
                                return a-b;
                });

                updated_index_time_to_investigate = cuepointsx.indexOf(time_to_inverstigate);

                if(cuepointsx.length >= updated_index_time_to_investigate+1){
                        if(slide_at[cuepointsx[updated_index_time_to_investigate]] == slide_at[cuepointsx[updated_index_time_to_investigate+1]]){
                                actions.remove_slide_period(parseInt(cuepointsx[updated_index_time_to_investigate+1]/1000));
                        }
                }

                if(updated_index_time_to_investigate-1 >= 0){
                        if(slide_at[cuepointsx[updated_index_time_to_investigate]] == slide_at[cuepointsx[updated_index_time_to_investigate-1]]){
                                cuepointsx.splice(updated_index_time_to_investigate, 1);          
                                //seek_time[active_slide_url]=null; // We do not need to check anything since seek time of preview time for same slide will be earlier than one removed
                                slide_at[time_to_inverstigate]=null;
                                return false;
                        }
                }

				
                min_diff = 36000000;
                min_diff_index = -1;
                
                for(i=0; i<cuepointsx.length; i++){
                    cut_diff = Math.abs(time_to_inverstigate - cuepointsx[i]);
                    if (cut_diff >0 && cut_diff < min_diff){
                        min_diff = cut_diff;
                        min_diff_index  = i;
                        if(time_to_inverstigate > cuepointsx[i])
                            min_diff_order = 'after';
                        else
                            min_diff_order = 'before';
                    }
                }
                if(min_diff_index >=0)
                    addToMarkerListSelection(begin_sec,null,'#marker_selection_'+parseInt(cuepointsx[min_diff_index]/1000,10),min_diff_order);
                else
                    addToMarkerListSelection(begin_sec,null);  

                $("#marker_list_content").stop();               
                if($("#marker_selection_"+begin_sec).offset().top > $("#marker_list_content").offset().top + $("#marker_list_content").scrollTop() && new_sync_based_on_video_time == 0){                    
                    $("#marker_list_content").animate({
                        scrollTop: $("#marker_selection_"+begin_sec).offset().top - $("#marker_list_content").offset().top + $("#marker_list_content").scrollTop() - 23 //23px is the height that the row will has as height when animation show is completed
                    });                    
                }else{                    
                    $("#marker_list_content").animate({
                        scrollTop: $("#marker_selection_"+begin_sec).offset().top - $("#marker_list_content").offset().top + $("#marker_list_content").scrollTop()
                    });
                }

                addSlideOnTimelineBar(begin_sec);
				
                $('.scroll-content-img-number:eq('+value_on_slide_order_array+')').addClass("slide_synced");
                
/*                pp.getClip(0).onCuepoint(begin_sec*1000, function(clip,point) {
                    if(cuepointsx.indexOf(point) !== -1 && mode === "preview"){
                        // get handle to element that wraps the image and make it semi-transparent	
                        if (debug) info.innerHTML = "onCuepoint: " + point ;	
                        var url = slide_at[point];

                        var wrap = $("#active_slide img");
                        wrap.css("opacity",0.3);
                        wrap.attr("src", url).fadeTo("normal",1); 
                        //slide_to_be_loaded = slide_order[url];
                        slide_to_be_loaded = slide_order.indexOf(url);
                        set_active_img($(".scroll-content-item img:eq("+slide_to_be_loaded+")"));                                           	
                    }
                });*/
                   
                refresh_presentation_of_synced_slides();
                $(".save_btn").prop('disabled', false);
                $(".save_btn").css('opacity', '1');                
        },
          synchronize_and_go:function(){
            var sync_failed = actions.synchronize();            
            var value_on_slide_order_array = $('.active .scroll-content-img-number').html();
            if(slide_order.length > value_on_slide_order_array && sync_failed != false){
                $('.scroll-content-img-number:eq('+value_on_slide_order_array+')').parent().find("img").click();
            }
        },
          go_to_unsynced_slide: function(){              
              $(".scroll-content-img-number:not(.slide_synced):first").parent().find("img").click();
        },
          edit_slide_period: function(second,event_sh){
              
                if(mode == "edit"){
                    var url = slide_at[second *1000];
                    slide_to_be_loaded = slide_order.indexOf(url);
                    $(".scroll-content-item img:eq("+slide_to_be_loaded+")").click();    
                }
                
                if (event_sh.shiftKey) {    
                    getVideo().pause = true; //pp.pause();
                    
                    $('#slide_selected_for_shifting').html($(".scroll-content-item img:eq("+slide_to_be_loaded+")").parent().find("div").html());

                    var time_bf_popup_shift = Math.round(getVideo().currentTime); //Math.round(pp.getTime());
                    var initial_diff_edit_time = time_bf_popup_shift - second;
                    $('#shift_time_diff').val(initial_diff_edit_time);                                        
                    $('#shift_time_start').val(secondsTimeSpanToHMS(time_bf_popup_shift));                                      
                    $('#shift_time_diff').on('change input keyup propertychange',function(){    
                        if($.isNumeric($(this).val()) &&
                            parseInt(second + parseInt($(this).val(),10),10) >= 0&&
                            parseInt(second + parseInt($(this).val(),10),10) < global_vid_duration){
                            $(this).removeClass('shift_time_validation');
                            $('#shift_time_start').removeClass('shift_time_validation');
                            $('#shift_time_start').val(secondsTimeSpanToHMS(parseInt(second + parseInt($(this).val(),10),10)));
                            $("#shift_attention").hide();                      
                            if( $('#shift_time_diff').val() < 0){                                
                                for(var i = 0; i < cuepointsx.length; i++){                                                      
                                  if(cuepointsx[i] >= parseInt(second,10) * 1000)
                                      break;                                                    
                                  if(cuepointsx[i] >= ( parseInt(second + parseInt($(this).val(),10),10)) * 1000 && cuepointsx[i] < parseInt(second,10) * 1000){
                                      $("#shift_attention").show();
                                  }
                                }
                            }
                        }else{
                            $(this).addClass('shift_time_validation');
                            $('#shift_time_start').addClass('shift_time_validation');
                            $('#shift_time_start').val('~'); 
                            $("#shift_attention").hide();
                        }
                    });                    
                    $('#shift_time_start').on('change input keyup propertychange',function(){
                        var broken_shift_start_time_original = $(this).val();
                        var broken_shift_start_time=$(this).val().split(':'); 
                        
                        if (broken_shift_start_time_original.length == 8 && broken_shift_start_time.length == 3 &&
                            convert_hh_mm_ss_format(broken_shift_start_time_original) >= 0 && convert_hh_mm_ss_format(broken_shift_start_time_original) < global_vid_duration &&
                            broken_shift_start_time[0].length == 2 && broken_shift_start_time[1].length == 2 && broken_shift_start_time[2].length == 2 && $.isNumeric( broken_shift_start_time[0] )  && $.isNumeric( broken_shift_start_time[1] ) && $.isNumeric( broken_shift_start_time[2] ) &&                           
                            broken_shift_start_time[0] < 60 && broken_shift_start_time[1] < 60 && broken_shift_start_time[2] < 60){
                                
                                $(this).removeClass('shift_time_validation');
                                $('#shift_time_diff').removeClass('shift_time_validation');
                                $('#shift_time_diff').val(parseInt(convert_hh_mm_ss_format($(this).val()) - second,10));    
                                $("#shift_attention").hide();  
                                if( $('#shift_time_diff').val() < 0){                                    
                                    for(var i = 0; i < cuepointsx.length; i++){                                                      
                                      if(cuepointsx[i] >= parseInt(second,10) * 1000)
                                          break;                                                    
                                      if(cuepointsx[i] >= ( parseInt(second + parseInt($('#shift_time_diff').val(),10),10)) * 1000 && cuepointsx[i] < parseInt(second,10) * 1000){
                                          $("#shift_attention").show();
                                      }
                                    }
                                }
                        }else{
                            $(this).addClass('shift_time_validation');
                            $('#shift_time_diff').addClass('shift_time_validation');
                            $('#shift_time_diff').val('~'); 
                            $("#shift_attention").hide();
                        }
                    });
                    
                    
                    $( "#shift_slides" ).dialog({
                              autoOpen: false,
                              height: 200,
                              width: 700,
                              resizable: false,
                              modal: true,
                              resizable: false,
                              draggable: true,
                              position:{
                                          my: "center",
                                          at: "top+200",
                                          of: window 
                                       },
                              buttons: [
                                {   
                                    text: language.s8,                                                      
                                    click: function() {
                                              
                                              var save_shift_time_diff = $('#shift_time_diff').val();
                                              var save_shift_time_start = $('#shift_time_start').val();
                                              
                                              if(save_shift_time_diff == '~' || save_shift_time_start == '~'){
                                                  alert(language.s10);                                                  
                                                  return false;
                                              }else if (save_shift_time_diff == 0){
                                                  $( this ).dialog( "close" );
                                                  return false;
                                              }else if(save_shift_time_diff < 0){
                                                  for(var i = 0; i < cuepointsx.length; i++){                                                      
                                                    if(cuepointsx[i] >= parseInt(second,10) * 1000)
                                                        break;                                                    
                                                    if(cuepointsx[i] >= (parseInt(parseInt(second,10)+parseInt(save_shift_time_diff,10),10)) * 1000 && cuepointsx[i] < parseInt(second,10) * 1000){
                                                        actions.remove_slide_period(cuepointsx[i]/1000); 
                                                        i--;                                                                                                             
                                                    }
                                                  }
                                              }

                                              var rows_to_shift = [];
                                              var rows_to_shift_url = [];
                                              
                                              rows_to_shift.push(second);
                                              rows_to_shift_url.push(slide_order.indexOf(slide_at[second*1000]));
                                              
                                              $("#marker_selection_"+second).nextAll(".marker_selections").each(function(){
                                                 var row_to_shift_tmp = parseInt($(this).attr('id').replace('marker_selection_',''),10);
                                                 rows_to_shift.push(row_to_shift_tmp);  
                                                 rows_to_shift_url.push(slide_order.indexOf(slide_at[row_to_shift_tmp*1000]));
                                              });
                                              
                                              actions.remove_slide_period(second);  
                                              $("#marker_selection_"+second).nextAll(".marker_selections").each(function(){
                                                 var row_to_shift_tmp = parseInt($(this).attr('id').replace('marker_selection_',''),10);
                                                 actions.remove_slide_period(row_to_shift_tmp); 
                                              });

                                              //for(var i = 0; i < rows_to_shift.length; i++){
                                              for(var i = rows_to_shift.length -1; i >= 0; i--){ //Backwards insert because of scroll top animation                                                  
                                                  $(".scroll-content-item").removeClass("active");
                                                  $(".scroll-content-item img:eq("+rows_to_shift_url[i]+")").parent().addClass("active");                                                   
                                                  actions.synchronize(parseInt(rows_to_shift[i],10)+parseInt(save_shift_time_diff,10));                                         
                                              }                                             
                                              
                                              $(".scroll-content-item").removeClass("active");
                                              $(".scroll-content-item img:eq("+rows_to_shift_url[0]+")").parent().addClass("active");   
                                                                                                                                        
                                              $( this ).dialog( "close" );

                                      }
                                  },
                                  {
                                    text: language.s9, 
                                    click: function() {    $( this ).dialog( "close" );    }
                                   }                                      
                              ],
                              close: function() { $( this ).dialog( "close" );    }             
                    });

                    $( "#shift_slides" ).dialog( "open" );

                    return false;
                }
                
                //DISABLED ALL BUTTONS ON MARKER EDIT
                edit_marker_on = 1;
                var initial_state_of_save_btn = $(".save_btn").prop('disabled');
                var initial_opacity_of_save_btn = $(".save_btn").css('opacity');
                $(".save_btn").prop('disabled',true);
                $(".save_btn").css('opacity', '0.5'); 
                $("button").prop('disabled',true);
                $("button").css('opacity', '0.2'); 
                 
                $('#active_slide_time_div_'+second).prop('disabled', false);
                $('#active_slide_time_div_'+second).addClass('borderEditTime');
                $('#active_slide_time_div_'+second).focus();               

                var initial_edit_time_hms = $('#active_slide_time_div_'+second).val();
                var initial_edit_time = convert_hh_mm_ss_format(initial_edit_time_hms); 
                
                $('#active_slide_time_div_'+second).bind('keyup', function(e) {

                    if(e.keyCode==13){//When enter key in pressed
                                                
                        //When edit with no modification and then edit again it gets into function 2 times
                        var save_edit_time = $('#active_slide_time_div_'+second).val();
                        var new_edit_time_sec = convert_hh_mm_ss_format(save_edit_time); 
                        var broken_time=save_edit_time.split(':'); 

                        if(initial_edit_time != new_edit_time_sec){
                            if(save_edit_time.length == 8 && new_edit_time_sec < global_vid_duration && broken_time.length == 3){
                                 if(broken_time[0].length == 2 && broken_time[1].length == 2 && broken_time[2].length == 2 && $.isNumeric( broken_time[0] )  && $.isNumeric( broken_time[1] ) && $.isNumeric( broken_time[2] )){
                                     if(broken_time[0] < 60 && broken_time[1] < 60 && broken_time[2] < 60 ){
                                        if(cuepointsx.indexOf(new_edit_time_sec*1000) >= 0){
                                            alert(language.s5);
                                            $('#active_slide_time_div_'+second).val(initial_edit_time_hms);  
                                        }else{
                                            actions.remove_slide_period(second);
                                            actions.synchronize(new_edit_time_sec);

                                            refresh_presentation_of_synced_slides();
                                            edit_marker_on = 0;
                                            $('#active_slide_time_div_'+second).removeClass('borderEditTime');
                                            $("button").prop('disabled',false);
                                            $("button").css('opacity', '0.4'); 
                                            
                                            $(".go_to_unsynced_slide").prop('disabled', false);
                                            $(".go_to_unsynced_slide").css('opacity', '1');
                                            
                                            $(".sync_button").prop('disabled', false);
                                            $(".sync_button").css('opacity', '1');

                                            $(".sync_and_go_button").prop('disabled', false);
                                            $(".sync_and_go_button").css('opacity', '1');
                                            $(".edit_mode_btn").css('opacity', '1');
                                            $(".preview_mode_btn").css('opacity', '1');

                                            $(".save_btn").prop('disabled', false);
                                            $(".save_btn").css('opacity', '1');  
                                            $('#active_slide_time_div_'+second).unbind('keyup');
                                            return false;
                                        }
                                     }else
                                      $('#active_slide_time_div_'+second).val(initial_edit_time_hms);   
                                 }else
                                    $('#active_slide_time_div_'+second).val(initial_edit_time_hms);  
                            }else
                                $('#active_slide_time_div_'+second).val(initial_edit_time_hms);  
                        }else   
                            $('#active_slide_time_div_'+second).val(initial_edit_time_hms); 
                            
                        $('#active_slide_time_div_'+second).prop('disabled', true);
                        $('#active_slide_time_div_'+second).removeClass('borderEditTime');
                        //ENABLE AGAIN ALL BUTTONS ON MARKER EDIT
                        edit_marker_on = 0;
                        $("button").prop('disabled',false);
                        $("button").css('opacity', '0.4'); 
                        
                        $(".go_to_unsynced_slide").prop('disabled', false);
                        $(".go_to_unsynced_slide").css('opacity', '1');
                                            
                        $(".sync_button").prop('disabled', false);
                        $(".sync_button").css('opacity', '1');
                        $(".sync_and_go_button").prop('disabled', false);
                        $(".sync_and_go_button").css('opacity', '1');
                        $(".edit_mode_btn").css('opacity', '1');
                        $(".preview_mode_btn").css('opacity', '1');
                                            
                        $(".save_btn").prop('disabled',initial_state_of_save_btn);
                        $(".save_btn").css('opacity', initial_opacity_of_save_btn); 
                        
                        $('#active_slide_time_div_'+second).unbind('keyup');
                    }else if (e.keyCode == 27){ //Quit on escape
                        $('#active_slide_time_div_'+second).val(initial_edit_time_hms);  
                        
                        $('#active_slide_time_div_'+second).prop('disabled', true);
                        $('#active_slide_time_div_'+second).removeClass('borderEditTime');
                        edit_marker_on = 0;
                        $("button").prop('disabled',false);
                        $("button").css('opacity', '0.4'); 
                        
                        $(".go_to_unsynced_slide").prop('disabled', false);
                        $(".go_to_unsynced_slide").css('opacity', '1');
                                            
                        $(".sync_button").prop('disabled', false);
                        $(".sync_button").css('opacity', '1');
                        $(".sync_and_go_button").prop('disabled', false);
                        $(".sync_and_go_button").css('opacity', '1');
                        $(".edit_mode_btn").css('opacity', '1');
                        $(".preview_mode_btn").css('opacity', '1');
                                            
                        $(".save_btn").prop('disabled',initial_state_of_save_btn);
                        $(".save_btn").css('opacity', initial_opacity_of_save_btn); 
                        
                        $('#active_slide_time_div_'+second).unbind('keyup');
                    }
                });
                
        },
          remove_slide_period: function(second){
                let time_sec = second; //MG
                second *= 1000;
                cuepointsx_second_index = cuepointsx.indexOf(second);
                if(cuepointsx_second_index == -1) return false;


                cuepointsx.splice(cuepointsx_second_index, 1);              
                var url = slide_at[second];
                var index_of_slide_order = slide_order.indexOf(url);
                slide_at[second] = null;
                // MG: get cuepointx_m with startTime == second and delete it . (if eXists:: Update endTime of previous Slide to Start of Next

                 let targetCueIndex;
                 let found=false;
                  if (cuepointsx_m.cues !== undefined) {
                      for (let s = 0; s < cuepointsx_m.cues.length; s++) {
                          let mCue = cuepointsx_m.cues[s];
                          if (mCue.startTime === time_sec) {
                              found = true;
                              targetCueIndex = s;
                              break;
                          }
                      }
                  }
                 if (found) {
                     if (debug) {console.log("CUE INDEX TO REMOVE:" + targetCueIndex);}
                     let previousCue;
                     let nextCue;
                     if (targetCueIndex>0) {previousCue = cuepointsx_m.cues[targetCueIndex-1]; }
                     if (targetCueIndex<cuepointsx_m.cues.length-1) { nextCue = cuepointsx_m.cues[targetCueIndex+1]; }
                     if (previousCue !== undefined && nextCue !== undefined) {
                         previousCue.endTime = nextCue.startTime-1;
                     }
                     if (previousCue !== undefined && nextCue === undefined) {
                         previousCue.endTime = global_vid_duration;
                     }
                     if (debug) {console.log("removing Sync Cue:" + targetCueIndex + "  from curepointsx_m");}
                     cuepointsx_m.removeCue(cuepointsx_m.cues[targetCueIndex]);
                 }
                 if (debug) {
                     console.log("SLIDE CUE AFTER REMOVAL OF:" + targetCueIndex);
                     //print cuepoints_m after update
                     for (let cm = 0; cm < cuepointsx_m.cues.length; cm++) {
                         let mCue = cuepointsx_m.cues[cm];
                         console.log("cue:" + cm + "   start:" + mCue.startTime + "(" + secondsTimeSpanToHMS(mCue.startTime) + ")" +
                             "     end:" + mCue.endTime + "(" + secondsTimeSpanToHMS(mCue.endTime) + " text: " + mCue.text + " )");

                     }
                 }
                // MG end ---------------------------------------------------------------------
                if(seek_time[url] == second){
                    if(cuepointsx_second_index == cuepointsx.length)
                        seek_time[url]=null;
                    else{
                        for(i = cuepointsx_second_index; i < cuepointsx.length; i++){
                            if(url == slide_at[cuepointsx[i]]){
                                 seek_time[url] = cuepointsx[i];
                                 break;
                             }else
                                seek_time[url]=null;
                        }
                    }
                }

                removeMarkerListSelection(parseInt(second/1000,10));
                removeSlideOnTimelineBar(parseInt(second/1000,10));
                if(seek_time[url]==null)
                    $('.scroll-content-img-number:eq('+index_of_slide_order+')').removeClass("slide_synced");
                
                //If after removal same slide appears two times them merge them
                if(cuepointsx_second_index>0 && cuepointsx_second_index < cuepointsx.length){
                    if(slide_at[cuepointsx[cuepointsx_second_index]] == slide_at[cuepointsx[cuepointsx_second_index-1]]){
                        actions.remove_slide_period(parseInt(cuepointsx[cuepointsx_second_index]/1000,10));
                    }
                }
                refresh_presentation_of_synced_slides();
                $(".save_btn").prop('disabled', false);
                $(".save_btn").css('opacity', '1');  
        },
        activate_preview_mode:function(){
            actions.seek(Math.round(getVideo().currentTime)); // actions.seek(Math.round(pp.getTime()));
            getVideo().paused = false; //pp.resume();
            $(".preview_mode_btn").prop('disabled', true);
            $(".preview_mode_btn").addClass('preview_mode_on');       
            
            $(".edit_mode_btn").prop('disabled', false);
            $(".edit_mode_btn").removeClass('edit_mode_on');
            
            $(".go_to_unsynced_slide").prop('disabled', true);
            $(".go_to_unsynced_slide").css('opacity', '0.5');
            
            $(".sync_button").prop('disabled', true);
            $(".sync_button").css('opacity', '0.2');
            
            $(".sync_and_go_button").prop('disabled', true);
            $(".sync_and_go_button").css('opacity', '0.2');
            
            $(".edit_row_from_marker_list").prop('disabled', true);
            $(".edit_row_from_marker_list").css('opacity', '0.2');
            
            $(".remove_row_from_marker_list").prop('disabled', true);
            $(".remove_row_from_marker_list").css('opacity', '0.2');

            mode = "preview";

        },
        activate_edit_mode:function(){
            $(".preview_mode_btn").prop('disabled', false);
            $(".preview_mode_btn").removeClass('preview_mode_on');
            
            $(".edit_mode_btn").prop('disabled', true);
            $(".edit_mode_btn").addClass('edit_mode_on');
            
            $(".go_to_unsynced_slide").prop('disabled', false);
            $(".go_to_unsynced_slide").css('opacity', '1');

            $(".sync_button").prop('disabled', false);
            $(".sync_button").css('opacity', '1');
            
            $(".sync_and_go_button").prop('disabled', false);
            $(".sync_and_go_button").css('opacity', '1');
            
            $(".edit_row_from_marker_list").prop('disabled', false);
            $(".edit_row_from_marker_list").css('opacity', '0.4');
            
            $(".remove_row_from_marker_list").prop('disabled', false);
            $(".remove_row_from_marker_list").css('opacity', '0.4');
            
            mode = "edit";
        },
           fast_seek: function(direction){
            time_now = Math.round(getVideo().currentTime); // Math.round(pp.getTime());
            if(direction == 'fwd'){
                if(time_now+5 >= global_vid_duration)
                    getVideo().currentTime = global_vid_duration; //= pp.seek(global_vid_duration);
                else
                    getVideo().currentTime = time_now+5; //pp.seek(time_now + 5);
            }else{
                if(time_now-5 <= 0)
                    getVideo().currentTime = 0; //pp.seek(0);
                else
                    getVideo().currentTime = time_now -5; // pp.seek(time_now - 5);
            }
        },
           send_json: function(){
            var urls_array = [];
            var times_array = [];
            for(i = 0; i < slide_order.length; i++){  
                flag_slide_no_found = 0;
                for(j = 0; j < cuepointsx.length; j++){
                    cuepointsx_time_save = cuepointsx[j];
                    if(slide_order[i] == slide_at[cuepointsx_time_save]){
                        urls_array.push(slide_order[i]);
                        times_array.push(secondsTimeSpanToHMS(parseInt(cuepointsx_time_save/1000,10)));
                        flag_slide_no_found = 1;
                    }
                }
                if(flag_slide_no_found == 0){
                    urls_array.push(slide_order[i]);
                    times_array.push(-1);
                }
            }
          
            for(i = 0; i < urls_array.length; i++){
                urls_array[i] = urls_array[i].replace(basepath,'');
            }
            //Merge times for same slide
            for(i = 0; i < urls_array.length-1; i++){
                if(urls_array[i] == urls_array[i+1]){
                    times_array[i] = times_array[i]+','+times_array[i+1];
                    urls_array.splice(i+1, 1);  
                    times_array.splice(i+1, 1);  
                    i--;
                }              
            }
            
            urls_json = JSON.stringify({urls: urls_array});
            times_json = JSON.stringify({times: times_array});
            initial_duration_json = JSON.stringify({initial_duration: secondsTimeSpanToHMS(global_vid_duration)});            
            total_json = urls_json.concat(times_json);
            total_json = total_json.replace('}{',','); 
            total_json = total_json.concat(initial_duration_json);
            total_json = total_json.replace('}{',',');             
            
            if(production_mode  === 0){
                //console.log(total_json);
                $(".save_btn").prop('disabled', true);
                $(".save_btn").css('opacity', '0.5');
            }else{
                //console.log(total_json);
                $(".save_btn").prop('disabled', true);
                $(".save_btn").css('opacity', '0.5');
                $.ajax({
                    url: slides_file,
                    type:"POST", 
                    contentType: "application/json; charset=utf-8",
                    dataType : "html",
                    crossDomain : crossDomain_option,
                    data: total_json, 	//Stringified Json Object
                    //async: false,    	//Cross-domain requests and dataType: "jsonp" requests do not support synchronous operation
                    cache: false,    	//This will force requested pages not to be cached by the browser  
                    processData:false,      //To avoid making query String instead of JSON
                    success: function(res) { 
                        if(res !== "1"){    //Rest call output should be 1 if everything is OK
                            if(res === "4")    //Under real editing
                                alert(language.s14);
                            else if(res === "5")   //Under real editing approval
                                alert(language.s15);
                            else if(res === "2")   //video has been replaced while editing
                                alert(language.s11);
                            else if(res === "3")   //presentation has been replaced while editing
                                alert(language.s12);
                            else if(res === "23")  //video and presentation have been replaced while editing
                                alert(language.s13);
                            else  
                                alert(language.s6);
                            $(".save_btn").prop('disabled', false);
                            $(".save_btn").css('opacity', '1');
                        }else{
                            $(".save_btn").prop('disabled', true);
                            $(".save_btn").css('opacity', '0.5');
                        }

                    },
                    error : function() { 		 
                        alert(language.s6);
                        $(".save_btn").prop('disabled', false);
                        $(".save_btn").css('opacity', '1');
                    }
                }); 
            }
           },
         zoom_active_slide: function(){
             $('.enlarged_active_slide').html('<div class="b-close">Close<\/div>');
               let active_slide_src = $('#active_slide img').attr('src');
                $("#show_slide_id").attr("src",active_slide_src);
                $("#showslide_modal").modal("show");
                /*active_slide_src = $('#active_slide img').attr('src');
                $('.enlarged_active_slide').bPopup({
                    content:'image',
                    contentContainer:'.enlarged_active_slide',
                    loadUrl: active_slide_src,
                    closeClass: '.b-close'
                });*/
         }
    }
}

function convert_hh_mm_ss_format(input_time) {   
	var broken_time=input_time.split(':'); 
	var hours=0;
	var minutes=0;
	var seconds=0;
	if (broken_time.length === 1) seconds = parseFloat(broken_time[0]);
	if (broken_time.length === 2) {
            minutes = parseFloat(broken_time[0]); 
            seconds = parseFloat(broken_time[1]);
		//alert(broken_time[0]+" "+minutes);
	}
	if (broken_time.length === 3) {
            hours = parseFloat(broken_time[0]);
            minutes = parseFloat(broken_time[1]); 
            seconds = parseFloat(broken_time[2]);
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

function secondsTimeSpanToHM(s){
        var h = Math.floor(s/3600); //Get whole hours
        s -= h*3600;
        var m = Math.floor(s/60); //Get remaining minutes
        s -= m*60;
        return (h+'h')+(m+'m')+((s>0)?(s+'s'):''); //zero padding on minutes and seconds
    
}

function timeline_progress_bar(){

        let $timeline = $('.timeline');
        var quarter_period = parseInt(global_vid_duration/indicator_every_no_secs);
        var minute_period = parseInt(global_vid_duration/60);

        for(i=0; i<= minute_period; i++){
            left_pos = Math.round((i*60/global_vid_duration)*timeline_width);
            if(i%5)
                $('<div class="minute_period"><\/div>').css({'left': +left_pos+'px'}).appendTo($timeline);
            else
                $('<div class="five_minute_period"><\/div>').css({'left': +left_pos+'px'}).appendTo($timeline);
        }

        for(i=0; i<= quarter_period; i++){
            left_pos = Math.round((i*indicator_every_no_secs/global_vid_duration)*timeline_width);
            $('<div class="quarter_period"><\/div>').html('<div class="quarter_period_time">'+
                                                            secondsTimeSpanToHMS(i*indicator_every_no_secs)+
                                                            '<\/div>').css({'left': +
                                                            left_pos+'px'}).appendTo($timeline);
            if (i === quarter_period && (timeline_width - left_pos) < 60){
                $(".quarter_period_time").html('');
            }
        }

        $('<div class="quarter_period"><\/div>').html('<div class="quarter_period_time">'+
                                                        secondsTimeSpanToHMS(global_vid_duration)+'<\/div>').css({'left':
                                                        +(timeline_width-1)+'px'}).appendTo($timeline);
        setInterval(function() {
            if(timeline_progress_bar_update_flag === 1){
                    lock_flag = 1;
                    time_now = getVideo().currentTime; //pp.getTime();
                    position_on_timeline_bar = Math.round((time_now/global_vid_duration)*timeline_width);
                    $(".timeline_progress_position_time").html(secondsTimeSpanToHMS(Math.round(time_now)));
                    $(".timeline_progress_position").animate({'left':position_on_timeline_bar+"px"},{duration: 100, queue: false});//Run animation immediately
                    if (go_to_time_refresh === 1){
                        $(".go_to_field").val(secondsTimeSpanToHMS(Math.round(time_now)));
                    }
                    setTimeout(function(){return false;},100); //Wait until animation is completed
                    lock_flag = 0;
                }

	    }, 1000);
}

function go_to_field_func(){

    let $go_to_field =$( ".go_to_field" );

    $go_to_field.click(function() {
            go_to_time_refresh = 0;
        });

    $go_to_field.focus(function() {
            go_to_time_refresh = 0;
        });

    $go_to_field.select(function() {
            go_to_time_refresh = 0;
        });

    $go_to_field.focusout(function() {
            go_to_time_refresh = 1;
            //setTimeout(function(){return false;},100); //Wait until seek is triggered
        });

    $go_to_field.bind('keyup', function(e) {
            if(e.keyCode === 13){//When enter key in pressed
                actions.go_to_time();
                go_to_time_refresh = 1;
            }else if (e.keyCode === 27){
                go_to_time_refresh = 1;
            }else{
                go_to_time_refresh = 0;
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

$(document).ready(function(){
        webRoot = getRootWebSitePath();
        //$(".save_btn").css('background', 'url('+webRoot+'/lib/richLecture_addons/css/images_synchronizer/saveButton-'+language.selectedLang+'.png) no-repeat');

        presentation_file=webRoot + "/api/v1/resource/presentation/";
        slides_file= webRoot + "/api/v1/resource/slides/";

        timeline_width = 1210;//$(".timeline").width();//Should work by this way but unfortunately some times it takes 100% of sceen width...unfixed bug
	    info = document.getElementById("info");
	    define_actions();
        scroll();
        if(production_mode === 1){
            vid_lecture_id = window.location.search.replace( "?id=", "" );
                        
            //If clip is provided to editor redirect to whole video
            if(vid_lecture_id.indexOf('&') !== -1) {
                window.location.href = window.location.search.replace(window.location.href.substring(window.location.href.indexOf('&')), "");
            }
            presentation_file = presentation_file + vid_lecture_id;
            slides_file = slides_file + vid_lecture_id;	
        }
	    read_parameters_XML();
        
        $("#marker_list_content").mouseleave(function(){
            $(".marker_selections").removeClass("highlight_marker_row");
            $('.marked_area').width("2px");
        });
        
        go_to_field_func();
        
        $(window).bind('beforeunload', function(){
            if($(".save_btn").prop('disabled') === false)
               return language.s7;
        });
        
        $(".scroll-pane").mousewheel(function(e, delta) {
		this.scrollLeft -= (delta * total_img_width);
		e.preventDefault();
	});
});

function toggle_play() {

    if (document._video.paused === false) {
        getVideo().pause();
        $('.player_play').css('background', 'url(' + webRoot + '/lib/richLecture_addons/css/images_editor/play-large.png) no-repeat');
        if (debug) info.innerHTML = "video paused";
    }
    else {
        getVideo().play();
        $('.player_play').css('background', 'url(' + webRoot + '/lib/richLecture_addons/css/images_editor/pause-large.png) no-repeat');
        if (debug) info.innerHTML = "video resumed";
    }

}

function onSeek() {

    if(mode === "preview"){
        curr_time =    Math.round(getVideo().currentTime)*1000; //Math.round(this.getTime())*1000;
        curr_time += 1; //Go to 1 millisecond forward otherwise you will fall to a loop because of cut point -end point- and seek to the same time

        //if (debug) 	info.innerHTML = "onSeek-preview" + curr_time  ;

        total_num_of_slides = cuepointsx.length ;

        for (var i = 0; i < total_num_of_slides; i++) {
            if (curr_time >= cuepointsx[total_num_of_slides-1]){
                //slide_to_be_loaded = total_num_of_slides -1 ;
                slide_to_be_loaded = slide_order.indexOf(slide_at[cuepointsx[total_num_of_slides -1]]);
                break;
            }else if (cuepointsx[i] <= curr_time  && curr_time < cuepointsx[i+1]){
                //slide_to_be_loaded = i;
                slide_to_be_loaded = slide_order.indexOf(slide_at[cuepointsx[i]]);
                break;
            }
        }

        if (debug)
            info.innerHTML = "onSeek: " + " current time at "+ Math.round(getVideo().currentTime) + " slide " + slide_to_be_loaded +" " + slide_order[slide_to_be_loaded];

        //var url = slide[slide_to_be_loaded];
        var url = slide_order[slide_to_be_loaded];

        var wrap = $("#active_slide img");
        wrap.css("opacity",0.3);
        wrap.attr("src", url).fadeTo("normal",1);

        // slide_to_be_loaded = slide_order[url];
        // slide_to_be_loaded = slide_order.indexOf(url);
        set_active_img($(".scroll-content-item img:eq("+slide_to_be_loaded+")"));
    }
}
