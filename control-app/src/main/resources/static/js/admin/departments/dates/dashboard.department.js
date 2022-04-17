/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.department = dashboard.department || {};

    dashboard.department.departmentId = "";
    dashboard.department.schoolId = "";

	dashboard.department.selectedDepartmentId = null;
	dashboard.department.selectedRowIndex = null;
	dashboard.department.selectedDepartmentName = null;

    let $select_department;
    let $select_school;
    let $department_button_edit;
    let $department_button_reset;
    let $department_datatable;
    let $inherit_note;
    let data_table;
    let $editPeriodModal;
    let $modal_scope;
    let DEPARTMENT_PERIODS_DT;
	let DEPARTMENT_PAUSES_DT;

    dashboard.department.init = function() { 

    	$select_department 		 = $("#department_select2");
    	$select_school 	   		 = $("#school_select2");
    	$department_button_edit  = $("#department-button-edit");
    	$department_button_reset = $("#department-button-reset");
    	$department_datatable 	 = $("#table_department");
    	$inherit_note			 = $("#inherit_note");
    	$editPeriodModal  		 = $("#editPeriodModal");
    	$modal_scope			 = $("#modal_scope");

    	$department_button_edit.on('click', function() {
    		
   		  	 dashboard.modal.initDataTable( dashboard.selected_year, dashboard.institution,dashboard.department.selectedDepartmentId,"");
   		 	
   		 	dashboard.modal_year.html(" " + dashboard.selected_year + "-" + (parseInt(dashboard.selected_year)+1));
   		 	 
   		 	dashboard.modal_header_img_institution.hide();
   		 	dashboard.modal_header_img_department.show();
   		 	dashboard.modal_header_img_study.hide();

   		 	dashboard.modal_header_institution.css("color", "black");
   		 	dashboard.modal_department_title.show();
   		 	dashboard.modal_header_department.html("<b>" + dashboard.department.selectedDepartmentName +"</b>");
   		 	dashboard.modal_header_department.css("color", "red");
   		 	dashboard.modal_study_title.hide();

   		 	dashboard.modal_period_error_messages.html(" ");
   		 	dashboard.modal_period_error_messages.attr('class','alert alert-danger invisible');
   		 
   		 	dashboard.modal_inherit.show();
   		 
   		 	$modal_scope.val("department");
   		 	$editPeriodModal.modal('show');
    	});
		$department_button_reset.on('click', function() {
    		let department_name = dashboard.department.selectedDepartmentName;
    		let header = "<span class='glyphicon glyphicon-magnet' aria-hidden='true'></span>Επαναφορά Περιόδων<br/>" +
						 "<small>Τμήμα: <span style='color:red'> " +  department_name + "</span></small>";
    		let confirm_text = "To Τμήμα θα επανέλθει στις <b>προκαθορισμένες περιόδους του Ιδρύματος</b>." +
				               " Θα επανέλθουν, επίσης, <b>τα προγράμματα σπουδών</b>, που δεν ακολουθούν προσαρμοσμένο πρόγραμμα. <br/><br/>Είστε σίγουρος?";
	 		alertify.confirm(header, confirm_text , function (e) {
	    	    if (e) {
	    	    		let url = dashboard.siteurl + '/api/v1/department/' + dashboard.department.departmentId + '/calendar/reset/' + dashboard.selected_year;
	    	    		postResetDepartmentPeriods(url, dashboard.selected_year, dashboard.department.departmentId);
	    	    }  
	 		}, function() {});	
    	});
    };
	dashboard.department.getDepartmentTables = function() {
		if (dashboard.department.selectedDepartmentId != null && dashboard.department.selectedDepartmentId !== "") {
			let name = dashboard.department.selectedDepartmentName;
			$(".selected_department_name").html("Τμήμα " + name);
		}
		else {
			$("#selected_department_name").html("");
		}

		let value = dashboard.department.selectedDepartmentId;
		if (value === null) { value="";}

		//console.log("Department Change:" +  value);

		dashboard.department.departmentId = value;
		dashboard.department.initDepartmentPeriodsDataTable( dashboard.selected_year, dashboard.department.selectedDepartmentId);
		dashboard.department.initDepartmentPausesDataTable(dashboard.selected_year, dashboard.department.selectedDepartmentId);

	}

    dashboard.department.initDepartmentPeriodsDataTable = function (year,departmentId) {
		if (year === "" || departmentId === "")
		{
			$department_button_edit.attr("disabled", true);
			$department_button_reset.attr("disabled", true);
			$inherit_note.html("");
			$department_datatable.DataTable().clear().destroy();

			$("#department-argies-edit").attr("disabled", true);
			$("#table_department_a").DataTable().clear().destroy();
			return;
		}
		let iid = $("#institutionId").val();

		DEPARTMENT_PERIODS_DT = $department_datatable.DataTable({
			"bProcessing": false,
			"bDestroy": true,
			"bFilter": false,
			"bPaginate": false,
			"bInfo" : false,
			"oLanguage": dtLanguageGr,
			"order": [[1, 'asc']],
			"ajax":  {
				"url" : dashboard.siteurl + '/api/v1/dt/institution/' +  iid + '/department/' + departmentId + '/calendar/' + year,
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

				$department_button_edit.attr("disabled", false);
				$("#department-argies-edit").attr("disabled", false);
				if (json.data.inherited === true) {
					$inherit_note.html("<i class=\"fas fa-circle\" style=\"color:green\"></i> προκαθορισμένο");
					$department_button_reset.attr("disabled", true);
				}
				else {
					$inherit_note.html("<i class=\"fas fa-circle\"  style=\"color:orangered\"></i> προσαρμογή");
					$department_button_reset.attr("disabled", false);
				}

			}
		}); // DataTable init
		DEPARTMENT_PERIODS_DT.on('order.dt search.dt', function () {
			DEPARTMENT_PERIODS_DT.column(0, { search: 'applied', order: 'applied' }).nodes().each(function (cell, i) {
				cell.innerHTML = i + 1;
			});
		}).draw();

	}
	dashboard.department.initDepartmentPausesDataTable = function (year,departmentId) {

		if (year === "" || departmentId === "")
		{
			$("#department-argies-edit").attr("disabled", true);
			//$inherit_note.html("");
			$("#table_department_a").DataTable().clear().destroy();
			return;
		}
		DEPARTMENT_PAUSES_DT = $("#table_department_a").DataTable({
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
				set_display_results(json);
			},
		}); // DataTable init

		DEPARTMENT_PAUSES_DT.on('order.dt search.dt', function () {
			DEPARTMENT_PAUSES_DT.column(0, { search: 'applied', order: 'applied' }).nodes().each(function (cell, i) {
				cell.innerHTML = i + 1;
			});
		}).draw();
	}

	function  set_display_results(json) {
		$department_button_edit.attr("disabled", false);
		$("#department-argies-edit").attr("disabled", false);
		if (json.data.inherited === true) {
			$inherit_note.html("<i class=\"fas fa-circle\" style=\"color:green\"></i> προκαθορισμένο");
			$department_button_reset.attr("disabled", true);
		}
		else {
			$inherit_note.html("<i class=\"fas fa-circle\"  style=\"color:orangered\"></i> προσαρμογή");
			$department_button_reset.attr("disabled", false);
		}
	}
	dashboard.department.loadDepartmentsOnSearchBar = function ($elem) {

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

	dashboard.department.reloadDataTables = function (iid,departmentId, year) {
		DEPARTMENT_PERIODS_DT.ajax.url(dashboard.siteurl + '/api/v1/dt/institution/' +  iid + '/department/' + departmentId + '/calendar/' + year);
		DEPARTMENT_PERIODS_DT.ajax.reload( function ( json ) {
			set_display_results(json);
		});
		DEPARTMENT_PAUSES_DT.ajax.url(dashboard.siteurl + '/api/v1/dt/department/' + departmentId + '/pause/' + year);
		DEPARTMENT_PAUSES_DT.ajax.reload();
	}

    function postResetDepartmentPeriods(postURL, year, departmentId) {
	   	   
   	 	$.ajax({
			
			url: postURL,		
			type:"POST", 
			contentType: "application/json; charset=utf-8",
			async: true,    	//Cross-domain requests and dataType: "jsonp" requests do not support synchronous operation
			
			success: function() {
		  			let message = {msg: "Academic Calendar Updated!", year: dashboard.selected_year, department: departmentId, study: ""};
		            dashboard.broker.trigger('refresh.page', [message]);
			},   		            	  
			error : function() {
				alert("Πρόβλημα Συστήματος. Επικοινωνήστε με το διαχειριστή");
			 }
		});
    } 

})();
