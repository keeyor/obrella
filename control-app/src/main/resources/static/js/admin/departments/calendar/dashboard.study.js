/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.study = dashboard.study || {};

    let Study_DT;
	let Edit_DT;
	let $periodMessages;
	let $periodErrorMessages;
	let $updatePeriodsButton;
	let scope;
	let $view_pane;
	let $edit_pane;
	let pre_meta = "st";

    dashboard.study.init = function () {

		$edit_pane = $("#study-edit-pane");
		$view_pane = $("#study-pane");
		scope = "study";
		$periodMessages = $("#s-PeriodMessages");
		$periodErrorMessages = $("#s-PeriodErrorMessages");
		$updatePeriodsButton = $("#s-UpdatePeriods");

		let $select_study = $("#study_select2");
		$select_study.on('select2:select', function (e) {
			let data = e.params.data;
			let sel_study = data.id;
			dashboard.study.LoadStudyTables(dashboard.institution_id,dashboard.selected_dep,sel_study,dashboard.selected_year);
			console.log("Reload Study Table for:" + sel_study);
		});

		$("#study-button-edit").on('click', function() {
			let sel_study = $("#study_select2").val();

			dashboard.study.editStudyPeriodsDataTable( dashboard.selected_year, dashboard.institution_id,dashboard.selected_dep,sel_study);
			$edit_pane.show();
			$view_pane.hide();
		});

		$("#study-edit-close").on('click',function(e){
			setMessage($periodErrorMessages,'alert alert-danger invisible', " ");
			$updatePeriodsButton.attr("disabled", false);
			$view_pane.show();
			$edit_pane.hide();
		});

		$updatePeriodsButton.on('click',function(){
			let refId =  $("#study_select2").val();
			let inherit = "0";
			let $study_periods_dt = $("#table_study_edit");
			let dataJSON = getTableDataAsJSON($study_periods_dt, refId,inherit);
			let url = dashboard.siteurl + '/api/v1/programs/' + refId + '/calendar/update/' + dashboard.selected_year;
			postCreateUpdatePeriods(url, dataJSON, scope, dashboard.selected_year, dashboard.selected_dep, refId);
		})
    };

    dashboard.study.refreshStudyList = function (institutionId, departmentId) {
    	getStudyPeriodList(institutionId, departmentId);
    };

	function getStudyPeriodList(institutionId, departmentId) {

		let $select_study = $("#study_select2");
		let select_study = "Επιλέξτε Πρόγραμμα Σπουδών";

		$select_study.empty();

		$.ajax({
			url: dashboard.siteurl + '/api/v1/s21/programs.web/department/' + departmentId,
			cache: false
		}).done(function( data ) {

				$select_study.select2({
					placeholder: select_study,
					width: 'style', // need to override the changed default
					data : data.results,
					escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
					templateResult: formatRepo,
					templateSelection: formatRepoSelection
				});
				$select_study.val("").trigger("change");
				$("#table_study").hide();
			});

		function formatRepo (repo) {
			if (repo.loading) {
				return repo.text;
			}

			let markup = "<div class='select2-result-repository clearfix'>" +
				"<div class='select2-result-repository__meta'>" +
				"<div class='select2-result-repository__title'> " + repo.text + "</div>";

			if (repo.children) {}
			else {
				markup += "<div class='select2-result-repository__statistics'>" +
					"<div class='select2-result-repository__stargazers' style='font-size: 0.9em'></div>" +
					"</div>" +
					"</div></div>";
			}
			return markup;
		}

		function formatRepoSelection (repo) {
			return repo.text;
		}
	}

	dashboard.study.LoadStudyTables = function(institutionId, departmentId, studyId, year) {

		//Get inheritance and load table only if not inherited!!
		getStudyPeriodsJson(institutionId, departmentId, studyId, year);
	}

	function initStudyDataTable (institutionId, departmentId, studyId, year) {

        let $study_table = $("#table_study");
		Study_DT = $study_table.DataTable({
			"bProcessing": false,
			"bFilter": false,
			"bPaginate": false,
			"bInfo" : false,
			"oLanguage": dtLanguageGr,
			"order": [[1, 'asc']],
			 "ajax":  {
				"url" : dashboard.siteurl + '/api/v1/dt/institution/' + institutionId + '/department/' + departmentId + '/program/' + studyId + '/calendar/' + year,
				"dataSrc":  "data.periods.period"
			 },
			"columns": [
				{ "mData": null},
				{ "mData": "startDate"},
				{ "mData": "name"},
				{ "mData": "startDate"},
				{ "mData": "endDate"}
			],
			"columnDefs": [
				{
					"aTargets": [0],
					"sortable": false,
					"sWidth": "20px"
				},
				{
					"aTargets": [1],
					"visible" : false
				},
				{
					"aTargets": [2],
					"render": function (data) {
						if (data == null) return '';
						else return '<span class="pb-0 mb-0" style="color: #003476;font-weight: 500">' + dashboard.broker.selectPeriod(data) + '</h6>';
					}
				},
				{
					"aTargets": [3,4],
					"render": function (data) {
						if (data == null) return '';
						else if (data !== "") {
							return moment.utc(data).format('D MMMM YYYY');
						}
						else {
							return '<span class="text-muted">-- not set --</span>';
						}
					}
				}
			],
			"initComplete": function(settings, json) {
				 StudyAfterInitComplete(json);
			}

		}); // DataTable init

		Study_DT.on('order.dt search.dt', function () {
			Study_DT.column(0, { search: 'applied', order: 'applied' }).nodes().each(function (cell, i) {
				cell.innerHTML = i + 1;
			});
		}).draw();
	}

	function reloadStudyDataTable (institutionId, departmentId, studyId, year) {
			Study_DT.ajax.url(dashboard.siteurl + '/api/v1/dt/institution/' + institutionId + '/department/' + departmentId + '/program/' + studyId + '/calendar/' + year);
			Study_DT.ajax.reload(function (json) {
				StudyAfterInitComplete(json);
			});
	}

	dashboard.study.clearStudyDataTable = function() {
		$("#study-button-edit").attr("disabled",true);
		$("#study-button-reset").attr("disabled", true);
		$("#inherit_study_note").html("");

		if (!$.fn.DataTable.isDataTable('#table_study')) {
			$("#table_study").DataTable().clear().destroy();
		}
	}

	function  StudyAfterInitComplete(json) {
		$("#table_study").show(); // just in case
		$("#study-button-edit").attr("disabled", false);
		if (json.data.inherited === true) {
			$("#inherit_study_note").html("<i class=\"fas fa-circle\" style=\"color:green\"></i> προκαθορισμένο");
			$("#study-button-reset").attr("disabled", true);
		}
		else {
			$("#inherit_study_note").html("<i class=\"fas fa-circle\"  style=\"color:orangered\"></i> προσαρμογή");
			$("#study-button-reset").attr("disabled", false);
		}
	}

	function getStudyPeriodsJson(institutionId, departmentId, studyId, year) {

		$.ajax({
			url: dashboard.siteurl + '/api/v1/dt/institution/' + institutionId + '/department/' + departmentId + '/program/' + studyId + '/calendar/' + year,
			cache: false
		})
		 .done(function( json ) {
			 const obj = JSON.parse(json);
			 $("#study-button-edit").attr("disabled", false);
			 if (obj.data.inherited === false) {
				 $("#inherit_study_note").html("<i class=\"fas fa-circle\"  style=\"color:orangered\"></i> προσαρμογή");
				 $("#study-button-reset").attr("disabled", false);
				 if (!$.fn.DataTable.isDataTable('#table_study')) {
					 initStudyDataTable(institutionId, departmentId, studyId, year);
				 } else {
					 reloadStudyDataTable(institutionId, departmentId, studyId, year);
				 }
			 }
			 else {
				 $("#inherit_study_note").html("<i class=\"fas fa-circle\" style=\"color:green\"></i> προκαθορισμένο");
				 $("#study-button-reset").attr("disabled", true);
				 $("#table_study").hide();
				 console.log("INHERITED REMOVE TABLE");
			 }
		 });
	}


	dashboard.study.editStudyPeriodsDataTable = function (year, institutionId, departmentId, studyId) {

		let $table = $("#table_study_edit");

		let url = dashboard.siteurl + '/api/v1/dt/institution/' + institutionId + '/department/' + departmentId + '/program/' + studyId + '/calendar/' + year;

		Edit_DT= $table.DataTable({
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

		Edit_DT.on('order.dt search.dt', function () {
			Edit_DT.column(0, { search: 'applied', order: 'applied' }).nodes().each(function (cell, i) {
				cell.innerHTML = i + 1;
			});
		}).draw();


	};

	function  set_display_results(json) {

		$edit_pane.show();
		$view_pane.hide();

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
			Edit_DT.cell({row: row_no, column: 1}).data(changed_date).draw();
			dashboard.study.checkTableDates(Edit_DT);
		});
		$(".modal-end-date").on('changeDate', function(ev){

			if (ev.date) {
				let row_no  = $(this).data("row");
				$(this).addClass('has-warning');

				let changed_date = moment(ev.date).format('YYYY-MM-DD');
				Edit_DT.cell({row: row_no, column: 2}).data(changed_date).draw();
				dashboard.study.checkTableDates(Edit_DT);
			}
		});

	}

	dashboard.study.checkTableDates = function(table) {

		var data = table.rows().data();
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
			setMessage($periodErrorMessages,'alert alert-danger visible', message);
			$updatePeriodsButton.attr("disabled", true);
		}
		else {
			setMessage($periodErrorMessages,'alert alert-danger invisible', " ");
			$updatePeriodsButton.attr("disabled", false);
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
				setMessage($periodMessages,'alert alert-success alert-dismissable visible',
					'<b><i class="fas fa-thumbs-up me-1"></i>Επιτυχής Ενημέρωση<b>');
				setTimeout(function() {
					setMessage($periodMessages,'alert alert-success invisible', " ");
					$("#study_select2").val(studyId).trigger("change");
					$edit_pane.hide();
					$view_pane.show();
				}, 1500);

				let message = {msg: "Academic Calendar Updated!", year: dashboard.selected_year, department: departmentId, study: studyId};
				dashboard.broker.trigger('refresh.page', [message]);
			},
			error : function() {
				$edit_pane.hide();
				$view_pane.show();
				setMessage($periodMessages,'alert alert-danger alert-dismissable visible',
					'<b><i class="fas fa-exclamation-triangle me-1"></i>Πρόβλημα Συστήματος. Επικοινωνήστε με το διαχειριστή<b>');
			}
		});
	}


})();
