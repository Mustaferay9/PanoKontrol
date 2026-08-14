import subprocess
import time
import os
import unicodedata
import xml.etree.ElementTree as ET

ADB = r"C:\Users\eray\AppData\Local\Android\Sdk\platform-tools\adb.exe"
OUTPUT_DIR = r"C:\Users\eray\Desktop\Sparky_Sunum_Gorselleri"
os.makedirs(OUTPUT_DIR, exist_ok=True)

def normalize_text(text):
    if not text:
        return ""
    text = text.replace("İ", "I").replace("ı", "i").replace("Ş", "S").replace("ş", "s")
    text = text.replace("Ğ", "G").replace("ğ", "g").replace("Ö", "O").replace("ö", "o")
    text = text.replace("Ü", "U").replace("ü", "u").replace("Ç", "C").replace("ç", "c")
    return ''.join(c for c in unicodedata.normalize('NFD', text) if unicodedata.category(c) != 'Mn').lower()

def adb_cmd(args):
    return subprocess.run([ADB] + args, capture_output=True)

def tap(x, y):
    print(f"  -> Tap ({x}, {y})")
    adb_cmd(["shell", "input", "tap", str(x), str(y)])
    time.sleep(1.2)

def swipe(x1, y1, x2, y2, duration=400):
    print(f"  -> Swipe ({x1}, {y1}) -> ({x2}, {y2})")
    adb_cmd(["shell", "input", "swipe", str(x1), str(y1), str(x2), str(y2), str(duration)])
    time.sleep(1.2)

def save_screen(filename):
    res = subprocess.run([ADB, "exec-out", "screencap", "-p"], capture_output=True)
    out_path = os.path.join(OUTPUT_DIR, filename)
    with open(out_path, "wb") as f:
        f.write(res.stdout)
    size = len(res.stdout)
    print(f"  [Saved] {filename} ({size} bytes)")
    return size

def get_nodes():
    adb_cmd(["shell", "uiautomator", "dump", "/sdcard/dump.xml"])
    res = subprocess.run([ADB, "exec-out", "cat", "/sdcard/dump.xml"], capture_output=True)
    xml_str = res.stdout.decode("utf-8", errors="ignore")
    if not xml_str.strip().startswith("<"):
        return []
    try:
        root = ET.fromstring(xml_str)
    except Exception:
        return []
    nodes = []
    import re
    for node in root.iter("node"):
        text = node.attrib.get("text", "")
        desc = node.attrib.get("content-desc", "")
        bounds = node.attrib.get("bounds", "")
        m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", bounds)
        if m:
            x1, y1, x2, y2 = map(int, m.groups())
            cx, cy = (x1 + x2) // 2, (y1 + y2) // 2
            nodes.append({"text": text, "desc": desc, "norm": normalize_text(text + " " + desc), "cx": cx, "cy": cy, "bounds": (x1, y1, x2, y2)})
    return nodes

def click_match(query, timeout=12):
    q_norm = normalize_text(query)
    start = time.time()
    while time.time() - start < timeout:
        nodes = get_nodes()
        for n in nodes:
            if q_norm in n["norm"]:
                print(f"  [Click] Matched '{query}' at ({n['cx']}, {n['cy']})")
                tap(n["cx"], n["cy"])
                return True
        time.sleep(0.5)
    print(f"  [Warn] Timeout finding '{query}'")
    return False

