$(document).ready(function () {

    $("#invalid-qty").hide();
    $("#added-to-cart-info").hide(); //making sure its not shown by default

    $('button.close').on('click', function () {
        var parentId = $(this).parent().get(0).getAttribute("id");
        console.log("Parent " + parentId);
        $('#' + parentId).hide();
    })


    $(".add-to-cart").click(function (event) {
        event.preventDefault();
        var movieId = this.getAttribute('data');
        var qty = $('#' + movieId + '--qty').val();
        if (qty > 0) {
            addToCart(movieId, qty);
        } else {
            $("#added-to-cart-info").hide();
            $("#invalid-qty").show();
            $('#' + movieId + '--qty').focus();
        }
    });

    function addToCart(movieId, qty) {
        console.log("Adding " + qty + " to movie " + movieId + " to cart");
        $.ajax({
            type: "GET",
            url: '/cart/add?movieId=' + movieId + "&quantity=" + qty,
            timeout: 600000,
            success: function (data) {
                console.log("Success : ", data);
                $("#cart-items-indicator").text(data);
                $("#invalid-qty").hide();
                $("#added-to-cart-info").show();
            },
            error: function (e) {
                console.log("ERROR : ", e);
            }
        });

    }

});