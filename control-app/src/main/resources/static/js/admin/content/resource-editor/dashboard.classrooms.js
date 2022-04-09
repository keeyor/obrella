(function () {
    'use strict';

    dashboard.classrooms = dashboard.classrooms || {};
    let $class_el;

    dashboard.classrooms.init = function () {
        $class_el = $("#classrooms_s2");

        InitControls();
        InitEvents();
    };

    function InitControls() {
        $class_el.select2({
            placeholder: 'Επιλέξτε μάθημα και υπ. καθηγητή για να εμφανιστούν οι επιλογές χώρων'
        })
    }

    function InitEvents() {
        $class_el.on('select2:select', function (e) {
            let data = e.params.data;
            let sel_class_Id = data.id;
            $("#classroom_id").val(sel_class_Id);
            //trigger change event
            let message = {msg: "Classroom selected!"};
            dashboard.broker.trigger('afterSelect.classroom', [message]);
        });
    }

    dashboard.classrooms.selectaAll2class = function () {

        $class_el.empty();
        let sel_class_id      = $("#classroom_id").val();
        dashboard.classrooms.fillAllClassSelect(sel_class_id);
    };

    dashboard.classrooms.select2class = function () {

        $class_el.empty();
        let sel_supervisor_id = $("#supervisor_id").val();
        let sel_course_id     = $("#course_id").val();
        let sel_class_id      = $("#classroom_id").val();

        if (sel_supervisor_id !== '' && sel_course_id !== '') {
            dashboard.classrooms.fillClassSelect(sel_class_id,sel_supervisor_id,sel_course_id);
        }
    };

    dashboard.classrooms.fillClassSelect = function(classId, staffId, courseId) {

        $.ajax({
            url:  dashboard.siteUrl + '/api/v1/s2/class.web/staff/' + staffId + '/course/' + courseId + '/usage/both/',
            cache: false
        })
            .done(function( data ) {
                $class_el.select2({
                    placeholder: 'Επιλέξτε Χώρο',
                    width: 'style', // need to override the changed default
                    data : data.results,
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });

                $class_el.val(classId).trigger("change");

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
                return repo.text;
        }
    }

    dashboard.classrooms.fillAllClassSelect = function(classId) {

        $.ajax({
            url:  dashboard.siteUrl + '/api/v1/s2/class.web/usage/events',
            cache: false
        })
            .done(function( data ) {
                $class_el.select2({
                    placeholder: 'Επιλέξτε Χώρο',
                    allowClear : true,
                    width: 'style', // need to override the changed default
                    data : data.results,
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });

                $class_el.val(classId).trigger("change");
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
            return repo.text;
        }
    }
})();