def main():
    print("=== 1. Starting App from scratch ===")
    adb_cmd(["shell", "am", "force-stop", "com.panokontrol.gridcheck"])
    time.sleep(1)
    adb_cmd(["shell", "am", "start", "-n", "com.panokontrol.gridcheck/.MainActivity"])
    time.sleep(2)
    
    # 01 Login
    print("Capturing 01_LoginScreen.png...")
    save_screen("01_LoginScreen.png")
    
    print("Logging in...")
    tap(540, 1600) # Tap 'Giriş Yap'
    time.sleep(2)
    
    # 02 Dashboard
    print("Capturing 02_DashboardScreen.png...")
    save_screen("02_DashboardScreen.png")
    
    # --- FLOW A: PANO İÇİ RESULT (via Past Inspection) ---
    print("\n=== Opening Past Inspection (Pano İçi: 0148) ===")
    # Tap item SDK-2026-0148
    if not click_match("0148"):
        tap(540, 1150)
    time.sleep(2)
    
    # 05 Result Pano İçi AI
    print("Capturing 05_Result_PanoIci_AI.png...")
    save_screen("05_Result_PanoIci_AI.png")
    
    # 05b Result Pano İçi Orijinal
    print("Switching to 'Orijinal' layer...")
    if not click_match("Orijinal"):
        tap(780, 780)
    time.sleep(1.2)
    save_screen("05b_Result_PanoIci_Orijinal.png")
    
    # Switch back to AI Analiz
    click_match("AI Analiz")
    time.sleep(0.8)
    
    # 06 Result 400A DSYA Detay
    print("Switching to '400A DSYA' tab...")
    if not click_match("400A DSYA"):
        tap(540, 720)
    time.sleep(1.2)
    save_screen("06_Result_400A_DSYA_Detay.png")
    
    # 07 Result 160A DSYA Detay
    print("Switching to '160A DSYA' tab...")
    if not click_match("160A DSYA"):
        tap(850, 720)
    time.sleep(1.2)
    save_screen("07_Result_160A_DSYA_Detay.png")
    
    # 07b Technical Table & Checklist
    print("Scrolling down to Technical Table & Checklist...")
    swipe(540, 1900, 540, 650, 400)
    time.sleep(1.5)
    save_screen("07b_Result_Teknik_Tablo_ve_Kontrol_Listesi.png")
    
    # Return to Dashboard
    print("Returning to Dashboard...")
    if not click_match("Don"):
        tap(540, 2280)
    time.sleep(2)
    
    # --- FLOW B: DIŞ KAPAK RESULT (via Past Inspection) ---
    print("\n=== Opening Past Inspection (Dış Kapak: 0147) ===")
    if not click_match("0147"):
        tap(540, 1400)
    time.sleep(2)
    
    # 09 Result Dış Kapak AI
    print("Capturing 09_Result_DisKapak_AI.png...")
    save_screen("09_Result_DisKapak_AI.png")
    
    # Return to Dashboard
    print("Returning to Dashboard...")
    if not click_match("Don"):
        tap(540, 2280)
    time.sleep(2)
    
    # --- FLOW C: CAPTURE SCREENS & PROCESSING ---
    print("\n=== Opening New Inspection Capture Screen ===")
    if not click_match("Yeni Pano"):
        tap(582, 465)
    time.sleep(2)
    
    # 03 Capture Pano İçi
    print("Capturing 03_CaptureScreen_PanoIci.png...")
    save_screen("03_CaptureScreen_PanoIci.png")
    
    # Select Dış Kapak card
    print("Selecting 'Dış Kapak' option card...")
    if not click_match("Dis Kapak"):
        tap(800, 2050)
    time.sleep(1.2)
    
    # 08 Capture Dış Kapak
    print("Capturing 08_CaptureScreen_DisKapak.png...")
    save_screen("08_CaptureScreen_DisKapak.png")
    
    # Click Analiz Et to show processing laser screen
    print("Clicking 'Analiz Et'...")
    if not click_match("Analiz Et"):
        tap(540, 2280)
    time.sleep(0.8)
    
    # 04 Processing Laser Scan
    print("Capturing 04_Processing_LaserScan.png...")
    save_screen("04_Processing_LaserScan.png")
    
    print("\n=== ALL SCREENSHOTS SUCCESSFULLY CAPTURED & SAVED! ===")

if __name__ == "__main__":
    main()
