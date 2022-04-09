let playermetadataloaded = 0;
let tt;
let ts;
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

const media_properties = ["error", "src", "srcObject", "currentSrc", "crossOrigin", "networkState", "preload", "buffered", "readyState", "seeking", "currentTime", "duration",
	"paused", "defaultPlaybackRate", "playbackRate", "played", "seekable", "ended", "autoplay", "loop", "controls", "volume",
	"muted", "defaultMuted", "audioTracks", "videoTracks", "textTracks", "width", "height", "videoWidth", "videoHeight", "poster"];

// CODE START HERE

let media_properties_elts = null;
let webm = null;
let track;

function init_HTML5() {

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

	//media_properties_elts = new Array(media_properties.length);

	init_events("events", media_events);
	getVideo().load();
}

function init_events(id, arrayEventDef) {

	let f;
	for (let key in arrayEventDef) {
		document._video.addEventListener(key, capture, false);
	}
}

function capture(event) {
	media_events[event.type]++;

	//console.log("event:" + event.type);
	let curr_time;
	if (event.type === "play") {
		if (debug) console.log("player resumed");
		$('#playPauseVideoBt').html('<span class="glyphicon glyphicon-pause" aria-hidden="true"></span>');
	} else if (event.type === "pause") {
		if (debug) console.log("player paused");
		$('#playPauseVideoBt').html('<span class="glyphicon glyphicon-play" aria-hidden="true"></span>');
	} else if (event.type === "seeked") {

		if (!introPlaying()) {

			if (debug) console.log("intro not playing. Seeked");

			curr_time = Math.round(getVideo().currentTime) * 1000;
			curr_time += 1;
			//console.log("onSeek" + curr_time);
			for (let i = 0; i < cuepoints_c.length; i++) {
				if (cuepoints_c[i] <= curr_time) {
					const end_c = cut_end_in_sec[cuepoints_c[i]] * 1000;
					const diff = curr_time - end_c;
					if (diff <= 0) {
						if (debug) console.log("on Seek of end-cut current time at " + curr_time + " cut_period " + i + ":" + cuepoints_c[i] + " - " + end_c);
						if ((end_c / 1000) < global_vid_duration) {
							getVideo().currentTime = end_c / 1000.0; //$f().seek(end_c / 1000.0);
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
		$("#dvLoading").hide();
		$("#myOverlay").hide();
		if (debug) console.log("loadedmetadata");
		onPlay();
		if (!introPlaying()) {
			if (debug)  console.log("intro not playing. Add cues");

			cuepoints_m = getVideo().addTextTrack("metadata", "CutsAndTrims", "en"); // getVideo().addTextTrack("metadata", "English", "en");
			cuepoints_m.mode = "showing";
			cuepointsx_m = getVideo().addTextTrack("metadata", "SlideSync", "en"); // getVideo().addTextTrack("metadata", "English", "en");
			cuepointsx_m.mode = "showing";

			//console.log("loaded");
			getVideo().textTracks.addEventListener('addtrack', function () {
				let allTextTacks = getVideo().textTracks;
				let attLength = allTextTacks.length;
				for (let i = 0; i < attLength; i++) {
					if (allTextTacks[i].label === 'Timed Cue Point') {
						tt = allTextTacks[i];
						break;
					}
				}
				let trackIndex = 0; //getVideo().textTracks.length - 1;
				tt = getVideo().textTracks[trackIndex]
				tt.addEventListener('cuechange', function () {
					if (tt.activeCues[0] !== undefined) {
						if (debug) {
							console.log(" Seeked to:" + tt.activeCues[0].endTime);
						}
						getVideo().currentTime = tt.activeCues[0].endTime;
					} else {
						if (debug) {
							console.log("Cue point duration over");
						}
					}
				}); //end oncuechange
				trackIndex = 1; //getVideo().textTracks.length - 1;
				ts = getVideo().textTracks[trackIndex]
				ts.addEventListener('cuechange', function () {
					if (ts.activeCues[0] !== undefined) {
						if (debug) {
							console.log(" Slide Cue Point" + ts.activeCues[0].startTime);
						}
						// set active slide
						if (mode === "sync_on") {
							jsonData = JSON.parse(ts.activeCues[0].text);
							let url_seek = jsonData.url; //slide_at[cuepointsx[k]];
							let wrap = $("#active_slide img");
							if (wrap.attr("src") !== url_seek) {
								wrap.css("opacity", 0.3);
								wrap.attr("src", url_seek).fadeTo("normal", 1);
								let k = jsonData.pos;
								slide_to_be_loaded = $(".scroll-content-item").index($('#scroll-content-item-' + cuepointsx[k] / 1000));
								set_active_img($('#scroll-content-item-' + cuepointsx[k] / 1000));
							}
						}
					} else {
						if (debug) {
							console.log("Slide Cue point duration over");
						}
						$(".scroll-content-item").removeClass("active");
						let wrap = $("#active_slide img");
						wrap.css("opacity", 0.3);
						wrap.attr("src", webRoot + "/resources/richLecture_addons/css/images_player/blank.gif").fadeTo("normal", 1);
						$(".scroll-pane").animate({scrollTop: 0});
						$(".go_to_slide").val("00");
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

			if (debug) console.log("load menu events");
			load_menu_events();
			if (debug) console.log("define_actions");
			define_actions();
			if (debug) console.log("go_to_time_func");
			go_to_time_func();
			if (debug) console.log("go_to_slide_func");
			go_to_slide_func();

			video_playing_index = 1;
			slides_loaded =0;
			playermetadataloaded = 1;
			read_parameters_XML();
		}
	}
	else if (event.type === "volumechange") {
		if (debug) console.log("volumechange");
		if (getVideo().muted) {
			$('.player_mute').html('<span class="glyphicon glyphicon-volume-off" aria-hidden="true"></span>');
		}
		else {
			$('.player_mute').html('<span class="glyphicon glyphicon-volume-up" aria-hidden="true"></span>');
		}
	}
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
		if (debug) info.innerHTML = "un-mute";
		$('.player_mute').html('<span class="glyphicon glyphicon-volume-up" aria-hidden="true"></span>');
		getVideo().muted = false;
		//getVideo().volume = 0.5;
		$(".show_current_vol").html(Math.round(getVideo().volume*10));
	}
	else {
		if (debug) info.innerHTML = "mute";
		$('.player_mute').html('<span class="glyphicon glyphicon-volume-off" aria-hidden="true"></span>');
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
	if (debug) info.innerHTML = "volume down:" + getVideo().volume;
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

	if (debug) info.innerHTML = "volume up:"+ getVideo().volume;
	$('.player_mute').html('<span class="glyphicon glyphicon-volume-up" aria-hidden="true"></span>');
}

function toggle_play() {

	if (document._video.paused === false) {
		getVideo().pause();
		$("#myOverlay").show();
		$('.player_play').css('background', 'url(' + webRoot + '/resources/richLecture_addons/css/images_editor/play-large.png) no-repeat');
		if (debug) info.innerHTML = "video paused";

	}
	else {
		getVideo().play();
		$("#myOverlay").hide();
		$('.player_play').css('background', 'url(' + webRoot + '/resources/richLecture_addons/css/images_editor/pause-large.png) no-repeat');
		if (debug) info.innerHTML = "video resumed";

	}

}

function stop_play() {
	getVideo().pause();
	getVideo().currentTime = 0;
	$('#playPauseVideoBt').html('<span class="glyphicon glyphicon-play" aria-hidden="true"></span>');

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



