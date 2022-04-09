(function () {
    'use strict';

    dashboard.wowza = dashboard.wowza || {};
    let stream_stats_array = [];

    dashboard.wowza.loadStatistics = function() {
        let number_of_live_lectures = parseInt($("#nof-lectures").val());
        let number_of_live_events = parseInt($("#nof-events").val());
        $("#overall_live").text(number_of_live_events + number_of_live_lectures);

        let $stream_data = document.getElementsByClassName("st-data");
        let streams_length = $stream_data.length;
        $("#streams_panel").html("");
        stream_stats_array.splice(0,stream_stats_array.length); // !Important reset

       // $stream_data.each(function (index, element) {
        Array.prototype.forEach.call($stream_data, function(element) {
            let st_server       =   $( element ).data("ss");
            let st_stream       =   $( element ).data("st") + ".stream";
            let st_title        =   $( element ).data("title");
            let st_did          =   $( element ).data("did");
            let st_dt           =   $( element ).data("dt");
            let st_sn           =   $( element ).data("sn");
            let streamInfo = {
                server: st_server,
                stream: st_stream,
                title: st_title,
                departmentId: st_did,
                departmentTitle: st_dt,
                supervisorName: st_sn
            }
            dashboard.wowza.getStreamStatistics(streamInfo,streams_length);
        });
        return streams_length;
    }

    dashboard.wowza.getStreamStatistics = function (streamInfo,streams_length) {

        let app_stats_url = dashboard.siteUrl + "/api/v1/wowza/" + streamInfo.server + "/stream/" + streamInfo.stream + "/stats";
        $.ajax({
            type:        "GET",
            url: 		  app_stats_url,
            async:		  true,
            success: function(response) {
                 let json_response = JSON.parse(response);
                 let title = streamInfo.title;
                 if (streamInfo.supervisorName !== "") {
                    title += ' (' + streamInfo.supervisorName + ')';
                 }
                //print Stream sessions
                let streamConnections  = json_response.totalConnections;
                let html = '<dt class="col-9 mb-2 font-weight-normal">' + title + '</dt>' +
                           '<dd class="col-3 text-right"><span class="badge badge-secondary badge-pill">' + streamConnections + '</span></dd>';
                $("#streams_panel").append(html);
                let stream_stat = {
                     id : streamInfo.departmentId,
                     title: streamInfo.departmentTitle,
                     users: streamConnections
                 }
                 stream_stats_array.push(stream_stat);
                 if (stream_stats_array.length === streams_length) {
                     calculate_stats_foreach_department(stream_stats_array);
                 }
            },
            error: function ()  {
                console.log("error getStreamStatistics");
            }
        });
    };

    function calculate_stats_foreach_department(stream_stats_array) {
        let totalConnections = 0;
        //deps stats
        let departments_stats = [];
        stream_stats_array.forEach(function(stream_stat) {
            let found = false;
            for(let i = 0; i < departments_stats.length; i++) {
                if (departments_stats[i].id === stream_stat.id) {
                    found = true;
                    departments_stats[i].users = parseInt(departments_stats[i].users) + parseInt(stream_stat.users);
                    totalConnections = totalConnections +  parseInt(stream_stat.users);
                    break;
                }
            }
            if (found === false) {
                    let new_dep_stat = {
                        id: stream_stat.id,
                        title: stream_stat.title,
                        users: parseInt(stream_stat.users)
                    }
                    totalConnections = totalConnections + parseInt(stream_stat.users);
                    departments_stats.push(new_dep_stat);
            }
        });
        let $departments_info_panel = $("#departments_panel");
        $departments_info_panel.html("");
        let html = "";
        departments_stats.forEach(function(dep_stat) {
            html += '<dt class="col-9 font-weight-normal">' + dep_stat.title + '</dt>' +
                    '<dd class="col-3 text-right"><span class="badge badge-secondary badge-pill">' + dep_stat.users + '</span></dd>';
        });
        $departments_info_panel.append(html);
        $("#totalConnections").text(totalConnections);
    }

/*

    dashboard.wowza.getAppLiveConnections = function (ssId) {

        let   app_stats_url = dashboard.siteUrl + "/api/v1/wowza/" + ssId + "/stats";
        $.ajax({
            type:        "GET",
            url: 		  app_stats_url,
            async:		  true,
            success: function(response){
                if (response !== "-") {
                    let json_response = JSON.parse(response);
                    console.log(json_response.totalConnections);
                    let totalConnections = parseInt($("#totalConnections").val());
                    let curr_connections = parseInt(json_response.totalConnections);
                    console.log("Adding: " + curr_connections + " connections...")
                    totalConnections = totalConnections + curr_connections;
                    $("#totalConnections").text(json_response.totalConnections);
                }
                else {
                    console.log("could not contact:" + ssId);
                }
            },
            error: function (jqXHR, textStatus, errorThrown)  {
                console.log("error:" + ssId);
            }
        });
    };
*/

})();