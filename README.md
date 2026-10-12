# ⚡ Sensor Playground

**Sensor Playground** adalah aplikasi Android berbasis **Kotlin, Jetpack Compose, dan Material 3** yang dirancang untuk mengeksplorasi penggunaan sensor smartphone Android secara interaktif.

Aplikasi ini menggabungkan sensor dalam tiga kategori utama:

- 🏀 **Motion** — Accelerometer & Gyroscope
- 🧭 **Position** — Magnetometer / Compass
- 💡 **Environment** — Ambient Light Sensor

---

## ✨ Fitur Utama

### 🏀 1. Orb — Motion Control

Tab **Orb** menghadirkan bola interaktif berbasis Canvas dengan simulasi fisika real-time hingga **60 FPS**.

Fitur fisika meliputi:

- ⚡ Kecepatan dan akselerasi
- 🧱 Pantulan dinding dengan restitution
- 🌀 Gesekan / friction
- 🫧 Efek squash & stretch
- 🌑 Parallax shadow
- ✨ Motion trail

#### 🎮 Mode Kontrol

| Mode | Sensor / Input | Deskripsi |
|---|---|---|
| **Tilt** | Accelerometer | Miringkan smartphone untuk menggerakkan Orb |
| **Gyro** | Gyroscope | Rotasikan smartphone untuk memberikan dorongan berdasarkan angular velocity |
| **Sentuh** | Touch | Drag Orb kemudian lepaskan untuk melakukan fling |

#### ⚙️ Kontrol Tambahan

- 🔄 **Invert Axes** — Membalik arah sumbu gerakan
- 🔊 **Suara Tabrakan** — Efek suara menggunakan `ToneGenerator`
- 📳 **Getaran Haptic** — Feedback getaran saat terjadi tabrakan
- 📱 **Tes Getar** — Menguji hardware vibrator
- 🎚️ **Sensitivitas Gerak** — Mengatur sensitivitas sensor
- 🎞️ **Kurangi Animasi** — Mengurangi animasi untuk pengalaman yang lebih nyaman

---

### 🧭 2. Kompas — Position Sensor

Tab **Kompas** menggunakan sensor posisi untuk menampilkan arah smartphone secara real-time.

#### 🧭 Fitur

- Jarum kompas interaktif
- Rotasi jarum menggunakan spring damping
- Azimuth **0°–360°**
- Indikator arah mata angin Bahasa Indonesia
- Hue Color Shift berdasarkan arah kompas
- Deteksi akurasi sensor
- Panduan kalibrasi sensor magnetik
- Debug sensor secara real-time

#### 🗺️ Arah Mata Angin

| Azimuth | Arah |
|---:|:---|
| 0° | U |
| 45° | TL |
| 90° | T |
| 135° | TG |
| 180° | S |
| 225° | BD |
| 270° | B |
| 315° | BL |

#### 🛠️ Sensor Debug

Mode debug dapat menampilkan data sensor secara real-time:

```text
X
Y
Z
Lux
Azimuth
```

Jika akurasi sensor rendah, aplikasi menampilkan peringatan dan panduan kalibrasi menggunakan pola gerakan **angka 8**.

---

### 💡 3. Cahaya — Environment Sensor

Tab **Cahaya** menggunakan **Ambient Light Sensor** untuk mendeteksi intensitas cahaya di sekitar perangkat.

#### 🎨 Mode Cahaya

##### 1. Manual

Pengguna dapat memilih tema secara langsung:

- ☀️ Terang
- 🌙 Gelap

##### 2. Realtime

Tema berubah secara otomatis berdasarkan intensitas cahaya ambient.

Sistem menggunakan:

- Moving Average
- Hysteresis
- Lux Meter Gauge

Tujuannya agar perubahan tema tidak terlalu cepat ketika nilai sensor naik-turun.

##### 3. Tutup / Senter

Mode reaktif berdasarkan nilai Lux:

```text
< 30 Lux  → 🌙 Mode GELAP
≥ 30 Lux  → ☀️ Mode TERANG
```

Contoh penggunaan:

- 🖐️ Sensor ditutup dengan tangan → Mode Gelap
- 🔦 Sensor disinari lampu → Mode Terang

#### 🧪 Simulasi Cahaya

Tersedia **Slider Simulasi Cahaya** untuk menguji perubahan tema pada emulator tanpa membutuhkan sensor fisik.

---

# ⚙️ Lifecycle & Power Optimization

Sensor dikelola dengan memperhatikan lifecycle Activity untuk menghindari memory leak dan penggunaan baterai yang tidak diperlukan.

### Sensor Registration

Sensor utama:

```text
onResume()
    ↓
Register Sensor Listener

onPause()
    ↓
Unregister Sensor Listener
```

Dengan pendekatan ini, listener tidak tetap aktif ketika aplikasi berada di background.

### Gyroscope Optimization

Gyroscope hanya aktif ketika mode **GYRO** digunakan.

```text
Mode GYRO
    ↓
Register Gyroscope

Pindah Mode
    ↓
Unregister Gyroscope

App masuk Background
    ↓
Unregister Gyroscope
```

Hal ini membantu mengurangi penggunaan sensor dan konsumsi daya.

---

# 🏗️ Architecture

Sensor Playground menggunakan pola arsitektur **MVVM (Model–View–ViewModel)**.

