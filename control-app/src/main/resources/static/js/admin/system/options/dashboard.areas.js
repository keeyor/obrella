(function () {
    'use strict';

    dashboard.areas = dashboard.areas || {};

    let  areasDT = null;
    let serialize_form;

    dashboard.areas.init = function () {

        InitControls();

        let $areasDtElem = $("#eventAreasDataTable");

        areasDT = $areasDtElem.DataTable({
            "ajax":  dashboard.siteurl + '/api/v1/dt/eareas-sorted.web',
            "columns": [
                {"data": "parentId"},
                {"data": "title"},
                {"data": "title_en"},
                {"data": "shortCode"},
                {"data": "order"},
                {"data": "id"}
            ],
            "language": dtLanguageGr,
            "dom": '<"top"fl><p>rt<"bottom">p<"clear">',
            ordering: false,
            "pagingType": "full_numbers",
            "pageLength": 25,
            "aoColumnDefs": [
                 {
                    "aTargets": [0],
                    "visible" : false
                },
                {
                    "aTargets": [1],
                    "mRender": function (data,type,row) {

                        let parentId = row["parentId"];
                        if (parentId == null || parentId === "") {
                            return '<span style="font-weight:700">' + data + '</span>';
                        }
                        else {
                            return '<i class="fas fa-level-up-alt fa-rotate-90 ml-2 mr-2" style="color: #ececf6"></i><span style="font-weight:400">' + data + '</span>';
                        }
                    }
                },
                {
                    "aTargets": [4],
                    "className": "border_left",
                    "mRender": function (data,type, row) {
                        let order = row["order"];
                        if (order !== 0) {
                            return data +
                                '<button class="btn btn-secondary btn-sm text-white moveDown mr-1 ml-1" title="μετακίνηση κάτω"><i class="fas fa-caret-down"></i> </button>' +
                                '<button class="btn btn-secondary btn-sm text-white moveUp mr-1 ml-1" title="μετακίνηση πάνω"><i class="fas fa-caret-up"></i> </button>';
                        }
                        else {
                            return '0';
                        }
                    }
                },
                {
                    "aTargets": [5],
                    "className": "border_left",
                    "mRender": function (data, type, row) {
                        let order = row["order"];
                        let ret= "";
                            if (order === 0) {
                                ret += '<button type="button" class="btn blue-btn-wcag-bgnd-color btn-pill btn-sm text-white mr-2 mainArea" title="επεξεργασία κατηγορίας"><i class="fas fa-edit"></i> </button>';
                            }
                            else {
                                ret += '<button type="button" class="btn blue-btn-wcag-bgnd-color btn-pill btn-sm text-white mr-2 subArea"  title="επεξεργασία ενότητας"><i class="fas fa-edit"></i> </button>';
                            }
                            if (order === 0) {
                                ret += '<button type="button" class="btn btn-success btn-sm text-white addSubArea"  title="προσθήκη ενότητας"><i class="fas fa-plus-circle"></i> </button>'
                            }
                            return ret;
                    }
                }
            ],
            "initComplete": setupMoveArrows
        });

        $areasDtElem.on("click", "tbody button", function () {
            // get selected row index
            let table_cell = $(this).closest('td');
            let rowIdx = areasDT.cell(table_cell).index().row;
            if ($(this).hasClass('addSubArea')) {
                setupAreaEdit("addSubArea", "Εισαγωγή Θεματικής Ενότητας", rowIdx);
            }
            else if ($(this).hasClass('moveDown')) {
                let row_data = areasDT.row( rowIdx ).data();
                console.log('MoveDown:' + row_data.order + ' of parentId:' + row_data.parentId);
                postMove(row_data.id,"down");
            }
            else if ($(this).hasClass('moveUp')) {
                let row_data = areasDT.row( rowIdx ).data();
                console.log('MoveDown:' + row_data.order + ' of parentId:' + row_data.parentId);
                postMove(row_data.id,"up");
            }
            else {
                if ($(this).hasClass('mainArea')) {
                    setupAreaEdit("editArea", "Επεξεργασία Κατηγορίας", rowIdx);
                }
                else {
                    setupAreaEdit("editSubArea", "Επεξεργασία Θεματικής Ενότητας", rowIdx);
                }
            }

        });

       InitControls;
       RegisterEvents();
    };

    function setupMoveArrows() {
        areasDT.rows().every( function ( rowIdx, tableLoop, rowLoop ) {
            let data = this.data();
            let order_of_row = data["order"];
            if (order_of_row === 1) {
                $('#eventAreasDataTable tr:nth-child(' + (rowIdx+1) + ') .moveUp').hide();
            }
            let nrd = $('#eventAreasDataTable').dataTable().fnGetData( rowIdx+1 );
            if ( nrd === null || nrd.order === 0) {
                $('#eventAreasDataTable tr:nth-child(' + (rowIdx+1) + ') .moveDown').hide();
            }
        });
    }

    function InitControls() {

    }
    function RegisterEvents() {

        $("#addOrUpdateArea").on("click", function() {

            let areaId           = $("#area_id").val();
            let area_title       = $("#area_title").val();
            let area_title_en    = $("#area_title_en").val();
            let area_short_code  = $("#area_short_code").val();
            let area_parentId    = $("#area_parentId").val();
            let area_order       = $("#area_order").val();

            if (area_title == null || area_title === "" || area_title_en == null || area_title_en === "" || area_short_code == null || area_short_code === "") {
                alertify.alert("<i style='color:red' class='fas fa-exclamation-triangle'></i> Πρόβλημα","Υπάρχουν παραλείψεις στη φόρμα. Διορθώστε τα κενά και προσπαθήστε πάλι")
            }
            else {
                let areaData = {
                    "id": areaId,
                    "title": area_title,
                    "title_en": area_title_en,
                    "shortCode" : area_short_code,
                    "order" : area_order,
                    "parentId": area_parentId
                };
                postUpdate(areaData);
            }
        });

        $("#deleteArea").on("click", function (e) {

            let area_id = $("#area_id").val();

            let msg = '<div class="font-weight-bold">Η Θεματική Περιοχή Θα διαγραφεί! Είστε σίγουρος;</div>';
            msg += '<div>'
            alertify.confirm('<i style="color: red" class="fas fa-trash-alt"></i> Διαγραφή Περιοχής', msg,
                function () {
                    postDelete(area_id);
                },
                function () {
                }).set('labels', {ok: 'Ναί!', cancel: 'Ακύρωση'});

            e.preventDefault();
        });

        $("#newAreaBt").on("click", function() {
            setupAreaEdit ("addArea", "Νέα Θεματική Κατηγορία",-1);
        });

    }

    function setupAreaEdit (action, title, rowIdx) {

        $("#area_edit_mode").val(action);
        $("#areaModalLabel").html(title);
        let row_data = areasDT.row( rowIdx ).data();

        if (action === "addSubArea") {
            //order of clicked row is always 0
            //# place new subArea to the end of the current Area ( set order = max_order_of_area + 1 )
            let max_order_of_area = getMaxOrderOfArea(rowIdx);

            $("#header_parent").html("Προσθήκη Θεματικής Περιοχής στην κατηγορία: " + row_data.title);
            $("#area_id").val("");
            $("#area_title").val("");
            $("#area_title_en").val("");
            $("#area_short_code").val("");
            $("#area_parentId").val(row_data.id);
            $("#area_order").val(max_order_of_area + 1);
            $("#deleteMessage").hide();
            $("#st_rowIdx_edited").val("");
        }
        else if (action === "addArea") {
            $("#area_id").val("");
            $("#area_title").val("");
            $("#area_title_en").val("");
            $("#area_short_code").val("");
            $("#area_parentId").val("");
            $("#area_order").val(0);
            $("#deleteMessage").hide();
            $("#st_rowIdx_edited").val("");
        }
        else {
            if (rowIdx !== -1) {                        // Edit Area or SubArea

                $("#area_id").val(row_data.id);
                $("#area_title").val(row_data.title);
                $("#area_title_en").val(row_data.title_en);
                $("#area_parentId").val(row_data.parentId);
                $("#area_short_code").val(row_data.shortCode);
                $("#area_order").val(row_data.order);
                $("#deleteMessage").show();
                $("#st_rowIdx_edited").val(rowIdx);
            }
        }
        $("#areaEditModal").modal("show");
    }

    function getMaxOrderOfArea(rowIdx) {
        let tmp_order = -1;
        let tmp_data = areasDT.row( rowIdx ).data();
        let max_order_of_area = 0;
        let idx = rowIdx;
        while (tmp_order !== 0) {
            idx = idx + 1;
            tmp_data = areasDT.row( idx ).data();
            if (tmp_data != null) {
                tmp_order = tmp_data.order;
                if (tmp_order === 0) {
                    break;
                } else {
                    max_order_of_area = tmp_order;
                }
            }
            else {
                break;
            }
        }
        return max_order_of_area;
    }

    function postMove(subareaId, action) {
        $.ajax({
            type:        "POST",
            url: 		  dashboard.siteurl + '/api/v1/areas/moveaction/' + action,
            contentType: "application/json; charset=utf-8",
            data: 		  subareaId,
            async:		  true,
            success: function(){
                dashboard.areas.refreshAreasTable();
            },
            error: function ()  {
                alertify.alert('Error-MoveDown-SubArea');
            }
        });
    }

    function postUpdate(areaData) {
        $.ajax({
            type:        "POST",
            url: 		  dashboard.siteurl + '/api/v1/areas/save',
            contentType: "application/json; charset=utf-8",
            data: 		  JSON.stringify(areaData),
            async:		  true,
            success: function(data){
                $("#areaEditModal").modal("hide");
                dashboard.areas.refreshAreasTable();
                alertify.notify("Η θεματική περιοχή αποθηκεύτηκε με επιτυχία", "success");
            },
            error: function (errorCode)  {
                if (errorCode.responseText === "SHORT_CODE_ERROR") {
                    alertify.alert('Ο Σύντομος Κωδικός υπάρχει ήδη...πληκτρολογήστε διαφορετικό κωδικό');
                }
                else {
                    alertify.alert('Άγνωστο Σφάλμα. Δείτε το αρχείο καταγραφής στα περισσότερες πληροφορίες');
                }
            }
        });
    }

    function postDelete(areaId) {

        $.ajax({
            type:        "DELETE",
            url: 		  dashboard.siteurl + '/api/v1/areas/delete/' + areaId,
            contentType: "application/json; charset=utf-8",
            async:		  true,
            success: function(){
                $("#areaEditModal").modal("hide");
                dashboard.areas.refreshAreasTable();
                alertify.notify("Η Θεματική Περιοχή διαγράφηκε με επιτυχία", "success");
            },
            error: function (data)  {
                let info = "Άγνωστο Σφάλμα";
                let msg = data.responseText;
                alertify.alert('Σφάλμα', '<i style="color: red" class="fas fa-exclamation-circle"></i> ' + msg + info);
            }
        });
    }
    dashboard.areas.refreshAreasTable = function () {
        areasDT.ajax.reload( function ( ) {
            setupMoveArrows();
        } );
    };

})();