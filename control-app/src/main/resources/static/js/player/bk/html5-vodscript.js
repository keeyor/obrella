let playermetadataloaded = 0;
let tt;
let ts;
let debug = 1;
// the following was extracted from the spec in October 2014

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

// CODE START HERE
let webm = null;

function init_HTML5(playlist_array) {

	document._video = document.getElementById("player");

	const mp4 = document.getElementById("mp4");
	const parent = mp4.parentNode;

	const webm = document.getElementById("webm");
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

	init_events("events", media_events);
	getVideo().load();
}

function init_events(id, arrayEventDef) {

	for (let key in arrayEventDef) {
		document._video.addEventListener(key, capture, false);
	}
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
				//Check if we are in a cut space
				//cuepoints_c -> Start times of cuts
				for (let i = 0; i < cuepoints_c.length; i++) {
					if (cuepoints_c[i] <= curr_time) {
						//cut_end_in_sec -> End times of cuts
						const end_c = cut_end_in_sec[cuepoints_c[i]] * 1000;
						const diff = curr_time - end_c;
						if (diff <= 0) {
							if (debug) console.log("on Seek of end-cut current time at " + curr_time + " cut_period " + i + ":" + cuepoints_c[i] + " - " + end_c);
							if ((end_c / 1000) < global_vid_duration) {
								getVideo().currentTime = end_c / 1000.0;
							} else {
								getVideo().play();
							}
							break;
						} else {
							if (debug) console.log("no cuts:" + i + " " + cuepoints_c[i] + " " + end_c + " |curr: " + curr_time + " dif: " + diff);
						}
					}
				}
			}
	} else if (event.type === "loadedmetadata") {

		if (debug) console.log("loadedmetadata");
		$("#dvLoading").hide();
		$("#myOverlay").hide();
		onPlay_controls();
		if (!introPlaying()) {
			if (debug)  console.log("intro not playing. Add cues");

			//Note: We have to add a cue_points array to video and THEN fill it with cue points
			cuepoints_m = getVideo().addTextTrack("metadata", "CutsAndTrims", "en"); // getVideo().addTextTrack("metadata", "English", "en");
			cuepoints_m.mode = "showing";

			cuepointsx_m = getVideo().addTextTrack("metadata", "SlideSync", "en"); // getVideo().addTextTrack("metadata", "English", "en");
			cuepointsx_m.mode = "showing";

			read_cuts_cuepoints_func();
			read_slide_cuepoints_func();

			//console.log("loaded");
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
						// set active slide
						if (mode === "sync_on") {
							let jsonData = JSON.parse(ts.activeCues[0].text);
							let slideIndexToShow = parseInt(jsonData.index);
							if (debug)  console.log("cuechange: GoTo Slide:" + slideIndexToShow);
							actions.go_to_slide(slideIndexToShow);
						}
					}
				}); //end oncuechange
			});
		}

		getVideo().volume = 0.5;
		$(".show_current_vol").html(Math.round(getVideo().volume*10) + "");

		playermetadataloaded = 1;

	} else if (event.type === "oncanplay") {
		if (debug) console.log("player ready");
		//$(this).bind('contextmenu',function(e){return false;});
	}
	else if (event.type === "ended") {
		if (debug) console.log("ended");
		if (introPlaying()) {
			if (debug) console.log("define_actions");
			define_actions();
			if (debug) console.log("go_to_time_func");
			go_to_time_func();
			if (debug) console.log("go_to_slide_func");
			//go_to_slide_func();
			video_playing_index = 1;
			slides_loaded = 0;
			playermetadataloaded = 1;

			init_HTML5(playlist_array);
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

function read_slide_cuepoints_func() {

	let increment 	 = 0;
	let slideIndexes = [];

	$( ".go_to_slide" ).each(function( i ) {
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

	if (!introPlaying()) {
		for (let s = 0; s < cuepoints_x.length; s++) {
			let slide_begin = cuepoints_x[s] / 1000;
			let slide_end;
			if (s < (cuepoints_x.length - 1)) {
				slide_end = (cuepoints_x[s + 1] / 1000) - 1;
			} else {
				slide_end = global_vid_duration;
			}
			let jsonData = '{ "index" : "' + slideIndexes[s] + '"}';
			cuepointsx_m.addCue(new window.VTTCue(slide_begin, slide_end, jsonData));
		}
	}

	if(increment === 0){	// number of synced Slides
		mode = "sync_off";
		$("[name='sync_mode']").bootstrapSwitch('disabled', true);
	}
	else {
		$("[name='sync_mode']").bootstrapSwitch('disabled', false);
		$("[name='sync_mode']").bootstrapSwitch('state', true, true);
	}
 }

function read_cuts_cuepoints_func(){

	if (debug) console.log("READ CUTS");
	//trim_start
	let $trim_start = $("#trim_start");

	let end_sec = -1;
	if ($trim_start !== undefined) {
		let cvs_data  = $trim_start.data("trim");
		let data 	  = cvs_data.split("#");
		let begin_sec = data[0];
		let end_sec	  = data[1];
		begin_sec=convert_hh_mm_ss_format(begin_sec);
		end_sec=convert_hh_mm_ss_format(end_sec);
		cut_end_in_sec[begin_sec*1000]=end_sec;
		cuepoints_c.push(begin_sec*1000);
		if (!introPlaying()) {
			cuepoints_m.addCue(new window.VTTCue(begin_sec, end_sec, 'trim start'));
			if (debug) console.log("TRIM START ADD:" + begin_sec + "->" + end_sec);
		}
	}

	let $trim_finish = $("#trim_end");

	if ($trim_finish !== undefined) {
		let cvs_data = $trim_finish.data("trim");
		let data 	 = cvs_data.split("#");
		let begin_sec = data[0];
		let end_sec	  = data[1];
		begin_sec=convert_hh_mm_ss_format(begin_sec);
		end_sec=convert_hh_mm_ss_format(end_sec);
		cut_end_in_sec[begin_sec*1000]=end_sec;
		cuepoints_c.push(begin_sec*1000);
		if (!introPlaying()) {
			cuepoints_m.addCue(new window.VTTCue(begin_sec, end_sec, 'trim finish'));
			if (debug) console.log("TRIM FINISH ADD:" + begin_sec + "->" + end_sec);
		}
	}

	$( ".cut_space" ).each(function( i ) {
		let csv_data  = $(this).data("cut");
		let data  	  = csv_data.split("#");
		let begin_sec = data[0];
		begin_sec=convert_hh_mm_ss_format(begin_sec);
		end_sec = data[1];
		end_sec=convert_hh_mm_ss_format(end_sec);

		cut_end_in_sec[begin_sec*1000]=end_sec;
		cuepoints_c.push(begin_sec*1000);
		if (!introPlaying()) {
			cuepoints_m.addCue(new window.VTTCue(begin_sec, end_sec, 'new cut'));
			if (debug) console.log("CUT ADD:" + begin_sec + "->" + end_sec);
		}

	});
}

function resize() {
	document._video.width = document._video.videoWidth + 10;
	document._video.height = document._video.videoHeight + 10;
}

function getVideo() {
	return document._video;
}

function toggle_mute() {
	if (getVideo().muted) {
		if (debug) console.log("un-mute");
		$('.player_mute').html('<i class="fas fa-volume-up"></i>');
		getVideo().muted = false;
		//getVideo().volume = 0.5;
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
		//$("#myOverlay").show();
		//$('.player_play').css('background', 'url(' + webRoot + '/resources/richLecture_addons/css/images_editor/play-large.png) no-repeat');
		if (debug) console.log("video paused");

	}
	else {
		getVideo().play();
		//$("#myOverlay").hide();
		//$('.player_play').css('background', 'url(' + webRoot + '/resources/richLecture_addons/css/images_editor/pause-large.png) no-repeat');
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



