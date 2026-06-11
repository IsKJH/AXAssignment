# Generic adb UI driver — app-agnostic, coordinate-free.
#
# Finds targets on ANY app/screen via uiautomator dump (text / content-desc /
# resource-id / class+index) and auto-waits by polling until the element appears,
# instead of hardcoded coordinates and fixed sleeps. Korean/UTF-8 text goes through
# the devicekit clipboard broadcast (com.mobilenext.devicekit) + KEYCODE_PASTE.
#
# Typical flow script:
#   from ui import UI
#   u = UI()
#   u.tap(desc="거래 추가")            # FAB by accessibility label
#   u.tap(cls="android.widget.EditText", index=0)
#   u.type("12000")
#   u.tap(text="구독", scroll=True)    # scrolls list until found
#   u.wait(desc="거래 추가")           # back on home = saved

import base64
import os
import subprocess
import time
import xml.etree.ElementTree as ET

# Resolve adb/device from env so the driver ports across machines/projects:
# ADB_PATH > ANDROID_HOME/ANDROID_SDK_ROOT > default Windows SDK location.
ADB = os.environ.get("ADB_PATH") or os.path.join(
    os.environ.get("ANDROID_HOME")
    or os.environ.get("ANDROID_SDK_ROOT")
    or os.path.expanduser(os.path.join("~", "AppData", "Local", "Android", "Sdk")),
    "platform-tools", "adb.exe")
DEFAULT_DEVICE = os.environ.get("UI_DEVICE", "R3CT50PWVHA")


