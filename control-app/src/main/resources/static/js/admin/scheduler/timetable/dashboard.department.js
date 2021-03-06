/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.department = dashboard.department || {};

    dashboard.department.departmentId = "";

    dashboard.department.init = function() {
		//dashboard.department.filterSchoolDepartments(dashboard.institution, "");
		dashboard.department.filterSchoolDepartments();
    };

	dashboard.department.loadDepartmentsByReport = function () {

		let url = dashboard.siteurl + '/api/v2/s2/departments-only.web/authorized/scheduler';
		$("#dpFilters").html("");

		$.ajax({
			type: 'GET',
			url: url,
			dataType: 'json',
			success: function (data) {
					$.each(data.results, function (index, el) {
						let html = '<li class="list-group-item">' +
									'<a class="text-dark text-decoration-none filter-item" href="#" data-filter="department" data-target="' + el.id + '">' + el.text + '</a>' +
									'</li>';
						$("#dpFilters").append(html);
					});

				if (data.results.length < 1) {
					$("#dpFilters").hide();
					$("#depCanvasLink").hide();
				}
				else {
					$("#depCanvasLink").show();
					$("#no_dyna_filters").hide();
				}
			}
		});
	}

	dashboard.department.filterSchoolDepartments = function() {

		//! Important: Servers Dropdown submenu along with some style
		$("#department-filter").on('click','a.dropdown-toggle', function() {
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

		$.ajax({
			url: dashboard.siteurl + '/api/v2/s2/departments.web/authorized/scheduler',
			cache: false
		})
			.done(function( data ) {
					let schools = data.results;
					let html = '';
					schools.forEach(function(sc) {
						html += '<li class="dropdown-submenu">';
						html += '<a class="dropdown-item dropdown-toggle" href="#">' + sc.text + '</a>';
						html += '<ul class="dropdown-menu">';
						let departments = sc.children;
						departments.forEach(function(dp) {
							html +=  '<li><a class="dropdown-item filter-item" href="#" data-filter="department" data-target="' + dp.id + '">' + dp.text + '</a></li>';
						});
						html += '</ul></li>';
					});
					$("#department-filter").html(html);
			});
	}

})();
