
    $(document).ready(function () {

        //load specific tab when returning from edit
        let url = document.location.toString();
        if (url.match('#')) {
            $('.nav-pills a[href="#' + url.split('#')[1] + '"]').tab('show');
        }
        //Change hash for page-reload
        $('.nav-pills a[href="#' + url.split('#')[1] + '"]').on('show.coreui.tab', function (e) {
            window.location.hash = e.target.hash;
        });

    })

