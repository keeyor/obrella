(function () {
    'use strict';

    dashboard.events = dashboard.events || {};
    let $event_el;

    dashboard.events.init = function () {
        $event_el = $("#events_s2");
        $event_el.select2({
            placeholder: "Επιλέξτε Τμήμα -> Επ. Υπεύθυνο -> Εκδήλωση"
        });
    };

    dashboard.events.getEventsByResponsiblePerson = function(staffId) {

        $.ajax({
            url:  dashboard.siteurl + '/api/v1/s2/scheduledEvents.web/staffmember/' + staffId,
            cache: false
        })
        .done(function( data ) {
                $event_el.select2({
                    placeholder: '-- Επιλέξτε Εκδήλωση ή αφήστε κενό για όλες τις Εκδηλώσεις--',
                    allowClear: true,
                    width: 'style', // need to override the changed default
                    data : data.results,
                    dropdownParent: $("#staffEventSelectModal"),
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });
                $event_el.val("").trigger("change");
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
