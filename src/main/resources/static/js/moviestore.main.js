$(document).ready(function () {

    $(".add-to-cart").click(function (event) {
        event.preventDefault()
        addToCart(this.getAttribute('data'));
    });

    function addToCart(movieId) {
        var qty = document.getElementById(movieId + '--qty').value;
        console.log("Adding " + qty + " to movie " + movieId + " in cart");
        $.ajax({
            type: "GET",
            url: '/cart/add?movieId=' + movieId + "&quantity=" + qty,
            timeout: 600000,
            success: function (data) {
                console.log("Success : ", data);
                $("#cart-items-indicator").text(data);
            },
            error: function (e) {
                console.log("ERROR : ", e);
            }
        });
    }

});