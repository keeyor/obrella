(function () {
    'use strict';

    dashboard.sevents = dashboard.sevents || {};

    let ScheduledEventsDT   = null;


    let $scheduledEventsDtElem;

    dashboard.sevents.init = function () {

        DisplayEventsDataTable();

    };

    function DisplayEventsDataTable() {
        let url = dashboard.siteUrl + '/api/v1/dt/sevents.web/authorized/content/';
        $scheduledEventsDtElem = $("#seventsDataTable");
        ScheduledEventsDT = $scheduledEventsDtElem.DataTable({
            "ajax":  url,
            "columns": [
                {"data": "isActive"}, //0
                {"data": "startDate"}, //1
                {"data": "title"}, //2
                {"data": "type"}, //3
                {"data": "responsibleUnit"}, //4
                {"data": "responsiblePerson"}, //5
                {"data": "place"}, //6
                {"data": "endDate"}, //7
                {"data": "dateModified"},//8
                {"data": "editor"}, //9
                {"data": "id"}, // 10
            ],
            "language":  dtLanguageGr,
            order : [[1, 'desc']],
            "pagingType": "full_numbers",
            "pageLength": 50,
/*            dom: "<'row'<'col-sm-3'l><'col-sm-6'p><'col-sm-3'f>>" +
                "<'row'<'col-sm-12'tr>>" +
                "<'row'<'col-sm-12 col-md-5'i><'col-sm-12 col-md-7'p>>",*/
            "aoColumnDefs": [
                {
                    "aTargets": [3,5,6,7,8],
                    "visible" : false
                },
                {
                    "aTargets": [0],
                    "mData": "isActive",
                    "className": "border_left",
                    "mRender": function (data) {
                        let ret = "";
                        if (data) {
                            ret += '<span style="display:none;' + '">1</span>' + "<i style='color:green' class=\"fas fa-circle\" title=\"Ενεργή Εκδήλωση\"></i>";
                        }
                        else {
                            ret += '<span style="display:none;' + '">0</span>' + "<i style='color:red' class=\"fas fa-circle\"  title=\"Ανενεργή Εκδήλωση\"></i>";
                        }
                        return ret
                    }
                },
                {
                    "aTargets": [1],
                    "className": "border_left",
                    "mData": "startDate",
                    "mRender": function (data,type, row) {
                        let ret = "";
                        if (data !== undefined && data !== null && data !== "") {
                            let epochDate = data;
                            let formatted_date = moment.unix(epochDate).format('YYYY-MM-DD');
                            let view_date = moment.unix(epochDate).format('DD MMM YYYY');
                            ret += '<span style="display:none;' + '">' + formatted_date + '</span><b>' + view_date + '</b>';

                            if (row.endDate !== undefined && row.endDate !== null && row.endDate !== "" && row.endDate !== data) {
                                let epochEndDate = row.endDate;
                                let view_endDate = moment.unix(epochEndDate).format('DD MMM YYYY');
                                ret += ' έως '+ view_endDate;
                            }
                        }
                        else {
                            let formatted_date = moment('1900-01-01').format('YYYY-MM-DD');
                            ret+= '<span style="display:none;' + '">' + formatted_date + '</span>' + 'μή έγκυρη ημερ.';
                        }
                        return ret;
                    }
                },
                {
                    "aTargets": [2],
                    "className": "border_left",
                    "mRender": function (data, type, row) {
                        let ret = "<div class=\"row my-0\">";
                        ret +=  '<div class="col-12">' +
                            '<span style="font-weight: bolder"></span>' +
                            '<strong><span style="color:#006A9B">' + row.title + '</span></strong>' +
                            '</div>';

                        if (row.responsiblePerson !== null) {
                            let rPers = "<div class='col-12 text-muted'>";
                            rPers +=  row.responsiblePerson.name + ", " + row.responsiblePerson.affiliation;
                            rPers += "</div>";
                            ret += rPers;
                        }
                        ret += "</div>";
                        return ret;
                    }
                },
                {
                    "aTargets": [4],
                    "className": "border_left",
                    "mData": "responsibleUnit",
                    "mRender": function (data) {
                        let ret = "";
                        if (data !== null) {
                            let runits = "";
                            data.forEach(function(runit,index) {
                                if (index > 0) {
                                    runits += "<br/>";
                                }
                                if (runit.structureType === "DEPARTMENT") {
                                    runits += "Τμήμα " + runit.title;
                                }
                                else if (runit.structureType === "SCHOOL") {
                                    runits += "Σχολή " + runit.title;
                                }
                                else {
                                    runits += runit.title;
                                }
                            });
                            runits += "";
                            ret += runits;
                        }
                        return ret;
                    }
                },
                {
                    "aTargets": [9],
                    "mData": "editor",
                    "className": "border_left",
                    "sortable": false,
                    "render": function (data) {
                        let editor_name = data.name.split(" ");
                        let badge  = '<span style="display:none;' + '">' + data + '</span>' // for sorting
                        badge += '<span title="' + editor_name + '" class="badge rounded-pill bg-warning">' + editor_name[0].charAt(0);
                        if (editor_name.length>1) {
                            badge += editor_name[1].charAt(0)
                        }
                        badge += '</span>';
                        return badge;
                    }
                },
                {
                    "aTargets": [10],
                    "mData": "id",
                    "className": "border_left",
                    "sortable": false,
                    "mRender": function (data) {
                        //return '<button type="button" class="btn" style="background-color:white;color: #006A9B;vertical-align: center!important;"><i class="fas fa-edit"></i></button>';
                        return '<a role="button" class="btn btn-sm blue-btn-wcag-bgnd-color text-white" href="sevent-editor?id=' + data + '"><i class="fas fa-edit"></i> Edit</a>';
                    }
                }
            ],
            "initComplete": set_display_results,
        });
    }
    function set_display_results() {
       // $("#count_results").html("" + ScheduledEventsDT.rows().count() + "");
        var info = ScheduledEventsDT.page.info();
        $("#count_results").html('Εμφάνιση '+(info.start +1 )+ ' - '+ info.end + ' από ' + info.recordsTotal);
    }

    dashboard.sevents.loadEventsByReport = function () {


        let url = dashboard.siteUrl + '/api/v1/getEventsOfReport';

        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.data, function (index, element) {
                    let eventDtId = element;
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("e", eventDtId);
                    queryParams.delete("skip");
                    let html = '<li class="list-group-item">' +
                        '<a class="text-dark text-decoration-none" href="search?' + queryParams + '">' + element.title +' (' + element.counter + ')</a>' +
                        '</li>';
                    $("#evFilters").append(html);
                });
                if (data.data.length < 1) {
                    $("#evFilters").hide();
                    $("#eventCanvasLink").hide();
                }
                else {
                    $("#eventCanvasLink").show();
                    $("#no_dyna_filters").hide();
                }
            }
        });
    }

    dashboard.sevents.loadAreasByReport = function () {


        let url = dashboard.siteUrl + '/api/v1/getAreasOfReport';

        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.data, function (index, element) {
                    let eventDtId = element.id;
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("ea", eventDtId);
                    queryParams.delete("skip");
                    let html = '<li class="list-group-item">' +
                        '<a class="text-dark text-decoration-none" href="sevents?' + queryParams + '">' + element.text +' (' + element.counter + ')</a>' +
                        '</li>';
                    $("#areaFilters").append(html);
                });
                if (data.data.length < 1) {
                    $("#areaFilters").hide();
                    $("#areaCanvasLink").hide();
                }
                else {
                    $("#areaCanvasLink").show();
                    $("#no_dyna_filters").hide();
                }
            }
        });
    }

    dashboard.sevents.loadTypesByReport = function () {


        let url = dashboard.siteUrl + '/api/v1/getTypesOfReport';

        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.data, function (index, element) {
                    let eventDtId = element.id;
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("et", eventDtId);
                    queryParams.delete("skip");
                    let html = '<li class="list-group-item">' +
                        '<a class="text-dark text-decoration-none" href="sevents?' + queryParams + '">' + element.text +' (' + element.counter + ')</a>' +
                    '</li>';
                    $("#typeFilters").append(html);
                });
                if (data.data.length < 1) {
                    $("#typeFilters").hide();
                    $("#typeCanvasLink").hide();
                }
                else {
                    $("#typeCanvasLink").show();
                    $("#no_dyna_filters").hide();
                }
            }
        });
    }

})();