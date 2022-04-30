/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.study = dashboard.study || {};

    dashboard.study.studyId = "";

	let Study_DT;
    
    let $select_study;

    let $add_study_button;
    let $edit_study_button;
    let $trash_study_button;
    
    let $study_button_edit;
    let $study_button_reset;
    let $study_datatable;
    
    let $StudyMessages;
    
    let $studyModal;
    
    let $newStudyButton;
    let $updateStudyButton;
    let $deleteStudyButton;
    let $studyForm;
	
    let $studyModalLabelNew;
    let $studyModalLabelUpdate;
    let $studyModalLabelDelete;
    let $dataEntryFormBottomMessageStudy;
    let $sure_for_delete_study;
    let $study_title;
    let $study_id;
    let $study_institution_id;
    let $study_school_id;
    let $study_department_id;
    let $error_study_title;
    let $div_study_title;
    let $editPeriodModal;
    let $modal_scope;
    let $inherit_study_note;
    let $department_of_study;

    dashboard.study.init = function () {

    	$select_study = $("#study_select2");
    	
    	$add_study_button   = $("#add_study");
    	$edit_study_button  = $("#edit_study");
    	$trash_study_button = $("#trash_study");

    	$study_button_edit  = $("#study-button-edit");
    	$study_button_reset = $("#study-button-reset");
    	$study_datatable 	= $("#table_study");
    	
    	$StudyMessages		= $("#StudyMessages");
    	$studyModal			= $("#studyModal");
    	
    	$newStudyButton 	= $("#newStudyButton");
    	$updateStudyButton  = $("#updateStudyButton");
    	$deleteStudyButton  = $("#deleteStudyButton");
    	$studyForm 			= $("#studyForm");
    	
    	$studyModalLabelNew 				= $("#studyModalLabelNew");
    	$studyModalLabelUpdate 				= $("#studyModalLabelUpdate");
    	$studyModalLabelDelete 				= $("#studyModalLabelDelete");
    	$dataEntryFormBottomMessageStudy 	= $("#dataEntryFormBottomMessageStudy");
    	$sure_for_delete_study 				= $("#sure_for_delete_study");
    	$study_title 						= $("#study_title");
    	$study_id 							= $("#study_id");
    	$study_institution_id 				= $("#study_institution_id");
    	$study_school_id					= $("#study_school_id");
    	$study_department_id 				= $("#study_department_id");
    	$error_study_title 					= $("#error_study_title");
    	$div_study_title 					= $("#div_study_title");
    	$department_of_study				= $("#department_of_study");

    	dashboard.modal_inherit.show();
    	
    	$editPeriodModal  	= $("#editPeriodModal");
    	$modal_scope		= $("#modal_scope");
    	$inherit_study_note = $("#inherit_study_note");
    	
    	initStudyList();
		dashboard.study.initDataTable(dashboard.selected_year, dashboard.department.selectedDepartmentId, "dummy");

    	$study_button_edit.on('click', function() {

    		let year= dashboard.selected_year;
    		let iid = dashboard.institution;
    		let departmentId = dashboard.department.selectedDepartmentId;
    		let studyId = dashboard.study.studyId;
  		 	dashboard.modal.initDataTable( year, iid,departmentId,studyId);

  		 	dashboard.modal_year.html(" " + dashboard.selected_year + "-" + (parseInt(dashboard.selected_year)+1));
  		 	 
  		 	dashboard.modal_header_img_institution.hide();
  		 	dashboard.modal_header_img_department.hide();
  		 	dashboard.modal_header_img_study.show();
  		 	dashboard.modal_header_institution.css("color", "black");
  		 	dashboard.modal_department_title.show();
  		 	dashboard.modal_header_department.html(dashboard.department.selectedDepartmentName);
  		 	dashboard.modal_header_department.css("color", "black");

  		 	dashboard.modal_study_title.show();
  		 	dashboard.modal_header_study.html("<b>" + $select_study.select2('data')[0].text + "</b>");
  		 	dashboard.modal_header_study.css("color", "red");

  		 	dashboard.modal_period_error_messages.html("");
   		 	dashboard.modal_period_error_messages.attr('class','alert alert-danger invisible');

   		 	$modal_scope.val("study");
			$("#study-edit-pane").show();
			$("#study-pane").hide();
       	});

		$("#study-edit-close").on('click',function(e){
			$("#study-pane").show();
			$("#study-edit-pane").hide();
		});

    	$select_study.on('change', function () {
    		let value = $select_study.val();
    		if (value === null) { value="";}
    		dashboard.study.studyId = value;
    		//console.log("Study Changed to:" + dashboard.study.studyId);
			if ( ! $.fn.DataTable.isDataTable( '#table_study' ) ) {
				dashboard.study.initDataTable(dashboard.selected_year, dashboard.department.selectedDepartmentId, dashboard.study.studyId);
			}
			else {
				let iid = $("#institutionId").val();
				dashboard.study.reloadDataTable(iid,dashboard.department.selectedDepartmentId,dashboard.study.studyId,dashboard.selected_year);
			}
        });

		$study_button_reset.on('click', function() {

    		let header = "Επαναφορά Ημερολογίου";
    		let confirm_text = "To επιλεγμένο Πρόγραμμα Σπουδών θα επανέλθει στο Ακαδημαϊκό Ημερολόγιο του Τμήματος<br/><br/>Είστε σίγουρος?";

	 		alertify.confirm(header, confirm_text , function (e) {
	    	    if (e) {
					let url = dashboard.siteurl + '/api/v1/programs/' + dashboard.study.studyId + '/calendar/reset/' + dashboard.selected_year;
	    	    	postResetStudyPeriods(url, dashboard.selected_year, dashboard.department.departmentId, dashboard.study.studyId);
	    	    }  
	 		}, function() {});	
    	});
    };

    dashboard.study.refreshStudyList = function (institutionId, departmentId) {
    	getStudyPeriodList(institutionId, departmentId);
    };

	dashboard.study.reloadDataTable = function (iid,departmentId, studyId, year) {
		if (studyId !== "" && studyId !== "dummy") {
			Study_DT.ajax.url(dashboard.siteurl + '/api/v1/dt/institution/' + iid + '/department/' + departmentId + '/program/' + studyId + '/calendar/' + year);
			Study_DT.ajax.reload(function (json) {
				set_display_results(json);
			});
		}
	}

	function getStudyPeriodList(institutionId, departmentId, programId) {

		let select_study = "Επιλέξτε Πρόγραμμα Σπουδών";

		$select_study.empty();
		if (departmentId === "") departmentId = "_all";
		$.ajax({
			url: dashboard.siteurl + '/api/v1/s21/programs.web/department/' + departmentId,
			cache: false
		})
			.done(function( data ) {

				$select_study.select2({
					placeholder: select_study,
					width: 'style', // need to override the changed default
					data : data.results,
					escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
					templateResult: formatRepo,
					templateSelection: formatRepoSelection
				});
				$select_study.val("").trigger("change");
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

	dashboard.study.initDataTable = function (year, departmentId,  studyId) {

	    if (studyId === "dummy") {
	    	//For Initial call in order to get an empty table
			departmentId = "dummy";
			year = "dummy";
			$study_button_edit.attr("disabled",true);
			$study_button_reset.attr("disabled", true);
			$study_datatable.DataTable().clear().destroy();
			$inherit_study_note.html("");
			return;
		}
    	else if (year === "" || departmentId === "" || studyId === "") {
    			$study_button_edit.attr("disabled",true);
    			$study_button_reset.attr("disabled", true);	
    			$study_datatable.DataTable().clear().destroy();
    			$inherit_study_note.html("");
    			return;
    	}
		$study_button_edit.attr("disabled",false);
		$study_button_reset.attr("disabled", false);
		let iid = $("#institutionId").val();
		Study_DT = $study_datatable.DataTable({
	        "bProcessing": false,
	        "bFilter": false,
	        "bPaginate": false,
	        "bInfo" : false,
            "oLanguage": dtLanguageGr,
            "order": [[1, 'asc']],  
            "ajax":  { 
        		"url" : dashboard.siteurl + '/api/v1/dt/institution/' + iid + '/department/' + departmentId + '/program/' + studyId + '/calendar/' + year,
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
				//set_display_results(json);
			}

        }); // DataTable init

		Study_DT.on('order.dt search.dt', function () {
			Study_DT.column(0, { search: 'applied', order: 'applied' }).nodes().each(function (cell, i) {
                cell.innerHTML = i + 1;
            });
        }).draw();
    }

	function  set_display_results(json) {
		$study_button_edit.attr("disabled", false);
		if (json.data.inherited === true) {
			$inherit_study_note.html("<i class=\"fas fa-circle\" style=\"color:green\"></i> προκαθορισμένο");
			$study_button_reset.attr("disabled", true);
		}
		else {
			$inherit_study_note.html("<i class=\"fas fa-circle\"  style=\"color:orangered\"></i> προσαρμογή");
			$study_button_reset.attr("disabled", false);
		}
	}

    function initStudyList() {
    	 
     $select_study.select2({
  	   	placeholder: "Επιλέξτε Πρόγραμμα Σπουδών"
  	 });
     	$select_study.val("");
     }

    function postResetStudyPeriods(postURL, year, departmentId, studyId) {

		$.ajax({
			url: postURL,
			type:"POST",
			contentType: "application/json; charset=utf-8",
			async: true,    	//Cross-domain requests and dataType: "jsonp" requests do not support synchronous operation

			success: function() {
				let message = {msg: "Academic Calendar Updated!", year: dashboard.selected_year, department: departmentId, study: studyId};
				dashboard.broker.trigger('refresh.page', [message]);
			},
			error : function() {
				alert("Πρόβλημα Συστήματος. Επικοινωνήστε με το διαχειριστή");
			}
		});
    }
    
})();
