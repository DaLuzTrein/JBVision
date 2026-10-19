import cv2
import requests
import time

# ─── CONFIGURAÇÕES ───────────────────────────────────────────
BACKEND_URL  = "http://localhost:8081/api/registro/upload"
CAMERA_TOKEN = "camera-exemplo"  # token cadastrado no sistema
INTERVALO    = 0.5       # segundos entre cada envio ao backend
INDICE_CAM   = 0       # 0 = webcam do PC, 1 = Iriun
# ─────────────────────────────────────────────────────────────

detector = cv2.CascadeClassifier(cv2.data.haarcascades + "haarcascade_frontalface_default.xml")

def tem_rosto(frame):
    cinza = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    rostos = detector.detectMultiScale(cinza, scaleFactor=1.1, minNeighbors=5, minSize=(80, 80))
    return len(rostos) > 0

def enviar_frame(frame):
    _, img_encoded = cv2.imencode(".jpg", frame)
    img_bytes = img_encoded.tobytes()
    try:
        resposta = requests.post(
            BACKEND_URL,
            files={"image": ("frame.jpg", img_bytes, "image/jpeg")},
            data={"cameraToken": CAMERA_TOKEN},
            timeout=10
        )
        print(f"[{CAMERA_TOKEN}] {resposta.text}")
    except requests.exceptions.ConnectionError:
        print(f"[{CAMERA_TOKEN}] Erro: não conseguiu conectar ao backend.")
    except Exception as e:
        print(f"[{CAMERA_TOKEN}] Erro ao enviar frame: {e}")

def main():
    print(f"Iniciando câmera — token: {CAMERA_TOKEN}")
    cam = cv2.VideoCapture(INDICE_CAM)

    if not cam.isOpened():
        print("Erro: câmera não encontrada.")
        return

    print(f"Câmera conectada! Pressione Q para encerrar.")
    ultimo_envio = 0

    while True:
        ok, frame = cam.read()
        if not ok:
            print("Erro ao capturar frame.")
            break

        cv2.imshow(f"JBVision — {CAMERA_TOKEN}", frame)

        agora = time.time()
        if agora - ultimo_envio >= INTERVALO:
            if tem_rosto(frame):
                enviar_frame(frame)
            else:
                print(f"[{CAMERA_TOKEN}] Aguardando rosto...")
            ultimo_envio = agora

        if cv2.waitKey(1) & 0xFF == ord("q"):
            print("Encerrando...")
            break

    cam.release()
    cv2.destroyAllWindows()

if __name__ == "__main__":
    main()