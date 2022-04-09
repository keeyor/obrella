(function () {
    'use strict';

    dashboard.courses = dashboard.courses || {};
    let $course_el;

    dashboard.courses.init = function () {
        $course_el = $("#courses_s2");

        $course_el.empty();
        let sel_course_id     = $("#course_id").val();
        let editor_id         = $("#editor_id").val();

        dashboard.courses.s2getAuthorizedCourses(sel_course_id);

        InitEvents();
    };

    function InitEvents() {

        $("#categories_sync").on('click',function(){
            let $sel2_element = $("#event_cats");
            let courseId = $("#course_id").val();
            dashboard.courses.getAndSetCourseCategories(courseId,$sel2_element);
        });

        $course_el.on('select2:select', function (e) {
            let data = e.params.data;
            let sel_course_Id = data.id;
            $("#course_id").val(sel_course_Id);
            $("#classrooms_s2").empty();
            //trigger change event
            let message = {msg: "Course selected!", value: sel_course_Id};
            dashboard.broker.trigger('afterSelect.course', [message]);
        });

        $('#CourseInfoModal').on('show.coreui.modal', function (e) {
            let course_id = $("#course_id").val();
            if (course_id != null && course_id !== "") {
                dashboard.courses.getCourseInfo(course_id);
            }
            else {
                $('#CourseInfoModal').modal("dispose");
            }
        })
    }
    dashboard.courses.s2getAuthorizedCourses = function(courseId) {

        $.ajax({
            url:  dashboard.siteUrl + '/api/v1/s2/courses.web/authorized/content',
            cache: false
        })
            .done(function( data ) {
                $course_el.select2({
                    placeholder: 'Επιλέξτε Μάθημα',
                    width: 'style', // need to override the changed default
                    data : data.results,
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });

                $course_el.val(courseId).trigger("change");

                let message = {msg: "Courses afterInit!", value: courseId};
                dashboard.broker.trigger('afterInit.course', [message]);
            });

        function formatRepo (repo) {
            if (repo.loading) {
                return repo.text;
            }

            let markup = "<div class='select2-result-repository clearfix'>" +
                "<div class='select2-result-repository__meta'>" +
                "<div class='select2-result-repository__title'><i class=\"fas fa-user-tie\"></i> " + repo.text + "</div>";

            if (repo.children) {

            }
            else {
                markup += "<div class='select2-result-repository__statistics'>" +
                    "<div class='select2-result-repository__stargazers'>" + repo.subheader + " </div>" +
                    "</div>" +
                    "</div></div>";
            }
            return markup;
        }

        function formatRepoSelection (repo) {
            if (!repo.text.startsWith("Επιλέξτε"))
                return "<span style='font-weight: 600'>" + repo.text + "</span><span> - " + repo.subheader + "</span>";
            else
                return repo.text
        }
    }
    dashboard.courses.fillCourseSelect = function(staffId, courseId, editorId) {

        $.ajax({
            url:  dashboard.siteUrl + '/api/v1/s2/courses.web/staff/' + staffId + '/user/' + editorId,
            cache: false
        })
            .done(function( data ) {
                $course_el.select2({
                    placeholder: 'Επιλέξτε Μάθημα',
                    width: 'style', // need to override the changed default
                    data : data.results,
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });

                $course_el.val(courseId).trigger("change");

                let message = {msg: "Courses afterInit!"};
                dashboard.broker.trigger('afterInit.course', [message]);
            });

        function formatRepo (repo) {
            if (repo.loading) {
                return repo.text;
            }

            let markup = "<div class='select2-result-repository clearfix'>" +
                "<div class='select2-result-repository__meta'>" +
                "<div class='select2-result-repository__title'><i class=\"fas fa-user-tie\"></i> " + repo.text + "</div>";

            if (repo.children) {

            }
            else {
                markup += "<div class='select2-result-repository__statistics'>" +
                    "<div class='select2-result-repository__stargazers' style='font-size: 0.9em'>" + repo.subheader + " </div>" +
                    "</div>" +
                    "</div></div>";
            }
            return markup;
        }

        function formatRepoSelection (repo) {
            if (!repo.text.startsWith("Επιλέξτε"))
                return "<b>" + repo.text + "</b>";
            else
                return repo.text;
        }
    }
    dashboard.courses.getCourseInfo = function(courseId) {

        $.ajax({
            url:  dashboard.siteUrl + '/api/v1/course/' + courseId,
            cache: false
        })
        .done(function( data ) {
            let course_info_html="";
            course_info_html += "<table style='border: none;width: 100%'>";
            course_info_html += "<tr><th style='width: 40%;font-weight: 550;margin-bottom: 10px;vertical-align: top'>Τίτλος</th><td>" + data.title + "</td></tr>";
            course_info_html += "<tr><th style='width: 40%;font-weight: 550;margin-bottom: 10px;vertical-align: top'>Τμήμα</th><td>" + data.department.title + "</td></tr>";
            if (data.departmentsRelated != null) {
                let rel_departments='';
                $.each(data.departmentsRelated, function (index, el) {
                    rel_departments += el.title + ' '
                });
                course_info_html += "<tr><th style='width: 40%;font-weight: 550;margin-bottom: 10px;vertical-align: top'>Διατμηματικό Μάθημα</th><td>" + rel_departments + "</td></tr>";
            }
            course_info_html += "<tr><th style='width: 40%;font-weight: 550;margin-bottom: 10px;vertical-align: top'>Πρόγραμμα Σπουδών</th><td>" + data.studyProgramTitle + "</td></tr>";
            course_info_html += "<tr><th style='width: 40%;font-weight: 550;margin-bottom: 10px;vertical-align: top'>Κωδικός Γραμματείας</th><td>";
            if (data.scopeId !== "") {
                course_info_html += data.scopeId + "</td></tr>";
            }
            else {
                course_info_html += " - ";
            }
            course_info_html += "<tr><th style='width: 40%;font-weight: 550;margin-bottom: 5px;vertical-align: top'>Κωδικοί LMS</th><td>";
            let lms_codes="";
            $.each(data.lmsReferences, function (index, el) {
                lms_codes += (index>0 ? ', ' : '') + el.lmsId + ' [' + el.lmsCode + ']';
            });
            course_info_html += lms_codes + "</td></tr>";
            course_info_html += "</table>";
            $("#course_info_panel").html(course_info_html);
        });
    }

    dashboard.courses.getAndSetCourseCategories = function(courseId, $sel2_element) {

        $.ajax({
            url:  dashboard.siteUrl + '/api/v1/course/' + courseId,
            cache: false
        })
            .done(function( data ) {
                 let course_categories = data.categories;
                 if (course_categories != null && course_categories !== "") {
                     $sel2_element.val(course_categories).trigger("change");
                 }
                 else {
                     $sel2_element.val("").trigger("change");
                 }
            });
    }

    dashboard.courses.getAndSetPeriodNameByCourseAndDate = function(courseId, date, year, $sel2_period) {

        $.ajax({
            url:  dashboard.siteUrl + '/api/v1/course/' + courseId + '/date/' + date + '/year/' + year,
            cache: false
        })
            .done(function( data ) {
                if (data != null && data !== "") {
                    //console.log("PERIOD RETURNED:" + data);
                    $sel2_period.val(data).trigger("change");
                }
                else {
                    //console.log("no period returned");
                    $sel2_period.val("").trigger("change");
                }
            });
    }

})();
