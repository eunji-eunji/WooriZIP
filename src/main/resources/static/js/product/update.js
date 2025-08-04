let selectedFiles = [];
let deleteIndexes = [];

function removeExistingImage(btn) {
    const container = btn.parentElement;
    const index = container.getAttribute("data-index");
    deleteIndexes.push(index);
    document.getElementById("deleteIndexes").value = deleteIndexes.join(",");
    container.remove();
}

document.getElementById("imageInput").addEventListener("change", function (event) {
    const previewArea = document.getElementById("previewArea");
    const newFiles = Array.from(event.target.files);

    if (selectedFiles.length + newFiles.length > 4) {
        alert("이미지는 최대 1장까지 업로드할 수 있습니다.");
        return;
    }

    newFiles.forEach(file => {
        selectedFiles.push(file);

        const reader = new FileReader();
        reader.onload = function (e) {
            const container = document.createElement('div');
            container.className = 'image-container';

            const img = document.createElement('img');
            img.src = e.target.result;
            img.className = 'image-preview';

            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'remove-btn';
            btn.innerHTML = '✕';
            btn.style.cssText = `
                position: absolute !important;
                top: 8px !important;
                right: 8px !important;
                background: #dc3545 !important;
                color: white !important;
                border: none !important;
                border-radius: 50% !important;
                width: 25px !important;
                height: 25px !important;
                cursor: pointer !important;
                font-size: 14px !important;
                line-height: 1 !important;
                display: flex !important;
                align-items: center !important;
                justify-content: center !important;
                transition: background-color 0.2s !important;
                padding: 0 !important;
                margin: 5px !important;
            `;
            btn.onclick = () => {
                const idx = selectedFiles.indexOf(file);
                if (idx > -1) selectedFiles.splice(idx, 1);
                container.remove();
            };

            container.appendChild(img);
            container.appendChild(btn);
            previewArea.appendChild(container);
        };
        reader.readAsDataURL(file);
    });

    // input 초기화해서 같은 파일 다시 선택 가능하게
    event.target.value = '';
});

// 옵션 조합 생성 및 테이블 렌더링, 폼 제출 로직 추가

document.getElementById('generateOptionsBtn').addEventListener('click', generateOptionTable);

function getCheckedValuesWithLabel(name) {
    return Array.from(document.querySelectorAll(`input[name="${name}"]:checked`))
        .map(cb => ({ id: cb.value, label: cb.dataset.label }));
}

function cartesianProduct(arrays) {
    return arrays.reduce((a, b) => a.flatMap(d => b.map(e => [...d, e])), [[]]);
}

document.getElementById('generateOptionsBtn').addEventListener('click', generateOptionTable);

function generateOptionTable() {
    const colors = getCheckedValuesWithLabel('color');
    const sizes = getCheckedValuesWithLabel('size');
    const materials = getCheckedValuesWithLabel('material');

    if (!colors.length || !sizes.length || !materials.length) {
        alert('모든 속성에서 최소 1개 이상 선택하세요.');
        return;
    }

    // ✅ 기존 옵션명과 가격/재고 백업
    const previousData = new Map();
    document.querySelectorAll('#optionTable tbody tr').forEach(row => {
        if (row.classList.contains('manual-row')) return; // 수동 옵션은 건드리지 않음
        const name = row.querySelector('span')?.innerText?.trim();
        const price = row.querySelector('.option-price')?.value || '';
        const stock = row.querySelector('.option-stock')?.value || '';
        if (name) previousData.set(name, { price, stock });
    });

    // ✅ 수동 row 유지하고 자동생성 부분만 초기화
    const tbody = document.querySelector('#optionTable tbody');
    const manualRows = Array.from(tbody.querySelectorAll('tr.manual-row'));
    tbody.innerHTML = '';
    manualRows.forEach(row => tbody.appendChild(row));

    // ✅ 새 조합 생성
    const allCombos = cartesianProduct([colors, sizes, materials]);
    const createdNames = new Set(manualRows.map(row => row.querySelector('span')?.innerText?.trim()));

    allCombos.forEach(combo => {
        const optionName = combo.map(c => c.label).join('/');
        if (previousData.has(optionName) || createdNames.has(optionName)) return; // ✅ 중복 방지

        const attrIds = combo.map(c => c.id);
        const tr = document.createElement('tr');
        const existing = previousData.get(optionName) || { price: '', stock: '' };

        tr.innerHTML = `
            <td>
                <input type="hidden" class="attr-ids" value="${attrIds.join(',')}">
                <span>${optionName}</span>
            </td>
            <td><input type="number" class="option-price" min="0" required value="${existing.price}"></td>
            <td><input type="number" class="option-stock" min="0" required value="${existing.stock}"></td>
            <td>
                <button type="button" class="btn btn-sm btn-danger remove-option-btn">삭제</button>
            </td>
        `;
        tbody.appendChild(tr);
    });

    document.getElementById('optionTable').style.display = '';
}

