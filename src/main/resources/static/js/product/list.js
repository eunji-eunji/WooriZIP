let categoryTree = [];

// 페이지 로드 시 카테고리 트리 가져오기
window.onload = function () {
    fetch("/categories/tree")
    .then(res => res.json())
    .then(data => {
        categoryTree = data;
        populateParentCategory(data);
        
        // URL 파라미터에서 카테고리 확인
        const urlParams = new URLSearchParams(window.location.search);
        const categoryParam = urlParams.get('category');
        
        // URL 파라미터에서 카테고리 이름 확인
        const categoryNameParam = urlParams.get('categoryName');
        
        if (categoryParam) {
            // 카테고리 ID로 직접 찾기
            let category = null;
            let parentCategory = null;
            
            for (let cat of data) {
                if (cat.id == categoryParam) {
                    category = cat;
                    break;
                }
                if (cat.children) {
                    for (let child of cat.children) {
                        if (child.id == categoryParam) {
                            category = child;
                            parentCategory = cat;
                            break;
                        }
                    }
                }
            }
            
            if (category) {
                // 대분류 선택
                document.getElementById("parentCategory").value = parentCategory ? parentCategory.id : category.id;
                
                // 소분류가 있다면 소분류도 선택
                if (parentCategory) {
                    populateChildCategory(parentCategory.children);
                    document.getElementById("childCategory").value = category.id;
                }
                
                // 제목 업데이트 (URL 파라미터의 카테고리 이름 우선 사용)
                const titleToUse = categoryNameParam || category.name;
                updateCategoryTitle(titleToUse);
            }
        }
    });
};

// 대분류 드롭다운 채우기
function populateParentCategory(data) {
    const parentSelect = document.getElementById("parentCategory");
    parentSelect.innerHTML = `<option value="">-- 선택 --</option>`;
    data.forEach(cat => {
        parentSelect.innerHTML += `<option value="${cat.id}">${cat.name}</option>`;
    });

    parentSelect.addEventListener("change", function () {
        const selectedId = parseInt(this.value);
        const selected = categoryTree.find(c => c.id === selectedId);
        populateChildCategory(selected?.children || []);
    });
}

// 소분류 드롭다운 채우기
function populateChildCategory(children) {
    const childSelect = document.getElementById("childCategory");
    childSelect.innerHTML = `<option value="">-- 선택 --</option>`;
    children.forEach(child => {
        childSelect.innerHTML += `<option value="${child.id}">${child.name}</option>`;
    });
}

// 카테고리 이름으로 ID 찾기
function findCategoryByName(categories, name) {
    for (let category of categories) {
        if (category.name === name) {
            return category;
        }
        if (category.children) {
            const found = findCategoryByName(category.children, name);
            if (found) return found;
        }
    }
    return null;
}

// 카테고리 기반 검색
function filterByCategory() {
    const categoryId = document.getElementById("childCategory").value || document.getElementById("parentCategory").value;
    if (!categoryId) {
        alert("카테고리를 선택해주세요.");
        return;
    }
    
    // 선택된 카테고리 이름 가져오기
    const selectedCategoryName = getSelectedCategoryName();
    
    // URL에 카테고리 이름도 포함하여 전달
    const url = `/products?category=${categoryId}&categoryName=${encodeURIComponent(selectedCategoryName)}`;
    window.location.href = url;
}

// 선택된 카테고리 이름 가져오기
function getSelectedCategoryName() {
    const childCategory = document.getElementById("childCategory");
    const parentCategory = document.getElementById("parentCategory");
    
    if (childCategory.value) {
        return childCategory.options[childCategory.selectedIndex].text;
    } else if (parentCategory.value) {
        return parentCategory.options[parentCategory.selectedIndex].text;
    }
    return "전체 상품";
}

// 카테고리 제목 업데이트
function updateCategoryTitle(categoryName) {
    const mainTitleElement = document.getElementById("mainTitle");
    if (mainTitleElement) {
        // 카테고리 이름이 없거나 "전체 상품"인 경우 기본값 설정
        const displayName = categoryName && categoryName !== "전체 상품" ? categoryName : "전체 상품";
        mainTitleElement.textContent = displayName;
    }
}