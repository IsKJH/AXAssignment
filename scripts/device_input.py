# Fast transaction-entry flow for AXAssignment, built on the generic ui.py driver.
# Coordinate-free: every target is found by text / accessibility label with
# auto-wait, so it survives layout changes and new screens only need new flows.
#
# Usage (start from the HOME screen):
#   python -X utf8 scripts/device_input.py add <amount> <memo> [category]   # expense
#   python -X utf8 scripts/device_input.py add <amount> <memo> --income     # income
#   python -X utf8 scripts/device_input.py bulk <file.json>
#     JSON: [{"amount":12000,"memo":"유튜브 프리미엄","category":"구독"},
#            {"amount":3000000,"memo":"월급","income":true}]

import json
import sys
import time

from ui import UI

EDIT_TEXT = "android.widget.EditText"


def select_category(u: UI, name):
    """On the category-select screen. Tapping the already-selected row would
    DESELECT it (re-tap-to-deselect feature), so if the target is already
    selected just go back; otherwise tap it (pops back automatically)."""
    for _ in range(5):
        target = u.center(text=name)
        if target:
            selected = u.center(desc="선택됨")
            if selected and abs(selected[1] - target[1]) < 80:
                u.back()
            else:
                u.tap(x=target[0], y=target[1])
            return
        u.adb("shell", "input", "swipe", "540", "1900", "540", "1000", "300")
        u.dump()
    raise TimeoutError(f"category not found: {name}")


def add_transaction(u: UI, amount, memo, category=None, income=False):
    start = time.time()
    u.tap(desc="거래 추가")                      # FAB (home only)
    u.gate(text="거래 내역 추가")                # add screen committed
    if income:
        u.tap(text="수입", keep=True)            # type toggle (same screen)
    u.tap(cls=EDIT_TEXT, index=0, keep=True)     # amount field
    u.type(str(amount))
    u.tap(cls=EDIT_TEXT, index=1, keep=True)     # memo (지출처/수입처) field
    u.type(memo)
    u.back()                                     # close keyboard
    if category and not income:
        u.tap(text="카테고리 선택", keep=True)
        u.gate(text="내 카테고리")               # select screen committed
        select_category(u, category)
        u.gate(text="카테고리 선택")             # back on add screen
    u.tap(text="확인")
    u.gate(desc="거래 추가")                     # FAB visible again = saved, home
    label = f"{'수입' if income else category or '미분류'} / {memo} / {amount}"
    print(f"OK ({time.time() - start:.1f}s): {label}")


def main():
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    u = UI()
    cmd = sys.argv[1]
    if cmd == "add":
        args = [a for a in sys.argv[2:] if a != "--income"]
        income = "--income" in sys.argv
        category = args[2] if len(args) > 2 else None
        add_transaction(u, args[0], args[1], category, income)
    elif cmd == "bulk":
        with open(sys.argv[2], encoding="utf-8") as f:
            items = json.load(f)
        t0 = time.time()
        for item in items:
            add_transaction(u, item["amount"], item["memo"],
                            item.get("category"), item.get("income", False))
        print(f"TOTAL: {len(items)} items in {time.time() - t0:.1f}s")
    else:
        sys.exit(f"unknown command: {cmd}")


if __name__ == "__main__":
    main()
