let selectedFiles = [];
let deleteIndexes = [];

function removeExistingImage(btn) {
    const container = btn.parentElement;
    const index = container.getAttribute("data-index");
    deleteIndexes.push(index);
    document.getElementById("deleteIndexes").value = deleteIndexes.join(",");
    container.remove();
}

function handleFileInput(event) {
    const files = Array.from(event.target.files);
    const preview = document.getElementById("previewArea");

    for (let file of files) {
        if (selectedFiles.length >= 8) {
            alert("최대 8장까지만 업로드할 수 있습니다.");
            break;
        }

        selectedFiles.push(file);

        const reader = new FileReader();
        reader.onload = e => {
            const container = document.createElement('div');
            container.className = 'image-container';

            const img = document.createElement('img');
            img.src = e.target.result;
            img.className = 'image-preview';

            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'remove-btn';
            btn.innerText = '✕';
            btn.onclick = () => {
                const idx = Array.from(preview.children).indexOf(container);
                selectedFiles.splice(idx, 1);
                container.remove();
            };

            container.appendChild(img);
            container.appendChild(btn);
            preview.appendChild(container);
        };
        reader.readAsDataURL(file);
    }

    event.target.value = "";
}

document.getElementById("imageInput").addEventListener("change", handleFileInput);

document.getElementById("editForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const form = e.target;
    const formData = new FormData(form);

    selectedFiles.forEach(file => {
        formData.append("files", file);
    });

    fetch("/interior/edit", {
        method: "POST",
        body: formData
    }).then(res => res.text())
    .then(result => {
        if (result === "success") {
            alert("수정 완료");
            location.href = "/interior/" + form.postId.value;
        } else {
            alert("수정 실패");
        }
    });
});