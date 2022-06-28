(function () {
    'use strict';

    dashboard.courses = dashboard.courses || {};
    let $course_el;

    dashboard.courses.init = function() {
        $course_el = $("#courses_s2");
        $course_el.select2({
            placeholder: "Επιλέξτε Τμήμα -> Διδάσκοντα -> Μάθημα",
            allowClear: true
        });
        $course_el.on('select2:select', function (e) {
            let data = e.params.data;
            let sel_course_Id = data.id;
            //trigger change event
            let message = {msg: "Course selected!", value: sel_course_Id};
            dashboard.broker.trigger('afterSelect.course', [message]);
        });
    }

    dashboard.courses.s2FillCoursesByTeachingStaff = function(staffId) {
        $.ajax({
            url:  dashboard.siteurl + '/api/v1/s2/courses.web/staff/' + staffId,
            cache: false
        })
            .done(function( data ) {
                $course_el.select2({
                    placeholder: '-- Επιλέξτε Μάθημα ή αφήστε κενό για όλα τα Μαθήματα --',
                    allowClear: true,
                    width: 'style', // need to override the changed default
                    data : data.results,
                    dropdownParent: $("#staffCourseSelectModal"),
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });
                $course_el.val("").trigger("change");
            });

        function formatRepo (repo) {
            if (repo.loading) {
                return repo.text;
            }

            let markup = "<div class='select2-result-repository clearfix'>" +
                "<div class='select2-result-repository__meta'>" +
                "<div class='select2-result-repository__title'> " + repo.text + "</div>";

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
