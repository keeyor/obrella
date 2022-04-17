(function () {
    'use strict';

    dashboard.units = dashboard.units || {};

    let  InstitutionUnitsDT = null;

    dashboard.units.InitInstitutionUnits = function () {

        let $institutionUnitsDtElem = $("#runitSelectDataTable");
        InstitutionUnitsDT = $institutionUnitsDtElem.DataTable({
            "ajax": dashboard.siteurl + '/api/v1/dt/units.web',
            "columns": [
                {"data": "id"},
                {"data": "structureType"},
                {"data": "title"},
            ],
            "language":  dtLanguageGr,
            select: {
                style: 'multi'
            },
            order: [[1, 'asc']],
            "pageLength": 10,
            "aoColumnDefs": [
                {
                    "aTargets": [0],
                    "mData": "id",
                    "visible": false
                },
                {
                    "aTargets": [1],
                    "name": "structureType",
                    "render": function (data) {
                        if (data != null) {
                            if (data === 'INSTITUTION') {
                                return 'Ίδρυμα';
                            }
                            else if (data === 'SCHOOL') {
                                return 'Σχολή';
                            }
                            else if (data === 'DEPARTMENT') {
                                return 'Τμήμα';
                            }
                        }
                        else {
                            return "-";
                        }
                    }
                },
            ]
        });
    };
})();