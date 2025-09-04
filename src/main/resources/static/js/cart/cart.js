$(document).ready(function () {
    let appliedCouponAmount = 0; // 전역 할인 금액 저장

    // 수량 변경 시 서버에 반영하고 요약만 갱신
    $('.item-count').on('change', function () {
        let $input = $(this);
        let newCount = $input.val();
        let cartId = $input.data('cart-id');

        $.ajax({
            url: '/cart/update',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({cartItemId: cartId, count: newCount}),
            success: function (response) {
                if (response.success) {
                    updateSummary(); // 페이지 새로고침 없이 요약만 업데이트
                } else {
                    alert('오류: ' + response.message);
                }
            },
            error: function (xhr) {
                let response = JSON.parse(xhr.responseText);
                alert('오류: ' + response.message);
                location.reload();
            }
        });
    });

    // 전체 주문 클릭 시 전체 선택 후 전송
    $(".allOrder").on("click", function (event) {
        event.preventDefault();
        $(".cart-item-checkbox").prop("checked", true);
        updateSummary();
        const selectedItems = document.querySelectorAll(".cart-item-checkbox:checked");
for (let item of selectedItems) {
    const stock = parseInt(item.dataset.stock);
    const cartItemId = item.value;

    // 해당 cart item과 같은 ID를 가진 수량 input을 찾아야 함
    const countInput = document.querySelector(`.item-count[data-cart-id='${cartItemId}']`);
    const count = parseInt(countInput.value);

    if (stock < count) {
        alert("재고가 부족한 상품이 있습니다.");
        return;
    }
}
        sendOrderRequest();
    });

    // 선택 주문 클릭 시 선택된 항목만 전송
    $(".selectOrder").on("click", function (event) {
        event.preventDefault();
        const selectedItems = document.querySelectorAll(".cart-item-checkbox:checked");
for (let item of selectedItems) {
    const stock = parseInt(item.dataset.stock);
    const cartItemId = item.value;

    // 해당 cart item과 같은 ID를 가진 수량 input을 찾아야 함
    const countInput = document.querySelector(`.item-count[data-cart-id='${cartItemId}']`);
    const count = parseInt(countInput.value);

    if (stock < count) {
        alert("재고가 부족한 상품이 있습니다.");
        return;
    }
}
        sendOrderRequest();
    });

    // 전체 선택/해제 체크박스
    $("#selectAll").on("change", function () {
        $(".cart-item-checkbox").prop("checked", $(this).prop("checked"));
        updateSummary();
    });

    // 수량 or 체크박스 변경 시 요약 갱신
    $(".cart-item-checkbox, .item-count").on("change", function () {
        updateSummary();
    });

    // 쿠폰 선택 시 할인 금액 계산 및 반영
    $("#couponSelect").on("change", function () {
        updateSummary(); // 쿠폰 선택도 요약 재계산에 포함
    });

    // 페이지 진입 시 최초 1회 결제 요약 계산
    updateSummary();

    // 결제 요약 계산 함수
    function updateSummary() {
        let totalPrice = 0;
        let deliveryFee = 3000;

        $("tr").has("input.cart-item-checkbox").each(function () {
            const priceText = $(this).find("td").eq(4).text().replace(/[^0-9]/g, "");
            const count = parseInt($(this).find("input[name='count']").val());
            const price = parseInt(priceText);

            if (!isNaN(price) && !isNaN(count)) {
                const subtotal = price * count;

                // 합계 셀 업데이트 (index 6)
                $(this).find("td").eq(6).text(subtotal.toLocaleString() + "원");

                if ($(this).find(".cart-item-checkbox").prop("checked")) {
                    totalPrice += subtotal;
                }
            }
        });

        // 쿠폰 할인 계산
                const selectedCoupon = $("#couponSelect").find("option:selected");
                const type = selectedCoupon.data("type");
                const amount = parseInt(selectedCoupon.data("amount")) || 0;
                const percent = parseInt(selectedCoupon.data("percent")) || 0;
                const minOrderPrice = parseInt(selectedCoupon.data("min-order-price")) || 0;

                if (type && minOrderPrice > 0 && totalPrice < minOrderPrice) {
                    appliedCouponAmount = 0;
                    $("#appliedCoupon").text("0원 (최소주문금액 미달)");
                } else if (type === "AMOUNT") {
                    appliedCouponAmount = amount;
                    $("#appliedCoupon").text(amount.toLocaleString() + "원");
                        } else if (type === "PERCENT") {
            appliedCouponAmount = Math.floor(totalPrice * percent / 100);
            $("#appliedCoupon").text(appliedCouponAmount.toLocaleString() + "원 (" + percent + "% 할인)");
                } else {
                    appliedCouponAmount = 0;
                    $("#appliedCoupon").text("0원");
                }

                // 5만원 미만일 때만 배송비 적용
                const appliedDeliveryFee = totalPrice >= 50000 ? 0 : deliveryFee;
                const finalAmount = totalPrice + appliedDeliveryFee - appliedCouponAmount;

                $("#finalTotalPrice").text(totalPrice.toLocaleString() + "원");
                $("#deliveryFee").text(appliedDeliveryFee.toLocaleString() + "원");
                $("#finalPaymentAmount").text(finalAmount.toLocaleString() + "원");
            }

    // 선택된 항목 기반 주문 전송 함수
    function sendOrderRequest() {
        let selectedItems = $(".cart-item-checkbox:checked").map(function () {
            return $(this).val();
        }).get();

        if (selectedItems.length === 0) {
            alert("주문할 상품을 선택하세요.");
            return;
        }

        let form = $('<form>', {
            'method': 'POST',
            'action': '/order'
        });

        selectedItems.forEach(function (item) {
            form.append($('<input>', {
                'type': 'hidden',
                'name': 'cartItemIds',
                'value': item
            }));
        });

        form.append($('<input>', {
            'type': 'hidden',
            'name': 'type',
            'value': 'cart'
        }));

        // 선택된 쿠폰 정보 추가
        const selectedCouponId = $("#couponSelect").val();
        if (selectedCouponId) {
            form.append($('<input>', {
                'type': 'hidden',
                'name': 'couponId',
                'value': selectedCouponId
            }));
        }

        $('body').append(form);
        form.submit();
    }

    // 선택 항목 삭제
    $('.deleteSelected').click(function () {
        const selectedItems = [];

        $('.cart-item-checkbox:checked').each(function () {
            selectedItems.push($(this).val());
        });

        if (selectedItems.length === 0) {
            alert('삭제할 항목을 선택해주세요.');
            return;
        }

        $.ajax({
            type: 'POST',
            url: '/cart/deleteSelected',
            contentType: 'application/json',
            data: JSON.stringify(selectedItems),
            success: function () {
                location.reload();
            },
            error: function () {
                alert('선택 삭제에 실패했습니다.');
            }
        });
    });

    // 전체 항목 삭제
    $('.deleteAll').click(function () {
        if (!confirm('장바구니의 모든 상품을 삭제하시겠습니까?')) return;

        $.ajax({
            type: 'POST',
            url: '/cart/clear',
            success: function () {
                location.reload();
            },
            error: function () {
                alert('전체 삭제에 실패했습니다.');
            }
        });
    });
});
