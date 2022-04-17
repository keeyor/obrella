(function () {
    'use strict';

    dashboard.streamers = dashboard.streamers || {};

    let  streamersDT = null;
    let serialize_form;

    dashboard.streamers.selectedId = null;
    dashboard.streamers.selectedRowIndex = null;
    dashboard.streamers.selectedName = null;

    dashboard.streamers.init = function () {

        InitControls();
        dashboard.streamers.selectedProgramId = -1;
        dashboard.streamers.selectedRowIndex = -1;
        dashboard.streamers.selectedProgramName = "all";

        let $streamersDtElem = $("#streamersDataTable");

        streamersDT = $streamersDtElem.DataTable({
            "ajax":  dashboard.siteurl + '/api/v1/dt/streamers.web',
            "columns": [
                {"data": null},
                {"data": "id"},
                {"data": "code"}, //2
                {"data": "type"},
                {"data": "description"}, //4
                {"data": "server"}, //5
                {"data": "application"},
                {"data": "enabled"}, //7
                {"data": "port"},
                {"data": "adminPort"},
                {"data": "restPort"}, //10
                {"data": "adminUser"},
                {"data": "adminPassword"},
                {"data": "id"},
                {"data": "protocol"},//14
            ],
            "language": dtLanguageGr,
            order: [[2, 'asc']],
            "dom": '<"top"fl><p>rt<"bottom">p<"clear">',
            "pageLength": 25,
            "pagingType": "full_numbers",
            "aoColumnDefs": [
                {
                    "aTargets": [1,4,8,9,10, 11,12,14],
                    "visible": false
                },
                {
                    "aTargets": [2],
                    "mRender": function (data,type, row) {
                        let ret = '<div class="pb-0 mb-0" style="color: #003476;font-weight: 500">' + data + '</div>';
                        ret += row["description"];
                        return ret;
                    }
                },
                {
                    "aTargets": [5],
                    "mRender": function (data) {
                        return '<b>' + data + '</b>';
                    }
                },
                {
                    "aTargets": [7],
                    "mRender": function (data) {
                        if (data === "true") {
                            return '<i class="fas fa-circle" style="color:green" title="ενεργός server"></i>';
                        }
                        else if (data === "false") {
                            return '<i class="fas fa-circle" style="color:red" title="ανενεργός server"></i>';
                        }
                    }
                },
                {
                    "aTargets": [13],
                    "mData": "id",
                    "sortable": false,
                    "className": "text-end",
                    "mRender": function () {
                            return '<button type="button" class="btn blue-btn-wcag-bgnd-color text-white btn-sm btn-pill"><i class="fas fa-edit"></i> </button>';
                    }
                }
            ],
            "initComplete": set_display_results,
        });
        function  set_display_results() {
            $("#count_streamers_results").html("" + streamersDT.rows().count() + "");
        }
        streamersDT.on( 'order.dt search.dt', function () {
            streamersDT.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
                cell.innerHTML = i+1;
            } );
        } ).draw();

        RegisterEvents();



        function RegisterEvents() {
            $streamersDtElem.on("dblclick", "tbody td", function (e) {
                // get selected row index
                let table_cell = $(this).closest('td');
                let rowIdx = streamersDT.cell(table_cell).index().row;

                setupStreamerEdit ("edit", "Επεξεργασία Εξυπηρετητή",rowIdx);
                e.stopPropagation();
            });
            $streamersDtElem.on("click", "tbody button", function () {

                // get selected row index
                let table_cell = $(this).closest('td');
                let rowIdx = streamersDT.cell(table_cell).index().row;
                setupStreamerEdit ("edit", "Επεξεργασία Εξυπηρετητή",rowIdx);
            });

            $("#addOrUpdateStreamer").on("click", function() {

                loader.showLoader();
                let streamerId      = $("#streamer_id").val();
                let streamer_code   = $("#streamer_code").val();
                let streamer_type   = $("#streamer_type").val();
                let streamer_descr  = $("#streamer_descr").val();
                let streamer_url    = $("#streamer_url").val();
                let streamer_app    = $("#streamer_app").val();
                let streamer_proto  = $("#streamer_protocol").val();
                let streamer_port   = $("#streamer_port").val();
                let streamer_aport  = $("#streamer_aport").val();
                let streamer_rport  = $("#streamer_rport").val();
                let streamer_user   = $("#streamer_user").val();
                let streamer_pass   = $("#streamer_pass").val();
                let streamer_enabled= $("#streamer_toggle").prop('checked');

                if (streamer_code == null || streamer_code === "" || streamer_type == null || streamer_type === "" || streamer_descr == null || streamer_descr === "" ||
                    streamer_url == null || streamer_url === "" || streamer_app == null || streamer_app === "" || streamer_port == null || streamer_port === "" ||
                    streamer_aport == null || streamer_aport === "" || streamer_rport == null || streamer_rport === "" || streamer_user == null || streamer_user === "" ||
                    streamer_pass == null || streamer_pass === "") {
                    alertify.alert("<i style='color:red' class='fas fa-exclamation-triangle'></i> Πρόβλημα","Υπάρχουν παραλείψεις στη φόρμα. Διορθώστε τα κενά και προσπαθήστε πάλι")
                }
                else {
                    let streamerData = {
                        "id": streamerId,
                        "code" : streamer_code,
                        "type": streamer_type,
                        "description": streamer_descr,
                        "server": streamer_url,
                        "application": streamer_app,
                        "protocol" : streamer_proto,
                        "port" : streamer_port,
                        "adminPort" : streamer_aport,
                        "restPort" : streamer_rport,
                        "adminUser":streamer_user,
                        "adminPassword": streamer_pass,
                        "enabled" : streamer_enabled
                    };
                    postUpdate(streamerData);
                }
            });
            $("#deleteStreamer").on("click", function (e) {

                let streamer_id = $("#streamer_id").val();
                let streamer_code = $("#streamer_code").val();

                let msg = '<div class="font-weight-bold">Ο Εξυπηρετητής "' + streamer_code + '" Θα διαγραφεί! Είστε σίγουρος;</div>';
                msg += '<div>'
                alertify.confirm('<i style="color: red" class="fas fa-trash-alt"></i> Διαγραφή Εξυπηρετητή', msg,
                    function () {
                        postDelete(streamer_id);
                    },
                    function () {
                    }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});

                e.preventDefault();
            });
            $("#closeUpdateStreamer").on('click',function(){
                let end_serialize = $("#streamer_form").serialize();
                if (serialize_form !== end_serialize) {
                    closeEditDialogWarning();
                }
                else {
                    unloadEditForm();
                }
            });

            $("#newStreamerBt").on("click", function() {
                setupStreamerEdit ("add", "Νέος Εξυπηρετητής",-1);
            });

        }

        function setupStreamerEdit (action, title, rowIdx) {
            $("#streamer_edit_mode").val(action);
            let $streamer_toggle = $('#streamer_toggle');
            $streamer_toggle.bootstrapToggle('off');

            if (rowIdx !== -1) {
                let row_data = streamersDT.row( rowIdx ).data();
                $("#streamerModalLabel").html('<div style="font-size: 1.4em">' + row_data.description + '</div><small>' + title + '</small>');
                $("#streamer_id").val(row_data.id);
                $("#streamer_code").val(row_data.code);
                $("#streamer_type").val(row_data.type).trigger("change");
                $("#streamer_descr").val(row_data.description);
                $("#streamer_url").val(row_data.server);
                $("#streamer_app").val(row_data.application);
                $("#streamer_port").val(row_data.port);
                $("#streamer_aport").val(row_data.adminPort);
                $("#streamer_rport").val(row_data.restPort);
                $("#streamer_user").val(row_data.adminUser);
                $("#streamer_pass").val(row_data.adminPassword);
                if (row_data.enabled === "true") {
                    $streamer_toggle.bootstrapToggle('on');
                }
                $("deleteStreamer").show();
                $("#st_rowIdx_edited").val(rowIdx);
              }
            else {
                    $("#streamerModalLabel").html(title);
                    $("#streamer_id").val("");
                    $("#streamer_code").val("");
                    $("#streamer_type").val("ipcamera");
                    $("#streamer_descr").val("");
                    $("#streamer_url").val("");
                    $("#streamer_app").val("");
                    $("#streamer_port").val("1935");
                    $("#streamer_aport").val("8086");
                    $("#streamer_rport").val("8087");
                    $("#streamer_user").val("admin");
                    $("#streamer_pass").val("");
                    $("#deleteStreamer").hide();
                    $("#st_rowIdx_edited").val("");
            }
            $("#streamers_card").hide();
            $("#streamers_edit_card").show();
            serialize_form = $("#streamer_form").serialize();
        }

        function postUpdate(streamerData) {
            $.ajax({
                type:        "POST",
                url: 		  dashboard.siteurl + '/api/v1/streamer/save',
                contentType: "application/json; charset=utf-8",
                data: 		  JSON.stringify(streamerData),
                async:		  true,
                success: function(data){
                    //Highlight edited/added row
                    streamersDT.ajax.reload();
                    $("#streamer_id").val(data);
                    alertify.notify("Ο Εξυπηρετητής αποθηκεύτηκε με επιτυχία", "success");
                    serialize_form = $("#streamer_form").serialize();
                    loader.hideLoader();
                },
                error: function ()  {
                    loader.hideLoader();
                    alertify.alert('Error-Update-Streamer');
                }
            });
        } //postUpdate

        function postDelete(streamerId) {

            $.ajax({
                type:        "DELETE",
                url: 		  dashboard.siteurl + '/api/v1/streamer/delete/' + streamerId,
                contentType: "application/json; charset=utf-8",
                async:		  true,
                success: function(){
                    unloadEditForm();
                    streamersDT.ajax.reload();
                    alertify.notify("Ο Εξυπηρετητής διαγράφηκε με επιτυχία", "success");
                },
                error: function (data)  {
                    let info = "Άγνωστο Σφάλμα";
                    let msg = data.responseText;
                    $("#programModal").modal('hide');
                    alertify.alert('Σφάλμα', '<i style="color: red" class="fas fa-exclamation-circle"></i> ' + msg + info);
                }
            });
        }

        function unloadEditForm() {
            $("#streamers_edit_card").hide();
            $("#streamers_card").show();
        }
        function closeEditDialogWarning() {
            let msg = '<div class="font-weight-bold">Οι αλλαγές θα χαθούν! Είστε σίγουρος?</div>';
            alertify.confirm('Προειδοποίηση', msg,
                function () {
                    unloadEditForm();
                },
                function () {
                }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});
        }
    };//

    function InitControls() {
        $('#streamer_toggle').bootstrapToggle({
            on: '<i class="fas fa-power-off"></i>',
            off: '<i class="fas fa-ban"></i>',
            onstyle: "success",
            offstyle: "danger",
            size: "small"
        });
        $('#streamer_protocol').select2({});
        $('#streamer_type').select2({});
    }

    dashboard.streamers.clearStreamerDtSelection = function () {

        //un-highlight selected rows
        $(streamersDT.rows().nodes()).removeClass('row_selected');

        //disble first column icon
        streamersDT.column(0, {order: 'applied'}).nodes().each(function (cell, i) {
            cell.innerHTML = '<i style="color:silver" class="fas fa-check-circle"></i>';
        });

        //disable edit buttons
        let lastColumnIndex = streamersDT.columns().count();
        streamersDT.column(lastColumnIndex - 1, {
            order: 'applied'
        }).nodes().each(function (cell, i) {
            cell.innerHTML = '<button type="button" class="btn btn-outline-secondary btn-sm" disabled><i class="fas fa-pencil-alt"></i> </button>';
        });

        dashboard.streamers.selectedId = -1;
        dashboard.streamers.selectedRowIndex = -1;
        dashboard.streamers.selectedName = "all";

    };

    dashboard.streamers.enableStreamerDtByRowIndex = function (rowIdx) {

            //clear current selections
            dashboard.streamers.clearStreamerDtSelection();

            //highlight selected row
            $(streamersDT.row(rowIdx).nodes()).addClass('row_selected');

            let node;
            //enable first column icon
            node = streamersDT.cell(rowIdx, 0).node();
            node.innerHTML = '<i style="color:green" class="fas fa-check-circle"></i>';

            dashboard.streamers.selectedRowIndex = rowIdx;
            dashboard.streamers.selectedId = streamersDT.cell(rowIdx, 0).data();
            dashboard.streamers.selectedName = streamersDT.cell(rowIdx, 2).data();

            //enable edit button of selected row

             let lastColumnIndex = streamersDT.columns().count();
             node = streamersDT.cell(rowIdx, lastColumnIndex - 1).node();
              node.innerHTML = '<button type="button" class="btn btn-primary btn-sm">' +
              '<i class="fas fa-pencil-alt"></i> ' +
              '</button>';

            // Display the rowIdx row in the table
            streamersDT.row(rowIdx).show().draw(false);

    };
    dashboard.streamers.enableStreamerDtByProgramId = function (streamerId) {


        if (streamerId !== undefined && streamerId.toString() !== "-1") {
            streamersDT.column(0, {order: 'applied'}).nodes().each(function (cell, i) {
                var indexProgramId = streamersDT.cell(i, 0).data();
                if (indexProgramId === streamerId) {
                    dashboard.streamers.enableStreamerDtByRowIndex(i);
                    console.log("enable streamer in row:" + i);
                }
            });
        }
        else {
            dashboard.streamers.clearStreamerDtSelection();
        }
    };

})();