$(".toggle-sidebar").click(function (e) {
    var button = $(this);// Button that triggered the modal
    var lectureId = button.data('lec') // Extract info from data-* attributes

    let siteUrl = getRootSitePath();
    getResourceByIdInRightSideBar(siteUrl, lectureId);
    toggleSidebar("right");
    return false;
});
$(".close-sidebar").click(function (e) {
    closeSidebar("right");
    return false;
});



/*$('#resource_view_modal').on('show.bs.modal', function (event) {
    var button = $(event.relatedTarget) // Button that triggered the modal
    var lectureId = button.data('lec') // Extract info from data-* attributes

    let siteUrl = getRootSitePath();
    getResourceById(siteUrl, lectureId);
});*/
$('.resource_view_modal_link').on('click', function (event) {
    var button = $(this);
    var lectureId = button.data('lec') // Extract info from data-* attributes

    let siteUrl = getRootSitePath();
    getResourceById(siteUrl, lectureId);
    $("#resource_view_modal").modal("show");
    event.preventDefault();
});
/*$('#resource_view_modal').on('show.coreui.modal', function (event) {
    var button = $(event.relatedTarget) // Button that triggered the modal
    var lectureId = button.data('lec') // Extract info from data-* attributes

    let siteUrl = getRootSitePath();
    getResourceById(siteUrl, lectureId);
});*/
function getRootSitePath() {

    let _location = document.location.toString();
    let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
    let applicationName = _location.substring(0, applicationNameIndex) + '/';
    let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

    return _location.substring(0, webFolderIndex);
}

