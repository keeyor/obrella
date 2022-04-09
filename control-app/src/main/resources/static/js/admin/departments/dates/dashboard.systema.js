/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.systema = dashboard.systema || {};

    let System_A_DT;
    let $table_system_a;

    dashboard.systema.init = function () {

    	$table_system_a 	= $("#table_system_a");
    	RegisterListeners();
    };

    function RegisterListeners() {

        $("#system-argies-edit").on('click',function(e){
            dashboard.modala.initDataTable( dashboard.selected_year, dashboard.institution,"");
            $("#modal_p_year").html(" " + dashboard.selected_year + "-" + (parseInt(dashboard.selected_year)+1));

            $("#modal_header_p_img_institution").show();
            $("#modal_header_p_img_department").hide();

            $("#modal_p_institution").css("color", "red");
            $("#modal_p_department_title").hide();

            $("#PPMessages").html(" ");
            $("#PPeriodErrorMessages").attr('class','alert alert-danger invisible');
            $("#modal_p_scope").val("system");
            $("#editPauseModal").modal('show');
        });
        $("#department-argies-edit").on('click',function(e){

            let $department_s2 = $("#department_select2");
            dashboard.modala.initDataTable( dashboard.selected_year, dashboard.institution,dashboard.department.selectedDepartmentId);
            $("#modal_p_year").html(" " + dashboard.selected_year + "-" + (parseInt(dashboard.selected_year)+1));

            $("#modal_header_p_img_institution").hide();
            $("#modal_header_p_img_department").show();

            $("#modal_p_institution").css("color", "black");
            $("#modal_p_department_title").show();
            $("#modal_p_department_title").html("<b>" + dashboard.department.selectedDepartmentName +"</b>");
            $("#modal_p_department_title").css("color", "red");

            $("#PPMessages").html(" ");
            $("#PPeriodErrorMessages").attr('class','alert alert-danger invisible');
            $("#modal_p_scope").val("department");
            $("#editPauseModal").modal('show');
        });
    }

    dashboard.systema.initDataTable = function (institutionId, year) {

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
                        return '<h6 class="pb-0 mb-0" style="color: #003476">' + data + '</h6>';
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

    dashboard.systema.reloadDataTable = function (institutionId, year) {
        System_A_DT.ajax.url(dashboard.siteurl + '/api/v1/dt/institution/' + institutionId + '/pause/' + year);
        System_A_DT.ajax.reload();
    }

})();
