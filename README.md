# WooriZip CSS 테마 가이드 v2.0

## 개요

WooriZip 프로젝트는 현대적이고 확장 가능한 디자인 시스템을 위한 CSS 테마를 제공합니다. 이 테마는 CSS 변수를 사용하여 색상, 간격, 타이포그래피, 컴포넌트 등을 체계적으로 관리하며, Google Fonts와 최신 CSS 기능을 활용합니다.

## 파일 구조

```
static/css/
├── theme.css          # 메인 테마 정의 (CSS 변수, 컴포넌트 스타일)
├── main_style.css     # 레이아웃 및 전역 스타일
└── THEME_GUIDE.md     # 이 가이드 문서
```

## 색상 팔레트

### Primary Colors
- `--primary-brown: #B17457` - 메인 브라운 (버튼, 강조)
- `--primary-brown-light: #C48A6F` - 밝은 브라운 (호버 상태)
- `--primary-brown-dark: #9A6347` - 어두운 브라운 (활성 상태)
- `--primary-cream: #F9F7F0` - 배경 크림 (보조 배경)
- `--primary-cream-light: #FDFCF8` - 밝은 크림 (그라데이션)
- `--primary-cream-dark: #F0EDE0` - 어두운 크림 (호버 상태)

### Secondary Colors
- `--secondary-gold: #D4AF37` - 골드 (강조, 특별 요소)
- `--secondary-sage: #9CAF88` - 세이지 그린 (자연, 평화)
- `--secondary-navy: #2C3E50` - 네이비 (신뢰, 안정)

### Text Colors
- `--text-dark: #2C2C2C` - 메인 텍스트 (제목, 본문)
- `--text-medium: #5A5A5A` - 보조 텍스트 (설명, 캡션)
- `--text-light: #8A8A8A` - 연한 텍스트 (플레이스홀더)
- `--text-muted: #B0B0B0` - 비활성 텍스트 (비활성화된 요소)

### Status Colors
- `--success: #10B981` - 성공 (완료, 확인)
- `--success-light: #D1FAE5` - 밝은 성공 (배경)
- `--error: #EF4444` - 오류 (경고, 삭제)
- `--error-light: #FEE2E2` - 밝은 오류 (배경)
- `--warning: #F59E0B` - 경고 (주의)
- `--warning-light: #FEF3C7` - 밝은 경고 (배경)
- `--info: #3B82F6` - 정보 (알림)
- `--info-light: #DBEAFE` - 밝은 정보 (배경)

## 타이포그래피

### Font Families
- `--font-family: 'Inter', sans-serif` - 기본 폰트 (본문, UI)
- `--font-family-display: 'Playfair Display', serif` - 제목 폰트 (헤딩, 로고)

### Font Sizes
- `--font-size-xs: 12px` - 매우 작은 텍스트
- `--font-size-sm: 14px` - 작은 텍스트
- `--font-size-base: 16px` - 기본 텍스트
- `--font-size-lg: 18px` - 큰 텍스트
- `--font-size-xl: 20px` - 매우 큰 텍스트
- `--font-size-2xl: 24px` - 제목 4
- `--font-size-3xl: 30px` - 제목 3
- `--font-size-4xl: 36px` - 제목 2
- `--font-size-5xl: 48px` - 제목 1
- `--font-size-6xl: 60px` - 히어로 제목

### Font Weights
- `--font-light: 300` - 가벼운 폰트
- `--font-normal: 400` - 기본 폰트
- `--font-medium: 500` - 중간 폰트
- `--font-semibold: 600` - 세미볼드
- `--font-bold: 700` - 볼드

### Line Heights
- `--leading-none: 1` - 기본
- `--leading-tight: 1.25` - 타이트
- `--leading-snug: 1.375` - 스너그
- `--leading-normal: 1.5` - 일반
- `--leading-relaxed: 1.625` - 여유
- `--leading-loose: 2` - 느슨

## 간격 (Spacing)

- `--spacing-0: 0` - 없음
- `--spacing-1: 4px` - 매우 작은 간격
- `--spacing-2: 8px` - 작은 간격
- `--spacing-3: 12px` - 작은-중간 간격
- `--spacing-4: 16px` - 기본 간격
- `--spacing-5: 20px` - 중간 간격
- `--spacing-6: 24px` - 큰 간격
- `--spacing-8: 32px` - 매우 큰 간격
- `--spacing-10: 40px` - 최대 간격
- `--spacing-12: 48px` - 섹션 간격
- `--spacing-16: 64px` - 큰 섹션 간격
- `--spacing-20: 80px` - 최대 섹션 간격
- `--spacing-24: 96px` - 히어로 간격

