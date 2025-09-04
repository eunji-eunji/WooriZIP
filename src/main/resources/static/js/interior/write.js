let selectedFiles = [];

const input = document.getElementById('imageInput');
const preview = document.getElementById('previewContainer');

input.addEventListener('change', function () {
    const newFiles = Array.from(this.files);

    if (selectedFiles.length + newFiles.length > 8) {
        alert("이미지는 최대 8장까지 첨부할 수 있습니다.");
        return;
    }

    newFiles.forEach(file => {
        selectedFiles.push(file);

            const reader = new FileReader();
            reader.onload = e => {
                const box = document.createElement('div');
                box.className = 'img-box';

                const img = document.createElement('img');
                img.src = e.target.result;

                const removeBtn = document.createElement('button');
                removeBtn.className = 'remove-btn';
                removeBtn.innerHTML = '✕';
                removeBtn.onclick = function() {
                    const index = selectedFiles.indexOf(file);
                    if (index > -1) {
                        selectedFiles.splice(index, 1);
                    }
                    box.remove();
                };

                box.appendChild(img);
                box.appendChild(removeBtn);
                preview.appendChild(box);
            };
        reader.readAsDataURL(file);
    });

  this.value = '';
});

document.getElementById('submitBtn').addEventListener('click', function () {
    const form = document.getElementById('writeForm');
    const formData = new FormData(form);

    selectedFiles.forEach(file => {
        formData.append("files", file);
    });

    fetch('/interior/write', {
        method: 'POST',
        body: formData
    }).then(res => res.text())
    .then(result => {
        if (result === 'success') {
            alert("등록 완료!");
            location.href = '/interior';
        } else if (result === 'unauthorized') {
            location.href = '/user/login';
        } else {
            alert("등록 실패");
        }
    });
});

