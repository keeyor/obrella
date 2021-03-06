$(function () {
    'use strict';
    dashboard.wowza = dashboard.wowza || {};

    dashboard.wowza.getStreamStatistics = function (streamingServerId, streamId) {
        let   app_stats_url = dashboard.siteUrl + "/api/v1/wowza/" + streamingServerId + "/stream/" + streamId+ "/stats";
        $.ajax({
            type:        "GET",
            url: 		  app_stats_url,
            async:		  true,
            success: function(response){
                 let json_response = JSON.parse(response);
                 let $stream_conn_badge = $("#stream_conn_badge");
                 let html = json_response.totalConnections;
                 $stream_conn_badge.html(html);
            },
            error: function (jqXHR, textStatus, errorThrown)  {
                console.log("error getStreamStatistics");
            }
        });
    };
});