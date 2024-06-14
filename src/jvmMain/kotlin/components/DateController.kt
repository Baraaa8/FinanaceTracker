package components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Under the TotalCards, DateInterval and DateSelector are displayed
@Composable
fun DateController(
    selectedInterval: String,
    selectedDate: String,
    setSelectedInterval: (String) -> Unit,
    setSelectedDate: (String) -> Unit,
    loading: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        DateInterval(selectedInterval, setSelectedInterval)
        DateSelector(selectedDate, setSelectedDate, loading)
    }
}

// Radio button group, select the interval
@Composable
fun DateInterval(selectedInterval: String, setSelectedInterval: (String) -> Unit) {
    val radioOptions = listOf("Day", "Month", "Year")

    Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Center) {
        radioOptions.forEach { text ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = (text == selectedInterval), onClick = { setSelectedInterval(text) })
                Text(
                    text = text
                )
            }
        }
    }
}

// TextField for date input
@Composable
fun DateSelector(
    selectedDate: String,
    setSelectedDate: (String) -> Unit,
    loading: () -> Unit
) {
    var uploadDialog by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.padding(start = 15.dp).width(200.dp).fillMaxHeight()
    ) {
        Row {
            TextField(value = selectedDate, onValueChange = { setSelectedDate(it) }, label = { Text("Date") })
        }
        Row {
            Button(shape = CircleShape, colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green), onClick = {
                uploadDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }

        // Button to add a new entry (open PaymentTypeSelectorDialog)
        if (uploadDialog) {
            UploadDialog(
                onDismissRequest = { uploadDialog = false },
                loading
            )
        }
    }
}