## 컴포넌트

### 버튼 (Buttons)

```html
<!-- 기본 버튼 -->
<button class="btn btn-primary">기본 버튼</button>
<button class="btn btn-secondary">보조 버튼</button>
<button class="btn btn-outline">아웃라인 버튼</button>
<button class="btn btn-ghost">고스트 버튼</button>

<!-- 크기 변형 -->
<button class="btn btn-primary btn-sm">작은 버튼</button>
<button class="btn btn-primary btn-lg">큰 버튼</button>
<button class="btn btn-primary btn-xl">매우 큰 버튼</button>

<!-- 상태 -->
<button class="btn btn-primary" disabled>비활성 버튼</button>
<button class="btn btn-primary btn-loading">로딩 버튼</button>
```

### 폼 (Forms)

```html
<div class="form-group">
    <label class="form-label" for="email">이메일</label>
    <input type="email" id="email" class="form-input" placeholder="이메일을 입력하세요">
    <div class="form-error">이메일 형식이 올바르지 않습니다.</div>
</div>

<div class="form-group">
    <label class="form-label" for="message">메시지</label>
    <textarea id="message" class="form-textarea" placeholder="메시지를 입력하세요"></textarea>
    <div class="form-help">최대 500자까지 입력 가능합니다.</div>
</div>

<div class="form-group">
    <label class="form-label" for="category">카테고리</label>
    <select id="category" class="form-select">
        <option value="">선택하세요</option>
        <option value="1">카테고리 1</option>
        <option value="2">카테고리 2</option>
    </select>
</div>
```

### 카드 (Cards)

```html
<div class="card">
    <div class="card-header">
        <h3 class="card-title">카드 제목</h3>
        <p class="card-subtitle">카드 부제목</p>
    </div>
    <div class="card-body">
        <p>카드 내용입니다.</p>
    </div>
    <div class="card-footer">
        <button class="btn btn-primary">액션</button>
    </div>
</div>
```

### 알림 (Alerts)

```html
<div class="alert alert-success">
    <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
    </svg>
    성공 메시지입니다.
</div>

<div class="alert alert-error">
    <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"></path>
    </svg>
    오류 메시지입니다.
</div>

<div class="alert alert-warning">
    <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"></path>
    </svg>
    경고 메시지입니다.
</div>

<div class="alert alert-info">
    <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"></path>
    </svg>
    정보 메시지입니다.
</div>
```

### 배지 (Badges)

```html
<span class="badge badge-primary">기본</span>
<span class="badge badge-secondary">보조</span>
<span class="badge badge-success">성공</span>
<span class="badge badge-error">오류</span>
<span class="badge badge-warning">경고</span>
<span class="badge badge-info">정보</span>
```

## 레이아웃

### 컨테이너 (Containers)

```html
<div class="container">기본 컨테이너 (최대 1200px)</div>
<div class="container-sm">작은 컨테이너 (최대 640px)</div>
<div class="container-lg">큰 컨테이너 (최대 1400px)</div>
<div class="container-xl">매우 큰 컨테이너 (최대 1600px)</div>
```

### 그리드 (Grid)

```html
<div class="row">
    <div class="col-6">6칸</div>
    <div class="col-6">6칸</div>
</div>

<div class="row">
    <div class="col-4">4칸</div>
    <div class="col-4">4칸</div>
    <div class="col-4">4칸</div>
</div>

<div class="row">
    <div class="col-3">3칸</div>
    <div class="col-6">6칸</div>
    <div class="col-3">3칸</div>
</div>
```

### 사이드바 레이아웃 (Sidebar Layout)

```html
<div class="sidebar-layout">
    <main class="sidebar-main">
        <!-- 메인 콘텐츠 -->
    </main>
    <aside class="sidebar-aside">
        <!-- 사이드바 콘텐츠 -->
    </aside>
</div>
```

### 히어로 섹션 (Hero Section)

```html
<section class="hero">
    <div class="hero-content">
        <h1 class="hero-title">환영합니다</h1>
        <p class="hero-subtitle">아름다운 인테리어로 당신의 공간을 완성하세요</p>
        <div class="hero-actions">
            <button class="btn btn-primary btn-lg">시작하기</button>
            <button class="btn btn-outline btn-lg">더 알아보기</button>
        </div>
    </div>
</section>
```

## 유틸리티 클래스

### 텍스트 정렬
- `.text-left` - 왼쪽 정렬
- `.text-center` - 가운데 정렬
- `.text-right` - 오른쪽 정렬
- `.text-justify` - 양쪽 정렬