// ✅ 삭제 버튼 작동
document.addEventListener('click', function (e) {
    if (e.target.classList.contains('remove-option-btn')) {
        const tr = e.target.closest('tr');
        if (tr) tr.remove();
    }
});


// ✅ 수동 옵션 추가: 버튼 생성 및 삽입
const manualBtn = document.createElement('button');
manualBtn.type = 'button';
manualBtn.id = 'addManualOptionBtn';
manualBtn.className = 'btn btn-outline-secondary mb-2';
manualBtn.innerText = '수동 옵션 추가';
document.getElementById('generateOptionsBtn')?.after(manualBtn);

manualBtn.addEventListener('click', function () {
    const tbody = document.querySelector('#optionTable tbody');
    const index = tbody.children.length;

    const tr = document.createElement('tr');
    tr.classList.add('manual-row');

    // 옵션명 입력란
    const tdName = document.createElement('td');
    tdName.innerHTML = `
        <input type="hidden" class="model-id" value="">
        <input type="hidden" class="attr-ids" value="">
    `;
    const input = document.createElement('input');
    input.type = 'text';
    input.className = 'form-control manual-option-input';
    input.placeholder = '옵션명 입력 후 Enter';
    input.required = true;

    input.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            const val = input.value.trim();
            if (!val) {
                alert("옵션명을 입력해주세요.");
                return;
            }
            const span = document.createElement('span');
            span.textContent = val;
            tdName.replaceChild(span, input);
        }
    });

    tdName.appendChild(input);

    // 가격, 재고
    const tdPrice = document.createElement('td');
    tdPrice.innerHTML = `<input type="number" class="form-control option-price" min="0" required>`;

    const tdStock = document.createElement('td');
    tdStock.innerHTML = `<input type="number" class="form-control option-stock" min="0" required>`;

    const tdDelete = document.createElement('td');
    const delBtn = document.createElement('button');
    delBtn.type = 'button';
    delBtn.textContent = '삭제';
    delBtn.className = 'btn btn-sm btn-danger';
    delBtn.addEventListener('click', () => tr.remove());
    tdDelete.appendChild(delBtn);

    tr.appendChild(tdName);
    tr.appendChild(tdPrice);
    tr.appendChild(tdStock);
    tr.appendChild(tdDelete);
    tbody.appendChild(tr);
});

