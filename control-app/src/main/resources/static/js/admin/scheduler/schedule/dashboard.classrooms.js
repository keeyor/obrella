(function () {
    'use strict';

    dashboard.classrooms = dashboard.classrooms || {};
    let $class_el;
    let $class_s2;

    dashboard.classrooms.init = function () {
        $class_el = $("#_classroom");
        $class_s2 = $("#classrooms_s2");

        let schedule_type = $("#_type").val();
        if (schedule_type === "lecture") {
            dashboard.classrooms.select2class();
        }
        else {
            dashboard.classrooms.fillAllClassSelect($class_el.val());
        }
        $class_s2.on('select2:select', function (e) {
            let data = e.params.data;
            let sel_class_Id = data.id;
            $class_el.val(sel_class_Id);
            //trigger change event
            let message = {msg: "Classroom selected!"};
            dashboard.broker.trigger('afterSelect.classroom', [message]);
        });
    };

    dashboard.classrooms.select2class = function () {

        $class_s2.empty();
        let sel_supervisor_id = $("#_supervisor").val();
        let sel_course_id     = $("#_course").val();
        let sel_class_id      = $class_el.val();

        if (sel_supervisor_id !== '' && sel_course_id !== '') {
            dashboard.classrooms.fillClassSelect(sel_class_id,sel_supervisor_id,sel_course_id);
        }
    };

    dashboard.classrooms.fillClassSelect = function(classId, staffId, courseId) {

        $class_s2.empty();
        $.ajax({
             // url:  dashboard.siteurl + '/api/v1/s2/class.web/staff/' + staffId + '/course/' + courseId + '/usage/lectures',
            //Δημιουργεί προβλήματα το από πάνω: Όταν φύγει η αίοθουσα από το Τμήμα -> βγαίνει κενή η φόρμα
            url:  dashboard.siteurl + '/api/v1/s2/class.web/usage/lectures',
            cache: false
        })
            .done(function( data ) {
                $class_s2.select2({
                    placeholder: 'Επιλέξτε Χώρο',
                    width: 'style', // need to override the changed default
                    data : data.results,
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });

                $class_s2.val(classId).trigger("change");

                let message = {msg: "Classroom afterInit!"};
                dashboard.broker.trigger('afterInit.classroom', [message]);
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
        }``

        function formatRepoSelection (repo) {
                return "<span  style=\"font-weight: 500\">" + repo.text + "</span>";
        }
    }

    dashboard.classrooms.fillAllClassSelect = function(classId) {

        $class_s2.empty();
        $.ajax({
            url:  dashboard.siteurl + '/api/v1/s2/class.web/usage/events',
            cache: false
        })
            .done(function( data ) {
                $class_s2.select2({
                    placeholder: 'Επιλέξτε Χώρο',
                    width: 'style', // need to override the changed default
                    data : data.results,
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });

                $class_s2.val(classId).trigger("change");
                let message = {msg: "Classroom afterInit!"};
                dashboard.broker.trigger('afterInit.classroom', [message]);
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
            return "<span  style=\"font-weight: 500\">" + repo.text + "</span>";
        }
    }
})();
