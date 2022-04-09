(function () {
    'use strict';

    window.dashboard = window.dashboard || {};
    dashboard.broker = $({});

    dashboard.siteUrl   = "";
    dashboard.app_path  = "";

    let status = "Pending";
    let time_elapsed = -1;
    let queryString;

    let courseId        = $("#courseId").val();
    let eventId         = $("#eventId").val();
    let staffId         = $("#staffId").val();
    let departmentId    = $("#departmentId").val();
    let accessPolicy    = $("#accessPolicy").val();
    let searchText      = $("#search-box").val();
    let tag             = $("#tag").val();

    let ajaxRefreshInterval;

    dashboard.init = function () {

        dashboard.siteUrl = dashboard.broker.getRootSitePath();
        dashboard.departments.init();
        dashboard.sevents.init();

        let today = moment().format("LL");
        $("#daily_date_now").html(today);

        queryString = $("#queryString").val();
            setOverallEnums();
            setFilterRemoveLinks();
            setTagLinks();
            ajaxRefreshInterval = setInterval( function () {
                    getReportQueryStatus();
                    //console.log(status);
                    if (status === 'Finished') {
                        getReportQueryTime();
                        clearInterval(ajaxRefreshInterval);
                        if (courseId === '') {
                            dashboard.course.loadCourseByReport();
                            $("#courses_filter_row").show();
                        }
                        else {
                            $("#courses_filter_row").hide();
                            $("#table-typefilter").hide();
                        }
                        if (staffId === '') {
                            dashboard.staffmembers.loadStaffByReport();
                             $("#staff_filters_row").show();
                        }
                        if (eventId === '' && courseId === '') {
                             dashboard.sevents.loadEventsByReport();
                             $("#events_filter_row").show();
                        }
                        else {
                            $("#courses_filter_row").hide();
                            $("#table-typefilter").hide();
                        }
                        if (departmentId === '') {
                             dashboard.departments.loadDepartmentsByReport();
                             $("#departments_filter_row").show();
                        }
                    }
            }, 1000 );

        function setOverallEnums() {

        }
        function getReportQueryStatus() {
            $.ajax({
                url: dashboard.siteUrl + '/api/v1/queryReportStatus',
                cache: false
            })
                .done(function (data) {
                    status = data;
                });
        }
        function getReportQueryTime() {
            $.ajax({
                url: dashboard.siteUrl + '/api/v1/queryReportTime',
                cache: false
            })
                .done(function (data) {
                    time_elapsed = data;
                    $("#QueryReportStatus").html(" (" + time_elapsed + " ms)");
                });
        }

        function setFilterRemoveLinks() {

            let courseFilterId = $("#courseFilterId").val();
            let staffFilterId = $("#staffMemberFilterId").val();
            let departmentFilterId = $("#departmentFilterId").val();
            let eventFilterId = $("#scheduledEventFilterId").val();
            let categoryCode = $("#categoryCode").val();
            let resourceType = $("#resourceType").val();
            let accessPolicy = $("#accessPolicy").val();

            let queryString = $("#queryString").val();
            if (queryString !== "" && queryString !== undefined) {
                let restParams = new URLSearchParams(window.location.search);
                //hide clear-filters if only sort, direction params remain
                restParams.delete("direction");
                restParams.delete("sort");
                if (restParams.toString() === "") {
                    $("#clear-all-filters").hide();
                } else {
                    $("#clear-all-filters").attr('href','daily');
                    $("#clear-all-filters").show();
                }
            }
            else {
                $("#clear-all-filters").hide();
            }

            if (
                (departmentFilterId !== '' && courseFilterId !== '' && staffFilterId !=='' && accessPolicy !== '') ||
                (departmentFilterId !== '' && eventFilterId !== '' && staffFilterId !==''  && accessPolicy !== '') ||
                (categoryCode !== '' && departmentFilterId !== '' && eventFilterId !== '' && staffFilterId !==''   && accessPolicy !== '') ||
                (categoryCode !== '' && departmentFilterId !== '' && courseFilterId !== '' && staffFilterId !==''  && accessPolicy !== '')
            ) {

            }

            let courseFilterText = $("#courseFilterText").val();
            if (courseFilterId !== undefined && courseFilterId != null && courseFilterId  !== '') {
                $('#course-dd-header').html("<span class='far fa-times-circle'></span> Μάθημα: " + courseFilterText);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("c");
                queryParams.delete("skip");
                removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
                $("#clear-co-filter").attr('href','daily?' + queryParams);
                $("#course-filter").show();
            }


            let staffFilterText = $("#staffMemberFilterText").val();
            if (staffFilterId !== undefined && staffFilterId != null && staffFilterId !== '') {
                $('#staff-dd-header').html("<span class='far fa-times-circle'></span> Καθηγητής: " + staffFilterText);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("s");
                queryParams.delete("skip");
                removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
                $("#clear-sm-filter").attr('href','daily?' + queryParams);
                $("#staff-filter").show();
            }

            let departmentFilterText = $("#departmentFilterText").val();
            if (departmentFilterId !== undefined && departmentFilterId != null && departmentFilterId !== '') {
                $('#department-dd-header').html("<span class='far fa-times-circle'></span> Τμήμα: " + departmentFilterText);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("d");
                queryParams.delete("skip");
                removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
                $("#clear-dt-filter").attr('href','daily?' + queryParams);
                $("#department-filter").show();
            }

            let eventFilterText = $("#scheduledEventFilterText").val();
            if (eventFilterId !== undefined && eventFilterId != null && eventFilterId !== '') {
                $('#events-dd-header').html("<span class='far fa-times-circle'></span> Εκδήλωση: " + eventFilterText);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("e");
                queryParams.delete("skip");
                removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
                $("#clear-ev-filter").attr('href','daily?' + queryParams);
                $("#events-filter").show();
            }

            let categoryFilterText = $("#categoryTitle").val();
            if (categoryCode !== undefined && categoryCode != null && categoryCode !== '') {
                $('#category-dd-header').html("<b class='mr-2'>Κατηγορία:</b>" + categoryFilterText);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("ca");
                queryParams.delete("skip");
                removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
                $("#clear-ca-filter").attr('href','daily?' + queryParams);
                $("#category-filter").show();
            }
            //ResourceType
            let resourceTypeTitle;
            if (resourceType !== undefined && resourceType != null && resourceType !== '') {
                if (resourceType === 'c') {resourceTypeTitle = 'Διάλεξη'}
                else if (resourceType === 'e') { resourceTypeTitle = 'Εκδήλωση'}
                $('#rt-dd-header').html("<b class='mr-2'>Τύπος:</b>" + resourceTypeTitle);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("rt");
                queryParams.delete("skip");
                removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
                $("#clear-rt-filter").attr('href','daily?' + queryParams);
                $("#resourceType-filter").show();
            }
            //AccessPolicy
            let accessPolicyTitle;
            if (accessPolicy !== undefined && accessPolicy != null && accessPolicy !== '') {
                if (accessPolicy === 'private') {accessPolicyTitle = 'Ιδιωτικό'}
                else if (accessPolicy === 'public') { accessPolicyTitle = 'Δημόσιο'}
                $('#ap-dd-header').html("<b class='mr-2'>Πρόσβαση:</b>" + accessPolicyTitle);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("ap");
                queryParams.delete("skip");
                removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
                $("#clear-ap-filter").attr('href','daily?' + queryParams);
                $("#accessPolicy-filter").show();
            }
            //Tags
            let tagTitle;
            let tag = $("#tag").val();
            if (tag !== undefined && tag !== null && tag !== '') {
                if (tag === 'ResApp') {tagTitle = 'Απαιτείται Αποδοχή'}
                else if (tag === 'MultUp') {tagTitle = 'Απαιτείται Μεταφόρτωση Βίντεο'}
                else if (tag === 'MetEdt') {tagTitle = 'Ελλειπή Μεταδεδομένα'}
                else if (tag === 'PreUp') {tagTitle = 'Απαιτείται Μεταφόρτωση Παρουσίασης'}
                else if (tag === 'MultEdt') {tagTitle = 'Απαιτείται Επεξεργασία Βίντεο'}
                else if (tag === 'MultRed') {tagTitle = 'Απαιτείται Πραγματική Κοπή'}
                else if (tag === 'PreSyn')  {tagTitle = 'Απαιτείται Συγχρονιμός'}
                $('#ap-tag-header').html("<b class='mr-2'>Έλλειψη:</b>" + tagTitle);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("t");
                queryParams.delete("skip");
                removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
                $("#clear-tag-filter").attr('href','daily?' + queryParams);
                $("#tags-filter").show();
            }
        }
        function removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams) {
            if (queryParams.get("ca") === null && queryParams.get("d") === null && queryParams.get("s") === null && queryParams.get("ap") === null
                && queryParams.get("rt") === null && queryParams.get("t") === null && queryParams.get("ft") === null && queryParams.get("c") === null && queryParams.get("e") === null) {
                queryParams.delete("sort");
                queryParams.delete("direction");
            }
        }
        function setTagLinks() {
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("t");
            queryParams.set("t","ResApp");
            $("#ResAppLink").attr("href",'daily?' + queryParams);
            queryParams.delete("t");
            queryParams.set("t","MultUp");
            $("#MultUpLink").attr("href",'daily?' + queryParams);
            queryParams.delete("t");
            queryParams.set("t","MetEdt");
            $("#MetEdtLink").attr("href",'daily?' + queryParams);
            queryParams.delete("t");
            queryParams.set("t","MultEdt");
            $("#PreUpLink").attr("href",'daily?' + queryParams);
            queryParams.delete("t");
            queryParams.set("t","PreUp");
            $("#MultEdtLink").attr("href",'daily?' + queryParams);
            queryParams.delete("t");
            queryParams.set("t","MultRed");
            $("#MultRedLink").attr("href",'daily?' + queryParams);
            queryParams.delete("t");
            queryParams.set("t","PreSyn");
            $("#PreSynLink").attr("href",'daily?' + queryParams);
        }

        alertify.defaults.transition = "slide";
        alertify.defaults.theme.ok = "btn btn-primary";
        alertify.defaults.theme.cancel = "btn btn-danger";
        alertify.defaults.theme.input = "form-control";
        alertify.set('notifier','position', 'top-center');
    };

    $(document).ready(function () {

        dashboard.init();
        define_events();
        init_controls();
    });

    dashboard.broker.getRootSitePath = function () {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let applicationName = _location.substring(0, applicationNameIndex) + '/';
        let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

        return _location.substring(0, webFolderIndex);
    };

    function define_events() {

        $( ".player_tab" ).mouseenter(function(e) {
            $(this).find(".btn").show();
        });
        $( ".player_tab" ).mouseleave(function(e) {
            $(this).find(".btn").hide();
        });

        $("#refresh_table").on('click',function(e){
             location.reload();
             e.preventDefault();
        });

        $('#sort_select').on('select2:select', function (e) {
            let data = e.params.data;
            let id = data.id;
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.set("sort", id);
            queryParams.delete("skip");
            window.location.href = "daily?" + queryParams;
        });

        $('#direction_select').on('select2:select', function (e) {
            let data = e.params.data;
            let id = data.id;
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.set("direction", id);
            queryParams.delete("skip");
            window.location.href = "daily?" + queryParams;
        });

        $("#department-columns, #category-list").on('click','a.dropdown-toggle', function() {
            if (!$(this).next().hasClass('show')) {
                $(this).parents('.dropdown-menu').first().find('.show').removeClass('show');
            }
            var $subMenu = $(this).next('.dropdown-menu');
            $subMenu.toggleClass('show');

            $(this).parents('li.nav-item.dropdown.show').on('hidden.bs.dropdown', function() {
                $('.dropdown-submenu .show').removeClass('show');
            });
            return false;
        });

        $("#play_edited_video").on('click',function(e) {
            let info = $(this).data("info").split("::");
            let id = info[0];
            let title = info[1];
            $("#playEditedModalTitle").text(title);
            load_edited_video(id);
            e.preventDefault();
        })

        let $body = $("body");
        $body.on('click','.cancel_future', function(e){
            let data_id = $(this).data("target");
            let data_date = $(this).data("date");
            let _date = moment(data_date).format("YYYY-MM-DD");
            let display_time = moment(data_date).format("HH:mm");
            let data_title =  $(this).data("title");

            let msg = '<div><b>Η μετάδοση</b> του Μαθήματος/Εκδήλωσης <b>"' + data_title + '"</b>' +
                      ' που είναι προγραμματισμένη για σήμερα ' +  _date   + ' στις <b>' + display_time + '</b>, θα ακυρωθεί. Είστε σίγουρος?' +
                '<br/><br/> Πληκτρολογήστε (προαιρετικά) την αιτία της ακύρωσης' +
                '</div>';
            alertify.prompt('Ακύρωση Μετάδοσης', msg,'',
                function (evt,value) {
                    postCancellation(data_id,_date,value);
                },
                function () {
                }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
            e.preventDefault();
        });

        $body.on('click','.stop_live', function(e){
            let data_id = $(this).data("target");
            let data_date = $(this).data("date");
            let _date = moment(data_date).format("YYYY-MM-DD");
            let display_time = moment(data_date).format("HH:mm");
            let data_title =  $(this).data("title");

            let msg = '<div><b>Η μετάδοση</b> του Μαθήματος/Εκδήλωσης <b>"' + data_title + '"</b>' +
                ' θα σταματήσει <b>ΑΜΕΣΩΣ</b>. Είστε σίγουρος?' +
                '<br/><br/> Πληκτρολογήστε (προαιρετικά) την αιτία της ακύρωσης' +
                '</div>';
            alertify.prompt('Ακύρωση Μετάδοσης', msg,'',
                function (evt,value) {
                   postImmediateStreamStop(data_id,_date,value);
                },
                function () {
                }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
            e.preventDefault();
        });


       $(".broadcast_info").on('click',function(e){

           let editor_name = $(this).data("editor");
           let server_info = $(this).data("server");
           let title = $(this).data("title");
           $("#LiveModalTitle").html("<h6>" + title +"<br/><small>Πληροφορίες Μετάδοσης</small></h6>");
           let dlist = '<dl class="row mt-0">';
           dlist += '<dt class="col-sm-6">Προγραμματιστής</dt><dd class="col-sm-6">' + editor_name + '</dd>';
           dlist += '<dt class="col-sm-6">Εξυπηρετητής/Εφαρμογή</dt><dd class="col-sm-6">' + server_info + '</dd>';
           dlist += "</dl>";
           $("#dl_info").html(dlist);
           $("#live_info_modal").modal('show');
           e.preventDefault();
       })
    }

    function postCancellation(id,date,reason) {
        let cancellation = {
            title : reason,
            date : date
        };
        $.ajax({
            url: dashboard.siteUrl + '/api/v1/schedule_table/set_cancellation/' + id,
            type:"POST",
            contentType: "application/json; charset=utf-8",
            data: 		  JSON.stringify(cancellation),
            async: true,
            success: function() {
                location.reload();
                alertify.notify("Η ακύρωση καταχωρήθηκε" , "success");
            },
            error : function(msg) {
                alertify.error("Σφάλμα: " + msg.responseText);
            }
        });
    }

    function postImmediateStreamStop(id,date,reason) {
        let cancellation = {
            title : reason,
            date : date
        };
        $.ajax({
            url: dashboard.siteUrl + '/api/v1/schedule_table/stream_cancellation/' + id,
            type:"POST",
            contentType: "application/json; charset=utf-8",
            data: 		  JSON.stringify(cancellation),
            async: true,
            success: function() {
                window.location.replace(window.location.href); //location.reload();
                alertify.notify("Η ακύρωση καταχωρήθηκε" , "success");
            },
            error : function(msg) {
                alertify.error("Σφάλμα: " + msg.responseText);
            }
        });
    }

    function init_controls() {
        $("#aFilterAccessPolicy").select2({
            minimumResultsForSearch: -1 //hides the searchbox
        });
        $("#aFilterType").select2({
            minimumResultsForSearch: -1
        });
        $("#aFilterTags").select2({
            minimumResultsForSearch: -1
        });

        //init sort select2
        let $sort_select = $("#sort_select");
        $sort_select.select2({minimumResultsForSearch: Infinity});
        let sortby = $("#sortField").val();
        $sort_select.val(sortby).trigger("change");

        //init direction select2
        let $direction_select = $("#direction_select");
        $direction_select.select2({minimumResultsForSearch: Infinity});
        let sortDirection = $("#directionField").val();
        $direction_select.val(sortDirection).trigger("change");

        $("#last_updated").html("<i class=\"fas fa-highlighter\"></i> " + moment(new Date()).format('HH:mm:ss') + " )");

        alertify.defaults.transition = "slide";
        alertify.defaults.theme.ok = "btn btn-primary";
        alertify.defaults.theme.cancel = "btn btn-danger";
        alertify.defaults.theme.input = "form-control";
    }

    function checkTime(i) {
        if (i < 10) {
            i = "0" + i;
        }
        return i;
    }

    function startTime() {
        var today = new Date();
        var h = today.getHours();
        var m = today.getMinutes();
        var s = today.getSeconds();
        // add a zero in front of numbers<10
        m = checkTime(m);
        s = checkTime(s);
        document.getElementById('time').innerHTML = h + ":" + m + ":" + s;
        let t = setTimeout(function() {
            startTime()
        }, 500);
    }
    startTime();
})();
