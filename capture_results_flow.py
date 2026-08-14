import subprocess
import time
import os
import re
import xml.etree.ElementTree as ET

ADB = r"C:\Users\eray\AppData\Local\Android\Sdk\platform-tools\adb.exe"
OUTPUT_DIR = r"C:\Users\eray\Desktop\Sparky_Sunum_Gorselleri"
os.makedirs(OUTPUT_DIR, exist_ok=True)

def adb_cmd(args):
    return subprocess.run([ADB] + args, capture_output=True)

def get_ui_nodes():
    subprocess.run([ADB, "shell", "uiautomator", "dump", "/sdcard/window_dump.xml"], capture_output=True)
    res = subprocess.run([ADB, "exec-out", "cat", "/sdcard/window_dump.xml"], capture_output=True)
    xml_str = res.stdout.decode("utf-8", errors="ignore")
    if not xml_str.strip().startswith("<"):
        return []
    try:
        root = ET.fromstring(xml_str)
    except Exception:
        return []
    nodes = []
    for node in root.iter("node"):
        text = node.attrib.get("text", "")
        desc = node.attrib.get("content-desc", "")
        bounds = node.attrib.get("bounds", "")
        m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", bounds)
        if m:
            x1, y1, x2, y2 = map(int, m.groups())
            cx, cy = (x1 + x2) // 2, (y1 + y2) // 2
            nodes.append({"text": text, "desc": desc, "cx": cx, "cy": cy, "bounds": (x1, y1, x2, y2)})
    return nodes

def wait_for_node(pattern, timeout=15):
    start = time.time()
    while time.time() - start < timeout:
        nodes = get_ui_nodes()
        for n in nodes:
            if pattern.lower() in n["text"].lower() or pattern.lower() in n["desc"].lower():
                return n
        time.sleep(0.5)
    return None

def click_node(pattern, timeout=15):
    n = wait_for_node(pattern, timeout)
    if n:
        print(f"  [Click] Found '{pattern}' at ({n['cx']}, {n['cy']})")
        adb_cmd(["shell", "input", "tap", str(n["cx"]), str(n["cy"])])
        return True
    print(f"  [Warn] Could not find '{pattern}'")
    return False

def swipe(x1, y1, x2, y2, duration=350):
    adb_cmd(["shell", "input", "swipe", str(x1), str(y1), str(x2), str(y2), str(duration)])

def save_screen(filename):
    res = subprocess.run([ADB, "exec-out", "screencap", "-p"], capture_output=True)
    out_path = os.path.join(OUTPUT_DIR, filename)
    with open(out_path, "wb") as f:
        f.write(res.stdout)
    print(f"  [Screenshot] Saved -> {filename} ({len(res.stdout)} bytes)")

def main():
    print("=== Launching App ===")
    adb_cmd(["shell", "am", "force-stop", "com.panokontrol.gridcheck"])
    time.sleep(1)
    adb_cmd(["shell", "am", "start", "-n", "com.panokontrol.gridcheck/.MainActivity"])
    
    # Login
    print("Waiting for Login...")
    click_node("Giriş Yap", timeout=15)
    
    # Dashboard
    print("Waiting for Dashboard...")
    wait_for_node("Yeni Pano Denetimi Başlat", timeout=15)
    time.sleep(1)
    
    # Tap Past Inspection 1: SDK-2026-0148 (Interior Result)
    print("Opening Pano İçi Result Screen...")
    click_node("0148", timeout=10)
    wait_for_node("Kontrol Paneline Dön", timeout=15)
    time.sleep(1.5)
    
    # 5. Result Pano İçi AI
    print("Saving 05_Result_PanoIci_AI.png...")
    save_screen("05_Result_PanoIci_AI.png")
    
    # Tab: Orijinal
    print("Switching to Orijinal layer...")
    click_node("Orijinal", timeout=10)
    time.sleep(1)
    save_screen("05b_Result_PanoIci_Orijinal.png")
    
    # Tab: 400A DSYA
    print("Switching to 400A DSYA Tab...")
    click_node("400A DSYA", timeout=10)
    time.sleep(1)
    save_screen("06_Result_400A_DSYA_Detay.png")
    
    # Tab: 160A DSYA
    print("Switching to 160A DSYA Tab...")
    click_node("160A DSYA", timeout=10)
    time.sleep(1)
    save_screen("07_Result_160A_DSYA_Detay.png")
    
    # Scroll down to show technical specs table & checklist
    print("Scrolling down result screen...")
    swipe(540, 1850, 540, 700, 400)
    time.sleep(1.2)
    save_screen("07b_Result_Teknik_Tablo_ve_Kontrol_Listesi.png")
    
    # Return to Dashboard
    print("Returning to Dashboard...")
    click_node("Kontrol Paneline Dön", timeout=10)
    wait_for_node("Yeni Pano Denetimi Başlat", timeout=15)
    time.sleep(1)
    
    # Tap Past Inspection 2: SDK-2026-0147 (Door Result)
    print("Opening Dış Kapak Result Screen...")
    click_node("0147", timeout=10)
    wait_for_node("Kontrol Paneline Dön", timeout=15)
    time.sleep(1.5)
    save_screen("09_Result_DisKapak_AI.png")
    
    # Return to Dashboard
    print("Returning to Dashboard...")
    click_node("Kontrol Paneline Dön", timeout=10)
    wait_for_node("Yeni Pano Denetimi Başlat", timeout=15)
    time.sleep(1)
    
    # Start Inspection to capture Dış Kapak capture screen
    print("Opening Capture Screen for Dış Kapak...")
    click_node("Yeni Pano Denetimi Başlat", timeout=10)
    wait_for_node("Analiz Et", timeout=15)
    time.sleep(1)
    click_node("Dış Kapak", timeout=10)
    time.sleep(1)
    save_screen("08_CaptureScreen_DisKapak.png")
    
    print("=== All screens successfully captured! ===")

if __name__ == "__main__":
    main()
