package com.task.appChecker

import kotlinx.html.*

fun DIV.detailsSingleLine(caption: String, value: String) {
    div("input-group mb-3") {
        div("input-group-prepend") {
            span("input-group-text input-group-fixed") { +caption }
        }
        textInput(classes = "form-control") {
            this.value = value
        }
    }
}

fun DIV.detailsMultiLine(caption: String, value: String) {
    div("input-group mb-3") {
        div("input-group-prepend") {
            span("input-group-text input-group-fixed") { +caption }
        }
        textArea(classes = "form-control text-monospace code-size") {
            +value
        }
    }
}