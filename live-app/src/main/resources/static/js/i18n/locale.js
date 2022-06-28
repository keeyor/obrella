(function () {
    'use strict';

    window.locale = window.locale || {};

    locale.broker = $({});

    $(document).ready(function () {

        $("#a-el").on("click", function(e) {
            $("#form-gr").submit();
            return false;
        });
        $("#a-en").on("click", function(e) {
            $("#form-en").submit();
            return false;
        })

    }); //document ready end



})();