### 색상
- `.text-primary` - 주요 색상 텍스트
- `.text-secondary` - 보조 색상 텍스트
- `.text-light` - 연한 색상 텍스트
- `.text-muted` - 비활성 색상 텍스트
- `.text-success` - 성공 색상 텍스트
- `.text-error` - 오류 색상 텍스트
- `.text-warning` - 경고 색상 텍스트
- `.text-info` - 정보 색상 텍스트

### 배경
- `.bg-primary` - 주요 색상 배경
- `.bg-secondary` - 보조 크림 배경
- `.bg-light` - 보조 크림 배경
- `.bg-gray` - 회색 배경
- `.bg-white` - 흰색 배경
- `.bg-dark` - 어두운 배경

### 마진 (Margin)
- `.m-0` ~ `.m-12` - 전체 마진
- `.mt-0` ~ `.mt-12` - 상단 마진
- `.mb-0` ~ `.mb-12` - 하단 마진
- `.ml-0` ~ `.ml-12` - 왼쪽 마진
- `.mr-0` ~ `.mr-12` - 오른쪽 마진

### 패딩 (Padding)
- `.p-0` ~ `.p-12` - 전체 패딩
- `.pt-0` ~ `.pt-12` - 상단 패딩
- `.pb-0` ~ `.pb-12` - 하단 패딩
- `.pl-0` ~ `.pl-12` - 왼쪽 패딩
- `.pr-0` ~ `.pr-12` - 오른쪽 패딩

### 디스플레이
- `.d-none` - 숨김
- `.d-block` - 블록
- `.d-inline` - 인라인
- `.d-inline-block` - 인라인 블록
- `.d-flex` - 플렉스
- `.d-inline-flex` - 인라인 플렉스
- `.d-grid` - 그리드
- `.d-table` - 테이블
- `.d-table-cell` - 테이블 셀

### 플렉스 정렬
- `.flex-row` - 가로 방향
- `.flex-col` - 세로 방향
- `.flex-wrap` - 줄바꿈
- `.flex-nowrap` - 줄바꿈 안함
- `.flex-1` - 1배 확장
- `.flex-auto` - 자동 확장
- `.flex-initial` - 초기 크기
- `.flex-none` - 크기 고정

- `.justify-start` - 시작 정렬
- `.justify-end` - 끝 정렬
- `.justify-center` - 가운데 정렬
- `.justify-between` - 양쪽 정렬
- `.justify-around` - 주변 정렬
- `.justify-evenly` - 균등 정렬

- `.items-start` - 위 정렬
- `.items-end` - 아래 정렬
- `.items-center` - 세로 가운데 정렬
- `.items-baseline` - 기준선 정렬
- `.items-stretch` - 늘리기

### 너비 & 높이
- `.w-0` - 너비 0
- `.w-full` - 전체 너비
- `.w-auto` - 자동 너비
- `.w-1/2` - 50% 너비
- `.w-1/3` - 33.33% 너비
- `.w-2/3` - 66.67% 너비
- `.w-1/4` - 25% 너비
- `.w-3/4` - 75% 너비

- `.h-0` - 높이 0
- `.h-full` - 전체 높이
- `.h-auto` - 자동 높이
- `.h-screen` - 화면 높이

### 테두리 반경
- `.rounded-none` - 없음
- `.rounded-sm` - 작은
- `.rounded` / `.rounded-md` - 기본
- `.rounded-lg` - 큰
- `.rounded-xl` - 매우 큰
- `.rounded-2xl` - 최대
- `.rounded-full` - 원형

### 그림자
- `.shadow-none` - 없음
- `.shadow-sm` - 작은
- `.shadow` / `.shadow-md` - 기본
- `.shadow-lg` - 큰
- `.shadow-xl` - 매우 큰
- `.shadow-2xl` - 최대

### 위치
- `.relative` - 상대 위치
- `.absolute` - 절대 위치
- `.fixed` - 고정 위치
- `.sticky` - 고정 위치 (스크롤)

### 오버플로우
- `.overflow-auto` - 자동 스크롤
- `.overflow-hidden` - 숨김
- `.overflow-visible` - 보임
- `.overflow-scroll` - 스크롤

### 커서
- `.cursor-auto` - 자동
- `.cursor-pointer` - 포인터
- `.cursor-not-allowed` - 금지

### 사용자 선택
- `.select-none` - 선택 불가
- `.select-text` - 텍스트 선택
- `.select-all` - 전체 선택

## 반응형 디자인

