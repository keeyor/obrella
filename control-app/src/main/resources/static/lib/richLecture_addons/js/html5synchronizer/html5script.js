let myPlayer;
let tt;
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
    document._video = document.getElementById("player_synchronizer");

	const mp4 = document.getElementById("mp4");
	const parent = mp4.parentNode;

	document._video.setAttribute("poster", webRoot + '/lib/richLecture_addons/css/images_editor/pause-large.png');
	mp4.setAttribute("src", video_url);

    webm = document.getElementById("webm");

    media_properties_elts = new Array(media_properties.length);

	$('.player_play').css('background', 'url(' + webRoot + '/lib/richLecture_addons/css/images_editor/play-large.png) no-repeat');

    init_events("events", media_events);
    //init_properties("properties", media_properties, media_properties_elts);
    //init_mediatypes();
    // properties are updated even if no event was triggered
    //setInterval(update_properties, 250);
	getVideo().load();
}

function init_events(id, arrayEventDef) {

	let f;
	for (let key in arrayEventDef) {
		document._video.addEventListener(key, capture, false);
    }

/*	const tbody = document.getElementById(id);
	let i = 1;
	let tr = null;
	for (let key in arrayEventDef) {
		if (tr == null) tr    = document.createElement("tr");
		const th = document.createElement("th");
		th.textContent = key;
		const td = document.createElement("td");
		td.setAttribute("id", "e_" + key);
		td.textContent = "0";
		td.className = "false";
		tr.appendChild(th);
		tr.appendChild(td);

		if ((i++ % 5) === 0) {
		    tbody.appendChild(tr);
		    tr = null;
		}
    }
    if (tr != null) tbody.appendChild(tr);*/
}

function init_properties(id, arrayPropDef, arrayProp) {
	const tbody = document.getElementById(id);
	let i = 0;
	let tr = null;
	do {
		if (tr == null) {
			tr = document.createElement("tr");
		}
		const th = document.createElement("th");
		th.textContent = arrayPropDef[i];
		const td = document.createElement("td");
		let r;
		td.setAttribute("id", "p_" + arrayPropDef[i]);
		r = eval("document._video." + arrayPropDef[i]);
		td.textContent = r;
		if (typeof(r) !== "undefined") {
		    td.className = "true";
		} else {
		    td.className = "false";
		}
		tr.appendChild(th);
		tr.appendChild(td);
		arrayProp[i] = td;
		if ((++i % 3) === 0) {
		    tbody.appendChild(tr);
		    tr = null;
		}
    } while (i < arrayPropDef.length);
    if (tr != null) tbody.appendChild(tr);
}

function init_mediatypes() {
	let td;
	const tbody = document.getElementById("m_video");
	let i;
	let tr;
	const videoTypes = ["video/mp4", "video/webm"];
	i = 0;
    tr = document.createElement("tr");
    do {
		td = document.createElement("th");
		td.textContent = videoTypes[i];
		tr.appendChild(td);
    } while (++i < videoTypes.length);
    tbody.appendChild(tr);

    i = 0;
    tr = document.createElement("tr");

    if (!!document._video.canPlayType) {
      do {
		  td = document.createElement("td");
		  const support = document._video.canPlayType(videoTypes[i]);
		  td.textContent = '"' + support + '"';
	if (support === "maybe") {
	    td.className = "true";
	} else if (support === "") {
	    td.className = "false";
	}
	tr.appendChild(td);
      } while (++i < videoTypes.length);
      tbody.appendChild(tr);
    }
}



