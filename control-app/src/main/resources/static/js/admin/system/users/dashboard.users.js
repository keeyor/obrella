(function () {
    'use strict';

    dashboard.users = dashboard.users || {};

    let staffDT;
    let $staffDtElem;

    dashboard.users.init = function () {
        dashboard.users.initDT();
    };

    dashboard.users.initDT = function () {

        let userType = $("#userType").val();
        if (userType === null || userType === "" || userType === "all") {
            userType = "all";
        }
        else {
            $("#userType_select").val(userType).trigger("change");
        }
        $staffDtElem = $("#staffDataTable");
        staffDT = $staffDtElem.DataTable({
            "ajax": dashboard.siteurl + '/api/v1/dt/managers.web/type/' + userType,
            "columns": [
                {
                    "className":      'details-control',
                    "orderable":      false,
                    "data":           null,
                    "defaultContent": ''
                },
                {"data": null}, //1
                {"data": "id"}, //2
                {"data": "active"}, //3
                {"data": "name"}, //4
                {"data": "uid"}, //5
                {"data": "rights"}, // 6
                {"data": "email"}, // 7
                {"data": "department"}, // 8
                {"data": "eduPersonPrimaryAffiliation"}, // 9
                {"data": "affiliation"}, //10
                {"data": "lastLogin"}, //11
                {"data": "id"}, //12
                {"data": "id"}, //13
                {"data": "authorities"} //14
            ],
            "language": dtLanguageGr,
            order: [[3, 'asc']],
            "pageLength": 25,
            "dom": '<"top"fl><p>rt<"bottom">p<"clear">',
            "pagingType": "full_numbers",
            "aoColumnDefs": [
                {
                    "aTargets": [2,5,8,9,10,13,14],
                    "sortable": false,
                    "visible" :false
                },
                {
                    "aTargets": [3],
                    "mRender": function (data) {
                        if (data) {
                            return  '<i class="fas fa-user-check" title="Ενεργός Χρήστης"></i>';
                        }
                        else {
                            return '<i style="color:red" class="fas fa-user-lock" title="Ανενεργός Χρήστης"></i>';
                        }
                    }
                },
                {
                    "aTargets": [4],
                    "mRender": function (data,type, row) {
                        let ret = '<div class="pb-0 mb-0" style="color: #003476;font-weight: 500">' + data + '</div>';
                        ret += row["affiliation"] + ', Τμήμα ' + row["department"].title;
                        return ret;
                    }
                },
                {
                    "aTargets": [6],
                    "data": "rights",
                    "mRender": function (data) {
                        let role = getManagerType(data);
                        if (role === "SA")
                            return '<span style="display: none">SA</span><span style="font-weight: 600">Διαχειριστής Συστήματος</span>';
                        else if (role === "INSTITUTION_MANAGER")
                            return '<span style="display: none">ΙΜ</span></span><span style="font-weight: 500">Διαχειριστής Ιδρύματος</span>';
                        else if (role === "MANAGER")
                            return '<span style="display: none">DΜ</span></span><span style="font-weight: 400">Διαχειριστής Σχολών | Τμημάτων</span>';
                        else if (role === "SUPPORT")
                            return '<span style="display: none">SP</span><span style="font-weight: 300">Προσωπικό Υποστήριξης</span>';
                        else
                            return "-";
                    }
                },
                {
                    "aTargets": [7],
                    "mRender": function (data) {
                        let fields = data.split('@');
                        return '<span style="color: #003476;font-weight: bolder">' + fields[0] + '</span><span>@'+ fields[1] + '</span>';
                    }
                },
                {
                    "aTargets": [8],
                    "mRender": function (data) {
                        return  data.title;
                    }
                },
                {
                    "aTargets": [11],
                    "mRender": function (data) {
                        if (data === null) {return  '-';}
                        else {
                            let epochDate = data;
                            let formatted_date = moment.unix(epochDate).format('YYYY-MM-DD');
                            let display_date = moment.unix(epochDate).format('D MMM YYYY');
                            return  '<span style="display:none;">' + formatted_date + '</span>' + display_date  + '<input type="hidden" value="' + epochDate + '">';
                        }
                    }
                },
                {
                    "aTargets": [12],
                    "mData": "id",
                    "sortable": false,
                    "className": "text-left",
                    "mRender": function (data,type,row) {
                        let ret = '';
                        let user_authorities;
                        user_authorities = row.authorities;
                        ret += '<a role="button" class="btn btn-sm btn-pill blue-btn-wcag-bgnd-color me-1 text-white" title="επεξεργασία στοιχείων"' +
                                ' href="user-editor?id=' + data + '"><i class="fas fa-edit"></i></a>';
                        if (user_authorities.includes("STAFFMEMBER")) {
                            ret += '<i class="fas fa-user-tag ms-2" title="Μέλος ΔΕΠ"></i>';
                        }
                        return  ret;
                    }
                },
                {
                    "aTargets": [13],
                    "mData": "id",
                    "sortable": false,
                    "className": "text-left",
                    "mRender": function () {
                        let ret = '';
                        ret += '<div><button type="button" class="btn btn-sm btn-pill btn-warning user_rights mt-1" title="επεξεργασία δικαιωμάτων"><i class="fas fa-user-shield"></i> </button></div>'
                        return  ret;
                    }
                }
            ],
            "initComplete": set_display_results_i,
            "rowCallback": function( row, data ) {
                if (data.rights.isSa) {
                    $('td:eq(0)', row).removeClass("details-control");
                }
            },
        });

        staffDT.on( 'order.dt search.dt', function () {
            staffDT.column(1, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
                cell.innerHTML = i+1;
            } );
        } ).draw();

        InitControls();
        RegisterEvents();

    }; // Staff DataTable Init


    function InitControls() {

        $("#userType_select").select2({
            minimumResultsForSearch: -1 //hides the searchbox
        });

        $("#staff_enabled").bootstrapToggle({
            on: '<i class="fas fa-power-off"></i>',
            off: '<i class="fas fa-ban"></i>',
            onstyle: "success",
            offstyle: "danger",
            size: "small"
        });
    }
    function RegisterEvents() {

        $("#userType_select").on('select2:select', function (e) {
            let data = e.params.data;
            let sel_userType = data.id;
            if (sel_userType !== "all") {
                window.location = "users?t=" + sel_userType;
            }
            else {
                window.location = "users";
            }
        });

        $staffDtElem.on("click", "td.details-control", function () {

            var tr = $(this).closest('tr'),
                row = staffDT.row(tr);
            if (tr.hasClass('shown')) {
                $('div.childWrap', row.child()).slideUp( function () {
                    tr.removeClass('shown');
                    row.child().remove();
                } );
            } else {
                let manager_type = getManagerType(row.data().rights);
                if (manager_type === "INSTITUTION_MANAGER" || manager_type === "MANAGER") {
                    $.when(getUserManagerDetails(row.data().id)).then(function (response) {
                        row.child(renderManagerChild(response), 'no-padding').show();
                        tr.addClass('shown');
                        $('div.childWrap', row.child()).slideDown();
                    });
                }
                else if ( manager_type === "SUPPORT") {
                    $.when(getUserSupportDetails(row.data().id)).then(function (response) {
                        row.child(renderSupportChild(response), 'no-padding').show();
                        tr.addClass('shown');
                        $('div.childWrap', row.child()).slideDown();
                    });
                }
            }
        });

        function getUserManagerDetails(id) {
            return $.ajax({
                url: dashboard.siteurl + '/api/v1/dt/managers/assigned_units/' + id,
                type: "GET"
            });
        }

        function renderManagerChild(data) {
            var wrapper = $('<div style="padding:5px 0" class="childWrap"></div>'),
                result = [];

            $.each(data.data, function (i, v) {
                let table_row_html = "<tr><td></td><td></td>";
                if (v.unitType === "DEPARTMENT") { table_row_html += "<td>ΤΜΗΜΑ</td>";}
                else if (v.unitType === "SCHOOL") { table_row_html += "<td>ΣΧΟΛΗ</td>";}
                else if (v.unitType === "INSTITUTION") { table_row_html += "<td>ΙΔΡΥΜΑ</td>";}
                table_row_html += "<td>" + v.unitTitle + "</td>";
                if (v.contentManager) { table_row_html += "<td><i class=\"far fa-check-square\" style=\"color: green\"></i></td>"; }
                else { table_row_html += "<td><i class=\"far fa-times-circle\" style=\"color: red\"></i></td>"; }
                if (v.dataManager) { table_row_html += "<td><i class=\"far fa-check-square\" style=\"color: green\"></i></td>"; }
                else { table_row_html += "<td><i class=\"far fa-times-circle\" style=\"color: red\"></i></td>"; }
                if (v.scheduleManager) { table_row_html += "<td><i class=\"far fa-check-square\" style=\"color: green\"></i></td>"; }
                else { table_row_html += "<td><i class=\"far fa-times-circle\" style=\"color: red\"></i></td>"; }
                table_row_html += "</tr>";
                result.push(table_row_html);
            });

            let cTable = '<table class="child-table" style="width: 100%;">' +
                '<thead><tr><th></th><th>Παραχωρημένα Δικαιώματα</th><th>Τύπος</th><th>Τίτλος</th><th>Περιεχόμενο</th><th>Στοιχεία Μονάδας</th><th>Προγραμματισμός</th></tr></thead>' +
                '<tbody>' + result.join('') + '</tbody></table>';
            wrapper.append(cTable);

            return wrapper;
        }
        function getUserSupportDetails(id) {
            return $.ajax({
                url: dashboard.siteurl + '/api/v1/dt/managers/assigned_courses/' + id,
                type: "GET"
            });
        }
        function renderSupportChild(data) {
            var wrapper = $('<div style="padding:0 0" class="childWrap"></div>'),
                result = [];

            $.each(data.data, function (i, v) {
                let table_row_html = "<tr><td></td><td></td>";
                table_row_html += "<td>" + v.staffMemberName + "</td>";
                table_row_html += "<td>" + v.courseTitle + "</td>";
                if (v.contentManager) { table_row_html += "<td><i class=\"far fa-check-square\" style=\"color: green\"></i></td>"; }
                else { table_row_html += "<td><i class=\"far fa-times-circle\" style=\"color: red\"></i></td>"; }
                if (v.scheduleManager) { table_row_html += "<td><i class=\"far fa-check-square\" style=\"color: green\"></i></td>"; }
                else { table_row_html += "<td><i class=\"far fa-times-circle\" style=\"color: red\"></i></td>"; }
                table_row_html += "</tr>";
                result.push(table_row_html);
            });

            let cTable = '<table class="child-table" style="width: 100%;">' +
                '<thead><tr><th></th><th>Παραχωρημένα Δικαιώματα</th><th>Καθηγητής</th><th>Μάθημα</th><th>Περιεχόμενο</th><th>Προγραμματισμός</th></tr></thead>' +
                '<tbody>' + result.join('') + '</tbody></table>';
            wrapper.append(cTable);

            return wrapper;
        }
    }

    function getManagerType(rights) {

        let role= "NOT_SET";
        if (rights.isSa) {
            role = "SA";
        }
        if (rights.unitPermissions != null && rights.unitPermissions.length > 0 && rights.unitPermissions[0].unitType === 'INSTITUTION') {
            role = 'INSTITUTION_MANAGER';
        }
        else if (rights.unitPermissions != null && rights.unitPermissions.length > 0) {
            role = 'MANAGER';
        }
        else if (rights.coursePermissions != null && rights.coursePermissions.length > 0) {
            role = 'SUPPORT';
        }
        return role;
    }
    function  set_display_results_i() {
        $("#count_staff_results").html("" + staffDT.rows().count() + "");
    }


    dashboard.users.reloadStaffTable = function(userType) {
            staffDT.ajax.url(dashboard.siteurl + '/api/v1/dt/managers.web/type/' + userType);
            staffDT.ajax.reload( function ( json ) {
                set_display_results_i(json);
        });
    }

})();
