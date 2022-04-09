/*
$(function(){
    var A = parseInt($('#A').width(), 10),
        B = parseInt($('#B').width(), 10),
        Z = parseInt($('#Z').width(), 10),
        minw = parseInt((A + B + Z) * 10 / 100, 10),
        offset = $('#container').offset(),
        splitter = function(event, ui){
            var aw = parseInt(ui.position.left),
                bw = A + B - aw;
            //set widths and information...
            $('#A').css({width : aw}); //.children().text(aw);
            $('#B').css({width : bw}); //.children().text(bw);
        };
    $('#Z').draggable({
        axis : 'x',
        containment : [
            offset.left + minw,
            offset.top,
            offset.left + A + B - minw,
            offset.top + $('#container').height()
        ],
        drag : splitter
    });
    //information...
/!*    $('#width').text(A + B + Z);
    $('#A div').text(A);
    $('#B div').text(B);*!/
});*/
(function () {
    'use strict';

    // Does the browser actually support the video element?
    var supportsVideo = !!document.createElement('video').canPlayType;

    if (supportsVideo) {
        var video = document.getElementById('video');

// Hide the default controls
        video.controls = false;
    }
    $('.draggable').draggable();

    $('.resizable').resizable({
        aspectRatio: true,
        handles: 'ne, se, sw, nw',
        minHeight: 250
    });
   // $('.resizable').parent().rotatable();
})();