### 브레이크포인트
- `1024px` - 데스크톱/태블릿
- `768px` - 태블릿/모바일
- `480px` - 모바일

### 모바일 최적화
모바일에서는:
- 그리드가 세로로 변경
- 버튼이 전체 너비
- 폰트 크기 조정
- 간격 축소
- 네비게이션이 햄버거 메뉴로 변경

## 애니메이션

### 기본 애니메이션
- `.fade-in` - 페이드 인
- `.slide-in-up` - 아래에서 위로 슬라이드
- `.slide-in-down` - 위에서 아래로 슬라이드
- `.slide-in-left` - 왼쪽에서 오른쪽으로 슬라이드
- `.slide-in-right` - 오른쪽에서 왼쪽으로 슬라이드
- `.scale-in` - 크기 확대

### 트랜지션
- `--transition-none: none` - 없음
- `--transition-fast: 150ms ease` - 빠른 전환
- `--transition-normal: 250ms ease` - 기본 전환
- `--transition-slow: 350ms ease` - 느린 전환
- `--transition-slower: 500ms ease` - 매우 느린 전환

## 사용 예시

### 메인 페이지
```html
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WooriZip</title>
    <link rel="stylesheet" href="/css/main_style.css">
</head>
<body>
    <header class="header">
        <nav class="nav">
            <a href="/" class="nav-logo">WooriZip</a>
            <button class="nav-toggle">
                <span></span>
                <span></span>
                <span></span>
            </button>
            <ul class="nav-menu">
                <li><a href="/" class="nav-link active">홈</a></li>
                <li><a href="/products" class="nav-link">제품</a></li>
                <li><a href="/about" class="nav-link">소개</a></li>
                <li><a href="/contact" class="nav-link">연락처</a></li>
            </ul>
        </nav>
    </header>

    <main>
        <section class="hero">
            <div class="hero-content">
                <h1 class="hero-title">아름다운 인테리어</h1>
                <p class="hero-subtitle">당신의 공간을 완성하는 최고의 선택</p>
                <div class="hero-actions">
                    <button class="btn btn-primary btn-lg">시작하기</button>
                    <button class="btn btn-outline btn-lg">더 알아보기</button>
                </div>
            </div>
        </section>

        <div class="container">
            <div class="row">
                <div class="col-4">
                    <div class="card">
                        <div class="card-header">
                            <h3 class="card-title">제품 1</h3>
                            <p class="card-subtitle">최고의 품질</p>
                        </div>
                        <div class="card-body">
                            <p>아름다운 디자인과 기능성을 모두 갖춘 제품입니다.</p>
                        </div>
                        <div class="card-footer">
                            <button class="btn btn-primary">자세히 보기</button>
                        </div>
                    </div>
                </div>
                <!-- 더 많은 제품들... -->
            </div>
        </div>
    </main>

    <footer class="footer">
        <div class="footer-content">
            <div class="footer-section">
                <h3>WooriZip</h3>
                <p>아름다운 인테리어로 당신의 공간을 완성하세요.</p>
            </div>
            <div class="footer-section">
                <h3>연락처</h3>
                <p>이메일: info@woorizip.com</p>
                <p>전화: 02-1234-5678</p>
            </div>
        </div>
        <div class="footer-bottom">
            <p>&copy; 2024 WooriZip. All rights reserved.</p>
        </div>
    </footer>
</body>
</html>
```

### 회원가입 폼
```html
<div class="container-sm">
    <div class="page-header">
        <div class="page-header-content">
            <h1 class="page-title">회원가입</h1>
            <p class="page-subtitle">WooriZip의 멤버가 되어 특별한 혜택을 받아보세요</p>
        </div>
    </div>
    
    <form class="card">
        <div class="card-body">
            <div class="form-group">
                <label class="form-label" for="email">이메일</label>
                <input type="email" id="email" class="form-input" required>
                <div class="form-help">가입 후 이메일 인증이 필요합니다.</div>
            </div>
            
            <div class="form-group">
                <label class="form-label" for="password">비밀번호</label>
                <input type="password" id="password" class="form-input" required>
                <div class="form-help">8자 이상, 영문/숫자/특수문자 포함</div>
            </div>
            
            <div class="form-group">
                <label class="form-label" for="confirm-password">비밀번호 확인</label>
                <input type="password" id="confirm-password" class="form-input" required>
            </div>
            
            <button type="submit" class="btn btn-primary btn-lg w-full">회원가입</button>
        </div>
    </form>
</div>
```

