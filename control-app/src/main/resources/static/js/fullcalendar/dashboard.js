/*jshint esversion: 6 */
(function () {
    'use strict';

    window.dashboard = window.dashboard || {};
    dashboard.broker = $({});

    dashboard.siteurl       	= "";
    dashboard.institution   	= "";
    dashboard.institution_name  = "";
    dashboard.selected_year 	= "";
    dashboard.repeat            = "";
    dashboard.dayOfWeek         = "";
    dashboard.dtLanguageGr = "";
    
    dashboard.init = function () {
    	
    	dashboard.siteurl           = dashboard.broker.getRootSitePath();
    	dashboard.institution 		= $("#institutionId").val();
    	dashboard.institution_name  = $("#institutionName").val();

    	dashboard.system.init();
        //dashboard.department.init();
        //dashboard.course.init();
        //dashboard.classroom.init();


        loader.initialize();

        alertify.defaults.transition = "slide";
        alertify.defaults.theme.ok = "btn blue-btn-wcag-bgnd-color text-white";
        alertify.defaults.theme.cancel = "btn red-btn-wcag-bgnd-color text-white";
        alertify.defaults.theme.input = "form-control";
        alertify.set('notifier','position', 'top-right');

        dashboard.selected_year = $("#currentAcademicYear").val();
        dashboard.department.loadDepartmentsOnSearchBar($("#department-columns"));

        let editor_is_staffMember = $("#editor_is_staffMember").val();
        if (editor_is_staffMember === "true") {
            dashboard.editor.loadEditorsOnSearchBar($("#editor-columns"));
        }
        //dashboard.classroom.loadEnabledClassroomsOnSearchBar($("#classroom-columns"));

        if ($("#departmentFilterId").val() === '') {
            dashboard.department.loadDepartmentsByReport();
            $("#departments_filter_row").show();
        }
        if ($("#courseFilterId").val() === '') {
            dashboard.course.loadCourseByReport();
            $("#courses_filter_row").show();
        }
        if ($("#scheduledEventFilterId").val() === '') {
            dashboard.sevents.loadEventsByReport();
            $("#events_filter_row").show();
        }
        if ($("#staffMemberFilterId").val() === '') {
            dashboard.staffmembers.loadStaffByReport();
            $("#staff_filters_row").show();
        }
        if ($("#classRoomFilterId").val() === '') {
            dashboard.classroom.loadClassroomByReport();
            $("#classroom_filter_row").show();
        }

        let view = $("#view").val();
        if (view === '' || view === undefined) {
            dashboard.calendar.view = 'listMonth';
        }
        else {
            dashboard.calendar.view = view;
        }

        dashboard.calendar.LoadFullCalendar();

        setFilterRemoveLinks();

        $("#QueryReportStatus").hide();
    };

    $(document).ready(function () {

       dashboard.init();

        $(document).on( 'preInit.dt', function (e, settings) {
            loader.showLoader();
        } );

        $("body").tooltip({
            selector: '[data-toggle="tooltip"]'
        });

      dashboard.broker.on("filter.select", function (event, message) {
            //console.log(message);
            dashboard.broker.clearOtherFilters(message.filter);
            let text = dashboard.broker.getTextForFilter(message.filter);
            dashboard.broker.toggleFilter(message,text);
        });

       dashboard.broker.on('ShowInstantMessage', function (event, message) {
           dashboard.broker.showInstantMessage(message.type ,message.val);
      });

    }); //document ready end

    dashboard.broker.getCurrentAcademicPeriod = function () {
    	
    	let currentAcademicYear;
    	
    	var d = new Date();
    	var n = d.getFullYear();

    	let currentDate = moment(d).format('YYYY-MM-DD');
    	let current_month = moment(currentDate).month();
    	let end_year = n;

        if (current_month>7) {
            end_year = n+1;
        }
        currentAcademicYear = end_year-1;

  	   return  currentAcademicYear;
   };
    dashboard.broker.getRootSitePath = function () {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let applicationName = _location.substring(0, applicationNameIndex) + '/';
        let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

        return _location.substring(0, webFolderIndex);
    };

    dashboard.broker.toggleFilter = function(message, text) {
        let filter_type     = message.filter;
        let filter_value    = message.id;
        let $filter_id      = "#" + filter_type + "_filter";
        let $filter_name    = "#" + filter_type + "_filter_name";

        $($filter_id).val(filter_value);
        $($filter_name).val(message.value);

        // handle clear filter event
        if (filter_value === null || filter_value === "") {
            dashboard.broker.initFilter(filter_type);
        }
        dashboard.calendar.reloadTimeTableLectureEDT();
    };

    dashboard.broker.initFilter = function(filter_type) {
        let $filter_id    =  "#" + filter_type + "_filter";
        let $filter_name  =  "#" + filter_type + "_filter_name";
        let $filter_clear =  "#" + filter_type + "_clear";
        let $filter_load  =  "#" + filter_type + "_load";
        let text = dashboard.broker.getTextForFilter(filter_type);

        $($filter_id).val("");
        $($filter_name).val("");
        $($filter_clear).hide();
        let $filter= $($filter_load);
        $filter.html(text);
    }
    dashboard.broker.getTextForFilter = function(filter) {

        let text;
        switch (filter) {
            case "school":
                text = "??????????";
                break;
            case "department":
                text = "??????????";
                break;
            case "classroom":
                text = "??????????????/??????????";
                break;
            case "repeat":
                text = "??????????????????";
                break;
            case "dow":
                text = "??????????";
                break;
            case "period":
                text = "????????????????";
                break;
            case "editor":
                text = "???????????????? ";
                break;
        }
        return text;
    };
    dashboard.broker.clearFilters = function() {

        let filters = ["department","classroom", "editor"];

        for (let i = 0; i < filters.length; i++) {
            let $filter_id    = "#" + filters[i] + "_filter";
            let $filter_name  = "#" + filters[i] + "_filter_name";
            let $filter_clear = "#" + filters[i] + "_clear";
            let $filter_load  = "#" + filters[i] + "_load";

            $($filter_id).val("");
            $($filter_name).val("");
            $($filter_clear).hide();

            let text = dashboard.broker.getTextForFilter(filters[i]);
            let $filter= $($filter_load);
            $filter.html(text);
        }
    };

    dashboard.broker.clearOtherFilters = function(exclude_filter) {

        let filters = ["department","classroom", "editor"];

        for (let i = 0; i < filters.length; i++) {
            if (filters[i] !== exclude_filter) {
                let $filter_id = "#" + filters[i] + "_filter";
                let $filter_name = "#" + filters[i] + "_filter_name";
                let $filter_clear = "#" + filters[i] + "_clear";
                let $filter_load = "#" + filters[i] + "_load";

                $($filter_id).val("");
                $($filter_name).val("");
                $($filter_clear).hide();

                let text = dashboard.broker.getTextForFilter(filters[i]);
                let $filter = $($filter_load);
                $filter.html(text);
             }
        }
    };

    function setFilterRemoveLinks() {

        let courseFilterId = $("#courseFilterId").val();
        let staffFilterId = $("#staffMemberFilterId").val();
        let departmentFilterId = $("#departmentFilterId").val();
        let eventFilterId = $("#scheduledEventFilterId").val();
        let classroomFilterId = $("#classRoomFilterId").val();
        let categoryCode = $("#categoryCode").val();
        let resourceType = $("#resourceType").val();
        let accessPolicy = $("#accessPolicy").val();

        let queryString = $("#queryString").val();

        if (queryString !== "" && queryString !== undefined) {
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("c");
            queryParams.delete("s");
            queryParams.delete("d");
            queryParams.delete("e");
            queryParams.delete("ca");
            queryParams.delete("rp");
            queryParams.delete("ap");
            queryParams.delete("cr");
            queryParams.delete("skip");
            $("#clear-all-filters").attr('href','calendar?' + queryParams);

            let restParams = new URLSearchParams(window.location.search);
            //hide clear-filters if only sd,ed,cv params remain
            restParams.delete("cv");
            restParams.delete("sd");
            restParams.delete("ed");
            console.log("rest:" + restParams.toString());
            if (restParams.toString() === "") {
                $("#clear-all-filters").hide();
            } else {
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
        ) {}

        let courseFilterText = $("#courseFilterText").val();
        if (courseFilterId !== undefined && courseFilterId != null && courseFilterId  !== '') {
            $('#course-dd-header').html("<span class='far fa-times-circle'></span> ????????????: " + courseFilterText);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("c");
            queryParams.delete("skip");
            removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
            $("#clear-co-filter").attr('href','calendar?' + queryParams);
            $("#course-filter").show();
        }


        let staffFilterText = $("#staffMemberFilterText").val();
        if (staffFilterId !== undefined && staffFilterId != null && staffFilterId !== '') {
            $('#staff-dd-header').html("<span class='far fa-times-circle'></span> ??????????????????: " + staffFilterText);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("s");
            queryParams.delete("skip");
            removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
            $("#clear-sm-filter").attr('href','calendar?' + queryParams);
            $("#staff-filter").show();
        }

        let departmentFilterText = $("#departmentFilterText").val();
        if (departmentFilterId !== undefined && departmentFilterId != null && departmentFilterId !== '') {
            $('#department-dd-header').html("<span class='far fa-times-circle'></span> ??????????: " + departmentFilterText);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("d");
            queryParams.delete("skip");
            removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
            $("#clear-dt-filter").attr('href','calendar?' + queryParams);
            $("#department-filter").show();
        }

        let eventFilterText = $("#scheduledEventFilterText").val();
        if (eventFilterId !== undefined && eventFilterId != null && eventFilterId !== '') {
            $('#events-dd-header').html("<span class='far fa-times-circle'></span> ????????????????: " + eventFilterText);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("e");
            queryParams.delete("skip");
            removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
            $("#clear-ev-filter").attr('href','calendar?' + queryParams);
            $("#events-filter").show();
        }
        let classroomFilterText = $("#classRoomFilterText").val();
        if (classroomFilterId !== undefined && classroomFilterId != null && classroomFilterId !== '') {
            $('#classrooms-dd-header').html("<span class='far fa-times-circle'></span> ??????????????: " + classroomFilterText);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("cr");
            queryParams.delete("skip");
            removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
            $("#clear-cr-filter").attr('href','calendar?' + queryParams);
            $("#classrooms-filter").show();
        }
        let categoryFilterText = $("#categoryTitle").val();
        if (categoryCode !== undefined && categoryCode != null && categoryCode !== '') {
            $('#category-dd-header').html("<b class='mr-2'>??????????????????:</b>" + categoryFilterText);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("ca");
            queryParams.delete("skip");
            removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
            $("#clear-ca-filter").attr('href','calendar?' + queryParams);
            $("#category-filter").show();
        }
        //ResourceType
        let resourceTypeTitle;
        if (resourceType !== undefined && resourceType != null && resourceType !== '') {
            if (resourceType === 'c') {resourceTypeTitle = '??????????????'}
            else if (resourceType === 'e') { resourceTypeTitle = '????????????????'}
            $('#rt-dd-header').html("<b class='mr-2'>??????????:</b>" + resourceTypeTitle);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("rt");
            queryParams.delete("skip");
            removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
            $("#clear-rt-filter").attr('href','calendar?' + queryParams);
            $("#resourceType-filter").show();
        }
        //AccessPolicy
        let accessPolicyTitle;
        if (accessPolicy !== undefined && accessPolicy != null && accessPolicy !== '') {
            if (accessPolicy === 'private') {accessPolicyTitle = '????????????????'}
            else if (accessPolicy === 'public') { accessPolicyTitle = '??????????????'}
            $('#ap-dd-header').html("<b class='mr-2'>????????????????:</b>" + accessPolicyTitle);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("ap");
            queryParams.delete("skip");
            removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
            $("#clear-ap-filter").attr('href','calendar?' + queryParams);
            $("#accessPolicy-filter").show();
        }
        //Tags
        let tagTitle;
        let tag = $("#tag").val();
        if (tag !== undefined && tag !== null && tag !== '') {
            if (tag === 'ResApp') {tagTitle = '???????????????????? ??????????????'}
            else if (tag === 'MultUp') {tagTitle = '???????????????????? ?????????????????????? ????????????'}
            else if (tag === 'MetEdt') {tagTitle = '?????????????? ????????????????????????'}
            else if (tag === 'PreUp') {tagTitle = '???????????????????? ?????????????????????? ??????????????????????'}
            else if (tag === 'MultEdt') {tagTitle = '???????????????????? ?????????????????????? ????????????'}
            else if (tag === 'MultRed') {tagTitle = '???????????????????? ???????????????????? ????????'}
            else if (tag === 'PreSyn')  {tagTitle = '???????????????????? ??????????????????????'}
            $('#ap-tag-header').html("<b class='mr-2'>??????????????:</b>" + tagTitle);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("t");
            queryParams.delete("skip");
            removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
            $("#clear-tag-filter").attr('href','calendar?' + queryParams);
            $("#tags-filter").show();
        }
    }
    function removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams) {
        if (queryParams.get("ca") === null && queryParams.get("d") === null && queryParams.get("s") === null && queryParams.get("ap") === null
            && queryParams.get("rt") === null && queryParams.get("t") === null && queryParams.get("ft") === null && queryParams.get("c") === null && queryParams.get("e") === null &&
            queryParams.get("cr") === null) {
            queryParams.delete("sort");
            queryParams.delete("direction");
        }
    }
})();