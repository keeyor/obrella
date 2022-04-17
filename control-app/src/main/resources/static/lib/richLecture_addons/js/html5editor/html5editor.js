
let crossDomain_option = false;
let cuepoints_c = [];
let cuepoints_m = [];
begin_cut_times = [];
end_cut_times = [];
cut_end_in_sec = [];
let provider_type = "";
let video_url = "";
let production_mode = 1;
let presentation_file = "";
let cuts_file = "";
let clips_list_file = "";
let realediting_file = "";
let realediting_status = "";
let vid_lecture_id = "";
let webRoot = "";
let info;
let actions;
let debug = 0;
let global_vid_duration = -1;
let pp;
let timeline_width = 0;
let cuts_counter = 0;
let time_now = 0;
let timeline_progress_bar_update_flag = 1;
let lock_flag = 0;
let go_to_time_refresh = 1;
let indicator_every_no_secs = 900;

function reformat_date(date_original) {
    let date_split = date_original.split("-");
    return (date_split[2] + '-' + date_split[1] + '-' + date_split[0]);
}

function read_parameters_XML() {
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
            init_HTML5();
            getVideo().load();
            timeline_progress_bar_HTML5();
            read_cuts_XML(data);
        })
}

function addToTrimsClipsSelection(begin_sec, until_video_time, next_to_div, div_order, cuts_counter_specific_index) {
    let cuts_counter_tmp = cuts_counter;
    if (cuts_counter_specific_index != null) {
        cuts_counter = cuts_counter_specific_index;
    }
    let div_txt = '<div class="trims_clips_presentation" id="trims_clips_' + begin_sec + '_' + until_video_time + '"><\/div>';
    let button_for_edit = '<button type="button" title="' + language.e1 + '" id="edit_row_from_trims_clips_presentation_' + begin_sec + '_' + until_video_time + '" class="edit_row_from_trims_clips_presentation" onclick="actions.edit_cut(' + begin_sec + ',' + until_video_time + ')"></button>';
    let button_for_play_cut = '<button type="button" title="' + language.e2 + '" id="play_row_from_trims_clips_presentation_' + begin_sec + '_' + until_video_time + '" class="play_row_from_trims_clips_presentation" onclick="actions.play_cut(' + begin_sec + ',' + until_video_time + ')"></button>';
    let button_for_check = '<button type="button" title="' + language.e3 + '" id="check_row_from_trims_clips_presentation_' + begin_sec + '_' + until_video_time + '" class="check_row_from_trims_clips_presentation" onclick="actions.seek(' + begin_sec + ')"></button>';

    let button_for_delete;
    if (begin_sec === 0) {
        button_for_delete = '<button type="button" title="' + language.e4 + '" id="remove_row_from_trims_clips_presentation_' + begin_sec + '_' + until_video_time + '" class="remove_row_from_trims_clips_presentation" onclick="actions.remove_start_video()"></button>';
    }
    else if (until_video_time === global_vid_duration) {
            button_for_delete = '<button type="button" title="' + language.e4 + '" id="remove_row_from_trims_clips_presentation_' + begin_sec + '_' + until_video_time + '"    class="remove_row_from_trims_clips_presentation" onclick="actions.remove_end_video()"></button>';
    }
    else if (until_video_time !== '999999999') {
        button_for_delete = '<button type="button" title="' + language.e4 + '" id="remove_row_from_trims_clips_presentation_' + begin_sec + '_' + until_video_time + '" class="remove_row_from_trims_clips_presentation" onclick="actions.remove_cut_period(' + cuts_counter + ')"></button>';
    } else {
        button_for_delete = '<button type="button" title="' + language.e4 + '" class="remove_row_from_trims_clips_presentation" onclick="actions.remove_incomplete_cut_period(' + cuts_counter + ')"></button>';
    }
    if (begin_sec === 0) {
        $(div_txt).html(secondsTimeSpanToHMS(begin_sec) + ' - ' + secondsTimeSpanToHMS(until_video_time) + button_for_edit + button_for_play_cut + button_for_check + button_for_delete).hide().prependTo($('.userTrimInputs')).fadeIn("slow");
    }
    else if (until_video_time === global_vid_duration) {
        $(div_txt).html(secondsTimeSpanToHMS(begin_sec) + ' - ' + secondsTimeSpanToHMS(until_video_time) + button_for_edit + button_for_play_cut + button_for_check + button_for_delete).hide().appendTo($('.userTrimInputs')).fadeIn("slow");
    }
    else if (until_video_time !== '999999999') {
        if (div_order === 'after') {
            $(div_txt).html(secondsTimeSpanToHMS(begin_sec) + ' - ' + secondsTimeSpanToHMS(until_video_time) + button_for_edit + button_for_play_cut + button_for_check + button_for_delete).hide().insertAfter($(next_to_div)).show('slow');
        } else if (div_order === 'before') {
            $(div_txt).html(secondsTimeSpanToHMS(begin_sec) + ' - ' + secondsTimeSpanToHMS(until_video_time) + button_for_edit + button_for_play_cut + button_for_check + button_for_delete).hide().insertBefore($(next_to_div)).show('slow');
        } else {
            $(div_txt).html(secondsTimeSpanToHMS(begin_sec) + ' - ' + secondsTimeSpanToHMS(until_video_time) + button_for_edit + button_for_play_cut + button_for_check + button_for_delete).hide().appendTo($('.userClipInputs')).show('slow');
        }
    } else {
        $(div_txt).html(language.e5 + secondsTimeSpanToHMS(begin_sec) + ' - ... ' + button_for_delete).hide().prependTo($('.userClipInputs')).show('slow');
    }
    cuts_counter = cuts_counter_tmp;

    let $trims_clips_ = $('#trims_clips_' + begin_sec + '_' + until_video_time);
    $trims_clips_.mouseenter(function() {
        $('#timeline_bar_' + begin_sec + '_' + until_video_time).addClass("showRegionOnTimeline");
        $(this).addClass('trims_clips_presentation_highlight');
    });
    $trims_clips_.mouseleave(function() {
        $('#timeline_bar_' + begin_sec + '_' + until_video_time).removeClass("showRegionOnTimeline");
        $(this).removeClass('trims_clips_presentation_highlight');
    });
}

function removeTrimClipSelection(begin_sec, until_video_time) {
    $('#trims_clips_' + begin_sec + '_' + until_video_time).fadeOut('slow', function() {
        $(this).remove();
    });
}

