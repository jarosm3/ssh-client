package org.cameek.sshclient.stage

import java.io.IOException
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

@Component
class StageInitializer(
    @Value("Demo title") val applicationTitle: String,
    @Value("classpath:/app.fxml") val appResource: Resource,
    val applicationContext: ApplicationContext
) : ApplicationListener<StageReadyEvent> {

    companion object {
        private val log = LoggerFactory.getLogger(StageInitializer::class.java)
    }

    init {
        log.debug("StageInitializer - Begin")

        log.debug("StageInitializer - End")
    }

    override fun onApplicationEvent(event: StageReadyEvent) {
        log.debug("onApplicationEvent - Begin")

        try {
            val fxmlLoader = FXMLLoader(appResource.url)

            // Configure Controller Factory
            // This helps to enable Spring @autowire capabilities when Controller instance is created by Spring,
            // (otherwise it should be created from FXML as default and Dependency Injection would not work)
            fxmlLoader.setControllerFactory {
                    requestedControllerClass -> applicationContext.getBean(requestedControllerClass)
            }

            val parent = fxmlLoader.load<Parent>()
            val stage = event.stage
            stage.scene = Scene(parent, 800.0, 600.0)
            stage.title = applicationTitle
            stage.show()
        } catch (e: IOException) {
            val msg = "Cannot handle in StageInitializer class onApplicationEvent" + e.message
            log.error(msg, e)
            throw IllegalStateException(msg, e)
        }

        log.debug("onApplicationEvent - End")
    }
}