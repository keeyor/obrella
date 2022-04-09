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
                    //$("#collapseType").collapse("show");

                }

            }

        //Restore category selected
        let filters_cat_html = '';
        let categoryTypeFilterIds = $("#categoryTypeFilterIds").val();
        if (categoryTypeFilterIds !== "") {
            let array = categoryTypeFilterIds.split(",");
            let $checkbox_elements = $('.category-type-filter');
            $checkbox_elements.each(function () {
                let element_id = $(this).attr("id");
                if (array.includes(element_id)) {
                    $(this).prop("checked", true);
                    filters_cat_html += $('#' + element_id + '_text').text() +
                        '<a  title="' + lang.u1 + '" href="#" class="remove_cat_filter mx-2" data-target="' + element_id + '">' +
                        '<i style="color: red" class="fas fa-minus-circle"></i></a>';
                }
            });
            if ($checkbox_elements.length > 0) {
                $("#filters-header").show();
                $('.remove_all_filters').show();
                filters_cat_html = "<b><u>" + lang.u5 + ":</u></b> " + filters_cat_html;
                let $catTextFilters = $("#categoryTextFilters");
                $catTextFilters.html(filters_cat_html);
               // $("#collapseCat").collapse("show");

            }
        }
    }

    function InitEvents() {
        onImageErrorEvent();
        onAllFiltersRemove();
        onTypeFilterRemove();
        onCategoryFilterRemove();
        onSortFilterChange();
        onSortDirectionChange();
        onEventTypeFilterChange();
        onCategoryTypeFilterChange();
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
            $("#eventTypeFilterIds").val("");
            let $cat_elements = $('.category-type-filter');
            $cat_elements.each(function() {
                $(this).prop("checked",false);
            });
            $("#categoryTypeFilterIds").val("");
            $("#search-form").submit();
        });
    }

    function onTypeFilterRemove() {
        $(".remove_type_filter").on('click',function (){
            let id = $(this).data("target");
            let el = $('#' + id);
            el.prop("checked", false);
            triggerEventTypeFilterChange(el);
        })
    }
    function onCategoryFilterRemove() {
        $(".remove_cat_filter").on('click',function (){
            let id = $(this).data("target");
            let el = $('#' + id);
            el.prop("checked", false);
            triggerCategoryTypeFilterChange(el);
        })
    }

    function onEventTypeFilterChange() {
        $(".event-type-filter").on('change', function(){
            triggerEventTypeFilterChange();
        });
    }


    function onCategoryTypeFilterChange() {
        $(".category-type-filter").on('change', function(){
            triggerCategoryTypeFilterChange();
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
    function triggerCategoryTypeFilterChange() {
        let $checkbox_elements = $('.category-type-filter');
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
        $("#categoryTypeFilterIds").val(checked_ids);
        $("#search-form").submit();
    }




})();
