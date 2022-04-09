/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.department = dashboard.department || {};

	let departmentsDT;
    dashboard.department.departmentId = "";
    dashboard.department.schoolId = "";

    dashboard.department.init = function() {

    };

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

		let url = siteUrl + '/api/v1/s2/departments.web/school/dummy';
		let html = '';
		$.ajax({
			type: 'GET',
			url: url,
			dataType: 'json',
			success: function (data) {
/*				let selected_department_id = $("#department_id").val();
				if (selected_department_id !== '') {
					html += '<li><a class="dropdown-item" href="calendar">Όλα τα Τμήματα</a></li>';
				}*/
				$.each(data.results, function (index, element) {
					html += '<li class="dropdown-submenu">';
					html += '<a class="dropdown-item dropdown-toggle" href="#">' + element.text + '</a>';
					html += '<ul class="dropdown-menu">';
					$.each(element.children, function (index1, el) {
						queryParams.set("d", el.id);
						queryParams.set("cv", dashboard.calendar.view);
						queryParams.set("sd",$("#start_date").val());
						queryParams.set("ed",$("#end_date").val());
						html +=  '<li><a class="dropdown-item" href="calendar?' + queryParams + '">' + el.text +'</a></li>';
					});
					html += '</ul></li>';
				});
				$elem.append(html);
			}
		});
	}

	dashboard.department.loadDepartmentsByReport = function () {

		let $departments_dt_el = $("#departments_rdt");

		departmentsDT = $departments_dt_el.DataTable({
			pagingType: "simple",
			paging: false,
			lengthChange: false,
			ordering: false,
			"dom": '<"top">rt<"bottom">ip<"clear">',
			"language": {
				"search": "",
				"sInfo": "Εμφάνιση από _START_ έως _END_ από _TOTAL_",
				"oPaginate": {
					"sNext": "<i class='fas fa-angle-right'></i>",
					"sPrevious": "<i class='fas fa-angle-left'></i>"
				}
			},
			"columns": [
				{"data": "id"},
				{"data": "title"},
				{"data": "counter"}
			],
			"aoColumnDefs": [
				{
					"aTargets": [0,2],
					"sortable": false,
					"visible" : false
				},
				{
					"aTargets": [1],
					"mData": "id",
					"mRender": function (data,type,row) {
						let depDtId = row["id"];
						let queryParams = new URLSearchParams(window.location.search);
						queryParams.set("cv", dashboard.calendar.view);
						queryParams.set("sd",$("#start_date").val());
						queryParams.set("ed",$("#end_date").val());
						queryParams.set("d", depDtId);
						return '<a style="color: #005cbf" href="calendar?' + queryParams + '">' + data +'</a>';
					}
				}
			],
			"initComplete": function() {
				if ( this.api().data().length === 0) {
					$("#departments_filter_row").hide();
				}
			}
		});
	}

	dashboard.department.addRowToReportTable = function(row) {
		$("#departments_rdt").DataTable().row.add(row).draw();
	}
	dashboard.department.clearTable = function() {
		$("#departments_rdt").DataTable().clear().draw();
	}
})();
