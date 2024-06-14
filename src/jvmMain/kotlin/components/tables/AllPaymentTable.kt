package components.tables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import filterByDate
import models.Entry

// Display all payments based on the selected interval and date (first table)
@Composable
fun AllPaymentTable(selectedInterval: String, selectedDate: String, entries: MutableList<Entry>) {
    val filteredEntries = entries.filterByDate(selectedInterval, selectedDate).sortedByDescending { it.date }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text("Transactions", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.h5)
        for (entry in filteredEntries) {
            EntryCard(entry)
        }
    }
}

fun String.getMonth(): String = takeIf { it.length >= 7 }?.substring(0, 7) ?: "" // 2024-01-01 -> 2024-01
fun String.getYear(): String = takeIf { it.length >= 4 }?.substring(0, 4) ?: "" // 2024-01-01 -> 2024