---
name: verify-screen-on-device
description: 구현된 Compose 화면을 Galaxy Z Flip3(R3CT50PWVHA)에서 실행하고 Figma 스펙과 비교 검증한다. device-qa 에이전트가 사용한다.
---

# 기기 화면 검증

## 환경
- 기기: Galaxy Z Flip3 (R3CT50PWVHA)
- 패키지명: `com.ax.assignment`
- 빌드 명령:
  ```bash
  export JAVA_HOME="/c/Users/mojis/AppData/Local/Programs/Android Studio/jbr"
  export PATH="$JAVA_HOME/bin:$PATH"
  ```

## 실행 절차

### Step 1. 빌드 & 설치
```bash
export JAVA_HOME="/c/Users/mojis/AppData/Local/Programs/Android Studio/jbr"
export PATH="$JAVA_HOME/bin:$PATH"
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
