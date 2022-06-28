/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.calendar = dashboard.calendar || {};

    let TimetableE_DT;

    let departments_report = [];
    let courses_report = [];
    let events_report = [];
    let staff_report = [];
    let classroom_report = [];
    dashboard.calendar.view;

    dashboard.calendar.LoadFullCalendar = function() {
        // set initial dates when left-side filters clicked (otherwise sets default = today )
        var param_start_date = $("#start_date").val();
        if (param_start_date === '') {
            param_start_date = null;
        }
        var calendarEl = document.getElementById('calendar');
        var calendar = new FullCalendar.Calendar(calendarEl, {
            locale: 'el',
            initialDate: param_start_date,
            initialView: dashboard.calendar.view,
            height: "auto",
            headerToolbar: {
                start: 'today prev next',
                center: 'title',
                end: 'dayGridMonth,listMonth timeGridWeek,listWeek timeGridDay,listDay'//end: 'listMonth ,listWeek ,listDay'
            },
            views: {
                dayGrid: {
                    // options apply to dayGrid
                },
                timeGrid: {
                    // options apply to timeGridDay views
                },
                week: {
                    // options apply to dayGridWeek and timeGridWeek views
                },
                day: {
                    // options apply to dayGridDay and timeGridDay views
                },
                listDay: {
                    buttonText: 'Ημέρα'
                },
                listWeek: {
                    buttonText: 'Εβδομάδα'
                },
                listMonth: {
                    buttonText: 'Μήνας',
                }
            },
            lazyFetching: false, //always get data again! >> otherwise does not refresh filters
            events: {
                url: dashboard.siteurl + '/api/v1/timetable_daterange/users/cal',
                method: 'GET',
                extraParams: function() { // a function that returns an object
                    return {
                        d: $("#departmentFilterId").val(),
                        c: $("#courseFilterId").val(),
                        s: $("#staffMemberFilterId").val(),
                        e: $("#scheduledEventFilterId").val(),
                        cr: $("#classRoomFilterId").val(),
                    }
                },
                failure: function() {
                    alert('there was an error while fetching events!');
                },
                success: function() {
                    departments_report = [];
                    courses_report = [];
                    events_report = [];
                    staff_report = [];
                    classroom_report = [];


                    //!important :: clear or otherwise filters area doubled in date change
                    $("#dpFilters").html("");
                    $("#coFilters").html("");
                    $("#stFilters").html("");
                    $("#evFilters").html("");
                    $("#clFilters").html("");

                    //console.log("start=" + moment(start).format("YYYY-MM-DD") + " end=" + moment(end).format("YYYY-MM-DD"));
                    $("#start_date").val(moment(calendar.view.activeStart).format("YYYY-MM-DD"));
                    $("#end_date").val(moment(calendar.view.activeEnd).format("YYYY-MM-DD"));

                    $("#view").val(calendar.view.type);
                    // Enable - Disable Month Range (disable if none of the major filter are selected
/*                    let departmentId = $("#departmentFilterId").val();
                    let courseId = $("#courseFilterId").val();
                    let staffId =  $("#staffMemberFilterId").val();
                    let classId = $("#classRoomFilterId").val();
                    if (courseId === '' && staffId === '' && departmentId === '' && classId === '') {
                        $(".fc-listMonth-button").attr("disabled",true);
                    }
                    else {
                        $(".fc-listMonth-button").attr("disabled",false);
                    }*/
                },
                color: 'yellow',   // a non-ajax option
                textColor: 'black' // a non-ajax option
            },
            eventMouseEnter: function(calEvent) {
                $("body").append(createToolTipForSchedule(calEvent));
                let $tooltipevent = $('.tooltipevent');
                $(this.el).mouseover(function() {
                     $(this).css('z-index', 10000);
                     $tooltipevent.fadeIn('500');
                     $tooltipevent.fadeTo('10', 1.9);
                }).mousemove(function(e) {
                    $tooltipevent.css('top', e.pageY + 10);
                    $tooltipevent.css('left', e.pageX + 20);
                });
            },
            eventMouseLeave: function() {
                $(this.el).css('z-index', 8);
                $('.tooltipevent').remove();
            },
            loading: function (isLoading) {
                if (isLoading) {
                    loader.showLoader();
                }
                else {
                    loader.hideLoader();
                }
            },
            eventContent: function(arg) {
                dashboard.calendar.view = calendar.view.type;
                appendReport(arg); // create filters report
                // if you need to customize event appearance
                //let arrayOfDomNodes = createEventDom(arg);
                // return { domNodes: arrayOfDomNodes }
            },
            datesSet: function() {
                dashboard.calendar.view = calendar.view.type;
                //console.log("dateSet:" + dashboard.calendar.view);
            }

        });
        calendar.setOption('validRange', {
            start: moment($("#start_date").val()),
            end: moment($("#end_date").val())
        });
        calendar.render();
    }



    function appendReport(calEvent) {

        let current_view = $("#view").val();

        $("#no_dyna_filters").show();
        $("#no_dyna_filters").html("Δημιουργία δυναμικών φίλτρων σε εξέλιξη. Παρακαλώ περιμένετε...");


        let res_title = calEvent.event._def.title;

        let departmentFilterId = $("#departmentFilterId").val()
        if ( departmentFilterId === '') {
            let department_title = calEvent.event._def.extendedProps.department;
            if (department_title != null && department_title !== "") {
                if (exists(departments_report, calEvent.event._def.extendedProps.departmentId) === false) {
                    let new_department = {};
                    new_department.id = calEvent.event._def.extendedProps.departmentId;
                    new_department.title = calEvent.event._def.extendedProps.department;
                    new_department.counter = 0;
                   // dashboard.department.addRowToReportTable(new_department);
                    let department_el = [calEvent.event._def.extendedProps.departmentId, calEvent.event._def.extendedProps.department];
                    departments_report.push(department_el);

                    $('#department-dd-header').html("<span class='fas fa-minus-circle'></span> | Τμήμα " + department_title);
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("d", new_department.id);

                    if (current_view !== undefined && current_view !== '') {
                        queryParams.set("view", current_view);
                    }
                    queryParams.delete("skip");
                    let html = '<li class="list-group-item">' +
                        '<a class="text-dark text-decoration-none" href="calendar?' + queryParams + '">' + new_department.title + '</a>' +
                        '</li>';
                    $("#dpFilters").append(html);
                }
            }
            $("#depCanvasLink").show();
            $("#dpFilters").show();
            $("#no_dyna_filters").hide();
        }

        let classroomFilterId = $("#classRoomFilterId").val()
        if ( classroomFilterId === '') {
                if (exists(classroom_report, calEvent.event._def.extendedProps.classroomId) === false) {
                    let new_classroom = {};
                    new_classroom.id = calEvent.event._def.extendedProps.classroomId;
                    new_classroom.title = calEvent.event._def.extendedProps.classroomName;
                    new_classroom.counter = 0;
                    //dashboard.classroom.addRowToReportTable(new_classroom);
                    let classsroom_el = [calEvent.event._def.extendedProps.classroomId, calEvent.event._def.extendedProps.classroomName];
                    classroom_report.push(classsroom_el);

                    $('#class-dd-header').html("<span class='fas fa-minus-circle'></span> | Μάθημα " + new_classroom.title);
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("cr", new_classroom.id);
                    queryParams.delete("skip");
                    if (current_view !== undefined && current_view !== '') {
                        queryParams.set("view", current_view);
                    }
                    let html = '<li class="list-group-item">' +
                        '<a class="text-dark text-decoration-none" href="calendar?' + queryParams + '">' + new_classroom.title + '</a>' +
                        '</li>';
                    $("#crFilters").append(html);
                }
            $("#classCanvasLink").show();
            $("#crFilters").show();
            $("#no_dyna_filters").hide();
        }

        let scheduledEventFilterId = $("#scheduledEventFilterId").val();
        let courseFilterId = $("#courseFilterId").val();

        if ( courseFilterId === '') {
            if (calEvent.event._def.extendedProps.type === "lecture") {
                if (exists(courses_report, calEvent.event._def.extendedProps.resourceId) === false) {
                    let new_course = {};
                    new_course.id = calEvent.event._def.extendedProps.resourceId;
                    new_course.title = res_title;
                    new_course.counter = 0;
                    new_course.department = [];
                    new_course.department.title = calEvent.event._def.extendedProps.department;
                    new_course.department.id = calEvent.event._def.extendedProps.departmentId;
                  //  dashboard.course.addRowToReportTable(new_course);
                    let course_el = [calEvent.event._def.extendedProps.resourceId, res_title];
                    courses_report.push(course_el);

                    $('#course-dd-header').html("<span class='fas fa-minus-circle'></span> | Μάθημα " + new_course.title);
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("c", new_course.id);
                    queryParams.delete("skip");
                    if (current_view !== undefined && current_view !== '') {
                        queryParams.set("view", current_view);
                    }
                    let html = '<li class="list-group-item">' +
                        '<a class="text-dark text-decoration-none" href="calendar?' + queryParams + '">' + new_course.title + '</a>' +
                        '</li>';
                    $("#coFilters").append(html);
                }
            }
            if (scheduledEventFilterId === '') {
                $("#courseCanvasLink").show();
                $("#no_dyna_filters").hide();
            }
        }
        if (scheduledEventFilterId === '') {
            if (calEvent.event._def.extendedProps.type === "event") {
                if (exists(events_report, calEvent.event._def.extendedProps.resourceId) === false) {
                    let new_event = {};
                    new_event.id = calEvent.event._def.extendedProps.resourceId;
                    new_event.title = res_title;
                    new_event.counter = 0;
                  //  dashboard.sevents.addRowToReportTable(new_event);
                    let event_el = [calEvent.event._def.extendedProps.resourceId, res_title];
                    events_report.push(event_el);

                    $('#event-dd-header').html("<span class='fas fa-minus-circle'></span> | Εκδήλωση " + new_event.title);
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("e", new_event.id);
                    queryParams.delete("skip");
                    if (current_view !== undefined && current_view !== '') {
                        queryParams.set("view", current_view);
                    }
                    let html = '<li class="list-group-item">' +
                        '<a class="text-dark text-decoration-none" href="calendar?' + queryParams + '">' + new_event.title + '</a>' +
                        '</li>';
                    $("#evFilters").append(html);
                }
            }
            if (courseFilterId === '') {
                $("#eventCanvasLink").show();
                $("#no_dyna_filters").hide();
            }
        }
        let userIsStaffMemberOnly = $("#userIsStaffMemberOnly").val();
        let staffMemberFilterId = $("#staffMemberFilterId").val();
        if (staffMemberFilterId === '' && userIsStaffMemberOnly === "false") {
            if (exists(staff_report, calEvent.event._def.extendedProps.supervisorId) === false) {
                    let new_staff = {};
                    new_staff.id = calEvent.event._def.extendedProps.supervisorId;
                    new_staff.name = calEvent.event._def.extendedProps.supervisor;
                    new_staff.counter = 0;
                    new_staff.department = [];
                    new_staff.department.title = calEvent.event._def.extendedProps.department;
                    new_staff.department.id = calEvent.event._def.extendedProps.departmentId;
                   // dashboard.staffmembers.addRowToReportTable(new_staff);
                    let staff_el = [calEvent.event._def.extendedProps.supervisorId, calEvent.event._def.extendedProps.supervisor];
                    staff_report.push(staff_el);

                $('#staff-dd-header').html("<span class='fas fa-minus-circle'></span> | Διδάσκων " + new_staff.name);
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.set("s",  new_staff.id);
                queryParams.delete("skip");
                if (current_view !== undefined && current_view !== '') {
                    queryParams.set("view", current_view);
                }
                let html = '<li class="list-group-item">' +
                    '<a class="text-dark text-decoration-none" href="calendar?' + queryParams + '">' + new_staff.name + '</a>' +
                    '</li>';
                $("#stFilters").append(html);
            }
            $("#staffCanvasLink").show();
            $("#no_dyna_filters").hide();
        }

        if (staffMemberFilterId !== '' && departmentFilterId !== '' && (courseFilterId !== '' || scheduledEventFilterId !== '') && classroomFilterId !== '') {
            $("#no_dyna_filters").html("δεν βρέθηκαν επιπλέον κριτήρια.");
        }
        if (staffMemberFilterId !== '' || departmentFilterId !== '' || courseFilterId !== '' || scheduledEventFilterId !== '' || classroomFilterId !== '') {
            $("#none_filter").hide();
        }

    }

    function exists(arr, search) {
        return arr.some(row => row.includes(search));
    }

    function createToolTipForSchedule(calEvent) {

      let tooltip = '<div class="tooltipevent" style="width:auto;height:20px;vertical-align:middle;background:white;position:absolute;z-index:10001;">'
                    + '<div class="card">'
                    + '<div class="card-header" style="background-color: #C0CCDA">' + calEvent.event.title;
                    if (calEvent.event._def.extendedProps.supervisor != null && calEvent.event._def.extendedProps.supervisor !== "") {
                        tooltip += '<br/><b>' + calEvent.event._def.extendedProps.supervisor + '</b>'
                    }
                    if (calEvent.event._def.extendedProps.department != null && calEvent.event._def.extendedProps.department !== "") {
                        tooltip += '<br/>Τμήμα ' + calEvent.event._def.extendedProps.department
                    }
                    tooltip += '</div>'
                    + '<div class="card-body pt-1">';
        if (calEvent.event._def.extendedProps.type === 'lecture') {
            if (calEvent.event._def.extendedProps.repeat === 'onetime') {
                tooltip = tooltip + '<div><b>\'Εκτακτη Διάλεξη Μαθήματος</b></div>';
            }
            else {
                tooltip = tooltip + '<div><b>Προγραμματισμένη Διάλεξη Μαθήματος</b></div>';
            }
        }
        else {
            tooltip = tooltip + '<div><b>Προγραμματισμένη Εκδήλωση</b></div>';
        }
        tooltip += '' + calEvent.event._def.extendedProps.classroomName;
        tooltip = tooltip + '<br/>' + moment(calEvent.event.start).format('LL') + ' - ' + moment(calEvent.event.start).format('HH:mm')
            + ' έως ' + moment(calEvent.event.end).format('HH:mm')

        if (calEvent.event._def.extendedProps.recording) {
            tooltip += "<div class='mt-3' style='border-top: #C0CCDA 1px solid'>" +
                "<img class='mt-2' width='16px' src='" + dashboard.siteurl + "/public/images/icons/IconRecAuto.png" + "' alt=''> Με Καταγραφή</b> ";
        }
        else {
            tooltip += "<div class='mt-3' style='border-top: #C0CCDA 1px solid'>" +
                "<img class='mt-2' width='16px' src='" + dashboard.siteurl + "/public/images/icons/IconRecOff.png" + "' alt=''> χωρίς Καταγραφή</b> ";
        }
        if (calEvent.event._def.extendedProps.access === 'open') {
            tooltip += "<img width='16px' src='" + dashboard.siteurl + "/public/images/icons/IconMetadosiStatusOpen.png" + "' alt=''> Πρόσβαση Ανοικτή</b> ";
        }
        else if (calEvent.event._def.extendedProps.access === 'sso') {
            tooltip += "<img width='16px' src='" + dashboard.siteurl + "/public/images/icons/IconMetadosiStatusUserName.png" + "' alt=''> Πρόσβαση με Ιδρυματικό Λογαριασμό</b> ";
        }
        else if (calEvent.event._def.extendedProps.access === 'password') {
            tooltip += "<img width='16px' src='" + dashboard.siteurl + "/public/images/icons/IconMetadosiStatusPassword.png" + "' alt=''> Πρόσβαση με Κωδικό Μετάδοσης</b> ";
        }
        tooltip = tooltip + '</div>'
        tooltip = tooltip + '</div>'
        tooltip = tooltip + '</div>'
        tooltip = tooltip + '</div>';

        return tooltip;
    }

    dashboard.calendar.reloadTimeTableLectureEDT = function() {

        loader.showLoader();
        $(".btn").attr("disabled",true);
        $("#department_load").addClass("disabled");
        $("#classroom_load").addClass("disabled");
        $("#editor_load").addClass("disabled");

        TimetableE_DT.ajax.reload( function() {
            enableNavigation();
        });

    }

    function enableNavigation() {

        let filters = ["department", "classroom", "editor"];

        for (let i = 0; i < filters.length; i++) {
            let $filter_name = "#" + filters[i] + "_filter_name";
            let $filter_clear = "#" + filters[i] + "_clear";
            let $filter_load = "#" + filters[i] + "_load";

            let filter_value = $($filter_name).val();
            if (filter_value !== '' && filter_value !== '-1') {
                $($filter_clear).show();
                let filter_text = dashboard.broker.getTextForFilter(filters[i]);
                $($filter_load).html("<span style='color:Dodgerblue;font-weight: bold'> " + filter_text + " "  + filter_value + "</span>");
            }
        }
        $(".btn").attr("disabled",false);
        $("#department_load").removeClass("disabled");
        $("#classroom_load").removeClass("disabled");
        $("#editor_load").removeClass("disabled");

        loader.hideLoader();
    }

    function SchedulerPDFHeader() {

        let html="";
        html += "Πρόγραμμα Μεταδόσεων/Καταγραφών\n" +
             $("#date_range_header").text();
        return html;
    }

    function createEventDom(arg) {
        let mainEL = document.createElement('div');
        mainEL.classList.add("row");

        let col1 = document.createElement('div');
        col1.classList.add("col-3");
        col1.innerHTML = arg.event._def.title;

        let col2 = document.createElement('div');
        col2.classList.add("col-9");
        col2.classList.add("smaller");

        let det1 = document.createElement("span");
        det1.classList.add("font-weight-bolder");
        det1.classList.add("mr-2");
        det1.innerHTML = arg.event._def.extendedProps.supervisor;


        /*      Add font-awesome example
                let i_element = document.createElement('i');
                i_element.classList.add("fas");
                i_element.classList.add("fa-map-marked-alt");
                i_element.classList.add("ml-5");
                i_element.classList.add("mr-1");
                i_element.classList.add("text-muted");*/

        let span_element = document.createElement('span');
        span_element.classList.add("text-muted");
        span_element.innerHTML = arg.event._def.extendedProps.classroomName;

        col2.appendChild(det1);
        col2.appendChild(span_element);

        mainEL.appendChild(col1);
        mainEL.appendChild(col2);
        let arrayOfDomNodes = [ mainEL ];

        return arrayOfDomNodes;
    }

})();