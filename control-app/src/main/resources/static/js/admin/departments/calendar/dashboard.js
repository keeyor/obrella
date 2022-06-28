/*jshint esversion: 6 */
(function () {
    'use strict';

    window.dashboard = window.dashboard || {};
    dashboard.broker = $({});

    dashboard.siteurl       	= "";
    dashboard.selected_dep      = "";
    dashboard.institution_id   	= "";
    dashboard.institution_name  = "";
    dashboard.selected_year 	= "";

    dashboard.init = function () {

        //PRESERVE TAB AFTER RELOAD
        // use HTML5 localStorage object to save some parameter for the current tab locally in the browser and get it back to make the last active tab selected on page reload.
        //ON CHANGE TAB EVENT
        $('a[data-coreui-toggle="tab"]').on('show.coreui.tab', function(e) {
            localStorage.setItem('activeTab', $(e.target).attr('href'));
            //console.log($(e.target).attr('href'));
        });
        //LOAD ACTIVE TAB
        var activeTab = localStorage.getItem('activeTab');
        if(activeTab){
            $('#myTab a[href="' + activeTab + '"]').tab('show');
        }

        dashboard.broker.InitGenericControls();
    	dashboard.siteurl     = dashboard.broker.getRootSitePath();
    	dashboard.institution_id 	= $("#institutionId").val();
    	dashboard.institution_name  = $("#institutionName").val();

        dashboard.system.init();
        dashboard.department.init();
        dashboard.department.loadDepartmentsDropdownList();
        dashboard.study.init();

        dashboard.selected_dep = $("#department_id").val();
        if (dashboard.selected_dep !== "") {
            dashboard.selected_year = $("#currentAcademicYear").val();
            dashboard.system.getAvailableYearList(dashboard.selected_year);

            $("#periods-panel").show();     //main panel
            $("#department_card").show();   // read department period card
            $("#argies_card").show();

            dashboard.department.loadDepartmentTables(); // Load OR Reload logic included in target function including study programs list
            console.log("Current Year: " + dashboard.selected_year + " - Loaded Department: " + dashboard.selected_dep);
            dashboard.study.refreshStudyList(dashboard.institution_id, dashboard.selected_dep);
            dashboard.study.clearStudyDataTable();
        }
        else {
            $("#periods-panel").hide(); //main panel
        }

    };

    $(document).ready(function () {

        dashboard.init();

        dashboard.broker.on('afterSelect.year refresh.page reload.page', function (event, message) {
            dashboard.selected_year = message.year;
            if (event.type === "reload") {
                console.log("Page RELOAD with default year:" + dashboard.selected_year);
                dashboard.system.getAvailableYearList(dashboard.selected_year);
            }
            else {
                //Load new year
                if (dashboard.selected_year !== "" && dashboard.selected_year != null) {    // just checking
                    if (dashboard.selected_dep !== "") {
                        dashboard.department.loadDepartmentTables();  // Load OR Reload logic included in target function including study programs list
                        console.log("Current Year: " + dashboard.selected_year + " - Loaded Department: " + dashboard.selected_dep);

                        let selected_study =  message.study;
                        //Get StudyPrograms Lists
                        if (selected_study === "") {
                            dashboard.study.refreshStudyList(dashboard.institution_id, dashboard.selected_dep);
                            dashboard.study.clearStudyDataTable();
                        }
                        else {
                            dashboard.study.LoadStudyTables(dashboard.institution_id,dashboard.selected_dep,selected_study,dashboard.selected_year);
                        }
                    }
                }
            }
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
    
    dashboard.broker.InitGenericControls = function() {
        alertify.defaults.transition = "slide";
        alertify.defaults.theme.ok = "btn blue-btn-wcag-bgnd-color text-white";
        alertify.defaults.theme.cancel = "btn red-btn-wcag-bgnd-color text-white";
        alertify.defaults.theme.input = "form-control";
        alertify.set('notifier','position', 'top-center');
    };

})();