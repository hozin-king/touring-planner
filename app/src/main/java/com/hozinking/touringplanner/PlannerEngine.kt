package com.hozinking.touringplanner

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToLong

// ------------------------------------------------------------------
// Port Kotlin dari touring_planner.hpp/.cpp (C++17).
// "Sumatra Touring Route & ETA Planner Engine" — Honda BeAT Deluxe Gen 2
// ------------------------------------------------------------------

data class Waypoint(
    val name: String,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val placeId: String = "",
    val distanceKm: Double = 0.0,   // jarak DARI waypoint sebelumnya
    val restDurationMin: Int = 0,    // istirahat SETELAH tiba (menit)
    val category: String            // "start" | "rest" | "fuel" | "stay" | "finish"
)

data class ScheduleItem(
    val waypointName: String,
    val category: String,
    val arrivalTimeStr: String,     // "01 Jan 2027 06:30"
    val departureTimeStr: String,   // "-" bila stay/finish
    val totalDistanceKm: Double,
    val fuelConsumedLiters: Double,
    val fuelCostIdr: Double,
    val legDistanceKm: Double,      // jarak etape ini saja
    val restDurationMin: Int        // istirahat di titik ini (0 bila stay/finish)
)

object TouringPlannerEngine {
    // ---- Konstanta motor ----
    const val AVG_SPEED_KMH = 45.0
    const val FUEL_EFFICIENCY_KM_L = 50.0
    const val FUEL_PRICE_IDR_L = 10000.0
    const val TANK_CAPACITY_L = 4.2
    const val SAFE_REFILL_KM = 150.0

    // ---- Aturan jadwal harian ----
    const val NEXT_DAY_START_HOUR = 6
    const val NEXT_DAY_START_MIN = 30
    const val LODGING_TARGET_HOUR = 18 // target tiba <= 18:00 (maks. Maghrib)

    // Parameter yang bisa diubah user dari aplikasi
    var avgSpeedKmh: Double = AVG_SPEED_KMH
    var startDateTime: LocalDateTime = LocalDateTime.of(2027, 1, 1, 6, 30)

    private val waypoints = mutableListOf<Waypoint>()
    private val fmt = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale("id"))

    fun addWaypoint(wp: Waypoint) = waypoints.add(wp)
    fun clearWaypoints() = waypoints.clear()
    fun waypointCount(): Int = waypoints.size

    fun calculateItinerary(): List<ScheduleItem> {
        val out = mutableListOf<ScheduleItem>()
        var cur = startDateTime
        var totalDist = 0.0

        waypoints.forEachIndexed { i, wp ->
            // Aturan inap: setelah "stay", hari berikutnya mulai 06:30.
            if (i > 0 && waypoints[i - 1].category == "stay") {
                cur = cur.plusDays(1)
                    .withHour(NEXT_DAY_START_HOUR)
                    .withMinute(NEXT_DAY_START_MIN)
            }
            val travelMin = (wp.distanceKm / avgSpeedKmh * 60.0).roundToLong()
            val arrival = cur.plusMinutes(travelMin)

            val endsDay = wp.category == "stay" || wp.category == "finish"
            val departure = if (endsDay) arrival
                            else arrival.plusMinutes(wp.restDurationMin.toLong())

            totalDist += wp.distanceKm
            val fuelL = totalDist / FUEL_EFFICIENCY_KM_L

            out.add(
                ScheduleItem(
                    waypointName = wp.name,
                    category = wp.category,
                    arrivalTimeStr = arrival.format(fmt),
                    departureTimeStr = if (endsDay) "-" else departure.format(fmt),
                    totalDistanceKm = totalDist,
                    fuelConsumedLiters = fuelL,
                    fuelCostIdr = fuelL * FUEL_PRICE_IDR_L,
                    legDistanceKm = wp.distanceKm,
                    restDurationMin = if (endsDay) 0 else wp.restDurationMin
                )
            )
            cur = departure
        }
        return out
    }

    /** Peringatan bila ada segmen antar-SPBU melebihi [SAFE_REFILL_KM]. */
    fun checkFuelRange(): List<String> {
        val warn = mutableListOf<String>()
        var sinceFuel = 0.0
        var segStart = waypoints.firstOrNull()?.name ?: "-"
        for (wp in waypoints) {
            sinceFuel += wp.distanceKm
            if (wp.category == "fuel") {
                if (sinceFuel > SAFE_REFILL_KM) {
                    warn.add(
                        "Segmen $segStart -> ${wp.name} = " +
                        "${"%.0f".format(sinceFuel)} km " +
                        "(melebihi batas aman ${"%.0f".format(SAFE_REFILL_KM)} km)"
                    )
                }
                sinceFuel = 0.0
                segStart = wp.name
            }
        }
        return warn
    }

    /** URL full loop rute (link Google Maps milik user). */
    fun generateGoogleMapsUrl(): String = MAPS_LOOP_URL

    const val MAPS_LOOP_URL: String =
        "https://www.google.com/maps/dir/" +
        "MADRASAH+DINIYYAH+TAKMILIYAH+MIFTAHUL+HUDA+BUNGO+TANJUNG/" +
        "Bangko/CitraRaya+Jambi/OYO+140+Hotel+Graha+Bukit+Syariah/" +
        "Homestay+Bangau+Putih/HOTEL+KRUI+SYARIAH/" +
        "Hotel+Pantai+Panjang+RedPartner/Mushola+Baitul+Mukminin/" +
        "360+Homestay/Penginapan+Syariah+Rumah+Ummi/" +
        "MADRASAH+DINIYYAH+TAKMILIYAH+MIFTAHUL+HUDA+BUNGO+TANJUNG/" +
        "data=!4m71!4m70!1m5!1m4!1s0x2e2e671610d5505f:0x1f3a878225384946!8m2!3d-1.8735739!4d102.4236304" +
        "!1m5!1m4!1s0x2e2e6d6a734b0dbb:0x8d8e0d5197aa04af!8m2!3d-2.0735579!4d102.275002" +
        "!1m5!1m4!1s0x2e2f62b38debc84b:0x7bb6df36e1129fab!8m2!3d-1.6319332!4d103.5280331" +
        "!1m5!1m4!1s0x2e3b75fd774b9937:0xe8607d21fcba57f4!8m2!3d-2.9884188999999997!4d104.7513097" +
        "!1m5!1m4!1s0x2e38734789408179:0xc3af092cf336e90d!8m2!3d-4.8111055!4d103.92790269999999" +
        "!1m5!1m4!1s0x2e4794c2b953042b:0x94e36ad10c185810!8m2!3d-5.1843077!4d103.9363262" +
        "!1m5!1m4!1s0x2e36b1d1d92879ed:0x630945abd1d653d2!8m2!3d-3.8021954!4d102.2579184" +
        "!1m5!1m4!1s0x2e325100117eed8b:0x3a585d7814b04ebc!8m2!3d-2.9284320999999998!4d101.4452257" +
        "!1m5!1m4!1s0x2fd4b94c3a37c39b:0x61dbc91580dd5d06!8m2!3d-0.9605049!4d100.3548598" +
        "!1m5!1m4!1s0x2e2b51ba02dbf7d7:0x472ec8497cbebad!8m2!3d-1.0669762!4d100.7849167" +
        "!1m5!1m4!1s0x2e2e671610d5505f:0x1f3a878225384946!8m2!3d-1.8735739!4d102.4236304" +
        "!2m2!2b1!11b1!3e0"
}
