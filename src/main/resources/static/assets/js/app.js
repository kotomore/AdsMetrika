! function(n) {
    "use strict";

    $('#datepicker1').click(function(){n("#status").fadeIn(), n("#preloader").delay(350).fadeIn("slow")})
    $('#datepicker2').click(function(){n("#status").fadeIn(), n("#preloader").delay(350).fadeIn("slow")})
    $('#datepicker3').click(function(){n("#status").fadeIn(), n("#preloader").delay(350).fadeIn("slow")})
    $('#datepicker4').click(function(){n("#status").fadeIn(), n("#preloader").delay(350).fadeIn("slow")})
    $('#datepicker5').click(function(){n("#status").fadeIn(), n("#preloader").delay(350).fadeIn("slow")})
    $('#datepicker6').click(function(){n("#status").fadeIn(), n("#preloader").delay(350).fadeIn("slow")})
    $('#datepicker7').click(function(){n("#status").fadeIn(), n("#preloader").delay(350).fadeIn("slow")})
    $('#datepicker8').click(function(){n("#status").fadeIn(), n("#preloader").delay(350).fadeIn("slow")})



    function s() {
        for (var e = document.getElementById("topnav-menu-content").getElementsByTagName("a"), t = 0, n = e.length; t < n; t++) "nav-item dropdown active" === e[t].parentElement.getAttribute("class") && (e[t].parentElement.classList.remove("active"), e[t].nextElementSibling.classList.remove("show"))
    }

    function e() {
        document.webkitIsFullScreen || document.mozFullScreen || document.msFullscreenElement || (console.log("pressed"), n("body").removeClass("fullscreen-enable"))
    }
    var a;
    n("#side-menu").metisMenu(), n("#vertical-menu-btn").on("click", function(e) {
        e.preventDefault(), n("body").toggleClass("sidebar-enable"), 992 <= n(window).width() ? n("body").toggleClass("vertical-collpsed") : n("body").removeClass("vertical-collpsed")
    }), n("body,html").click(function(e) {
        var t = n("#vertical-menu-btn");
        t.is(e.target) || 0 !== t.has(e.target).length || e.target.closest("div.vertical-menu") || n("body").removeClass("sidebar-enable")
    }), n("#sidebar-menu a").each(function() {
        var e = window.location.href.split(/[?#]/)[0];
        this.href == e && (n(this).addClass("active"), n(this).parent().addClass("mm-active"), n(this).parent().parent().addClass("mm-show"), n(this).parent().parent().prev().addClass("mm-active"), n(this).parent().parent().parent().addClass("mm-active"), n(this).parent().parent().parent().parent().addClass("mm-show"), n(this).parent().parent().parent().parent().parent().addClass("mm-active"))
    }), n(".navbar-nav a").each(function() {
        var e = window.location.href.split(/[?#]/)[0];
        this.href == e && (n(this).addClass("active"), n(this).parent().addClass("active"), n(this).parent().parent().addClass("active"), n(this).parent().parent().parent().addClass("active"), n(this).parent().parent().parent().parent().addClass("active"), n(this).parent().parent().parent().parent().parent().addClass("active"))
    }), n(document).ready(function() {
        var e;
        0 < n("#sidebar-menu").length && 0 < n("#sidebar-menu .mm-active .active").length && (300 < (e = n("#sidebar-menu .mm-active .active").offset().top) && (e -= 300, n(".vertical-menu .simplebar-content-wrapper").animate({
            scrollTop: e
        }, "slow")))
    }), n('[data-toggle="fullscreen"]').on("click", function(e) {
        e.preventDefault(), n("body").toggleClass("fullscreen-enable"), document.fullscreenElement || document.mozFullScreenElement || document.webkitFullscreenElement ? document.cancelFullScreen ? document.cancelFullScreen() : document.mozCancelFullScreen ? document.mozCancelFullScreen() : document.webkitCancelFullScreen && document.webkitCancelFullScreen() : document.documentElement.requestFullscreen ? document.documentElement.requestFullscreen() : document.documentElement.mozRequestFullScreen ? document.documentElement.mozRequestFullScreen() : document.documentElement.webkitRequestFullscreen && document.documentElement.webkitRequestFullscreen(Element.ALLOW_KEYBOARD_INPUT)
    }), document.addEventListener("fullscreenchange", e), document.addEventListener("webkitfullscreenchange", e), document.addEventListener("mozfullscreenchange", e), n(".right-bar-toggle").on("click", function(e) {
        n("body").toggleClass("right-bar-enabled")
    }), n(document).on("click", "body", function(e) {
        0 < n(e.target).closest(".right-bar-toggle, .right-bar").length || n("body").removeClass("right-bar-enabled")
    }),
        function() {
            if (document.getElementById("topnav-menu-content")) {
                for (var e = document.getElementById("topnav-menu-content").getElementsByTagName("a"), t = 0, n = e.length; t < n; t++) e[t].onclick = function(e) {
                    "#" === e.target.getAttribute("href") && (e.target.parentElement.classList.toggle("active"), e.target.nextElementSibling.classList.toggle("show"))
                };
                window.addEventListener("resize", s)
            }
        }(), [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]')).map(function(e) {
        return new bootstrap.Tooltip(e)
    }), [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]')).map(function(e) {
        return new bootstrap.Popover(e)
    }), n(window).on("DOMContentLoaded", function() {
        n("#status").fadeOut(), n("#preloader").delay(350).fadeOut("slow")
    }), Waves.init()
}(jQuery);
