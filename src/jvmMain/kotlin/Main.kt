import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import components.BarGraph
import components.DateController
import components.TotalCard
import components.tables.AllPaymentTable
import components.tables.RegularExpensesTable
import components.tables.TopExpensesTable
import models.Entry
import java.awt.Dimension
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun App() {
    val currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    var selectedInterval by remember { mutableStateOf("Day") }
    var selectedDate by remember { mutableStateOf(currentDate) }

    val entries by remember {
        mutableStateOf(EntryManager.entries)
    }

    var totalIncome by remember {
        mutableStateOf(calculateIncome(selectedInterval, selectedDate, entries))
    }

    var totalExpense by remember {
        mutableStateOf(calculateExpense(selectedInterval, selectedDate, entries))
    }

    var loadingBit by remember { mutableStateOf(0) }

    // Recalculate total income and expense when selected date or interval changes
    LaunchedEffect(loadingBit, selectedDate, selectedInterval) {
        totalIncome = calculateIncome(selectedInterval, selectedDate, entries)
        totalExpense = calculateExpense(selectedInterval, selectedDate, entries)
    }

    Column(modifier = Modifier.padding(start = 12.dp)) {
        Row(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                TotalCard(label = "Total income", value = totalIncome, backgroundColor = Color(0xFF2DC455))
                TotalCard(label = "Total expense", value = totalExpense, backgroundColor = Color(0xFFFF6E63))
                DateController(selectedInterval,
                    selectedDate,
                    { selectedInterval = it },
                    { selectedDate = it }) { loadingBit = 1 - loadingBit }
            }
            Column(
                modifier = Modifier.weight(2f).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.padding(12.dp).background(Color.LightGray).fillMaxSize()
                ) {
                    BarGraph(entries)
                }
            }
        }
        Row(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier.fillMaxHeight().weight(1f).padding(12.dp).background(Color.LightGray)
            ) {
                AllPaymentTable(
                    selectedInterval, selectedDate, entries
                )
            }
            Box(
                modifier = Modifier.fillMaxHeight().weight(1f).padding(12.dp).background(Color.LightGray)
            ) {
                TopExpensesTable(entries)
            }
            Box(
                modifier = Modifier.fillMaxHeight().weight(1f).padding(12.dp).background(Color.LightGray)
            ) {
                RegularExpensesTable(entries)
            }
        }
    }
}

fun main() = application {
    val options = FirebaseOptions.builder().setCredentials(GoogleCredentials.getApplicationDefault())
        .setDatabaseUrl("https://(default).firebaseio.com").build()
    FirebaseApp.initializeApp(options)

    MaterialTheme {
        Window(
            title = "FinanceTracker", onCloseRequest = ::exitApplication
        ) {
            window.minimumSize = Dimension(1366, 768)
            window.isResizable = false
            App()
        }
    }
}

// Calculate total income based on selected interval and date
private fun calculateIncome(selectedInterval: String, selectedDate: String, entries: MutableList<Entry>): Double {
    return entries.filterByDate(selectedInterval, selectedDate) { it.value > 0.0 }.sumOf { it.value }
}

// Calculate total expense based on selected interval and date
private fun calculateExpense(selectedInterval: String, selectedDate: String, entries: MutableList<Entry>): Double {
    return entries.filterByDate(selectedInterval, selectedDate) { it.value < 0.0 }.sumOf { it.value }
}

// Return double formatted 1000,00 -> 1 000.00
fun Double.toFormattedString(): String = "%,.2f".format(Locale.ROOT, this).replace(",", " ")