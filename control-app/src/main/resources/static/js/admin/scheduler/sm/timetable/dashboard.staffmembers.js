(function () {
    'use strict';

    dashboard.staffmembers = dashboard.staffmembers || {};

    let StaffMembersDT;

    dashboard.staffmembers.init = function () {
        $("#staff_load").on('click',function(e){
            let department_filter = $("#department_filter").val();

            if ( ! $.fn.DataTable.isDataTable( '#StaffMembersSelectDataTable' ) ) {
                dashboard.staffmembers.InitAuthorizedInstitutionStaffMembers(department_filter);
            }
            else {
                dashboard.staffmembers.reloadInstitutionStaffMembers(department_filter);
            }

            $("#StaffMemberSelectModal").modal('show');
            e.preventDefault();
        });

        $("#applyStaffMemberFilter").on('click',function(){
            let StaffMembersDT = $("#StaffMembersSelectDataTable").DataTable();
            let nodes= StaffMembersDT.rows( { selected: true } ).data();
            let staffId, staffName;
            if (nodes.length>0) {
                for (let l = 0; l < nodes.length; l++) {
                    let _row = nodes[l];
                    staffId = _row.id;
                    staffName = _row.name;
                }
                $("#StaffMemberSelectModal").modal('hide');
                let message = {msg: "Filter:" + "staff" + " Selected!", filter: "staff", id: staffId, value: staffName};
                dashboard.broker.trigger('filter.select', [message]);
            }
        });
    }

    dashboard.staffmembers.InitAuthorizedInstitutionStaffMembers = function (department_filter) {

        let $institutionStaffMembersDtElem = $("#StaffMembersSelectDataTable");
        let url;
        if (department_filter === undefined || department_filter === "") {
            url = dashboard.siteurl + '/api/v2/dt/staff.web/authorized/scheduler';
        }
        else {
            url =  dashboard.siteurl + '/api/v2/dt/staff.web/authorized/scheduler/d/' + department_filter;
        }
        StaffMembersDT = $institutionStaffMembersDtElem.DataTable({
            "ajax": url,
            "sDom": 'Zfrtip',
            "oListNav" : {
                sLetterClass : "btn abcdaire",
            },
            "columns": [
                {"data": "id"},
                {"data": "name"},
                {"data": "affiliation"},
                {"data": "department.title"}
            ],
            select: {
                style: 'single'
            },
            "language": dtLanguageGr,
            order: [[1, 'asc']],
            "pageLength": 10,
            "pagingType" : "full_numbers",
            "aoColumnDefs": [
                {
                    "aTargets": [0,2],
                    "mData": "id",
                    "visible": false,
                }
            ]
        });
    }; // Staff DataTable Init

    dashboard.staffmembers.reloadInstitutionStaffMembers = function(department_filter) {
        let url;
        if (department_filter === undefined || department_filter === "") {
            url = dashboard.siteurl + '/api/v2/dt/staff.web/authorized/scheduler';
        }
        else {
            url =  dashboard.siteurl + '/api/v2/dt/staff.web/authorized/scheduler/d/' + department_filter;
        }
        StaffMembersDT.ajax.url(url);
        StaffMembersDT.ajax.reload();
    }
})();
