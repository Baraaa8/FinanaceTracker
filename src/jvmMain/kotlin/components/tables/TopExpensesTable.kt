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

// Take the 20 highest negative value entries (second table)
@Composable
fun TopExpensesTable(entries: MutableList<Entry>) {
    val sortedEntries = entries.asSequence().filter { it.value < 0.0 }.sortedBy { it.value }.take(20)

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text("Top 20 expenses", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.h5)
        for (entry in sortedEntries) {
            EntryCard(entry)
        }
    }
}