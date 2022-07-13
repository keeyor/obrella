(function () {
    'use strict';

    dashboard.messages = dashboard.messages || {};

    let  messagesDT = null;
    let serialize_form;

    dashboard.messages.init = function () {

        InitControls();

        let $messagesDtElem = $("#messagesDataTable");

        let check_hidden = '<span style="display:none">checked</span>';
        let uncheck_hidden = '<span style="display:none">unchecked</span>';

        messagesDT = $messagesDtElem.DataTable({
            "ajax":  dashboard.siteurl + '/api/v1/dt/messages.web',
            "columns": [
                {"data": null},
                {"data": "id"},
                {"data": "startDate"},
                {"data": "status"}, //3
                {"data": "text"},   //4
                {"data": "target"}, //5
                {"data": "target"}, //6
                {"data": "target"}, //7
                {"data": "site"},   //8
                {"data": "site"},   //9
                {"data": "site"},   //10
                {"data": "site"},   //11
                {"data": "visible"}, //12
                {"data": "id"}      //13
            ],
            "language": dtLanguageGr,
            "dom": '<"top"fl><p>rt<"bottom">p<"clear">',
            stateSave: true,
            order: [[2, 'desc']],
            "pagingType": "full_numbers",
            "pageLength": 25,
            "aoColumnDefs": [
                {
                    "aTargets": [1],
                    "visible": false
                },
                {
                    "aTargets": [2],
                    "className" : "dt-center",
                    "mRender": function (data) {
                        if (data != null)
                            return '<span style="color:#003476;font-weight: 500">' + moment.unix(data).format("DD/MM/YY HH:mm") + '</span>';
                        else
                            return "";
                    }
                },
                {
                    "aTargets": [3],
                    "className" : "left_border",
                    "mRender": function (data) {
                        if (data === "info") {
                            return "<h6><span class=\"badge bg-info\">Πληροφορία</span></h6>";
                        }
                        else if (data === "warning") {
                            return "<h6><span class=\"badge bg-warning\">Προειδοποίηση</span></h6>";
                        }
                        else if (data === "success") {
                            return "<h6><span class=\"badge bg-success\">Επιτυχία</span></h6>";
                        }
                        else if (data === "danger") {
                            return "<h6><span class=\"badge bg-danger\">Κίνδυνος</span></h6>";
                        }
                    }
                },
                {
                    "aTargets": [4],
                    "mRender": function (data) {
                        if (data.length < 100) {
                            return '<span>' + data + '</span>';
                        }
                        else {
                            return '<span title="' + data + '">' + data.substring(0,100) + "..." + '</span>';
                        }
                    }
                },
                {
                    "aTargets": [5],
                    "className" : "left_border",
                    "mRender": function (data) {
                        if (data != null && data.includes("visitors")) {
                            return check_hidden + "<i class='fas fa-check' style='color: green'></i>";
                        }
                        else {
                            return  uncheck_hidden + "<i class=\"fas fa-minus\"></i>";
                        }
                    }
                },
                {
                    "aTargets": [6],
                    "mRender": function (data) {
                        if (data != null && data.includes("admins")) {
                            return check_hidden + "<i class='fas fa-check' style='color: green'></i>";
                        }
                        else {
                            return  uncheck_hidden + "<i class=\"fas fa-minus\"></i>";
                        }
                    }
                },
                {
                    "aTargets": [7],
                    "mRender": function (data) {
                        if (data != null && data.includes("users")) {
                            return check_hidden + "<i class='fas fa-check' style='color: green'></i>";
                        }
                        else {
                            return  uncheck_hidden + "<i class=\"fas fa-minus\"></i>";
                        }
                    }
                },
                {
                    "aTargets": [8],
                    "className" : "left_border",
                    "mRender": function (data) {
                        if (data != null && data.includes("admin")) {
                            return check_hidden + "<i class='fas fa-check' style='color: green'></i>";
                        }
                        else {
                            return  uncheck_hidden + "<i class=\"fas fa-minus\"></i>";
                        }
                    }
                },
                {
                    "aTargets": [9],
                    "mRender": function (data) {
                        if (data != null && data.includes("vod")) {
                            return check_hidden + "<i class='fas fa-check' style='color: green'></i>";
                        }
                        else {
                            return  uncheck_hidden + "<i class=\"fas fa-minus\"></i>";
                        }
                    }
                },
                {
                    "aTargets": [10],
                    "mRender": function (data) {
                        if (data != null && data.includes("live")) {
                            return check_hidden + "<i class='fas fa-check' style='color: green'></i>";
                        }
                        else {
                            return  uncheck_hidden + "<i class=\"fas fa-minus\"></i>";
                        }
                    }
                },
                {
                    "aTargets": [11],
                    "mRender": function (data) {
                        if (data != null && data.includes("events")) {
                            return check_hidden + "<i class='fas fa-check' style='color: green'></i>";
                        }
                        else {
                            return  uncheck_hidden + "<i class=\"fas fa-minus\"></i>";
                        }
                    }
                },
                {
                    "aTargets": [12],
                    "mRender": function (data,type,row) {
                       let hiddenDataForSorting = '<span style="display:none">' + data + '<br/></span>';
                       if (data === true) {
                           return hiddenDataForSorting + "<input type=\"checkbox\" checked class=\"vis_toggle\" data-id=\"" + row.id + "\">";
                       }
                       else {
                           return hiddenDataForSorting + "<input type=\"checkbox\"  class=\"vis_toggle\">";
                       }
                    }
                },
                {
                    "aTargets": [13],
                    "className": "border_left",
                    "mRender": function (data) {
                        return '<button type="button" class="btn blue-btn-wcag-bgnd-color btn-pill btn-sm text-white"><i class="fas fa-edit"></i> </button>';
                    }
                }
            ],
            "initComplete": set_display_results,
        });
        messagesDT.on( 'order.dt search.dt', function () {
            messagesDT.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
                cell.innerHTML = i+1;
            } );
        } ).draw();

        RegisterEvents();

        function InitControls() {
            $("#message_type").select2({});
            $("#message_target").select2({});
            $("#message_toggle").bootstrapToggle({
                on: '<i class="fas fa-power-off"></i>',
                off: '<i class="fas fa-ban"></i>',
                onstyle: "success",
                offstyle: "danger",
                size: "small"
            });
        }

        function RegisterEvents() {

            $('body').on('change', '.vis_toggle', function(e) {
                let table_cell = $(this).closest('td');
                let rowIdx = messagesDT.cell(table_cell).index().row;
                let row_data = messagesDT.row( rowIdx ).data();
                let message_type    = row_data.status;
                let message_descr   = row_data.text;
                let id = row_data.id;
                let value = $(this).prop('checked');

                let target = [];
                let row_target = row_data.target;
                if (row_target != null && row_target.includes("admins")) {
                    target.push("admins");
                }
                if (row_target != null && row_target.includes("visitors")) {
                    target.push("visitors");
                }
                if (row_target != null && row_target.includes("users")) {
                    target.push("users");
                }
                //Check Site
                let sites = [];
                let row_site = row_data.site;
                if (row_site != null && row_site.includes("admin")) {
                    sites.push("admin");
                }
                if (row_site != null && row_site.includes("vod")) {
                    sites.push("vod");
                }
                if (row_site != null && row_site.includes("live")) {
                    sites.push("live");
                }
                if (row_site != null && row_site.includes("events")) {
                    sites.push("events");
                }

                let messageData = {
                        "id": id,
                        "status": message_type,
                        "target": target,
                        "site" : sites,
                        "text": message_descr,
                        "visible" : value
                 };
                postUpdate(messageData);
            }) ;

            $messagesDtElem.on("dblclick", "tbody td", function (e) {
                // get selected row index
                let table_cell = $(this).closest('td');
                let rowIdx = messagesDT.cell(table_cell).index().row;

                setupMessageEdit ("edit", "Επεξεργασία Μηνύματος",rowIdx);
                e.stopPropagation();
            });
            $messagesDtElem.on("click", "tbody button", function () {

                // get selected row index
                let table_cell = $(this).closest('td');
                let rowIdx = messagesDT.cell(table_cell).index().row;
                setupMessageEdit ("edit", "Επεξεργασία Μηνύματος",rowIdx);
            });

            $("#addOrUpdateMessage").on("click", function() {

                loader.showLoader();
                let messageId      = $("#message_id").val();
                let message_type   = $("#message_type").val();
                let message_descr    = $("#message_descr").val();
                let message_visible  = $("#message_toggle").prop('checked');

                let target = [];
                if ($("#target_visitors").prop("checked")) {
                    target.push("visitors");
                }
                if ($("#target_admins").prop("checked")) {
                    target.push("admins");
                }
                if ($("#target_users").prop("checked")) {
                    target.push("users");
                }
                let sites  = [];
                if ($("#site_admin").prop("checked")) {
                    sites.push("admin");
                }
                if ($("#site_vod").prop("checked")) {
                    sites.push("vod");
                }
                if ($("#site_live").prop("checked")) {
                    sites.push("live");
                }
                if ($("#site_events").prop("checked")) {
                    sites.push("events");
                }
                if (sites.length < 1 || target.length < 1 || message_type == null || message_type === "" || message_descr == null || message_descr === "") {
                    loader.hideLoader();
                    alertify.alert("<i style='color:red' class='fas fa-exclamation-triangle'></i> Πρόβλημα","Υπάρχουν παραλείψεις στη φόρμα. Διορθώστε τα κενά και προσπαθήστε πάλι")

                }
                else {
                    let messageData = {
                        "id": messageId,
                        "status": message_type,
                        "target": target,
                        "site" : sites,
                        "text": message_descr,
                        "visible" : message_visible
                    };
                    postUpdate(messageData);
                }
            });
            $("#deleteMessage").on("click", function (e) {

                let message_id = $("#message_id").val();

                let msg = '<div class="font-weight-bold">Το Μήνυμα Θα διαγραφεί! Είστε σίγουρος;</div>';
                msg += '<div>'
                alertify.confirm('<i style="color: red" class="fas fa-trash-alt"></i> Διαγραφή Μηνύματος', msg,
                    function () {
                        postDelete(message_id);
                    },
                    function () {
                    }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});

                e.preventDefault();
            });
            $("#closeUpdateMessage").on('click',function(){
                let end_serialize = $("#message_form").serialize();
                if (serialize_form !== end_serialize) {
                    closeEditDialogWarning();
                }
                else {
                    unloadEditForm();
                }
            });
            $("#newMessageBt").on("click", function() {
                setupMessageEdit ("add", "Νέο Μήνυμα",-1);
            });

        }

        function setupMessageEdit (action, title, rowIdx) {
            $("#message_edit_mode").val(action);
            $("#messageModalLabel").html(title);
            let $message_toggle = $('#message_toggle');
            $message_toggle.bootstrapToggle('off');

            if (rowIdx !== -1) {
                let row_data = messagesDT.row( rowIdx ).data();
                $("#message_id").val(row_data.id);
                $("#message_type").val(row_data.status).trigger("change");
                $("#message_descr").val(row_data.text);
                if (row_data.visible) {
                    $message_toggle.bootstrapToggle('on');
                }
                // Check Target
                let row_target = row_data.target;
                if (row_target != null && row_target.includes("admins")) {
                    $("#target_admins").prop("checked", true);
                }
                if (row_target != null && row_target.includes("visitors")) {
                    $("#target_visitors").prop("checked", true);
                }
                if (row_target != null && row_target.includes("users")) {
                    $("#target_users").prop("checked", true);
                }
                //Check Site
                let row_site = row_data.site;
                if (row_site != null && row_site.includes("admin")) {
                    $("#site_admin").prop("checked", true);
                }
                if (row_site != null && row_site.includes("vod")) {
                    $("#site_vod").prop("checked", true);
                }
                if (row_site != null && row_site.includes("live")) {
                    $("#site_live").prop("checked", true);
                }
                if (row_site != null && row_site.includes("events")) {
                    $("#site_events").prop("checked", true);
                }

                $("#deleteMessage").show();
                $("#st_rowIdx_edited").val(rowIdx);
            }
            else {
                    $("#message_id").val("");
                    $("#message_type").val("success").trigger("change");
                    $("#message_descr").val("");
                    $("#deleteMessage").hide();
                    $("#st_rowIdx_edited").val("");
                    //TARGET
                    $("#target_admins").prop("checked",false);
                    $("#target_users").prop("checked",false);
                    $("#target_visitors").prop("checked",false);
                    //SITE
                    $("#site_admin").prop("checked",false);
                    $("#site_vod").prop("checked",false);
                    $("#site_live").prop("checked",false);
                    $("#site_events").prop("checked",false);

            }
            $("#messages_card").hide();
            $("#message_edit_card").show();
            serialize_form = $("#message_form").serialize();
        }

        function postUpdate(messageData) {
            $.ajax({
                type:        "POST",
                url: 		  dashboard.siteurl + '/api/v1/message/save',
                contentType: "application/json; charset=utf-8",
                data: 		  JSON.stringify(messageData),
                async:		  true,
                success: function(data){
                    //Highlight edited/added row
                    dashboard.messages.refreshMessagesTable();
                    $("#message_id").val(data);
                    alertify.notify("Το μήνυμα αποθηκεύτηκε με επιτυχία", "success");
                    serialize_form = $("#message_form").serialize();
                    loader.hideLoader();
                },
                error: function ()  {
                    loader.hideLoader();
                    alertify.alert('Error-Update-Message');
                }
            });
        }

        function postDelete(messageId) {

            $.ajax({
                type:        "DELETE",
                url: 		  dashboard.siteurl + '/api/v1/message/delete/' + messageId,
                contentType: "application/json; charset=utf-8",
                async:		  true,
                success: function(){
                    unloadEditForm();
                    dashboard.messages.refreshMessagesTable();
                    alertify.notify("Το Μήνυμα διαγράφηκε με επιτυχία", "success");
                },
                error: function (data)  {
                    let info = "Άγνωστο Σφάλμα";
                    let msg = data.responseText;
                    alertify.alert('Σφάλμα', '<i style="color: red" class="fas fa-exclamation-circle"></i> ' + msg + info);
                }
            });
        }

        function unloadEditForm() {
            $("#message_edit_card").hide();
            $("#messages_card").show();
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

    };

    function  set_display_results() {
        $("#count_messages_results").html("" + messagesDT.rows().count() + "");
        $('.vis_toggle').bootstrapToggle({
            on: '<i class="fas fa-power-off"></i>',
            off: '<i class="fas fa-ban"></i>',
            onstyle: "success",
            offstyle: "danger",
            size: "small"
        });
    }
    dashboard.messages.refreshMessagesTable = function () {
        messagesDT.ajax.reload( function ( ) {
            set_display_results();
        } );
    };

})();