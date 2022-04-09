(function () {
    'use strict';

    window.dashboard = window.dashboard || {};
    dashboard.broker = $({});

    dashboard.siteUrl   = "";
    dashboard.app_path  = "";
    dashboard.page = "";


    dashboard.init = function () {

        dashboard.siteUrl = dashboard.broker.getRootSitePath();
        dashboard.page = $("#page").val();

        InitControls();
        InitEvents();
    };

    $(document).ready(function () {
        dashboard.init();
    });

    dashboard.broker.getRootSitePath = function () {

        let _location = document.location.toString();
        let applicationNameIndex = _location.indexOf('/', _location.indexOf('://') + 3);
        let applicationName = _location.substring(0, applicationNameIndex) + '/';
        let webFolderIndex = _location.indexOf('/', _location.indexOf(applicationName) + applicationName.length);

        return _location.substring(0, webFolderIndex);
    };

    function InitControls() {
        RestoreCheckedItemsFromFilters();
    }

    function RestoreCheckedItemsFromFilters() {

//Restore department selected
        let departmentFilterId = $("#departmentFilterId").val();
        let filters_dep_html='';
        if (departmentFilterId !== "") {
            let $checkbox_elements = $('.department-type-filter');
            $checkbox_elements.each(function () {
                let element_id = $(this).attr("id");
                if (departmentFilterId === element_id) {
                    $(this).prop("checked", true);
                    filters_dep_html += $('#' + element_id + '_text').text() +
                        '<a  title="' + lang.u1 + '" href="#" class="remove_dep_filter mx-2" data-target="' + element_id + '">' +
                        '<i style="color: red" class="fas fa-minus-circle"></i></a>';
                }
            });
            if ($checkbox_elements.length > 0) {
                $("#filters-header").show();
                $('.remove_all_filters').show();
                filters_dep_html = "<b><u>" + lang.u2 + ":</u></b> " + filters_dep_html;
                let $departmentTextFilters = $("#depTextFilters");
                $departmentTextFilters.html(filters_dep_html);
                //$("#collapseUnit").collapse("show");
            }
        }
        //Restore school selected
        let schoolFilterId = $("#schoolFilterId").val();
        let filters_school_html='';
        if (schoolFilterId !== "") {
            let $checkbox_elements = $('.school-type-filter');
            $checkbox_elements.each(function () {
                let element_id = $(this).attr("id");
                if (schoolFilterId === element_id) {
                    filters_school_html += $('#' + element_id + '_text').text() +
                        '<a   title="' + lang.u1 + '" href="#" class="remove_school_filter mx-2" data-target="' + element_id + '">' +
                        '<i style="color: red" class="fas fa-minus-circle"></i></a>';
                    $(this).prop("checked", true);
                }
            });
            if ($checkbox_elements.length > 0) {
                $("#filters-header").show();
                $('.remove_all_filters').show();
                filters_school_html = "<b><u>" + lang.u3 + ":</u></b> " + filters_school_html;
                let $schoolTextFilters = $("#schoolTextFilters");
                $schoolTextFilters.html(filters_school_html);
              //  $("#collapseUnit").collapse("show");
            }
        }
        let filters_type_html='';
        //Restore event type(s) selected
        let eventTypeFilterIds = $("#eventTypeFilterIds").val();
        if (eventTypeFilterIds !== "") {
            let array = eventTypeFilterIds.split(",");
            let $checkbox_elements = $('.event-type-filter');
            $checkbox_elements.each(function () {
                let element_id = $(this).attr("id");
                if (array.includes(element_id)) {
                    $(this).prop("checked", true);
                    filters_type_html += $('#' + element_id + '_text').text() +
                        '<a  title="' + lang.u1 + '" href="#" class="remove_type_filter mx-2" data-target="' + element_id + '">' +
                        '<i style="color: red" class="fas fa-minus-circle"></i></a>';
                }
            });
            if ($checkbox_elements.length > 0) {
                $("#filters-header").show();
                $('.remove_all_filters').show();
                filters_type_html = "<b><u>" + lang.u4 + "</u></b> " + filters_type_html;
                let $eventTextFilters = $("#eventTextFilters");
                $eventTextFilters.html(filters_type_html);
             //   $("#collapseType").collapse("show");

            }

        }

    }

    function InitEvents() {
        onImageErrorEvent();
        onAllFiltersRemove();
        onDepartmentFilterRemove();
        onSchoolFilterRemove();
        onEventTypeFilterRemove();
        onSortFilterChange();
        onSortDirectionChange();
        onEventTypeFilterChange();
        onDepartmentFilterChange();
        onSchoolFilterChange();
    }

    function onImageErrorEvent() {
         $('img').each(function() {
            if ( !this.complete
                ||   typeof this.naturalWidth == "undefined"
                ||   this.naturalWidth === 0 ) {
                // image was broken, replace with your new image
                this.src = dashboard.siteUrl + '/public/images/default/default_event_thumb.jpg';
            }
        });
    }

    function onSortFilterChange() {
        $('.sort_select').on('click', function () {
            let id = $(this).data("value");
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.set("sort", id);
            queryParams.delete("skip");
            window.location.href = dashboard.page + "?" + queryParams;
        });
    }

    function onSortDirectionChange() {
        $('.direction_select').on('click', function () {
            let id = $(this).data("value");
            let queryParams = new URLSearchParams(window.location.search);
            queryParams.set("direction", id);
            queryParams.delete("skip");
            window.location.href = dashboard.page + "?" + queryParams;
        });
    }

    function onAllFiltersRemove() {
        $(".remove_all_filters").on('click',function (){
            let $type_elements = $('.event-type-filter');
            $type_elements.each(function() {
                    $(this).prop("checked",false);
            });
            $("#departmentFilterId").val("");
            let $school_elements = $('.school-type-filter');
            $school_elements.each(function() {
                $(this).prop("checked",false);
            });
            $("#schoolFilterId").val("");
            $("#eventTypeFilterIds").val("");
            $("#search-form").submit();
        });
    }

    function onEventTypeFilterRemove() {
        $(".remove_type_filter").on('click',function (){
            let id = $(this).data("target");
            let el = $('#' + id);
            el.prop("checked", false);
            triggerEventTypeFilterChange(el);
        })
    }
    function onDepartmentFilterRemove() {
        $(".remove_dep_filter").on('click',function (){
            let id = $(this).data("target");
            let el = $('#' + id);
            el.prop("checked", false);
            triggerDepartmentTypeFilterChange(el);
        })
    }
    function onSchoolFilterRemove() {
        $(".remove_school_filter").on('click',function (){
            let id = $(this).data("target");
            let el = $('#' + id);
            el.prop("checked", false);
            triggerSchoolTypeFilterChange(el);
        })
    }



    function onEventTypeFilterChange() {
        $(".event-type-filter").on('change', function(){
            triggerEventTypeFilterChange();
        });
    }


    function triggerEventTypeFilterChange() {
        let $checkbox_elements = $('.event-type-filter');
        let checked_ids = "";
        $checkbox_elements.each(function() {
            if ($(this).prop('checked')) {
                if (checked_ids === "") {
                    checked_ids = checked_ids + $(this).attr("id");
                }
                else {
                    checked_ids = checked_ids + "," + $(this).attr("id");
                }
            }
        });
        $("#eventTypeFilterIds").val(checked_ids);
        $("#search-form").submit();
    }
    function onDepartmentFilterChange() {
        $(".department-type-filter").on('change', function(){
            let el = $(this);
            triggerDepartmentTypeFilterChange(el);
        });
    }

    function triggerDepartmentTypeFilterChange(el) {
        //uncheck all others
        let $checkbox_department_elements = $('.department-type-filter');
        let checked_did = el.attr("id");
        $checkbox_department_elements.each(function() {
            let elem_id = $(this).attr("id");
            if (elem_id !== checked_did) {
                $(this).prop("checked",false);
            }
        });
        // Uncheck all other school filters
        let $checkbox_school_elements = $('.school-type-filter');
        $checkbox_school_elements.each(function() {
            $(this).prop("checked",false);
        });
        //check if element that was checked -> unchecked
        if (el.prop("checked")) {
            $("#departmentFilterId").val(checked_did);
        }
        else {
            $("#departmentFilterId").val("");
        }
        // set School Filter to none
        $("#schoolFilterId").val("");
        //Submit form
        $("#search-form").submit();
    }

    function onSchoolFilterChange() {

        $(".school-type-filter").on('change', function() {
            let el = $(this);
            triggerSchoolTypeFilterChange(el)
        });
    }
    function triggerSchoolTypeFilterChange(el) {
        //uncheck all others
        let $checkbox_school_elements = $('.school-type-filter');
        let checked_sid = el.attr("id");
        $checkbox_school_elements.each(function() {
            let elem_id = $(this).attr("id");
            if (elem_id !== checked_sid) {
                $(this).prop("checked",false);
            }
        });
        // Uncheck all other school filters
        let $checkbox_department_elements = $('.department-type-filter');
        $checkbox_department_elements.each(function() {
            $(this).prop("checked",false);
        });
        //check if element that was checked -> unchecked
        if (el.prop("checked")) {
            $("#schoolFilterId").val(checked_sid);
        }
        else {
            $("#schoolFilterId").val("");
        }
        // set Department Filter to none
        $("#departmentFilterId").val("");
        //Submit form
        $("#search-form").submit();
    }



})();
