// 비밀번호 확인
document.addEventListener('DOMContentLoaded', function () {
    const userPwInput = document.getElementById('userPw');
    const checkPwInput = document.getElementById('checkPw');
    const pwCheckResult = document.getElementById('pwCheckResult');

    function checkPasswordsMatch() {
        if (userPwInput.value === '' && checkPwInput.value === '') {
            pwCheckResult.textContent = '';
            pwCheckResult.className = '';
            return;
        }
        if (userPwInput.value === checkPwInput.value) {
            pwCheckResult.textContent = '비밀번호가 일치합니다.';
            pwCheckResult.className = 'success';
        } else {
            pwCheckResult.textContent = '비밀번호가 일치하지 않습니다. 다시 입력해 주세요.';
            pwCheckResult.className = 'error';
        }
    }

    userPwInput.addEventListener('input', checkPasswordsMatch);
    checkPwInput.addEventListener('input', checkPasswordsMatch);
});

// 전화번호 합치기
window.addEventListener("DOMContentLoaded", function () {
    let fullPhone = /*[[${loginUser.phone}]]*/ '';

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
        // document.getElementById("phone").value = fullPhone;
    }

    function updatePhoneValue() {
        const p1 = document.getElementById("phone1").value;
        const p2 = document.getElementById("phone2").value;
        const p3 = document.getElementById("phone3").value;
        document.getElementById("phone").value = p1 + p2 + p3;
    }

    document.getElementById("phone1").addEventListener("input", updatePhoneValue);
    document.getElementById("phone2").addEventListener("input", updatePhoneValue);
    document.getElementById("phone3").addEventListener("input", updatePhoneValue);
});

// 성별, 가구유형 토글 스타일 적용
document.addEventListener("DOMContentLoaded", function () {
    const selectGroups = document.querySelectorAll(".select-buttons");
    selectGroups.forEach(group => {
        const labels = group.querySelectorAll("label");
        labels.forEach(label => {
            label.addEventListener("click", () => {
                labels.forEach(l => l.classList.remove("active"));
                label.classList.add("active");
            });
        });
        const checked = group.querySelector("input:checked");
        if (checked) {
            const matched = group.querySelector(`label[for="${checked.id}"]`);
            if (matched) matched.classList.add("active");
        }
    });
});

// 주소 API 함수
function sample4_execDaumPostcode() {
    new daum.Postcode({
        oncomplete: function(data) {
            // 팝업에서 검색결과 항목을 클릭했을때 실행할 코드를 작성하는 부분.

            // 도로명 주소의 노출 규칙에 따라 주소를 표시한다.
            // 내려오는 변수가 값이 없는 경우엔 공백('')값을 가지므로, 이를 참고하여 분기 한다.
            var roadAddr = data.roadAddress; // 도로명 주소 변수
            var extraRoadAddr = ''; // 참고 항목 변수

            // 법정동명이 있을 경우 추가한다. (법정리는 제외)
            // 법정동의 경우 마지막 문자가 "동/로/가"로 끝난다.
            if(data.bname !== '' && /[동|로|가]$/g.test(data.bname)){
                extraRoadAddr += data.bname;
            }
            // 건물명이 있고, 공동주택일 경우 추가한다.
            if(data.buildingName !== '' && data.apartment === 'Y'){
                extraRoadAddr += (extraRoadAddr !== '' ? ', ' + data.buildingName : data.buildingName);
            }
            // 표시할 참고항목이 있을 경우, 괄호까지 추가한 최종 문자열을 만든다.
            if(extraRoadAddr !== ''){
                extraRoadAddr = ' (' + extraRoadAddr + ')';
            }

            // 우편번호와 주소 정보를 해당 필드에 넣는다.
            document.getElementById("sample4_postcode").value = data.zonecode;
            document.getElementById("sample4_roadAddress").value = roadAddr;
            document.getElementById("sample4_jibunAddress").value = data.jibunAddress;

            // 참고항목 문자열이 있을 경우 해당 필드에 넣는다.
            if(roadAddr !== ''){
                document.getElementById("sample4_extraAddress").value = extraRoadAddr;
            } else {
                document.getElementById("sample4_extraAddress").value = '';
            }

            var guideTextBox = document.getElementById("guide");
            // 사용자가 '선택 안함'을 클릭한 경우, 예상 주소라는 표시를 해준다.
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