package org.cameek.sshclient.controller

import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.TextArea
//import net.rgielen.fxweaver.core.FxmlView
import org.cameek.sshclient.service.SshClientService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
//@FxmlView("/chart.fxml")
class AppController(
    @Autowired val sshClientService: SshClientService
) {

    companion object {
        private val log = LoggerFactory.getLogger(AppController::class.java)
    }

    @FXML
    lateinit var appPane: Parent

    @FXML
    lateinit var textArea: TextArea

    @FXML
    lateinit var buttonSingle: Button

    @FXML
    lateinit var buttonMulti: Button

    @FXML
    lateinit var buttonOk: Button

    @FXML
    lateinit var buttonCancel: Button

    //@Autowired
    //lateinit var sshClientService: SshClientService

    @FXML
    fun initialize() {

//        appPane.isCache = false
//
//        textArea.isCache = false
//
//        textArea.childrenUnmodifiable.forEach { node -> node.isCache = false }

        buttonSingle.setOnAction { value ->
            run {
                log.info("Button 'Single' Clicked! ActionEvent: $value")
                textArea.appendText("Button 'Single' Clicked! ActionEvent: $value\n")

                val command = "ls -all\n"
                log.info("Calling SSH Client Service: $command")
                val result = sshClientService.remoteCommand("jarosm3", "lqrtpb_2",
                    "127.0.0.1", 2231, 10, command
                )
                log.info("Called  SSH Client Service: $command")

                textArea.appendText(result)
            }
        }

        buttonMulti.setOnAction { value ->
            run {
                log.info("Button 'Multi' Clicked! ActionEvent: $value")
                textArea.appendText("Button 'Multi' Clicked! ActionEvent: $value\n")

                val commands = listOf(
                    "ls -all /tmp\n",
                    "ls -all /root\n",
                    "ls -all /home/jarosm3\n"
                )

                log.info("Calling SSH Client Service: $commands")

                val results = sshClientService.remoteCommands(
                    host = "127.0.0.1", port = 2231,
                    username = "jarosm3", password = "lqrtpb_2",
                    defaultTimeoutSeconds = 2,
                    commands = commands
                    )

                log.info("Called  SSH Client Service: $commands")

                var i = 1
                for (result in results) {
                    textArea.appendText("\n")
                    textArea.appendText("\n")
                    textArea.appendText("$i.\n")
                    textArea.appendText(" command: ${result.input}")
                    textArea.appendText(" output: ${result.output}")
                    i++
                }

            }
        }

        buttonCancel.setOnAction { value -> println("Button Cancel Clicked! ActionEvent: $value") }
    }

}
