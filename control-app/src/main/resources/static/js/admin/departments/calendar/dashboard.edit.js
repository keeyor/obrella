/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.edit = dashboard.edit || {};

    let $PeriodErrorMessages;
    let $UpdatePeriodsButton;
    let $PeriodStatusMessages;
    let $periodsDataTable;
    let $table;
    let tableDT;

    let pre_meta;
    
    dashboard.edit.init = function () {
    	
		$PeriodErrorMessages  = $(".PeriodErrorMessages");
		$PeriodStatusMessages = $(".PeriodMessages");
    	$UpdatePeriodsButton  = $(".updatePeriodsButton");
    	$periodsDataTable	  = $("#table_modal");
    	
    	
    	/*EVENTS*/
    	$UpdatePeriodsButton.on('click', function() {

    		loader.showLoader();
    		let scope = $("#modal_scope").val();
    		if (scope === "system" || scope === "") {
    			   let refId = dashboard.institution;
    			   let inherit = "0";
    			   let dataJSON = getTableDataAsJSON($periodsDataTable, refId,inherit);
				   let url = dashboard.siteurl + '/api/v1/institution/' + refId + '/calendar/update/' + dashboard.selected_year;
				   postCreateUpdatePeriods(url, dataJSON,scope,  dashboard.selected_year, "","" );
    		}
    		else if (scope === "department") {
 			   		let refId = dashboard.department.departmentId;
 			   		let inherit = "0";
 			   		let dataJSON = getTableDataAsJSON($periodsDataTable, refId,inherit);
					 let url = dashboard.siteurl + '/api/v1/department/' + refId + '/calendar/update/' + dashboard.selected_year;
	    	    	 postCreateUpdatePeriods(url, dataJSON, scope, dashboard.selected_year, dashboard.department.selectedDepartmentId, "");
    		}
    		if (scope === "study") {
 			   		let refId = dashboard.study.studyId;
 			   		let inherit = "0";
					$periodsDataTable	  = $("#table_study_modal");
    				let dataJSON = getTableDataAsJSON($periodsDataTable, refId,inherit);
					let url = dashboard.siteurl + '/api/v1/programs/' + refId + '/calendar/update/' + dashboard.selected_year;
    				postCreateUpdatePeriods(url, dataJSON, scope, dashboard.selected_year, dashboard.department.selectedDepartmentId, dashboard.study.studyId);
    		}
    	});
    	dashboard.editPeriodModal.on('show.coreui.modal', function() {
    		setMessage($PeriodStatusMessages,'alert alert-success alert-dismissable invisible', ' ');
    	});
    };

    dashboard.edit.initDataTable = function (year,institutionId, departmentId, studyId) {

    	$table = $("#table_department_edit");

    	let  url;
    	if (departmentId === "") {
    		url = dashboard.siteurl + '/api/v1/dt/institution/' + institutionId + '/calendar/' + year;
    	}
    	else {
    		if (studyId === "") {
				url = dashboard.siteurl + '/api/v1/dt/institution/' + institutionId + '/department/' + departmentId + '/calendar/' + year;
			}
    		else {
				url = dashboard.siteurl + '/api/v1/dt/institution/' + institutionId + '/department/' + departmentId + '/program/' + studyId + '/calendar/' + year;
				$table = $("#table_study_edit");
			}
    	}
		pre_meta = "st";
    	if (studyId === "") {
			pre_meta = "dep";
		}

		tableDT= $table.DataTable({
	        "bProcessing": false,
	        "bDestroy": true,
	        "bFilter": false,
	        "bPaginate": false,
            "oLanguage": {
                "sSearch": "<small>Αναζήτηση</small>"
            },
            "order": [[1, 'asc']],  
            "ajax":  { 
    			"url":  url,
    			"dataSrc":  "data.periods.period"
            },
            "columns": [
                { "mData": null , "sWidth": "40px", "bSortable": false, "bVisible":false },
                { "mData": "startDate", "sWidth": "0px", "bSortable": true, "bVisible":false },	// sorting, actual database data
                { "mData": "endDate", "sWidth": "0px", "bSortable": true, "bVisible":false },	// sorting, actual database data
                { "mData": "name", "sWidth": "380px", "bSortable": false},
                { "mData": "startDate", "sWidth": "180px", "bSortable": false },				// display data formatted
                { "mData": "endDate", "sWidth": "200px", "bSortable": false }					// display data formatted
            ],
            "columnDefs": [
                	{
                		"aTargets": [1],
                		"name": "startDateData",
	                    "render": function (data) {
                			if (data !== null) {return data;}
                			else {return "";}
	                    } 
                	},
                	{
                		"aTargets": [2],
                		"name": "endDateData",
	                    "render": function (data) {
							if (data !== null) {return data;}
							else {return "";}
	                    } 
                	},
                	{
                		"aTargets": [3],
                		"name": "name",
	                    "render": function (data) {
							if (data !== null) {return dashboard.broker.selectPeriod(data);}
							else {return "";}
	                    } 
                	},
                	{
                		"aTargets": [4],
                		"name": "startDate",
                		"render": function (data,type,row,meta) {
							return '<div class="input-group date modal-start-date" data-row="' + meta.row + '" id="' + pre_meta + '-div-startDate-' + meta.row + '"  >' +
								'<span class="input-group-addon input-group-text">' +
								'<i class="fas fa-calendar-alt"></i>' +
								'</span>' +
								'<input class="form-control" value=""  />' +
								'</div>';
	                    }
                	},
                	{
                		"aTargets": [5],
                		"name": "endDate",
                		"render": function (data,type,row, meta) {
							return '<div class="input-group date modal-end-date" data-row="' + meta.row + '" id="' + pre_meta + '-div-endDate-' + meta.row + '"  >' +
								'<span class="input-group-addon input-group-text">' +
								'<i class="fas fa-calendar-alt"></i>' +
								'</span>' +
								'<input class="form-control" value=""  />' +
								'</div>';
	                    }
                	},
            ],
            "initComplete": function( settings, json ) {
				 set_display_results(json);
              }
        }); // DataTable init

		tableDT.on('order.dt search.dt', function () {
			tableDT.column(0, { search: 'applied', order: 'applied' }).nodes().each(function (cell, i) {
                cell.innerHTML = i + 1;
            });
        }).draw();
    };

	function  set_display_results(json) {
		for (var i=0; i < json.data.periods.period.length; i++) {
			let $startDateElementInPosI = $(`#${pre_meta}-div-startDate-${i}`);
			$startDateElementInPosI.datepicker({
				format: "dd MM yyyy",
				todayBtn: false,
				language: "el",
				autoclose: true,
				todayHighlight: false
			});

			if (json.data.periods.period[i].startDate !== "") {
				let d = new Date(json.data.periods.period[i].startDate);
				let curr_date = d.getDate();
				let curr_month = d.getMonth() + 1; //Months are zero based
				let curr_year = d.getFullYear();

				let startdate = curr_date + "/" + curr_month + "/" + curr_year;
				$startDateElementInPosI.datepicker("setDate", startdate);
			}
			else {
				$startDateElementInPosI.datepicker("setDate", "");
			}

			let $endDateElementInPosI = $(`#${pre_meta}-div-endDate-${i}`);
			$endDateElementInPosI.datepicker({
				format: "dd MM yyyy",
				todayBtn: false,
				language: "el",
				autoclose: true,
				todayHighlight: false
			});
			if (json.data.periods.period[i].endDate !== "") {
				let d = new Date(json.data.periods.period[i].endDate);
				let curr_date = d.getDate();
				let curr_month = d.getMonth() + 1; //Months are zero based
				let curr_year = d.getFullYear();

				let enddate = curr_date + "/" + curr_month + "/" + curr_year;
				$endDateElementInPosI.datepicker("setDate", enddate);
			}
			else {
				$endDateElementInPosI.datepicker("setDate", "");
			}
		}

		$(".modal-start-date").on('changeDate', function(ev){

			let row_no  = $(this).data("row");
			$(this).addClass('has-warning');

			let changed_date = moment(ev.date).format('YYYY-MM-DD');
			tableDT.cell({row: row_no, column: 1}).data(changed_date).draw();
			dashboard.edit.checkTableDates(tableDT);
		});
		$(".modal-end-date").on('changeDate', function(ev){

			if (ev.date) {
				let row_no  = $(this).data("row");
				$(this).addClass('has-warning');

				let changed_date = moment(ev.date).format('YYYY-MM-DD');
				tableDT.cell({row: row_no, column: 2}).data(changed_date).draw();
				dashboard.edit.checkTableDates(tableDT);
			}
		});

	}

    dashboard.edit.checkTableDates = function(tableDT) {
    		
    	var data = tableDT.rows().data();
    	var message = "<div><b>Εντοπίστηκαν προβλήματα στη φόρμα</b><div><ul>";
    	var errors = 0;
    	   //Check overlap between start and end date of the same period
    	   for (let c=0; c < data.length; c++) {
    		   let  _r = data[c];
    		   if (_r.startDate !== "" && _r.endDate !== "") {
	    		   let startDate_m = moment(_r.startDate); 
	    		   let endDate_m = moment(_r.endDate);
	    		   if (endDate_m.isBefore(startDate_m)) {
	    			    errors = 1;
	    			    message += "<li><b>" + dashboard.broker.selectPeriod(_r.name) + ":</b> Η καταληκτική ημερομηνία είναι <b>πρίν</b> την αρχική: " + "</li>";
	    		   }
    		   }
    	   }
    	 //Check overlap between end date and start date of the next period
    	   for (let c=0; c < data.length-1; c++) {
    		   let  _r1 = data[c];
    		   let  _r2 = data[c+1];
    		   
    		   if (_r2.startDate !== "" && _r1.endDate !== "") {
	    		   let endDate_m1 = moment(_r1.endDate).add(1 ,'days');  
	    		   let startDate_m2 = moment(_r2.startDate);

	    		   if (startDate_m2.isBefore(endDate_m1)) {
	    			    errors = 1;
	    			    message += "<li><b>" + dashboard.broker.selectPeriod(_r2.name) + ":</b>  H περίοδος αρχίζει <b>πρίν</b>  το τέλος της προηγούμενης " + "</li>";
	    			   
	    		   }
	    		 //Check for gaps between periods
	    		   if (	startDate_m2.diff(endDate_m1, 'days')  > 0) {
	    			   errors = 1;
	   			       message += "<li><b>" + dashboard.broker.selectPeriod(_r2.name) + ":</b>  Υπάρχουν <b>κενές μέρες πριν</b> την αρχή της περιόδου " + "</li>";
	    		   }		   
    		   } 
    	   }
    	   message += "</ul>";
		   if (errors === 1) {
			   setMessage($PeriodErrorMessages,'alert alert-danger visible', message);
			   $UpdatePeriodsButton.attr("disabled", true);
		   }
		   else {
			   setMessage($PeriodErrorMessages,'alert alert-danger invisible', " ");
			   $UpdatePeriodsButton.attr("disabled", false);
		   }
    };
    
    function setMessage($element, attributes, message) {
    	
    	$element.attr('class', attributes);
    	$element.html(message); 
    }
    
    function getTableDataAsJSON($table,refId,inherit) {
    	
		var period_list = [];
		var periods = {};
			periods.refId = refId;
			periods.inherit = inherit;
		
    	var data = $table.DataTable().rows().data();
    	   for (let c=0; c < data.length; c++) {
    		   let  _r 		 = data[c];
    		   let name 	 = _r.name;
    		   let startDate = _r.startDate;
    		   let endDate 	 = _r.endDate;

    		   var period = {"name":name, "endDate" : endDate, "startDate": startDate};

    		   period_list.push(period);
    	   }
    	   periods.period = period_list;

		//console.log("POST:" + dataJSON);
    	   
    	   return JSON.stringify(periods);
    }

	function postCreateUpdatePeriods(postURL, dataJSON, scope, year, departmentId, studyId) {

		$.ajax({
			url: postURL,
			type:"POST",
			contentType: "application/json; charset=utf-8",
			data: dataJSON,
			async: true,
			success: function() {
				loader.hideLoader();
				setMessage($PeriodStatusMessages,'alert alert-success alert-dismissable visible',
					'<b><i class="fas fa-thumbs-up me-1"></i>Επιτυχής Ενημέρωση<b>');
				setTimeout(function() {
					setMessage($PeriodStatusMessages,'alert alert-success invisible', " ");
					$UpdatePeriodsButton.attr("disabled", true);
					if (scope === "department") {
						$("#department_edit_card").hide();
						$("#department_card").show();
					}
					else if (scope === "study") {
						$("#study-edit-pane").hide();
						$("#study-pane").show();
					}
				}, 1500);

				let message = {msg: "Academic Calendar Updated!", year: dashboard.selected_year, department: departmentId, study: studyId};
				dashboard.broker.trigger('refresh.page', [message]);
			},
			error : function(err_message) {
				loader.hideLoader();
				//## NEW WO MODAL
				if (scope === "department") {
					$("#department_edit_card").hide();
					$("#department_card").show();
				}
				else if (scope === "study") {
					$("#study-edit-pane").hide();
					$("#study-pane").show();
				}
				//#
				setMessage($PeriodStatusMessages,'alert alert-danger alert-dismissable visible',
					'<b><i class="fas fa-exclamation-triangle me-1"></i>Πρόβλημα Συστήματος. Επικοινωνήστε με το διαχειριστή<b>');
			}
		});
	}

})();