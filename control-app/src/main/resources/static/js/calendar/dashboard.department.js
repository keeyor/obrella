/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.department = dashboard.department || {};

    dashboard.department.departmentId = "";
    dashboard.department.schoolId = "";
 


    dashboard.department.init = function() {
		dashboard.department.filterSchoolDepartments(dashboard.institution, dashboard.department.schoolId);
    };

	dashboard.department.filterSchoolDepartments = function(institution, school) {

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

		if (school === "") { school = "dummy";}
		$.ajax({
			url: dashboard.siteurl + '/api/v1/s2/departments.web/school/' + school,
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
							html +=  '<li><a style="font-size: 0.92em" class="dropdown-item filter-item" href="#" data-filter="department" data-target="' + dp.id + '">' + dp.text + '</a></li>';
						});
						html += '</ul></li>';
					});
					$("#department-filter").html(html);
			});
	}

})();