function truncate(str, maxLength, suffix) {
    if (str.length > maxLength) {
        str = str.substring(0, maxLength + 1);
        str = str.substring(0, Math.min(str.length, str.lastIndexOf(" ")));
        str = str + suffix;
    }
    return str;
}

function define_actions() {
    actions = {
        mute: function() {
            toggle_mute();
        },
        voldown: function() {
            volume_down();
        },
        volup: function() {
            volume_up();
        },
        play: function() {
            toggle_play();
        },
        seek: function(second) {
            if (second - 5 > 0)
                getVideo().currentTime = second-5;
            else
                getVideo().currentTime = second;
        },
        fast_seek: function(direction) {
            time_now = Math.round(getVideo().currentTime);
            if (direction === 'fwd') {
                if (time_now + 5 >= global_vid_duration)
                    getVideo().currentTime = global_vid_duration;
                else
                    getVideo().currentTime = time_now +5;
            } else {
                if (time_now - 5 <= 0)
                    getVideo().currentTime = 0;
                else
                    getVideo().currentTime = time_now -5;
            }
        },
        edit_cut: function(begin_sec, end_sec) {

            let $edit_selected_period =  $("#edit_selected_period");
            let initial_start_edit_time = secondsTimeSpanToHMS(begin_sec);
            let initial_end_edit_time = secondsTimeSpanToHMS(end_sec);
            $edit_selected_period.dialog({
                autoOpen: false,
                resizable: false,
                modal: true,
                draggable: false,
                position: {
                    my: "center",
                    at: "top+200",
                    of: window
                },
                buttons: [{
                    text: language.e21, //Αποθήκευση - Save Edit Button
                    click: function() {
                        let bValid = false;
                        let save_start_edit_time = $('#edit_start_time').val();
                        let save_end_edit_time = $('#edit_end_time').val();
                        let broken_start_time = save_start_edit_time.split(':');
                        let broken_end_time = save_end_edit_time.split(':');
                        if (begin_sec === convert_hh_mm_ss_format(save_start_edit_time) && end_sec === convert_hh_mm_ss_format(save_end_edit_time)) {
                            $(this).dialog("close");
                            return false;
                        }
                        if (save_start_edit_time.length === 8 && convert_hh_mm_ss_format(save_start_edit_time) < global_vid_duration && 
                            broken_start_time.length === 3 && save_end_edit_time.length === 8 && 
                            convert_hh_mm_ss_format(save_end_edit_time) <= global_vid_duration && 
                            broken_end_time.length === 3 && broken_start_time[0].length === 2 && 
                            broken_start_time[1].length === 2 && broken_start_time[2].length === 2 && 
                            $.isNumeric(broken_start_time[0]) && $.isNumeric(broken_start_time[1]) && 
                            $.isNumeric(broken_start_time[2]) && broken_end_time[0].length === 2 && 
                            broken_end_time[1].length === 2 && broken_end_time[2].length === 2 && 
                            $.isNumeric(broken_end_time[0]) && $.isNumeric(broken_end_time[1]) && 
                            $.isNumeric(broken_end_time[2]) && broken_start_time[0] < 60 && broken_start_time[1] < 60 && 
                            broken_start_time[2] < 60 && broken_end_time[0] < 60 && broken_end_time[1] < 60 && broken_end_time[2] < 60) {
                            bValid = true;
                        }
                        if (bValid) {
                            if (begin_sec === 0) {
                                bValid = actions.set_start_video(convert_hh_mm_ss_format(save_end_edit_time));
                            }
                            else if (end_sec === global_vid_duration) {
                                bValid = actions.set_end_video(convert_hh_mm_ss_format(save_start_edit_time));
                            }
                            else {
                                bValid = actions.edit_cut_times(begin_cut_times.indexOf(begin_sec), begin_sec, end_sec, convert_hh_mm_ss_format(save_start_edit_time), convert_hh_mm_ss_format(save_end_edit_time));
                            }
                            if (bValid) {
                                $(this).dialog("close");
                                getVideo().currentTime = convert_hh_mm_ss_format(save_end_edit_time);
                            }
                        } else {
                            alert(language.e6);//Ο χρόνος έναρξης και λήξης πρέπει να είναι της μορφής 'HH:MM:SS' και να μην υπερβαίνει τη διάρκεια του βίντεο !
                        }
                    }
                }, {
                    text: language.e22, //Ακύρωση
                    click: function() {
                        $(this).dialog("close");
                    }
                }],
                close: function() {
                    $(this).dialog("close");
                }
            });
            $('#edit_start_time').val(initial_start_edit_time);
            $('#edit_end_time').val(initial_end_edit_time);
            if (begin_sec === 0)
                $("#edit_start_time").prop('disabled', true);
            else
                $("#edit_start_time").prop('disabled', false);
            if (end_sec === global_vid_duration)
                $("#edit_end_time").prop('disabled', true);
            else
                $("#edit_end_time").prop('disabled', false);
            $edit_selected_period.dialog("open");
        },
        edit_cut_times: function(cuts_counter_specific_index, begin_cut_time_orig, end_cut_time_orig, begin_cut_time_curr, end_cut_time_curr) {
            if (begin_cut_time_curr === 0) {
                alert(language.e7);   // Δεν επιτρέπεται ο ορισμός ενός Cut στη αρχή του βίντεο !
                return false;
            }
            if (begin_cut_time_curr === global_vid_duration) {
                alert(language.e8);  // Δεν επιτρέπεται ο ορισμός ενός Cut στο τέλος του βίντεο !
                return false;
            }
            if (end_cut_time_curr === global_vid_duration) {
                alert(language.e9);  // H λήξη ενός Cut δεν μπορεί να συμπίπτει με το τέλος του βίντεο !
                return false;
            }
            if (end_cut_time_curr <= begin_cut_time_curr) {
                alert(language.e10); // H λήξη ενός Cut πρέπει να οριστεί μετά το χρόνο έναρξής του !
                return false;
            }

            let remove_index = [];
            for (let l = 0; l < cuepoints_c.length; l++) {
                if (l !== cuepoints_c.indexOf(begin_cut_times[cuts_counter_specific_index] * 1000)) {
                    if (cuepoints_c[l] / 1000 <= begin_cut_time_curr && cut_end_in_sec[cuepoints_c[l]] >= begin_cut_time_curr) {
                        alert(language.e11); //Η έναρξη ενός Cut δεν μπορεί να οριστεί έντος άλλου Cut !
                        return false;
                    }
                    if (cuepoints_c[l] / 1000 <= end_cut_time_curr && cut_end_in_sec[cuepoints_c[l]] >= end_cut_time_curr) {
                        alert(language.e12); //Η λήξη ενός Cut δεν μπορεί να οριστεί έντος άλλου Cut !
                        return false;
                    }
                    if (cuepoints_c[l] / 1000 >= begin_cut_time_curr && cut_end_in_sec[cuepoints_c[l]] <= end_cut_time_curr) {
                        remove_index.push(l);
                        console.log("found one to remove, with index" + l);
                    }
                }
            }
            begin_cut_times[cuts_counter_specific_index] = begin_cut_time_curr;
            cuepoints_c[cuepoints_c.indexOf(begin_cut_time_orig * 1000)] = begin_cut_time_curr * 1000;
            end_cut_times[cuts_counter_specific_index] = end_cut_time_curr;

            //print cuepoints_m before update
            for (let cm=0; cm < cuepoints_m.cues.length; cm++) {
                let mCue = cuepoints_m.cues[cm];
                console.log("cue:" + cm + "   start:"+ mCue.startTime + "  end:" + mCue.endTime);
            }

            // MODIFY CUEPOINT in cuepoints_m
            for (let cm=0; cm < cuepoints_m.cues.length; cm++) {
                let mCue = cuepoints_m.cues[cm];
                if (mCue.startTime === begin_cut_time_orig && mCue.endTime === end_cut_time_orig) {
                    console.log(" found target cue to Alter");
                    mCue.startTime = begin_cut_time_curr;
                    mCue.endTime = end_cut_time_curr;
                }
            }

            //print cuepoints_m after update
            for (let cm=0; cm < cuepoints_m.cues.length; cm++) {
                let mCue = cuepoints_m.cues[cm];
                console.log("cue:" + cm + "   start:"+ mCue.startTime + "  end:" + mCue.endTime);
            }

            cut_end_in_sec[begin_cut_time_orig * 1000] = null;
            cut_end_in_sec[begin_cut_times[cuts_counter_specific_index] * 1000] = end_cut_time_curr;

            removeCutOnTimelineBar(begin_cut_time_orig, end_cut_time_orig);
            removeTrimClipSelection(begin_cut_time_orig, end_cut_time_orig);

            let consider_no_removed_items = 0;
            let index_of_begin_cut_time;
            for (let i = 0; i < remove_index.length; i++) {
                if (cuepoints_c[remove_index[i - consider_no_removed_items]] === 0)
                    continue;
                index_of_begin_cut_time = begin_cut_times.indexOf(cuepoints_c[remove_index[i - consider_no_removed_items]] / 1000);
                removeCutOnTimelineBar(cuepoints_c[remove_index[i - consider_no_removed_items]] / 1000, cut_end_in_sec[cuepoints_c[remove_index[i - consider_no_removed_items]]]);
                removeTrimClipSelection(cuepoints_c[remove_index[i - consider_no_removed_items]] / 1000, cut_end_in_sec[cuepoints_c[remove_index[i - consider_no_removed_items]]]);
                cut_end_in_sec[cuepoints_c[remove_index[i - consider_no_removed_items]]] = null;
                begin_cut_times[index_of_begin_cut_time] = null;
                end_cut_times[index_of_begin_cut_time] = null;
                cuepoints_c.splice(remove_index[i - consider_no_removed_items], 1);
                consider_no_removed_items++;
            }
            
            addCutOnTimelineBar(begin_cut_times[cuts_counter_specific_index], end_cut_times[cuts_counter_specific_index]);
            let min_diff = 360000;
            let min_diff_index = -1;
            let cut_diff;
            let min_diff_order;
            for (let i = 0; i < begin_cut_times.length; i++) {
                cut_diff = Math.abs(begin_cut_times[cuts_counter_specific_index] - begin_cut_times[i]);
                if (cut_diff > 0 && cut_diff < min_diff && begin_cut_times[i] != null) {
                    min_diff = cut_diff;
                    min_diff_index = i;
                    if (begin_cut_times[cuts_counter_specific_index] > begin_cut_times[i])
                        min_diff_order = 'after';
                    else
                        min_diff_order = 'before';
                }
            }
            if (min_diff_index >= 0)
                addToTrimsClipsSelection(begin_cut_times[cuts_counter_specific_index], end_cut_times[cuts_counter_specific_index], '#trims_clips_' + begin_cut_times[min_diff_index] + '_' + end_cut_times[min_diff_index], min_diff_order, cuts_counter_specific_index);
            else
                addToTrimsClipsSelection(begin_cut_times[cuts_counter_specific_index], end_cut_times[cuts_counter_specific_index], null, null, cuts_counter_specific_index);

            let $send_json_button = $(".send_json_button");
            $send_json_button.prop('disabled', false);
            $send_json_button.css('opacity', '1');
            return true;
        },
        play_cut: function(begin_sec, end_sec) {

            let $play_cut_section = $('.play_cut_section');
            getVideo().paused = true;
            $play_cut_section.html('<div class="b-close">Close<\/div>');
            $play_cut_section.bPopup({
                content: 'iframe',
                iframeAttr: 'width="640px" height="480px"',
                contentContainer: '.play_cut_section',
                loadUrl: webRoot + '/admin/video-editor/editor_play_cut?id=' + video_url + '&duration=' + global_vid_duration + '&start=' + begin_sec + '&end=' + end_sec + '&provider=' + provider_type,
                //closeClass: '.b-close'
            });
        },
        go_to_time: function() {
            // NOT USED HERE in EDITOR
            let go_to_time = $(".go_to_field").val();
            let go_to_time_sec = convert_hh_mm_ss_format(go_to_time);
            let broken_time = go_to_time.split(':');
            if (go_to_time.length === 8 && go_to_time_sec < global_vid_duration && broken_time.length === 3) {
                if (broken_time[0].length === 2 && broken_time[1].length === 2 && broken_time[2].length === 2 &&
                    $.isNumeric(broken_time[0]) && $.isNumeric(broken_time[1]) && $.isNumeric(broken_time[2])) {
                    if (broken_time[0] < 60 && broken_time[1] < 60 && broken_time[2] < 60) {
                        getVideo().currentTime = go_to_time;
                    }
                }
            }
        },

        set_start_video: function(start_video_time) {
            if (start_video_time == null) {
                start_video_time = Math.round(getVideo().currentTime);
            }
            const start_trim_index = cuepoints_c.indexOf(0);

            if (start_video_time === 0) {
                alert(language.e13);
                return false;
            }
            if (start_video_time === global_vid_duration) {
                alert(language.e14);
                return false;
            }
            for (let l = 0; l < cuepoints_c.length; l++) {
                if (l === start_trim_index) {
                    if (cut_end_in_sec[0] === start_video_time) {
                        return false;
                    }
                } else {
                    if (cuepoints_c[l] / 1000 <= start_video_time) {
                        alert(language.e15);
                        return false;
                    }
                }
            }
            let $set_start_button = $(".set_start_button");
            let $send_json_button = $(".send_json_button");

            $set_start_button.prop('disabled', true);
            $set_start_button.css('opacity', '0.4');
            $send_json_button.prop('disabled', false);
            $send_json_button.css('opacity', '1');

            let begin_sec = 0;
            if (start_trim_index !== -1) {
                removeCutOnTimelineBar(cuepoints_c[start_trim_index], cut_end_in_sec[begin_sec * 1000]);
                removeTrimClipSelection(cuepoints_c[start_trim_index], cut_end_in_sec[begin_sec * 1000]);
                cut_end_in_sec[begin_sec * 1000] = start_video_time;
                cuepoints_c[start_trim_index] = begin_sec * 1000;
            } else {
                cut_end_in_sec[begin_sec * 1000] = start_video_time;
                cuepoints_c.push(begin_sec * 1000);
                if (debug) {
                    info.innerHTML = "Trim Start:: Start:" + begin_sec + "-->End:" + start_video_time;
                }
                cuepoints_m.addCue(new window.VTTCue(begin_sec, start_video_time, 'trim start'));
            }
            addCutOnTimelineBar(begin_sec, start_video_time);
            addToTrimsClipsSelection(begin_sec, start_video_time);

            $set_start_button.prop('disabled', false);
            $set_start_button.css('opacity', '1');
            return true
        },
        remove_start_video: function() {
            let start_trim_index = cuepoints_c.indexOf(0);
            if (start_trim_index !== -1) {
                removeCutOnTimelineBar(cuepoints_c[start_trim_index], cut_end_in_sec[cuepoints_c[start_trim_index] * 1000]);
                removeTrimClipSelection(cuepoints_c[start_trim_index], cut_end_in_sec[cuepoints_c[start_trim_index] * 1000]);
                cut_end_in_sec[cuepoints_c[start_trim_index] * 1000] = null;
                cuepoints_c.splice(start_trim_index, 1);

                // FINd INDEX OF CUEPOINT to delete in in cuepoints_m
                for (let cm=0; cm < cuepoints_m.cues.length; cm++) {
                    let mCue = cuepoints_m.cues[cm];
                    if (mCue.startTime === 0) {
                        m_index = cm;
                        break;
                    }
                }
                if (m_index !== -1) {
                    console.log("removing start trim with index:" + m_index + "  from curepoints_m")
                    cuepoints_m.removeCue(cuepoints_m.cues[m_index]);
                }

                let $send_json_button = $(".send_json_button");
                $send_json_button.prop('disabled', false);
                $send_json_button.css('opacity', '1');
            }
        },
        set_end_video: function(end_video_time) {
            if (end_video_time == null)
                end_video_time = Math.round(getVideo().currentTime);//Math.round(pp.getTime());
            const video_duration = global_vid_duration;
            const end_trim_cut_end_index = cut_end_in_sec.indexOf(video_duration);
            if (end_video_time === global_vid_duration) {
                alert(language.e16); //Η έναρξη του τελικού Trim δεν μπορεί να συμπίπτει με το τέλος του βίντεο !
                return false;
            }
            if (end_video_time === 0) {
                alert(language.e17); // Η έναρξη του τελικού Trim δεν μπορεί να συμπίπτει με την αρχή του βίντεο !
                return false;
            }
            if (end_trim_cut_end_index / 1000 === end_video_time)
                return false;
            for (let l = 0; l < cuepoints_c.length; l++) {
                if (l !== cuepoints_c.indexOf(end_trim_cut_end_index)) {
                    if (cuepoints_c[l] / 1000 >= end_video_time) {
                        alert(language.e18);
                        return false;
                    }
                    if (cuepoints_c[l] / 1000 <= end_video_time && cut_end_in_sec[cuepoints_c[l]] >= end_video_time) {
                        alert(language.e19);
                        return false;
                    }
                }
            }
            let $set_end_button = $(".set_end_button");
            let $send_json_button = $(".send_json_button");

            $set_end_button.prop('disabled', true);
            $set_end_button.css('opacity', '0.4');
            $send_json_button.prop('disabled', false);
            $send_json_button.css('opacity', '1');

            let begin_sec = end_video_time;
            if (end_trim_cut_end_index !== -1) {
                const end_trim_cut_begin_index = Math.round(cuepoints_c.indexOf(end_trim_cut_end_index));
                removeCutOnTimelineBar(cuepoints_c[end_trim_cut_begin_index] / 1000, cut_end_in_sec[end_trim_cut_end_index]);
                removeTrimClipSelection(cuepoints_c[end_trim_cut_begin_index] / 1000, cut_end_in_sec[end_trim_cut_end_index]);
                cut_end_in_sec[end_trim_cut_end_index] = null;
                cuepoints_c.splice(end_trim_cut_begin_index, 1);
            }
            cut_end_in_sec[begin_sec * 1000] = video_duration;
            cuepoints_c.push(begin_sec * 1000);

            if (debug) {
                info.innerHTML = "Trim End:: Start:" + (begin_sec) + ">End:" + global_vid_duration;
            }
            cuepoints_m.addCue(new window.VTTCue(begin_sec, global_vid_duration, 'trim end'));

            addCutOnTimelineBar(begin_sec, video_duration);
            addToTrimsClipsSelection(begin_sec, video_duration);

            $set_end_button.prop('disabled', false);
            $set_end_button.css('opacity', '1');
            return true;
        },
        remove_end_video: function() {
            let end_trim_cut_end_index = cut_end_in_sec.indexOf(global_vid_duration);
            if (end_trim_cut_end_index !== -1) {
                let end_trim_cut_begin_index = Math.round(cuepoints_c.indexOf(end_trim_cut_end_index));
                removeCutOnTimelineBar(cuepoints_c[end_trim_cut_begin_index] / 1000, cut_end_in_sec[end_trim_cut_end_index]);
                removeTrimClipSelection(cuepoints_c[end_trim_cut_begin_index] / 1000, cut_end_in_sec[end_trim_cut_end_index]);
                cut_end_in_sec[end_trim_cut_end_index] = null;
                cuepoints_c.splice(end_trim_cut_begin_index, 1);

                // FINd INDEX OF CUEPOINT to delete in in cuepoints_m
                for (let cm=0; cm < cuepoints_m.cues.length; cm++) {
                    let mCue = cuepoints_m.cues[cm];
                    if (mCue.endTime === global_vid_duration) {
                        m_index = cm;
                        break;
                    }
                }
                if (m_index !== -1) {
                    console.log("removing end trim with index:" + m_index + "  from curepoints_m")
                    cuepoints_m.removeCue(cuepoints_m.cues[m_index]);
                }

                let $send_json_button = $(".send_json_button");
                $send_json_button.prop('disabled', false);
                $send_json_button.css('opacity', '1');
            }
        },
        begin_cut: function() {
            let begin_cut_time_curr = Math.round(getVideo().currentTime); //Math.round(pp.getTime());
            if (debug) info.innerHTML = "Cut period " + cuts_counter + "begin: " + begin_cut_time_curr;
            if (begin_cut_time_curr === 0) {
                alert(language.e20);//Δεν επιτρέπεται ο ορισμός ενός Cut στη αρχή του βίντεο! Ορίστε το αρχικό Trim ( [ ) αντί για Cut!
                return false;
            }
            if (begin_cut_time_curr === global_vid_duration) {
                alert(language.e8); //Δεν επιτρέπεται ο ορισμός ενός Cut στο τέλος του βίντεο !
                return false;
            }
            for (let l = 0; l < cuepoints_c.length; l++) {
                if (cuepoints_c[l] / 1000 <= begin_cut_time_curr && cut_end_in_sec[cuepoints_c[l]] >= begin_cut_time_curr) {
                    alert(language.e11);//Η έναρξη ενός Cut δεν μπορεί να οριστεί έντος άλλου Cut !
                    return false;
                }
            }
            begin_cut_times[cuts_counter] = begin_cut_time_curr;

            cuepoints_c.push(begin_cut_times[cuts_counter] * 1000);

            addToTrimsClipsSelection(begin_cut_times[cuts_counter], '999999999');
            markStartOfCutOnTimeline(begin_cut_times[cuts_counter]);
            let $begin_cut_button = $(".begin_cut_button");
            $begin_cut_button.prop('disabled', true);
            $begin_cut_button.css('opacity', '0.4');

            let $end_cut_button = $(".end_cut_button");
            $end_cut_button.prop('disabled', false);
            $end_cut_button.css('opacity', '1');

            let $set_start_button = $(".set_start_button");
            $set_start_button.prop('disabled', true);
            $set_start_button.css('opacity', '0.4');

            let $set_end_button = $(".set_end_button");
            $set_end_button.prop('disabled', true);
            $set_end_button.css('opacity', '0.4');

            let $send_json_button = $(".send_json_button");
            $send_json_button.prop('disabled', true);
            $send_json_button.css('opacity', '0.5');
        },
        end_cut: function() {
            let end_cut_time_curr = Math.round(getVideo().currentTime);//Math.round(pp.getTime());

            if (debug) info.innerHTML = "Cut period " + cuts_counter + "end: " + end_cut_time_curr;

            if (end_cut_time_curr === global_vid_duration) {
                alert(language.e23); //Δεν επιτρέπεται ο ορισμός ενός Cut στο τέλος του βίντεο! Ορίστε το τελικό Trim ( ] ) αντί για Cut!
                return false;
            }
            if (end_cut_time_curr <= begin_cut_times[cuts_counter]) {
                alert(language.e10); //H λήξη ενός Cut πρέπει να οριστεί μετά το χρόνο έναρξής του !
                return false;
            }
            let remove_index = [];
            for (let l = 0; l < cuepoints_c.length; l++) {
                if (l !== cuepoints_c.indexOf(begin_cut_times[cuts_counter] * 1000)) {
                    if (cuepoints_c[l] / 1000 <= end_cut_time_curr && cut_end_in_sec[cuepoints_c[l]] >= end_cut_time_curr) {
                        alert(language.e12); //Η λήξη ενός Cut δεν μπορεί να οριστεί έντος άλλου Cut !
                        return false;
                    }
                    if (cuepoints_c[l] / 1000 >= begin_cut_times[cuts_counter] && cut_end_in_sec[cuepoints_c[l]] <= end_cut_time_curr) {
                        remove_index.push(l);
                    }
                }
            }
            let consider_no_removed_items = 0;
            for (let i = 0; i < remove_index.length; i++) {
                if (cuepoints_c[remove_index[i - consider_no_removed_items]] === 0)
                    continue;
                let index_of_begin_cut_time = begin_cut_times.indexOf(cuepoints_c[remove_index[i - consider_no_removed_items]] / 1000);
                removeCutOnTimelineBar(cuepoints_c[remove_index[i - consider_no_removed_items]] / 1000, cut_end_in_sec[cuepoints_c[remove_index[i - consider_no_removed_items]]]);
                removeTrimClipSelection(cuepoints_c[remove_index[i - consider_no_removed_items]] / 1000, cut_end_in_sec[cuepoints_c[remove_index[i - consider_no_removed_items]]]);
                cut_end_in_sec[cuepoints_c[remove_index[i - consider_no_removed_items]]] = null;
                begin_cut_times[index_of_begin_cut_time] = null;
                end_cut_times[index_of_begin_cut_time] = null;
                cuepoints_c.splice(remove_index[i - consider_no_removed_items], 1);
                consider_no_removed_items++;
            }
            end_cut_times[cuts_counter] = end_cut_time_curr;
            cut_end_in_sec[begin_cut_times[cuts_counter] * 1000] = end_cut_times[cuts_counter];

            removeMarkStartOfCutOnTimeline(begin_cut_times[cuts_counter]);
            addCutOnTimelineBar(begin_cut_times[cuts_counter], end_cut_times[cuts_counter]);
            removeTrimClipSelection(begin_cut_times[cuts_counter], '999999999');

            let min_diff = 360000;
            let min_diff_index = -1;
            let min_diff_order;
            for (let i = 0; i < begin_cut_times.length; i++) {
                let cut_diff = Math.abs(begin_cut_times[cuts_counter] - begin_cut_times[i]);
                if (cut_diff > 0 && cut_diff < min_diff && begin_cut_times[i] != null) {
                    min_diff = cut_diff;
                    min_diff_index = i;
                    if (begin_cut_times[cuts_counter] > begin_cut_times[i])
                        min_diff_order = 'after';
                    else
                        min_diff_order = 'before';
                }
            }

            if (debug) {
                info.innerHTML = "(ADD CUT) Start:" + begin_cut_times[cuts_counter] + "-> End:" + end_cut_times[cuts_counter]
            }
            cuepoints_m.addCue(new window.VTTCue(begin_cut_times[cuts_counter], end_cut_times[cuts_counter], 'new cut:' + cuts_counter));

            if (min_diff_index >= 0) {
                addToTrimsClipsSelection(begin_cut_times[cuts_counter], end_cut_times[cuts_counter], '#trims_clips_' + begin_cut_times[min_diff_index] + '_' + end_cut_times[min_diff_index], min_diff_order);
            }
            else {
                addToTrimsClipsSelection(begin_cut_times[cuts_counter], end_cut_times[cuts_counter]);
            }

            let $begin_cut_button = $(".begin_cut_button");
            $begin_cut_button.prop('disabled', false);
            $begin_cut_button.css('opacity', '1');

            let $end_cut_button = $(".end_cut_button");
            $end_cut_button.prop('disabled', true);
            $end_cut_button.css('opacity', '0.4');

            let $set_start_button = $(".set_start_button");
            $set_start_button.prop('disabled', false);
            $set_start_button.css('opacity', '1');

            let $set_end_button = $(".set_end_button");
            $set_end_button.prop('disabled', false);
            $set_end_button.css('opacity', '1');

            let $send_json_button = $(".send_json_button");
            $send_json_button.prop('disabled', false);
            $send_json_button.css('opacity', '1');
            cuts_counter++;
        },
        remove_cut_period: function(period) {
            const start_trim_index = cuepoints_c.indexOf(begin_cut_times[period] * 1000);
            if (start_trim_index !== -1 && cuepoints_c[start_trim_index] !== 0) {
                removeCutOnTimelineBar(cuepoints_c[start_trim_index] / 1000, cut_end_in_sec[cuepoints_c[start_trim_index]]);
                removeTrimClipSelection(cuepoints_c[start_trim_index] / 1000, cut_end_in_sec[cuepoints_c[start_trim_index]]);
                cut_end_in_sec[cuepoints_c[start_trim_index]] = null;
                cuepoints_c.splice(start_trim_index, 1);

                console.log("remove period:"+ period + " with start time:" + begin_cut_times[period]);

                //print cuepoints_m before update
                console.log("cue BEFORE");
                for (let cm=0; cm < cuepoints_m.cues.length; cm++) {
                    let mCue = cuepoints_m.cues[cm];
                    console.log(cm + "   start:"+ mCue.startTime + "  end:" + mCue.endTime);
                }

                let to_del_startTime = begin_cut_times[period];
                let m_index = -1;

                // FINd INDEX OF CUEPOINT to delete in in cuepoints_m
                for (let cm=0; cm < cuepoints_m.cues.length; cm++) {
                    let mCue = cuepoints_m.cues[cm];
                    if (mCue.startTime === to_del_startTime) {
                        m_index = cm;
                        break;
                    }
                }
                if (m_index !== -1) {
                    console.log("removing index:" + m_index + "  from curepoints_m")
                    cuepoints_m.removeCue(cuepoints_m.cues[m_index]);
                }

                //print cuepoints_m before update
                console.log("cue AFTER");
                for (let cm=0; cm < cuepoints_m.cues.length; cm++) {
                    let mCue = cuepoints_m.cues[cm];
                    console.log(cm + "   start:"+ mCue.startTime + "  end:" + mCue.endTime);
                }

                begin_cut_times[period] = null;
                end_cut_times[period] = null;

                let $send_json_button = $(".send_json_button");
                $send_json_button.prop('disabled', false);
                $send_json_button.css('opacity', '1');
            }
        },
        remove_incomplete_cut_period: function(period) {
            const start_trim_index = cuepoints_c.indexOf(begin_cut_times[period] * 1000);
            if (start_trim_index !== -1) {

                let $begin_cut_button = $(".begin_cut_button");
                $begin_cut_button.prop('disabled', false);
                $begin_cut_button.css('opacity', '1');

                let $end_cut_button = $(".end_cut_button");
                $end_cut_button.prop('disabled', true);
                $end_cut_button.css('opacity', '0.4');

                let $set_start_button = $(".set_start_button");
                $set_start_button.prop('disabled', false);
                $set_start_button.css('opacity', '1');

                let $set_end_button = $(".set_end_button");
                $set_end_button.prop('disabled', false);
                $set_end_button.css('opacity', '1');

                removeMarkStartOfCutOnTimeline(cuepoints_c[start_trim_index] / 1000);
                removeTrimClipSelection(cuepoints_c[start_trim_index] / 1000, '999999999');
                cuepoints_c.splice(start_trim_index, 1);
                begin_cut_times[period] = null;
            }
        },
        send_json: function() {
            let $send_json_button = $(".send_json_button");
            $send_json_button.prop('disabled', true);
            $send_json_button.css('opacity', '0.5');

            let final_trim_begin = [];
            let final_trim_end = [];
            let final_clip_begin = [];
            let final_clip_end = [];
            for (let k = 0; k < cuepoints_c.length; k++) {
                if (cuepoints_c[k] === 0) {
                    final_trim_begin.push(secondsTimeSpanToHMS(cuepoints_c[k] / 1000));
                    final_trim_end.push(secondsTimeSpanToHMS(cut_end_in_sec[cuepoints_c[k]]));
                } else if (cut_end_in_sec[cuepoints_c[k]] === global_vid_duration) {
                    final_trim_begin.push(secondsTimeSpanToHMS(cuepoints_c[k] / 1000));
                    final_trim_end.push(secondsTimeSpanToHMS(cut_end_in_sec[cuepoints_c[k]]));
                } else {
                    final_clip_begin.push(secondsTimeSpanToHMS(cuepoints_c[k] / 1000));
                    final_clip_end.push(secondsTimeSpanToHMS(cut_end_in_sec[cuepoints_c[k]]));
                }
            }
            let trim_begin_json = JSON.stringify({
                trim_begin: final_trim_begin
            });
            let trim_end_json = JSON.stringify({
                trim_end: final_trim_end
            });
            let clip_begin_json = JSON.stringify({
                clip_begin: final_clip_begin
            });
            let clip_end_json = JSON.stringify({
                clip_end: final_clip_end
            });
            let total_trims_json = trim_begin_json.concat(trim_end_json);
            let total_clips_json = clip_begin_json.concat(clip_end_json);
            let total_json = total_trims_json.concat(total_clips_json);
            let real_duration_value = [];
            real_duration_value.push(recalculate_real_duration());
            total_json = total_json.concat(JSON.stringify({
                real_duration: real_duration_value
            }));
            let initial_duration_value = [];
            initial_duration_value.push(secondsTimeSpanToHMS(global_vid_duration));
            total_json = total_json.concat(JSON.stringify({
                initial_duration: initial_duration_value
            }));
            if (production_mode === 0) {
                info.innerHTML = "JSON: " + total_json;
            } else {
                $.ajax({
                    url: cuts_file,
                    type: "POST",
                    contentType: "application/json; charset=utf-8",
                    dataType: "html",
                    crossDomain: crossDomain_option,
                    data: total_json,
                    cache: false,
                    processData: false,
                    success: function(res) {
                        if (res !== "1") {
                            if (res === "4")
                                alert(language.e29);
                            else if (res === "5")
                                alert(language.e30);
                            else if (res === "2")
                                alert(language.e28);
                            else
                                alert(language.e24);
                            $send_json_button.prop('disabled', false);
                            $send_json_button.css('opacity', '1');
                        } else {
                            $send_json_button.prop('disabled', true);
                            $send_json_button.css('opacity', '0.5');
                            alertify.notify('Επιτυχής Αποθήκευση','success');
                        }
                    },
                    error: function() {
                        alert(language.e24);
                        $send_json_button.prop('disabled', false);
                        $send_json_button.css('opacity', '1');
                    }
                });
            }
        },
        realedit: function() {
            let $send_json_button  = $(".send_json_button");
            let $realediting_btn   = $(".realediting_btn");
            let $realediting_modal = $("#realediting_modal");

            if ($send_json_button.prop('disabled') === false || (typeof $send_json_button.prop('disabled') === 'undefined')) {
                alert(language.e31)
            } else if (cuepoints_c.length === 0) {
                alert(language.e32)
            } else {
                $realediting_btn.prop('disabled', true);
                $realediting_btn.css('opacity', '0.4');
                $realediting_modal.modal('show');
            }
        }
    }
}

