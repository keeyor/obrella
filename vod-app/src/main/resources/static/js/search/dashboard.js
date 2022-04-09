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

         dashboard.categories.init();
         dashboard.departments.init();
         dashboard.sevents.init();

        queryString = $("#queryString").val();
        $("#clear-all-filters").hide();

        if ( queryString !== undefined && queryString !== '') {

            $("#stFilters").hide();
            $("#no_dyna_filters").html("παρακαλώ περιμένετε...").show();
            $("#search-panel").hide();
            $("#results-panel").show();
            setFilterRemoveLinks();

            ajaxRefreshInterval = setInterval( function () {
                    getReportQueryStatus();
                    //console.log(status);
                    if (status === 'Finished') {
                        getReportQueryTime();
                        clearInterval(ajaxRefreshInterval);
                        if (courseId === '') {
                            dashboard.course.loadCourseByReport();
                            $("#coFilters").show();
                        }
                        else {
                            $("#coFilters").hide();
                            $("#courses_filter_row").hide();
                            $("#table-typefilter").hide();
                        }
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
                        if (staffId !== '' && departmentId !== '' && courseId !== '') {
                            $("#no_dyna_filters").html("δεν βρέθηκαν επιπλέον κριτήρια.");
                            $("#AllFilters").hide();
                        }
                    }
            }, 1000 );
        }
        else {
            $("#QueryReportStatus").html("");
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
            let categoryCode = $("#categoryCode").val();

            $("#clear-all-filters").attr('href','search').show();

            if (
                (departmentFilterId !== '' && courseFilterId !== '' && staffFilterId !=='') ||
                (departmentFilterId !== '' && staffFilterId !=='') ||
                (categoryCode !== '' && departmentFilterId !== '' && staffFilterId !=='') ||
                (categoryCode !== '' && departmentFilterId !== '' && courseFilterId !== '' && staffFilterId !=='')
            ) {
            }

            if (searchText!== null && searchText !== '') {
                $('#ft-dd-header').html("<span class='fas fa-ban'></span> Κείμενο:" + searchText);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("ft");
                queryParams.delete("skip");
                removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
                $("#clear-ft-filter").attr('href','search?' + queryParams);
                $("#ft-filter").show();
            }

            let courseFilterText = $("#courseFilterText").val();
            if (courseFilterId !== undefined && courseFilterId != null && courseFilterId  !== '') {
                $('#course-dd-header').html("<span class='fas fa-ban'></span> | Μάθημα: " + courseFilterText);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("c");
                queryParams.delete("skip");
                removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
                $("#clear-co-filter").attr('href','search?' + queryParams);
                $("#course-filter").show();
            }

            let staffFilterText = $("#staffMemberFilterText").val();
            if (staffFilterId !== undefined && staffFilterId != null && staffFilterId !== '') {
                $('#staff-dd-header').html("<span class='fas fa-ban'></span> | Διδάσκων: " + staffFilterText);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("s");
                queryParams.delete("skip");
                removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
                $("#clear-sm-filter").attr('href','search?' + queryParams);
                $("#staff-filter").show();
            }

            let departmentFilterText = $("#departmentFilterText").val();
            if (departmentFilterId !== undefined && departmentFilterId != null && departmentFilterId !== '') {
                $('#department-dd-header').html("<span class='fas fa-ban'></span> | Τμήμα " + departmentFilterText);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("d");
                queryParams.delete("skip");
                removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
                $("#clear-dt-filter").attr('href','search?' + queryParams);
                $("#department-filter").show();
            }

            let categoryFilterText = $("#categoryTitle").val();
            if (categoryCode !== undefined && categoryCode != null && categoryCode !== '') {
                $('#category-dd-header').html("<span class='fas fa-ban'></span> | Κατηγορία: " + categoryFilterText);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("ca");
                queryParams.delete("skip");
                removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams);
                $("#clear-ca-filter").attr('href','search?' + queryParams);
                $("#category-filter").show();
            }
        }
        function removeSortAndDirectionFiltersIfOnlyOnesLeft(queryParams) {
            if (queryParams.get("ca") === null && queryParams.get("d") === null && queryParams.get("s") === null && queryParams.get("ap") === null
                && queryParams.get("rt") === null && queryParams.get("t") === null && queryParams.get("ft") === null && queryParams.get("c") === null && queryParams.get("e") === null) {
                queryParams.delete("sort");
                queryParams.delete("direction");
            }
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

    function define_events() {



        $( ".player_tab" ).mouseenter(function(e) {
            $(this).find(".btn").show();
        });
        $( ".player_tab" ).mouseleave(function(e) {
            $(this).find(".btn").hide();
        });

        $("#staff_list").on('click',function(e){
            if ( ! $.fn.DataTable.isDataTable( '#StaffMembersSelectDataTable' ) ) {
                dashboard.staffmembers.loadStaffMembersInDataTable();
            }
            else {
                dashboard.staffmembers.reloadInstitutionStaffMembers();
            }

            $("#StaffMemberSelectModal").modal('show');
            e.preventDefault();
        });

        $("#applyStaffMemberFilter").on('click',function(){
            let StaffMembersDT = $("#StaffMembersSelectDataTable").DataTable();
            let nodes= StaffMembersDT.rows( { selected: true } ).data();
            let staffId;
            if (nodes.length>0) {
                for (let l = 0; l < nodes.length; l++) {
                    let _row = nodes[l];
                    staffId = _row.id;
                }
                $("#StaffMemberSelectModal").modal('hide');
                location.href = "search?s=" + staffId;
            }
        });

        $("#course_list").on('click',function(e){
            if ( ! $.fn.DataTable.isDataTable( '#CourseSelectDataTable' ) ) {
                dashboard.course.loadCoursesInDataTable();
            }
            else {
                dashboard.course.reloadInstitutionCourses();
            }

            $("#CourseSelectModal").modal('show');
            e.preventDefault();
        });

        $("#applyCourseFilter").on('click',function(){
            let courseSelectDT = $("#CourseSelectDataTable").DataTable();
            let nodes= courseSelectDT.rows( { selected: true } ).data();
            let courseId;
            if (nodes.length>0) {
                for (let l = 0; l < nodes.length; l++) {
                    let _row = nodes[l];
                    courseId = _row.id;
                }
                $("#CourseSelectModal").modal('hide');
                location.href = "search?c=" + courseId;
            }
        });

        $("#event_list").on('click',function(e){
            if ( ! $.fn.DataTable.isDataTable( '#EventSelectDataTable' ) ) {
                dashboard.sevents.InitInstitutionEvents();
            }
            else {
                dashboard.sevents.reloadInstitutionEvents();
            }
            $("#EventSelectModal").modal('show');
            e.preventDefault();
        });

        $("#applyEventFilter").on('click',function(){
            let eventSelectDT = $("#EventSelectDataTable").DataTable();
            let nodes= eventSelectDT.rows( { selected: true } ).data();
            let eventId;
            if (nodes.length>0) {
                for (let l = 0; l < nodes.length; l++) {
                    let _row = nodes[l];
                    eventId = _row.id;
                }
                $("#EventSelectModal").modal('hide');
                location.href = "search?e=" + eventId;
            }
        });

        $("#refresh_page").on('click',function(e){
            location.reload();
            e.preventDefault();
        });

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
        })

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


        alertify.defaults.transition = "slide";
        alertify.defaults.theme.ok = "btn btn-primary";
        alertify.defaults.theme.cancel = "btn btn-danger";
        alertify.defaults.theme.input = "form-control";

        dashboard.dtLanguageGr = {
            "search": "Αναζήτηση",
            "lengthMenu": "Εμφάνιση _MENU_ εγγραφών",
            "emptyTable": "Δεν βρέθηκαν εγγραφές",
            "zeroRecords": "Η αναζήτηση δεν βρήκε εγγραφές",
            "sInfo": "Εμφάνιση από _START_ έως _END_ από _TOTAL_",
            "infoEmpty": "Εμφάνιση από 0 σε 0 από 0 εγγραφές",
            "infoFiltered": "(Φίλτρο από _MAX_ συνολικές εγγραφές)",
            "loadingRecords": "Φόρτωση...",
            "processing": "Επεξεργασία...",
            "oPaginate": {
                "sNext": "<i class='fas fa-angle-right'></i>",
                "sPrevious": "<i class='fas fa-angle-left'></i>"
            },
            "aria": {
                "sortAscending": ": αύξουσα ταξινόμηση",
                "sortDescending": ": φθίνουσα ταξινόμηση"
            },
            "select": {
                "1": "%d επιλεγμένη γραμμή",
                "_": "%d επιλογές",
                rows: {
                    _: "Έχεται επιλέξει %d γραμμές",
                    0: "Click a row to select it",
                    1: "1 γραμμή επιλεγμένη"
                }
            }
        } // dtLanguageGr
    }
})();
