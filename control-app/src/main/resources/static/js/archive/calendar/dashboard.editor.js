/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.editor = dashboard.editor || {};
    
	dashboard.editor.loadEditorsOnSearchBar = function($elem) {
		
		let queryParams = new URLSearchParams(window.location.search);
		let editor_id = $("#editor_id").val();
		let editor_name = $("#editor_name").val();
		let html = '';
		queryParams.delete("d");
		queryParams.delete("c");
		queryParams.delete("e");
		queryParams.delete("cr");
		queryParams.set("s", editor_id);
		if (dashboard.calendar.view !== undefined) {
			queryParams.set("cv", dashboard.calendar.view);
		}
		else {
			queryParams.delete("cv");
		}
		queryParams.set("sd",$("#start_date").val());
		queryParams.set("ed",$("#end_date").val());
		html +=  '<li><a class="dropdown-item" href="calendar?' + queryParams + '">' + editor_name +'</a></li>';
		$elem.append(html);
	}
})();
