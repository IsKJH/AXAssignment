# Session Notes — 2026-06-11 (발표 D-1)

## 현재 상태
- **완성**: 전 화면 × 최신 Figma 전수 대조, 풀 E2E 9/9 PASS, 픽셀 디테일 검수(사용자 검수 4라운드) 완료
- 상세 규칙/구조/인셋 규칙은 **CLAUDE.md가 최신** — 먼저 읽을 것
- main 브랜치 24+커밋, 워킹 트리 클린. 산출물: `_workspace/final-spec-*.md`, `final-qa-diff-report.md`, `full-app-qa-report.md`

## 6/10~11 디테일 검수에서 잡은 것 (사용자 스크린샷 기반)
1. 상태바 이중 인셋 → 전 화면 36dp 밀림 수정 (+하단 버튼 navBars 회귀까지 — 교훈: 상단/하단/시트 3종 검증)
2. AppTopBar 44→56dp (홈만 60dp — Figma 두 규격)
3. 도넛: SVG 실측(stroke 18, 내부 125+1dp 보더, 9.5dp 갭), 중앙 12sp/28sp
4. 토글 패딩 7→4dp, 툴팁 꼬리 정렬, 커스텀 카테고리 8색 자동 배정
5. 정기 거래 양방향 완성: 편집에서 신규 체크 → registerRecurring(12개월)
6. **도넛 프레스&스크럽** (신규 차별화): 누르면 카테고리명+%, 문지르면 실시간 전환+햅틱, 떼면 복귀

## 의도적 결정 (스펙과 다르거나 해석한 것)
- 카테고리 시트 키보드 자동 포커스 제거 — 사용자 선호 (SCR-06 텍스트와 다름)
- 초과 문구 "수입의 N원 초과하셨습니다" — 실제 UI 프레임(446:731) 기준
- % 반올림·최소보정 없음 — Figma 코멘트 확정 정책
- 6개월 내역 상단=실제 현재 주기 고정 — Figma 488:854 디자인 그대로

## 남은 작업 (코드 아님)
1. **git push** — 명시 요청 시에만 (`git push -u origin main`)
2. 시연 데이터 입력 + 잔여 테스트 거래(qaconv 등) 확인
3. `_trash/` 폴더 수동 삭제 (영상 프레임/구 리소스 보관함)

## 작업 팁 (다음 세션용)
- Figma: 노드ID만 있으면 get_design_context/get_screenshot(base64)로 직접 확인. 에셋이 SVG면 수치 정답
- 순간 UI(스플래시/토스트) 검증: `adb shell screenrecord --time-limit N //sdcard/x.mp4` → pull → cv2 프레임 추출
- 바텀시트/다이얼로그 터치: 스크린샷 좌표 ≠ 실좌표 — list_elements_on_screen 필수
- 사용자 비교 스크린샷 받으면 → Figma 메타데이터/SVG 실측으로 수치 검증하는 패턴이 최고 효율
