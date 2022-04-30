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
        dashboard.department.init();
        dashboard.classroom.init();

        loader.initialize();

        dashboard.dtLanguageGr = {
            "search": "Αναζήτηση",
            "lengthMenu": "Εμφάνιση _MENU_ εγγραφών",
            "emptyTable": "Δεν βρέθηκαν εγγραφές",
            "zeroRecords": "Η αναζήτηση δεν βρήκε εγγραφές",
            "sInfo": "Εμφάνιση από _START_ έως _END_ από _TOTAL_",
            "infoEmpty": "Εμφάνιση από 0 σε 0 από 0 εγγραφές",
            "infoFiltered": "(Φίλτρο από _MAX_ συνολικές εγγραφές)",
            "loadingRecords": "Φόρτωση...",
            "processing": "Επεξεργασία...παρακαλώ περιμένετε",
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
                "cells": {
                    "1": "1 cell selected",
                    "_": "%d cells selected"
                },
                "columns": {
                    "1": "1 column selected",
                    "_": "%d columns selected"
                }
            }
        } // dtLanguageGr

        alertify.defaults.transition = "slide";
        alertify.defaults.theme.ok = "btn blue-btn-wcag-bgnd-color text-white";
        alertify.defaults.theme.cancel = "btn red-btn-wcag-bgnd-color text-white";
        alertify.defaults.theme.input = "form-control";
        alertify.set('notifier','position', 'top-right');
    };

    $(document).ready(function () {
   	
       dashboard.init();

        $(document).on( 'preInit.dt', function (e, settings) {
            loader.showLoader();
        } );

       dashboard.broker.on('afterSelect.year refresh.page reload.page', function (event, message) {

    	   dashboard.selected_year = message.year;
           if (event.type === "reload") {
        	   dashboard.system.getAvailableYearList(dashboard.selected_year);
           }
           else {
               if ( ! $.fn.DataTable.isDataTable( '#table_timetable_events' ) ) {
                   dashboard.calendar.initTimeTableLectureEDT(dashboard.selected_year);
               }
               else {
                   dashboard.calendar.reloadTimeTableLectureEDT();
               }

           }
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
            return "Τακτική";
        }
        else if (data === "onetime") {
            return "Έκτακτη";
        }
        else return "";
    };
    dashboard.broker.selectDayOfWeek = function(data) {

        if (data === "MONDAY") {
            return language.MONDAY;
        }
        else if (data === "TUESDAY") {
            return language.TUESDAY;
        }
        else if (data === "WEDNESDAY") {
            return language.WEDNESDAY;
        }
        else if (data === "THURSDAY") {
            return language.THURSDAY;
        }
        else if (data === "FRIDAY") {
            return language.FRIDAY;
        }
        else if (data === "SATURDAY") {
            return language.SATURDAY;
        }
        else if (data === "SUNDAY") {
            return language.SUNDAY;
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
    dashboard.broker.getCurrentPage = function () {

        let path = window.location.pathname;

        return path.split("/").pop();
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
                text = "Σχολή";
                break;
            case "department":
                text = "Τμήμα";
                break;
            case "classroom":
                text = "Άιθουσα/Χώρος";
                break;
            case "repeat":
                text = "Επανάληψη";
                break;
            case "dow":
                text = "Ημέρα";
                break;
            case "period":
                text = "Περίοδος";
                break;
            case "editor":
                text = "Εμφάνιση ";
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
})();