

document.addEventListener("DOMContentLoaded", function () {
    const modelSelect = document.getElementById('modelSelect');
    const countInput = document.getElementById('count');
    const totalPriceDisplay = document.getElementById('totalPrice'); // HTML의 id와 일치
    const selectedPriceSpan = document.getElementById('selectedPrice');
    const modelInfo = document.getElementById('modelInfo');

    // 컨트롤러에서 전달받은 모든 AttributeValue 데이터
    // 이 데이터는 { id: Long, value: String, attributeName: String } 형태여야 합니다.
    const allAttributeValues = window.allAttributeValues || [];

    // 개별 속성 드롭다운 요소들
    const colorSelect = document.getElementById('colorSelect');
    const sizeSelect = document.getElementById('sizeSelect');
    const materialSelect = document.getElementById('materialSelect');

    // =====================================================================
    // 헬퍼 함수: dataset.attr 문자열을 숫자 배열로 파싱
    // =====================================================================
    function parseAttributeIds(attrString) {
        if (!attrString) return [];
        try {
            // '[1,2,3]' 형태의 JSON 배열 문자열을 파싱
            return JSON.parse(attrString).map(Number).sort((a, b) => a - b);
        } catch (e) {
            // JSON 파싱 실패 시, '1,2,3' 형태의 콤마 구분 문자열을 파싱
            console.warn("JSON parse failed for data-attr, trying split. Error:", e);
            return attrString.replace(/\[|\]|\s/g, '') // 괄호와 공백 제거
                             .split(',')            // 콤마로 분리
                             .filter(Boolean)       // 빈 문자열 제거
                             .map(Number)           // 숫자로 변환
                             .sort((a, b) => a - b); // 숫자 정렬
        }
    }

    // =====================================================================
    // 1. 가격, 재고, 총 결제 금액 및 모델 정보 업데이트 함수
    // =====================================================================
    function updatePriceAndTotal(price, stock, modelName = '') {
        const currentPrice = price !== undefined ? price : 0;
        const currentStock = stock !== undefined ? stock : 0;
        const count = parseInt(countInput?.value) || 1;

        selectedPriceSpan.innerText = currentPrice.toLocaleString() + '원';
        totalPriceDisplay.innerText = (currentPrice * count).toLocaleString() + '원';

        // 모델 정보 표시 업데이트
        if (!modelName || currentPrice === 0) {
        // 초기 상태 또는 모델명 없이 price만 있는 경우
        modelInfo.innerHTML = `<span style="color: #666; font-weight: 500; font-style: italic;">옵션을 선택해주세요</span>`;
        modelInfo.style.display = 'block';
        document.getElementById('cartButton').disabled = true;
        document.getElementById('buyButton').disabled = true;
        } else {
        const count = parseInt(countInput?.value) || 1;
        modelInfo.innerHTML = `<span style="color: #8d5a41; font-weight: 600;">${modelName}</span> <span style="color: #666; margin: 0 8px;">•</span> <span style="color: #28a745; font-weight: 600;">${currentPrice.toLocaleString()}원</span> <span style="color: #666; margin: 0 8px;">•</span> <span style="color: #007bff; font-weight: 600;">수량: ${count}</span>`;
        modelInfo.style.display = 'block';

        if (currentStock <= 0) {
                    const count = parseInt(countInput?.value) || 1;
                    modelInfo.innerHTML = `<span style="color: #8d5a41; font-weight: 600;">${modelName}</span> <span style="color: #666; margin: 0 8px;">•</span> <span style="color: #28a745; font-weight: 600;">${currentPrice.toLocaleString()}원</span> <span style="color: #666; margin: 0 8px;">•</span> <span style="color: #dc3545; font-weight: 600;">품절</span>`;
                    cartButton.disabled = true;
                    cartButton.classList.add("btn-disabled");
         // TODO: 품절 시 장바구니/구매 버튼 비활성화 로직 추가
        buyButton.disabled = true;
                    buyButton.classList.add("btn-disabled");
                } else {
                    cartButton.disabled = false;
                    cartButton.classList.remove("btn-disabled");
        // TODO: 기본 상태에서 버튼 비활성화 (모델 선택 필요)
        buyButton.disabled = false;
                    buyButton.classList.remove("btn-disabled");
                }
    }
    }

    function submitForm(actionType) {
            document.getElementById('actionType').value = actionType;
            document.getElementById('productForm').submit();
        }

    // =====================================================================
    // 2. 개별 속성 드롭다운 (색상, 사이즈, 소재) 동적 채우기 및 선택 표시
    // =====================================================================
    function populateIndividualDropdowns(selectedModelAttrIds = []) {
        // 모든 드롭다운 초기화
        [colorSelect, sizeSelect, materialSelect].forEach(select => {
            if (select) select.innerHTML = '<option value="">' + select.id.replace('Select', ' 선택') + '</option>';
        });

        // allAttributeValues 데이터를 사용하여 각 드롭다운 채우기
        allAttributeValues.forEach(val => {
            const option = document.createElement('option');
            option.value = val.id; // AttributeValue의 ID (Long)
            option.textContent = val.value; // AttributeValue의 실제 값 (예: "블랙")

            // 현재 선택된 modelSelect에 해당하는 속성값이면 'selected'로 표시
            if (selectedModelAttrIds.includes(val.id)) {
                option.selected = true;
            }

            if (val.attributeName === '색상' && colorSelect) {
                colorSelect.appendChild(option);
            } else if (val.attributeName === '사이즈' && sizeSelect) {
                sizeSelect.appendChild(option);
            } else if (val.attributeName === '소재' && materialSelect) {
                materialSelect.appendChild(option);
            }
        });
    }

    // 3. ProductModel을 찾는 로직 (개별 옵션 드롭다운 변경 시)
    function findAndSelectModelFromIndividualOptions() {
        const selectedColorId = colorSelect?.value ? Number(colorSelect.value) : null;
        const selectedSizeId = sizeSelect?.value ? Number(sizeSelect.value) : null;
        const selectedMaterialId = materialSelect?.value ? Number(materialSelect.value) : null;

        // 모든 옵션이 선택되지 않았다면 메인 모델 셀렉트 박스 초기화 및 가격/재고 초기화
        if (!selectedColorId || !selectedSizeId || !selectedMaterialId) {
            modelSelect.value = "";
            updatePriceAndTotal(0, 0, '');
            return;
        }

        // 사용자가 선택한 속성 ID들을 정렬하여 비교 준비
        const selectedAttrIdsFromIndividuals = [selectedColorId, selectedSizeId, selectedMaterialId].sort((a, b) => a - b);

        let foundModelOption = null;
        let foundModelName = '';

        // modelSelect의 모든 옵션들을 순회하며 일치하는 ProductModel을 찾습니다.
        for (let i = 0; i < modelSelect.options.length; i++) {
            const option = modelSelect.options[i];
            if (option.value === "") continue; // "옵션을 선택하세요" 제외

            const modelAttrIds = parseAttributeIds(option.dataset.attr); // 헬퍼 함수 사용

            // 선택된 속성 ID들과 모델의 속성 ID들이 정확히 일치하는지 확인 (개수 및 값 모두 일치)
            if (selectedAttrIdsFromIndividuals.length === modelAttrIds.length &&
                selectedAttrIdsFromIndividuals.every((id, index) => id === modelAttrIds[index])) {
                foundModelOption = option;
                // 모델명 추출 (예: "블랙 S / 10,000원 / 재고:10" -> "블랙 S")
                foundModelName = option.textContent.split(' / ')[0];
                break;
            }
        }

        if (foundModelOption) {
            modelSelect.value = foundModelOption.value; // 메인 모델 셀렉트 박스 선택
            updatePriceAndTotal(parseInt(foundModelOption.dataset.price), parseInt(foundModelOption.dataset.stock), foundModelName);
        } else {
            modelSelect.value = ""; // 일치하는 모델이 없으면 메인 셀렉트 박스 초기화
            updatePriceAndTotal(0, 0, ''); // 가격/재고 초기화
            console.log("선택된 옵션 조합과 일치하는 모델이 없습니다.");
            alert("선택하신 옵션 조합의 상품은 존재하지 않습니다."); // 사용자에게 알림
        }
    }

    // 4. 이벤트 리스너 설정
    // 메인 모델 드롭다운 변경 시
    modelSelect?.addEventListener('change', function () {
        const selected = this.selectedOptions[0];
        if (selected && selected.dataset.price && selected.dataset.stock) {
            // 모델명 추출
            const modelName = selected.textContent.split(' / ')[0];
            updatePriceAndTotal(parseInt(selected.dataset.price), parseInt(selected.dataset.stock), modelName);
            const currentAttrIds = parseAttributeIds(selected.dataset.attr); // 헬퍼 함수 사용
            populateIndividualDropdowns(currentAttrIds); // 개별 드롭다운도 해당 모델의 속성으로 채우고 선택 표시

            const productId = window.location.pathname.split("/").pop();
            const modelId = selected.value;
            const actionType = "VIEW";
            const weight = 1;

            fetch("/recommend/log", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({
                    productId: productId,
                    modelId: modelId,
                    actionType: actionType,
                    weight: weight
                })
            }).then(response => {
                if(!response.ok) {
                console.warn("로그 전송 실패");
                }
            }).catch(error => {
                console.error("로그 전송 에러: ", error);
            });

        } else {
            // "옵션을 선택하세요" 선택 시 초기화
            updatePriceAndTotal(0, 0, '');
            populateIndividualDropdowns([]); // 개별 드롭다운 초기화
        }
    });

    // 수량 입력 변경 시
    countInput?.addEventListener('input', function() {
        const selected = modelSelect?.selectedOptions[0];
        if (selected && selected.dataset.price && selected.dataset.stock) {
            // 모델명은 이미 선택된 상태에서 가져옴
            const modelName = selected.textContent.split(' / ')[0];
            updatePriceAndTotal(parseInt(selected.dataset.price), parseInt(selected.dataset.stock), modelName);
        } else {
            updatePriceAndTotal(0, 0, '');
        }
    });

    // 개별 속성 드롭다운 변경 시 (findAndSelectModelFromIndividualOptions 호출)
    colorSelect?.addEventListener('change', findAndSelectModelFromIndividualOptions);
    sizeSelect?.addEventListener('change', findAndSelectModelFromIndividualOptions);
    materialSelect?.addEventListener('change', findAndSelectModelFromIndividualOptions);

    // =====================================================================
    // 5. 초기 로딩 시 UI 상태 설정
    // 페이지 로드 시 Product의 초기 가격을 표시
    // HTML의 selectedPriceSpan에 th:data-initial-price="${product.price}"가 있어야 합니다.
    const initialPrice = parseInt(selectedPriceSpan.dataset.initialPrice || '0');
    // 초기 로드 시 ProductModel이 선택되지 않았으므로 재고나 모델명은 알 수 없습니다.
    updatePriceAndTotal(initialPrice, 0, '');
    populateIndividualDropdowns([]); // 초기에는 어떤 옵션도 선택되지 않은 상태로 개별 드롭다운 채움

    // =====================================================================
    // 6. 탭 전환 로직
    function initializeTabs() {
        function switchTab(targetId) {
            document.querySelectorAll('.tab-button').forEach(b => {
                const isActive = b.getAttribute('data-target') === targetId;
                b.classList.toggle('active', isActive);
                
                // 스타일 직접 업데이트
                if (isActive) {
                    b.style.borderBottom = '2px solid var(--brand-point)';
                    b.style.color = 'var(--brand-point)';
                } else {
                    b.style.borderBottom = '2px solid transparent';
                    b.style.color = '#666';
                }
            });
            document.querySelectorAll('.tab-content-section').forEach(section => {
                section.style.display = section.id === targetId ? 'block' : 'none';
            });
        }

        // URL 파라미터에서 값을 가져오는 함수
        function getParamFromURL(param) {
            const urlParams = new URLSearchParams(window.location.search);
            return urlParams.get(param);
        }

        // 페이지 로드 시와 해시 변경 시 모두 실행되는 함수
        function handleTabChange() {
            const activeTab = getParamFromURL('activeTab');
            const editQnaId = getParamFromURL('editQna');
            const editReviewId = getParamFromURL('editReview');

            // 탭 전환
            if (activeTab === 'qna') {
                switchTab('qna-section');
                // QnA 수정 처리
                if (editQnaId) {
                    setTimeout(() => {
                        const qnaRow = document.querySelector(`[data-qna-id="${editQnaId}"]`);
                        if (qnaRow) {
                            const editBtn = qnaRow.querySelector('.qna-edit-btn');
                            if (editBtn) {
                                editBtn.click();
                            }
                        }
                    }, 500);
                }
            } else if (activeTab === 'review') {
                switchTab('review-section');
                // 리뷰 수정 처리
                if (editReviewId) {
                    setTimeout(() => {
                        const reviewRow = document.querySelector(`[data-review-id="${editReviewId}"]`);
                        if (reviewRow) {
                            const editBtn = reviewRow.querySelector('.review-edit-btn');
                            if (editBtn) {
                                editBtn.click();
                            }
                        }
                    }, 500);
                }
            } else {
                switchTab('review-section'); // 기본값
            }

            // 해시에 따른 스크롤 처리
            if (window.location.hash) {
                setTimeout(() => {
                    const section = document.querySelector(window.location.hash);
                    if (section) {
                        section.scrollIntoView({ behavior: 'auto' });
                    }
                }, 50);
            }
        }

        // 탭 버튼 클릭 이벤트
        document.querySelectorAll('.tab-button').forEach(btn => {
            btn.addEventListener('click', function () {
                const target = this.getAttribute('data-target');
                switchTab(target);

                // URL 파라미터 업데이트
                const url = new URL(window.location);
                url.searchParams.set('activeTab', target === 'qna-section' ? 'qna' : 'review');
                window.history.pushState({}, '', url);
            });
        });

        // 페이지 로드 시 실행
        handleTabChange();

        // 해시가 변경될 때도 처리
        window.addEventListener('hashchange', handleTabChange);
    }

    initializeTabs();

    // =====================================================================
    // 7. 장바구니/구매 폼 전송 함수 (이전과 동일)
    window.submitForm = function (action) {
        const form = document.getElementById('productForm');

        // 기존 hidden input 제거
        ['items[0].modelId', 'items[0].count'].forEach(name => {
            const old = form.querySelector(`input[name="${name}"]`);
            if (old) old.remove();
        });

        const selectedModelId = modelSelect?.value;
        const count = countInput?.value;

        if (!selectedModelId || !count || parseInt(count) === 0) {
            alert("옵션과 수량을 정확히 선택해주세요.");
            return;
        }

        // hidden input 추가
        const modelInput = document.createElement("input");
        modelInput.type = "hidden";
        modelInput.name = "items[0].modelId";
        modelInput.value = selectedModelId;
        form.appendChild(modelInput);

        const countInputHidden = document.createElement("input");
        countInputHidden.type = "hidden";
        countInputHidden.name = "items[0].count";
        countInputHidden.value = count;
        form.appendChild(countInputHidden);

        // 추천 로그 정보
        const productId = window.location.pathname.split("/").pop();

        const productInput = document.createElement("input");
        productInput.type = "hidden";
        productInput.name = "items[0].productId"; // 반드시 이 형태로
        productInput.value = productId;
        form.appendChild(productInput);

        const actionType = action === 'cart' ? 'CART' : 'BUY';
        const weight = actionType === 'CART' ? 3 : 5;

        // actionType hidden input 값 설정
        document.getElementById('actionType').value = action; // "cart" or "order"
        form.action = action === 'cart' ? "/cart/add" : "/order/now";

        // 추천 로그 전송 후 폼 제출
        fetch("/recommend/log", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                productId: productId,
                modelId: selectedModelId,
                count: count,
                actionType: actionType,
                weight: weight
            })
        })
        .then(() => {
            form.submit(); // Ajax 성공 후 제출
        })
        .catch(err => {
            console.error("추천 로그 전송 실패:", err);
            form.submit(); // 실패해도 제출은 진행
        });
    };

});

