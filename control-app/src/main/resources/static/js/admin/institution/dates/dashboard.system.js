/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.system = dashboard.system || {};

    let System_DT;
    let $select_year;
    let current_academic_year = null;

    let $system_button_edit;
    let $table_system;
    
    let $modal_scope;
    
    let $editPeriodModal;

    dashboard.system.init = function () {

    	$select_year 		= $("#year_select2");
    	$system_button_edit = $("#system-button-edit");
    	$table_system 		= $("#table_system");
    	$modal_scope		= $("#modal_scope");
    	$editPeriodModal  	= $("#editPeriodModal");

    	InitControls();
    	RegisterListeners();

    	loader.initialize();

    	current_academic_year = dashboard.broker.getCurrentAcademicPeriod();
    	dashboard.system.getAvailableYearList(current_academic_year);

    	$select_year.on('change', function () {
    		dashboard.selected_year = $select_year.val();	 //cannot be null
            let message = {msg: "Year selected!", year: dashboard.selected_year};
            dashboard.broker.trigger('afterSelect.year', [message]);
        });

    	$system_button_edit.on('click', function() {
    		
    		 dashboard.modal.initDataTable( dashboard.selected_year, dashboard.institution,"");
    		 dashboard.modal_year.html(" " + dashboard.selected_year + "-" + (parseInt(dashboard.selected_year)+1));
    		 
    		 dashboard.modal_header_img_institution.show();
    		 dashboard.modal_header_img_department.hide();
    		 dashboard.modal_header_img_study.hide();
    		 
    		 dashboard.modal_header_institution.css("color", "red");
    		 dashboard.modal_department_title.hide();
    		 dashboard.modal_study_title.hide();
  		 	 
    		 dashboard.modal_period_error_messages.html(" ");
    		 dashboard.modal_period_error_messages.attr('class','alert alert-danger invisible');
       		 	
    		 dashboard.modal_inherit.hide();
    		 
    		 $modal_scope.val("system");
    		 
    		 $editPeriodModal.modal('show');
    	});

    	$("#calendar-edit").on('click',function(e){

            dashboard.modal.initDataTable( dashboard.selected_year, dashboard.institution,"");
            dashboard.modal_year.html(" " + dashboard.selected_year + "-" + (parseInt(dashboard.selected_year)+1));

            dashboard.modal_header_img_institution.show();
            dashboard.modal_header_img_department.hide();
            dashboard.modal_header_img_study.hide();

            dashboard.modal_header_institution.css("color", "red");
            dashboard.modal_department_title.hide();
            dashboard.modal_study_title.hide();

            dashboard.modal_period_error_messages.html(" ");
            dashboard.modal_period_error_messages.attr('class','alert alert-danger invisible');

            $("#calendar_card").hide();
            $("#calendar_edit_card").show();
        });
        $("#calendar-edit-close").on('click',function(e){
            $("#calendar_card").show();
            $("#calendar_edit_card").hide();
        });
    };


    function RegisterListeners() {

        $("#newCalendarBt").on('click', function(){
            alertify.prompt('Ακαδημαϊκό Έτος', 'Πλήκτρολογήστε το έτος εκκίνησης (πχ.2020)', '',
                function(evt, value) {
                    if (parseInt(value)> 2013) {
                        postCreateDefaultCalendar(dashboard.institution, value);
                    }
                    else {
                        alertify.error("Μή έγκυρο έτος: " + value);
                        return false;
                    }
                },
                function() {}
            );
        }) ;

    }
    function InitControls() {
/*        $select_year.select2({
            placeholder: 'Επιλέξτε Ακαδημαϊκό Έτος',
            minimumResultsForSearch: -1
        });*/
    }


    dashboard.system.initDataTable = function (institutionId, year) {

        System_DT = $table_system.DataTable({
	        "bProcessing": false,
	        "bDestroy": true,
	        "bFilter": false,
	        "bPaginate": false,
	        "bInfo" : false,	//hide Showing ....
            "oLanguage": dtLanguageGr,
            "order": [[1, 'asc']],
            "ajax":  {
            			"url": dashboard.siteurl + '/api/v1/dt/institution/' + institutionId + '/calendar/' + year,
            			"dataSrc":  "data.periods.period"
            },

            "columns": [
                { "mData": null , "sWidth": "40px", "bSortable": false },
                { "mData": "startDate", "sWidth": "0px", "bSortable": true, "bVisible":false },
                { "mData": "name", "sWidth": "380px", "bSortable": false},
                { "mData": "startDate", "sWidth": "180px", "bSortable": false },
                { "mData": "endDate", "sWidth": "200px", "bSortable": false }
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
	                    "render": function (data) {
	                        return data;
	                    }
                	},
                	{
                		"aTargets": [2],
	                    "render": function (data) {
	                        return '<span class="pb-0 mb-0" style="color: #003476;font-weight: 500">' + dashboard.broker.selectPeriod(data) + '</span>';
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
            		if (json.data.refId === null) {
            			$system_button_edit.attr("disabled",true);
            			//System Periods not set. Disable editing on Department & Studies too.
            		}
            		else {
            			$system_button_edit.attr("disabled",false);
            			dashboard.system_periods_set = 1;
            		}
              }
        }); // DataTable init

        System_DT.on('order.dt search.dt', function () {
            System_DT.column(0, { search: 'applied', order: 'applied' }).nodes().each(function (cell, i) {
                cell.innerHTML = i + 1;
            });
        }).draw();

    };
    dashboard.system.reloadDataTable = function (institutionId, year) {
        System_DT.ajax.url(dashboard.siteurl + '/api/v1/dt/institution/' + institutionId + '/calendar/' + year);
        System_DT.ajax.reload();
    }
    dashboard.system.getAvailableYearList = function (selectAcademicYear) {
    
    	$select_year.empty();
    	current_academic_year = dashboard.broker.getCurrentAcademicPeriod();
    	let institutionId = $("#institutionId").val();

        $.ajax({
            url: dashboard.siteurl + '/api/v1/s2/institution/' + institutionId + '/calendars',
            cache: false,
            dataType: "json"
        })
            .done(function( data ) {

            	$select_year.select2({
                    placeholder: 'Επιλέξτε Ακαδημαϊκό Έτος',
                    width: 'style',
                    data : data.results,
                    minimumResultsForSearch: -1,
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });
             	$select_year.val(selectAcademicYear).trigger("change");
            });

        function formatRepo (repo) {
            if (repo.loading) {
                return repo.text;
            }

            let markup = "<div class='select2-result-repository clearfix'>" +
                		 "<div class='select2-result-repository__meta'>" +
                		 "<div class='select2-result-repository__title'>" + repo.text + "</div>";


            if (repo.children) {

            }
            else {
                markup += "<div class='select2-result-repository__statistics'><div class='select2-result-repository__stargazers' style='font-size: 0.9em'>";
                if (repo.descr === "FULL") {
                	markup +="<i class=\"fas fa-user-tie\"></i><span class='text-muted'><small>" + "ακαδημαϊκό έτος: " + repo.text + "</small></span></div>";
                }
                else if (repo.descr === "EMPTY") {
                	markup += "<i class=\"fas fa-user-tie\"></i><span class='text-muted'><small>" + "-- κενό --" + "</small></span></div>";
                }
                markup +="</div></div></div>";
            }
            return markup;
        }

        function formatRepoSelection (repo) {
                return repo.text;
        }
    };

    function postCreateDefaultCalendar(institutionId, year) {

        $.ajax({

            url: dashboard.siteurl + '/api/v1/s2/institution/' + institutionId + '/calendar-default/' + year,
            type:"POST",
            contentType: "application/json; charset=utf-8",
            async: true,
            success: function() {
                    dashboard.system.getAvailableYearList(year);
                    alertify.notify("Δημιουργήθηκε νέο Ημερολόγιο για το έτος: " + year, "success");
            },
            error : function(msg) {
                alertify.error("Σφάλμα: " + msg.responseText);
            }
        });
    }
})();
