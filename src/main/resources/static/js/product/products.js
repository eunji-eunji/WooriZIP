document.addEventListener("DOMContentLoaded", function () {
    const parentSelect = document.getElementById("parentCategory");
    const middleSelect = document.getElementById("middleCategory");
    const childSelect = document.getElementById("childCategory");
    const input = document.getElementById('imageInput');
    const preview = document.getElementById('previewContainer');
    const form = document.getElementById('productForm');
    const optionContainer = document.getElementById('optionContainer');
    let selectedFiles = [];

    // 1. 카테고리 드롭다운 로딩
    fetch("/categories/parents")
        .then(res => res.json())
        .then(data => {
            data.forEach(c => {
                const option = new Option(c.name, c.id);
                parentSelect.appendChild(option);
            });
        });

    parentSelect?.addEventListener('change', function () {
        const parentId = this.value;
        middleSelect.innerHTML = '<option value="">중분류 선택</option>';
        childSelect.innerHTML = '<option value="">소분류 선택</option>';
        if (!parentId) return;
        fetch(`/categories/children?parentId=${parentId}`)
            .then(res => res.json())
            .then(data => {
                data.forEach(c => {
                    middleSelect.appendChild(new Option(c.name, c.id));
                });
            });
    });

    middleSelect?.addEventListener('change', function () {
        const middleId = this.value;
        childSelect.innerHTML = '<option value="">소분류 선택</option>';
        if (!middleId) return;
        fetch(`/categories/children?parentId=${middleId}`)
            .then(res => res.json())
            .then(data => {
                data.forEach(c => {
                    childSelect.appendChild(new Option(c.name, c.id));
                });
            });
    });

    // 2. 이미지 업로드 영역 클릭 이벤트
    const imageUploadArea = document.getElementById('imageUploadArea');
    if (imageUploadArea) {
        imageUploadArea.addEventListener('click', function() {
            input.click();
        });

        // 드래그 앤 드롭 이벤트
        imageUploadArea.addEventListener('dragover', function(e) {
            e.preventDefault();
            this.classList.add('dragover');
        });

        imageUploadArea.addEventListener('dragleave', function(e) {
            e.preventDefault();
            this.classList.remove('dragover');
        });

        imageUploadArea.addEventListener('drop', function(e) {
            e.preventDefault();
            this.classList.remove('dragover');
            
            const files = Array.from(e.dataTransfer.files);
            const imageFiles = files.filter(file => file.type.startsWith('image/'));
            
            if (imageFiles.length > 0) {
                input.files = new DataTransfer().files;
                input.files = imageFiles;
                input.dispatchEvent(new Event('change'));
            }
        });
    }

    // 3. 이미지 미리보기
    input.addEventListener('change', function () {
        selectedFiles = Array.from(this.files);
        if (selectedFiles.length > 1) {
            alert("이미지는 1장만 업로드할 수 있습니다.");
            selectedFiles = selectedFiles.slice(0, 1);
            this.value = '';
            preview.innerHTML = '';
            return;
        }

        preview.innerHTML = '';
        selectedFiles.forEach(file => {
            const reader = new FileReader();
            reader.onload = e => {
                const box = document.createElement('div');
                box.className = 'image-container';
                box.style.position = 'relative';
                
                const img = document.createElement('img');
                img.src = e.target.result;
                img.className = 'image-preview';
                img.onerror = function() {
                    console.error('이미지 로드 실패:', file.name);
                    box.remove();
                };
                
                const removeBtn = document.createElement('button');
                removeBtn.type = 'button';
                removeBtn.className = 'btn btn-danger btn-sm position-absolute top-0 end-0 rounded-circle';
                removeBtn.style.width = '25px';
                removeBtn.style.height = '25px';
                removeBtn.style.padding = '0';
                removeBtn.style.lineHeight = '1';
                removeBtn.style.margin = '5px';
                removeBtn.innerHTML = '✕';
                removeBtn.addEventListener('click', function() {
                    const index = selectedFiles.indexOf(file);
                    if (index > -1) {
                        selectedFiles.splice(index, 1);
                    }
                    box.remove();
                });
                
                box.appendChild(img);
                box.appendChild(removeBtn);
                preview.appendChild(box);
                
                console.log('이미지 미리보기 생성:', file.name);
            };
            reader.onerror = function() {
                console.error('파일 읽기 실패:', file.name);
            };
            reader.readAsDataURL(file);
        });
    });

    // === 모든 조합 자동생성 및 옵션 테이블 ===
    function getCheckedValuesWithLabel(name) {
        return Array.from(document.querySelectorAll(`input[name="${name}"]:checked`))
            .map(cb => ({ id: cb.value, label: cb.dataset.label }));
    }

    function cartesianProduct(arrays) {
        return arrays.reduce((a, b) => a.flatMap(d => b.map(e => [...d, e])), [[]]);
    }

    function generateOptionTable() {
        const colors = getCheckedValuesWithLabel('color');
        const sizes = getCheckedValuesWithLabel('size');
        const materials = getCheckedValuesWithLabel('material');

        if (!colors.length || !sizes.length || !materials.length) {
            alert('모든 속성에서 최소 1개 이상 선택하세요.');
            return;
        }

        const allCombos = cartesianProduct([colors, sizes, materials]);
        const tbody = document.querySelector('#optionTable tbody');
        tbody.innerHTML = '';

        allCombos.forEach((combo, idx) => {
            const optionName = combo.map(c => c.label).join('/');
            const attrIds = combo.map(c => c.id);
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>
                    <input type="hidden" class="attr-ids" value="${attrIds.join(',')}">
                    <span>${optionName}</span>
                </td>
                <td><input type="number" class="option-price" min="0" required></td>
                <td><input type="number" class="option-stock" min="0" required></td>
                <td><button type="button" class="remove-row-btn">삭제</button></td>
            `;
            tr.querySelector('.remove-row-btn').addEventListener('click', () => tr.remove());
                        tbody.appendChild(tr);
        });

        document.getElementById('optionTable').style.display = '';
    }

    document.getElementById('generateOptionsBtn').addEventListener('click', generateOptionTable);

    // 수동 옵션 추가: input → Enter 누르면 span으로 변환
        const manualBtn = document.createElement('button');
        manualBtn.type = 'button';
        manualBtn.id = 'addManualOptionBtn';
        manualBtn.innerText = '수동 옵션 추가';
        document.getElementById('generateOptionsBtn').after(manualBtn);

        manualBtn.addEventListener('click', function () {
            const tbody = document.querySelector('#optionTable tbody');
            const tr = document.createElement('tr');

            const td = document.createElement('td');
            td.innerHTML = `<input type="hidden" class="attr-ids" value="">`;

            const input = document.createElement('input'); // 변경
            input.type = 'text';
            input.placeholder = '옵션명 입력 후 Enter';
            input.required = true;
            input.className = 'option-name-input';

            input.addEventListener('keydown', function (e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    if (!input.value.trim()) {
                        alert('옵션명을 입력해주세요.');
                        return;
                    }
                    const span = document.createElement('span');
                    span.textContent = input.value.trim();
                    td.replaceChild(span, input); // input → span
                }
            });

            td.appendChild(input);

            const tdPrice = document.createElement('td');
            tdPrice.innerHTML = `<input type="number" class="option-price" min="0" required>`;

            const tdStock = document.createElement('td');
            tdStock.innerHTML = `<input type="number" class="option-stock" min="0" required>`;

            const tdDelete = document.createElement('td');
            const delBtn = document.createElement('button');
            delBtn.type = 'button';
            delBtn.textContent = '삭제';
            delBtn.className = 'remove-row-btn';
            delBtn.addEventListener('click', () => tr.remove());
            tdDelete.appendChild(delBtn);

            tr.appendChild(td);
            tr.appendChild(tdPrice);
            tr.appendChild(tdStock);
            tr.appendChild(tdDelete);

            tbody.appendChild(tr);
            document.getElementById('optionTable').style.display = '';
        });

    // 폼 제출 시 모든 옵션을 JSON으로 변환
    function collectOptionsToJson() {
        const rows = document.querySelectorAll('#optionTable tbody tr');
        const models = [];
        rows.forEach(row => {
            const attrIds = row.querySelector('.attr-ids').value.split(',').filter(Boolean);
            const optionNameEl = row.querySelector('span') || row.querySelector('.option-name-input'); // 변경
            const optionName = optionNameEl?.innerText || optionNameEl?.value || ''; // 변경
            const price = row.querySelector('.option-price').value;
            const prStock = row.querySelector('.option-stock').value;
            models.push({
                productModelSelect: optionName,
                price: price,
                prStock: prStock,
                attributeValueIds: attrIds
            });
        });
        return models;
    }

    // 4. 옵션 추가 함수
    window.addOption = function () {
        const optionCount = optionContainer.children.length;
        if (optionCount >= 50) {
            alert("옵션은 최대 10개까지 추가할 수 있습니다.");
            return;
        }

        const div = document.createElement('div');
        div.className = 'option-item';

        let colorChecks = '', sizeChecks = '', materialChecks = '';
        window.attributeValues.forEach(val => {
            const checkbox = `<input type="checkbox" name="productModelDtoList[${optionCount}].attributeValueIds" value="${val.id}">${val.value} `;
            if (val.attributeName === '색상') colorChecks += checkbox;
            if (val.attributeName === '사이즈') sizeChecks += checkbox;
            if (val.attributeName === '소재') materialChecks += checkbox;
        });

        div.innerHTML = `
            <label>모델명:</label>
            <input type="text" name="productModelDtoList[${optionCount}].productModelSelect" placeholder="모델명" required><br/>
            <label>재고:</label>
            <input type="number" name="productModelDtoList[${optionCount}].prStock" placeholder="재고 입력"><br/>
            <label>가격:</label>
            <input type="number" name="productModelDtoList[${optionCount}].price" placeholder="가격 입력"><br/>
            <div class="option-attributes">
                <label>색상:</label> ${colorChecks}<br/>
                <label>사이즈:</label> ${sizeChecks}<br/>
                <label>소재:</label> ${materialChecks}
            </div>
            <button type="button" class="removeOptionBtn">옵션 삭제</button>
            <hr/>
        `;

        optionContainer.appendChild(div);
        updateRemoveButtons();
    };

    function updateRemoveButtons() {
        const items = optionContainer.querySelectorAll('.option-item');
        items.forEach((item, idx) => {
            const btn = item.querySelector('.removeOptionBtn');
            btn.style.display = (items.length > 1) ? '' : 'none';
            btn.onclick = function () {
                item.remove();
                reorderOptionNames();
                updateRemoveButtons();
            };
        });
    }

    function reorderOptionNames() {
        const items = optionContainer.querySelectorAll('.option-item');
        items.forEach((item, idx) => {
            item.querySelector('input[name$=".productModelSelect"]').name = `productModelDtoList[${idx}].productModelSelect`;
            item.querySelector('input[name$=".prStock"]').name = `productModelDtoList[${idx}].prStock`;
            item.querySelector('input[name$=".price"]').name = `productModelDtoList[${idx}].price`;

            const checkboxes = item.querySelectorAll('input[type="checkbox"]');
            checkboxes.forEach(cb => {
                cb.name = `productModelDtoList[${idx}].attributeValueIds`;
            });
        });
    }

    updateRemoveButtons(); // 초기화

    // 기존 폼 제출 이벤트 오버라이드
    form.addEventListener('submit', function (e) {
        e.preventDefault();
        let models = [];
        // optionTable이 보이면 테이블 기반, 아니면 기존 방식
        if (document.getElementById('optionTable').style.display !== 'none') {
            models = collectOptionsToJson();
        } else {
            // 기존 옵션 입력 UI (숨겨져 있지만 혹시 모를 fallback)
            const optionItems = optionContainer.querySelectorAll('.option-item');
            optionItems.forEach(item => {
                const model = {
                    productModelSelect: item.querySelector('input[name$=".productModelSelect"]').value,
                    prStock: item.querySelector('input[name$=".prStock"]').value,
                    price: item.querySelector('input[name$=".price"]').value,
                    attributeValueIds: Array.from(item.querySelectorAll('input[type="checkbox"]:checked')).map(cb => cb.value)
                };
                models.push(model);
            });
        }

        const formData = new FormData(form);
        // 기존 옵션 관련 필드 제거
        for (let pair of formData.keys()) {
            if (pair.startsWith('productModelDtoList[')) {
                formData.delete(pair);
            }
        }
        formData.append('productModelDtoListJson', JSON.stringify(models));

        fetch(form.action, {
            method: 'POST',
            body: formData
        }).then(res => {
            if (res.redirected) {
                location.href = res.url;
            } else {
                alert('등록 실패');
            }
        }).catch(() => alert('에러 발생'));
    });
});
