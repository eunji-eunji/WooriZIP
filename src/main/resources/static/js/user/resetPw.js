document.addEventListener('DOMContentLoaded', function () {
    const pw1 = document.getElementById('newPw');
    const pw2 = document.getElementById('confirmPw');
    const result = document.getElementById('pwCheckResult');

    function checkMatch() {
        if (pw1.value === '' && pw2.value === '') {
            result.textContent = '';
            result.className = '';
            return;
        }

        if (pw1.value === pw2.value) {
            result.textContent = '비밀번호가 일치합니다.';
            result.className = 'success';
        } else {
            result.textContent = '비밀번호가 일치하지 않습니다.';
            result.className = 'error';
        }
    }

    pw1.addEventListener('input', checkMatch);
    pw2.addEventListener('input', checkMatch);
});