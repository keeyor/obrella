/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.study = dashboard.study || {};

    dashboard.study.studyId = "";
    
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
    	
    	//$study_button_insert = $("#study-button-insert");
    	
    	$add_study_button   = $("#add_study");
    	$edit_study_button  = $("#edit_study");
    	$trash_study_button = $("#trash_study");

    	$study_button_edit  = $("#study-button-edit");
    	$study_button_reset = $("#study-button-reset");
    	$study_datatable 	= $("#table_study");
    	
    	$StudyMessages		= $("#StudyMessages");

    	
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
    	
    	$editPeriodModal  = $("#editPeriodModal");
    	$modal_scope	= $("#modal_scope");
    	$inherit_study_note = $("#inherit_study_note");
    	
    	initStudyList();
    	
    	$study_button_edit.on('click', function() {
    		
  		 	dashboard.modal.initDataTable( dashboard.selected_year, dashboard.institution,dashboard.department.departmentId,$select_study.select2('data')[0].id);
  		 	
  		 	dashboard.modal_year.html(" " + dashboard.selected_year + "-" + (parseInt(dashboard.selected_year)+1));
  		 	 
  		 	dashboard.modal_header_img_institution.hide();
  		 	dashboard.modal_header_img_department.hide();
  		 	dashboard.modal_header_img_study.show();
  		 	
  		 	dashboard.modal_header_institution.css("color", "black");
  		 	dashboard.modal_department_title.show();
  		 	dashboard.modal_header_department.html($("#department_select2").select2('data')[0].text);
  		 	dashboard.modal_header_department.css("color", "black");
  		 	dashboard.modal_study_title.show();
  		 	dashboard.modal_header_study.html("<b>" + $select_study.select2('data')[0].text + "</b>");
  		 	dashboard.modal_header_study.css("color", "red");

  		 	dashboard.modal_period_error_messages.html("");
   		 	dashboard.modal_period_error_messages.attr('class','alert alert-danger hidden');
   		 
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
    		
    		console.log("Study Change:" +  value);
    		
    		if (value === "") {
    			$edit_study_button.attr("disabled",true);
    			$trash_study_button.attr("disabled",true);
    		}
    		else {
  			
    			$edit_study_button.attr("disabled",false);
    			$trash_study_button.attr("disabled",false);
    		}  		
    		if (dashboard.department.departmentId === "") {
    			$add_study_button.attr("disabled",true);
    		}
    		else {
    			$add_study_button.attr("disabled",false);
    		}
    		
    		//console.log("Study Changed to:" + dashboard.study.studyId);
    		initDataTable( dashboard.selected_year, dashboard.department.departmentId, dashboard.study.studyId);
    		
    		let message = {msg: "Study selected!", value: dashboard.department.departmentId};
            dashboard.broker.trigger('afterSelect.study', [message]);

        });

		$study_button_reset.on('click', function() {
    		
    		
    		let department_name = $("#department_select2").select2('data')[0].text;
    		let study_name = $select_study.select2('data')[0].text;
    		
    		let header = "<span class='glyphicon glyphicon-magnet' aria-hidden='true'></span>Επαναφορά Περιόδων<br/><small><span>Τμήμα: " + department_name + "</span><br/>Πρόγραμμα Σπουδών:<span style='color:red'> " + study_name + "</span></small>";
    		let confirm_text = "To Πρόγραμμα θα επανέλθει στις <b>προκαθορισμένες περιόδους του αντίστοιχου Τμήματος</b><br/><br/>Είστε σίγουρος?";
    	
    		
	 		alertify.confirm(header, confirm_text , function (e) {
	    	    if (e) {
					let url = dashboard.siteurl + '/api/v1/programs/' + dashboard.study.studyId + '/calendar/reset/' + dashboard.selected_year;
	    	    	postResetStudyPeriods(url, dashboard.selected_year, dashboard.department.departmentId, dashboard.study.studyId);
	    	    }  
	 		}, function() {});	
    	});
    };

    dashboard.study.setVal = function (year, departmentId, studyId) {
    	
    	dashboard.study.studyId = studyId;
    	//console.log("SET VAL STUDY:" + studyId);
    	$select_study.val(studyId).trigger("change");
    	
    };
    dashboard.study.refreshData = function (institutionId, departmentId) {
    	
    	getStudyPeriodList(institutionId, departmentId);
    };

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
				if (programId === "") { programId = "program_default";}
				$select_study.val(programId).trigger("change");
				let message = {msg: "Program afterInit!"};
				dashboard.broker.trigger('afterInit.Program', [message]);
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
	function initDataTable(year, departmentId,  studyId) {
 
    	if (year === "" || departmentId === "" || studyId === "")
    		{
    			$study_button_edit.attr("disabled",true);
    			$study_button_reset.attr("disabled", true);	
    			$study_datatable.DataTable().clear().destroy();
    			$inherit_study_note.html("");
    			return;
    	}
		let iid = $("#institutionId").val();
    	// console.log("InitDataTable for STudy:" + " dep:" + departmentId + " study:" + studyId);
    	
        let table= $study_datatable.DataTable({
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
        		"url" : dashboard.siteurl + '/api/v1/dt/institution/' + iid + '/department/' + departmentId + '/program/' + studyId + '/calendar/' + year,
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
            			$study_button_edit.attr("disabled", false);	
            			if (json.data.inherited === true) {
		            		$inherit_study_note.html("<i class=\"fas fa-circle\" style=\"color:green\"></i> προκαθορισμένο");
		            		$study_button_reset.attr("disabled", true);
		            	}
		            	else {
		            		$inherit_study_note.html("<i class=\"fas fa-circle\"  style=\"color:orangered\"></i> προσαρμογή");
		            		$study_button_reset.attr("disabled", false);
		            	}
          },
            "createdRow": function ( row, data, index ) {
            }
        }); // DataTable init

        table.on('order.dt search.dt', function () {
            table.column(0, { search: 'applied', order: 'applied' }).nodes().each(function (cell, i) {
                cell.innerHTML = i + 1;
            });
        }).draw();
        
        
        $('#table_study tbody').on( 'click', 'tr', function (event) {

            let data = table.row($(this)).data();

            let message = {msg: "Study Period selected!", value: data.id};
            dashboard.broker.trigger('study_period.select', [message]);
            event.preventDefault();
        } );

       
        
        return table;
    }
    function initStudyList() {
    	 
     $select_study.select2({
  	   placeholder: "Επιλέξτε Πρόγραμμα Σπουδών"
  	 });
     $select_study.val("");
     $select_study.trigger("change");
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