function getResourceById(siteUrl, id) {

    let url =  dashboard.siteUrl + '/apiw/v1/resource/' + id;
    $.ajax({
        type: 'GET',
        url: url,
        dataType: 'json',
        success: function (data) {
                $("#resource-id").html(data.id);
                $("#resource-identity").html(data.storage);
                let header_html = '<i class="fas fa-info-circle"></i> ' + data.title;
                if (data.partNumber != null) {
                    header_html += ' / Μέρος ' + data.partNumber;
                }
                header_html += '<br/>' + '<span class="text-muted" style="font-size: 0.6em;font-style: italic; font-weight: normal">Κωδικός Συστήματος: ' + data.id + ' (' + data.storage + ')</span>';
                $("#modalTitle").html(header_html);

                let dlist = '<dl class="row mt-0">';
                if (data.course !== null) {
                    dlist += '<dt class="col-sm-12 mt-0 pt-2 mb-2 text-white" style="border-bottom: #cce5ff 1px solid;background-color: #006A9B"><h6>Διάλεξη Μαθήματος</h6></dt>';
                    dlist +=
                             '<dt class="col-sm-6">Υπεύθυνος Καθηγητής</dt><dd class="col-sm-6">' + data.supervisor.name + ', '  + data.supervisor.affiliation + '</dd>' +
                             '<dt class="col-sm-6">Τμήμα</dt><dd class="col-sm-6">' + data.supervisor.department.title + '</dd>';
                }
                else {
                    dlist += '<dt class="col-sm-12 mt-2 pt-2 mb-2 text-white" style="border-bottom: #cce5ff 1px solid;background-color: #006A9B"><h6>Μαγνητοσκοπημένη Εκδήλωση</h6></dt>';
                }
                dlist += '<dt class="col-sm-6">Περιγραφή</dt><dd class="col-sm-6 text-wrap">' + data.description + '</dd>' +
                         '<dt class="col-sm-6">Ημ. Εγγραφής</dt><dd class="col-sm-6">' + moment.utc(data.date).format("LL") + '</dd>' +
                         '<dt class="col-sm-6">Ημ. Ενημέρωσης</dt><dd class="col-sm-6">' + moment.utc(data.dateModified).format("LL") + '</dd>';

                if (data.course !== null) {
                        dlist += '<dt class="col-sm-12 mt-2 pt-2 mb-2" style="border-bottom: #cce5ff 1px solid"><h6><b>Μάθημα</b></h6></dt>';
                        dlist += '<dt class="col-sm-6">Τίτλος</dt><dd class="col-sm-6">' + data.course.title + '</dd>' +
                                 '<dt class="col-sm-6">Τμήμα</dt><dd class="col-sm-6">' + data.course.department.title + '</dd>' +
                                 '<dt class="col-sm-6">Εξάμηνο</dt><dd class="col-sm-6">' + semester[data.course.semester] + '</dd>' +
                                 '<dt class="col-sm-6">Κωδικός Γραμματείας</dt><dd class="col-sm-6">';
                                  if (data.course.scopeId !== data.course.identity) {
                                        dlist += data.course.scopeId;
                                  }
                                  dlist += '&nbsp;</dd>';
                                  dlist += '<dt class="col-sm-6">Κωδικοί LMS</dt><dd class="col-sm-6">';
                                  $.each(data.course.lmsReferences, function (index, el) {
                                        dlist += (index>0 ? ', ' : '') + el.lmsId + ' [' + el.lmsCode + ']';
                                  });
                                  dlist += '</dd>';
                }
                else if ( data.event != null) {
                        dlist += '<dt class="col-sm-12 mt-2 pt-2 mb-2" style="border-bottom: #cce5ff 1px solid"><h6>Εκδήλωση</h6></dt>'
                        dlist += '<dt class="col-sm-6">Τίτλος</dt><dd class="col-sm-6">' + data.event.title + '</dd>';
                        dlist += '<dt class="col-sm-6">Οργάνωση</dt><dd class="col-sm-6">';
                        if (data.event.responsibleUnit != null) {
                            $.each(data.event.responsibleUnit, function (index, ru) {
                                dlist += (index>0 ? ', ' : '');
                                if (ru.structureType === 'DEPARTMENT') { dlist += 'Τμήμα ';}
                                if (ru.structureType === 'SCHOOL') { dlist += 'Σχολή ';}
                                dlist += ru.title;
                            });
                        }
                        dlist += '</dd>';
                        dlist += '<dt class="col-sm-6">Υπεύθυνος</dt><dd class="col-sm-6">';
                        if (data.event.responsiblePerson != null) {
                            dlist += data.event.responsiblePerson.name + ', '  + data.event.responsiblePerson.affiliation + '</dd>';
                        }
                        dlist += '</dd>';
                }
                        if (data.status.inclMultimedia !== -1) {
                            dlist += '<dt class="col-sm-12 mt-2 pt-2 mb-2" style="border-bottom: #cce5ff 1px solid"><h6>Πολυμέσα</h6></dt>' +
                                '<dt class="col-sm-6">Τύπος/Διαμόρφωση</dt><dd class="col-sm-6">' + data.resourceAccess.type + '/' + data.resourceAccess.format + '</dd>' +
                                '<dt class="col-sm-6">Διάρκεια</dt><dd class="col-sm-6">' + data.resourceAccess.duration + '</dd>' +
                                '<dt class="col-sm-6">Ανάλυση (Αναλογία Απεικόνισης)</dt><dd class="col-sm-6">' + data.resourceAccess.resolution + ' (' + data.resourceAccess.aspectRatio + ')</dd>';
                        }
                        if (data.status.inclPresentation !== -1) {
                            dlist += '<dt class="col-sm-12 mt-2 pt-2 mb-2" style="border-bottom: #cce5ff 1px solid"><h6>Παρουσίαση</h6></dt>' +
                                '<dt class="col-sm-6">Συννημένη Παρουσίαση (Αριθμός Διαφανειών)</dt><dd class="col-sm-6">';
                            dlist += ((data.presentation !== null) ? ' ΝΑΙ' : ' ΟΧΙ') +
                                ((data.presentation.slides !== null) ? ' (' + data.presentation.slides.length + ')' : ' (-) ');
                            dlist += '</dd>';
                        }

                            dlist += '<dt class="col-sm-12 mt-2 pt-2 mb-2" style="border-bottom: #cce5ff 1px solid"><h6>Πνευματικά Δικαιώματα</h6></dt>' +
                                '<dt class="col-sm-6">Άδεια Χρήσης</dt><dd class="col-sm-6">' + language[data.license] +
                                '<a class="ml-2" href="' + language[data.license + '_url'] + '" target="_blank"><i class="fas fa-external-link-alt"></i></a>' +
                                '</dd>';

                        if (data.status.inclMultimedia !== -1) {
                            dlist += '<dt class="col-4 offset-4 mt-2 text-white text-center">' +
                                '<a class="btn btn-primary btn-sm blue-btn-wcag-bgnd-color" style="width:100%" target="_blank" ' +
                                'href="' + siteUrl + '/player?id=' + data.id + '"><i class="fas fa-play"></i> Αναπαραγωγή</a></dt>';
                        }

                        dlist += "</dl>";
                        $("#dl_info").html(dlist);
        }
    });
}