// 빠른 이동 스크롤 함수
function scrollToSection(sectionId) {
    // 플로팅 메뉴 닫기
    closeFloatingNav();

    if (sectionId === 'qna-section') {
        // Q&A 탭으로 직접 전환 (기존 함수 사용)
        switchTabDirect('qna-section');
        setTimeout(() => {
            const section = document.getElementById(sectionId);
            if (section) {
                section.scrollIntoView({
                    behavior: 'auto',
                    block: 'start',
                    inline: 'nearest'
                });
            }
        }, 100);
    } else if (sectionId === 'review-section') {
        // 리뷰 탭으로 직접 전환 (기존 함수 사용)
        switchTabDirect('review-section');
        setTimeout(() => {
            const section = document.getElementById(sectionId);
            if (section) {
                section.scrollIntoView({
                    behavior: 'auto',
                    block: 'start',
                    inline: 'nearest'
                });
            }
        }, 100);
    } else {
        // 상세정보 등 일반 섹션
        const section = document.getElementById(sectionId);
        if (section) {
            section.scrollIntoView({
                behavior: 'auto',
                block: 'start',
                inline: 'nearest'
            });
        }
    }
}

// 탭 직접 전환 함수 (기존 initializeTabs의 switchTab 로직 활용)
function switchTabDirect(targetId) {
    // 탭 버튼 활성화 상태 변경
    document.querySelectorAll('.tab-button').forEach(b => {
        b.classList.toggle('active', b.getAttribute('data-target') === targetId);
    });
    // 탭 컨텐츠 표시/숨김
    document.querySelectorAll('.tab-content-section').forEach(section => {
        section.style.display = section.id === targetId ? 'block' : 'none';
    });
}

