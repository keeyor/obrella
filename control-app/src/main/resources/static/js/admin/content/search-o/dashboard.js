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
    let resourceType    = $("#resourceType").val();
    let accessPolicy    = $("#accessPolicy").val();
    let searchText      = $("#search-box").val();
    let tag             = $("#tag").val();

    let ajaxRefreshInterval;

    dashboard.init = function () {

        dashboard.siteUrl = dashboard.broker.getRootSitePath();

        let cpage = $("#cpage").val();
        let user_access_type   = $("#userAccess").val();

        if (cpage === "search" && (user_access_type === "SA" || user_access_type === "MANAGER")) {
            dashboard.departments.init();
        }
        dashboard.staffmembers.init();
        dashboard.course.init();
        dashboard.sevents.init();

        queryString = $("#queryString").val();

       // if ( queryString !== undefined && queryString !== '')
        {
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
                        //admin-filters are hidden elsewhere if needed
                        $("#a-admin-filters").show();

                        if (resourceType === '') {
                            $("#table-typefilter").show();
                        }
                        else {
                         $("#table-typefilter").hide();
                        }
                        if (accessPolicy === '') {
                            dashboard.afilters.loadApFiltersByReport();
                            $("#table-apfilter").show();
                        }
                        else {
                            $("#table-apfilter").hide();
                        }
                        if (tag === '') {
                            $("#a-tag-filters").show();
                        }
                        else {
                            $("#a-tag-filters").hide();
                        }
                    }
            }, 1000 );
        }

        let hasOtherParam = false;
        let queryParams = new URLSearchParams(window.location.search);
        let it = queryParams.entries();
        let result = it.next();
        while (!result.done) {
            if (result.value[0] !== "skip" && result.value[0] !== "sort" && result.value[0] !== "direction")  {
                hasOtherParam = true;
            }
            result = it.next();
        }
        //Hide clear-all-filter if skip | sort | direction param are the only ones present
        if (hasOtherParam === false) {
            //$("#QueryReportStatus").html("");
            $("#clear-all-filters").hide();
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

            $("#clear-all-filters").attr('href','search');
            $("#clear-all-filters").show();

            if (searchText!== null && searchText !== '') {
                $('#ft-dd-header').html("<span class='far fa-times-circle'></span> Κείμενο: " + searchText);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("ft");
                queryParams.delete("skip");
                removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
                $("#clear-ft-filter").attr('href','?' + queryParams);
                $("#ft-filter").show();
            }

            let courseFilterText = $("#courseFilterText").val();
            if (courseFilterId !== undefined && courseFilterId != null && courseFilterId  !== '') {
                $('#course-dd-header').html("<span class='far fa-times-circle'></span> Μάθημα: " + courseFilterText);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("c");
                queryParams.delete("skip");
                removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
                $("#clear-co-filter").attr('href','?' + queryParams);
                $("#course-filter").show();
            }

            let staffFilterText = $("#staffMemberFilterText").val();
            if (staffFilterId !== undefined && staffFilterId != null && staffFilterId !== '') {
                $('#staff-dd-header').html("<span class='far fa-times-circle'></span> Καθηγητής: " + staffFilterText);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("s");
                queryParams.delete("skip");
                removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
                $("#clear-sm-filter").attr('href','?' + queryParams);
                $("#staff-filter").show();
            }

            let departmentFilterText = $("#departmentFilterText").val();
            if (departmentFilterId !== undefined && departmentFilterId != null && departmentFilterId !== '') {
                $('#department-dd-header').html("<span class='far fa-times-circle'></span> Τμήμα: " + departmentFilterText);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("d");
                queryParams.delete("skip");
                removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
                $("#clear-dt-filter").attr('href','?' + queryParams);
                $("#department-filter").show();
            }

            let eventFilterText = $("#scheduledEventFilterText").val();
            if (eventFilterId !== undefined && eventFilterId != null && eventFilterId !== '') {
                $('#events-dd-header').html("<span class='far fa-times-circle'></span> Εκδήλωση: " + eventFilterText);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("e");
                queryParams.delete("skip");
                removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
                $("#clear-ev-filter").attr('href','?' + queryParams);
                $("#events-filter").show();
            }

            let categoryFilterText = $("#categoryTitle").val();
            if (categoryCode !== undefined && categoryCode != null && categoryCode !== '') {
                $('#category-dd-header').html("<span class='far fa-times-circle'></span>  Κατηγορία: " + categoryFilterText);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("ca");
                queryParams.delete("skip");
                removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
                $("#clear-ca-filter").attr('href','?' + queryParams);
                $("#category-filter").show();
            }
            //ResourceType
            let resourceTypeTitle;
            if (resourceType !== undefined && resourceType != null && resourceType !== '') {
                if (resourceType === 'c') {resourceTypeTitle = 'Διάλεξη'}
                else if (resourceType === 'e') { resourceTypeTitle = 'Εκδήλωση'}
                $('#rt-dd-header').html("<span class='far fa-times-circle'></span> Τύπος: " + resourceTypeTitle);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("rt");
                queryParams.delete("skip");
                removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
                $("#clear-rt-filter").attr('href','?' + queryParams);
                $("#resourceType-filter").show();
            }
            //AccessPolicy
            let accessPolicyTitle;
            if (accessPolicy !== undefined && accessPolicy != null && accessPolicy !== '') {
                if (accessPolicy === 'private') {accessPolicyTitle = 'Ιδιωτικό'}
                else if (accessPolicy === 'public') { accessPolicyTitle = 'Δημόσιο'}
                $('#ap-dd-header').html("<span class='far fa-times-circle'></span> Πρόσβαση: " + accessPolicyTitle);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("ap");
                queryParams.delete("skip");
                removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
                $("#clear-ap-filter").attr('href','?' + queryParams);
                $("#accessPolicy-filter").show();
            }
            //Tags
            let tagTitle;
            let tag = $("#tag").val();
            if (tag !== undefined && tag !== null && tag !== '') {
                if (tag === 'ResApp') {tagTitle = 'Απαιτείται Αποδοχή'}
                else if (tag === 'MetEdt') {tagTitle = 'Ελλειπή Μεταδεδομένα'}
                else if (tag === 'PreUp') {tagTitle = 'Απαιτείται Μεταφόρτωση Παρουσίασης'}
                else if (tag === 'MultUp') {tagTitle = 'Απαιτείται Μεταφόρτωση Βίντεο'}
                else if (tag === 'MultEdt') {tagTitle = 'Απαιτείται Επεξεργασία Βίντεο'}
                else if (tag === 'MultRed') {tagTitle = 'Απαιτείται Πραγματική Κοπή'}
                else if (tag === 'PreSyn')  {tagTitle = 'Απαιτείται Συγχρονιμός'}
                $('#ap-tag-header').html("<span class='far fa-times-circle'></span> Έλλειψη: " + tagTitle);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("t");
                queryParams.delete("skip");
                removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
                $("#clear-tag-filter").attr('href','?' + queryParams);
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
            $("#ResAppLink").attr("href",'?' + queryParams);
            queryParams.delete("t");
            queryParams.set("t","MultUp");
            $("#MultUpLink").attr("href",'?' + queryParams);
            queryParams.delete("t");
            queryParams.set("t","MetEdt");
            $("#MetEdtLink").attr("href",'?' + queryParams);
            queryParams.delete("t");
            queryParams.set("t","MultEdt");
            $("#PreUpLink").attr("href",'?' + queryParams);
            queryParams.delete("t");
            queryParams.set("t","PreUp");
            $("#MultEdtLink").attr("href",'?' + queryParams);
            queryParams.delete("t");
            queryParams.set("t","MultRed");
            $("#MultRedLink").attr("href",'?' + queryParams);
            queryParams.delete("t");
            queryParams.set("t","PreSyn");
            $("#PreSynLink").attr("href",'?' + queryParams);
        }


    };

    $(document).ready(function () {

        dashboard.init();
        init_controls();
        define_events();
    });

    dashboard.broker.getRootSitePath = function () {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let applicationName = _location.substring(0, applicationNameIndex) + '/';
        let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

        return _location.substring(0, webFolderIndex);
    };

    function define_events() {

        $("#copy-url").on('click', function(e){
            copyUrl();
            e.preventDefault();
        });
        $("#copy-embed").on('click', function(e){
            copyEmbedUrl();
            e.preventDefault();
        });

       $(".course_info_lnk").on('click',function(e){
            let target= $(this).data("id");
            dashboard.course.getCourseInfo(target);
            $("#CourseInfoModal").modal("show");
            e.preventDefault();
        })

        $(".help_info").hover(
            function() {
                let target= $(this).data("target");
                $( "." + target).show();
                $( ".gen_info").hide();
            }, function() {
                let target= $(this).data("target");
                $( "." + target).hide();
                $( ".gen_info").show();
            }
        );

        $('.sort_select').on('click', function (e) {
            let id = $(this).data("value");
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.set("sort", id);
            queryParams.delete("skip");
            window.location.href = "search?" + queryParams;
        });

        $('.direction_select').on('click', function (e) {
            let id = $(this).data("value");
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.set("direction", id);
            queryParams.delete("skip");
            window.location.href = "search?" + queryParams;
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
        });


        let $collapseAdminFilters = $('#collapseAdminFilters');
        let $collapseIcon         = $("#collapse-icon");

        $collapseAdminFilters.on('hidden.coreui.collapse', function () {
            $collapseIcon.removeClass("fa-caret-up");
            $collapseIcon.addClass("fa-caret-down");
        })
        $collapseAdminFilters.on('shown.coreui.collapse', function () {
            $collapseIcon.removeClass("fa-caret-down");
            $collapseIcon.addClass("fa-caret-up");
        })
    }

    function init_controls() {

       /* $("#sidebar-right").toggleClass("collapsed");
        $("#content").toggleClass("col-md-12 col-md-9");*/

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
        //let $sort_select = $("#sort_select");
        //$sort_select.select2({minimumResultsForSearch: Infinity});
        let sortby = $("#sortField").val();
        // $sort_select.val(sortby).trigger("change");
        if (sortby === 'rel') {
            $("#direction_dd").addClass("disabled");
        }
        else {
            $("#direction_dd").remove("disabled");
        }
        //init direction select2
        let $direction_select = $("#direction_select");
        //$direction_select.select2({minimumResultsForSearch: Infinity});
        let sortDirection = $("#directionField").val();
        //$direction_select.val(sortDirection).trigger("change");

        alertify.defaults.transition = "slide";
        alertify.defaults.theme.ok = "btn blue-btn-wcag-bgnd-color text-white";
        alertify.defaults.theme.cancel = "btn red-btn-wcag-bgnd-color text-white";
        alertify.defaults.theme.input = "form-control";
    }

    function copyUrl() {
        let input = $("#cp_resource_id");
        let resource_id = input.data("target");
        let public_url = dashboard.siteUrl + "/player?id=" + resource_id;
        input.val(public_url);
        copyToClipboard(input, "Η διεύθυνση έχει αντιγραφεί στο πρόχειρο!");
    }

    function copyEmbedUrl() {
        let input = $("#cp_resource_id");
        let resource_id = input.data("target");
        let text = '<iframe width="560" height="315" src="' + dashboard.siteUrl + '/api/v1/embed/' + resource_id + '" frameBorder="0"></iframe>';
        input.val(text);
        copyToClipboard(input, "Η κώδικας ενσωμάτωσης έχει αντιγραφεί στο πρόχειρο!");
    }

    function copyToClipboardFF(text) {
        window.prompt ("Copy to clipboard: Ctrl C, Enter", text);
    }

    function copyToClipboard(input, message) {

        var success   = true,
            range     = document.createRange(),
            selection;

        // For IE.
        if (window.clipboardData) {
            window.clipboardData.setData("Text",input.val());
        } else {
            // Create a temporary element off screen.
            var tmpElem = $('<div>');
            tmpElem.css({
                position: "absolute",
                left:     "-1000px",
                top:      "-1000px",
            });
            // Add the input value to the temp element.
            tmpElem.text(input.val());
            $("body").append(tmpElem);
            // Select temp element.
            range.selectNodeContents(tmpElem.get(0));
            selection = window.getSelection();
            selection.removeAllRanges ();
            selection.addRange (range);
            // Lets copy.
            try {
                success = document.execCommand ("copy", false, null);
            }
            catch (e) {
                copyToClipboardFF(input.val());
            }
            if (success) {
                alert (message);
                tmpElem.remove();
            }
        }
    }
})();
