/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.department = dashboard.department || {};

	let DepartmentDT;
	let DepartmentPausesDT;
	let $periodMessages;
	let $periodErrorMessages;
	let $updatePeriodsButton;

	let $argiesMessages;
	let $argiesErrorMessages;
	let $updateArgiesButton;

	let scope;
	let $view_pane;
	let $edit_pane;
	let Edit_DT;
	let Edit_Pauses_DT;
	let pre_meta = "dep";

    let $view_a_pane;
    let $edit_a_pane;

	dashboard.department.init = function () {

		 $edit_pane = $("#department_edit_card");
		 $view_pane = $("#department_card");

		 $edit_a_pane = $("#argies_card_edit");
		 $view_a_pane = $("#argies_card");

		 scope = "department";
		 $periodMessages = $("#d-PeriodMessages");
		 $periodErrorMessages = $("#d-PeriodErrorMessages");
		 $updatePeriodsButton = $("#d-UpdatePeriods");

		$argiesMessages = $("#d-PauseMessages");
		$argiesErrorMessages = $("#d-PauseErrorMessages");
		$updateArgiesButton = $("#d-updatePauses");

		$("#department-periods-edit").on('click', function() {
			dashboard.department.editDepartmentPeriodsDataTable( dashboard.selected_year, dashboard.institution_id,dashboard.selected_dep,"");
		});

		$("#department-periods-edit-close").on('click',function(e){
			setMessage($periodErrorMessages,'alert alert-danger invisible', " ");
			$updatePeriodsButton.attr("disabled", false);
			$view_pane.show();
			$edit_pane.hide();
		});

		$updatePeriodsButton.on('click',function(){
			let refId = dashboard.selected_dep;
			let inherit = "0";
			let $department_periods_dt = $("#table_department_edit");
			let dataJSON = getTableDataAsJSON($department_periods_dt, refId,inherit);
			let url = dashboard.siteurl + '/api/v1/department/' + refId + '/calendar/update/' + dashboard.selected_year;
			postCreateUpdatePeriods(url, dataJSON, scope, dashboard.selected_year, dashboard.selected_dep, "");
		});

		$("#department-argies-edit").on('click', function() {
			editDepartmentPausesTable(dashboard.selected_year,dashboard.institution_id,dashboard.selected_dep);
		});

		$("#argies-edit-close").on('click',function(e){
			setMessage($argiesErrorMessages,'alert alert-danger invisible', " ");
			$updateArgiesButton.attr("disabled", false);
			$view_a_pane.show();
			$edit_a_pane.hide();
		});

		$("#argia-button-new").on('click',function(e){
			// Add NEW ROW to DATATABLE with: name = "" and startDate==endData==Today
			let new_argia = {};
			new_argia.name = "[Τίτλος Αργίας/Παύσης]";

			let d = new Date();
			let curr_date = d.getDate();
			let curr_month = d.getMonth() + 1; //Months are zero based
			let curr_year = d.getFullYear();

			if (curr_month < 10) { curr_month = "0" + curr_month;}
			let toDay = curr_year + "-" + curr_month + "-" + curr_date;

			new_argia.startDate = toDay;
			new_argia.endDate =  toDay;

			let $department_pauses_dt = $("#table_department_argies_edit");
			$department_pauses_dt.DataTable().row.add(new_argia).draw(true);
			let data = getArgiesTableDataAsJSON($department_pauses_dt,"");
			PauseEditCallback(data, "new_argia_added"); //!Important
		});

		$updateArgiesButton.on('click',function(){

			let year = dashboard.selected_year;
			let institutionId = dashboard.institution_id;
			let departmentId = dashboard.selected_dep;
			let $department_pauses_dt = $("#table_department_argies_edit");
			let dataJSON = getArgiesTableDataAsJSON($department_pauses_dt, departmentId);
			let dataString  = JSON.stringify(dataJSON);
			let url = dashboard.siteurl + '/api/v1/department/' + departmentId + '/pause/update/' + dashboard.selected_year;
			postCreateUpdatePauses(url, dataString, scope, year, institutionId, departmentId);
		});
	}

    dashboard.department.loadDepartmentTables = function() {

		if (!$.fn.DataTable.isDataTable('#table_department')) {
			console.log("Load department periods");
			initDepartmentPeriodsDataTable(dashboard.institution_id,dashboard.selected_dep, dashboard.selected_year);
		} else {
			console.log("RE-Load department periods");
			reloadDepartmentPeriodsDataTable(dashboard.institution_id,dashboard.selected_dep, dashboard.selected_year);
		}
		if (!$.fn.DataTable.isDataTable('#table_department_a')) {
			console.log("Load department argies");
			initDepartmentPausesDataTable(dashboard.selected_dep, dashboard.selected_year);
		} else {
			console.log("RE-Load department argies");
			reloadDepartmentPausesDataTable(dashboard.selected_dep, dashboard.selected_year);
		}
		if (!$.fn.DataTable.isDataTable('#table_institution_a')) {
			console.log("Load institution argies");
			dashboard.institutions.initInstitutionPausesDataTable(dashboard.institution_id, dashboard.selected_year);
		} else {
			console.log("RE-Load institution argies");
			dashboard.institutions.reloadInstitutionPausesDataTable(dashboard.institution_id, dashboard.selected_year);
		}

	};

	function initDepartmentPeriodsDataTable(institutionId, departmentId, year,) {

		let $department_datatable = $("#table_department");

		DepartmentDT= $department_datatable.DataTable({
			"bProcessing": false,
			"bDestroy": true,
			"bFilter": false,
			"bPaginate": false,
			"bInfo" : false,
			"oLanguage": dtLanguageGr,
			"order": [[1, 'asc']],
			"ajax":  {
				"url" : dashboard.siteurl + '/api/v1/dt/institution/' +  institutionId + '/department/' + departmentId + '/calendar/' + year,
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
					"sWidth": "20px",
				},
				{
					"aTargets": [1],
					"visible" : false,
				},
				{
					"aTargets": [2],
					"sWidth": "33%",
					"render": function (data) {
						return '<span class="pb-0 mb-0" style="color: #003476;font-weight: 500">' + dashboard.broker.selectPeriod(data) + '</span>';
					}
				},
				{
					"aTargets": [3,4],
					"sWidth": "33%",
					"render": function (data) {
						if (data !== "") {
							return moment.utc(data).format('D MMMM YYYY');
						}
						else {
							return '<span class="text-muted">-- not set --</span>';
						}
					}
				}
			],
			"initComplete": function( settings, json ) {
				PeriodsAfterInitComplete(json);
			}
		}); // DataTable init
		DepartmentDT.on('order.dt search.dt', function () {
			DepartmentDT.column(0, { search: 'applied', order: 'applied' }).nodes().each(function (cell, i) {
				cell.innerHTML = i + 1;
			});
		}).draw();
	}

	function PeriodsAfterInitComplete(json) {

		$("#department-periods-edit").attr("disabled", false);
		if (json.data.inherited === true) {
			$("#department_inherit_note").html("<i class=\"fas fa-circle\" style=\"color:green\"></i> προκαθορισμένο");
			$("#department-periods-reset").attr("disabled", true);
		}
		else {
			$("#department_inherit_note").html("<i class=\"fas fa-circle\"  style=\"color:orangered\"></i> προσαρμογή");
			$("#department-periods-reset").attr("disabled", false);
		}
	}

	function reloadDepartmentPeriodsDataTable (institutionId, departmentId, year,) {
		DepartmentDT.ajax.url(dashboard.siteurl + '/api/v1/dt/institution/' +  institutionId + '/department/' + departmentId + '/calendar/' + year);
		DepartmentDT.ajax.reload( function ( json ) {
			PeriodsAfterInitComplete(json);
		});
	}

	function initDepartmentPausesDataTable (departmentId,year) {

		DepartmentPausesDT = $("#table_department_argies").DataTable({
			"bProcessing": false,
			"bDestroy": true,
			"bFilter": false,
			"bPaginate": false,
			"bInfo" : false,
			"oLanguage": {
				"sSearch": "<small>Αναζήτηση</small>"
			},
			"order": [[1, 'asc']],
			"ajax":  {
				"url" : dashboard.siteurl + '/api/v1/dt/department/' + departmentId + '/pause/' + year,
				"dataSrc":  "data.argies.argia"
			},
			"columns": [
				{ "mData": null , "sWidth": "20px", "bSortable": false },
				{ "mData": "startDate", "sWidth": "0px", "bSortable": true, "bVisible":false },
				{ "mData": "name", "sWidth": "380px", "bSortable": true},
				{ "mData": "startDate", "sWidth": "180px", "bSortable": true },
				{ "mData": "endDate", "sWidth": "200px", "bSortable": true }
			],
			"columnDefs": [
				{
					"aTargets": [0],
					"render": function (data,type,row) {
						return row;
					}
				},
				{
					"aTargets": [1],
					visible: true
				},
				{
					"aTargets": [2],
					"render": function (data) {
						return '<span class="pb-0 mb-0" style="color: #003476;font-weight: 500">' + data + '</span>';
					}
				},
				{
					"aTargets": [3,4],
					"render": function (data) {
						if (data !== "") {
							return moment.utc(data).format('D MMMM YYYY');
						}
						else {
							return '<span class="text-muted">-- not set --</span>';
						}
					}
				}
			],
			"initComplete": function( settings, json ) {
			},
		}); // DataTable init

		DepartmentPausesDT.on('order.dt search.dt', function () {
			DepartmentPausesDT.column(0, { search: 'applied', order: 'applied' }).nodes().each(function (cell, i) {
				cell.innerHTML = i + 1;
			});
		}).draw();
	}

	function  reloadDepartmentPausesDataTable( departmentId, year) {
		DepartmentPausesDT.ajax.url(dashboard.siteurl + '/api/v1/dt/department/' + departmentId + '/pause/' + year);
		DepartmentPausesDT.ajax.reload();
	}

	dashboard.department.loadDepartmentsDropdownList = function() {

		let $elem = $("#department-ddlist");

		$elem.on('click','a.dropdown-toggle', function() {
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

		let siteUrl          = dashboard.siteurl;
		let queryParams = new URLSearchParams(window.location.search);

		let url = siteUrl + '/api/v2/s2/departments.web/authorized/data';
		let html = '';
		$.ajax({
			type: 'GET',
			url: url,
			dataType: 'json',
			success: function (data) {
				$.each(data.results, function (index, element) {
					html += '<li class="dropdown-submenu">';
					html += '<a class="dropdown-item dropdown-toggle" href="#">' + element.text + '</a>';
					html += '<ul class="dropdown-menu">';
					$.each(element.children, function (index1, el) {
						queryParams.set("id", el.id);
						html +=  '<li><a class="dropdown-item" href="?' + queryParams + '">' + el.text +'</a></li>';
					});
					html += '</ul></li>';
				});
				$elem.append(html);
			}
		});
	}

	dashboard.department.editDepartmentPeriodsDataTable = function (year,institutionId, departmentId) {

		let $table = $("#table_department_edit");

		let  url;
		if (departmentId === "") {
			url = dashboard.siteurl + '/api/v1/dt/institution/' + institutionId + '/calendar/' + year;
		}
		else {
			url = dashboard.siteurl + '/api/v1/dt/institution/' + institutionId + '/department/' + departmentId + '/calendar/' + year;
		}

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
				PeriodEditCallback(json);
			}
		}); // DataTable init

		Edit_DT.on('order.dt search.dt', function () {
			Edit_DT.column(0, { search: 'applied', order: 'applied' }).nodes().each(function (cell, i) {
				cell.innerHTML = i + 1;
			});
		}).draw();


	};

	function  PeriodEditCallback(json) {

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
			dashboard.department.checkTableDates(Edit_DT);
		});
		$(".modal-end-date").on('changeDate', function(ev){

			if (ev.date) {
				let row_no  = $(this).data("row");
				$(this).addClass('has-warning');

				let changed_date = moment(ev.date).format('YYYY-MM-DD');
				Edit_DT.cell({row: row_no, column: 2}).data(changed_date).draw();
				dashboard.department.checkTableDates(Edit_DT);
			}
		});

	}

	dashboard.department.checkTableDates = function(table) {

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

	dashboard.department.checkPausesDates = function(table) {

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
					message += "<li>" + _r.name + ": Η καταληκτική ημερομηνία είναι <b>πρίν</b> την αρχική: " + "</li>";
				}
			}
		}
		// NULL TITLE
		for (let c=0; c < data.length; c++) {
			let  _r = data[c];
			if (_r.name === null || _r.name === "") {
				errors = 1;
				message += "<li>" + "Πληκτρολογήστε 'Τίτλο' αργίας" + "</li>";
			}
		}
		message += "</ul>";
		if (errors === 1) {
			setMessage($argiesErrorMessages,'alert alert-danger visible', message);
			$updateArgiesButton.attr("disabled", true);
		}
		else {
			setMessage($argiesErrorMessages,'alert alert-danger invisible', " ");
			$updateArgiesButton.attr("disabled", false);
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

	function getArgiesTableDataAsJSON($table,refId) {

		var data = $table.DataTable().rows().data();

		let argies_list = [];
		let argies = {};
		argies.refId = refId;

		for (let c=0; c < data.length; c++) {
			let name = $("#data_row_" + c).val();
			let row_node = $table.DataTable().cell(c,6).node().innerHTML;
			if (row_node.includes("btn-danger") === false) {
				let  _r 		 = data[c];
				let startDate 	 = _r.startDate;
				let endDate 	 = _r.endDate;
				let argia = {"name":name, "endDate" : endDate, "startDate": startDate};
				argies_list.push(argia);
			}
			// console.log("line:" + c + " result:" + row_node.includes("btn-danger"));
		}

		argies.argia = argies_list;
		return argies;
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

	function editDepartmentPausesTable(year,institutionId, departmentId) {

		let  url;
		if (departmentId === "") {
			url = dashboard.siteurl + '/api/v1/dt/institution/' + institutionId + '/pause/' + year;
		}
		else {
			url = dashboard.siteurl + '/api/v1/dt/department/' + departmentId + '/pause/' + year;
		}

		Edit_Pauses_DT = $("#table_department_argies_edit").DataTable({
			"bProcessing": false,
			"bDestroy": true,
			"bFilter": false,
			"bPaginate": false,
			"oLanguage": dtLanguageGr,
			// "order": [[1, 'asc']],
			"ajax":  {
				"url":  url,
				"dataSrc":  "data.argies.argia"
			},
			"columns": [
				{ "mData": null , "sWidth": "40px", "bSortable": false, "bVisible":false },
				{ "mData": "startDate", "sWidth": "0px", "bSortable": true, "bVisible":false },	// sorting, actual database data
				{ "mData": "endDate", "sWidth": "0px", "bSortable": true, "bVisible":false },	// sorting, actual database data
				{ "mData": "name", "bSortable": false},
				{ "mData": "startDate", "sWidth": "200px", "bSortable": false },				// display data formatted
				{ "mData": "endDate", "sWidth": "200px", "bSortable": false },					// display data formatted
				{ "mData": null, "sWidth": "40px", "bSortable": false }							// delete button
			],
			"columnDefs": [
				{
					"name": "index",
					"render": function (data,type,row) {
						return row;
					},
					"targets" :0
				},
				{
					"name": "startDateData",
					"render": function (data) {
						return data;
					} ,
					"targets" :1
				},
				{
					"name": "endDateData",
					"render": function (data) {
						return data;
					},
					"targets" : 2
				},
				{
					"name": "name",
					"render": function (data,type,row, meta) {
						return '<input class="form-control" size="30" type="text" value="' + data + '"  id="data_row_' + meta.row + '" name="data_row-' + meta.row + '"/>';
					},
					"targets" :3
				},
				{
					"name": "startDate",
					"render": function (data,type,row, meta) {

						return '<div class="input-group  date modal-pstart-date" data-row="' + meta.row + '" id="' + pre_meta + '-div-startpDate-' + meta.row + '"  >' +
							'<span class="input-group-addon input-group-text">' +
							'<i class="fas fa-calendar-alt"></i>' +
							'</span>' +
							'<input class="form-control" value=""  />' +
							'</div>';
					},
					"targets" : 4
				},
				{
					"name": "endDate",
					"render": function (data,type,row, meta) {


						return '<div class="input-group date modal-pend-date" data-row="' + meta.row + '" id="' + pre_meta + '-div-endpDate-' + meta.row + '"  >' +
							'<span class="input-group-addon input-group-text">' +
							'<i class="fas fa-calendar-alt"></i>' +
							'</span>' +
							'<input class="form-control" value=""  />' +
							'</div>';
					},
					"targets" : 5
				},
				{

					"name": "name",
					"render": function (data,type,row,meta) {
						return "<button data-row='" + meta.row + "' class='btn btn-light delete_argia_row float-end'><i class=\"far fa-trash-alt\"></i></button>";
					},
					"targets" : 6
				},
			],
			"initComplete": function( settings, json ) {
				PauseEditCallback(json.data.argies);

			}
		}); // DataTable init

		Edit_Pauses_DT.on('order.dt search.dt', function () {
			Edit_Pauses_DT.column(0, { search: 'applied', order: 'applied' }).nodes().each(function (cell, i) {
				cell.innerHTML = i + 1;
			});
		}).draw();
	}

     function PauseEditCallback(json, action) {

		$view_a_pane.hide();
		$edit_a_pane.show();

		let table = $("#table_department_argies_edit").DataTable();

		for (let i=0; i < json.argia.length; i++) {

			let $startDateElementInPosI = $(`#${pre_meta}-div-startpDate-${i}`);
			$startDateElementInPosI.datepicker({
				format: "dd MM yyyy",
				todayBtn: false,
				language: "el",
				autoclose: true,
				todayHighlight: false
			});

			if (json.argia[i].startDate !== "") {
				let d = new Date(json.argia[i].startDate);
				let curr_date = d.getDate();
				let curr_month = d.getMonth() + 1; //Months are zero based
				let curr_year = d.getFullYear();

				let startdate = curr_date + "/" + curr_month + "/" + curr_year;
				$startDateElementInPosI.datepicker("setDate", startdate);
			}
			else {
				$startDateElementInPosI.datepicker("setDate", "");
			}
			let $endDateElementInPosI = $(`#${pre_meta}-div-endpDate-${i}`);
			$endDateElementInPosI.datepicker({
				format: "dd MM yyyy",
				todayBtn: false,
				language: "el",
				autoclose: true,
				todayHighlight: false
			});
			if (json.argia[i].endDate !== "") {
				let d = new Date(json.argia[i].endDate);
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
		 //SET FOCUS ON NEWLY CREATED ARGIA
		 if (action !== undefined && action === "new_argia_added") {
			 let last_row_counter = json.argia.length - 1;
			 let $last_argia_input = $("#data_row_" + last_row_counter);
			 $last_argia_input.focus();
			 $last_argia_input.select();
			 let $closest_tr = $last_argia_input.closest('tr');
			 $closest_tr.css('background-color', '#2eb85c');
		 }

		$(".delete_argia_row").on('click',function(e){
			if ($(this).hasClass("btn-light")) {
				$(this).removeClass("btn-light");
				$(this).addClass("btn-danger");
			}
			else {
				$(this).removeClass("btn-danger");
				$(this).addClass("btn-light");
			}
		});

		$(".modal-pstart-date").on('changeDate', function(ev){

			let row_no  = $(this).data("row");
			$(this).addClass('has-warning');

			let changed_date = moment(ev.date).format('YYYY-MM-DD');
			table.cell({row: row_no, column: 1}).data(changed_date).draw();
			dashboard.department.checkPausesDates(table);
		});
		$(".modal-pend-date").on('changeDate', function(ev){

			if (ev.date) {
				let row_no  = $(this).data("row");
				$(this).addClass('has-warning');

				let changed_date = moment(ev.date).format('YYYY-MM-DD');
				table.cell({row: row_no, column: 2}).data(changed_date).draw();
				dashboard.department.checkPausesDates(table);
			}
		});
	};

	function postCreateUpdatePauses(postURL, dataJSON, scope, year, institutionId, departmentId) {

		$.ajax({

			url: postURL,
			type:"POST",
			contentType: "application/json; charset=utf-8",
			data: dataJSON,
			async: true,    	//Cross-domain requests and dataType: "jsonp" requests do not support synchronous operation
			success: function() {
				setMessage($argiesMessages,'alert alert-success alert-dismissable visible',
					'<b><i class="fas fa-thumbs-up me-1"></i>Επιτυχής Ενημέρωση<b>');
				setTimeout(function() {
					setMessage($argiesMessages,'alert alert-success invisible', " ");
					$edit_a_pane.hide();
					$view_a_pane.show();
				}, 1000);

				let message = {msg: "Academic Calendar Updated!", year: dashboard.selected_year, department: departmentId, institution: institutionId};
				dashboard.broker.trigger('refresh.page', [message]);
			},
			error: function() {
				$edit_a_pane.hide();
				$view_a_pane.show();
				setMessage($argiesMessages,'alert alert-danger alert-dismissable show',
					'<b><i class="fas fa-exclamation-triangle me-1"></i>Πρόβλημα Συστήματος. Επικοινωνήστε με το διαχειριστή<b>');
			}
		});
	}

})();