// 맨 위로 스크롤
function scrollToTop() {
    closeFloatingNav();
    window.scrollTo({
        top: 0,
        behavior: 'auto'
    });
}

// 플로팅 네비게이션 토글
function toggleFloatingNav() {
    const menu = document.getElementById('floatingNavMenu');
    if (menu) {
        menu.classList.toggle('active');
    }
}

// 플로팅 네비게이션 닫기
function closeFloatingNav() {
    const menu = document.getElementById('floatingNavMenu');
    if (menu) {
        menu.classList.remove('active');
    }
}

// 페이지 클릭 시 플로팅 메뉴 닫기
document.addEventListener('click', function(event) {
    const floatingNav = document.getElementById('floatingNav');
    if (floatingNav && !floatingNav.contains(event.target)) {
        closeFloatingNav();
    }
});

// URL 해시 기반 탭 전환 및 스크롤 기능
function handleTabFromHash() {
    const hash = window.location.hash;
    if (hash) {
        let targetId = hash;
        
        // #qna-tab -> qna-section 변환 (기존 호환성)
        if (hash.includes('-tab')) {
            targetId = hash.replace('-tab', '-section');
        }
        
        const tabButton = document.querySelector(`.tab-button[data-target="${targetId.substring(1)}"]`);

        if (tabButton) {
            // 모든 탭 컨텐츠 숨기기
            document.querySelectorAll('.tab-content-section').forEach(section => {
                section.style.display = 'none';
            });

            // 모든 탭 버튼 비활성화
            document.querySelectorAll('.tab-button').forEach(button => {
                button.classList.remove('active');
            });

            // 선택된 탭 활성화
            tabButton.classList.add('active');
            const targetSection = document.querySelector(targetId);
            if (targetSection) {
                targetSection.style.display = 'block';

                // 스크롤 이동 (리뷰/QnA 등록/수정 후 스크롤)
                setTimeout(() => {
                    targetSection.scrollIntoView({ behavior: 'auto', block: 'start' });
                }, 100);
            }
        }
    }
}

