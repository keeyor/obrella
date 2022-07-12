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
    dashboard.dtLanguageGr      = "";
    
    dashboard.init = function () {

    	dashboard.siteurl           = dashboard.broker.getRootSitePath();
    	dashboard.institution 		= $("#institutionId").val();
    	dashboard.institution_name  = $("#institutionName").val();

        loader.initialize();

        alertify.defaults.transition = "slide";
        alertify.defaults.theme.ok = "btn btn-primary";
        alertify.defaults.theme.cancel = "btn btn-danger";
        alertify.defaults.theme.input = "form-control";
        alertify.set('notifier','position', 'top-right');

        dashboard.selected_year = $("#currentAcademicYear").val();

        //initialize view from request
        let view = $("#view").val();
        if (view !== '' || view !== undefined) {
            dashboard.calendar.view = view;
        }
        else {
            dashboard.calendar.view = "listWeek";
        }

        let departmentFilterId  = $("#departmentFilterId").val();
        let courseFilterId      = $("#courseFilterId").val();
        let staffMemberFilterId = $("#staffMemberFilterId").val();

        if (departmentFilterId !== "") {
            $("#showFilterWarning").hide();
            $("#showCalendar").show();
            if (courseFilterId === '') {
                $("#courseCanvasLink").show();
            }
            if (staffMemberFilterId === '') {
                $("#staffCanvasLink").show();
            }
            dashboard.calendar.LoadFullCalendar();
        }
        else {
            $("#showFilterWarning").show();
            //Hide Other Filters to Force Department Selection
            $("#depCanvasLink").show();
            $("#staffCanvasLink").hide();
            $("#courseCanvasLink").hide();
            $("#showCalendar").hide();
            //force show of Department selection canvas
            $("#offcanvasDep").offcanvas("show");
        }

        setFilterRemoveLinks();
        $("#QueryReportStatus").hide();
    };

    $(document).ready(function () {

       dashboard.init();

        $(document).on( 'preInit.dt', function () {
            loader.showLoader();
        } );

        $("body").tooltip({
            selector: '[data-toggle="tooltip"]'
        });

        $(".short-list").on("keyup",function() {

            let what = $(this).data("target");
            let value = $(this).val();
            value = value.toLowerCase().replace(/\b[a-z]/g, function(letter) {
                return letter.toUpperCase();
            });
            if (value.length < 2) {
                $('#' + what +' > li').show();
            }
            else {
                $('#' + what + '>li').slideUp().filter( function() {
                    return $(this).text().toLowerCase().indexOf(value) > -1
                }).stop(true).fadeIn();
            }
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


    function setFilterRemoveLinks() {

        let $clearAllFiltersLnk = $("#clear-all-filters");

        let courseFilterId      = $("#courseFilterId").val();
        let staffFilterId       = $("#staffMemberFilterId").val();
        let departmentFilterId  = $("#departmentFilterId").val();

        let queryString = $("#queryString").val();

        if (queryString !== "" && queryString !== undefined) {
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("c");
            queryParams.delete("s");
            queryParams.delete("d");
            queryParams.set("cld",1);
            queryParams.delete("skip");
            $clearAllFiltersLnk.attr('href','calendar?' + queryParams);

            let restParams = new URLSearchParams(window.location.search);
            //hide clear-filters if only sd,ed,view params remain
            restParams.delete("view");
            restParams.delete("sd");
            restParams.delete("ed");
            restParams.delete("cld");

            if (restParams.toString() === "") {
                $clearAllFiltersLnk.hide();
            } else {
                $clearAllFiltersLnk.show();
            }
        }
        else {
            $clearAllFiltersLnk.hide();
        }

        let courseFilterText = $("#courseFilterText").val();
        if (courseFilterId !== undefined && courseFilterId != null && courseFilterId  !== '') {
            $('#course-dd-header').html("<span class='far fa-times-circle'></span> | Μάθημα: " + courseFilterText);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("c");
            queryParams.delete("skip");

            $("#clear-co-filter").attr('href','calendar?' + queryParams);
            $("#course-filter").show();
            $("#courseCanvasLink").hide();
        }

        let staffFilterText = $("#staffMemberFilterText").val();
        if (staffFilterId !== undefined && staffFilterId != null && staffFilterId !== '') {
            $('#staff-dd-header').html("<span class='far fa-times-circle'></span> | Διδάσκων: " + staffFilterText);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("s");
            queryParams.delete("skip");

            $("#clear-sm-filter").attr('href','calendar?' + queryParams);
            $("#staff-filter").show();
            $("#staffCanvasLink").hide();
        }

        let departmentFilterText = $("#departmentFilterText").val();
        if (departmentFilterId !== undefined && departmentFilterId != null && departmentFilterId !== '') {
            $('#department-dd-header').html("<span class='far fa-times-circle'></span> | Τμήμα: " + departmentFilterText);
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.delete("d");
            queryParams.delete("s");    //for live-app only: require department filter to show results
            queryParams.delete("c");    //for live-app only: require department filter to show results
            queryParams.set("cld","1")
            queryParams.delete("skip");

            $("#clear-dt-filter").attr('href','calendar?' + queryParams);
            $("#department-filter").show();
            $("#depCanvasLink").hide();
        }

    }

})();