function postRealEditing() {

    $.ajax({
        url: realediting_file,
        type: "POST",
        contentType: "application/json; charset=utf-8",
        dataType: "html",
        cache: false,
        processData: false,
        success: function(res) {
            if (res !== "1") {
                alert(language.e34);
            } else {
                $("#playEditedModalTitle").text($("#resource_title").text());
                $("#wait_realedit_modal").modal('show');
                RealEditDaemon(vid_lecture_id);
            }
        },
        error: function() {
            alert(language.e35);
        }
    });
}

function recalculate_real_duration() {
    let real_duration = global_vid_duration;
    for (let i = 0; i < cuepoints_c.length; i++) {
        real_duration -= cut_end_in_sec[cuepoints_c[i]] - parseInt((cuepoints_c[i] / 1000).toString(), 10);
    }
    if (real_duration < 0)
        real_duration = 0;
    return secondsTimeSpanToHMS(real_duration);
}

function read_cuts_XML(data) {

            cuepoints_m = getVideo().addTextTrack("metadata", "English", "en");
            cuepoints_m.mode = "showing";

            let xml_clips;
            if (data.cuts != null && data.cuts.clips != null) {
                xml_clips = data.cuts.clips;
            }
            let xml_trims;
            if (data.cuts != null && data.cuts.trims != null) {
                xml_trims = data.cuts.trims;
            }
            if (xml_trims !== undefined) {

                    let begin_sec = xml_trims.start.begin;
                    let end_sec = xml_trims.start.end;
                    begin_sec = convert_hh_mm_ss_format(begin_sec);
                    end_sec = convert_hh_mm_ss_format(end_sec);
                    cut_end_in_sec[begin_sec * 1000] = end_sec;

                    cuepoints_c.push(begin_sec * 1000);
                    cuepoints_m.addCue(new window.VTTCue(begin_sec, end_sec, 'start'));

                    addCutOnTimelineBar(begin_sec, end_sec);
                    addToTrimsClipsSelection(begin_sec, end_sec);


                    begin_sec = xml_trims.finish.begin;
                    end_sec = xml_trims.finish.end;
                    begin_sec = convert_hh_mm_ss_format(begin_sec);
                    end_sec = convert_hh_mm_ss_format(end_sec);
                    cut_end_in_sec[begin_sec * 1000] = end_sec;

                    cuepoints_c.push(begin_sec * 1000);
                    cuepoints_m.addCue(new window.VTTCue(begin_sec, end_sec, 'finish'));

                    addCutOnTimelineBar(begin_sec, end_sec);
                    addToTrimsClipsSelection(begin_sec, end_sec);

            }
            if (xml_clips !== undefined) {
                    xml_clips.cuts.forEach(function(item) {
                        let begin_sec = item.begin;
                        let end_sec = item.end;
                        begin_sec = convert_hh_mm_ss_format(begin_sec);
                        end_sec = convert_hh_mm_ss_format(end_sec);
                        cut_end_in_sec[begin_sec * 1000] = end_sec;

                        cuepoints_c.push(begin_sec * 1000);
                        cuepoints_m.addCue(new window.VTTCue(begin_sec, end_sec, 'cut no.' + cuts_counter));

                        begin_cut_times[cuts_counter] = begin_sec;
                        end_cut_times[cuts_counter] = end_sec;
                        addCutOnTimelineBar(begin_cut_times[cuts_counter], end_cut_times[cuts_counter]);
                        let min_diff = 360000;
                        let min_diff_index = -1;
                        let min_diff_order;
                        for (let i = 0; i < begin_cut_times.length; i++) {
                            let cut_diff = Math.abs(begin_cut_times[cuts_counter] - begin_cut_times[i]);
                            if (cut_diff > 0 && cut_diff < min_diff) {
                                min_diff = cut_diff;
                                min_diff_index = i;
                                if (begin_cut_times[cuts_counter] > begin_cut_times[i])
                                    min_diff_order = 'after';
                                else
                                    min_diff_order = 'before';
                            }
                        }
                        if (min_diff_index >= 0) {
                            addToTrimsClipsSelection(begin_cut_times[cuts_counter], end_cut_times[cuts_counter], '#trims_clips_' + begin_cut_times[min_diff_index] +
                                                     '_' + end_cut_times[min_diff_index], min_diff_order);
                        }
                       else {
                            addToTrimsClipsSelection(begin_cut_times[cuts_counter], end_cut_times[cuts_counter]);
                        }
                        cuts_counter++;
                    });
            }
}