function capture(event) {
    media_events[event.type]++;

    //console.log("event:" + event.type);
	let curr_time;
	if (event.type === "play") {
		info.innerHTML = "player resumed";
		$('.player_play').css('background', 'url(' + webRoot + '/lib/richLecture_addons/css/images_editor/pause-large.png) no-repeat');
		draggable_bar_HTML5();
	} else if (event.type === "pause") {
		info.innerHTML = "player resumed";
		$('.player_play').css('background', 'url(' + webRoot + '/lib/richLecture_addons/css/images_editor/play-large.png) no-repeat');
	} else if (event.type === "seeked") {
		curr_time = Math.round(getVideo().currentTime) * 1000;
		curr_time += 1;
		if (debug) info.innerHTML = "onSeek-Seeked: " + curr_time;
		if (tt.activeCues[0] !== undefined && mode === "preview") {
			onSeek();
		}
		else {
			if (mode === "preview") {
				if (debug) {
					info.innerHTML = "No Active Cue Point" + "<br/><br/>";
				}
				$(".scroll-content-item").removeClass("active");
				let wrap = $("#active_slide img");
				wrap.css("opacity", 0.3);
				wrap.attr("src", webRoot + "/lib/richLecture_addons/css/images_synchronizer/blank.gif").fadeTo("normal", 1);
				$(".scroll-pane").animate({scrollTop: 0});
				$(".go_to_slide").val("00");
			}
		}
	} else if (event.type === "loadedmetadata") {

		cuepointsx_m = getVideo().addTextTrack("metadata", "English", "en");
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
			const trackIndex = getVideo().textTracks.length - 1;
			tt = getVideo().textTracks[trackIndex]
			tt.addEventListener('cuechange', function () {
				// get handle to element that wraps the image and make it semi-transparent
				if (debug) info.innerHTML = "onCuepoint: " ;
				if (tt.activeCues[0] !== undefined && mode === "preview") {
					let url = tt.activeCues[0].text
					let wrap = $("#active_slide img");
					wrap.css("opacity",0.3);
					wrap.attr("src", url).fadeTo("normal",1);
					//slide_to_be_loaded = slide_order[url];
					slide_to_be_loaded = slide_order.indexOf(url);
					set_active_img($(".scroll-content-item img:eq("+slide_to_be_loaded+")"));
				} else  if (mode === "preview" ){
					$(".scroll-content-item").removeClass("active");
					if (debug) {info.innerHTML = "No Active Cue Point" + "<br/><br/>";}
					let wrap = $("#active_slide img");
					wrap.css("opacity", 0.3);
					wrap.attr("src", webRoot + "/lib/richLecture_addons/css/images_synchronizer/blank.gif").fadeTo("normal", 1);
					$(".scroll-pane").animate({scrollTop: 0});
					$(".go_to_slide").val("00");
				}
			}); //end oncuechange
		});
		getVideo().volume = 0.5;
	} else if (event.type === "oncanplay") {
		if (debug) { info.innerHTML = "player ready";}
	}
}


function update_properties() {

    let td;
	let i = 0;
	for (let key in media_events) {
		const e = document.getElementById("e_" + key);
		if (e) {
		    e.textContent = media_events[key];
		    if (media_events[key] > 0) e.className = "true";
		}
    }
    for (let key in media_properties) {
		let val = eval("document._video." + media_properties[key]);
		media_properties_elts[i++].textContent = val;
    }
    if (document._video.audioTracks !== undefined) {
		try {
			td = document.getElementById("m_audiotracks");
			td.textContent = (document._video.audioTracks.length).toString();
		    td.className = "true";
		} catch (e) {}
	}
	if (document._video.videoTracks !== undefined) {
		try {
		    td = document.getElementById("m_videotracks");
		    td.textContent = (document._video.videoTracks.length).toString();
		    td.className = "true";
		} catch (e) {}
	}
	if (document._video.textTracks !== undefined) {
		try {
		    td = document.getElementById("m_texttracks");
		    td.textContent = (document._video.textTracks.length).toString();
		    td.className = "true";
		} catch (e) {}
    }
}


function resize() {
    document._video.width = document._video.videoWidth + 10;
    document._video.height = document._video.videoHeight + 10;
}

function getVideo() {
	return document._video;
}

