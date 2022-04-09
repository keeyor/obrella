/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.department = dashboard.department || {};

    dashboard.department.departmentId = "";
    dashboard.department.schoolId = "";
 
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
    	
    	initSchoolList();
		initDepartmentList();

    	$department_button_edit.on('click', function() {
    		
   		  	data_table = dashboard.modal.initDataTable( dashboard.selected_year, dashboard.institution,$select_department.select2('data')[0].id,"");
   		 	
   		 	dashboard.modal_year.html(" " + dashboard.selected_year + "-" + (parseInt(dashboard.selected_year)+1));
   		 	 
   		 	dashboard.modal_header_img_institution.hide();
   		 	dashboard.modal_header_img_department.show();
   		 	dashboard.modal_header_img_study.hide();
    		 
   		 	
   		 	dashboard.modal_header_institution.css("color", "black");
   		 	dashboard.modal_department_title.show();
   		 	dashboard.modal_header_department.html("<b>" + $select_department.select2('data')[0].text +"</b>");
   		 	dashboard.modal_header_department.css("color", "red");
   		 	dashboard.modal_study_title.hide();
 		 	 
   	 	 
   		 	dashboard.modal_period_error_messages.html(" ");
   		 	dashboard.modal_period_error_messages.attr('class','alert alert-danger hidden');
   		 
   		 	dashboard.modal_inherit.show();
   		 
   		 	$modal_scope.val("department");
   		 	$editPeriodModal.modal('show');

    	});
    	$select_school.on('change', function () {

    		let value = $select_school.val();
    		
    		if (value === null) { value="";}
    		//console.log("Department Change:" +  value);
    		dashboard.department.schoolId = value;
    		if (value !== "") {
    			getSchoolDepartments(dashboard.institution, dashboard.department.schoolId);
    		}
        });
    	$select_department.on('change', function () {
    		if ($select_department.val() != null && $select_department.val() !== "") {
				let name = $select_department.select2('data')[0].text;
				$(".selected_department_name").html("Τμήμα " + name);
			}
    		else {
				$("#selected_department_name").html("");
			}

    		let value = $select_department.val();
    		if (value === null) { value="";}
    		
    		console.log("Department Change:" +  value);
 
    		dashboard.department.departmentId = value;
			dashboard.department.initDepartmentPeriodsDataTable( dashboard.selected_year, dashboard.department.departmentId);
    		dashboard.department.initDepartmentPausesDataTable(dashboard.selected_year, dashboard.department.departmentId);
    		
            let message = {msg: "Department selected!", value: dashboard.department.departmentId};
            dashboard.broker.trigger('afterSelect.department', [message]);
        });

    	//This is the only way to make select2 re-initialize on page enter (e.g. after forward+backward). Don't have to handle event
    	$select_school.val("").trigger("change");
		$department_button_reset.on('click', function() {
    		let department_name = $select_department.select2('data')[0].text;
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
    
    dashboard.department.setVal = function (year, departmentId) {

    	//console.log("SETVAL for DEP:" + " dep:" + departmentId);
    	dashboard.department.departmentId = departmentId;
    	$select_department.val(departmentId).trigger("change");
    };
    dashboard.department.triggerClear = function () {
    	
    	getSchoolDepartments(dashboard.institution, "");
    	$select_school.val("").trigger("change");
    };

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
		// console.log("InitDataTable for DEP:" + " dep:" + departmentId);

		DEPARTMENT_PERIODS_DT = $department_datatable.DataTable({
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
				"url" : dashboard.siteurl + '/api/v1/dt/institution/' +  iid + '/department/' + departmentId + '/calendar/' + year,
				"dataSrc":  "data.periods.period"
			},
			"columns": [
				{ "mData": null , "sWidth": "40px", "bSortable": false },
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
						return '<h6 class="pb-0 mb-0" style="color: #003476">' + dashboard.broker.selectPeriod(data) + '</h6>';
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

			},
			"createdRow": function ( row, data, index ) {

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
				{ "mData": null , "sWidth": "40px", "bSortable": false },
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
						return '<h6 class="pb-0 mb-0" style="color: #003476">' + data + '</h6>';
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
			"createdRow": function ( row, data, index ) {
			}
		}); // DataTable init

		DEPARTMENT_PAUSES_DT.on('order.dt search.dt', function () {
			DEPARTMENT_PAUSES_DT.column(0, { search: 'applied', order: 'applied' }).nodes().each(function (cell, i) {
				cell.innerHTML = i + 1;
			});
		}).draw();
	}


    function getSchoolDepartments(institution, school) {

		$select_department.empty();
		if (school === "") { school = "dummy";}
		$.ajax({
			url: dashboard.siteurl + '/api/v1/s2/departments.web/school/' + school,
			cache: false
		})
			.done(function( data ) {
				$select_department.select2({
					placeholder: 'Επιλέξτε Τμήμα',
					width: '100%', // need to override the changed default
					data : data.results,
					escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
					templateResult: formatRepo,
					templateSelection: formatRepoSelection
				});
				$select_department.val("").trigger("change");
				let message = {msg: "Department afterInit!"};
				dashboard.broker.trigger('init.Department', [message]);
			});
		function formatRepo (repo) {
			if (repo.loading) {
				return repo.text;
			}
			//this is the head
			let markup = "<div class='select2-result-repository clearfix'>" +
				"<div class='select2-result-repository__meta'>" +
				"<div class='select2-result-repository__title'>" + repo.text + "</div>";

			if (repo.children) { //2nd line head
				markup += "" ;
			}
			else {              //children
				markup += "<div class='select2-result-repository__statistics'>" +
					"<div class='select2-result-repository__stargazers'></div>" +
					"</div>" +
					"</div></div>";
			}
			return markup;
		}
		//format selection
		function formatRepoSelection (repo) {
			return repo.text;
		}
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
    
    function initDepartmentList() {
 
    	//console.log("INIT DEPARTMENT LIST");
    	$select_department.select2({
    		  placeholder: "Επιλέξτε Τμήμα"
    	  });
    	 $select_department.val("");
		 $select_department.trigger("change");

     }
    
    function initSchoolList() {
    	
  	  $select_school.select2({
  		  placeholder: "Επιλέξτε Σχολή"
  	  });
  	  $select_department.val("");
  	  $select_department.trigger("change");
   }
})();
