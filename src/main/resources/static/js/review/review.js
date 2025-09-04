

document.addEventListener('DOMContentLoaded', function() {
    // 리뷰 관련 함수들
    window.toggleReviewForm = function () {
        const form = document.getElementById("reviewForm");
        if (form) {
            form.classList.toggle("d-none");
        }
    };

    window.hideReviewEditForm = function(reviewId) {
        const form = document.getElementById(`reviewEditForm_${reviewId}`);
        if (form) {
            form.style.display = 'none';
        }
    };

    window.toggleReviewEditForm = function(reviewId) {
        const form = document.getElementById(`reviewEditForm_${reviewId}`);
        if (form) {
            form.style.display = form.style.display === 'none' ? 'block' : 'none';
        }
    };

    // 리뷰 등록 폼 초기화
    function initializeReviewForm() {
        const form = document.getElementById('reviewForm');
        if (!form) return;

        const imageInput = form.querySelector('input[type="file"]');
        const previewContainer = document.getElementById('reviewPreviewContainer');
        let selectedFiles = [];

        if (imageInput && previewContainer) {
            imageInput.addEventListener('change', function(e) {
                const newFiles = Array.from(this.files);
                
                if (selectedFiles.length + newFiles.length > 4) {
                    alert('이미지는 최대 4장까지 첨부할 수 있습니다.');
                    return;
                }

                newFiles.forEach(file => {
                    selectedFiles.push(file);
                    
                    const reader = new FileReader();
                    reader.onload = function(e) {
                        const imgContainer = document.createElement('div');
                        imgContainer.className = 'position-relative d-inline-block me-2 mb-2';
                        
                        const img = document.createElement('img');
                        img.src = e.target.result;
                        img.className = 'img-thumbnail';
                        img.style.width = '150px';
                        img.style.height = '150px';
                        img.style.objectFit = 'cover';
                        
                        const deleteBtn = document.createElement('button');
                        deleteBtn.type = 'button';
                        deleteBtn.className = 'btn btn-danger btn-sm position-absolute top-0 end-0 rounded-circle delete-existing-image';
                        deleteBtn.innerHTML = '×';
                        deleteBtn.style.width = '25px';
                        deleteBtn.style.height = '25px';
                        deleteBtn.style.padding = '0';
                        deleteBtn.style.margin = '5px';
                        
                        const fileIndex = selectedFiles.length - 1;
                        deleteBtn.onclick = function() {
                            selectedFiles.splice(fileIndex, 1);
                            imgContainer.remove();
                        };
                        
                        imgContainer.appendChild(img);
                        imgContainer.appendChild(deleteBtn);
                        previewContainer.appendChild(imgContainer);
                    };
                    reader.readAsDataURL(file);
                });
                
                this.value = '';
            });

            form.addEventListener('submit', function(e) {
                e.preventDefault();
                const formData = new FormData(this);
                
                formData.delete('files');
                selectedFiles.forEach(file => {
                    formData.append('files', file);
                });

                fetch(this.action, {
                    method: 'POST',
                    body: formData
                })
                .then(res => {
                    if (res.redirected) {
                        // 페이지 로드 완료 후 해시 추가
                        window.addEventListener('load', function() {
                            window.location.hash = '#review-section';
                        }, { once: true });
                        window.location.href = res.url;
                    } else if (!res.ok) {
                        throw new Error('서버 오류');
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('오류가 발생했습니다. 다시 시도해주세요.');
                });
            });
        }
    }

    // 리뷰 수정 폼 초기화
    function initializeReviewEditForm(form) {
        if (!form) return;

        const imageInput = form.querySelector('input[type="file"]');
        const previewContainer = form.querySelector('.review-preview-container');
        const existingImagesContainer = form.querySelector('.existing-images-container');
        let selectedFiles = [];

        // 기존 이미지 삭제 처리
        if (existingImagesContainer) {
            const deletedImages = new Set();

            existingImagesContainer.querySelectorAll('.delete-existing-image').forEach(btn => {
                btn.onclick = function() {
                    const path = this.getAttribute('data-path');
                    const imageContainer = this.closest('.position-relative');
                    const hiddenInput = imageContainer.querySelector('input[name="deleteImages"]');
                    
                    if (deletedImages.has(path)) {
                        // 삭제 취소
                        deletedImages.delete(path);
                        imageContainer.style.opacity = '1';
                        hiddenInput.disabled = true;
                        this.textContent = '×';
                        this.classList.remove('btn-success');
                        this.classList.add('btn-danger');
                    } else {
                        // 삭제 표시
                        deletedImages.add(path);
                        imageContainer.style.opacity = '0.5';
                        hiddenInput.disabled = false;
                        this.textContent = '↺';
                        this.classList.remove('btn-danger');
                        this.classList.add('btn-success');
                    }
                };
            });
        }

        // 새 이미지 업로드 처리
        if (imageInput && previewContainer) {
            imageInput.addEventListener('change', function(e) {
                const newFiles = Array.from(this.files);
                const existingCount = existingImagesContainer ? 
                    existingImagesContainer.querySelectorAll('.position-relative').length - 
                    existingImagesContainer.querySelectorAll('input[name="deleteImages"]:not([disabled])').length : 0;
                
                if (existingCount + selectedFiles.length + newFiles.length > 4) {
                    alert('이미지는 최대 4장까지 첨부할 수 있습니다.');
                    return;
                }

                newFiles.forEach(file => {
                    selectedFiles.push(file);
                    
                    const reader = new FileReader();
                    reader.onload = function(e) {
                        const imgContainer = document.createElement('div');
                        imgContainer.className = 'position-relative d-inline-block me-2 mb-2';
                        
                        const img = document.createElement('img');
                        img.src = e.target.result;
                        img.className = 'img-thumbnail';
                        img.style.width = '150px';
                        img.style.height = '150px';
                        img.style.objectFit = 'cover';
                        
                        const deleteBtn = document.createElement('button');
                        deleteBtn.type = 'button';
                        deleteBtn.className = 'btn btn-danger btn-sm position-absolute top-0 end-0 rounded-circle';
                        deleteBtn.innerHTML = '×';
                        deleteBtn.style.width = '25px';
                        deleteBtn.style.height = '25px';
                        deleteBtn.style.padding = '0';
                        deleteBtn.style.margin = '5px';
                        
                        const fileIndex = selectedFiles.length - 1;
                        deleteBtn.onclick = function() {
                            selectedFiles.splice(fileIndex, 1);
                            imgContainer.remove();
                        };
                        
                        imgContainer.appendChild(img);
                        imgContainer.appendChild(deleteBtn);
                        previewContainer.appendChild(imgContainer);
                    };
                    reader.readAsDataURL(file);
                });
                
                this.value = '';
            });
        }

        // 폼 제출 처리
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            const formData = new FormData(this);
            
            formData.delete('files');
            selectedFiles.forEach(file => {
                formData.append('files', file);
            });

            fetch(this.action, {
                method: 'POST',
                body: formData
            })
            .then(res => {
                if (res.redirected) {
                    // 페이지 로드 완료 후 해시 추가
                    window.addEventListener('load', function() {
                        window.location.hash = '#review-section';
                    }, { once: true });
                    window.location.href = res.url;
                } else if (!res.ok) {
                    throw new Error('서버 오류');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('오류가 발생했습니다. 다시 시도해주세요.');
            });
        });
    }

    // 모든 리뷰 등록/수정 폼 초기화
    initializeReviewForm();
    document.querySelectorAll('.review-form').forEach(form => {
        initializeReviewEditForm(form);
    });
});

