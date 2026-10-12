package com.hozinking.touringplanner

// ------------------------------------------------------------------
// Data rute 9 hari (loop) Sumatera, 1-9 Jan 2027 — revisi 12 Okt 2026.
// Prinsip: tiba MAKS. Maghrib (~18:00) tiap hari, inap <= Rp 150rb/malam.
// ------------------------------------------------------------------

data class DayRoute(val label: String, val title: String, val waypoints: List<Waypoint>)

fun buildRoute(): List<DayRoute> = listOf(
    DayRoute("HARI 1", "Bungo Tanjung → Bayung Lencir", listOf(
        Waypoint("Start Bungo Tanjung", -1.874251, 102.422339, "", 0.0, 0, "start"),
        Waypoint("Bangko - Bakso Mbah Bejo + isi BBM", 0.0, 0.0, "", 28.0, 30, "fuel"),
        Waypoint("Sarolangun - Saoenk Bang Radja + isi BBM", 0.0, 0.0, "", 95.0, 45, "fuel"),
        Waypoint("CitraRaya Jambi via Jalur Ness", 0.0, 0.0, "", 120.0, 60, "rest"),
        Waypoint("SPBU Simpang Paal 10 Jambi", 0.0, 0.0, "", 12.0, 15, "fuel"),
        Waypoint("SPBU Bayung Lencir", 0.0, 0.0, "", 110.0, 15, "fuel"),
        Waypoint("Penginapan budget Bayung Lencir (<= Rp 150rb)", 0.0, 0.0, "", 5.0, 0, "stay"),
    )),
    DayRoute("HARI 2", "Bayung Lencir → Baturaja", listOf(
        Waypoint("SPBU Lintas Bayung Lencir-Palembang (km 80)", 0.0, 0.0, "", 80.0, 15, "fuel"),
        Waypoint("Palembang - makan siang + isi BBM", -2.9884189, 104.7513097, "", 76.0, 30, "fuel"),
        Waypoint("SPBU Prabumulih", 0.0, 0.0, "", 95.0, 15, "fuel"),
        Waypoint("SPBU Baturaja", 0.0, 0.0, "", 105.0, 15, "fuel"),
        Waypoint("Penginapan budget Baturaja (<= Rp 150rb)", 0.0, 0.0, "", 5.0, 0, "stay"),
    )),
    DayRoute("HARI 3", "Baturaja → Danau Ranau", listOf(
        Waypoint("SPBU Simpang Sender Ranau", 0.0, 0.0, "", 110.0, 15, "fuel"),
        Waypoint("Homestay Bangau Putih Danau Ranau", -4.8111055, 103.9279027,
            "ChIJeYFAiUdzOC4RDek28ywJr8M", 12.0, 0, "stay"),
    )),
    DayRoute("HARI 4", "Danau Ranau → Krui", listOf(
        Waypoint("HOTEL KRUI SYARIAH", -5.1843077, 103.9363262,
            "ChIJKwRTucKURy4REFgYDNFq45Q", 74.0, 0, "stay"),
    )),
    DayRoute("HARI 5", "Krui → Bengkulu", listOf(
        Waypoint("SPBU Krui (isi penuh pagi hari)", 0.0, 0.0, "", 3.0, 15, "fuel"),
        Waypoint("SPBU Kaur", 0.0, 0.0, "", 110.0, 15, "fuel"),
        Waypoint("SPBU Manna", 0.0, 0.0, "", 100.0, 15, "fuel"),
        Waypoint("SPBU Bengkulu", 0.0, 0.0, "", 119.0, 15, "fuel"),
        Waypoint("Hotel Pantai Panjang RedPartner Bengkulu", -3.8021954, 102.2579184,
            "ChIJ7Xko2dGxNi4R0lPW0atFCWM", 10.0, 0, "stay"),
    )),
    DayRoute("HARI 6", "Bengkulu → Mukomuko", listOf(
        Waypoint("SPBU Lais", 0.0, 0.0, "", 95.0, 15, "fuel"),
        // Jarak Lais -> Mukomuko belum ada di spek: 0.0 (mohon dilengkapi)
        Waypoint("Rumah Saudara Sumber Makmur Mukomuko (Gratis)", -2.930100, 101.444308,
            "ChIJi-1-EQBRMi4RvE6wFHhdWDo", 0.0, 0, "stay"),
    )),
    DayRoute("HARI 7", "Mukomuko → Padang", listOf(
        Waypoint("SPBU Tapan", 0.0, 0.0, "", 120.0, 15, "fuel"),
        Waypoint("SPBU Painan", 0.0, 0.0, "", 130.0, 15, "fuel"),
        Waypoint("SPBU Veteran Padang", 0.0, 0.0, "", 110.0, 15, "fuel"),
        Waypoint("360 Homestay Padang", -0.9605049, 100.3548598,
            "ChIJm8M3Oky51C8RBl3dgBXJ22E", 14.0, 0, "stay"),
    )),
    DayRoute("HARI 8", "Padang → Alahan Panjang", listOf(
        Waypoint("Sitinjau Lauik (via)", 0.0, 0.0, "", 0.0, 0, "rest"),
        Waypoint("Penginapan Syariah Rumah Ummi Alahan Panjang", -1.0669762, 100.7849167,
            "ChIJ1_fbArpRKy4RrevLl4TscgQ", 69.0, 0, "stay"),
    )),
    DayRoute("HARI 9", "Alahan Panjang → Bungo Tanjung", listOf(
        Waypoint("SPBU Alahan Panjang (isi penuh pagi hari)", 0.0, 0.0, "", 3.0, 15, "fuel"),
        Waypoint("SPBU Kiliran Jao", 0.0, 0.0, "", 120.0, 15, "fuel"),
        Waypoint("SPBU Lintas Kiliran Jao-Muara Bungo (km 75)", 0.0, 0.0, "", 75.0, 15, "fuel"),
        Waypoint("Rest Pasar Muara Bungo", 0.0, 0.0, "", 78.0, 30, "rest"),
        Waypoint("Bungo Tanjung (loop selesai)", -1.874251, 102.422339, "", 68.0, 0, "finish"),
    )),
)
