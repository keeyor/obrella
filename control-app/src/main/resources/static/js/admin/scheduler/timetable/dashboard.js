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

        dashboard.lectab.init();

        dashboard.department.init();
        dashboard.staffmembers.init();
        dashboard.course.init();
        dashboard.type.init();

        alertify.defaults.transition = "slide";
        alertify.defaults.theme.ok = "btn blue-btn-wcag-bgnd-color text-white";
        alertify.defaults.theme.cancel = "btn red-btn-wcag-bgnd-color text-white";
        alertify.defaults.theme.input = "form-control";
        alertify.set('notifier','position', 'top-right');

    };

    $(document).ready(function () {
   	
       dashboard.init();

       //init short-list field
        $(".offcanvas").on('show.coreui.offcanvas',function() {
            $(".short-list").val("");
        });

        $(".short-list").on("keyup",function() {

            let what = $(this).data("target");
            let value = $(this).val();
            if (value.length < 2) {
                $('#' + what +' > li').show();
            }
            else {
                value = value.toLowerCase().replace(/\b[a-z]/g, function(letter) {
                    return letter.toUpperCase();
                });
                $('#' + what + '>li').slideUp().filter( function() {
                    return $(this).text().toLowerCase().indexOf(value) > -1
                }).stop(true).fadeIn();
            }
        });

        //load specific tab when returning from edit
        let url = document.location.toString();
        if (url.match('#')) {
            $('.nav-tabs a[href="#' + url.split('#')[1] + '"]').tab('show');
        }
        //Change hash for page-reload
        $('.nav-tabs a[href="#' + url.split('#')[1] + '"]').on('shown', function (e) {
            window.location.hash = e.target.hash;
        });

        $('a[data-coreui-toggle="tab"]').on('shown.coreui.tab', function (e) {
            let activated_tab = e.target;
            if (activated_tab.id.includes("events")) {
                $("#lecture-filters").hide();
                $("#event-filters").show();

            }
            else {
                $("#lecture-filters").show();
                $("#event-filters").hide();
            }
        });


       $("#lecture-filters").on( "click", ".filter-item", function(event) {
            let filter = $(this).data("filter");
            let itemId = $(this).data("target");
            let itemName = $(this).text();
            let message = {msg: "Filter:" + filter + " Selected!", filter: filter, id: itemId, value: itemName};
            dashboard.broker.trigger('filter.select', [message]);

            let canvas_element = $(this).parents(".offcanvas");
            canvas_element.offcanvas('hide');
        });

        $("#event-filters").on( "click", ".filter-item", function(event) {
            let filter = $(this).data("filter");
            let itemId = $(this).data("target");
            let itemName = $(this).text();
            let message = {msg: "Filter:" + filter + " Selected!", filter: filter, id: itemId, value: itemName};
            dashboard.broker.trigger('filter.select', [message]);

            let canvas_element = $(this).parents(".offcanvas");
            canvas_element.offcanvas('hide');
        });

        $(".clear-filter").on("click", "a", function(event) {
            let target = $(this).data("target");
            let message = {msg: "Filter De-Selected!", filter: target, id: "", value: ""};
            dashboard.broker.trigger('filter.select', [message]);
            event.preventDefault();
        });
        $("#clear-all-filters").on("click", function(event) {
            dashboard.broker.clearFilters();
            event.preventDefault();
        });

       dashboard.broker.on("filter.select", function (event, message) {
            //console.log(message);
            let text = dashboard.broker.getTextForFilter(message.filter);
            dashboard.broker.toggleFilter(message,text);
        });

       dashboard.broker.on('afterSelect.year refresh.page reload.page', function (event, message) {

    	   dashboard.selected_year = message.year;
           if (event.type === "reload") {
        	   dashboard.lectab.getAvailableYearList(dashboard.selected_year);
           }
           else {
               if ( ! $.fn.DataTable.isDataTable( '#table_timetable_lectures' ) ) {
                   dashboard.lectab.initTimeTableLectureDT();
               }
               else {
                   dashboard.lectab.reloadTimeTableLectureDT();
               }
               if ( ! $.fn.DataTable.isDataTable( '#table_timetable_events' ) ) {
                   dashboard.evtab.initTimeTableLectureEDT(dashboard.selected_year);
               }
               else {
                   dashboard.evtab.reloadTimeTableLectureEDT();
               }
           }

      });
       dashboard.broker.on('afterSelect.department afterSelect.repeat afterSelect.dayOfWeek', function (event, message) {
    	  //console.log("Filter Changed:" + message.msg + " to: " + message.value);
    	  dashboard.lectab.reloadTimeTableLectureDT();
    	  event.preventDefault();
      });

       dashboard.broker.on('ShowInstantMessage', function (event, message) {
           dashboard.broker.showInstantMessage(message.type ,message.val);
      });
       
    }); //document ready end

 
    dashboard.broker.selectPeriod = function(data) {
    	
    	if (data === "winter") {
    		return language.winter;
    	}
    	else if (data === "intervening") {
    		return language.intervening;
    	}
    	else if (data === "spring") {
    		return language.spring;
    	}
    	else if (data === "summer") {
    		return language.summer;
    	}
    	else return "";
    };
    dashboard.broker.selectRepeat = function(data) {

        if (data === "regular") {
            return "??????????????";
        }
        else if (data === "onetime") {
            return "??????????????";
        }
        else return "";
    };
    dashboard.broker.selectDayOfWeek = function(data) {

        if (data === "MONDAY") {
            return language.MONDAY;
        }
        else if (data === "TUESDAY") {
            return language.TUESDAY
        }
        else if (data === "WEDNESDAY") {
            return language.WEDNESDAY
        }
        else if (data === "THURSDAY") {
            return language.THURSDAY
        }
        else if (data === "FRIDAY") {
            return language.FRIDAY
        }
        else if (data === "SATURDAY") {
            return language.SATURDAY
        }
        else if (data === "SUNDAY") {
            return language.SUNDAY
        }
        else return "";
    };

    dashboard.broker.selectNumberedDayOfWeek = function(data) {

        if (data === "MONDAY") {
            return 1;
        }
        else if (data === "TUESDAY") {
            return 2;
        }
        else if (data === "WEDNESDAY") {
            return 3;
        }
        else if (data === "THURSDAY") {
            return 4;
        }
        else if (data === "FRIDAY") {
            return 5;
        }
        else if (data === "SATURDAY") {
            return 6;
        }
        else if (data === "SUNDAY") {
            return 7;
        }
        else return "";
    };


    dashboard.broker.select_eStatus = function(data) {
        if (data === "past") {
            return "??????????????????????????"
        }
        else if (data === "future") {
            return "??????????????????????";
        }
        else if (data === "today") {
            return "??????????????????"
        }
        else return "";
    };

    dashboard.broker.showInstantMessage = function(type, val) {

        //Override alertify defaults
        alertify.set('notifier','position', 'top-right');

        switch (type) {
            case "alert-success":
                alertify.success(val);
                break;
            case "alert-danger":
                alertify.error(val);
                break;
            case "alert-warning":
                alertify.warning(val);
                break;
            case "alert-info":
                alertify.info(val);
                break;
        }
    };

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

    dashboard.broker.getServerUrl = function () {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);

        return _location.substring(0, applicationNameIndex);

    };

    dashboard.broker.toggleFilter = function(message, text) {
        let filter_type     = message.filter;
        let filter_value    = message.id;
        let $filter_id      = "#" + filter_type + "_filter";
        let $filter_name    = "#" + filter_type + "_filter_name";

        $($filter_id).val(filter_value);
        $($filter_name).val(message.value);

        if (filter_type === "department") {         //clear staff and course filters
            dashboard.broker.initFilter("staff");
            dashboard.broker.initFilter("course");
          //  $("#offcanvasDep").hide();
        }
        if (filter_type === "staff") {
            dashboard.broker.initFilter("course");
            $("#course_load").addClass("disabled text-muted");
        }
        if (filter_type === "course") {
            dashboard.broker.initFilter("staff");
            $("#staff_load").addClass("disabled text-muted");
        }
        if (filter_type === "school") {
            dashboard.department.filterSchoolDepartments(dashboard.institution, filter_value);
            dashboard.broker.initFilter("department");
        }
        if (filter_type === "repeat" && filter_value === "onetime") {
            dashboard.broker.initFilter("dow");
            $("#dow_load").addClass("disabled text-muted");
        }
        else {
            $("#dow_load").removeClass("disabled text-muted");
        }
        // handle clear filter event
        if (filter_value === null || filter_value === "") {
            dashboard.broker.initFilter(filter_type);
        }
        if (filter_type !== "estatus") { //filters for events go here!
           dashboard.lectab.reloadTimeTableLectureDT();
        }
        else {
           dashboard.evtab.reloadTimeTableLectureEDT();
        }
    };

    dashboard.broker.initFilter = function(filter_type) {
        let $filter_id    =  "#" + filter_type + "_filter";
        let $filter_name  =  "#" + filter_type + "_filter_name";
        let $filter_clear =  "#" + filter_type + "_clear";
        let $filter_load  =  "#" + filter_type + "_load";
        let text = dashboard.broker.getTextForFilter(filter_type);

        if (filter_type === "staff") {
            $("#course_load").removeClass("disabled text-muted");
        }
        if (filter_type === "course") {
            $("#staff_load").removeClass("disabled text-muted");
        }

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
            case "repeat":
                text = "??????????";
                break;
            case "dow":
                text = "??????????";
                break;
            case "period":
                text = "????????????????";
                break;
            case "type":
                text = "??????????";
                break;
            case "estatus":
                text = "?????????????????? ??????????????????";
                break;
            case "staff":
                text = '<i class="fas fa-user-tag mr-1"></i> ???????????????? <span class="fas fa-sort-down"></span>';
                break;
            case "course":
                text = '<span class="fas fa-book mr-1"></span> ???????????? <span class="fas fa-sort-down"></span>';
                break;
        }
        return text;
    };

    dashboard.broker.clearFilters = function() {

        let filters = ["staff","course","period","department","repeat", "dow"];

        for (let i = 0; i < filters.length; i++) {
            let $filter_id    = "#" + filters[i] + "_filter";
            let $filter_name  = "#" + filters[i] + "_filter_name";
            let $filter_clear = "#" + filters[i] + "_clear";
            let $filter_load  = "#" + filters[i] + "_load";

            $($filter_id).val("");
            $($filter_name).val("");
            $($filter_clear).hide();

            //let text = dashboard.courses.getTextForFilter(filters[i]);
            //let $filter= $($filter_load);
            //$filter.html(text);
        }
        dashboard.lectab.reloadTimeTableLectureDT();
    };
})();