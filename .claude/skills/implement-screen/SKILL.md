---
name: implement-screen
description: AXAssignment 가계부 앱의 화면을 Figma → Compose → 기기 검증 파이프라인으로 구현한다. 화면 구현을 요청받으면 반드시 이 스킬을 사용하라. 트리거: '홈 화면 구현', '거래 추가 화면 만들어줘', '통계 화면 만들어줘', 'SCR-02 구현', '화면 구현', '다시 구현', '화면 수정', '재구현', '전체 화면 구현'.
---

# 화면 구현 오케스트레이터

**실행 모드:** 서브 에이전트 파이프라인

## Phase 0: 컨텍스트 확인

실행 전 이전 작업 결과가 있는지 확인한다:
```
Glob("_workspace/*.md")
```

- `_workspace/` 없거나 비어있음 → **초기 실행**
- `_workspace/` 있고 사용자가 수정 요청 → **부분 재실행** (실패한 Phase만 재실행)
- 사용자가 새 화면 추가 요청 → **신규 화면** (`_workspace_prev/`로 기존 이동 후 새 실행)

## Phase 1: 대상 화면 파악

사용자 요청에서 구현할 화면 ID를 확정한다.
화면 카탈로그: `references/screen-catalog.md`

화면 ID가 명시되지 않은 경우:
- "홈 화면" → SCR-02 (+ SCR-01)
- "거래 추가" → SCR-03 + SCR-04
- "통계" → SCR-08
- "카테고리" → SCR-05 + SCR-06

## Phase 2: Figma 스펙 추출 (figma-reader)

각 화면에 대해 figma-reader 에이전트를 서브 에이전트로 호출한다.
여러 화면이면 병렬 실행 (`run_in_background: true`).

```python
Agent(
  description="SCR-02 Figma 스펙 추출",
  subagent_type="Explore",
  model="opus",
  prompt="""
    agents/figma-reader.md를 읽고 역할을 수행한다.
    대상: SCR-02 홈 화면 (거래 있음), Figma node: 27:112
    skills/read-figma-screen/SKILL.md의 절차를 따른다.
    Figma 파일키: CVoVoOVq55dAEjcz4cHGZK
    출력: _workspace/figma-SCR-02-spec.md
  """
)
```

## Phase 3: Compose 구현 (compose-builder)

Figma 스펙이 모두 준비된 후 compose-builder를 호출한다.
공유 컴포넌트 → 화면 순서로 구현한다.

```python
Agent(
  description="SCR-02 Compose 구현",
  subagent_type="general-purpose",
  model="opus",
  prompt="""
    agents/compose-builder.md를 읽고 역할을 수행한다.
    대상: SCR-02 홈 화면
    _workspace/figma-SCR-02-spec.md를 읽고 구현한다.
    skills/build-compose-screen/SKILL.md의 절차를 따른다.
    출력: 수정된 파일 목록, _workspace/SCR-02-build-result.txt
  """
)
```

## Phase 4: 기기 검증 (device-qa)

compose-builder 완료 후 device-qa를 호출한다.

```python
Agent(
  description="SCR-02 기기 검증",
  subagent_type="general-purpose",
  model="opus",
  prompt="""
    agents/device-qa.md를 읽고 역할을 수행한다.
    대상: SCR-02 홈 화면
    _workspace/figma-SCR-02-spec.md와 구현 결과를 비교한다.
    skills/verify-screen-on-device/SKILL.md의 절차를 따른다.
    출력: _workspace/SCR-02-qa-report.md
  """
)
```

## Phase 5: 결과 보고

QA 보고서를 읽고 사용자에게 요약한다:
- 구현 완료 화면 목록
- PASS / FAIL 판정
- FAIL 항목 수정 필요 사항
- 다음 구현 권장 화면

## 에러 핸들링
- **Figma 스펙 추출 실패**: 오류 메시지와 함께 중단. 사용자에게 MCP 연결 상태 확인 요청.
- **컴파일 오류**: compose-builder가 오류 수정 후 재시도 (최대 3회). 3회 후에도 실패 시 오류 내용 보고.
- **기기 미연결**: device-qa에서 ADB 기기 확인 실패 시 "기기 연결 후 재실행" 메시지와 함께 스킵.
- **QA FAIL**: 수정 필요 사항을 정리하여 사용자에게 보고. compose-builder 자동 재호출은 하지 않고 사용자 판단에 맡긴다.

## 테스트 시나리오

### 정상 흐름 (SCR-02)
1. "홈 화면 구현해줘" → Phase 2에서 SCR-02 스펙 추출
2. Phase 3에서 HomeScreen.kt, core/component/ 구현
3. BUILD SUCCESSFUL 확인
4. Phase 4에서 기기 스크린샷 → PASS 판정
5. "SCR-02 구현 완료" 보고

### 에러 흐름 (컴파일 오류)
1. Phase 3에서 오류 발생
2. compose-builder가 오류 수정 (최대 3회)
3. 3회 후 실패 시 오류 내용과 함께 사용자에게 보고
4. Phase 4 스킵

## 화면 카탈로그
`references/screen-catalog.md` 참조.
