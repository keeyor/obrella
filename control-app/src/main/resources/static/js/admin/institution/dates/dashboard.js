/*jshint esversion: 6 */
(function () {
    'use strict';

    window.dashboard = window.dashboard || {};
    dashboard.broker = $({});

    dashboard.siteurl       	= "";
    dashboard.institution   	= "";
    dashboard.institution_name  = "";
    dashboard.selected_year 	= "";
    dashboard.calendar_id       = "";
    
    dashboard.modal_year = null; 
    dashboard.modal_header_img_institution= null; 
    dashboard.modal_header_img_department= null; 
    dashboard.modal_header_img_study= null; 
    
    dashboard.modal_header_institution= null; 
    dashboard.modal_header_department= null; 
    dashboard.modal_header_study= null; 
    
    dashboard.modal_department_title= null; 
    dashboard.modal_study_title= null; 
    
    dashboard.modal_period_error_messages= null; 
    dashboard.modal_inherit = null; 
    
    dashboard.modal_update_button = null;
    
    dashboard.editPeriodModal = null;
    dashboard.editPauseModal = null;
    
    dashboard.system_periods_set = 0;
    
    dashboard.init = function () {
    	
    	dashboard.siteurl     = dashboard.broker.getRootSitePath();
    	dashboard.institution 		= $("#institutionId").val();
    	dashboard.institution_name  = $("#institutionName").val();
      
    	dashboard.modal_year 		=	$("#modal_year");
    	dashboard.modal_header_img_institution 	= $("#modal_header_img_institution");
    	dashboard.modal_header_img_department  	= $("#modal_header_img_department");
    	dashboard.modal_header_img_study 		= $("#modal_header_img_study");
	     
    	dashboard.modal_header_institution 	= $("#modal_institution");
    	dashboard.modal_header_department 	= $("#modal_department");
    	dashboard.modal_header_study 		= $("#modal_study");
	     
    	dashboard.modal_department_title = $("#modal_department_title");
    	dashboard.modal_study_title		 = $("#modal_study_title");
	    
    	dashboard.modal_period_error_messages = $("#PeriodErrorMessages");
    	dashboard.modal_inherit = $("#modal_inherit");
    	
    	dashboard.modal_update_button = $("#updatePeriodsButton");
    	
    	dashboard.editPeriodModal = $("#editPeriodModal");
        dashboard.editPauseModal = $("#editPauseModal");

    	dashboard.system.init();
        dashboard.systema.init();
        dashboard.department.init();
        dashboard.study.init();
        dashboard.modal.init();
        dashboard.modala.init();

        alertify.defaults.transition = "slide";
        alertify.defaults.theme.ok = "btn btn-primary";
        alertify.defaults.theme.cancel = "btn btn-danger";
        alertify.defaults.theme.input = "form-control";
        alertify.set('notifier','position', 'top-center');
    };

    $(document).ready(function () {
   	
       dashboard.init();
       
       dashboard.broker.on('afterSelect.year refresh.page reload.page', function (event, message) {

    	   dashboard.selected_year = message.year;
           if (event.type === "reload") {
        	   dashboard.system.getAvailableYearList(dashboard.selected_year);
           }
           else {
                console.log("REFERSH PAGe ... with year:" + dashboard.selected_year);
        	   	let selected_department =  message.department;
        	   	let selected_study =  message.study;

        	   	if (dashboard.selected_year !== "" && dashboard.selected_year != null) {
                    if ( ! $.fn.DataTable.isDataTable( '#table_system' ) ) {
                        dashboard.system.initDataTable(dashboard.institution, dashboard.selected_year);
                    }
                    else {
                        dashboard.system.reloadDataTable(dashboard.institution, dashboard.selected_year);
                    }
                    if ( ! $.fn.DataTable.isDataTable( '#table_system_a' ) ) {
                        dashboard.systema.initDataTable(dashboard.institution, dashboard.selected_year);
                    }
                    else {
                        dashboard.systema.reloadDataTable(dashboard.institution, dashboard.selected_year);
                    }

                   if (event.type === "afterSelect") {
                        dashboard.department.triggerClear();
                    }
                    //!Important: Check study before department!!!
                    else if (event.type === "refresh" && selected_study !== undefined && selected_study !== "") {
                        dashboard.study.setVal(message.year, selected_department, selected_study);
                    } else if (event.type === "refresh" && selected_department !== "") {
                        dashboard.department.setVal(message.year, selected_department);
                    }
                }
           }
      });

       dashboard.broker.on('afterSelect.department', function (event, message) {
  
    	  // console.log("AFTER DEP SELECT:" + message.value);
    	  let departmentId = message.value;
    	  dashboard.study.refreshData(dashboard.institution, departmentId);
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
 
 

})();