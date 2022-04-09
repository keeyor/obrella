(function () {
    'use strict';

    dashboard.institutions = dashboard.institutions || {};

    dashboard.institutions.institutionId = null;
    dashboard.institutions.institutionName = null;

    dashboard.institutions.init = function () {

                dashboard.institutions.institutionId  =$("#institutionIdentity").val();
                dashboard.institutions.institutionName = $("#institutionTitle").val();

                $("#institution_license").select2();
                //get default institution
                getInstitution();

                let $InstitutionModalButton = $(".institutionModalButton");

                $InstitutionModalButton.on("click", function() {
                    setupInModal ("Τροποποίηση Ιδρύματος");
                    return false;
                });
                $("#addOrUpdateInstitution").on("click", function() {
                    postUpdate();
                });

                function setupInModal (title) {
                    $("#institutionModalLabel").html(title);
                    $('#institutionModal').modal('show');
                }
                function postUpdate() {

                   let institutionData = {
                        "id": $("#institution_id").val(),
                        "identity": $("#institutionIdentity").val(),
                        "title": $("#institution_title").val(),
                        "url": $("#institution_url").val(),
                        "administrator": {
                            "name": $("#institution_admin_name").val(),
                            "email": $("#institution_admin_email").val()
                        },
                       "logoUrl": $("#institution_logourl").val()
                    };

                    $.ajax({
                        type:        "PUT",
                        url: 		  dashboard.siteurl + '/api/v1/institution/update',
                        contentType: "application/json; charset=utf-8",
                        data: 		  JSON.stringify(institutionData),
                        async:		  true,
                        success: function(){
                            getInstitution();
                            $("#institutionModal").modal('hide');
                        },
                        error: function ()  {
                            alertify.alert('Error-Update-Institution');
                        }
                    });
                } //postUpdate
                function getInstitution() {
                    let get_url = dashboard.siteurl + '/api/v1/institution/identity/' + dashboard.institutions.institutionId;
                    $.get(get_url)
                        .done(function( data ) {
                            $("#institution_id").val(data.id);
                            $("#institution_title").val(data.title);
                            $("#institution_url").val(data.url);                            $("#i_url").html(data.url);
                            $("#institution_logoUrl").val(data.logoUrl);
                            $("#institution_admin_name").val(data.administrator.name);      $("#i_adminName").html(data.administrator.name);
                            $("#institution_admin_email").val(data.administrator.email);    $("#i_adminEmail").html(data.administrator.email);
                            //$("#institution_license").val(data.organizationLicense);
                            //let dat2a = $("#institution_license").select2('data');          $("#i_lic").html(dat2a[0].text);
                        });
                } //getInstitution
    }; //init

})();
