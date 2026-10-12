package com.hozinking.touringplanner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hozinking.touringplanner.ui.Amber
import com.hozinking.touringplanner.ui.Asphalt
import com.hozinking.touringplanner.ui.CardBg
import com.hozinking.touringplanner.ui.LeafGreen
import com.hozinking.touringplanner.ui.Lilac
import com.hozinking.touringplanner.ui.Muted
import com.hozinking.touringplanner.ui.RailBg
import com.hozinking.touringplanner.ui.SignalRed
import com.hozinking.touringplanner.ui.SkyBlue
import com.hozinking.touringplanner.ui.TouringPlannerTheme
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TouringPlannerTheme {
                PlannerScreen()
            }
        }
    }
}

// ---------- helper ----------

private data class CatMeta(val label: String, val icon: ImageVector, val tint: Color)

private fun catMeta(cat: String) = when (cat) {
    "start" -> CatMeta("Start", Icons.Filled.PlayArrow, LeafGreen)
    "fuel" -> CatMeta("Isi BBM", Icons.Filled.LocalGasStation, Amber)
    "rest" -> CatMeta("Istirahat", Icons.Filled.Restaurant, SkyBlue)
    "stay" -> CatMeta("Menginap", Icons.Filled.Hotel, Lilac)
    else -> CatMeta("Finish", Icons.Filled.Flag, SignalRed)
}

private fun formatIDR(v: Double): String {
    var n = (v + 0.5).toLong().toString()
    val out = StringBuilder()
    var c = 0
    for (i in n.length - 1 downTo 0) {
        out.insert(0, n[i])
        if (++c % 3 == 0 && i > 0) out.insert(0, '.')
    }
    return "Rp " + out.toString()
}

private fun formatIDRShort(v: Double): String {
    val rb = (v / 1000.0)
    return if (rb >= 100) "Rp ${"%.0f".format(rb)}rb" else "Rp ${"%.1f".format(rb).replace('.', ',')}rb"
}

private val hariId = mapOf(
    DayOfWeek.MONDAY to "Senin", DayOfWeek.TUESDAY to "Selasa",
    DayOfWeek.WEDNESDAY to "Rabu", DayOfWeek.THURSDAY to "Kamis",
    DayOfWeek.FRIDAY to "Jumat", DayOfWeek.SATURDAY to "Sabtu",
    DayOfWeek.SUNDAY to "Minggu"
)

private val tglFmt = DateTimeFormatter.ofPattern("d MMM", Locale("id"))

// ---------- layar utama ----------

