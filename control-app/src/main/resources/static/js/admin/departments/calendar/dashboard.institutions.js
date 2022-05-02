/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.institutions = dashboard.institutions || {};

    let System_A_DT;
    let $table_system_a;

    dashboard.institutions.initInstitutionPausesDataTable = function (institutionId, year) {

        $table_system_a 	= $("#table_institution_a");
        
        System_A_DT = $table_system_a.DataTable({
            "bProcessing": false,
            "bDestroy": true,
            "bFilter": false,
            "bPaginate": false,
            "bInfo" : false,
            "oLanguage": {
                "sSearch": "<small>Αναζήτηση</small>"
            },
            "order": [[1, 'asc']],
            "ajax":  {
                "url": dashboard.siteurl + '/api/v1/dt/institution/' + institutionId + '/pause/' + year,
                "dataSrc":  "data.argies.argia"
            },

            "columns": [
                { "mData": null , "sWidth": "40px", "bSortable": false },
                { "mData": "startDate", "sWidth": "0px", "bSortable": true, "bVisible":false },
                { "mData": "name", "sWidth": "380px", "bSortable": false},
                { "mData": "startDate", "sWidth": "180px", "bSortable": false },
                { "mData": "endDate", "sWidth": "200px", "bSortable": false }
            ],
            "columnDefs": [
                {
                    "aTargets": [0],
                    "render": function (data,type,row) {
                        return row;
                    }
                },
                {
                    "aTargets": [1],
                    "render": function (data) {
                        return data;
                    }
                },
                {
                    "aTargets": [2],
                    "render": function (data) {
                        return '<span class="pb-0 mb-0" style="color: #003476;font-weight: 500">' + data + '</span>';
                    }
                },
                {
                    "aTargets": [3,4],
                    "render": function (data) {

                        if (data !== "") {
                            return moment.utc(data).format('D MMMM YYYY');
                        }
                        else {
                            return '<span class="text-muted">-- not set --</span>';
                        }
                    }
                }
            ],
            "initComplete": function( settings, json ) {
            }
        }); // DataTable init

        System_A_DT.on('order.dt search.dt', function () {
            System_A_DT.column(0, { search: 'applied', order: 'applied' }).nodes().each(function (cell, i) {
                cell.innerHTML = i + 1;
            });
        }).draw();
    };

    dashboard.institutions.reloadInstitutionPausesDataTable = function (institutionId, year) {
        System_A_DT.ajax.url(dashboard.siteurl + '/api/v1/dt/institution/' + institutionId + '/pause/' + year);
        System_A_DT.ajax.reload();
    }

})();
