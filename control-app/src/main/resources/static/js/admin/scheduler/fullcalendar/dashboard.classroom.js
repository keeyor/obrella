/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.classroom = dashboard.classroom || {};

	let classroomsDT;

    dashboard.classroom.departmentId = "";

    dashboard.classroom.init = function() {
		//dashboard.classroom.filterClassrooms();
		dashboard.classroom.loadEnabledClassroomsOnSearchBar()
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

	dashboard.classroom.loadEnabledClassroomsOnSearchBar = function ($elem) {

		let siteUrl          = dashboard.siteurl;
		let queryParams = new URLSearchParams(window.location.search);

		let url = siteUrl + '/api/v1/s2/class_enabled.web';
		let html = '';
		$.ajax({
			type: 'GET',
			url: url,
			dataType: 'json',
			success: function (data) {
				let selected_classroom_id = $("#classroom_id").val();
				if (selected_classroom_id !== '') {
					html += '<li><a class="dropdown-item" href="calendar">Όλες οι Αίθουσες</a></li>';
				}
				$.each(data.results, function (index, element) {
						queryParams.set("cl", element.id);
						html +=  '<li><a class="dropdown-item" href="calendar?' + queryParams + '">' + element.text +'</a></li>';
				});

				$elem.append(html);
			}
		});
	}

	dashboard.classroom.loadClassroomByReport = function () {

		let $classrooms_dt_el = $("#classroom_rdt");

		classroomsDT = $classrooms_dt_el.DataTable({
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
					"mData": "text",
					"mRender": function (data,type,row) {
						let classroomDtId = row["id"];
						let queryParams = new URLSearchParams(window.location.search);
						queryParams.set("cr", classroomDtId);
						queryParams.set("cv", dashboard.calendar.view);
						queryParams.set("sd",$("#start_date").val());
						queryParams.set("ed",$("#end_date").val());
						return '<a style="color: #005cbf" href="calendar?' + queryParams + '">' + data +'</a>';
					}
				}
			],
			"initComplete": function() {
				if ( this.api().data().length === 0) {
					$("#classroom_filter_row").hide();
				}
			}
		});
	}
	dashboard.classroom.addRowToReportTable = function(row) {
		$("#classroom_rdt").DataTable().row.add(row).draw();
		$("#classroom_filter_row").show();
	}
	dashboard.classroom.clearTable = function() {
		$("#classroom_rdt").DataTable().clear().draw();
	}

})();
