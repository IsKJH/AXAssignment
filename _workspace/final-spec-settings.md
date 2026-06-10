# AXAssignment 설정 섹션 + 인트로 화면 스펙

## 1. 요구사항/정책 전문 (SCR-07 기준)

### 화면 목적
- 정산 기준일(1~28일)을 설정. 변경 시 홈·통계 집계 즉시 재계산

### UI 요소
- **TopNavigation**: 뒤로가기(좌) + "시작일 설정"(중)
- 정산 기준일 설명 텍스트
- 기준일 드럼롤 피커 (1~28일)
- 저장/확인 버튼

### 정책
- **SET-001**: 기준일 1~28일만 선택 가능. 설정 화면 리스트에서만 진입
- **AC-012**: 저장 탭 → 확인 시 저장 및 홈·통계 즉시 재계산
- 이전 설정 값 있으면 이전 설정 값 기본 표시 (기준일 기본값: 1일)
- 29~31일 선택 불가

### 플로우
- 저장 확인 → 설정 화면 (변경 사항 저장)
- 뒤로가기 → 설정 화면 (변경 저장 안 함, 갱신 없음)

---

## 2. 설정 화면 UI 스펙 (SCR-07, 노드 368:531)

### 레이아웃 구조
```
Scaffold(
  statusBar: Material3 Status Bar (시간, Wi-Fi, Signal, Battery)
  topBar: TopNavigation(
    - 뒤로가기 버튼 (28px icon, rounded 10px)
    - 타이틀: "설정" (Pretendard Bold 20px, 중앙)
  )
  content: Column(
    - 환경설정 섹션
    - 고객 지원 섹션
    - 앱 정보 섹션
  )
  bottomNavigation: BottomNavigation(
    - 홈 (icon + "홈" label)
    - 통계 (icon + "통계" label)
    - 설정 (icon + "설정" label, ACTIVE)
  )
)
```

### 섹션별 상세 스펙

