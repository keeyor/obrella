/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.system = dashboard.system || {};

    let $year_list;
    dashboard.system.init = function () {
        $year_list = $("#year_select2");
        InitEvents();
    };

    function InitEvents() {
        $year_list.on('select2:select', function (e) {
            let data = e.params.data;
            let sel_AYear = data.id;
            let message = {msg: "Year selected!", year: sel_AYear};
            dashboard.broker.trigger('afterSelect.year', [message]);
        });
    }

    dashboard.system.getAvailableYearList = function (selectedAcademicYear) {

        $year_list.empty();

        $.ajax({
            url: dashboard.siteurl + '/api/v1/s2/institution/' + dashboard.institution_id  + '/calendars',
            cache: false,
            dataType: "json"
        })
            .done(function( data ) {

                $year_list.select2({
                    placeholder: 'Επιλέξτε Ακαδημαϊκό Έτος',
                    width: 'style',
                    data : data.results,
                    escapeMarkup: function (markup) { return markup; },
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                });
                $year_list.val(selectedAcademicYear).trigger("change");
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

})();
