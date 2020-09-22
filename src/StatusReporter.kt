package com.task.appChecker

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ClosedSendChannelException
import java.util.ArrayList

class StatusReporter {

    private val activeSessions =
        ArrayList<WebSocketSession>()

    fun addSession(session: WebSocketSession) {
        synchronized(activeSessions) {
            activeSessions.add(session)
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun report(attempt: Attempt) {

        val sessions = synchronized(activeSessions) {
            activeSessions.filter { !it.outgoing.isClosedForSend }
            return@synchronized activeSessions.toList()
        }

        for (session in sessions) {
            try {
                val json =
                    "{ \"id\":${attempt.id}, \"status\":\"${statusFormatter.format(attempt.status)}\", \"score\":\"${attempt.score}%\" }"
                session.send(Frame.Text(json))
            } catch (e: ClosedSendChannelException) {
                // expected
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}