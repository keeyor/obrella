(function () {
    'use strict';

    dashboard.schools = dashboard.schools || {};

    let siteUrl;
    let $school_list;

    dashboard.schools.init = function (schoolId) {
        siteUrl = dashboard.siteUrl;
        $school_list = $('#school-list');
        dashboard.schools.loadschools(schoolId);

    };

    dashboard.schools.loadschools = function (schoolId) {
        let schoolTitle='';
        let url = siteUrl + '/api/v1/s2/schools.web';
        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                let queryParams = new URLSearchParams(window.location.search);
                queryParams.delete("sc");
                queryParams.delete("d");
                queryParams.delete("skip");
                $school_list.append('<li><a class="dropdown-item" href="live?' + queryParams + '">Όλες οι Σχολές</a></li>');
                $school_list.append('<li class="dropdown-divider"></li>');
                $.each(data.results, function (index, el) {
                     if (el.id === schoolId) {
                         schoolTitle = el.text;
                     }
                     let queryParams = new URLSearchParams(window.location.search);
                     queryParams.set("sc", el.id);
                     queryParams.delete("d");
                     queryParams.delete("skip");
                     $('#school-list').append('<li><a class="dropdown-item" href="live?' + queryParams + '">' + el.text +'</a></li>');
                });
                if (schoolId !== '') {
                    $('#school-dd-header').html("<b>" + schoolTitle + "</b>");
                }
                else {
                    $('#school-dd-header').html("Σχολή");
                }
                let message = {msg: "School Changed!", value: { id : schoolId, title: schoolTitle}};
                dashboard.broker.trigger('change.school', [message]);
            }
        });
    }


})();
