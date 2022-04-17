(function () {
    'use strict';

    window.dashboard = window.dashboard || {};
    dashboard.broker = $({});

    dashboard.siteUrl   = "";
    dashboard.app_path  = "";

    let marked_resources = [];
    let status = "Pending";
    let time_elapsed = -1;
    let queryString;

    let courseId        = $("#courseFilterId").val();
    let eventId         = $("#scheduledEventFilterId").val();
    let staffId         = $("#staffMemberFilterId").val();
    let departmentId    = $("#departmentFilterId").val();
    let accessPolicy    = $("#accessPolicy").val();
    let tag             = $("#tag").val();
    let res_type        = $("#resource_type").val();

    let $none_dynamic_filter_msg = $("#no_dyna_filters");
    let $none_applied_filter_msg = $("#none_filter");

    let ajaxRefreshInterval;

    dashboard.init = function () {

        dashboard.siteUrl = dashboard.broker.getRootSitePath();

        queryString = $("#queryString").val();
        $("#clear-all-filters").hide();

        $none_dynamic_filter_msg.show();
        $none_dynamic_filter_msg.html("Δημιουργία δυναμικών φίλτρων σε εξέλιξη. Παρακαλώ περιμένετε...");

        setupAppliedFilters();

            if (staffId !== '' || departmentId !== '' || courseId !== '' || eventId !== '' || accessPolicy !== '' || tag !== '' || res_type !== '') {
                $none_applied_filter_msg.hide();
            }

            ajaxRefreshInterval = setInterval( function () {
                    getReportQueryStatus();
                    if (status === 'Finished') {
                        getReportQueryTime();
                        clearInterval(ajaxRefreshInterval);

                        if ( res_type === '' &&  (courseId === '' || courseId === undefined) && (eventId === '' || eventId === undefined)) {
                            dashboard.afilters.loadResourceTypeCounterFiltersByReport();
                            $("#rtFilters").show();
                        }
                        else {
                            $("#rtFilters").hide();
                        }
                        if (courseId === '' && res_type !== 'e' && (eventId === undefined || eventId === '')) {
                            dashboard.course.loadCourseByReport();
                            $("#coFilters").show();
                        }
                        else {
                            $("#coFilters").hide();
                            $("#courses_filter_row").hide();
                            $("#table-typefilter").hide();
                        }
                        if (eventId === '' &&  res_type !== 'c' && (courseId === undefined || courseId === '')) {
                            dashboard.sevents.loadEventsByReport();
                            $("#evFilters").show();
                        }
                        else {
                            $("#evFilters").hide();
                            $("#events_filter_row_filter_row").hide();
                            $("#table-typefilter").hide();
                        }
                        if (staffId === '') {
                            dashboard.staffmembers.loadStaffByReport();
                            $("#stFilters").show();
                        }
                        else {
                            $("#stFilters").hide();
                        }
                        if (departmentId === '') {
                             dashboard.departments.loadDepartmentsByReport();
                             $("#dpFilters").show();
                        }
                        else {
                            $("#dpFilters").hide();
                        }
                        if (accessPolicy === '') {
                            dashboard.afilters.loadApFiltersByReport();
                            $("#apFilters").show();
                        }
                        else {
                            $("#apFilters").hide();
                        }

                        if (tag === '') {
                            dashboard.afilters.loadTags();
                            $("#tagFilters").show();
                        }

                        if (staffId !== '' && departmentId !== '' && (courseId !== '' || eventId !== '') && accessPolicy !== '' && tag !== '' && res_type !== '') {
                            $none_dynamic_filter_msg.html("δεν βρέθηκαν επιπλέον κριτήρια.");
                        }
                    }
            }, 1000 );


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

    function setupAppliedFilters() {

/*
        let courseId = $("#courseId").val();
        let eventId = $("#scheduledEventFilterId").val();
        let staffId = $("#staffMemberFilterId").val();
        let departmentId = $("#departmentId").val();

*/

        if (courseId !== '' || eventId !== '' || staffId !== '' || departmentId !== '' || accessPolicy !== '' || tag !== '' || res_type !== '') {
            $("#clear-all-filters").attr('href', 'search').show();
        }

        let courseFilterText = $("#courseFilterText").val();
        if (courseId !== undefined && courseId != null && courseId  !== '') {
            $('#course-dd-header').html("<span class='fas fa-minus-circle'></span> | Μάθημα: " + courseFilterText);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("c");
            queryParams.delete("skip");
            removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
            $("#clear-co-filter").attr('href','search?' + queryParams);
            $("#course-filter").show();
        }

        let eventFilterText = $("#scheduledEventFilterText").val();
        if (eventId !== undefined && eventId != null && eventId  !== '') {
            $('#event-dd-header').html("<span class='fas fa-minus-circle'></span> | Εκδήλωση: " + eventFilterText);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("e");
            queryParams.delete("skip");
            removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
            $("#clear-ev-filter").attr('href','search?' + queryParams);
            $("#event-filter").show();
        }

        let staffFilterText = $("#staffMemberFilterText").val();
        if (staffId !== undefined && staffId != null && staffId !== '') {
            $('#staff-dd-header').html("<span class='fas fa-minus-circle'></span> | Διδάσκων: " + staffFilterText);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("s");
            queryParams.delete("skip");
            removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
            $("#clear-sm-filter").attr('href','search?' + queryParams);
            $("#staff-filter").show();
        }

        let resourceTypeFilterText;
        if (res_type !== undefined && res_type != null && res_type !== '') {
            if (res_type === "c") { resourceTypeFilterText = "Διάλεξη";}
            else { resourceTypeFilterText = "Πολυμέσο Εκδήλωσης";}
            $('#rt-dd-header').html("<span class='fas fa-minus-circle'></span> | Τύπος: " + resourceTypeFilterText);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("rt");
            queryParams.delete("skip");
            removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
            $("#clear-rt-filter").attr('href','search?' + queryParams);
            $("#rt-filter").show();
        }


        let departmentFilterText = $("#departmentFilterText").val();
        if (departmentId !== undefined && departmentId != null && departmentId !== '') {
            $('#department-dd-header').html("<span class='fas fa-minus-circle'></span> | Τμήμα " + departmentFilterText);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("d");
            queryParams.delete("skip");
            removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
            $("#clear-dt-filter").attr('href','search?' + queryParams);
            $("#department-filter").show();
        }

        let apFilterText = "Δημόσιο";
        if (accessPolicy === "private") {
            apFilterText = "Ιδιωτικό";
        }
        if (accessPolicy !== undefined && accessPolicy != null && accessPolicy !== '') {
            $('#ap-dd-header').html("<span class='fas fa-minus-circle'></span> | Κατάσταση: " + apFilterText);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("ap");
            queryParams.delete("skip");
            removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
            $("#clear-ap-filter").attr('href','search?' + queryParams);
            $("#ap-filter").show();
        }
        //Tags
        let tagTitle;
        if (tag !== undefined && tag !== null && tag !== '') {
            if (tag === 'ResApp') {tagTitle = 'Απαιτείται Αποδοχή'}
            else if (tag === 'MetEdt') {tagTitle = 'Ελλειπή Μεταδεδομένα'}
            else if (tag === 'PreUp') {tagTitle = 'Απαιτείται Μεταφόρτωση Παρουσίασης'}
            else if (tag === 'MultUp') {tagTitle = 'Απαιτείται Μεταφόρτωση Βίντεο'}
            else if (tag === 'MultEdt') {tagTitle = 'Απαιτείται Επεξεργασία Βίντεο'}
            else if (tag === 'MultRed') {tagTitle = 'Απαιτείται Πραγματική Κοπή'}
            else if (tag === 'PreSyn')  {tagTitle = 'Απαιτείται Συγχρονιμός'}
            $('#ap-tag-header').html("<span class='fas fa-minus-circle'></span> Έλλειψη: " + tagTitle);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("t");
            queryParams.delete("skip");
            removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
            $("#clear-tag-filter").attr('href','?' + queryParams);
            $("#tags-filter").show();
        }
    }

    function removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams) {
        if (queryParams.get("d") === null && queryParams.get("s") === null && queryParams.get("ap") === null
            && queryParams.get("rt") === null && queryParams.get("t") === null && queryParams.get("c") === null && queryParams.get("e") === null) {
            queryParams.delete("sort");
            queryParams.delete("direction");
        }
    }


    function define_events() {

        $(".short-list").on("keyup",function() {

            let what = $(this).data("target");
            let value = $(this).val();
            value = value.toLowerCase().replace(/\b[a-z]/g, function(letter) {
                return letter.toUpperCase();
            });
            if (value.length < 2) {
                $('#' + what +' > a').show();
            }
            else {
                $('#' + what + '> a').slideUp().filter( function() {
                    return $(this).text().toLowerCase().indexOf(value) > -1
                }).stop(true).fadeIn();
            }
        });

        $(".academic-year").each(function(){
            let academic_year = $(this).text();
            let  parsed = parseInt(academic_year);
            let set_text = academic_year + " - " + (parsed+1);
            $(this).text(set_text);
        })

        $('#resource_ay').on('select2:select', function (e) {
            let data = e.params.data;
            let sel_ay = data.id;
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.set("y", sel_ay);
            queryParams.delete("skip");
            window.location.href = "search?" + queryParams;
        });

        $(".mark-resource").on('click',function(e1){

            let target = $(this).data("target");
            if (marked_resources.includes(target)) {
                ///remove element -> https://stackoverflow.com/questions/3954438/how-to-remove-item-from-array-by-value
                marked_resources = marked_resources.filter(function(e) { return e !== target })
                $(this).removeClass("blue-btn-wcag-bgnd-color").removeClass("text-white");
            }
            else {
                marked_resources.push(target);
                $(this).addClass("blue-btn-wcag-bgnd-color").addClass("text-white");
            }
            $("#marked-resources").val(marked_resources);
            let marked_items_no = marked_resources.length;
            $("#marked_items_no").html(" (" + marked_items_no + ")");
            if (marked_items_no >0) {
                $("#remove_marked_resources").removeClass("disabled");
            }
            else {
                $("#remove_marked_resources").addClass("disabled");
            }
            console.log("marked_resources:" + marked_resources);
            e1.preventDefault();
        });

        $("#mark_all_resources").on('click',function(e1){
            marked_resources = [];
            $(".mark-resource").each(function () {
                    let target = $(this).data("target");
                    marked_resources.push(target);
                    $(this).addClass("blue-btn-wcag-bgnd-color").addClass("text-white");
            });
            $("#marked-resources").val(marked_resources);
            let marked_items_no = marked_resources.length;
            $("#marked_items_no").html(" (" + marked_items_no + ")");

            //enable actions here
            $("#remove_marked_resources").removeClass("disabled");

            console.log("marked_resources:" + marked_resources);
            e1.preventDefault();
        });

        $("#unmark_all_resources").on('click',function(e1){
            marked_resources.length = 0;
            $(".mark-resource").each(function () {
                let target = $(this).data("target");
                $(this).removeClass("blue-btn-wcag-bgnd-color").removeClass("text-white");
            });
            $("#marked-resources").val("");
            let marked_items_no = marked_resources.length;
            $("#marked_items_no").html(" (" + marked_items_no + ")");
            //disable actions here
            $("#remove_marked_resources").addClass("disabled");

            console.log("marked_resources:" + marked_resources);
            e1.preventDefault();
        });

        $("#copy-url").on('click', function(e){
            copyUrl();
            e.preventDefault();
        });
        $("#copy-embed").on('click', function(e){
            copyEmbedUrl();
            e.preventDefault();
        });

        $('.sort_select').on('click', function () {
            let id = $(this).data("value");
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.set("sort", id);
            queryParams.delete("skip");
            window.location.href = "search?" + queryParams;
        });

        $('.direction_select').on('click', function () {
            let id = $(this).data("value");
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.set("direction", id);
            queryParams.delete("skip");
            window.location.href = "search?" + queryParams;
        });
    }

    function init_controls() {

        //Enable Tooltips
        var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-toggle="tooltip"]'))
        var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new coreui.Tooltip(tooltipTriggerEl)
        });

        let selected_academic_year = $("#academicYear").val();
        $("#resource_ay").select2({

        });
        $("#resource_ay").val(selected_academic_year).trigger("change");

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

        alertify.defaults.transition = "slide";
        alertify.defaults.theme.ok = "btn btn-primary";
        alertify.defaults.theme.cancel = "btn btn-danger";
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
        let text = '<iframe width="560" height="315" src="' + dashboard.siteUrl + '/api/v1/embed/' + resource_id + '"></iframe>';
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
