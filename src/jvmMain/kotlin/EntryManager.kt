import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.cloud.firestore.CollectionReference
import com.google.firebase.cloud.FirestoreClient
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import components.tables.getMonth
import components.tables.getYear
import models.Entry
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

// Entry manager object, handle reading files and creating entries
object EntryManager {

    private val collection: CollectionReference =
        FirestoreClient.getFirestore().collection(System.getenv("COLLECTIONNAME")?: "entries") // Collection name

    var entries: MutableList<Entry> by mutableStateOf(collection.get().get().map { entry ->
        Entry(
            id = entry.getString("id"),
            date = entry.getString("date"),
            information = entry.getString("information") ?: "",
            name = entry.getString("name") ?: "",
            value = entry.getDouble("value") ?: 0.0
        )
    }.toMutableList())

    // Reading Revolut csv file
    fun readRevolutCsv(filePath: String) {
        val inputStream = FileInputStream(filePath)
        val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))

        reader.useLines { lines ->
            lines.forEach { line ->
                val parts = line.split(",")
                if (parts[1] == "Current" && parts[8] == "COMPLETED") {
                    val date = parts[2].split(' ')[0]
                    val name = parts[4].replace(
                        Regex("^From |^To "), ""
                    ) // eliminate From and To from the beginning of the string
                    val value = parts[5].toDouble() - parts[6].toDouble()
                    Entry(
                        id = "REV-${UUID.nameUUIDFromBytes("$date$name$value".toByteArray())}",
                        date = date,
                        name = name,
                        value = value
                    ).save()
                }
            }
        }
        reader.close()
    }

    // Reading government sec. xls file
    fun readGovernXls(filePath: String) {
        val inputStream = FileInputStream(filePath)
        val xlWb = WorkbookFactory.create(inputStream)
        val xlWs = xlWb.getSheetAt(0)
        var rowNumber = 1

        do {
            val row = xlWs.getRow(rowNumber)
            val cell2 = row.getCell(2).toString()
            if (cell2 in listOf("Eladás", "Vétel")) {
                val date = row.getCell(9).toString().replace(".", "-").trimEnd('-')
                val information = row.getCell(1).toString()
                val name = row.getCell(4).toString()
                // Eladás should be negative
                // If Vétel is negative, then buying "stock" shows as double loss
                val value = row.getCell(7).toString().toDouble() * if (cell2 == "Eladás") -1 else 1
                Entry(
                    id = "GOVERN-${row.getCell(3)}", date = date, information = information, name = name, value = value
                ).save()
            }
            rowNumber++
        } while (xlWs.getRow(rowNumber).getCell(2) != null)

        xlWb.close()
    }

    // Reading OTP xlsx file
    // https://internetbank.otpbank.hu/
    fun readOtpXlsx(filePath: String) {
        val inputStream = FileInputStream(filePath)
        val xlWb = WorkbookFactory.create(inputStream)
        val xlWs = xlWb.getSheetAt(0)
        var rowNumber = 1

        do {
            val row = xlWs.getRow(rowNumber)
            val informationTemp = row.getCell(7).toString()
            val substringStart = (Regex("\\d{10}").find(informationTemp)?.range?.last?.plus(1)) ?: 0
            val substringEnd =
                informationTemp.indexOf("-APPLE").takeIf { it != -1 } ?: informationTemp.indexOf("-ÉRINTŐ")
                    .takeIf { it != -1 } ?: informationTemp.length // cut down -APPLE or -ÉRINTŐ from the end

            val date = row.getCell(0).toString().split(' ')[0]
            val information =
                informationTemp.substring(substringStart, substringEnd).trimStart(' ').replace(Regex(" {2,}"), "")
            val name = row.getCell(4).toString()
            val value = row.getCell(10).toString().toDouble()
            Entry(
                id = "OTP-${UUID.nameUUIDFromBytes("$date$information$name$value".toByteArray())}",
                date = date,
                information = information,
                name = name,
                value = value
            ).save()
            rowNumber++
        } while (xlWs.getRow(rowNumber) != null)

        xlWb.close()
    }

    // Reading Szep pdf file
    fun readSzepPdf(filePath: String) {
        val pdfReader = PdfReader(filePath)
        val extractedLines = PdfTextExtractor.getTextFromPage(pdfReader, 1).split("\n")

        for (line in extractedLines) {
            val parts = line.split(',', limit = 4)

            if (line.contains("Kártyás vásárlás")) {
                val date = parts[0].split("  ")[0].convertDate()
                val (name, value) = parts[3].split(Regex(" -"))
                Entry(
                    id = "SZEP-${parts[2].trimStart(' ').split(' ')[0]}",
                    date = date,
                    name = name.trim(' '),
                    value = "-${value.replace(".", "")}".toDouble()
                ).save()
            } else if (line.contains("Átutalás")) {
                val date = parts[0].split("  ")[0].convertDate()
                val name: String = parts[2].trim(' ')
                val value: Double = parts[3].split(Regex("(LEVBANK-(\\d*-?)*)|(/ )"))[1].trim().replace(".", "").toDouble()
                Entry(
                    id = "SZEP-${UUID.nameUUIDFromBytes("$date$name$value".toByteArray())}",
                    date = date,
                    name = name,
                    value = value
                ).save()
            }
        }
        pdfReader.close()
    }

    // Create entry from manual values
    fun readManual(date: String, name: String, info: String, value: Double) {
        Entry(
            id = "MAN-${UUID.nameUUIDFromBytes("$date$info$name$value".toByteArray())}",
            date = date,
            name = name,
            information = info,
            value = value
        ).save()
    }


    // Check duplication and save entry locally and in Firebase
    private fun Entry.save() {
        if (this !in entries) {
            entries.add(this)
            collection.document(this.id!!).set(this)
        }
    }
}

// Convert date format from yy.MM.dd to yyyy-MM-dd
private fun String.convertDate(): String {
    val inputFormat = DateTimeFormatter.ofPattern("yy.MM.dd")
    val outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return LocalDate.parse(this, inputFormat).format(outputFormat)
}

// Filter entryList based on selected interval and date (additional predicate can be passed)
fun MutableList<Entry>.filterByDate(
    selectedInterval: String, selectedDate: String, predicate: (Entry) -> Boolean = { true }
): Sequence<Entry> {
    return when (selectedInterval) {
        "Day" -> this.asSequence().filter { it.date == selectedDate && predicate(it) }
        "Month" -> this.asSequence().filter { it.date?.getMonth() == selectedDate.getMonth() && predicate(it) }
        "Year" -> this.asSequence().filter { it.date?.getYear() == selectedDate.getYear() && predicate(it) }
        else -> emptySequence()
    }
}