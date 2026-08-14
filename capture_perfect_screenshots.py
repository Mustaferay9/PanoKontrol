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

def wait_for_node(pattern, timeout=20):
    start = time.time()
    while time.time() - start < timeout:
        nodes = get_ui_nodes()
        for n in nodes:
            if pattern.lower() in n["text"].lower() or pattern.lower() in n["desc"].lower():
                return n
        time.sleep(0.6)
    return None

def click_node(pattern, timeout=20):
    n = wait_for_node(pattern, timeout)
    if n:
        print(f"  [Click] Found '{pattern}' at ({n['cx']}, {n['cy']})")
        adb_cmd(["shell", "input", "tap", str(n["cx"]), str(n["cy"])])
        return True
    print(f"  [Warn] Could not find '{pattern}'")
    return False

def tap(x, y):
    adb_cmd(["shell", "input", "tap", str(x), str(y)])

def swipe(x1, y1, x2, y2, duration=400):
    adb_cmd(["shell", "input", "swipe", str(x1), str(y1), str(x2), str(y2), str(duration)])

def save_screen(filename):
    res = subprocess.run([ADB, "exec-out", "screencap", "-p"], capture_output=True)
    out_path = os.path.join(OUTPUT_DIR, filename)
    with open(out_path, "wb") as f:
        f.write(res.stdout)
    print(f"  [Screenshot] Saved -> {filename} ({len(res.stdout)} bytes)")

def main():
    print("=== Starting Screen Capture Automation ===")
    
    # Force restart app
    adb_cmd(["shell", "am", "force-stop", "com.panokontrol.gridcheck"])
    time.sleep(1.5)
    adb_cmd(["shell", "am", "start", "-n", "com.panokontrol.gridcheck/.MainActivity"])
    
    # 1. Login Screen
    print("1. Waiting for Login Screen...")
    login_btn = wait_for_node("Giriş Yap", timeout=20)
    time.sleep(1.5)
    save_screen("01_LoginScreen.png")
    
    # Click Login
    print("2. Logging in...")
    click_node("Giriş Yap")
    
    # 2. Dashboard Screen
    print("3. Waiting for Dashboard...")
    wait_for_node("Yeni Pano Denetimi Başlat", timeout=15)
    time.sleep(1.5)
    save_screen("02_DashboardScreen.png")
    
    # Click Start Inspection
    print("4. Starting Inspection...")
    click_node("Yeni Pano Denetimi Başlat")
    
    # 3. Capture Screen (Pano İçi)
    print("5. Waiting for Capture Screen...")
    wait_for_node("Analiz Et", timeout=15)
    time.sleep(1.5)
    save_screen("03_CaptureScreen_PanoIci.png")
    
    # Click Analyze
    print("6. Clicking Analyze...")
    click_node("Analiz Et")
    time.sleep(0.8)
    
    # 4. Processing Screen (Laser effect)
    print("7. Capturing Processing Screen...")
    save_screen("04_Processing_LaserScan.png")
    
    # Wait for result screen
    print("8. Waiting for Results Screen...")
    res_btn = wait_for_node("Kontrol Paneline Dön", timeout=25)
    if not res_btn:
        print("Waiting extra for processing...")
        time.sleep(5)
    time.sleep(2)
    
    # 5. Result Pano İçi AI
    print("9. Capturing Result Pano İçi AI...")
    save_screen("05_Result_PanoIci_AI.png")
    
    # Switch to Orijinal
    print("10. Switching to Orijinal layer...")
    click_node("Orijinal")
    time.sleep(1.2)
    save_screen("05b_Result_PanoIci_Orijinal.png")
    
    # Tab 400A DSYA
    print("11. Switching to 400A DSYA Tab...")
    click_node("400A DSYA")
    time.sleep(1.2)
    save_screen("06_Result_400A_DSYA_Detay.png")
    
    # Tab 160A DSYA
    print("12. Switching to 160A DSYA Tab...")
    click_node("160A DSYA")
    time.sleep(1.2)
    save_screen("07_Result_160A_DSYA_Detay.png")
    
    # Scroll down to show technical table & checklist
    print("13. Scrolling down to show technical specs & checklist...")
    swipe(540, 1800, 540, 700, 500)
    time.sleep(1.5)
    save_screen("07b_Result_Teknik_Tablo_ve_Kontrol_Listesi.png")
    
    # Back to Dashboard
    print("14. Returning to Dashboard...")
    click_node("Kontrol Paneline Dön")
    wait_for_node("Yeni Pano Denetimi Başlat", timeout=15)
    time.sleep(1.5)
    
    # Start Inspection again for Dış Kapak
    print("15. Starting Dış Kapak Inspection...")
    click_node("Yeni Pano Denetimi Başlat")
    wait_for_node("Analiz Et", timeout=15)
    time.sleep(1.5)
    
    # Click Dış Kapak option
    print("16. Selecting Dış Kapak option card...")
    click_node("Dış Kapak")
    time.sleep(1.5)
    save_screen("08_CaptureScreen_DisKapak.png")
    
    # Click Analyze
    print("17. Analyzing Dış Kapak...")
    click_node("Analiz Et")
    wait_for_node("Kontrol Paneline Dön", timeout=25)
    time.sleep(2)
    
    # 9. Result Dış Kapak AI
    print("18. Capturing Result Dış Kapak AI...")
    save_screen("09_Result_DisKapak_AI.png")
    
    print("=== All High-Resolution Screenshots Successfully Captured! ===")

if __name__ == "__main__":
    main()
