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
import models.Entry

// Display the top 20 total expenses grouped by place (third table)
@Composable
fun RegularExpensesTable(entries: MutableList<Entry>) {
    val regularExpenses = entries.asSequence().filter { it.value < 0.0 }.groupBy { it.name }
        .map { group -> group.key to group.value.sumOf { entry -> entry.value } }.sortedBy { it.second }.take(20)

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text(
            "Top 20 places with the most expense",
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.h5
        )
        for ((name, value) in regularExpenses) {
            val entry = Entry(
                id = null, date = null, information = null, name = name, value = value
            )
            EntryCard(entry)
        }
    }
}