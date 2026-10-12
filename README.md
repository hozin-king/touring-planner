# Touring Planner — Sumatra Touring Route & ETA Planner

Aplikasi Android (native, Kotlin + Jetpack Compose) untuk merencanakan touring
motor 9 hari (loop) Sumatera — port dari engine C++17 `TouringPlannerEngine`.

- Jadwal ETA per hari: tiba/berangkat tiap titik, akumulasi jarak & BBM
- Peringatan otomatis: tiba lewat Maghrib (>18:00), segmen antar-SPBU > 150 km
- Parameter bisa diubah: tanggal/jam start & kecepatan rata-rata → hitung ulang
- Tombol "Google Maps": buka full loop rute di aplikasi Maps

Motor acuan: Honda BeAT Deluxe Gen 2 (45 km/jam, 50 km/L Pertalite Rp 10.000/L,
tangki 4,2 L).

Build lokal: butuh Android SDK + `gradle :app:assembleDebug`.
CI: GitHub Actions otomatis build APK debug tiap push ke `main`.
