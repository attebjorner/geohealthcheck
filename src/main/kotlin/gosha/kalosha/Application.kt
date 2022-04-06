package gosha.kalosha

import gosha.kalosha.config.*
import io.ktor.application.*

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    configureDI()
    configureRouting()
    configureSerialization()
    configureLogging()
    scheduleJobs()
//    Runtime.getRuntime().addShutdownHook(Thread {
//        println("SHUTTING DOWN")
//    })
//    for (sig in signals) {
//        Signal.handle(Signal(sig)) {
//            println("SIGNAL $sig")
//        }
//    }
//    launch {
//        delay(10000)
//        exitProcess(0)
//    }
}
