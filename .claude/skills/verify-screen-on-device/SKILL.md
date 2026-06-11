---
name: verify-screen-on-device
description: 구현된 Compose 화면을 Galaxy Z Flip3(R3CT50PWVHA)에서 실행하고 Figma 스펙과 비교 검증한다. device-qa 에이전트가 사용한다.
---

# 기기 화면 검증

## 환경
- 기기: Galaxy Z Flip3 (R3CT50PWVHA)
- 패키지명: `com.ax.assignment`
- JAVA_HOME은 전역 env에 설정됨 — 빌드 명령에 export 접두사 금지
  (붙이면 권한 allowlist 미매칭으로 매번 승인 프롬프트 발생)

## 실행 절차

### Step 1. 빌드 & 설치
```bash
./gradlew installDebug 2>&1 | tail -5
```
빌드 실패 시 오케스트레이터에 오류 내용을 반환하고 중단한다.

### Step 2. 앱 실행
```
mcp__mobile-mcp__mobile_launch_app(
  device="R3CT50PWVHA",
  packageName="com.ax.assignment"
)
```

### Step 3. 화면 이동
홈 화면 이외의 화면은 UI 요소를 탭해서 이동한다:
```
mcp__mobile-mcp__mobile_list_elements_on_screen(device="R3CT50PWVHA")
# → 탭 대상 요소 확인 후:
mcp__mobile-mcp__mobile_click_on_screen_at_coordinates(
  device="R3CT50PWVHA", x=..., y=...
)
```

### Step 4. 스크린샷 촬영
```
mcp__mobile-mcp__mobile_take_screenshot(device="R3CT50PWVHA")
```
이미지를 `_workspace/{screenId}-device-screenshot.png`에 저장한다.

### Step 5. 스펙과 비교
`_workspace/figma-{screenId}-spec.md`를 읽고 항목별로 비교한다:

**비교 항목:**
- [ ] 레이아웃 구조 (Scaffold, TopBar, FAB 위치)
- [ ] 색상 (배경, 카드, 강조색)
- [ ] 타이포그래피 (크기, 굵기)
- [ ] 여백/패딩
- [ ] 빈 상태 / 로딩 상태 UI
- [ ] 텍스트 내용 (라벨, 플레이스홀더)

### Step 6. 보고서 작성
`_workspace/{screenId}-qa-report.md`:
```markdown
# {화면명} QA 보고서 (SCR-XX)
**날짜:** {오늘}
**빌드:** PASS/FAIL

## 항목별 검증
| 항목 | 기대 | 실제 | 판정 |
|------|------|------|------|
| 배경색 | Background(#F5F6FA) | ... | PASS/FAIL |

## 종합: PASS / FAIL ({n}개 항목 미충족)

## 수정 필요 사항
1. (우선순위 높음) ...
2. ...
```

## 크래시 확인
```
mcp__mobile-mcp__mobile_list_crashes(device="R3CT50PWVHA")
```
크래시가 있으면 로그를 확인하고 오케스트레이터에 보고한다.

## mobile-mcp 사용 규칙 (속도·정확도 — 2026-06-11 조사 기준)
- **좌표계**: 탭/스와이프 좌표는 항상 실디바이스 픽셀(1080×2640). 스크린샷은 클라이언트에서
  축소되므로 스크린샷에서 읽은 좌표는 `× (1080 / 이미지폭)` 환산 필수.
- **클릭은 요소 리스트 1순위**: `mobile_list_elements_on_screen`의 bounds(실픽셀) 중심을 탭.
  스크린샷 기반 좌표 추정은 최후 수단 (공식 위키 권장). 스크린샷은 시각 검증용으로만.
- **스와이프**: direction = 손가락 이동 방향 (down = 손가락 아래로 = 이전/위 콘텐츠 표시,
  up = 다음/아래 콘텐츠). `x`/`y`로 시작점, `distance`로 거리(px) 지정 — 휠/리스트 등
  특정 영역만 스크롤할 때 반드시 시작점을 그 영역 안에 지정.
- **키보드**: 입력 후 키보드가 하단 버튼을 가림 — `mobile_press_button(button="BACK")`으로
  닫은 뒤 탭. 입력+엔터는 `type_keys(submit=true)` 한 번으로.
- **한글 입력**: devicekit APK(`com.mobilenext.devicekit`) 설치됨 — `type_keys`로 한글 직접
  입력 가능. 미설치 기기는 ASCII만 됨.
- **스크린샷 400 에러** (알려진 issue #140, 세로 2640px 조건): 발생 시 세션 재시작 대신
  `mobile_save_screenshot`으로 파일 저장 후 Read로 읽어 우회.
- **스크린샷 "Not a valid PNG" 에러**: 기기 화면이 잠겼거나 꺼진 상태 — 도구 문제가 아님.
  `dumpsys power | grep mWakefulness`로 확인하고, PIN 잠금이면 사용자에게 해제 요청.
- **adb screencap은 디스플레이 지정 필수** (Z Flip3 = 폴더블, 커버스크린 포함 2개):
  지정 없으면 커버스크린(검은 화면)이 잡힐 수 있다.
  `adb shell screencap -p -d 4630947232161729154 //sdcard/x.png` (메인 디스플레이,
  ID 목록: `dumpsys SurfaceFlinger --display-id`). git-bash에선 `/sdcard`가 경로 변환되므로
  `//sdcard`로 쓸 것.
- **호출 최소화**: 화면당 스크린샷 1회 원칙. 상태 확인은 요소 리스트가 더 빠르고 토큰도 적음.
- **거래 입력은 스크립트 사용**: `python -X utf8 scripts/device_input.py add <금액> <메모> [카테고리]`
  (수입은 `--income`, 대량은 `bulk <json>`) — 순수 adb로 전체 플로우를 1회 호출에 처리.
  MCP 단계별 입력(~107초/건) 대비 **~13초/건**. 홈 화면에서 시작해야 함.
- **범용 드라이버 `scripts/ui.py`** (앱 무관, 좌표 0개): 텍스트/접근성라벨/클래스 셀렉터 +
  요소 출현 폴링(auto-wait). 새 화면/앱 자동화 = 이 드라이버 위에 플로우 함수만 추가.
  핵심 규칙 — ① uiautomator dump는 ~2.3초라 같은 화면에선 캐시 재사용(`keep=True`로 보존),
  ② 화면 전환 후엔 반드시 `u.gate(목적지 고유 마커)` — 전환 애니메이션 중 이전 화면 요소를
  매칭하는 경합 방지, ③ 한글은 devicekit 클립보드 브로드캐스트(`u.type`이 자동 분기),
  ④ 카테고리 선택처럼 "재탭=해제" UI는 현재 선택 확인 후 분기 (device_input.select_category 참고).
