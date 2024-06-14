package components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aay.compose.barChart.BarChart
import com.aay.compose.barChart.model.BarParameters
import components.tables.getMonth
import models.Entry
import java.time.LocalDateTime

// Bar graph displaying the income and expense for the last 6 months (top right corner)
@Composable
fun BarGraph(entries: MutableList<Entry>) {
    val currentDate = LocalDateTime.now()

    // Get the previous 6 months' dates in format like 2024-01
    val previousMonths = mutableListOf<String>()
    for (i in 0..5) {
        val previousMonth = currentDate.minusMonths(i.toLong())
        previousMonths.add(0, String.format("%d-%02d", previousMonth.year, previousMonth.monthValue))
    }

    // Group by month and sum the income
    val incomePerMonths = entries.asSequence().filter { it.value > 0.0 }.groupBy { it.date!!.getMonth() }
        .map { it.key to it.value.sumOf { entry -> entry.value } }

    // Group by month and sum the expense
    val expensePerMonths = entries.asSequence().filter { it.value < 0.0 }.groupBy { it.date!!.getMonth() }
        .map { it.key to it.value.sumOf { entry -> entry.value } }

    val incomeList = mutableListOf<Double>()
    val expenseList = mutableListOf<Double>()

    for (month in previousMonths) {
        // Add the income for the last 6 months
        incomeList.add(incomePerMonths.find { it.first == month }?.second ?: 0.0)

        // Add the expense for the last 6 months
        // Multiply by -1 to get the positive value
        expenseList.add(expensePerMonths.find { it.first == month }?.second?.times(-1.0) ?: 0.0)
    }

    val barParameters: List<BarParameters> = listOf(
        BarParameters(
            dataName = "Income", data = incomeList, barColor = Color.Green,
        ), BarParameters(
            dataName = "Expense",
            data = expenseList,
            barColor = Color.Red,
        )
    )

    BarChart(
        chartParameters = barParameters, gridColor = Color.DarkGray, xAxisData = previousMonths, yAxisStyle = TextStyle(
            fontSize = 14.sp,
            color = Color.DarkGray,
        ), xAxisStyle = TextStyle(
            fontSize = 10.sp, color = Color.DarkGray
        ), yAxisRange = 10, barWidth = 40.dp
    )
}