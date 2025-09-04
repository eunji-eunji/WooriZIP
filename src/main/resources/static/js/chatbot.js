// 챗봇 JavaScript
let isTyping = false;

// 페이지 로드 시 현재 시간 표시
document.addEventListener('DOMContentLoaded', function() {
    updateCurrentTime();
    setInterval(updateCurrentTime, 1000);
});

// 현재 시간 업데이트
function updateCurrentTime() {
    const now = new Date();
    const timeString = now.toLocaleTimeString('ko-KR', {
        hour: '2-digit',
        minute: '2-digit'
    });
    const timeElements = document.querySelectorAll('.message-time');
    if (timeElements.length > 0) {
        timeElements[timeElements.length - 1].textContent = timeString;
    }
}

// 키보드 이벤트 처리
function handleKeyPress(event) {
    if (event.key === 'Enter') {
        sendMessage();
    }
}

// 메시지 전송
function sendMessage(message = null) {
    const messageInput = document.getElementById('messageInput');
    const messageText = message || messageInput.value.trim();

    if (!messageText || isTyping) return;

    // 사용자 메시지 추가
    addMessage(messageText, 'user');

    // 입력창 초기화
    if (!message) {
        messageInput.value = '';
    }

    // 타이핑 표시
    showTypingIndicator();

    // 서버에 메시지 전송
    fetch('/api/chatbot/chat', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'message=' + encodeURIComponent(messageText)
    })
    .then(response => response.json())
    .then(data => {
        hideTypingIndicator();
        handleBotResponse(data);
    })
    .catch(error => {
        console.error('Error:', error);
        hideTypingIndicator();
        addMessage('죄송합니다. 오류가 발생했습니다. 다시 시도해 주세요.', 'bot');

        // 오류 발생 시 기본 제안사항 표시
        updateSuggestions(['자주하는질문', '카테고리 보여줘', '배송 안내 알려줘', '반품 정책 알려줘', '결제 방법 알려줘', '이벤트 정보 알려줘']);
    });
}

// 봇 응답 처리
function handleBotResponse(response) {
    const { message, type, products, link, suggestions } = response;

    let responseContent = message;

    if (type === 'product_list' && products && products.length > 0) {
        responseContent += '<div class="product-list">';
        products.forEach(product => {
            responseContent += `
                <div class="product-card">
                    <h4>${product.name}</h4>
                    <p>${product.description || '상품 설명'}</p>
                    <p class="price">${formatPrice(product.price)}원</p>
                    <a href="/products/${product.id}" class="product-link">상품 보기</a>
                </div>
            `;
        });
        responseContent += '</div>';
    }

    if (link) {
        let linkText = "상품 목록 보기";
        if (link === "/event") {
            linkText = "공지/이벤트 보기";
        }
        responseContent += `<br><a href="${link}" class="chat-link">${linkText}</a>`;
    }

    addMessage(responseContent, 'bot');

    // 서브카테고리 타입들도 처리
    if (type === 'product_list' ||
        type === 'category_selection' ||
        type === 'furniture_subcategory' ||
        type === 'lighting_subcategory' ||
        type === 'fabric_subcategory' ||
        type === 'storage_subcategory' ||
        type === 'kitchen_subcategory' ||
        type === 'daily_subcategory' ||
        type === 'interior_subcategory') {
        addBackToSuggestionsButton();
    }

    if (suggestions && suggestions.length > 0) {
        updateSuggestions(suggestions);
    }

    // 뒤로가기 버튼이 포함된 경우 기존 뒤로가기 버튼 제거
    if (suggestions && suggestions.includes('뒤로가기')) {
        const existingBackBtn = document.getElementById('backToSuggestionsBtn');
        if (existingBackBtn) {
            existingBackBtn.remove();
        }
    }
}


// 메시지 추가
function addMessage(content, sender) {
    const chatMessages = document.getElementById('chatMessages');
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${sender}-message`;

    const now = new Date();
    const timeString = now.toLocaleTimeString('ko-KR', {
        hour: '2-digit',
        minute: '2-digit'
    });

    messageDiv.innerHTML = `
        <div class="message-content">${content}</div>
        <div class="message-time">${timeString}</div>
    `;

    chatMessages.appendChild(messageDiv);
    scrollToBottom();
}

// 타이핑 표시
function showTypingIndicator() {
    if (isTyping) return;

    isTyping = true;
    const chatMessages = document.getElementById('chatMessages');
    const typingDiv = document.createElement('div');
    typingDiv.className = 'message bot-message';
    typingDiv.id = 'typing-indicator';

    typingDiv.innerHTML = `
        <div class="typing-indicator">
            <div class="typing-dot"></div>
            <div class="typing-dot"></div>
            <div class="typing-dot"></div>
        </div>
    `;

    chatMessages.appendChild(typingDiv);
    scrollToBottom();
}

// 타이핑 숨기기
function hideTypingIndicator() {
    isTyping = false;
    const typingIndicator = document.getElementById('typing-indicator');
    if (typingIndicator) {
        typingIndicator.remove();
    }
    // 추가 안전장치: 3초 후에도 타이핑 인디케이터가 남아있으면 강제 제거
    setTimeout(() => {
        const remainingIndicator = document.getElementById('typing-indicator');
        if (remainingIndicator) {
            remainingIndicator.remove();
            isTyping = false;
        }
    }, 3000);
}

// 제안사항 업데이트
function updateSuggestions(suggestions) {
    const suggestionsContainer = document.getElementById('suggestions');
    suggestionsContainer.innerHTML = '';

    suggestions.forEach(suggestion => {
        const button = document.createElement('button');
        button.className = 'suggestion-btn';

        // 뒤로가기 버튼인 경우 특별한 스타일 적용
        if (suggestion === '뒤로가기') {
            button.className = 'suggestion-btn back-btn';
        }

        button.textContent = suggestion;
        button.onclick = () => sendMessage(suggestion);
        suggestionsContainer.appendChild(button);
    });
}

// 스크롤을 맨 아래로
function scrollToBottom() {
    const chatMessages = document.getElementById('chatMessages');
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

// 가격 포맷팅
function formatPrice(price) {
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

// 제안사항 버튼 클릭 이벤트
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('suggestion-btn')) {
        const message = e.target.textContent;
        sendMessage(message);
    }
});

function addBackToSuggestionsButton() {
    // 이미 버튼이 있으면 중복 추가하지 않음
    if (document.getElementById('backToSuggestionsBtn')) return;

    const btn = document.createElement('button');
    btn.id = 'backToSuggestionsBtn';
    btn.className = 'suggestion-btn';
    btn.textContent = '이전 질문 목록';
    btn.onclick = () => {
        fetch('/api/chatbot/suggestions')
            .then(response => response.json())
            .then(data => {
                const suggestionsDiv = document.getElementById('suggestions');
                suggestionsDiv.innerHTML = ''; // 기존 버튼 초기화

                data.forEach(suggestion => {
                    const btn = document.createElement('button');
                    btn.className = 'suggestion-btn';
                    btn.textContent = suggestion;
                    btn.onclick = () => sendMessage(suggestion);
                    suggestionsDiv.appendChild(btn);
                });

                document.getElementById('backToSuggestionsBtn').remove();
            });
    };

    const suggestionsDiv = document.getElementById('suggestions');
    suggestionsDiv.appendChild(btn);
}