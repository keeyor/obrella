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
        InitEvents();
    };
    function InitControls() {
        $select_year.select2({
            placeholder: 'Επιλέξτε Ακαδημαϊκό Έτος'
        });
    }
    function InitEvents() {
        $select_year.on('change', function () {
            dashboard.selected_year = $select_year.val();	 //cannot be null
            let message = {msg: "Year selected!", year: dashboard.selected_year};
            dashboard.broker.trigger('afterSelect.year', [message]);
        });
    }
    dashboard.system.initDataTable = function (institutionId, year) {

        System_DT = $table_system.DataTable({
	        "bProcessing": false,
	        "bDestroy": true,
	        "bFilter": false,
	        "bPaginate": false,
	        "bInfo" : false,
            "oLanguage": dtLanguageGr,
            "order": [[1, 'asc']],
            "ajax":  {
            			"url": dashboard.siteurl + '/api/v1/dt/institution/' + institutionId + '/calendar/' + year,
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
                        "visible" : false,
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
            ]
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

})();