```text
com.example.sensorplayground/
│
├── MainActivity.kt
│
├── sensor/
│   ├── SensorController.kt
│   ├── SensorModels.kt
│   └── SensorFilters.kt
│
├── data/
│   └── SettingsRepository.kt
│
├── util/
│   └── FeedbackManager.kt
│
├── viewmodel/
│   └── SensorViewModel.kt
│
└── ui/
    ├── theme/
    │   ├── Color.kt
    │   ├── Theme.kt
    │   └── Type.kt
    │
    ├── components/
    │   ├── OrbCanvas.kt
    │   ├── CompassView.kt
    │   ├── LightMeterCard.kt
    │   └── SensorStatusChip.kt
    │
    └── screens/
        └── HomeScreen.kt
```

### 📂 Penjelasan Struktur

| File / Folder | Fungsi |
|---|---|
| `MainActivity.kt` | Activity utama dan pengelolaan lifecycle sensor |
| `sensor/` | Abstraksi, model, dan filter sensor |
| `SensorController.kt` | Mengelola `SensorManager` dan `SensorEventListener` |
| `SensorModels.kt` | Data model untuk state sensor |
| `SensorFilters.kt` | Filter sensor, smoothing, averaging, dan hysteresis |
| `data/` | Penyimpanan konfigurasi aplikasi |
| `SettingsRepository.kt` | Persistent settings menggunakan DataStore |
| `util/` | Utility aplikasi |
| `FeedbackManager.kt` | Audio dan haptic feedback |
| `viewmodel/` | State dan business logic aplikasi |
| `SensorViewModel.kt` | Fisika Orb, sensor state, dan mode control |
| `ui/components/` | Komponen UI reusable |
| `ui/screens/` | Screen utama aplikasi |
| `ui/theme/` | Material 3 theme |

---

# 🛠️ Tech Stack

| Teknologi | Penggunaan |
|---|---|
| **Kotlin** | Bahasa pemrograman utama |
| **Jetpack Compose** | UI framework |
| **Material 3** | Design system |
| **MVVM** | Arsitektur aplikasi |
| **DataStore Preferences** | Persistent settings |
| **Android Sensor Framework** | Akses sensor perangkat |
| **Canvas API** | Rendering Orb dan visualisasi |
| **ToneGenerator** | Collision sound |
| **Vibrator / VibratorManager** | Haptic feedback |

---

# 📱 System Requirements

- **Android Studio:** Ladybug / Koala / 2024.1+
- **JDK:** 11+
- **Minimum SDK:** Android 7.0 — API 24
- **Compile SDK:** Android 15 — API 35+
- **Target SDK:** Android 15 — API 35+

> Beberapa fitur membutuhkan hardware sensor tertentu. Perangkat tanpa sensor yang diperlukan akan ditampilkan sebagai **sensor tidak tersedia**.

---

# 🚀 Getting Started

### 1. Clone Repository

```bash
git clone https://github.com/USERNAME/sensor-playground.git
```

Masuk ke folder project:

```bash
cd sensor-playground
```

### 2. Buka di Android Studio

Buka folder project menggunakan Android Studio.

Tunggu proses:

```text
Gradle Sync
↓
Indexing
↓
Build
```

### 3. Jalankan Aplikasi

Hubungkan perangkat Android atau jalankan emulator.

Kemudian tekan:

```text
Run ▶
```

atau gunakan:

```text
Shift + F10
```

---

# 🧪 Testing di Android Emulator

Beberapa sensor dapat disimulasikan menggunakan **Extended Controls**.

Buka:

```text
Emulator
→ Extended Controls (...)
→ Virtual Sensors
```

### 🎮 Motion

Gunakan:

```text
Device Pose
```

untuk mengubah:

- Roll
- Pitch

Hal ini dapat digunakan untuk menguji mode **Tilt** dan **Gyro**.

### 💡 Light Sensor

Gunakan:

```text
Virtual Sensors
→ Light
```

untuk mengubah nilai Lux.

Contoh:

```text
Lux < 30
→ Mode Gelap

Lux ≥ 30
→ Mode Terang
```

### 👆 Touch

Mode **Sentuh** dapat diuji langsung dengan:

```text
Drag Orb → Lepaskan → Fling
```

---

# 📸 Screenshots

Tambahkan screenshot aplikasi di folder:

```text
docs/
└── screenshots/
    ├── orb.png
    ├── compass.png
    └── light.png
```

Kemudian tampilkan di README:

```markdown
## 📸 Screenshots

| Orb | Compass | Light |
|---|---|---|
| ![Orb](docs/screenshots/orb.png) | ![Compass](docs/screenshots/compass.png) | ![Light](docs/screenshots/light.png) |
```

---

# 🔮 Future Improvements

Beberapa pengembangan yang dapat ditambahkan:

- 📊 Riwayat data sensor
- 📈 Grafik sensor real-time
- 🎨 Custom Orb theme
- 🧩 Tambahan jenis sensor
- 📱 Dukungan tablet
- 🌐 Export data sensor
- 🧪 Sensor diagnostic mode
- 🎮 Lebih banyak physics interaction
- 🏆 Achievement untuk eksperimen sensor

---

# 📄 License

Project ini dibuat untuk tujuan **pembelajaran, eksperimen, dan eksplorasi Android Sensor API**.

Silakan sesuaikan bagian lisensi dengan kebutuhan repository sebelum dipublikasikan.

---

## 👨‍💻 Author

**Sensor Playground**

Built with ❤️ using **Kotlin + Jetpack Compose + Material 3**.
