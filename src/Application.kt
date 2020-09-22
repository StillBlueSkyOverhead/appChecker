package com.task.appChecker

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.isMultipart
import io.ktor.request.receiveMultipart
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.html.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.time.Duration

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

val statusReporter = StatusReporter()
val statusFormatter = StatusFormatter()
val compiler = Compiler()
val testRunner = TestRunner()

@ExperimentalCoroutinesApi
@Suppress("unused") // referenced in application.conf
fun Application.module() {

    install(WebSockets) {
        timeout = Duration.ofMinutes(30)
    }

    DbSettings.db // ensure initialization

    transaction {
        SchemaUtils.create(Attempts)
    }

    routing {
        static("/static") {
            resources("static")
        }
        webSocket("/") {
            statusReporter.addSession(this)
            incoming.consumeEach { }
        }
        get("/") {
            handleGetAllAttempts(call)
        }
        get("/attemptDetails/{id}") {
            handleGetAttemptDetails(call)
        }
        get("/newAttempt") {
            handleGetNewAttempt(call)
        }
        post("/newAttempt") {
            handlePostNewAttempt(call)
        }
    }
}

suspend fun handleGetAllAttempts(call: ApplicationCall) {

    val attempts = transaction {
        Attempt.all().toList()
    }

    call.respondHtml {
        head {
            script(src = "/static/script/allAttempts.js") { }
            link(rel = "stylesheet", href = "/static/style/bootstrap.min.css") { }
        }
        body {
            div("container-fluid px-4 py-2") {
                h1 { +"All Attempts" }
                a("/newAttempt", classes = "btn btn-primary btn-small mb-3") { +"New attempt" }
                table("table table-sm table-hover table-striped") {
                    thead {
                        tr {
                            th(ThScope.col) { +"#" }
                            th(ThScope.col) { +"Date" }
                            th(ThScope.col) { +"Name" }
                            th(ThScope.col) { +"Status" }
                            th(ThScope.col) { +"Score" }
                        }
                    }
                    tbody {
                        for (attempt in attempts) {
                            tr {
                                id = "row${attempt.id}"
                                th(ThScope.row) { +"${attempt.id}" }
                                td { +attempt.date.toString("dd.MM.yyyy HH:mm") }
                                td {
                                    a("/attemptDetails/${attempt.id.value}") { +attempt.name }
                                }
                                td { +statusFormatter.format(attempt.status) }
                                td { +"${attempt.score}%" }
                            }
                        }
                    }
                }
            }
        }
    }
}

suspend fun handleGetAttemptDetails(call: ApplicationCall) {

    val id = call.parameters["id"]?.toIntOrNull()
    if (id == null) {
        call.respondText("Invalid ID", contentType = ContentType.Text.Plain)
        return
    }

    val attempt = transaction {
        Attempt.findById(id)
    }

    if (attempt == null) {
        call.respondText("ID not found", contentType = ContentType.Text.Plain)
        return
    }

    call.respondHtml {
        head {
            script(src = "/static/script/attemptDetails.js") { }
            link(rel = "stylesheet", href = "/static/style/bootstrap.min.css") { }
            link(rel = "stylesheet", href = "/static/style/tweaks.css") { }
        }
        body {
            div("container-fluid px-4 py-2") {
                h1 { +"Attempt Details" }
                a("/", classes = "btn btn-outline-secondary btn-small mb-3") { +"Back" }

                detailsSingleLine("Name", attempt.name)
                detailsSingleLine("Date", attempt.date.toString("dd.MM.yyyy HH:mm"))
                detailsSingleLine("Status", statusFormatter.format(attempt.status))
                detailsSingleLine("Score", "${attempt.score}%")

                if (attempt.compilerOutput.isNotEmpty())
                    detailsMultiLine("Compiler output", attempt.compilerOutput)

                if (attempt.testOutput.isNotEmpty())
                    detailsMultiLine("Test output", attempt.testOutput)

                detailsMultiLine("Source code", attempt.src)
            }
        }
    }
}

suspend fun handleGetNewAttempt(call: ApplicationCall) {
    call.respondHtml {
        head {
            script(src = "/static/script/newAttempt.js") { }
            link(rel = "stylesheet", href = "/static/style/bootstrap.min.css") { }
            link(rel = "stylesheet", href = "/static/style/tweaks.css") { }
        }
        body {
            div("container-fluid px-4 py-2") {
                h1 { +"New Attempt" }
                a("/", classes = "btn btn-outline-secondary btn-small mb-3") { +"Back" }
                form("/newAttempt", encType = FormEncType.multipartFormData, method = FormMethod.post) {
                    autoComplete = false
                    div("form-group mb-3") {
                        label { +"Name" }
                        textInput(name = "name", classes = "form-control") { }
                        div("invalid-feedback") { +"Please enter a submission name" }
                    }
                    div("form-group") {
                        label { +"Source code" }
                        fileInput(name = "file", classes = "d-none") { accept = ".kt" }
                        button(type = ButtonType.button, classes = "btn btn-sm btn-outline-primary ml-3") {
                            onClick = "openFileDialog()"
                            +"Load from file"
                        }
                        textArea(classes = "form-control mt-2 text-monospace code-size", rows = "10") { id = "src" }
                        div("invalid-feedback") { +"Please enter or upload a source code" }
                    }
                    button(type = ButtonType.submit, classes = "btn btn-primary") { +"Submit" }
                }
            }
        }
    }
}

@ExperimentalCoroutinesApi
suspend fun handlePostNewAttempt(call: ApplicationCall) {

    if (!call.request.isMultipart()) {
        call.respondText("Invalid request", contentType = ContentType.Text.Plain)
        return
    }

    val multipart = call.receiveMultipart()

    var name = ""
    var src = ""

    multipart.forEachPart {
        if (it is PartData.FormItem) {
            if (it.name == "name") name = it.value
            if (it.name == "src") src = it.value
        }
    }

    if (name.isEmpty() || src.isEmpty()) {
        call.respondText("Both 'name' and 'src' are required", contentType = ContentType.Text.Plain)
        return
    }

    val attempt = transaction {
        Attempt.new {
            date = DateTime.now()
            this.name = name
            this.src = src
            status = AttemptStatus.COMPILING
            score = 0
            compilerOutput = ""
            testOutput = ""
        }
    }

    call.respondRedirect("/")

    GlobalScope.launch {

        val (jarFile, compilationError) = compiler.compile(src)
        if (jarFile == null) {
            transaction {
                attempt.compilerOutput = compilationError.orEmpty()
                attempt.status = AttemptStatus.COMPILATION_FAILED
            }
            statusReporter.report(attempt)
            return@launch
        }

        transaction {
            attempt.status = AttemptStatus.RUNNING_TESTS
        }
        statusReporter.report(attempt)

        val (success, score, testOutput) = testRunner.runAllTests(jarFile)
        transaction {
            attempt.score = score
            attempt.testOutput = testOutput

            attempt.status =
                if (success) AttemptStatus.EVALUATED
                else AttemptStatus.TESTING_FAILED
        }
        statusReporter.report(attempt)
    }
}
