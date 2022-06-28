/*jshint esversion: 6 */
(function () {
    'use strict';
    dashboard.modala = dashboard.modala || {};

    let $PauseErrorMessages;
    let $UpdatePausesButton;
    let $PauseStatusMessages;
    let $pausesDataTable;
    let $argia_button_new;
    let $modal_scope;
    let PausesDT;

    dashboard.modala.init = function () {

        $PauseErrorMessages   = $("#PauseErrorMessages");
        $PauseStatusMessages  = $("#PauseMessages");
        $UpdatePausesButton   = $("#updatePausesButton");
        $pausesDataTable	  = $("#table_p_modal");
        $argia_button_new	  = $("#argia-button-new");
        $modal_scope          = $("#modal_p_scope");

        /*EVENTS*/
        $UpdatePausesButton.on('click', function() {

            loader.showLoader();
            let scope = $modal_scope.val();
            let year = dashboard.selected_year;
            let institutionId = dashboard.institution;
            let dataJSON;
            let url;
            if (scope === "system" || scope === "") {
                dataJSON = getTableDataAsJSON($pausesDataTable, institutionId);
                let dataString  = JSON.stringify(dataJSON);
                url = dashboard.siteurl + '/api/v1/institution/' + institutionId + '/pause/update/' + year;
                postCreateUpdatePauses(url, dataString, scope,  year, institutionId,"" );
            }
            else if (scope === "department") {
                let departmentId = dashboard.department.departmentId;
                dataJSON = getTableDataAsJSON($pausesDataTable, departmentId);
                let dataString  = JSON.stringify(dataJSON);
                url = dashboard.siteurl + '/api/v1/department/' + departmentId + '/pause/update/' + dashboard.selected_year;
                postCreateUpdatePauses(url, dataString, scope, year, institutionId, departmentId);
            }
            console.log(dataJSON);
        });

        dashboard.editPeriodModal.on('show.bs.modal', function() {
            setMessage($PauseStatusMessages,'alert alert-success alert-dismissable hidden', ' ');
        });

        $argia_button_new.on('click', function() {

            // Add NEW ROW to DATATABLE with: name = "" and startDate==endData==Today
            let new_argia = {};
            new_argia.name = "[Τίτλος Αργίας/Παύσης]";

            let d = new Date();
            let curr_date = d.getDate();
            let curr_month = d.getMonth() + 1; //Months are zero based
            let curr_year = d.getFullYear();

            if (curr_month < 10) { curr_month = "0" + curr_month;}
            let toDay = curr_year + "-" + curr_month + "-" + curr_date;

            new_argia.startDate = toDay;
            new_argia.endDate =  toDay;

            $pausesDataTable.DataTable().row.add(new_argia).draw(true);
            let data = getTableDataAsJSON($pausesDataTable,"");
            myDataTableCallback(data, "new_argia_added"); //!Important
        });

    };

    dashboard.modala.initDataTable = function (year,institutionId, departmentId) {

        let  url;
        if (departmentId === "") {
            url = dashboard.siteurl + '/api/v1/dt/institution/' + institutionId + '/pause/' + year;
        }
        else {
            url = dashboard.siteurl + '/api/v1/dt/department/' + departmentId + '/pause/' + year;
        }

        PausesDT = $("#table_p_modal").DataTable({
            "bProcessing": false,
            "bDestroy": true,
            "bFilter": false,
            "bPaginate": false,
            "oLanguage": dtLanguageGr,
            // "order": [[1, 'asc']],
            "ajax":  {
                "url":  url,
                "dataSrc":  "data.argies.argia"
            },
            "columns": [
                { "mData": null , "sWidth": "40px", "bSortable": false, "bVisible":false },
                { "mData": "startDate", "sWidth": "0px", "bSortable": true, "bVisible":false },	// sorting, actual database data
                { "mData": "endDate", "sWidth": "0px", "bSortable": true, "bVisible":false },	// sorting, actual database data
                { "mData": "name", "bSortable": false},
                { "mData": "startDate", "sWidth": "200px", "bSortable": false },				// display data formatted
                { "mData": "endDate", "sWidth": "200px", "bSortable": false },					// display data formatted
                { "mData": null, "sWidth": "40px", "bSortable": false }							// delete button
            ],
            "columnDefs": [
                {
                    "name": "index",
                    "render": function (data,type,row) {
                        return row;
                    },
                    "targets" :0
                },
                {
                    "name": "startDateData",
                    "render": function (data) {
                        return data;
                    } ,
                    "targets" :1
                },
                {
                    "name": "endDateData",
                    "render": function (data) {
                        return data;
                    },
                    "targets" : 2
                },
                {
                    "name": "name",
                    "render": function (data,type,row, meta) {
                        return '<input class="form-control" size="30" type="text" value="' + data + '"  id="data_row_' + meta.row + '" name="data_row-' + meta.row + '"/>';
                    },
                    "targets" :3
                },
                {
                    "name": "startDate",
                    "render": function (data,type,row, meta) {

                        return '<div class="input-group  date modal-pstart-date" data-row="' + meta.row + '" id="div-startpDate-' + meta.row + '"  >' +
                                    '<span class="input-group-addon input-group-text">' +
                                        '<i class="fas fa-calendar-alt"></i>' +
                                    '</span>' +
                                    '<input class="form-control" value=""  />' +
                                '</div>';
                    },
                    "targets" : 4
                },
                {
                    "name": "endDate",
                    "render": function (data,type,row, meta) {


                        return '<div class="input-group date modal-pend-date" data-row="' + meta.row + '" id="div-endpDate-' + meta.row + '"  >' +
                                        '<span class="input-group-addon input-group-text">' +
                                            '<i class="fas fa-calendar-alt"></i>' +
                                        '</span>' +
                                        '<input class="form-control" value=""  />' +
                                '</div>';
                    },
                    "targets" : 5
                },
                {

                    "name": "name",
                    "render": function (data,type,row,meta) {
                        return "<button data-row='" + meta.row + "' class='btn btn-light delete_row float-end'><i class=\"far fa-trash-alt\"></i></button>";
                    },
                    "targets" : 6
                },
            ],
            "initComplete": function( settings, json ) {
                myDataTableCallback(json.data.argies);

            }
        }); // DataTable init

        PausesDT.on('order.dt search.dt', function () {
            PausesDT.column(0, { search: 'applied', order: 'applied' }).nodes().each(function (cell, i) {
                cell.innerHTML = i + 1;
            });
        }).draw();
    };
    var myDataTableCallback = function(json, action) {

        let table = $("#table_p_modal").DataTable();

        for (let i=0; i < json.argia.length; i++) {

            let $startDateElementInPosI = $(`#div-startpDate-${i}`);
            $startDateElementInPosI.datepicker({
                format: "dd MM yyyy",
                todayBtn: false,
                language: "el",
                autoclose: true,
                todayHighlight: false
            });

            if (json.argia[i].startDate !== "") {
                let d = new Date(json.argia[i].startDate);
                let curr_date = d.getDate();
                let curr_month = d.getMonth() + 1; //Months are zero based
                let curr_year = d.getFullYear();

                let startdate = curr_date + "/" + curr_month + "/" + curr_year;
                $startDateElementInPosI.datepicker("setDate", startdate);
            }
            else {
                $startDateElementInPosI.datepicker("setDate", "");
            }
            let $endDateElementInPosI = $(`#div-endpDate-${i}`);
            $endDateElementInPosI.datepicker({
                format: "dd MM yyyy",
                todayBtn: false,
                language: "el",
                autoclose: true,
                todayHighlight: false
            });
            if (json.argia[i].endDate !== "") {
                let d = new Date(json.argia[i].endDate);
                let curr_date = d.getDate();
                let curr_month = d.getMonth() + 1; //Months are zero based
                let curr_year = d.getFullYear();

                let enddate = curr_date + "/" + curr_month + "/" + curr_year;
                $endDateElementInPosI.datepicker("setDate", enddate);
            }
            else {
                $endDateElementInPosI.datepicker("setDate", "");
            }
        }
        //SET FOCUS ON NEWLY CREATED ARGIA
        if (action !== undefined && action === "new_argia_added") {
            let last_row_counter = json.argia.length - 1;
            let $last_argia_input = $("#data_row_" + last_row_counter);
            $last_argia_input.focus();
            $last_argia_input.select();
            let $closest_tr = $last_argia_input.closest('tr');
            $closest_tr.css('background-color', '#2eb85c');
        }

        $(".delete_row").on('click',function(e){
            if ($(this).hasClass("btn-light")) {
                $(this).removeClass("btn-light");
                $(this).addClass("btn-danger");
            }
            else {
                $(this).removeClass("btn-danger");
                $(this).addClass("btn-light");
            }
        });

        $(".modal-pstart-date").on('changeDate', function(ev){

            let row_no  = $(this).data("row");
            $(this).addClass('has-warning');

            let changed_date = moment(ev.date).format('YYYY-MM-DD');
            table.cell({row: row_no, column: 1}).data(changed_date).draw();
            dashboard.modala.checkTableDates(table);
        });
        $(".modal-pend-date").on('changeDate', function(ev){

            if (ev.date) {
                let row_no  = $(this).data("row");
                $(this).addClass('has-warning');

                let changed_date = moment(ev.date).format('YYYY-MM-DD');
                table.cell({row: row_no, column: 2}).data(changed_date).draw();
                dashboard.modala.checkTableDates(table);
            }
        });
    };

    dashboard.modala.checkTableDates = function(table) {


        var data = table.rows().data();
        var message = "<div>Εντοπίστηκαν προβλήματα στη φόρμα<div><ul>";
        var errors = 0;
        //Check overlap between start and end date of the same period
        for (let c=0; c < data.length; c++) {
            let  _r = data[c];
            if (_r.startDate !== "" && _r.endDate !== "") {
                let startDate_m = moment(_r.startDate);
                let endDate_m = moment(_r.endDate);
                if (endDate_m.isBefore(startDate_m)) {
                    errors = 1;
                    message += "<li>" + _r.name + ": Η καταληκτική ημερομηνία είναι <b>πρίν</b> την αρχική: " + "</li>";
                }
            }
        }
        // NULL TITLE
        for (let c=0; c < data.length; c++) {
            let  _r = data[c];
            if (_r.name === null || _r.name === "") {
                errors = 1;
                message += "<li>" + "Πληκτρολογήστε 'Τίτλο' αργίας" + "</li>";
            }
        }

        //Check overlap between end date and start date of the next period

        //TODO: Check αλληλοκάλυψη μεταξύ όλων των αργιών
        /*
                     for (let c=0; c < data.length-1; c++) {
                       let  _r1 = data[c];
                       let  _r2 = data[c+1];

                       if (_r2.startDate !== "" && _r1.endDate !== "") {
                           let endDate_m1 = moment(_r1.endDate).add(1 ,'days');
                           let startDate_m2 = moment(_r2.startDate);


                           if (startDate_m2.isBefore(endDate_m1)) {
                                errors = 1;
                                message += "<li>" + _r2.name+ ": H αργία αρχίζει <b>πρίν</b>  το τέλος της προηγούμενης " + "</li>";

                           }
                         //Check for gaps between periods
                       if (	startDate_m2.diff(endDate_m1, 'days')  > 0) {
                               errors = 1;
                                  message += "<li>" + _r2.name + ": Υπάρχουν <b>κενές μέρες πριν</b> την αρχή της περιόδου " + "</li>";
                           }
                       }
                   } */

        message += "</ul>";
        if (errors === 1) {
            setMessage($PauseErrorMessages,'alert alert-danger show', message);
             $("#updatePausesButton").attr("disabled",true);
        }
        else {
            setMessage($PauseErrorMessages,'alert alert-danger invisible', "");
           $("#updatePausesButton").attr("disabled", false);
        }

    };

    function setMessage($element, attributes, message) {

        $element.attr('class', attributes);
        $element.html(message);
    }

    function getTableDataAsJSON($table,refId) {

        var data = $table.DataTable().rows().data();

        let argies_list = [];
        let argies = {};
        argies.refId = refId;

        for (let c=0; c < data.length; c++) {
            let name = $("#data_row_" + c).val();
            let row_node = $table.DataTable().cell(c,6).node().innerHTML;
            if (row_node.includes("btn-danger") === false) {
                let  _r 		 = data[c];
                let startDate 	 = _r.startDate;
                let endDate 	 = _r.endDate;
                let argia = {"name":name, "endDate" : endDate, "startDate": startDate};
                argies_list.push(argia);
            }
           // console.log("line:" + c + " result:" + row_node.includes("btn-danger"));
        }

        argies.argia = argies_list;
        return argies;
    }

    function postCreateUpdatePauses(postURL, dataJSON, scope, year, institutionId, departmentId) {

        $.ajax({

            url: postURL,
            type:"POST",
            contentType: "application/json; charset=utf-8",
            data: dataJSON,
            async: true,    	//Cross-domain requests and dataType: "jsonp" requests do not support synchronous operation
            success: function() {
                    loader.hideLoader();
                    setMessage($PauseStatusMessages,'alert alert-success alert-dismissable visible',
                        '<b><i class="fas fa-thumbs-up me-1"></i>Επιτυχής Ενημέρωση<b>');

                    setTimeout(function() {
                        setMessage($PauseStatusMessages,'alert alert-success invisible', " ");
                        $("#argies_card_edit").hide();
                        $("#argies_card").show();
                    }, 1500);

                    let message = {msg: "Academic Calendar Updated!", year: dashboard.selected_year, department: departmentId, institution: institutionId};
                    dashboard.broker.trigger('refresh.page', [message]);
            },
            error : function() {
                loader.hideLoader();
                $("#argies_card_edit").hide();
                $("#argies_card").show();
                setMessage($PauseStatusMessages,'alert alert-danger alert-dismissable show',
                    '<b><i class="fas fa-exclamation-triangle me-1"></i>Πρόβλημα Συστήματος. Επικοινωνήστε με το διαχειριστή<b>');
            }
        });
    }

})();
