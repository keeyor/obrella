(function () {
    'use strict';

    dashboard.events = dashboard.events || {};
    let $event_el;

    dashboard.events.init = function () {
        $event_el = $("#events_s2");

        $event_el.on('select2:select', function (e) {
            let data = e.params.data;
            let sel_event_Id = data.id;
            $("#event_id").val(sel_event_Id);
            //trigger change event
            let message = {msg: "Event selected!"};
            dashboard.broker.trigger('afterSelect.event', [message]);
        });

        $('#ScheduledEventInfoModal').on('show.coreui.modal', function (e) {
            let event_id = $("#event_id").val();
            if (event_id != null && event_id !== "") {
                dashboard.events.getScheduledEventInfo(event_id);
            }
            else {
                $('#ScheduledEventInfoModal').modal("dispose");
            }
        })
    };

    dashboard.events.select2event = function () {
        $event_el.empty();
        let sel_event_id     = $("#event_id").val();

        dashboard.events.fillEventSelect(sel_event_id);
    };

    dashboard.events.fillEventSelect = function(eventId) {
        $.ajax({
            url:  dashboard.siteUrl + '/api/v2/s2/scheduledEvents.web/authorized/content/all',
            cache: false
        })
        .done(function( data ) {
                $event_el.select2({
                    placeholder: 'Επιλέξτε Προγραμματισμένη Εκδήλωση',
                    width: 'style', // need to override the changed default
                    data : data.results,
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });
                $event_el.val(eventId).trigger("change");
                let message = {msg: "Events afterInit!"};
                dashboard.broker.trigger('afterInit.event', [message]);
        });

        function formatRepo (repo) {
            if (repo.loading) {
                return repo.text;
            }

            let markup = "<div class='select2-result-repository clearfix'>" +
                "<div class='select2-result-repository__meta'>" +
                "<div class='select2-result-repository__title'><i class=\"icon-event\"></i> " + repo.text + "</div>";

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
                return "<b>" + repo.text + "</b>";
            else
                return repo.text;
        }
    }

    dashboard.events.getScheduledEventInfo = function(eventId) {

        $.ajax({
            url:  dashboard.siteUrl + '/api/v1/sevent/' + eventId,
            cache: false
        })
            .done(function( data ) {
                $("#event_info_title").html(data.title);
                let event_info_html="";
                event_info_html += "<table style='border: none;width: 100%'>";
                event_info_html += "<tr><th style='width: 40%;font-weight: 550;margin-bottom: 10px;vertical-align: top'>Τίτλος</th><td>" + data.title + "</td></tr>";
                event_info_html += "<tr><th style='width: 40%;font-weight: 550;margin-bottom: 10px;vertical-align: top'>Ημερομηνία</th><td>" + moment(data.startDate).format("LL");
                if (data.endDate !== null && data.endDate !== "" && data.endDate !== data.startDate) {
                    event_info_html += " - " + moment(data.endDate).format("LL");
                }
                event_info_html += "</td></tr>";
                event_info_html += "<tr><th style='width: 40%;font-weight: 550;margin-bottom: 5px;vertical-align: top'>Διοργανωτές</th><td>";
                if (data.responsibleUnit != null) {
                    $.each(data.responsibleUnit, function (index, ru) {
                        event_info_html += (index>0 ? ', ' : '');
                        if (ru.structureType === 'DEPARTMENT') { event_info_html += "Τμήμα ";}
                        if (ru.structureType === 'SCHOOL') { event_info_html += "Σχολή ";}
                        event_info_html += ru.title;
                    });
                }
                event_info_html += "</td></tr>";
                event_info_html += "<tr><th style='width: 40%;font-weight: 550;margin-bottom: 5px;vertical-align: top'>Επ. Υπεύθυνος</th><td>";
                if (data.responsiblePerson != null) {
                    event_info_html += data.responsiblePerson.name + ', '  + data.responsiblePerson.affiliation;
                }
                event_info_html += "</td></tr>";
                event_info_html += "</table>";
                $("#event_info_panel").html(event_info_html);
            });
    }
})();
