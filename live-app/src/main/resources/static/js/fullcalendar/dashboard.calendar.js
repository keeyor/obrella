/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.calendar = dashboard.calendar || {};

    let departments_report = [];
    let courses_report = [];
    let events_report = [];
    let staff_report = [];

    dashboard.calendar.view;

    dashboard.calendar.LoadFullCalendar = function() {

        let $startDateHf = $("#start_date");
        let $endDateHf = $("#end_date");

        let param_start_date = $startDateHf.val();
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
                // end: 'dayGridMonth,listMonth timeGridWeek,listWeek timeGridDay,listDay'
                end: 'listMonth,listWeek,listDay'
            },
            views: {
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
            lazyFetching: false,
            events: {
                url: dashboard.siteurl + '/api/v1/timetable_daterange/users/cal',
                method: 'GET',
                extraParams: function() { // a function that returns an object
                    return {
                        d: $("#departmentFilterId").val(),
                        c: $("#courseFilterId").val(),
                        s: $("#staffMemberFilterId").val()
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

                    $("#dpFilters").html("");
                    $("#coFilters").html("");
                    $("#stFilters").html("");

                    // console.log("start=" + moment(calendar.view.activeStart).format("YYYY-MM-DD"));
                    $startDateHf.val(moment(calendar.view.activeStart).format("YYYY-MM-DD"));
                    $endDateHf.val(moment(calendar.view.activeEnd).format("YYYY-MM-DD"));

                    $("#view").val(calendar.view.type);
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
                appendReport(arg);
            },
            datesSet: function() {
                dashboard.calendar.view = calendar.view.type;
            }

        });
        calendar.setOption('validRange', {
            start: moment($startDateHf.val()),
            end: moment($endDateHf.val())
        });
        calendar.render();
    }

    function appendReport(calEvent) {

        let $dpFiltersUl = $("#dpFilters");
        let $noDynaFiltersDiv = $("#no_dyna_filters");
        let current_view = $("#view").val();
        let param_start_date = $("#start_date").val();

        $noDynaFiltersDiv.show();
        $noDynaFiltersDiv.html("Δημιουργία δυναμικών φίλτρων σε εξέλιξη. Παρακαλώ περιμένετε...");

        let res_title = calEvent.event._def.title;

        let departmentFilterId = $("#departmentFilterId").val();
        if ( departmentFilterId === '' ) {
            let department_title = calEvent.event._def.extendedProps.department;
            if (department_title != null && department_title !== "") {
                if (exists(departments_report, calEvent.event._def.extendedProps.departmentId) === false) {
                    let new_department = {};
                    new_department.id = calEvent.event._def.extendedProps.departmentId;
                    new_department.title = calEvent.event._def.extendedProps.department;
                    new_department.counter = 0;

                    let department_el = [calEvent.event._def.extendedProps.departmentId, calEvent.event._def.extendedProps.department];
                    departments_report.push(department_el);

                    $('#department-dd-header').html("<span class='fas fa-minus-circle'></span> | Τμήμα " + department_title);
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("d", new_department.id);
                    if (current_view !== undefined && current_view !== '') {
                        queryParams.set("view", current_view);
                    }
                    if (param_start_date !== undefined && param_start_date !== '') {
                        queryParams.set("sd",param_start_date);
                    }
                    queryParams.delete("skip");
                    let html = '<li class="list-group-item"><a class="text-dark text-decoration-none" href="calendar?' + queryParams + '">' + new_department.title + '</a></li>';
                    $dpFiltersUl.append(html);
                }
            }
            $("#depCanvasLink").show();
            $dpFiltersUl.show();
            $noDynaFiltersDiv.hide();
        }

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

                    let course_el = [calEvent.event._def.extendedProps.resourceId, res_title];
                    courses_report.push(course_el);
                    $('#course-dd-header').html("<span class='fas fa-minus-circle'></span> | Μάθημα " + new_course.title);
                    let queryParams = new URLSearchParams(window.location.search);
                    queryParams.set("c", new_course.id);
                    if (current_view !== undefined && current_view !== '') {
                        queryParams.set("view", current_view);
                    }
                    if (param_start_date !== undefined && param_start_date !== '') {
                        queryParams.set("sd",param_start_date);
                    }
                    queryParams.delete("skip");
                    let html = '<li class="list-group-item"><a class="text-dark text-decoration-none" href="calendar?' + queryParams + '">' + new_course.title + '</a></li>';
                    $("#coFilters").append(html);
                }
            }

        }

        let staffMemberFilterId = $("#staffMemberFilterId").val();
        if (staffMemberFilterId === '') {
            if (exists(staff_report, calEvent.event._def.extendedProps.supervisorId) === false) {
                let new_staff = {};
                new_staff.id = calEvent.event._def.extendedProps.supervisorId;
                new_staff.name = calEvent.event._def.extendedProps.supervisor;
                new_staff.counter = 0;
                new_staff.department = [];
                new_staff.department.title = calEvent.event._def.extendedProps.department;
                new_staff.department.id = calEvent.event._def.extendedProps.departmentId;

                let staff_el = [calEvent.event._def.extendedProps.supervisorId, calEvent.event._def.extendedProps.supervisor];
                staff_report.push(staff_el);

                $('#staff-dd-header').html("<span class='fas fa-minus-circle'></span> | Διδάσκων " + new_staff.name);
                let queryParams = new URLSearchParams(window.location.search);
                if (current_view !== undefined && current_view !== '') {
                    queryParams.set("view", current_view);
                }
                if (param_start_date !== undefined && param_start_date !== '') {
                    queryParams.set("sd",param_start_date);
                }
                queryParams.set("s",  new_staff.id);
                queryParams.delete("skip");
                let html = '<li class="list-group-item"><a class="text-dark text-decoration-none" href="calendar?' + queryParams + '">' + new_staff.name + '</a></li>';
                $("#stFilters").append(html);
            }
            $("#staffCanvasLink").show();
            $noDynaFiltersDiv.hide();
        }

        if (staffMemberFilterId !== '' && departmentFilterId !== '' && courseFilterId !== '' ) {
            $noDynaFiltersDiv.html("δεν βρέθηκαν επιπλέον κριτήρια.");
        }
        if (staffMemberFilterId !== '' || departmentFilterId !== '' || courseFilterId !== '') {
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


})();