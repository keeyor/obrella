(function () {
    'use strict';

    dashboard.events = dashboard.events || {};
    let $events_s2;

    dashboard.events.init = function () {
        $events_s2 = $("#events_s2");
        let sel_event_id     = $("#_event").val();
        dashboard.events.fillEventSelect(sel_event_id);

        $events_s2.on('select2:select', function (e) {
            let data = e.params.data;
            let sel_event_Id = data.id;
            $("#_event").val(sel_event_Id);
            $("#_classroom").val("");

            //trigger change event
            let message = {msg: "Event selected!"};
            dashboard.broker.trigger('afterSelect.event', [message]);
        });

    };

    dashboard.events.select2event = function () {


        let sel_supervisor_id = $("#_supervisor").val();
        let sel_course_id     = $("#_event").val();
        let editor_id         = $("#editor_id").val();

        //if (sel_supervisor_id !== '') {
            dashboard.events.fillEventSelect();
       // }
    };

    dashboard.events.fillEventSelect = function(eventId) {
        $events_s2.empty();
        $.ajax({
            url:  dashboard.siteurl + '/api/v2/s2/scheduledEvents.web/authorized/scheduler/active',
            cache: false
        })
            .done(function( data ) {
                $events_s2.select2({
                    placeholder: 'Επιλέξτε Προγραμματισμένη Εκδήλωση',
                    width: 'style', // need to override the changed default
                    data : data.results,
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });
                $events_s2.val(eventId).trigger("change");
                let message = {msg: "Events afterInit!"};
                dashboard.broker.trigger('afterInit.event', [message]);
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
