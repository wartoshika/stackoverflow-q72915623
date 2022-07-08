package test

import kotlinx.coroutines.*
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import javax.persistence.*
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

fun main() {
    SpringApplication.run(Main::class.java)
}

@EnableJpaRepositories
@SpringBootApplication
open class Main {

    @OptIn(ExperimentalTime::class)
    @Bean
    open fun run(
        jdbcTemplate: NamedParameterJdbcTemplate,
        definitionRepository: IdentifierDefinitionRepository,
        listValueRepository: IdentifierListValuesRepository
    ) = CommandLineRunner {

        val status = (0 until 3).map { random(10) }
        val types = (0 until 2).map { random(12) }
        val identifierValue = (0 until 10).map { random(15) }


        if (definitionRepository.count() == 0L) {
            println("seeding IdentifierDefinition")
            (0 until 3).forEach {
                definitionRepository.save(
                    IdentifierDefinition(
                        definition_id = 1,
                        status = status[Random.nextInt(0, status.size)],
                        type = types[Random.nextInt(0, types.size)],
                    )
                )
            }
        }


        if (listValueRepository.count() == 0L) {
            println("seeding IdentifierListValues")
            runBlocking {
                (0 until 10000).map {
                    async {
                        withContext(Dispatchers.IO) {
                            listValueRepository.save(
                                IdentifierListValues(
                                    definition_id = 1,
                                    identifier_value = identifierValue[Random.nextInt(0, identifierValue.size)]
                                )
                            )
                        }
                    }
                }.awaitAll()
            }
        }


        println("reading db")
        val query = """
            SELECT iv.* FROM identifier_definition id
            INNER JOIN identifier_list_values iv on id.definition_id = iv.definition_id where
            id.status IN (:statuses) AND id.type = :listType AND iv.identifier_value IN (:valuesToAdd)
        """.trimIndent()

        val statusesToQuery = (0 until 1).map { status[Random.nextInt(0, status.size)] }
        val typeToQuery = types[Random.nextInt(0, types.size)]
        val identifierValuesToQuery = (0 until 1).map { identifierValue[Random.nextInt(0, identifierValue.size)] }

        val time = measureTime {
            jdbcTemplate.query(query, MapSqlParameterSource().apply {
                addValue("statuses", statusesToQuery)
                addValue("listType", typeToQuery)
                addValue("valuesToAdd", identifierValuesToQuery)
            }) {
                println("reading row ${it.row}")
            }
        }
        println("done reading in $time")
    }


    private fun random(length: Int): String {
        return (0 until length).joinToString {
            Random.nextInt(64 until 90).toChar().toString()
        }
    }
}

@Repository
interface IdentifierDefinitionRepository : JpaRepository<IdentifierDefinition, Int>

@Repository
interface IdentifierListValuesRepository : JpaRepository<IdentifierListValues, Int>

@Entity
@Table(name = "identifier_definition")
class IdentifierDefinition(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @field:Column(name="definition_id")
    var definition_id: Int? = null,
    @field:Column(name="status")
    var status: String? = null,
    @field:Column(name="type")
    var type: String? = null,
)

@Entity
@Table(name = "identifier_list_values")
class IdentifierListValues(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @field:Column(name="definition_id")
    var definition_id: Int? = null,
    @field:Column(name="identifier_value")
    var identifier_value: String? = null,
)