function go_to_field_func() {

    let $go_to_field = $(".go_to_field");

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
    });

    $go_to_field.bind('keyup', function(e) {
        if (e.keyCode === 13) {
            actions.go_to_time();
            go_to_time_refresh = 1;
        } else if (e.keyCode === 27) {
            go_to_time_refresh = 1;
        } else {
            go_to_time_refresh = 0;
        }
    });
}

function addCutOnTimelineBar(begin_sec, until_video_time) {
    let timeline_bar_start = Math.round(((begin_sec) / global_vid_duration) * timeline_width);
    let timeline_bar_width = Math.round(((until_video_time - begin_sec) / global_vid_duration) * timeline_width);
    if (begin_sec - 5 > 0)
        $('<div class="marked_area" id="timeline_bar_' + begin_sec + '_' + until_video_time + '" title="' + secondsTimeSpanToHMS(begin_sec) + ' - ' + secondsTimeSpanToHMS(until_video_time) + '"><\/div>').animate({
            'left': timeline_bar_start + "px",
            'width': timeline_bar_width + "px"
        }).appendTo($('.timeline')).click(function() {
            getVideo().currentTime = begin_sec -5; //$f().seek(begin_sec - 5);
        }).tooltip({
            position: 'top center',
            effect: 'slide'
        });
    else
        $('<div class="marked_area" id="timeline_bar_' + begin_sec + '_' + until_video_time + '" title="' + secondsTimeSpanToHMS(begin_sec) + ' - ' + secondsTimeSpanToHMS(until_video_time) + '"><\/div>').animate({
            'left': timeline_bar_start + "px",
            'width': timeline_bar_width + "px"
        }).appendTo($('.timeline')).click(function() {
            getVideo().currentTime = begin_sec; //$f().seek(begin_sec - 5);
        }).tooltip({
            position: 'top center',
            effect: 'slide'
        });

    let $timeline_bar_ = $('#timeline_bar_' + begin_sec + '_' + until_video_time);
    $timeline_bar_.mouseenter(function() {
        $(this).addClass("showRegionOnTimeline");
    });
    $timeline_bar_.mouseleave(function() {
        $(this).removeClass("showRegionOnTimeline");
    });
}