#### 2.1 환경설정 섹션
- **헤더**: "환경설정" (Pretendard Medium 14px, #272727)
- **컨테이너**: 배경 #FFFFFF, 모서리 8px 라운드
- **단일 항목**: "시작일 설정"
  - 좌측 아이콘: volume_down (28px, 회색)
  - 중앙 텍스트: "시작일 설정" (Pretendard Bold 16px, #272727)
  - 우측 값: "매달 25일" (Pretendard Medium 14px, #559aff - 브랜드 블루)
  - 우측 화살표: chevron_right (24px, 검은색)
  - 높이: 64px
  - 패딩: 좌우 16px, 상하 14px
  - 탭 가능 (네비게이션 링크)

#### 2.2 고객 지원 섹션
- **헤더**: "고객 지원" (Pretendard Medium 14px, #272727)
- **컨테이너**: 배경 #FFFFFF, 모서리 8px 라운드
- **항목들** (각 높이 52px):
  1. "리뷰를 남겨주세요"
     - 좌측 아이콘: kid_star (28px, 회색)
     - 텍스트: Pretendard Bold 16px, #272727
     - 우측 화살표: chevron_right (24px)
     - 상단 구분선: #DCDEE3, 1px
  
  2. "친구에게 앱 추천하기"
     - 좌측 아이콘: thumb_up (28px, 회색)
     - 텍스트: Pretendard Bold 16px, #272727
     - 우측 화살표: chevron_right (24px)
     - 상단 구분선: #DCDEE3, 1px
  
  3. "사용 방법"
     - 좌측 아이콘: help (28px, 회색)
     - 텍스트: Pretendard Bold 16px, #272727
     - 우측 화살표: chevron_right (24px)
     - 상단 구분선: #DCDEE3, 1px

#### 2.3 앱 정보 섹션
- **헤더**: "앱 정보" (Pretendard Medium 14px, #272727)
- **컨테이너**: 배경 #FFFFFF, 모서리 8px 라운드
- **항목**: "앱 버전"
  - 좌측 아이콘: error (28px, 회색)
  - 중앙 텍스트: "앱 버전" (Pretendard Bold 16px, #272727)
  - 우측 값: "1.00" (Pretendard Medium 14px, #666666 - 더 옅은 회색)
  - 높이: 52px
  - 상단 구분선: #DCDEE3, 1px

### 색상 매핑
| 요소 | Figma 색상 | 설명 |
|------|-----------|------|
| 배경 (세컨더리) | #EEEEEE | 스크롤 영역 배경 |
| 배경 (카드/흰색) | #FFFFFF | 리스트 아이템, 톱바 |
| 텍스트 (주) | #272727 | 헤더, 제목, 기본 텍스트 |
| 텍스트 (보조) | #AFAFAF | 설명, 가벼운 텍스트 |
| 텍스트 (더 옅음) | #666666 | "앱 버전" 값 |
| 텍스트 (브랜드) | #559AFF | "시작일 설정" 값 |
| 구분선 | #DCDEE3 | 항목 간 구분선 |
| 아이콘 (비활성) | #AFAFAF | 하단 내비게이션 미선택 |
| 아이콘 (활성) | #559AFF | 하단 내비게이션 "설정" |

### 타이포그래피
| 요소 | 폰트 | 크기 | 굵기 | 줄간격 |
|------|------|------|------|--------|
| 헤더 | Pretendard | 14px | Medium (500) | 1.5 |
| 제목 (톱바) | Pretendard | 20px | Bold (700) | 1.5 |
| 항목 텍스트 | Pretendard | 16px | Bold (700) | 1.5 |
| 값 텍스트 | Pretendard | 14px | Medium (500) | 1.5 |
| 라벨 텍스트 | Pretendard | 12px | Medium (500) | 1.5 |

---

## 3. 시작일 설정 화면 UI 스펙 (SCR-07, 노드 368:916)

### 레이아웃 구조
```
Scaffold(
  statusBar: Material3 Status Bar
  topBar: TopNavigation(
    - 뒤로가기 버튼 (28px icon, rounded 10px)
    - 타이틀: "시작일 설정" (Pretendard Bold 20px, 중앙)
  )
  content: Column(
    - 정산 시작일 설명
    - 드럼롤 피커 (스크롤 가능)
  )
  floatingButton: 확인 버튼 (하단 중앙)
)
```

### 상세 스펙

#### 3.1 설명 섹션
- **제목**: "정산 시작일" (Pretendard Bold 16px, #272727)
- **부제**: "매달 이 날짜를 시작으로 한 주기를 계산합니다." (Pretendard Regular 14px, #272727)
- **패딩**: 상단 24px, 좌우 16px
- **간격**: 제목과 부제 사이 4px

#### 3.2 드럼롤 피커 (Wheel Picker)
- **컨테이너**: 배경 #FFFFFF, 모서리 12px 라운드, 패딩 상하 16px
- **아이템 레이아웃**:
  - 각 아이템 높이: 50px (추정, 5개 가시 행)
  - 각 아이템 텍스트 정렬: 중앙
  - 간격: 8px
  
- **비선택 항목** (23일, 24일, 26일, 27일):
  - 폰트: Pretendard Regular 16px
  - 색상: #AFAFAF (회색)
  
- **선택된 항목** (25일, 현재값):
  - 배경: #EEF5FF (밝은 파란색)
  - 폰트: Pretendard Bold 18px
  - 색상: #559AFF (브랜드 파란색)
  - 상하 패딩: 12px
  - 수직 중앙 정렬

#### 3.3 확인 버튼
- **위치**: 하단 고정, 중앙 정렬
- **배경**: #272727 (검정)
- **텍스트**: "확인" (Pretendard Bold 16px, #FFFFFF - 흰색)
- **크기**: 폭 328px, 높이 48px
- **모서리**: 12px 라운드
- **패딩**: 상하 14px
- **하단 마진**: 15.64px (Safe Area 고려)
- **탭 액션**: 선택된 기준일 저장 → 설정 화면으로 이동

### 초기값
- 기본값: 25일 (또는 이전 설정값 있으면 그 값)
- 범위: 1~28일 (29~31일 선택 불가)

---

## 4. 인트로/스플래시 화면 스펙 (노드 368:1519)

### 레이아웃 구조
```
Column(
  horizontalAlignment: CENTER,
  verticalArrangement: CENTER,
  children: [
    // 로고 및 앱명 (중앙 상단)
    Column(
      items: [
        Box(배경: 파란색, icon: AX 로고)
        Text("AX 가계부")
      ]
    ),
    
    // 저작권 (하단)
    Text("COPYRIGHT 2026.AX 가계부 ALL RIGHTS RESERVED.")
  ]
)
```

### 상세 스펙

#### 4.1 배경
- **색상**: #FFFFFF (순백색)
- **크기**: 360px × 800px (전체 화면)

#### 4.2 로고 박스
- **위치**: 화면 중앙 (수평/수직)
- **배경**: #559AFF (브랜드 파란색)
- **크기**: 90px × 90px
- **모서리**: 16px 라운드
- **내부 아이콘**: 80px × 80px (Rectangle13 이미지, 중앙)

#### 4.3 앱명 텍스트
- **텍스트**: "AX 가계부"
- **폰트**: Pretendard Bold 18px
- **색상**: #020202 (거의 검정, 약간의 회색)
- **위치**: 로고 하단 약 60px 아래
- **정렬**: 중앙

#### 4.4 저작권 텍스트
- **텍스트**: "COPYRIGHT 2026.AX 가계부 ALL RIGHTS RESERVED."
- **폰트**: Pretendard Regular 12px
- **색상**: #020202
- **위치**: 화면 하단 50px 위
- **정렬**: 중앙
- **줄간격**: 1.5

### 색상 매핑
| 요소 | 색상 | Hex |
|------|------|-----|
| 배경 | 순백 | #FFFFFF |
| 로고 배경 | 브랜드 파란색 | #559AFF |
| 텍스트 | 거의 검정 | #020202 |

### 타이포그래피
| 요소 | 폰트 | 크기 | 굵기 | 줄간격 |
|------|------|------|------|--------|
| 앱명 | Pretendard | 18px | Bold (700) | 1.5 |
| 저작권 | Pretendard | 12px | Regular (400) | 1.5 |

---

## 5. 스크린샷 참조
- SCR-07 기준일 설정 스펙 문서: `screenshot-scr07-spec.png`
- 설정 화면 UI: `screenshot-settings.png`
- 시작일 설정 UI: `screenshot-start-day.png`
- 인트로 화면: `screenshot-intro.png`

---

## 6. 구현 체크리스트

### 설정 화면 (SCR-07)
- [ ] Status Bar 구성 (시간, Wi-Fi, Signal, Battery, Camera Cutout)
- [ ] TopNavigation 뒤로가기 + "설정" 타이틀
- [ ] 환경설정 섹션 (시작일 설정 항목, 브랜드 파란색 값)
- [ ] 고객 지원 섹션 (3개 항목, 아이콘 + 텍스트 + 화살표)
- [ ] 앱 정보 섹션 (앱 버전 항목)
- [ ] BottomNavigation (홈, 통계, 설정 - 설정 활성)
- [ ] 색상 정확도 (#559AFF, #AFAFAF, #DCDEE3)
- [ ] 타이포그래피 (Pretendard 정확한 크기, 굵기)

### 시작일 설정 화면 (SCR-07)
- [ ] TopNavigation "시작일 설정"
- [ ] 설명 텍스트 (제목 + 부제)
- [ ] 드럼롤 피커 (1~28일, 5개 가시)
- [ ] 선택 항목 하이라이트 (#EEF5FF 배경, #559AFF 텍스트)
- [ ] 확인 버튼 (검정 배경, 하단 고정)
- [ ] 저장 로직 (AC-012)

### 인트로 화면
- [ ] 순백 배경
- [ ] 파란색 로고 박스 (90×90, 모서리 16px)
- [ ] 로고 아이콘 (80×80, 중앙)
- [ ] "AX 가계부" 텍스트
- [ ] 저작권 텍스트 (하단)
- [ ] 중앙 정렬 및 위치

---

## 7. 디자인 토큰 (core/theme/Color.kt 기준)
```kotlin
// 배경
val BackgroundSecondary = Color(0xFFEEEEEE)
val BackgroundCardSecondary = Color(0xFFFFFFFF)

// 텍스트
val TextDefault = Color(0xFF272727)
val TextBrand = Color(0xFF559AFF)
val TextDescription = Color(0xFFAFAFAF)
val TextSubtle = Color(0xFF666666)
val TextInverted = Color(0xFFFFFFFF)

// 경계
val BorderDefault = Color(0xFFDCDEE3)

// 네비게이션
val NavigationOn = Color(0xFF559AFF)
val NavigationOff = Color(0xFFAFAFAF)
val NavigationFill = Color(0xFFFFFFFF)

// 브랜드
val BrandPrimary = Color(0xFF559AFF)
val BrandLight = Color(0xFFEEF5FF)
```