// 페이지 로드 시 URL 해시 처리
document.addEventListener('DOMContentLoaded', handleTabFromHash);

// URL 해시 변경 시 처리
window.addEventListener('hashchange', handleTabFromHash);

// 탭 버튼 클릭 이벤트
document.querySelectorAll('.tab-button').forEach(button => {
    button.addEventListener('click', function() {
        const targetId = this.getAttribute('data-target');

        // 모든 탭 컨텐츠 숨기기
        document.querySelectorAll('.tab-content-section').forEach(section => {
            section.style.display = 'none';
        });

        // 모든 탭 버튼 비활성화
        document.querySelectorAll('.tab-button').forEach(button => {
            button.classList.remove('active');
        });

        // 선택된 탭 활성화
        this.classList.add('active');
        const targetSection = document.getElementById(targetId);
        if (targetSection) {
            targetSection.style.display = 'block';

            // URL 해시 업데이트 (#qna-section -> #qna-tab)
            const hash = '#' + targetId.replace('-section', '-tab');
            history.pushState(null, '', hash);

            // 스크롤 이동
            targetSection.scrollIntoView({ behavior: 'auto', block: 'start' });
        }
    });
});

window.addEventListener('load', function() {
    setTimeout(function() {
        const params = new URLSearchParams(window.location.search);
        const scrollTo = params.get('scrollTo');
        const activeTab = params.get('activeTab');
        if (scrollTo && activeTab === 'review') {
            switchTabDirect('review-section');
            setTimeout(() => {
                const el = document.getElementById('review-' + scrollTo);
                if (el) el.scrollIntoView({ behavior: 'auto', block: 'center' });
            }, 100);
        } else if (scrollTo && activeTab === 'qna') {
            switchTabDirect('qna-section');
            setTimeout(() => {
                const el = document.getElementById('qna-' + scrollTo);
                if (el) el.scrollIntoView({ behavior: 'auto', block: 'center' });
            }, 100);
        }
    }, 100);
});

