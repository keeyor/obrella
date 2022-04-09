(function () {
    'use strict';

    dashboard.courses = dashboard.courses || {};
    let $course_s2;

    dashboard.courses.init = function () {
        $course_s2            = $("#courses_s2");
        let sel_course_id     = $("#_course").val();
        dashboard.courses.s2getAuthorizedCourses(sel_course_id);

        $course_s2.on('select2:select', function (e) {
            let data = e.params.data;
            let sel_course_Id = data.id;
            $("#_course").val(sel_course_Id);
            $("#_classroom").val("");
            $("#classrooms_s2").empty();
            //trigger change event
            let message = {msg: "Course selected!", value: sel_course_Id};
            dashboard.broker.trigger('afterSelect.course', [message]);
        });

    };

    dashboard.courses.s2getAuthorizedCourses = function(courseId) {

        $.ajax({
            url:  dashboard.siteurl + '/api/v1/s2/courses.web/authorized/scheduler',
            cache: false
        })
            .done(function( data ) {
                $course_s2.select2({
                    placeholder: 'Επιλέξτε Μάθημα',
                    width: 'style', // need to override the changed default
                    data : data.results,
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });

                $course_s2.val(courseId).trigger("change");

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

    dashboard.courses.select2course = function () {

        $course_s2.empty();
        let sel_supervisor_id = $("#_supervisor").val();
        let sel_course_id     = $("#_course").val();
        let editor_id         = $("#editor_id").val();

        if (sel_supervisor_id !== '') {
            dashboard.courses.fillCourseSelect(sel_supervisor_id,sel_course_id, editor_id);
        }
    };

    dashboard.courses.fillCourseSelect = function(staffId, courseId, editorId) {

        $.ajax({
            url:  dashboard.siteurl + '/api/v1/s2/courses.web/staff/' + staffId + '/user/' + editorId,
            cache: false
        })
            .done(function( data ) {
                $course_s2.select2({
                    placeholder: 'Επιλέξτε Μάθημα',
                    width: 'style', // need to override the changed default
                    data : data.results,
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });

                $course_s2.val(courseId).trigger("change");
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
})();
