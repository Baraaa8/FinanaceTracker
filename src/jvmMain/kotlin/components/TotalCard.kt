package components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import toFormattedString

// Card displaying the total income or expense (top left corner)
@Composable
fun TotalCard(label: String, value: Double, backgroundColor: Color) {
    Card(
        backgroundColor = backgroundColor,
        modifier = Modifier.size(width = 330.dp, height = 100.dp).padding(12.dp)
    ) {
        Column {
            Text(
                text = label, fontSize = MaterialTheme.typography.subtitle1.fontSize, fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, top = 8.dp)
            )
            Text(
                text = value.toFormattedString(),
                fontSize = MaterialTheme.typography.h6.fontSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.End).padding(end = 8.dp, bottom = 8.dp)
            )
        }
    }
}