function markStartOfCutOnTimeline(begin_sec) {
    let timeline_bar_start = Math.round(((begin_sec) / global_vid_duration) * timeline_width);
    $('<div class="marked_area_position_cut" id="timeline_bar_' + begin_sec + '"><\/div>').html('<div class="timeline_start_cut_position">' + language.e26 + '<\/div>').animate({
        'left': timeline_bar_start + "px"
    }).appendTo($('.timeline'));
}

function removeMarkStartOfCutOnTimeline(begin_sec) {
    $('#timeline_bar_' + begin_sec).hide('slow', function() {
        $(this).remove();
    });
}

function removeCutOnTimelineBar(begin_sec, until_video_time) {
    $('#timeline_bar_' + begin_sec + '_' + until_video_time).hide('slow', function() {
        $(this).remove();
    });
}

function convert_hh_mm_ss_format(input_time) {
    let _input_time;
    const ms_time = input_time.split('.');
    if (ms_time.length === 1) _input_time = input_time;
    else _input_time = ms_time[0];
    const broken_time = _input_time.split(':');
    let hours = 0;
    let minutes = 0;
    let seconds = 0;
    if (broken_time.length === 1) seconds = parseFloat(broken_time[0]);
    if (broken_time.length === 2) {
        minutes = parseFloat(broken_time[0]);
        seconds = parseFloat(broken_time[1]);
    }
    if (broken_time.length === 3) {
        hours = parseFloat(broken_time[0]);
        minutes = parseFloat(broken_time[1]);
        seconds = parseFloat(broken_time[2]);
    }
    return Math.round(seconds + 60 * minutes + 3600 * hours);
}