class UI:
    def __init__(self, device=DEFAULT_DEVICE, timeout=6.0, interval=0.35):
        self.device = device
        self.timeout = timeout
        self.interval = interval
        # uiautomator dump costs ~2s on-device, so within one screen we reuse the
        # last dump (element positions are static) and only re-dump on navigation
        # (fresh=True) or on a cache miss
        self._cache = None

    # ---- low-level -----------------------------------------------------------

    def adb(self, *args):
        r = subprocess.run(
            [ADB, "-s", self.device, *args],
            capture_output=True, text=True, encoding="utf-8", errors="replace",
        )
        return r.stdout or ""

    def dump(self):
        # exec-out prints the XML plus a trailing "UI hierchary dumped to:" line
        raw = self.adb("exec-out", "uiautomator", "dump", "/dev/tty")
        start = raw.find("<?xml")
        end = raw.rfind("</hierarchy>")
        if start < 0 or end < 0:
            self._cache = None
            return None
        try:
            self._cache = ET.fromstring(raw[start:end + len("</hierarchy>")])
        except ET.ParseError:
            self._cache = None
        return self._cache

    @staticmethod
    def _center(node):
        b = node.get("bounds", "")
        try:
            l, t, r, bo = (
                int(v) for part in b.strip("[]").split("][") for v in part.split(",")
            )
            return ((l + r) // 2, (t + bo) // 2)
        except ValueError:
            return None

    def _match(self, root, text=None, desc=None, rid=None, cls=None,
               index=0, contains=False):
        if root is None:
            return None
        found = 0
        for node in root.iter("node"):
            if text is not None:
                v = node.get("text", "")
                if not (text in v if contains else text == v):
                    continue
            if desc is not None:
                v = node.get("content-desc", "")
                if not (desc in v if contains else desc == v):
                    continue
            if rid is not None and node.get("resource-id", "") != rid:
                continue
            if cls is not None and node.get("class", "") != cls:
                continue
            if found == index:
                return node
            found += 1
        return None

    # ---- waiting primitives --------------------------------------------------

    def wait(self, timeout=None, fresh=False, **sel):
        """Find via cache first (unless fresh); else poll fresh dumps. Returns center."""
        if not fresh and self._cache is not None:
            node = self._match(self._cache, **sel)
            if node is not None:
                c = self._center(node)
                if c:
                    return c
        deadline = time.time() + (timeout or self.timeout)
        while True:
            node = self._match(self.dump(), **sel)
            if node is not None:
                c = self._center(node)
                if c:
                    return c
            if time.time() > deadline:
                raise TimeoutError(f"element not found: {sel}")
            time.sleep(self.interval)

    def exists(self, fresh=True, **sel):
        tree = self.dump() if fresh or self._cache is None else self._cache
        return self._match(tree, **sel) is not None

    def center(self, fresh=False, **sel):
        """Non-waiting find: return the element center or None."""
        tree = self.dump() if fresh or self._cache is None else self._cache
        node = self._match(tree, **sel)
        return self._center(node) if node is not None else None

    def gate(self, **sel):
        """Wait (fresh) for a marker unique to the destination screen after a
        navigation tap. The matching dump stays cached, so follow-up taps on the
        same screen are free — and matching the marker guarantees finds can no
        longer hit leftovers of the previous screen mid-transition."""
        return self.wait(fresh=True, **sel)

    # ---- actions ---------------------------------------------------------------

    def tap(self, x=None, y=None, scroll=False, scroll_max=4, fresh=False,
            keep=False, **sel):
        """Tap an element. Any tap may navigate, so the dump cache is dropped
        afterwards — pass keep=True for same-screen taps (e.g. focusing a field)
        to reuse the cached dump for subsequent finds."""
        try:
            if x is not None:
                self.adb("shell", "input", "tap", str(x), str(y))
                return
            for attempt in range(scroll_max + 1):
                try:
                    cx, cy = self.wait(
                        timeout=self.timeout if attempt == 0 else 1.5,
                        fresh=fresh or attempt > 0, **sel,
                    )
                    self.adb("shell", "input", "tap", str(cx), str(cy))
                    return
                except TimeoutError:
                    if not scroll or attempt == scroll_max:
                        raise
                    self.adb("shell", "input", "swipe",
                             "540", "1900", "540", "1000", "300")
            raise TimeoutError(f"element not found after scrolling: {sel}")
        finally:
            if not keep:
                self._cache = None

    def type(self, text):
        if text == "":
            return
        if text.isascii():
            self.adb("shell", "input", "text", text.replace(" ", "%s"))
        else:
            b64 = base64.b64encode(text.encode("utf-8")).decode()
            self.adb("shell", "am", "broadcast", "-a", "devicekit.clipboard.set",
                     "-e", "encoding", "base64", "-e", "text", b64,
                     "-n", "com.mobilenext.devicekit/.ClipboardBroadcastReceiver")
            self.adb("shell", "input", "keyevent", "KEYCODE_PASTE")
            self.adb("shell", "am", "broadcast", "-a", "devicekit.clipboard.clear",
                     "-n", "com.mobilenext.devicekit/.ClipboardBroadcastReceiver")

    def back(self):
        self.adb("shell", "input", "keyevent", "4")

    def key(self, keycode):
        self.adb("shell", "input", "keyevent", str(keycode))

    def screenshot(self, path):
        self.adb("shell", "screencap", "-p", "/sdcard/ui_shot.png")
        self.adb("pull", "/sdcard/ui_shot.png", path)


# ---- CLI -------------------------------------------------------------------
# Token-cheap screen inspection for agent use (text out, no images):
#   python -X utf8 scripts/ui.py texts            # all visible texts + centers
#   python -X utf8 scripts/ui.py find <문자열>     # nodes containing it (exit 1 if none)
#   python -X utf8 scripts/ui.py tap <문자열>      # tap first node containing it
if __name__ == "__main__":
    import sys

    u = UI()
    cmd = sys.argv[1] if len(sys.argv) > 1 else "texts"
    root = u.dump()
    if root is None:
        sys.exit("dump failed (screen off/locked?)")

    def rows(pred):
        for node in root.iter("node"):
            label = node.get("text") or node.get("content-desc")
            if label and pred(label):
                c = UI._center(node)
                yield f"{label}\t({c[0]},{c[1]})" if c else label

    if cmd == "texts":
        print("\n".join(rows(lambda s: True)))
    elif cmd == "find":
        needle = sys.argv[2]
        out = list(rows(lambda s: needle in s))
        print("\n".join(out) if out else f"NOT FOUND: {needle}")
        sys.exit(0 if out else 1)
    elif cmd == "tap":
        u.tap(text=sys.argv[2], contains=True)
        print("tapped")
    else:
        sys.exit(f"unknown command: {cmd}")
