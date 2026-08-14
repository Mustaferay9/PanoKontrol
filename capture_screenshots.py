import subprocess
import time
import os

ADB = r"C:\Users\eray\AppData\Local\Android\Sdk\platform-tools\adb.exe"
OUTPUT_DIR = r"C:\Users\eray\Desktop\Sparky_Sunum_Gorselleri"
os.makedirs(OUTPUT_DIR, exist_ok=True)

def adb_cmd(args):
    return subprocess.run([ADB] + args, capture_output=True, text=False)

def tap(x, y):
    adb_cmd(["shell", "input", "tap", str(x), str(y)])

def swipe(x1, y1, x2, y2, duration=300):
    adb_cmd(["shell", "input", "swipe", str(x1), str(y1), str(x2), str(y2), str(duration)])

def capture(filename):
    res = adb_cmd(["exec-out", "screencap", "-p"])
    path = os.path.join(OUTPUT_DIR, filename)
    with open(path, "wb") as f:
        f.write(res.stdout)
    print(f"[OK] Saved {filename} ({len(res.stdout)} bytes)")

def main():
    print("=== Launching App ===")
    adb_cmd(["shell", "am", "force-stop", "com.panokontrol.gridcheck"])
    time.sleep(1)
    adb_cmd(["shell", "am", "start", "-n", "com.panokontrol.gridcheck/.MainActivity"])
    time.sleep(2.5)

    # 1. Login Screen
    print("Capturing 01_LoginScreen.png...")
    capture("01_LoginScreen.png")

    # Tap 'Giriş Yap' (around x=540, y=1450)
    print("Tapping Login button...")
    tap(540, 1450)
    time.sleep(2)

    # 2. Dashboard Screen
    print("Capturing 02_DashboardScreen.png...")
    capture("02_DashboardScreen.png")

    # Tap 'Yeni Pano Denetimi Başlat' (around x=540, y=410)
    print("Tapping Start Inspection button...")
    tap(540, 410)
    time.sleep(2)

    # 3. Capture Screen (Pano İçi Seçili)
    print("Capturing 03_CaptureScreen_PanoIci.png...")
    capture("03_CaptureScreen_PanoIci.png")

    # Tap 'Analiz Et' (bottom bar around x=540, y=2250)
    print("Tapping Analyze button...")
    tap(540, 2250)
    time.sleep(0.6)

    # 4. Processing Screen with Laser
    print("Capturing 04_Processing_LaserScan.png...")
    capture("04_Processing_LaserScan.png")

    # Wait for result screen navigation
    time.sleep(2.5)

    # 5. Result Screen Pano İçi (AI Katmanı)
    print("Capturing 05_Result_PanoIci_AI.png...")
    capture("05_Result_PanoIci_AI.png")

    # Tab: Orijinal katman (around x=770, y=600)
    print("Tapping Orijinal layer switcher...")
    tap(770, 600)
    time.sleep(1)
    capture("05b_Result_PanoIci_Orijinal.png")

    # Tab: 400A DSYA (around x=540, y=530)
    print("Tapping 400A DSYA tab...")
    tap(540, 530)
    time.sleep(1)
    capture("06_Result_400A_DSYA_Detay.png")

    # Tab: 160A DSYA (around x=880, y=530)
    print("Tapping 160A DSYA tab...")
    tap(880, 530)
    time.sleep(1)
    capture("07_Result_160A_DSYA_Detay.png")

    # Scroll down to show technical specs table & checklist
    print("Scrolling down result screen...")
    swipe(540, 1800, 540, 600, 400)
    time.sleep(1)
    capture("07b_Result_Teknik_Tablo_ve_Kontrol_Listesi.png")

    # Go back to dashboard (tap bottom button or back)
    print("Tapping back to dashboard...")
    tap(540, 2250)
    time.sleep(1.5)

    # Tap 'Yeni Pano Denetimi Başlat' again
    tap(540, 410)
    time.sleep(1.5)

    # Tap 'Dış Kapak' option card (around x=800, y=1980)
    print("Selecting Dış Kapak option...")
    tap(800, 1980)
    time.sleep(1)
    capture("08_CaptureScreen_DisKapak.png")

    # Tap 'Analiz Et'
    print("Analyzing Dış Kapak...")
    tap(540, 2250)
    time.sleep(3.0)

    # 9. Result Screen Dış Kapak
    print("Capturing 09_Result_DisKapak_AI.png...")
    capture("09_Result_DisKapak_AI.png")

    print("=== All screenshots captured successfully! ===")

if __name__ == "__main__":
    main()