function secondsTimeSpanToHMS(s) {
    let h = Math.floor(s / 3600);
    s -= h * 3600;
    let m = Math.floor(s / 60);
    s -= m * 60;
    return (h < 10 ? '0' + h : h) + ":" + (m < 10 ? '0' + m : m) + ":" + (s < 10 ? '0' + s : s);
}

function getRootWebSitePath() {
    const _location = document.location.toString();
    const applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
    const applicationName = _location.substring(0, applicationNameIndex) + '/';
    const webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

    return _location.substring(0, webFolderIndex);
}



$(document).ready(function() {
    webRoot = getRootWebSitePath();

    presentation_file = webRoot + "/api/v1/resource/presentation/";
    cuts_file = webRoot + "/api/v1/resource/cuts/";
    clips_list_file = webRoot + "/api/v1/resource/cliplist/";
    realediting_file = webRoot + "/api/v1/realediting/do/";
    realediting_status = webRoot + "/api/v1/realediting/status";

    timeline_width = $(".timeline").width();
    info = document.getElementById("info");
    define_actions();
    if (production_mode === 1) {
        vid_lecture_id = window.location.search.replace("?id=", "");
        if (vid_lecture_id.indexOf('&') !== -1) {
            window.location.href = window.location.search.replace(window.location.href.substring(window.location.href.indexOf('&')), "");
        }
        presentation_file = presentation_file + vid_lecture_id;
        cuts_file = cuts_file + vid_lecture_id;
        clips_list_file = clips_list_file + vid_lecture_id;
        realediting_file = realediting_file + vid_lecture_id;
        realediting_status = realediting_status + vid_lecture_id;
    }
    read_parameters_XML();
    go_to_field_func();

    let $go_to_field = $(".go_to_field");
    $go_to_field.focus(function() {
        go_to_time_refresh = 0;
    });
    $go_to_field.focusout(function() {
        go_to_time_refresh = 1;
        setTimeout(function() {
            return false;
        }, 100);
    });

    $(window).bind('beforeunload', function() {
        if ($(".send_json_button").prop('disabled') === false)
            return language.e27;
    });

    $('#realediting_modal').on('show.coreui.modal', function (e) {
        $("#start_real_edit_bt").text(language.e33);
    });
    $('#realediting_modal').on('hide.coreui.modal', function (e) {
        let $realediting_btn = $(".realediting_btn");
        $realediting_btn.prop('disabled', false);
        $realediting_btn.css('opacity', '1');
    });
    $("#start_real_edit_bt").on('click', function() {
        $("#realediting_modal").modal("hide");
        postRealEditing();
    });

    alertify.defaults.transition = "slide";
    alertify.defaults.theme.ok = "btn btn-primary";
    alertify.defaults.theme.cancel = "btn btn-danger";
    alertify.defaults.theme.input = "form-control";
});