(function () {
    'use strict';

    window.dashboard = window.dashboard || {};

    dashboard.broker = $({});

    dashboard.siteUrl = "";
    dashboard.hostUrl = "";
    dashboard.dtLanguageGr = "";

    let marked_resources = [];
    let time_elapsed = -1;
    let ajaxRefreshInterval;
    let cpage;

    let staffId         = $("#staff_filter").val();
    let departmentId    = $("#department_filter").val();
    let event_area      = $("#area_filter").val();
    let event_type      = $("#etype_filter").val();
    let event_cat       = $("#ecat_filter").val();

    $(document).ready(function () {
        dashboard.init();
    });

    dashboard.init = function () {

        dashboard.siteUrl = dashboard.broker.getRootSitePath();
        dashboard.hosturl = dashboard.broker.getHostURL();

        $(".clear-filter").on("click", "a", function(event) {
            let target = $(this).data("target");
            dashboard.broker.initFilter(target);
            event.preventDefault();
        });
        cpage = $("#cpage").val();

        define_events();
        init_controls();

        $("#stFilters").hide();
        $("#no_dyna_filters").show();
        $("#no_dyna_filters").html("Δημιουργία δυναμικών φίλτρων σε εξέλιξη. Παρακαλώ περιμένετε...");
        $("#search-panel").hide();
        $("#results-panel").show();
        setFilterRemoveLinks();

        if (departmentId !== '' || staffId !== '' || event_area !== '' || event_type !== '') {
            $("#none_filter").hide();
        }

        ajaxRefreshInterval = setInterval( function () {
            getReportQueryStatus();
            if (status === 'Finished') {
                getReportQueryTime();
                clearInterval(ajaxRefreshInterval);

                if (staffId === '') {
                    dashboard.staffmembers.loadStaffByReport();
                    $("#stFilters").show();
                }
                else {
                    $("#stFilters").hide();
                    // $("#staffCanvasLink").hide();
                }
                if (departmentId === '') {
                    dashboard.departments.loadDepartmentsByReport();
                    $("#dpFilters").show();
                }
                else {
                    $("#dpFilters").hide();
                }
                if (event_area === '') {
                    dashboard.sevents.loadAreasByReport();
                    $("#areaFilters").show();
                }
                else {
                    $("#areaFilters").hide();
                }
                if (event_cat === '' && event_area === "ea_uas") {
                    dashboard.sevents.loadCategoriesByReport();
                    $("#catFilters").show();
                }
                else {
                    $("#catFilters").hide();
                }
                if (event_type === '' && event_area !== '') {
                    dashboard.sevents.loadTypesByReport();
                    $("#typeFilters").show();
                }
                else {
                    $("#typeFilters").hide();
                }
                if (staffId !== '' && departmentId !== '' && event_type !== '' && event_area !== '' && event_cat !== '') {
                    $("#no_dyna_filters").html("δεν βρέθηκαν επιπλέον κριτήρια.");
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

        set_display_results();
    };

    function setFilterRemoveLinks() {

        let staffFilterId = $("#staff_filter").val();
        let departmentFilterId = $("#department_filter").val();
        let areaFilterId = $("#area_filter").val();
        let etypeFilterId = $("#etype_filter").val();
        let catFilterId   = $("#ecat_filter").val();

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
            $("#clear-all-filters").hide();
        }
        else {
            $("#clear-all-filters").attr('href','sevents');
            $("#clear-all-filters").show();
        }

        let staffFilterText = $("#staff_filter_name").val();
        if (staffFilterId !== undefined && staffFilterId != null && staffFilterId !== '') {
            $('#staff-dd-header').html("<span class='fas fa-minus-circle'></span> | Επ. Υπεύθυνος: " + staffFilterText);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("s");
            queryParams.delete("skip");
           // removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
            $("#clear-sm-filter").attr('href','?' + queryParams);
            $("#staff-filter").show();
        }

        let departmentFilterText = $("#department_filter_name").val();
        if (departmentFilterId !== undefined && departmentFilterId != null && departmentFilterId !== '') {
            $('#department-dd-header').html("<span class='fas fa-minus-circle'></span> | Τμήμα " + departmentFilterText);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("d");
            queryParams.delete("skip");
          //  removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
            $("#clear-dt-filter").attr('href','?' + queryParams);
            $("#department-filter").show();
        }

        let areaFilterText = $("#area_filter_name").val();
        if (areaFilterId !== undefined && areaFilterId != null && areaFilterId !== '') {
            $('#area-dd-header').html("<span class='fas fa-minus-circle'></span> | Κατηγορία " + areaFilterText);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("ea");
            queryParams.delete("skip");
            //  removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
            $("#clear-area-filter").attr('href','?' + queryParams);
            $("#area-filter").show();
        }

        let catFilterText = $("#ecat_filter_name").val();
        if ( catFilterId !== undefined && catFilterId != null && catFilterId !== '') {
            $('#cat-dd-header').html("<span class='fas fa-minus-circle'></span> | Θεματική Περιοχή  " + catFilterText);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("ec");
            queryParams.delete("skip");
            //  removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
            $("#clear-cat-filter").attr('href','?' + queryParams);
            $("#cat-filter").show();
        }

        let etypeFilterText = $("#etype_filter_name").val();
        if (etypeFilterId !== undefined && etypeFilterId != null && etypeFilterId !== '') {
            $('#type-dd-header').html("<span class='fas fa-minus-circle'></span> | Τύπος " + etypeFilterText);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("et");
            queryParams.delete("skip");
            $("#clear-type-filter").attr('href','?' + queryParams);
            $("#type-filter").show();
        }

    }
    function removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams) {
        if (queryParams.get("ca") === null && queryParams.get("d") === null && queryParams.get("s") === null && queryParams.get("ap") === null
            && queryParams.get("rt") === null && queryParams.get("t") === null && queryParams.get("ft") === null && queryParams.get("c") === null && queryParams.get("e") === null) {
            queryParams.delete("sort");
            queryParams.delete("direction");
        }
    }
    function  set_display_results() {
        let $clear_filters = $(".clear-filters");
        // disable clear filters button
        $clear_filters.attr("disabled", true);
        let filters = ["department","staff", "area", "etype"];

        for (let i = 0; i < filters.length; i++) {
            let $filter_name = "#" + filters[i] + "_filter_name";
            let $filter_clear = "#" + filters[i] + "_clear";
            let $filter_load = "#" + filters[i] + "_load";

            let filter_value = $($filter_name).val();
            if (filter_value !== '' && filter_value !== '-1') {
                $($filter_clear).show();
                $($filter_load).html("<span style='color:Dodgerblue;font-weight: bold'>" + filter_value + "</span>");
                //enable clear filters buttons if any filter not empty
                $clear_filters.attr("disabled", false);
            }
        }
    }


    dashboard.broker.getRootSitePath = function () {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let applicationName = _location.substring(0, applicationNameIndex) + '/';
        let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

        return _location.substring(0, webFolderIndex);
    };

    dashboard.broker.getHostURL = function () {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let hostUrl= _location.substring(0, applicationNameIndex);

        return hostUrl;
    };

    dashboard.broker.initFilter = function(filter_type) {
        let $filter_id    =  "#" + filter_type + "_filter";
        let $filter_name  =  "#" + filter_type + "_filter_name";
        let $filter_clear =  "#" + filter_type + "_clear";

        $($filter_id).val("");
        $($filter_name).val("");
        $($filter_clear).hide();

        let queryParams = new URLSearchParams(window.location.search);
        if (filter_type === "department") {
            queryParams.delete("d");
            queryParams.delete("s"); //init staff list too!
        }
        else if (filter_type === "staff") {
            queryParams.delete("s");
        }
        else if (filter_type === "area") {
            queryParams.delete("ea");
            queryParams.delete("et");
        }
        else if (filter_type === "etype") {
            queryParams.delete("et");
        }
        location.href = cpage + "?" + queryParams;

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
            $(".mark-resource").each(function (index) {
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
            $(".mark-resource").each(function (index) {
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
            window.location.href = cpage + "?" + queryParams;
        });

        $('.direction_select').on('click', function (e) {
            let id = $(this).data("value");
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.set("direction", id);
            queryParams.delete("skip");
            window.location.href = cpage + "?" + queryParams;
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
    }

    function init_controls() {

        //Enable Tooltips
        var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-toggle="tooltip"]'))
        var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new coreui.Tooltip(tooltipTriggerEl)
        });


        let sortby = $("#sortField").val();
        if (sortby === 'rel') {
            $("#direction_dd").addClass("disabled");
        }
        else {
            $("#direction_dd").remove("disabled");
        }
        alertify.defaults.transition = "slide";
        alertify.defaults.theme.ok = "btn blue-btn-wcag-bgnd-color text-white";
        alertify.defaults.theme.cancel = "btn btn-danger";
        alertify.defaults.theme.input = "form-control";
        alertify.set('notifier','position', 'top-right');

    }
})();