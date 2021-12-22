import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import entities.Country
import entities.loadFromFile
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.exposed.sql.Database
import java.io.IOException
import java.io.StringReader
import java.util.*
import kotlin.collections.ArrayList
import mu.KotlinLogging
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.redundent.kotlin.xml.xml


private val logger = KotlinLogging.logger {}
private val client = OkHttpClient()

fun main(args: Array<String>){
    val rawJson = fetchCountries()
    val countries = serializeCountries(rawJson)
    val countriesMap = mutableMapOf<String, Country>()
//    println(countries.size)
    for (country in countries){
        countriesMap[country.cca3] = country
    }
//    println(Arrays.toString(countriesMap["GBR"]?.capital))
    val something = loadFromFile()
//    println(something.fth.planes[0].name)

//    logger.trace { "This is trace log" }
//    logger.debug { "This is debug log" }
//    logger.info { "This is info log" }
//    logger.warn { "This is warn log" }
//    logger.error { "This is error log" }

    Database.connect("jdbc:sqlite:db/data.sqlite", "org.sqlite.JDBC")
//    Database.connect("jdbc:h2:~/db/h2db", "org.h2.Driver")
    transaction {
        // print sql to std-out
        addLogger(StdOutSqlLogger)

        SchemaUtils.create (Cities)

        // insert new city. SQL: INSERT INTO Cities (name) VALUES ('St. Petersburg')
        val stPete = City.new {
            name = "St. Petersburg"
        }

        // 'select *' SQL: SELECT Cities.id, Cities.name FROM Cities
        println("Cities: ${City.all()}")
    }

    val people = xml("people") {
        xmlns = "http://example.com/people"
        "person" {
            attribute("id", 1)
            "firstName" {
                -"John"
            }
            "lastName" {
                -"Doe"
            }
            "phone" {
                -"555-555-5555"
            }
        }
    }

    val asString = people.toString()
    println(asString)
}

object Cities: IntIdTable() {
    val name = varchar("name", 50)
}

class City(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<City>(Cities)

    var name by Cities.name
}


fun fetchCountries(): String {
    val request = Request.Builder()
        .url("https://restcountries.com/v3.1/all?fields=name,capital,cca3,borders")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        for ((name, value) in response.headers) {
            println("$name: $value")
        }

        return response.body!!.string()
    }
}

fun serializeCountries(array: String): ArrayList<Country> {
    val klaxon = Klaxon()
    val result = arrayListOf<Country>()
    JsonReader(StringReader(array)).use { reader ->
        reader.beginArray {
            while (reader.hasNext()) {
                val person = klaxon.parse<Country>(reader)
                if (person != null){
                    result.add(person)
                }

            }
        }
    }
    return result
}