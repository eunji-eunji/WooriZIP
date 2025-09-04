$(document).ready(function () {
    let appliedCouponAmount = 0;
    let isCouponValid = true;

    $("#couponSelect").on("change", function () {
        updateSummary();
    });

    updateSummary();

    function updateSummary() {
        let totalPriceText = $("#finalTotalPrice").text().trim();
        let totalPrice = parseInt(totalPriceText.replace(/,/g, "")) || 0;
        let deliveryFee = totalPrice >= 50000 ? 0 : 3000;

        const selectedCoupon = $("#couponSelect").find("option:selected");
        const selectedValue = $("#couponSelect").val();

        if (selectedValue === "" || !selectedValue) {
            appliedCouponAmount = 0;
            isCouponValid = false;
            $("#appliedCoupon").text("0원");
        } else {
            const type = selectedCoupon.data("type");
            const amount = parseInt(selectedCoupon.data("amount")) || 0;
            const percent = parseInt(selectedCoupon.data("percent")) || 0;
            const minOrderPrice = parseInt(selectedCoupon.data("min-order-price")) || 0;

            if (type && minOrderPrice > 0 && totalPrice < minOrderPrice) {
                appliedCouponAmount = 0;
                isCouponValid = false;
                $("#appliedCoupon").text("0원 (최소주문금액 미달)");
            } else if (type === "AMOUNT") {
                appliedCouponAmount = amount;
                isCouponValid = true;
                $("#appliedCoupon").text(amount.toLocaleString() + "원");
            } else if (type === "PERCENT") {
                appliedCouponAmount = Math.floor(totalPrice * percent / 100);
                isCouponValid = true;
                $("#appliedCoupon").text(appliedCouponAmount.toLocaleString() + "원 (" + percent + "% 할인)");
            } else {
                appliedCouponAmount = 0;
                isCouponValid = false;
                $("#appliedCoupon").text("0원");
            }
        }

        const finalAmount = totalPrice + deliveryFee - appliedCouponAmount;

        $("#deliveryFee").text(deliveryFee.toLocaleString());
        $("#deliveryFeeDisplay").text(deliveryFee.toLocaleString());
        $("#totalWithDelivery").text((totalPrice + deliveryFee).toLocaleString());
        $("#finalPaymentAmount").text(finalAmount.toLocaleString());
    }

    $('#submit-orderDto').on('click', function (e) {
        e.preventDefault();

        const orderId = $('input[name="orderId"]').val();
        const rawCouponId = $('#couponSelect').val();
        const couponId = (!isCouponValid || rawCouponId === "") ? null : rawCouponId;

        const requestData = {
            orderId: orderId,
            couponId: couponId
        };

        $.ajax({
            url: '/pay/checkout',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(requestData),
            success: function (response) {
                if (response.success) {
                    window.location.href = response.redirectUrl;
                } else {
                    alert('오류: ' + response.message);
                }
            },
            error: function (xhr) {
                let response = JSON.parse(xhr.responseText);
                alert('오류: ' + response.message);
            }
        });
    });

    // 페이지 떠날 때 이벤트 처리
    window.addEventListener('beforeunload', function (e) {
        if (!isPaymentSuccess) { // 결제 한것이 아니면 cancel 로 이동
            fetch('/cancel', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({orderId: order.orderID})
            }).then(response => {
                if (!response.ok) {
                    console.error('Failed to cancel order');
                }
            }).catch(error => {
                console.error('Error canceling order:', error);
            });

            // 사용자에게 경고 메시지 표시
            e.preventDefault();
            e.returnValue = '주문을 취소합니다.';
        }
    });
});

// 전화번호 합치기
window.addEventListener("DOMContentLoaded", function () {
    let fullPhone = /*[[${orderDto.phone}]]*/ '';

    // null, undefined, 'null' 문자열 등 예외 처리
    if (fullPhone == null || fullPhone === 'null' || fullPhone.trim() === '') {
        fullPhone = '';
    } else {
        fullPhone = fullPhone.replace(/[^0-9]/g, ''); // 숫자만 추출
    }

    if (fullPhone && fullPhone.length === 11) {
        document.getElementById("phone1").value = fullPhone.substring(0, 3);
        document.getElementById("phone2").value = fullPhone.substring(3, 7);
        document.getElementById("phone3").value = fullPhone.substring(7);
    }

    function updatePhoneValue() {
        const phone1 = document.getElementById("phone1").value;
        const phone2 = document.getElementById("phone2").value;
        const phone3 = document.getElementById("phone3").value;
        document.getElementById("phone").value = phone1 + phone2 + phone3;
    }
    function validatePhoneNumber() {
        updatePhoneValue();
        const phone = document.getElementById('phone').value;
        if (phone.length !== 11) {
            alert('전화번호를 다시 한 번 확인해 주세요.');
            document.getElementById('phone1').focus();
            return false;
        }
        return true;
    }

    document.getElementById("phone1").addEventListener("input", updatePhoneValue);
    document.getElementById("phone2").addEventListener("input", updatePhoneValue);
    document.getElementById("phone3").addEventListener("input", updatePhoneValue);
});

//이메일 입력
document.addEventListener("DOMContentLoaded", function () {
    const domainInput = document.getElementById("email-domain");
    const domainSelect = document.getElementById("domain-select");

    // select 선택 시 → input 값을 해당 도메인으로 변경
    domainSelect.addEventListener("change", function () {
        if (this.value !== "") {
            domainInput.value = this.value;
        } else {
            domainInput.value = "";
        }
    });

    // input 수동 입력 시 select 값을 "직접입력"으로 되돌림
    domainInput.addEventListener("input", function () {
    domainSelect.value = "";
    });
});

// 주소 API
function sample4_execDaumPostcode() {
    new daum.Postcode({
        oncomplete: function(data) {
            var roadAddr = data.roadAddress;
            var extraRoadAddr = '';

            if(data.bname !== '' && /[동|로|가]$/g.test(data.bname)){
                extraRoadAddr += data.bname;
            }
            if(data.buildingName !== '' && data.apartment === 'Y'){
                extraRoadAddr += (extraRoadAddr !== '' ? ', ' + data.buildingName : data.buildingName);
            }
            if(extraRoadAddr !== ''){
                extraRoadAddr = ' (' + extraRoadAddr + ')';
            }

            document.getElementById("sample4_postcode").value = data.zonecode;
            document.getElementById("sample4_roadAddress").value = roadAddr;
            document.getElementById("sample4_jibunAddress").value = data.jibunAddress;

            if(roadAddr !== ''){
                document.getElementById("sample4_extraAddress").value = extraRoadAddr;
            } else {
                document.getElementById("sample4_extraAddress").value = '';
            }

            var guideTextBox = document.getElementById("guide");
            if(data.autoRoadAddress) {
                var expRoadAddr = data.autoRoadAddress + extraRoadAddr;
                guideTextBox.innerHTML = '(예상 도로명 주소 : ' + expRoadAddr + ')';
                guideTextBox.style.display = 'block';
            } else if(data.autoJibunAddress) {
                var expJibunAddr = data.autoJibunAddress;
                guideTextBox.innerHTML = '(예상 지번 주소 : ' + expJibunAddr + ')';
                guideTextBox.style.display = 'block';
            } else {
                guideTextBox.innerHTML = '';
                guideTextBox.style.display = 'none';
            }
        }
    }).open();
}