function getResourceByIdInRightSideBar(siteUrl, id) {

    let url =  dashboard.siteUrl + '/apiw/v1/resource/' + id;
    $.ajax({
        type: 'GET',
        url: url,
        dataType: 'json',
        success: function (data) {
            $("#resource-id").html(data.id);
            $("#resource-identity").html(data.storage);
            let header_html = '<i class="fas fa-info-circle"></i> ' + data.title;
            if (data.partNumber != null) {
                header_html += ' /Μέρος ' + data.partNumber;
            }
            header_html += '<br/>' + '<span class="text-muted" style="font-size: 0.6em;font-style: italic; font-weight: normal">Κωδικός Συστήματος: ' + data.id + ' (' + data.storage + ')</span>';
            $("#sidebar-right-title").html(header_html);
            if (data.status.inclMultimedia !== -1) {
                $("#sidebar-right-play").attr("href", siteUrl + '/player?id=' + data.id);
            }
            else {
                $("#sidebar-right-play").attr("disabled", true);
            }

            let dlist = '<dl class="row mt-0 font-weight-normal mt-2" style="font-size: 0.9em">';
            dlist += '<dt class="col-sm-12 mt-2 pt-2 mb-2" style="border-bottom: #cce5ff 1px solid"><h6>Πληροφορίες</h6></dt>';
            if (data.course !== null) {
                $("#sidebar-right-edit").attr("href", siteUrl + '/admin/lecture-editor?id=' + data.id);
                dlist +=
                    '<dt class="col-sm-6">Τύπος</dt><dd class="col-sm-6">Μαγνητοσκοπημένη Διάλεξη</dd>' +
                    '<dt class="col-sm-6">Υπεύθυνος Καθηγητής</dt><dd class="col-sm-6">' + data.supervisor.name + ', '  + data.supervisor.affiliation + '</dd>' +
                    '<dt class="col-sm-6">Τμήμα</dt><dd class="col-sm-6">' + data.supervisor.department.title + '</dd>';
            }
            else {
                $("#sidebar-right-edit").attr("href", siteUrl + '/admin/event-editor?id=' + data.id);
                dlist += '<dt class="col-sm-6">Τύπος</dt><dd class="col-sm-6">Μαγνητοσκοπημένη Εκδήλωση</dd>';
            }
            dlist += '<dt class="col-sm-6">Περιγραφή</dt><dd class="col-sm-6 text-wrap">';
            if ( data.description !== '') {
                dlist += data.description + '</dd>';
            }
            else {
                dlist += 'χωρίς περιγραφή</dd>';
            }
            dlist +=    '<dt class="col-sm-6">Ημ. Εγγραφής</dt><dd class="col-sm-6">' + moment.utc(data.date).format("LL") + '</dd>' +
                        '<dt class="col-sm-6">Ημ. Ενημέρωσης</dt><dd class="col-sm-6">' + moment.utc(data.dateModified).format("LL") + '</dd>';
                        '<dt class="col-sm-6">Ομιλητές</dt><dd class="col-sm-6">';

            dlist += '&nbsp;</dd>';

            if (data.course !== null) {
                dlist += '<dt class="col-sm-12 mt-2 pt-2 mb-2" style="border-bottom: #cce5ff 1px solid"><h6>Μάθημα</h6></dt>';
                dlist += '<dt class="col-sm-6">Τίτλος</dt><dd class="col-sm-6">' + data.course.title + '</dd>' +
                    '<dt class="col-sm-6">Τμήμα</dt><dd class="col-sm-6">' + data.course.department.title + '</dd>' +
                    '<dt class="col-sm-6">Εξάμηνο</dt><dd class="col-sm-6">' + semester[data.course.semester] + '</dd>' +
                    '<dt class="col-sm-6">Κωδικός Γραμματείας</dt><dd class="col-sm-6">';
                if (data.course.scopeId !== data.course.identity) {
                    dlist += data.course.scopeId;
                }
                dlist += '&nbsp;</dd>';
                dlist += '<dt class="col-sm-6">Κωδικοί LMS</dt><dd class="col-sm-6">';
                $.each(data.course.lmsReferences, function (index, el) {
                    dlist += (index>0 ? ', ' : '') + el.lmsId + ' [' + el.lmsCode + ']';
                });
                dlist += '</dd>';
            }
            else if ( data.event != null) {
                dlist += '<dt class="col-sm-12 mt-2 pt-2 mb-2" style="border-bottom: #cce5ff 1px solid"><h6>Εκδήλωση</h6></dt>'
                dlist += '<dt class="col-sm-6">Τίτλος</dt><dd class="col-sm-6">' + data.event.title + '</dd>';
                dlist += '<dt class="col-sm-6">Οργάνωση</dt><dd class="col-sm-6">';
                if (data.event.responsibleUnit != null) {
                    $.each(data.event.responsibleUnit, function (index, ru) {
                        dlist += (index>0 ? ', ' : '');
                        if (ru.structureType === 'DEPARTMENT') { dlist += 'Τμήμα ';}
                        if (ru.structureType === 'SCHOOL') { dlist += 'Σχολή ';}
                        dlist += ru.title;
                    });
                }
                dlist += '</dd>';
                dlist += '<dt class="col-sm-6">Υπεύθυνος</dt><dd class="col-sm-6">';
                if (data.event.responsiblePerson != null) {
                    dlist += data.event.responsiblePerson.name + ', '  + data.event.responsiblePerson.affiliation + '</dd>';
                }
                dlist += '</dd>';
            }
            if (data.status.inclMultimedia !== -1) {
                dlist += '<dt class="col-sm-12 mt-2 pt-2 mb-2" style="border-bottom: #cce5ff 1px solid"><h6>Πολυμέσα</h6></dt>' +
                    '<dt class="col-sm-6">Τύπος/Διαμόρφωση</dt><dd class="col-sm-6">' + data.resourceAccess.type + '/' + data.resourceAccess.format + '</dd>' +
                    '<dt class="col-sm-6">Διάρκεια</dt><dd class="col-sm-6">' + data.resourceAccess.duration + '</dd>' +
                    '<dt class="col-sm-6">Ανάλυση (Αναλογία Απεικόνισης)</dt><dd class="col-sm-6">' + data.resourceAccess.resolution + ' (' + data.resourceAccess.aspectRatio + ')</dd>';
            }
            if (data.status.inclPresentation !== -1) {
                dlist += '<dt class="col-sm-12 mt-2 pt-2 mb-2" style="border-bottom: #cce5ff 1px solid"><h6>Παρουσίαση</h6></dt>' +
                    '<dt class="col-sm-6">Συννημένη Παρουσίαση (Αριθμός Διαφανειών)</dt><dd class="col-sm-6">';
                dlist += ((data.presentation !== null) ? ' ΝΑΙ' : ' ΟΧΙ') +
                    ((data.presentation !== null) ? ' (' + data.presentation.slides.length + ')' : ' (-) ');
                dlist += '</dd>';
            }

            dlist += '<dt class="col-sm-12 mt-2 pt-2 mb-2" style="border-bottom: #cce5ff 1px solid"><h6>Πνευματικά Δικαιώματα</h6></dt>' +
                '<dt class="col-sm-6">Άδεια Χρήσης</dt><dd class="col-sm-6">' + language[data.license] +
                '<a class="ml-2" href="' + language[data.license + '_url'] + '" target="_blank"><i class="fas fa-external-link-alt"></i></a>' +
                '</dd>';

            dlist += "</dl>";
            $("#sidebar-right-main").html(dlist);
        }
    });
}
function toggleSidebar(side) {
    if (side !== "left" && side !== "right") {
        return false;
    }
    var right = $("#sidebar-right"),
        content = $("#content"),
        openSidebarsCount = 0,
        contentClass = "";

    right.toggleClass("collapsed");
    if (!right.hasClass("collapsed")) {
        openSidebarsCount = 0;
    }

    // determine appropriate content class
    if (openSidebarsCount === 0) {
        contentClass = "col-md-9";
    }

    // apply class to content
    content.removeClass("col-md-12 col-md-9 col-md-6").addClass(contentClass);
}
function closeSidebar(side) {
    if (side !== "left" && side !== "right") {
        return false;
    }
    var right = $("#sidebar-right"),
        content = $("#content"),
        openSidebarsCount = 0,
        contentClass = "";

    right.toggleClass("collapsed");
    contentClass = "col-md-12";
    // apply class to content
    content.removeClass("col-md-12 col-md-9 col-md-6").addClass(contentClass);
}

