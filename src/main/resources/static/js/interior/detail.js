document.getElementById('likeBtn').addEventListener('click', function () {
    const postId = this.getAttribute('data-post-id');

    fetch(`/interior/${postId}/like`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => {
            if (response.status === 401 || response.status === 403) {
                location.href = '/user/login';
                return;
            }
                return response.text();
        })
        .then(status => {
            const count = document.getElementById('likeCount');
            if (status === 'liked') {
                count.textContent = parseInt(count.textContent) + 1;
            } else if (status === 'unliked') {
                count.textContent = parseInt(count.textContent) - 1;
            }
        })
        .catch(err => {
            console.error(err);
            alert('좋아요 처리 중 오류 발생');
        });
});