@Composable
fun PlannerScreen() {
    val context = LocalContext.current
    val days = remember { buildRoute() }

    var speed by remember { mutableStateOf(TouringPlannerEngine.AVG_SPEED_KMH) }
    var startDT by remember { mutableStateOf(TouringPlannerEngine.startDateTime) }
    var selectedDay by remember { mutableStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }

    // Batas indeks waypoint per hari
    val bounds = remember(days) {
        val list = mutableListOf<Pair<Int, Int>>()
        var acc = 0
        for (d in days) {
            val from = acc
            acc += d.waypoints.size
            list.add(from to acc)
        }
        list
    }

    val schedule = remember(speed, startDT) {
        TouringPlannerEngine.avgSpeedKmh = speed
        TouringPlannerEngine.startDateTime = startDT
        TouringPlannerEngine.clearWaypoints()
        days.forEach { day -> day.waypoints.forEach { TouringPlannerEngine.addWaypoint(it) } }
        TouringPlannerEngine.calculateItinerary()
    }

    val warnings = remember(schedule) {
        val w = mutableListOf<String>()
        bounds.forEachIndexed { di, (from, to) ->
            val last = schedule[to - 1]
            val h = last.arrivalTimeStr.takeLast(5).take(2).toIntOrNull() ?: 0
            if ((last.category == "stay" || last.category == "finish") &&
                h >= TouringPlannerEngine.LODGING_TARGET_HOUR
            ) {
                w.add("${days[di].label}: tiba ${last.arrivalTimeStr} — lewat Maghrib!")
            }
        }
        w += TouringPlannerEngine.checkFuelRange()
        w
    }

    val total = schedule.last()
    val (from, to) = bounds[selectedDay]
    val dayItems = schedule.subList(from, to)
    val dayKm = total.let { schedule[to - 1].totalDistanceKm -
        (if (from > 0) schedule[from - 1].totalDistanceKm else 0.0) }
    val dayFuel = schedule[to - 1].fuelConsumedLiters -
        (if (from > 0) schedule[from - 1].fuelConsumedLiters else 0.0)
    val dayDate = startDT.plusDays(selectedDay.toLong())

    Scaffold(
        containerColor = Asphalt,
        bottomBar = {
            BottomAppBar(containerColor = CardBg) {
                OutlinedButton(
                    onClick = { showSettings = true },
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Parameter")
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        val i = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(TouringPlannerEngine.generateGoogleMapsUrl())
                        )
                        context.startActivity(i)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Amber, contentColor = Color(0xFF201503)
                    ),
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Icon(Icons.Filled.Map, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Google Maps", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            item {
                // Header
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("SUMATRA TOURING", color = Muted, fontSize = 11.sp,
                        letterSpacing = 3.sp)
                    Text("Rute & ETA Planner", fontSize = 26.sp,
                        fontWeight = FontWeight.Bold)
                    Text("BeAT Deluxe Gen 2 • ${days.size} hari • " +
                            "${startDT.format(tglFmt)}–${startDT.plusDays((days.size - 1).toLong()).format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id")))}",
                        color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard("Total Jarak", "${"%.0f".format(total.totalDistanceKm)} km",
                            Modifier.weight(1f))
                        StatCard("BBM", "${"%.1f".format(total.fuelConsumedLiters).replace('.', ',')} L",
                            Modifier.weight(1f))
                        StatCard("Biaya BBM", formatIDRShort(total.fuelCostIdr),
                            Modifier.weight(1f))
                    }
                }
            }

            item {
                // Status: peringatan atau aman
                if (warnings.isEmpty()) {
                    Surface(
                        color = LeafGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Row(Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, null, tint = LeafGreen,
                                modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Semua aman — tiba ≤ Maghrib, segmen BBM ≤ 150 km.",
                                fontSize = 13.sp, color = LeafGreen)
                        }
                    }
                } else {
                    Surface(
                        color = SignalRed.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Warning, null, tint = SignalRed,
                                    modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("${warnings.size} peringatan", fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold, color = SignalRed)
                            }
                            warnings.forEach {
                                Text("• $it", fontSize = 12.sp, color = SignalRed,
                                    modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            item {
                // Pilih hari
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(days) { i, _ ->
                        val d = startDT.plusDays(i.toLong())
                        val sel = i == selectedDay
                        Surface(
                            color = if (sel) Amber else CardBg,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.clickable { selectedDay = i }
                        ) {
                            Column(
                                Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("H${i + 1}", fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (sel) Color(0xFF201503) else Color.White)
                                Text(d.format(tglFmt), fontSize = 11.sp,
                                    color = if (sel) Color(0xFF201503).copy(alpha = 0.7f) else Muted)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            item {
                // Judul hari
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(days[selectedDay].label, fontWeight = FontWeight.Bold,
                        fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("${hariId[dayDate.dayOfWeek]}, ${dayDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id")))}",
                        color = Muted, fontSize = 12.sp)
                }
            }

            itemsIndexed(dayItems) { idx, item ->
                WaypointRow(item = item, isLast = idx == dayItems.lastIndex)
            }

            item {
                // Footer hari
                val last = schedule[to - 1]
                Surface(
                    color = CardBg, shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tiba ${last.arrivalTimeStr.takeLast(5)}",
                            fontWeight = FontWeight.Bold, color = Amber, fontSize = 14.sp)
                        Text("${"%.0f".format(dayKm)} km • ${"%.1f".format(dayFuel).replace('.', ',')} L",
                            color = Muted, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (showSettings) {
        SettingsDialog(
            speed = speed,
            startDT = startDT,
            onDismiss = { showSettings = false },
            onApply = { s, dt ->
                speed = s
                startDT = dt
                showSettings = false
            }
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label, color = Muted, fontSize = 11.sp)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Amber)
        }
    }
}

@Composable
private fun WaypointRow(item: ScheduleItem, isLast: Boolean) {
    val m = catMeta(item.category)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        // Kolom jam
        Column(
            modifier = Modifier.width(52.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(item.arrivalTimeStr.takeLast(5), color = Amber,
                fontWeight = FontWeight.Bold, fontSize = 14.sp)
            if (item.departureTimeStr != "-") {
                Text("→${item.departureTimeStr.takeLast(5)}", color = Muted,
                    fontSize = 10.sp)
            }
            Spacer(Modifier.height(2.dp))
        }
        // Rel timeline
        Column(
            modifier = Modifier.padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(32.dp)
                    .background(m.tint.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(m.icon, contentDescription = m.label, tint = m.tint,
                    modifier = Modifier.size(17.dp))
            }
            if (!isLast) {
                Box(
                    modifier = Modifier.width(2.dp).height(30.dp)
                        .background(RailBg)
                )
            }
        }
        // Konten
        Column(modifier = Modifier.weight(1f).padding(bottom = 14.dp)) {
            Text(item.waypointName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = {},
                    label = { Text(m.label, fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(m.icon, contentDescription = null,
                            modifier = Modifier.size(14.dp))
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = m.tint.copy(alpha = 0.14f),
                        labelColor = m.tint,
                        leadingIconContentColor = m.tint
                    )
                )
                Spacer(Modifier.width(6.dp))
                val info = buildString {
                    if (item.legDistanceKm > 0)
                        append("${"%.0f".format(item.legDistanceKm)} km")
                    if (item.restDurationMin > 0) {
                        if (isNotEmpty()) append(" • ")
                        append("istirahat ${item.restDurationMin} mnt")
                    }
                }
                if (info.isNotEmpty()) {
                    Text(info, color = Muted, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun SettingsDialog(
    speed: Double,
    startDT: LocalDateTime,
    onDismiss: () -> Unit,
    onApply: (Double, LocalDateTime) -> Unit
) {
    var d by remember { mutableStateOf(startDT.dayOfMonth.toString()) }
    var mo by remember { mutableStateOf(startDT.monthValue.toString()) }
    var y by remember { mutableStateOf(startDT.year.toString()) }
    var h by remember { mutableStateOf(startDT.hour.toString()) }
    var mi by remember { mutableStateOf(startDT.minute.toString()) }
    var spd by remember { mutableStateOf(speed.toInt().toString()) }
    var err by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Parameter Perjalanan", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tanggal & jam start", color = Muted, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumField(d, { d = it }, "Tgl", Modifier.weight(1f))
                    NumField(mo, { mo = it }, "Bln", Modifier.weight(1f))
                    NumField(y, { y = it }, "Thn", Modifier.weight(1.4f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumField(h, { h = it }, "Jam", Modifier.weight(1f))
                    NumField(mi, { mi = it }, "Mnt", Modifier.weight(1f))
                    NumField(spd, { spd = it }, "Km/jam", Modifier.weight(1.4f))
                }
                err?.let {
                    Text(it, color = SignalRed, fontSize = 12.sp)
                }
                Text("Jadwal & peringatan dihitung ulang otomatis.",
                    color = Muted, fontSize = 11.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val dd = d.toIntOrNull(); val mm = mo.toIntOrNull()
                val yy = y.toIntOrNull(); val hh = h.toIntOrNull()
                val nn = mi.toIntOrNull(); val ss = spd.toDoubleOrNull()
                if (dd == null || mm == null || yy == null || hh == null ||
                    nn == null || ss == null || ss <= 0 ||
                    dd !in 1..31 || mm !in 1..12 || hh !in 0..23 || nn !in 0..59
                ) {
                    err = "Isi semua kolom dengan angka yang valid."
                    return@TextButton
                }
                onApply(ss, LocalDateTime.of(yy, mm, dd, hh, nn))
            }) {
                Text("Hitung Ulang", color = Amber, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = Muted) }
        },
        containerColor = CardBg
    )
}

@Composable
private fun NumField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { v ->
            if (v.all { it.isDigit() } && v.length <= 4) onChange(v)
        },
        label = { Text(label, fontSize = 11.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier
    )
}
