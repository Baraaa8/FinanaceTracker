package components

import EntryManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.io.File

// Dialog for uploading new entries
// Opened after clicking on the "+" button at the IntervalController
@Composable
fun UploadDialog(
    onDismissRequest: () -> Unit, loading: () -> Unit
) {
    // Upload method for the selected payment type
    val uploadMethods: Map<String, (String) -> Unit> = mapOf(
        "OTP Card" to { EntryManager.readOtpXlsx(it) },
        "OTP SZÉP Card" to { EntryManager.readSzepPdf(it) },
        "Revolut" to { EntryManager.readRevolutCsv(it) },
        "Government sec." to { EntryManager.readGovernXls(it) },
    )

    val paymentTypes: List<String> = uploadMethods.keys.toList() + listOf("Manual", "Later feature")

    val (selectedPaymentType, onPaymentSelected) = remember { mutableStateOf("") }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier.width(400.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Choose the file to upload:",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp),
                    style = MaterialTheme.typography.h6,
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), contentPadding = PaddingValues(8.dp)
                ) {
                    items(paymentTypes.size) { index ->
                        Card(
                            // Later features is disabled
                            modifier = Modifier.padding(4.dp).alpha(if (index == paymentTypes.size - 1) 0.5f else 1f)
                                .clickable { if (index != paymentTypes.size - 1) onPaymentSelected(paymentTypes[index]) },
                            // Blue background color if selected
                            backgroundColor = if (selectedPaymentType == paymentTypes[index]) Color(0xFF81D4FA) else Color.LightGray,
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = paymentTypes[index],
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp),
                                )
                            }
                        }
                    }
                }
                if (selectedPaymentType == "Manual") {
                    ManualEntrySurvey(
                        onDismissRequest, loading
                    )
                } else if (selectedPaymentType.isNotEmpty()) {
                    uploadMethods[selectedPaymentType]?.let {
                        FileSurvey(
                            onDismissRequest, it, loading
                        )
                    }
                }
            }
        }
    }
}

// After selecting anything but "Manual" payment type, we get text field for the file path
@Composable
fun FileSurvey(
    onDismissRequest: () -> Unit, uploadMethod: (String) -> Unit, loading: () -> Unit
) {
    var filePath by remember { mutableStateOf("") }

    Text(
        text = "Add the full path of the file:",
        textAlign = TextAlign.Center,
    )
    TextField(value = filePath,
        onValueChange = { filePath = it.trim('"') },
        label = { Text("File path") },
        placeholder = { Text("C:\\Users\\...") })

    Button(onClick = {
        if (File(filePath).exists()) {
            try {
                uploadMethod(filePath) // call the selected upload method
                loading()
                onDismissRequest()
            } catch (e: Exception) {
                println("Error reading file: $e")
            }
        } else println("File not found")
    }) {
        Text("Upload")
    }
}

// After selecting "Manual" payment type, we get text fields for the manual entry
@Composable
fun ManualEntrySurvey(
    onDismissRequest: () -> Unit, loading: () -> Unit
) {
    var dateValue by remember { mutableStateOf("") }
    var nameValue by remember { mutableStateOf("") }
    var infoValue by remember { mutableStateOf("") }
    var manualValue by remember { mutableStateOf("") }
    val (selectedPut, onPutSelected) = remember { mutableStateOf("Income") }

    Text(
        text = "Fill the required fields:",
        textAlign = TextAlign.Center,
    )
    Row(horizontalArrangement = Arrangement.SpaceAround) {
        listOf("Income", "Outcome").forEach { come ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selectedPut == come, onClick = { onPutSelected(come) })
                Text(
                    text = come,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
        }
    }

    TextField(value = dateValue, onValueChange = {
        dateValue = it
    }, label = { Text("Date") }, placeholder = { Text(text = "YYYY-MM-DD") })
    TextField(
        value = nameValue,
        onValueChange = { nameValue = it },
        label = { Text("Name") },
    )
    TextField(
        value = infoValue,
        onValueChange = { infoValue = it },
        label = { Text("Info") },
    )
    TextField(value = manualValue, onValueChange = {
        if (it.startsWith("+")) {
            onPutSelected("Income")
        } else if (it.startsWith("-")) {
            onPutSelected("Outcome")
        }

        if (it.isEmpty() || it.matches(Regex("^[1-9]\\d*\$"))) {
            manualValue = it
        }
    }, label = { Text("Value") }, trailingIcon = {
        Text("HUF")
    }, leadingIcon = {
        if (selectedPut == "Income") Icon(
            Icons.Default.Add, contentDescription = "Add icon"
        ) else if (selectedPut == "Outcome") Text(
            "-", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold
        )
    })
    Button(onClick = {
        check(dateValue.isNotEmpty()) { "Date is empty" }
        check(dateValue.matches(Regex("\\d{4}-\\d{2}-\\d{2}")).not()) { "Date is not valid" }
        check(nameValue.isNotBlank()) { "Name is empty" }
        check(manualValue.isNotBlank()) { "Value is empty" }
        // Information can be empty

        val value = manualValue.trim().toDouble() * (if (selectedPut == "Income") 1 else -1)
        EntryManager.readManual(dateValue, nameValue.trim(), infoValue.trim(), value)
        loading()
        onDismissRequest()
    }) {
        Text("Upload")
    }
}