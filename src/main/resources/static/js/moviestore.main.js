$(document).ready(function () {

    $("#added-to-cart-info").toggleClass("hidden"); //making sure its not shown by default

    $('button.close').on('click', function () {
        $("#added-to-cart-info").toggleClass("hidden");
    })

    $(".add-to-cart").click(function (event) {
        event.preventDefault();
        addToCart(this.getAttribute('data'));
    });

    function addToCart(movieId) {
        var qty = document.getElementById(movieId + '--qty').value;
        console.log("Adding " + qty + " to movie " + movieId + " to cart");
        $.ajax({
            type: "GET",
            url: '/cart/add?movieId=' + movieId + "&quantity=" + qty,
            timeout: 600000,
            success: function (data) {
                console.log("Success : ", data);
                $("#cart-items-indicator").text(data);
                $("#added-to-cart-info").toggleClass("hidden");
            },
            error: function (e) {
                console.log("ERROR : ", e);
            }
        });
    }

});