/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.system = dashboard.system || {};

    let $select_year;
    let current_academic_year = null;

    dashboard.system.init = function () {
    	$select_year 		= $("#year_select2");
    	InitControls();
    	RegisterListeners();
    	current_academic_year = dashboard.broker.getCurrentAcademicPeriod();
    	dashboard.system.getAvailableYearList(current_academic_year);
    };

    function RegisterListeners() {

        $('#radioBtn button').on('click', function(){
            var sel = $(this).data('title');
            var tog = $(this).data('toggle');
            $('#'+tog).prop('value', sel);

            $('button[data-toggle="'+tog+'"]').not('[data-title="'+sel+'"]').removeClass('active').addClass('notActive');
            $('button[data-toggle="'+tog+'"][data-title="'+sel+'"]').removeClass('notActive').addClass('active');

            let $_date_from = $("#_date_from");
            let $_date_to= $("#_date_to");

            let _from_date = $_date_from.val();
            let _m_from_date = moment(_from_date);

            let _to_date = $_date_to.val();

            if (sel === 'Day') {

                $_date_from.datepicker('update', _from_date);
                $_date_to.datepicker('update', _from_date);

                $("#datepicker").hide();
                $("#previous_in_date_range").show();
                $("#next_in_date_range").show();

                setDateRangeHeader("Πρόγραμμα Μεταδόσεων/Καταγραφών ",moment(_from_date).format('dddd') + ", " + moment(_from_date).format('LL'));

                $("#today_date_range").show();
                $("#thisweek_date_range").hide();
                $("#thismonth_date_range").hide();
            }
            else if (sel === 'Week') {

                let _m_week_start = _m_from_date.startOf('week');
                let _week_start = moment(_m_week_start).format('YYYY-MM-DD');
                let _week_start_ll = moment(_m_week_start).format('LL');

                let _m_week_end = _m_from_date.endOf('week');
                let _week_end = moment(_m_week_end).format('YYYY-MM-DD');
                let _week_end_ll = moment(_m_week_end).format('LL');

                setDateRangeHeader("Πρόγραμμα Μεταδόσεων/Καταγραφών ", _week_start_ll + " έως "  + _week_end_ll);

                $_date_from.datepicker('update', _week_start);
                $_date_to.datepicker('update', _week_end);

                $("#datepicker").hide();//.attr("disabled",true);
                $("#previous_in_date_range").show();
                $("#next_in_date_range").show();

                $("#today_date_range").hide();
                $("#thisweek_date_range").show();
                $("#thismonth_date_range").hide();

            }
            else if (sel === 'Month') {
                let _m_month_start = _m_from_date.startOf('month');
                let _month_start = moment(_m_month_start).format('YYYY-MM-DD');

                let _m_month_end = _m_from_date.endOf('month');
                let _month_end = moment(_m_month_end).format('YYYY-MM-DD');

                let _month_end_ll = moment(_month_end).format('MMMM YYYY');
                setDateRangeHeader("Πρόγραμμα Μεταδόσεων/Καταγραφών ", _month_end_ll);

                $_date_from.datepicker('update', _month_start);
                $_date_to.datepicker('update', _month_end);

                $("#datepicker").hide();//.attr("disabled",true);
                $("#previous_in_date_range").show();
                $("#next_in_date_range").show();

                $("#today_date_range").hide();
                $("#thisweek_date_range").hide();
                $("#thismonth_date_range").show();
            }
            else if (sel === 'Custom') {
                $("#previous_in_date_range").hide();
                $("#next_in_date_range").hide();

                let _start_ = moment(_from_date).format('YYYY-MM-DD');
                let _end = moment(_to_date).format('YYYY-MM-DD');

                setDateRangeHeader("Πρόγραμμα Μεταδόσεων/Καταγραφών ", moment(_start_).format('LL') + " έως "  + moment(_end).format('LL'));

                $_date_from.datepicker('update', _from_date);
                $_date_to.datepicker('update', _to_date)

                $("#datepicker").show();
                $("#today_date_range").hide();
                $("#thisweek_date_range").hide();
                $("#thismonth_date_range").hide();
            }
            if (sel !== 'Custom') {
                dashboard.calendar.reloadTimeTableLectureEDT();
            }
        })

        $("#gen-nav").on( "click", ".filter-item", function(event) {
            let filter = $(this).data("filter");
            let itemId = $(this).data("target");
            let itemName = $(this).text();
            let message = {msg: "Filter:" + filter + " Selected!", filter: filter, id: itemId, value: itemName};
            dashboard.broker.trigger('filter.select', [message]);
            event.preventDefault();
        });

        $(".clear-filter").on("click", "a", function(event) {
            let target = $(this).data("target");
            let message = {msg: "Filter De-Selected!", filter: target, id: "", value: ""};
            dashboard.broker.trigger('filter.select', [message]);
            event.preventDefault();
        });

        $select_year.on('change', function () {
            dashboard.selected_year = $select_year.val();	 //cannot be null
            let message = {msg: "Year selected!", year: dashboard.selected_year};
            dashboard.broker.trigger('afterSelect.year', [message]);
        });

        $("#previous_in_date_range").on('click',function(){

            let $_date_from = $("#_date_from");
            let $_date_to= $("#_date_to");

            let range_type = $("#happy").val(); //"$('input[type=radio][name=dateRangeOptions]:checked').val();
            let _from_date = $_date_from.val();

            if (range_type === "Day") {
               // console.log("previous day");
                let _m_from_date = moment(_from_date);
                let _m_prev_day = _m_from_date.subtract(1, 'day');
                let _prev_day = moment(_m_prev_day).format('YYYY-MM-DD');

                setDateRangeHeader("Πρόγραμμα Μεταδόσεων/Καταγραφών ",moment(_m_prev_day).format('dddd') + ", " + moment(_m_prev_day).format('LL'));


                $_date_from.datepicker('update', _prev_day);
                $_date_to.datepicker('update', _prev_day);
            }
            else if (range_type === "Week") {
                //console.log("previous week");
                let _from_date = $_date_from.val();
                let _m_from_date = moment(_from_date);
                let _m_prev_week = _m_from_date.subtract(1, 'week');
                let _prev_week = moment(_m_prev_week).format('YYYY-MM-DD');
                let _week_start_ll = moment(_m_prev_week).format('LL');
                $_date_from.datepicker('update', _prev_week);


                let _to_date = $_date_to.val();
                let _m_to_date = moment(_to_date);
                let _m_prev_to_week = _m_to_date.subtract(1, 'week');
                let _prev_to_week = moment(_m_prev_to_week).format('YYYY-MM-DD');
                let _week_end_ll = moment(_m_prev_to_week).format('LL');

                setDateRangeHeader("Πρόγραμμα Μεταδόσεων/Καταγραφών ",_week_start_ll + " έως "  + _week_end_ll);

                $_date_to.datepicker('update', _prev_to_week);
            }
            if (range_type === "Month") {
               //console.log("next month");
                let _from_date = $_date_from.val();
                let _m_from_date = moment(_from_date).subtract(1,'month');
                let _m_prev_month = _m_from_date.startOf('month');
                let _prev_month = moment(_m_prev_month).format('YYYY-MM-DD');
                $_date_from.datepicker('update', _prev_month);

                let _to_date = $_date_to.val();
                let _m_to_date = moment(_to_date).subtract(1,'month');
                let _m_prev_to_month = _m_to_date.endOf('month');
                let _prev_to_month = moment(_m_prev_to_month).format('YYYY-MM-DD');
                $_date_to.datepicker('update', _prev_to_month);

                let _month_end_ll = moment(_m_to_date).format('MMMM YYYY');
                setDateRangeHeader("Πρόγραμμα Μεταδόσεων/Καταγραφών ", _month_end_ll);
            }
            dashboard.calendar.reloadTimeTableLectureEDT();

        });
        $("#next_in_date_range").on('click',function(){

            let $_date_from = $("#_date_from");
            let $_date_to= $("#_date_to");

            let range_type = $("#happy").val(); // $('input[type=radio][name=dateRangeOptions]:checked').val();
            let _from_date = $_date_from.val();

            if (range_type === "Day") {
                //console.log("next day");
                let _m_from_date = moment(_from_date);
                let _m_next_day = _m_from_date.add(1, 'day');
                let _next_day = moment(_m_next_day).format('YYYY-MM-DD');

                setDateRangeHeader("Πρόγραμμα Μεταδόσεων/Καταγραφών ",moment(_m_next_day).format('dddd') + ", " + moment(_m_next_day).format('LL'));

                $_date_from.datepicker('update', _next_day);
                $_date_to.datepicker('update', _next_day);
            }
            else if (range_type === "Week") {
                //console.log("next week");
                let _from_date = $_date_from.val();
                let _m_from_date = moment(_from_date);
                let _m_next_week = _m_from_date.add(1, 'week');
                let _next_week = moment(_m_next_week).format('YYYY-MM-DD');
                let _week_start_ll = moment(_m_next_week).format('LL');
                $_date_from.datepicker('update', _next_week);

                let _to_date = $_date_to.val();
                let _m_to_date = moment(_to_date);
                let _m_next_to_week = _m_to_date.add(1, 'week');
                let _next_to_week = moment(_m_next_to_week).format('YYYY-MM-DD');
                let _week_end_ll = moment(_m_next_to_week).format('LL');

                setDateRangeHeader("Πρόγραμμα Μεταδόσεων/Καταγραφών ",_week_start_ll + " έως "  + _week_end_ll);

                $_date_to.datepicker('update', _next_to_week);
            }
            if (range_type === "Month") {
                //console.log("next month");
                let _from_date = $_date_from.val();
                let _m_from_date = moment(_from_date).add(1,'month');
                let _m_next_month = _m_from_date.startOf('month');
                let _next_month = moment(_m_next_month).format('YYYY-MM-DD');
                $_date_from.datepicker('update', _next_month);

                let _to_date = $_date_to.val();
                let _m_to_date = moment(_to_date).add(1,'month');
                let _m_next_to_month = _m_to_date.endOf('month');
                let _next_to_month = moment(_m_next_to_month).format('YYYY-MM-DD');
                $_date_to.datepicker('update', _next_to_month);

                let _month_end_ll = moment(_m_to_date).format('MMMM YYYY');
                setDateRangeHeader("Πρόγραμμα Μεταδόσεων/Καταγραφών ", _month_end_ll);
            }
            dashboard.calendar.reloadTimeTableLectureEDT();
        });
        $('#_date_from').datepicker().on('changeDate', function(e) {

            let $_date_from = $("#_date_from");
            let $_date_to= $("#_date_to");

            let _from_date = $_date_from.val();
            let _to_date = $_date_to.val();

            let _start_ = moment(_from_date).format('YYYY-MM-DD');
            let _end = moment(_to_date).format('YYYY-MM-DD');
            setDateRangeHeader("Πρόγραμμα Μεταδόσεων/Καταγραφών ", moment(_start_).format('LL') + " έως "  + moment(_end).format('LL'));

            dashboard.calendar.reloadTimeTableLectureEDT();
        });
        $('#_date_to').datepicker().on('changeDate', function(e) {

            let $_date_from = $("#_date_from");
            let $_date_to= $("#_date_to");

            let _from_date = $_date_from.val();
            let _to_date = $_date_to.val();

            let _start_ = moment(_from_date).format('YYYY-MM-DD');
            let _end = moment(_to_date).format('YYYY-MM-DD');
            setDateRangeHeader("Πρόγραμμα Μεταδόσεων/Καταγραφών ", moment(_start_).format('LL') + " έως "  + moment(_end).format('LL'));

            dashboard.calendar.reloadTimeTableLectureEDT();
        });

        $("#today_date_range").on('click',function(){

            let _from_date = new Date();
            let $_date_from = $("#_date_from");
            let $_date_to= $("#_date_to");

            $_date_from.datepicker('update', _from_date);
            $_date_to.datepicker('update', _from_date);

            //$('input[type=radio][name=dateRangeOptions][value="day"]').attr("checked","checked");
            $("#datepicker").hide(); //.attr("disabled",true);
            $("#previous_in_date_range").show();
            $("#next_in_date_range").show();

            setDateRangeHeader("Πρόγραμμα Μεταδόσεων/Καταγραφών ",moment(_from_date).format('dddd') + ", " + moment(_from_date).format('LL'));
            dashboard.calendar.reloadTimeTableLectureEDT();
        });
        $("#thisweek_date_range").on('click',function(){

            let _from_date = new Date();
            let _m_from_date = moment(_from_date);
            let $_date_from = $("#_date_from");
            let $_date_to= $("#_date_to");

            let _m_week_start = _m_from_date.startOf('week');
            let _week_start = moment(_m_week_start).format('YYYY-MM-DD');
            let _week_start_ll = moment(_m_week_start).format('LL');

            let _m_week_end = _m_from_date.endOf('week');
            let _week_end = moment(_m_week_end).format('YYYY-MM-DD');
            let _week_end_ll = moment(_m_week_end).format('LL');

            setDateRangeHeader("Πρόγραμμα Μεταδόσεων/Καταγραφών ",_week_start_ll + " έως "  + _week_end_ll);

            $_date_from.datepicker('update', _week_start);
            $_date_to.datepicker('update', _week_end);

            //$('input[type=radio][name=dateRangeOptions][value="week"]').attr("checked","checked");
            $("#datepicker").hide();
            $("#previous_in_date_range").show();
            $("#next_in_date_range").show();
            dashboard.calendar.reloadTimeTableLectureEDT();
        });
        $("#thismonth_date_range").on('click',function(){

            let _from_date = new Date();
            let _m_from_date = moment(_from_date);
            let $_date_from = $("#_date_from");
            let $_date_to= $("#_date_to");

            let _m_month_start = _m_from_date.startOf('month');
            let _month_start = moment(_m_month_start).format('YYYY-MM-DD');

            let _m_month_end = _m_from_date.endOf('month');
            let _month_end = moment(_m_month_end).format('YYYY-MM-DD');
            let _month_end_ll = moment(_month_end).format('MMMM YYYY');

            setDateRangeHeader("Πρόγραμμα Μεταδόσεων/Καταγραφών ", _month_end_ll);

            $_date_from.datepicker('update', _month_start);
            $_date_to.datepicker('update', _month_end);

            //$('input[type=radio][name=dateRangeOptions][value="month"]').attr("checked","checked");
            $("#datepicker").hide();
            $("#previous_in_date_range").show();
            $("#next_in_date_range").show();

            dashboard.calendar.reloadTimeTableLectureEDT();
        });

    }

    function InitControls() {

        $("#_date").datepicker({
            format: "yyyy-mm-dd",
            todayBtn: false,
            language: "el",
            autoclose: true,
            todayHighlight: false
        });

        $select_year.select2({
            placeholder: 'Επιλέξτε Ακαδημαϊκό Έτος'
        });
        $("#dayOfWeek_select2").select2({
            placeholder: 'Επιλέξτε Ημέρα',
            allowClear: true
        });
        $("#repeat_select2").select2({
            placeholder: 'Επιλέξτε Τύπο Επανάληψης',
            allowClear: true
        });

        $("#date-range-pick .input-daterange").datepicker({
            format: "yyyy-mm-dd",
            todayBtn: false,
            language: "el",
            autoclose: true,
            todayHighlight: false
        });

        let _from_date = new Date();
        let $_date_from = $("#_date_from");
        let $_date_to= $("#_date_to");

        $_date_from.datepicker('update', _from_date);
        $_date_to.datepicker('update', _from_date);

        setDateRangeHeader("Πρόγραμμα Μεταδόσεων/Καταγραφών ",moment(_from_date).format('dddd') + ", " + moment(_from_date).format('LL'));

        $("#datepicker").hide();
        $("#thisweek_date_range").hide();
        $("#thismonth_date_range").hide();

        $("#previous_in_date_range").show();
        $("#next_in_date_range").show();
        $("#today_date_range").show();
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

    function setDateRangeHeader(header, text) {

        let $date_range_header = $("#date_range_header");
        let html  = '<h6><i class="far fa-calendar-check ml-1 mr-2"></i>' + header
            html += '<i>: ' + text + ' </i>';
            html += '</h6>';
        $date_range_header.html(html);
    }

})();
