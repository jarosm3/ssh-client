package org.cameek.sshclient

import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import org.cameek.sshclient.stage.StageReadyEvent
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext

/**
 * Java FX Application
 *
 * [https://blog.jetbrains.com/idea/2019/11/tutorial-reactive-spring-boot-a-javafx-spring-boot-application/](https://blog.jetbrains.com/idea/2019/11/tutorial-reactive-spring-boot-a-javafx-spring-boot-application/)
 */
@SpringBootApplication
class JavaFxApp : Application() {

    companion object {
        private var log = LoggerFactory.getLogger(JavaFxApp::class.java)
    }

    private lateinit var applicationContext: ConfigurableApplicationContext

    @Throws(Exception::class)
    override fun init() {
        log.debug("init - Begin")

        super.init()
        applicationContext = SpringApplicationBuilder(JavaFxApp::class.java).run()

        log.debug("init - End")
    }

    @Throws(Exception::class)
    override fun start(primaryStage: Stage) {
        log.debug("start - Begin")

        applicationContext.publishEvent(StageReadyEvent(primaryStage))

        log.debug("start - End")
    }

    @Throws(Exception::class)
    override fun stop() {
        log.debug("stop - Begin")

        super.stop()
        applicationContext.close()
        Platform.exit()

        log.debug("stop - End")
    }
}

fun main(args: Array<String>) {
    Application.launch(JavaFxApp::class.java, *args)
}