⚡ Sensor Playground
Sensor Playground adalah aplikasi Android berbasis Kotlin, Jetpack Compose, dan Material 3 yang dirancang untuk mengeksplorasi potensi interaktif sensor smartphone Android dari 3 kategori berbeda: Motion, Environment, dan Position.
🚀 Fitur Utama
1. 🏀 Tab Orb (Motion Control)
Sebuah "Orb" (bola) interaktif yang hidup pada Canvas dengan mesin fisika 60fps (kecepatan, gesekan, pantulan dinding dengan restitusi, efek squash & stretch, bayangan parallax, dan jejak trail).
Pengguna dapat memilih 3 Mode Kontrol:
•
Tilt (Akselerometer): Miringkan HP untuk menggerakkan orb sesuai arah gravitasi.
•
Gyro (Gyroscope): Putar/rotasikan HP untuk mendorong orb sesuai kecepatan sudut (angular velocity) dengan integrasi sudut virtual dan deadzone.
•
Sentuh (Touch Drag & Fling): Sentuh & seret orb dengan jari, lalu lepaskan dengan kecepatan usapan (fling) untuk melempar orb.
Fitur Kontrol Tambahan:
•
Switch "Balik arah (Invert Axes)" untuk penyesuaian sumbu HP.
•
Switch "Suara Tabrakan" (ToneGenerator) & "Getaran Haptic" (Vibrator).
•
Tombol "Tes Getar" untuk menguji hardware getar secara langsung.
•
Slider "Sensitivitas Gerak" & Switch "Kurangi Animasi".
2. 🧭 Tab Kompas (Position Sensor)
•
Jarum Kompas Interaktif: Berputar mulus (spring damping) mengarah ke azimuth 0–360°.
•
Arah Mata Angin: Teks indikator Bahasa Indonesia (U, TL, T, TG, S, BD, B, BL).
•
Hue Color Shift: Warna gradien Orb berubah warna secara otomatis menyesuaikan derajat kompas.
•
Peringatan & Dialog Kalibrasi: Indikator akurasi rendah dan panduan gerakan pola angka 8 untuk mengkalibrasi sensor magnetik.
•
Mode Debug Sensor: Menampilkan data mentah ($x, y, z$, lux, azimuth) secara real-time.
3. 💡 Tab Cahaya (Environment Sensor)
Mendukung 3 Mode Tema & Cahaya:
1.
Manual: Memilih tema Terang atau Gelap secara langsung via RadioButton.
2.
Realtime: Tema dan Lux Meter Gauge berubah secara otomatis dan kontinu mengikuti tingkat cahaya ambient (moving average + histeresis).
3.
Tutup / Senter (Reaktif):
◦
🖐️ Tutup Sensor ($< 30\text{ Lux}$ / ditutupi jari) $\rightarrow$ Otomatis berubah ke Mode GELAP.
◦
🔦 Disenter / Terang ($\ge 30\text{ Lux}$ / disinari lampu) $\rightarrow$ Otomatis berubah ke Mode TERANG.
•
Slider Simulasi Cahaya: Memungkinkan pengujian manual tema pada Emulator tanpa sensor fisik.
⚙️ Lifecycle Awareness & Penghematan Daya
•
Registrasi Terkendali:
◦
Listener sensor utama di-register pada onResume() dan dilepas (unregistered) pada onPause() di MainActivity.kt untuk mencegah listener leak dan pengurasan baterai.
•
Optimasi Gyroscope:
◦
Sensor Gyroscope HANYA di-register saat mode GYRO dipilih, dan langsung dilepas saat berpindah ke mode lain atau saat aplikasi di-background.
🏗️ Arsitektur Proyek
Aplikasi dibangun menggunakan pola MVVM (Model-View-ViewModel) dengan struktur direktori sebagai berikut:
com.example.sensorplayground/
 ├─ MainActivity.kt                # Activity utama & lifecycle listener (onResume / onPause)
 ├─ sensor/
 │   ├─ SensorController.kt        # Membungkus SensorManager & SensorEventListener
 │   ├─ SensorModels.kt            # Data class (MotionState, GyroState, LightState, CompassState)
 │   └─ SensorFilters.kt           # Low-pass filter, smooth angle, average lux, histeresis
 ├─ data/
 │   └─ SettingsRepository.kt     # Pengaturan persistent menggunakan DataStore Preferences
 ├─ util/
 │   └─ FeedbackManager.kt        # Pengelola audio ToneGenerator & haptic Vibrator/VibratorManager
 ├─ viewmodel/
 │   └─ SensorViewModel.kt        # Integrasi fisika 60fps, logika mode, dan state holder
 ├─ ui/
 │   ├─ theme/                    # Color.kt, Theme.kt, Type.kt (MaterialTheme colorScheme)
 │   ├─ components/
 │   │   ├─ OrbCanvas.kt          # Canvas fisika Orb, gestur Drag & Fling
 │   │   ├─ CompassView.kt        # Tampilan kompas & jarum berputar
 │   │   ├─ LightMeterCard.kt     # Gauge & indikator lux
 │   │   └─ SensorStatusChip.kt   # Chip status aktif/tidak tersedia per sensor
 │   └─ screens/
 │       └─ HomeScreen.kt         # Layout Material 3 NavigationBar (3 Tab)
🛠️ Persyaratan System & Jalankan Aplikasi
•
Android Studio: Android Studio Ladybug / Koala / 2024.1+
•
JDK: JDK 11+
•
Minimum SDK: Android 7.0 (API Level 24)
•
Target / Compile SDK: Android 15 (API Level 35 / 37)
Cara Menguji di Emulator
1.
Buka Android Studio dan jalankan Emulator (Virtual Device).
2.
Tekan tombol Run (▶) (Shift + F10).
3.
Pada Emulator, buka Extended Controls (...) > Virtual Sensors:
◦
Device Pose: Putar slider Roll/Pitch untuk menguji mode GYRO / TILT.
◦
Light: Ubah slider Lux untuk menguji mode tema Realtime dan Tutup / Senter.
◦
Drag Gesture: Seret dan lempar bola pada Canvas di tab Orb untuk menguji mode Sentuh.