### 제품 목록
```html
<div class="container">
    <div class="page-header">
        <div class="page-header-content">
            <h1 class="page-title">제품 목록</h1>
            <p class="page-subtitle">다양한 인테리어 제품을 만나보세요</p>
        </div>
    </div>
    
    <div class="row">
        <div class="col-4">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">모던 소파</h3>
                    <p class="card-subtitle">편안함과 스타일의 조화</p>
                </div>
                <div class="card-body">
                    <p>현대적인 디자인과 최고의 편안함을 제공하는 소파입니다.</p>
                    <div class="mt-4">
                        <span class="badge badge-primary">인기</span>
                        <span class="badge badge-success">재고있음</span>
                    </div>
                </div>
                <div class="card-footer">
                    <button class="btn btn-primary">자세히 보기</button>
                    <button class="btn btn-outline">장바구니</button>
                </div>
            </div>
        </div>
        <!-- 더 많은 제품들... -->
    </div>
</div>
```

## 커스터마이징

### 색상 변경
`theme.css`의 `:root` 섹션에서 CSS 변수를 수정하여 색상을 변경할 수 있습니다.

```css
:root {
    --primary-brown: #새로운색상;
    --primary-cream: #새로운색상;
    /* ... */
}
```

### 새로운 컴포넌트 추가
`theme.css`에 새로운 컴포넌트 스타일을 추가할 수 있습니다.

```css
.custom-component {
    background-color: var(--bg-light);
    border: 1px solid var(--border-medium);
    border-radius: var(--radius-md);
    padding: var(--spacing-6);
    box-shadow: var(--shadow-sm);
    transition: all var(--transition-normal);
}

.custom-component:hover {
    box-shadow: var(--shadow-md);
    transform: translateY(-2px);
}
```

### 다크 모드 지원
CSS 변수를 활용하여 다크 모드를 쉽게 구현할 수 있습니다.

```css
@media (prefers-color-scheme: dark) {
    :root {
        --bg-white: #1a1a1a;
        --text-dark: #ffffff;
        --text-medium: #cccccc;
        --border-light: #333333;
        /* ... */
    }
}
```

## 모범 사례

1. **CSS 변수 사용**: 하드코딩된 값 대신 CSS 변수를 사용하세요.
2. **일관된 간격**: `--spacing-*` 변수를 사용하여 일관된 간격을 유지하세요.
3. **반응형 고려**: 모바일 환경을 고려하여 디자인하세요.
4. **접근성**: 색상 대비와 키보드 네비게이션을 고려하세요.
5. **성능**: 불필요한 중첩과 복잡한 선택자를 피하세요.
6. **시맨틱 HTML**: 의미있는 HTML 구조를 사용하세요.
7. **점진적 향상**: 기본 기능이 작동하는 상태에서 향상시키세요.

## 브라우저 지원

- Chrome (최신 2개 버전)
- Firefox (최신 2개 버전)
- Safari (최신 2개 버전)
- Edge (최신 2개 버전)
- 모바일 브라우저 (iOS Safari, Chrome Mobile)

## 문제 해결

### CSS 변수가 작동하지 않는 경우
1. 브라우저 지원 확인
2. CSS 파일 로드 순서 확인
3. 변수명 철자 확인
4. `:root` 선택자 확인

### 스타일이 적용되지 않는 경우
1. CSS 파일 경로 확인
2. 클래스명 철자 확인
3. CSS 우선순위 확인
4. 개발자 도구로 스타일 확인

### 반응형이 작동하지 않는 경우
1. 뷰포트 메타 태그 확인
2. 미디어 쿼리 브레이크포인트 확인
3. CSS 캐시 클리어
4. 모바일 디바이스에서 테스트

### 폰트가 로드되지 않는 경우
1. 인터넷 연결 확인
2. Google Fonts URL 확인
3. 폰트 로딩 시간 대기
4. 폴백 폰트 확인

## 성능 최적화

1. **CSS 압축**: 프로덕션에서는 CSS를 압축하세요.
2. **불필요한 스타일 제거**: 사용하지 않는 스타일을 제거하세요.
3. **폰트 최적화**: 필요한 폰트 웨이트만 로드하세요.
4. **캐싱**: 적절한 캐시 헤더를 설정하세요.
5. **CDN 사용**: 정적 파일을 CDN에서 제공하세요.

## 업데이트 로그

### v2.0 (2024-01-XX)
- Google Fonts (Inter, Playfair Display) 추가
- 색상 팔레트 확장 및 개선
- 새로운 유틸리티 클래스 추가
- 반응형 디자인 개선
- 접근성 향상
- 애니메이션 추가
- 컴포넌트 스타일 개선
- 문서화 개선 