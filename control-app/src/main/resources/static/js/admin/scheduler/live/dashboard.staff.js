(function () {
    'use strict';

    dashboard.staff = dashboard.staff || {};

    let siteUrl;
    let $staff_list;

    dashboard.staff.init = function () {
        siteUrl = dashboard.siteUrl;
        $staff_list = $('#staff-list');
        $("#staff-dd").attr("disabled", true);
        dashboard.staff.events();
    };

    dashboard.staff.loadStaffByDepartment = function (departmentId) {


        let url = siteUrl + '/api/v1/s2/staff.web/department/' + departmentId;
        $('#staff-list').html("");
        $('#staff-dd-header').html("Καθηγητής");
        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'json',
            success: function (data) {
                $.each(data.results, function (index, element) {
                        $staff_list.append('<li><a class="gray-accesskeys-link-wcag-color staff-item dropdown-item" href="#" data-title="<b>' + element.text + '</b>" data-id="' + element.id + '">' + element.text +'</a></li>');
                });
                $("#staff-dd").attr("disabled", false);
            }
        });

    }

    dashboard.staff.events = function() {

        $('body').on('click', '.staff-item', function() {
            let el = $(this);
            let staffId = el.data("id");
            let staffName = el.data("title");
            $('#staff-dd-header').html(staffName);

            let data = {
                id : staffId,
                title : staffName
            }
            let message = {msg: "Staff Changed!", value: data};
            dashboard.broker.trigger('change.staff', [message]);
        });
    }

})();
