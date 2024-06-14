package components.tables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import models.Entry
import toFormattedString

// Small card displayed in the tables
@Composable
fun EntryCard(entry: Entry) {
    var showDialog by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp).height(80.dp).clickable {
        showDialog = true
    }) {
        Row(modifier = Modifier.fillMaxWidth().padding(6.dp)) {
            // If it has id, then it is a payment entry, otherwise a calculated entry
            // Only used for the first table
            if (entry.id != null) {
                Column(
                    modifier = Modifier.fillMaxHeight().padding(8.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (entry.value < 0.0) Icon(Icons.Default.ShoppingCart, contentDescription = "Payment")
                    else Icon(Icons.Default.Add, contentDescription = "Income")
                }
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(text = entry.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                entry.date?.let { Text(text = it) } // In the third table, the date is not displayed in the card (grouped by place)
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // In the third table, the information is not displayed in the card (grouped by place)
                entry.information?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.caption,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(text = "${entry.value.toFormattedString()} Ft", fontWeight = FontWeight.Bold)
            }
        }
    }
    if (showDialog) {
        ExtendedEntryCardDialog(entry = entry, onDismissRequest = { showDialog = false })
    }
}

// Extend the EntryCard with fully, readable infos
@Composable
fun ExtendedEntryCardDialog(
    entry: Entry, onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier.width(450.dp).height(100.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(6.dp)) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = entry.name, fontWeight = FontWeight.Bold
                    )
                    // In the third table, the date is not displayed in the card (grouped by place)
                    entry.date?.let { Text(text = it) }
                }
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    // In the third table, the information is not displayed in the card (grouped by place)
                    entry.information?.let {
                        Text(
                            text = it, style = MaterialTheme.typography.caption
                        )
                    }
                    Text(text = "${entry.value.toFormattedString()} Ft", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}