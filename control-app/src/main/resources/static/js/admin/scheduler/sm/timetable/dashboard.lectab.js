/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.lectab = dashboard.lectab || {};

    let Timetable_DT;
    let $select_year;
    let current_academic_year = null;
    let first_run = 0;
    let StaffMembersDT;

    dashboard.lectab.init = function () {
    	$select_year 		= $("#year_select2");
    	InitControls();
    	RegisterListeners();
    	current_academic_year = dashboard.broker.getCurrentAcademicPeriod();
    	dashboard.lectab.getAvailableYearList(current_academic_year);
    };



    function RegisterListeners() {

        let $repeat_s2      =  $("#repeat_select2");
        let $dayOfWeek_s2   = $("#dayOfWeek_select2");

        $select_year.on('change', function () {
            dashboard.selected_year = $select_year.val();	 //cannot be null
            let message = {msg: "Year selected!", year: dashboard.selected_year};
            dashboard.broker.trigger('afterSelect.year', [message]);
        });

        $repeat_s2.on("change",function(){
            let val  = $repeat_s2.val();
            if (val === "onetime") {
                $dayOfWeek_s2.attr("disabled",true);
            }
            else {
                $dayOfWeek_s2.attr("disabled",false);
            }
            dashboard.repeat = val;
            let message = {msg: "Repeat Changed!", value: dashboard.repeat};
            dashboard.broker.trigger('afterSelect.repeat', [message]);
        });

        $dayOfWeek_s2.on("change",function(){
            let val  = $dayOfWeek_s2.val();
            if (val !== "") {
                $repeat_s2.val("regular").trigger("change");
                $repeat_s2.attr("disabled",true);
            }
            else {
                $repeat_s2.attr("disabled",false);
            }
            dashboard.dayOfWeek = val;
            let message = {msg: "DayOfWeek Changed!", value: dashboard.dayOfWeek};
            dashboard.broker.trigger('afterSelect.dayOfWeek', [message]);
        })
    }

    function InitControls() {
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

    }

    dashboard.lectab.getAvailableYearList = function (selectAcademicYear) {
    
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

    dashboard.lectab.initTimeTableLectureDT = function() {

        Timetable_DT = $("#table_timetable_lectures").DataTable( {
            "processing": true,
            "ajax": {
                contentType: "application/json; charset=utf-8",
                type : "POST",
                url: dashboard.siteurl + '/api/v1/timetable/dt/authorized/scheduler',
                data: function ( d ) {
                    d = constructScheduleQuery();
                    return JSON.stringify( d );
                }
            },
            responsive: true,
            language: dtLanguageGr,
            fixedHeader: true,
            order: [[5, 'asc']],
            pagingType: "full_numbers",
            pageLength : 25,
            "dom": '<"top"flB>rti<"bottom">p<"clear">',
            "columns": [
                {"data": null},
                {"data": "enabled"},
                {"data": "id"}, //2
                {"data": "repeat"}, //3
                {"data": "period"}, //4
                {"data": "dayOfWeek"}, //5
                {"data": "date"}, //6
                {"data": "startTime"}, //7
                {"data": "durationHours"}, //8
                {"data": "durationMinutes"}, //9
                {"data": "type"},   //10
                {"data": "department.title"}, //11
                {"data": "course.title"},   //12
                {"data": "supervisor.name"}, //13
                {"data": "classroom.name"}, //14
                {"data": "broadcast"}, //15
                {"data": "access"}, //16
                {"data": "recording"}, //17
                {"data": "publication"}, //18
                {"data": "editor"}, //19
                {"data": "id"}, //20
                {"data": "id"}, //21
                {"data": "cancellations"}, //22
                {"data": "dayOfWeek"}, //23
            ],
            "aoColumnDefs": [
                {
                    "aTargets": [0],
                    "sortable": false
                },
                {
                    "aTargets": [2,4,6,9,10,13,22,23],
                    "sortable": false,
                    "visible": false,
                    "sWidth": "0px"
                },
                {
                    "aTargets": [1],
                    "mData": "enabled",
                    responsivePriority: 1,
                    "sortable": false,
                    "className" : "dt-center",
                    "mRender": function (data,type,row) {
                        if (data) {
                            let repeat = row["repeat"];
                            let cancellations = row["cancellations"];
                            if (repeat === "onetime" && cancellations != null && cancellations.length >0) {
                                return '<i class="fas fa-circle" style="color:orangered"  title="Ανενεργό Πρόγραμμα"></i>';
                            }
                            return '<i class="fas fa-circle" style="color:greenyellow"  title="Ενεργό Πρόγραμμα"></i>';
                        }
                        else {
                            return '<i class="fas fa-circle" style="color:orangered"  title="Ανενεργό Πρόγραμμα"></i>';
                        }
                    }
                },
                {
                    "aTargets": [3],
                    "mData": "repeat",
                    responsivePriority: 3,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        let hiddenDataForSorting = '<span style="display:none">' + data + '</span>';
                        if (data === "regular") {
                            return hiddenDataForSorting + '<img title="τακτική" src="' + dashboard.siteurl +   '/public/images/icons/IconEidosTaktiki.png" width="15px " alt="τακτική"  />';
                        }
                        else {
                            return hiddenDataForSorting + '<img title="έκτακτη"  src="' + dashboard.siteurl +   '/public/images/icons/IconEidosEktakti.png" width="15px " alt="έκτακτη"  />';
                        }
                    }
                },
                {
                    "aTargets": [4],
                    "mData": "period",
                    responsivePriority: 3,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        return dashboard.broker.selectPeriod(data);
                    }
                },
                {
                    "aTargets": [5],
                    "mData": "dayOfWeek" ,
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data,type, row) {
                        let repeat = row["repeat"];
                        let cancellations = row["cancellations"];
                        if (repeat === "regular") {
                            let cancellation_mark = "";
                            if (cancellations != null && cancellations.length >0) {
                                cancellation_mark = '<i class="fas fa-exclamation font-weight-bolder ml-1" style="color: red" title="Υπάρχουν Ακυρώσεις"></i>';
                            }
                            let ret = '';
                            ret =   '<span style="display:none">' + dashboard.broker.selectNumberedDayOfWeek(data) + row.startTime + '</span>';
                            return ret + '<b>' + dashboard.broker.selectDayOfWeek(data) + '</b>' + cancellation_mark;
                        }
                        else {
                            return "";
                        }
                    }
                },
                {
                    "aTargets": [6],
                    "mData": "date",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data,type,row) {
                        let cancellations = row["cancellations"];
                        if (data != null && data !== "") {
                            let display_date = moment(data).format('ll');
                            let hiddenDataForSorting = '<span style="display:none">' + data + '</span>';
                            let cancellation_mark = "";
                            if (cancellations != null && cancellations.length >0) {
                                cancellation_mark = '<i class="fas fa-exclamation font-weight-bolder ml-1" style="color: red" title="Ακύρωση"></i>';
                            }
                            return hiddenDataForSorting + '<span><b>' + display_date + '</b></span>' + cancellation_mark;
                        }
                        else {
                            return "";
                        }
                    }
                },
                {
                    "aTargets": [7],
                    "mData": "startTime",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        return data;
                    }
                },
                {
                    "aTargets": [8],
                    "mData": "durationHours",
                    responsivePriority: 2,
                    "className" : "dt-center",
                    "mRender": function (data,type,row) {
                        let val = data + " Ώ ";
                        if (row["durationMinutes"] !== 0) {
                            val +=row["durationMinutes"] + " λ"
                        }
                        return val;
                    }
                },
                {
                    "aTargets": [11],
                    "mData": "department.title",
                    responsivePriority: 2,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        return data;
                    }
                },
                {
                    "aTargets": [12],
                    "mData": "course.title",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        return '<span style="color: #003476;font-weight: 600">' + data + '</span>';
                    }
                },
                {
                    "aTargets": [13],
                    "mData": "supervisor.name",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        return data;
                    }
                },
                {
                    "aTargets": [14],
                    "mData": "classroom.name",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        return data;
                    }
                },
                {
                    "aTargets": [15],
                    "mData": "broadcast",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        let hiddenDataForSorting = '<span style="display:none;">' + data + '</span>';
                        if (data) {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl +   '/public/images/icons/IconMetadosiOn.png" width="20px" alt=""  />';
                        }
                        else {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl +   '/public/images/icons/IconMetadosiOff.png" width="20px" alt=""  />';
                        }
                    }
                },
                {
                    "aTargets": [16],
                    "mData": "access",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        let hiddenDataForSorting = '<span style="display:none">' + data + '</span>';
                        if (data === "open") {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl + '/public/images/icons/IconMetadosiStatusOpen.png" width="20px" alt=""  />';
                        } else if (data === "sso") {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl + '/public/images/icons/IconMetadosiStatusUserName.png" width="20px" alt=""  />';
                        } else if (data === "password") {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl + '/public/images/icons/IconMetadosiStatusPassword.png" width="20px" alt=""  />';
                        } else  {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl + '/public/images/icons/IconProsvasiNA.png" width="20px" alt=""  />';
                        }
                    }
                },
                {
                    "aTargets": [17],
                    "mData": "recording",
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        let hiddenDataForSorting = '<span style="display:none;">' + data + '</span>';
                        if (data) {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl +   '/public/images/icons/IconRecAuto.png" width="20px" alt=""  />';
                        }
                        else {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl +   '/public/images/icons/IconRecOff.png" width="20px" alt=""  />';
                        }
                    }
                },
                {
                    "aTargets": [18],
                    "mData": "publication",
                    "className" : "dt-center",
                    "mRender": function (data) {
                        let hiddenDataForSorting = '<span style="display:none">' + data + '</span>';
                        if (data === "public") {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl + '/public/images/icons/IconRecStatusPublic.png" width="20px" alt=""  />';
                        } else if (data === "private") {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl + '/public/images/icons/IconRecStatusPrivate.png" width="20px" alt=""  />';
                        } else  {
                            return hiddenDataForSorting + '<img src="' + dashboard.siteurl + '/public/images/icons/IconProsvasiNA.png" width="20px" alt=""  />';
                        }
                    }
                },
                {
                    "aTargets": [19],
                    "mData": "editor",
                    "sortable": false,
                    "className" : "dt-center",
                    "mRender": function (data) {
                        let editor_name = data.name.split(" ");
                        let badge = '<span title="' + editor_name + '" class="badge rounded-pill bg-warning">' + editor_name[0].charAt(0);
                        if (editor_name.length>1) {
                            badge += editor_name[1].charAt(0)
                        }
                        badge += '</span>';
                        return badge;
                    }
                },
                {
                    "aTargets": [20],
                    responsivePriority: 1,
                    "mData": "id",
                    "sortable": false,
                    "mRender": function (data) {
                        return '<a role="button" class="btn blue-btn-wcag-bgnd-color btn-pill btn-sm edit-schedule" title="επεξεργασία μετάδοσης" href="schedule?id=' + data + '"><i class="fas fa-edit text-white"></i></a>';
                    }
                },
                {
                    "aTargets": [21],
                    responsivePriority: 1,
                    "mData": "id",
                    "sortable": false,
                    "mRender": function (data) {
                        return ' <a role="button" class="btn btn-sm btn-warning btn-pill edit-schedule"  title="αντιγραφή μετάδοσης σε νέα" href="schedule?cloneId=' + data + '"><i class="far fa-clone"></i></a>';
                    }
                },
                {
                    "aTargets": [23],
                    "mData": "dayOfWeek" ,
                    responsivePriority: 1,
                    "className" : "dt-center",
                    "mRender": function (data,type, row) {
                        let repeat = row["repeat"];
                        if (repeat === "regular") {
                            return  dashboard.broker.selectDayOfWeek(data);
                        }
                        else {
                            return "";
                        }
                    }
                },
            ],
            buttons: [
                {
                    extend: 'pdf',
                    exportOptions: {
                        columns: [ 4,23,6,7,8,11,12,13,14],
                        stripHtml: true,
                    },
                    text:'<span title="Εξαγωγή σε PDF"><i class="fas fa-download"></i> PDF</span>',
                    className: 'ml-2 blue-btn-wcag-bgnd-color text-white'
                }
            ],
            "initComplete": set_display_results
        });
        Timetable_DT.on( 'order.dt search.dt', function () {
            Timetable_DT.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
                cell.innerHTML = i+1;
            } );
        }).draw();
        Timetable_DT.on( 'page.dt', function () {
            set_display_results();
        } );
    }

    function  set_display_results() {
        let $clear_filters = $(".clear-filters");
        // disable clear filters button
        $clear_filters.attr("disabled", true);
        let page  = Timetable_DT.page.len();
        let info  = Timetable_DT.page.info();
        let total = Timetable_DT.rows().count();
        let page_start = (info.page*page + 1);
        let page_end =  page_start + page;
        if (page_end > total) {
            page_end = total
        }
        $("#count_results").html('Εμφάνιση '+ page_start + ' ως ' + page_end + ' από '+ total + '');

        let filters = ["period", "department","repeat", "dow", "staff", "course"];

        for (let i = 0; i < filters.length; i++) {
            let $filter_name = "#" + filters[i] + "_filter_name";
            let $filter_clear = "#" + filters[i] + "_clear";
            let $filter_load = "#" + filters[i] + "_load";

            let filter_value = $($filter_name).val();
            if (filter_value !== '' && filter_value !== '-1') {
                $($filter_clear).show();
                $($filter_load).html("<span style='color:Dodgerblue;font-weight: bold'>" + filter_value + "</span>");
                //enable clear filters buttons if any filter not empty
                $clear_filters.attr("disabled", false);
            }
        }
    }

    function constructScheduleQuery() {
        let year
        let school_filter;
        let department_filter;
        let repeat_filter;
        let dow_filter;
        let type;
        let period_filter;
        let timetable_search_history = $("#search_filters").val();
        let d;
        let staff_filter, course_filter;

        type = "lecture";

        if (timetable_search_history === "" || first_run !== 0) {
            year = dashboard.selected_year;

            school_filter = $("#school_filter").val();
            if (school_filter == null || school_filter === "") {
                school_filter = "_all";
            }
            department_filter = $("#department_filter").val();
            if (department_filter == null || department_filter === "") {
                department_filter = "_all";
            }
            repeat_filter = $("#repeat_filter").val()
            dow_filter = $("#dow_filter").val()
            if (repeat_filter == null || repeat_filter === "") {
                repeat_filter = "_all"
            }
            if (repeat_filter === "onetime") {
                dow_filter = "_all"
            }
            if (dow_filter == null || dow_filter === "") {
                dow_filter = "_all";
            }
            period_filter = $("#period_filter").val();
            if (period_filter == null || period_filter === "") {
                period_filter = "_all";
            }
            staff_filter = $("#staff_filter").val();
            if (staff_filter == null || staff_filter === "") {
                staff_filter = "_all";
            }
            course_filter = $("#course_filter").val();
            if (course_filter == null || course_filter === "") {
                course_filter = "_all";
            }
           d = {
                year: year,
                type : type,
                repeat: repeat_filter,
                dayOfWeek : dow_filter,
                departmentId : department_filter,
                schoolId: school_filter,
                period: period_filter,
                supervisorId: staff_filter,
                courseId: course_filter
            }
        }
        else {
            let json_filters = JSON.parse(timetable_search_history);
             d = {
                year: json_filters.year,
                type : json_filters.type,
                repeat: json_filters.repeat,
                dayOfWeek : json_filters.dayOfWeek,
                departmentId : json_filters.departmentId,
                schoolId: json_filters.schoolId,
                period: json_filters.period,
                supervisorId: json_filters.supervisorId,
                courseId: json_filters.courseId
            }
          //  console.log(json_filters);

            $("#year_select2").val(json_filters.year);

            $("#dow_filter").val( (json_filters.dayOfWeek === "_all") ? "" : json_filters.dayOfWeek);
            $("#dow_filter_name").val(dashboard.broker.selectDayOfWeek(json_filters.dayOfWeek));

            $("#repeat_filter").val( (json_filters.repeat === "_all") ? "" : json_filters.repeat);
            $("#repeat_filter_name").val(dashboard.broker.selectRepeat(json_filters.repeat));

            $("#period_filter").val( (json_filters.period === "_all") ? "" : json_filters.period);
            $("#period_filter_name").val(dashboard.broker.selectPeriod(json_filters.period));

            $("#staff_filter").val( (json_filters.supervisorId === "_all") ? "" : json_filters.supervisorId);
            $("#staff_filter_name").val(json_filters.supervisorName);

            $("#course_filter").val( (json_filters.courseId === "_all") ? "" : json_filters.courseId);
            $("#course_filter_name").val(json_filters.courseTitle);

            //School differs cause we cannot get the title from the page: so fill it in session store
            $("#school_filter").val( (json_filters.schoolId === "_all") ? "" : json_filters.schoolId);
            $("#school_filter_name").val(json_filters.schoolTitle);

            //Department differs cause we cannot get the title from the page: so fill it in session store
            $("#department_filter").val( (json_filters.departmentId === "_all") ? "" : json_filters.departmentId);
            $("#department_filter_name").val(json_filters.departmentTitle);
        }

        first_run = 1;
        $("#search_filters").val(JSON.stringify(d));
        return d;
    }

    dashboard.lectab.reloadTimeTableLectureDT = function() {
        Timetable_DT.ajax.reload(set_display_results);
    }

    dashboard.lectab.InitAuthorizedInstitutionStaffMembers = function () {

        let $institutionStaffMembersDtElem = $("#StaffMembersSelectDataTable");
        StaffMembersDT = $institutionStaffMembersDtElem.DataTable({
            "ajax": dashboard.siteUrl + '/api/v2/dt/staff.web/authorized/content',
            "sDom": 'Zfrtip',
            "oListNav" : {
                sLetterClass : "btn abcdaire",
            },
            "columns": [
                {"data": "id"},
                {"data": "name"},
                {"data": "affiliation"},
                {"data": "department.title"}
            ],
            "language":  dashboard.dtLanguageGr,
            select: {
                style: 'single'
            },
            order: [[1, 'asc']],
            "pageLength": 10,
            "aoColumnDefs": [
                {
                    "aTargets": [0],
                    "mData": "id",
                    "visible": false,
                }
            ]
        });
    }; // Staff DataTable Init
})();