// ✅ 수정 페이지: 기존 옵션 자동 렌더링 시 가격값 출력 + 수정 가능하게
if (window.productModels && Array.isArray(window.productModels) && window.productModels.length > 0) {
    const tbody = document.querySelector('#optionTable tbody');
    tbody.innerHTML = '';
    window.productModels.forEach((model, index) => {
        const optionName = model.productModelSelect;
        const attrIds = model.attributeValueIds || [];
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>
                <input type="hidden" class="attr-ids" value="${attrIds.join(',')}">
                <span contenteditable="true">${optionName}</span>
            </td>
            <td>
                <input type="number" class="option-price" min="0" required value="${model.price ?? ''}">
            </td>
            <td>
                <input type="number" class="option-stock" min="0" required value="${model.prStock ?? ''}">
            </td>
            <td>
                <button type="button" class="btn btn-sm btn-danger remove-option-btn">삭제</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
    document.getElementById('optionTable').style.display = '';
}

document.addEventListener('click', function (e) {
    if (e.target.classList.contains('remove-option-btn')) {
        const tr = e.target.closest('tr');
        if (tr) tr.remove();
    }
});

// 폼 제출 시 옵션 정보를 JSON으로 변환해서 전송
const editForm = document.getElementById("editForm");
if (editForm) {
    editForm.addEventListener("submit", function (e) {
        e.preventDefault();
        let models = [];
        if (document.getElementById('optionTable').style.display !== 'none') {
            // 테이블 기반
            const rows = document.querySelectorAll('#optionTable tbody tr');
            rows.forEach(row => {
                const attrIds = row.querySelector('.attr-ids')?.value?.split(',') || [];
                const modelId = row.querySelector('.model-id')?.value || null;
                const optionName = row.querySelector('span')?.innerText?.trim() || '';
                const price = row.querySelector('.option-price')?.value || 0;
                const prStock = row.querySelector('.option-stock')?.value || 0;

                models.push({
                    id: modelId, // ✅ 이게 핵심
                    productModelSelect: optionName,
                    price: price,
                    prStock: prStock,
                    attributeValueIds: attrIds
                });
            });
        } else {
            // 기존 옵션 입력 UI (숨겨져 있지만 혹시 모를 fallback)
        }

        // 상품 정보 JSON 생성
        const productData = {
            id: editForm.querySelector('[name="id"]').value,
            name: editForm.querySelector('[name="name"]').value,
            price: editForm.querySelector('[name="price"]').value,
            categoryId: editForm.querySelector('[name="categoryId"]').value,
            productModelDtoList: models
        };

        // 🔧 deleteIndexes를 문자열이 아닌 배열로 전송
        const deleteIndexesRaw = editForm.querySelector('[name="deleteIndexes"]').value;
        if (deleteIndexesRaw) {
            productData.deleteIndexes = deleteIndexesRaw
                .split(',')
                .map(s => parseInt(s.trim()))
                .filter(n => !isNaN(n));
        }

        const formData = new FormData();
        formData.append('productJson', JSON.stringify(productData));
        selectedFiles.forEach(file => {
            formData.append("images", file);
        });

        fetch(`/admin/products/${productData.id}/update`, {
            method: "POST",
            body: formData
        })
        .then(res => {
            if (!res.ok) throw new Error("서버 오류");
            return res.text();
        })
        .then(result => {
            alert("상품 수정 완료");
            window.location.href = `/admin/products`;
        })
        .catch(err => {
            alert("수정 실패: " + err.message);
        });
    });
}

// 상품 수정 화면용 JS
// 카테고리 드롭다운 및 옵션 정보 자동 세팅

document.addEventListener("DOMContentLoaded", function () {
    const parentSelect = document.getElementById("parentCategory");
    const middleSelect = document.getElementById("middleCategory");
    const childSelect = document.getElementById("childCategory");

    // 기존 값
    const currentParentId = document.getElementById("currentParentId").value;
    const currentMiddleId = document.getElementById("currentMiddleId").value;
    const currentCategoryId = document.getElementById("currentCategoryId").value;

    // ✅ 여기에 추가
        console.log("🔍 window.productModels 확인:", window.productModels);

    // 1. 대분류 로딩
    fetch("/categories/parents")
        .then(res => res.json())
        .then(data => {
            parentSelect.innerHTML = '<option value="">대분류 선택</option>';
            const added = new Set();
            data.forEach(c => {
                if (!added.has(c.id)) {
                    parentSelect.appendChild(new Option(c.name, c.id));
                    added.add(c.id);
                }
            });
            if (currentParentId && currentParentId !== 'null') {
                parentSelect.value = currentParentId;
                // 2. 중분류 로딩
                fetch(`/categories/children?parentId=${currentParentId}`)
                    .then(res => res.json())
                    .then(middleData => {
                        middleSelect.innerHTML = '<option value="">중분류 선택</option>';
                        const addedM = new Set();
                        middleData.forEach(c => {
                            if (!addedM.has(c.id)) {
                                middleSelect.appendChild(new Option(c.name, c.id));
                                addedM.add(c.id);
                            }
                        });
                        if (currentMiddleId && currentMiddleId !== 'null') {
                            middleSelect.value = currentMiddleId;
                            // 3. 소분류 로딩
                            fetch(`/categories/children?parentId=${currentMiddleId}`)
                                .then(res => res.json())
                                .then(childData => {
                                    childSelect.innerHTML = '<option value="">소분류 선택</option>';
                                    const addedC = new Set();
                                    childData.forEach(c => {
                                        if (!addedC.has(c.id)) {
                                            childSelect.appendChild(new Option(c.name, c.id));
                                            addedC.add(c.id);
                                        }
                                    });
                                    if (currentCategoryId && currentCategoryId !== 'null') {
                                        childSelect.value = currentCategoryId;
                                    }
                                });
                        }
                    });
            }
        });

    window.loadMiddleCategories = function () {
        const parentId = parentSelect.value;
        // 중복 방지: 완전히 초기화
        middleSelect.innerHTML = '<option value="">중분류 선택</option>';
        childSelect.innerHTML = '<option value="">소분류 선택</option>';
        if (!parentId) return;
        fetch(`/categories/children?parentId=${parentId}`)
            .then(res => res.json())
            .then(data => {
                const added = new Set();
                data.forEach(c => {
                    if (!added.has(c.id)) {
                        middleSelect.appendChild(new Option(c.name, c.id));
                        added.add(c.id);
                    }
                });
                // 기존 값 있으면 자동 선택
                const currentMiddleId = document.getElementById("currentMiddleId").value;
                if (currentMiddleId && currentMiddleId !== 'null') {
                    middleSelect.value = currentMiddleId;
                    loadChildCategories();
                }
            });
    };

    window.loadChildCategories = function () {
        const middleId = middleSelect.value;
        // 중복 방지: 완전히 초기화
        childSelect.innerHTML = '<option value="">소분류 선택</option>';
        if (!middleId) return;
        fetch(`/categories/children?parentId=${middleId}`)
            .then(res => res.json())
            .then(data => {
                const added = new Set();
                data.forEach(c => {
                    if (!added.has(c.id)) {
                        childSelect.appendChild(new Option(c.name, c.id));
                        added.add(c.id);
                    }
                });
                // 기존 값 있으면 자동 선택
                const currentCategoryId = document.getElementById("currentCategoryId").value;
                if (currentCategoryId && currentCategoryId !== 'null') {
                    childSelect.value = currentCategoryId;
                }
            });
    };

    // 옵션 정보 자동 세팅
    // window.productModels: [{productModelSelect, price, prStock, attributeValueIds: [id, ...]}, ...]
    if (window.productModels && Array.isArray(window.productModels) && window.productModels.length > 0) {
        // 옵션 테이블 자동 렌더링
        const tbody = document.querySelector('#optionTable tbody');
        tbody.innerHTML = '';
        window.productModels.forEach(model => {
            const optionName = model.productModelSelect;
            const attrIds = model.attributeValueIds || [];
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>
                    <input type="hidden" class="model-id" value="${model.id}">
                    <input type="hidden" class="attr-ids" value="${attrIds.join(',')}">
                    <span>${optionName}</span>
                </td>
                <td><input type="number" class="option-price" min="0" required value="${model.price ?? ''}"></td>
                <td><input type="number" class="option-stock" min="0" required value="${model.prStock ?? ''}"></td>
                <td>
                    <button type="button" class="btn btn-sm btn-danger remove-option-btn">삭제</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
        document.getElementById('optionTable').style.display = '';

        // 옵션별 속성값 체크박스 자동 체크
        // (색상/사이즈/소재 체크박스는 옵션 조합 생성용이므로, 실제 옵션 정보는 테이블에서 수정)
        // 필요시, 체크박스 자동 체크 로직 추가 가능
    }
});