function draggable_bar_HTML5() {
	$('.timeline_progress_position').draggable({
		axis: "x",
		containment: 'parent',
		start: function() {
			while (lock_flag === 1) {
				setTimeout(function() {
					return false;
				}, 100);
			}
			timeline_progress_bar_update_flag = 0;
		},
		stop: function() {
			let cursor_pos = $(this).css('left');
			cursor_pos = cursor_pos.substring(0, cursor_pos.length - 2);
			let cursor_sec = Math.round((cursor_pos / timeline_width) * global_vid_duration);
			getVideo().currentTime = cursor_sec; //pp.seek(cursor_sec);
			timeline_progress_bar_update_flag = 1;
		},
		drag: function() {
			let cursor_pos = $(this).css('left');
			cursor_pos = Math.round(cursor_pos.substring(0, cursor_pos.length - 2));
			let cursor_sec = Math.round((cursor_pos / timeline_width) * global_vid_duration);
			$(".timeline_progress_position_time").html(secondsTimeSpanToHMS(Math.round(cursor_sec)));
			$(".go_to_field").val(secondsTimeSpanToHMS(Math.round(cursor_sec)));
		}
	});
}

function timeline_progress_bar_HTML5() {

	const quarter_period = parseInt((global_vid_duration / indicator_every_no_secs).toString());
	const minute_period = parseInt((global_vid_duration / 60).toString());

	let $timeline = $('.timeline');
	let left_pos;
	for (let i = 0; i <= minute_period; i++) {
		left_pos = Math.round((i * 60 / global_vid_duration) * timeline_width);
		if (i % 5)
			$('<div class="minute_period"><\/div>').css({
				'left': +left_pos + 'px'
			}).appendTo($timeline);
		else
			$('<div class="five_minute_period"><\/div>').css({
				'left': +left_pos + 'px'
			}).appendTo($timeline);
	}
	for (let i = 0; i <= quarter_period; i++) {
		left_pos = Math.round((i * indicator_every_no_secs / global_vid_duration) * timeline_width);
		$('<div class="quarter_period"><\/div>').html('<div class="quarter_period_time">' + secondsTimeSpanToHMS(i * indicator_every_no_secs) + '<\/div>').css({
			'left': +left_pos + 'px'
		}).appendTo($timeline);
		if (i === quarter_period && (timeline_width - left_pos) < 25) {
			$(".quarter_period_time").html('');
		}
	}
	$('<div class="quarter_period"><\/div>').html('<div class="quarter_period_time">' + secondsTimeSpanToHMS(global_vid_duration) + '<\/div>').css({
		'left': +(timeline_width - 1) + 'px'
	}).appendTo($timeline);

	let lock_flag;
	let position_on_timeline_bar;

	setInterval(function() {
		if (timeline_progress_bar_update_flag === 1) {
			lock_flag = 1;
			time_now = getVideo().currentTime; //pp.getTime();
			position_on_timeline_bar = Math.round((time_now / global_vid_duration) * timeline_width);
			$(".timeline_progress_position_time").html(secondsTimeSpanToHMS(Math.round(time_now)));
			$(".timeline_progress_position").animate({
				'left': position_on_timeline_bar + "px"
			}, {
				duration: 100,
				queue: false
			});
			if (go_to_time_refresh === 1) {
				$(".go_to_field").val(secondsTimeSpanToHMS(Math.round(time_now)));
			}
			setTimeout(function () {
				return false;
			}, 100);
			lock_flag = 0;
		}
	}, 1000);
}

function toggle_mute() {
	if (getVideo().muted) {
		if (debug) info.innerHTML = "un-mute";
		$('.player_mute').css('background', 'url(' + webRoot + '/lib/richLecture_addons/css/images_editor/unmute-large.png) no-repeat')
		getVideo().muted = false;
		getVideo().volume = 0.5;
	}
	else {
		if (debug) info.innerHTML = "mute";
		$('.player_mute').css('background', 'url(' + webRoot + '/lib/richLecture_addons/css/images_editor/mute-large.png) no-repeat')
		getVideo().muted = true;
	}

}
function volume_down() {
	if  (getVideo().volume >= 0.2) {
		getVideo().volume -= 0.1;
	}
	else {
		getVideo().volume = 0;
	}
	if (debug) info.innerHTML = "volume down:" + getVideo().volume;
}
function volume_up() {
	if  (getVideo().volume <= 0.9) {
		getVideo().volume += 0.1;
	}
	else {
		getVideo().volume = 1;
	}
	if (debug) info.innerHTML = "volume up:"+ getVideo().volume;
}

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

