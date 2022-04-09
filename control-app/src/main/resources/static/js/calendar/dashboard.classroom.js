/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.classroom = dashboard.classroom || {};

    dashboard.classroom.departmentId = "";

    dashboard.classroom.init = function() {
		dashboard.classroom.filterClassrooms();
    };

	dashboard.classroom.filterClassrooms = function() {

		$.ajax({
			url: dashboard.siteurl + '/api/v1/s2/class_enabled.web',
			cache: false
		})
		.done(function( data ) {
			let classrooms = data.results;
			let html = '';
			classrooms.forEach(function(room) {
				html +=  '<li><a style="font-size: 0.92em" class="dropdown-item filter-item" href="#" data-filter="classroom" data-target="' + room.id + '">' + room.text
						 + '<br/>' + room.subheader + '</a>' +
						 '</li>';
			});
			$("#classroom-filter").append(html);
			let all_classrooms_counter = $("#all_classrooms_counter").val();
			if (data.results.length < all_classrooms_counter) {
				$("#disabled_classrooms_warning").show();
			}
			else {
				$("#disabled_classrooms_warning").hide();
			}
		});
	}

})();
