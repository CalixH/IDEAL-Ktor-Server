package com.ideal.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureRouting() {
    routing {
        get("/courses") {
            call.respond(getCourses())
        }
        get("/courses/{id}") {
            val id = call.parameters["id"]
            if (id != null) {
                call.respond(getCourse(id.toInt())!!)
            }
        }
    }
}

private fun getCourses(): List<Course> {
    val homedir = System.getProperty("user.home")
    Database.connect("jdbc:sqlite:${homedir}/db.sqlite3")
    var ret: List<Course>? = null
    transaction {
        if (CourseTable.exists()) {
            ret = CourseTable.selectAll().map {
                Course(
                    id = it[CourseTable.id].value,
                    subjectCode = it[CourseTable.subjectCode],
                    catalogNumber = it[CourseTable.catalogNumber],
                    title = it[CourseTable.title],
                    description = it[CourseTable.description],
                    credit = it[CourseTable.credit],
                )
            }
        }
    }
    return ret!!
}

private fun getCourse(id: Int): Course? {
    val homedir = System.getProperty("user.home")
    Database.connect("jdbc:sqlite:${homedir}/db.sqlite3")
    var ret: Course? = null
    transaction {
        ret = CourseTable.select { CourseTable.id eq id }.firstOrNull()?.let {
            Course(
                id = it[CourseTable.id].value,
                subjectCode = it[CourseTable.subjectCode],
                catalogNumber = it[CourseTable.catalogNumber],
                title = it[CourseTable.title],
                description = it[CourseTable.description],
                credit = it[CourseTable.credit],
                requirementsDescription = it[CourseTable.requirementsDescription]
            )
        }
    }
    return ret
}

object CourseTable: IntIdTable() {
    val subjectCode = varchar("subjectCode", 7)
    val catalogNumber = varchar("catalogNumber", 7)
    val title = varchar("title", 255)
    val description = varchar("description", 4095)
    val credit = double("credit")
    val requirementsDescription = varchar("requirementsDescription", 2047)
}

@Serializable
data class Course(
    val id: Int,
    val subjectCode: String,
    val catalogNumber: String,
    val title: String,
    val description: String,
    val credit: Double,
    val requirementsDescription: String = ""
)
