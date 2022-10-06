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

        buttonOk.setOnAction { value ->
            run {
                log.info("Button OK Clicked! ActionEvent: $value")
                textArea.appendText("Button OK Clicked! ActionEvent: $value\n")

                val command = "ls -all\n"
                log.info("Calling SSH Client Service: $command")
                val result = sshClientService.remoteCommand("jarosm3", "lqrtpb_2",
                    "127.0.0.1", 2231, 10, command
                )
                log.info("Called  SSH Client Service: $command")

                textArea.appendText(result)
            }
        }

        buttonCancel.setOnAction { value -> println("Button Cancel Clicked! ActionEvent: $value") }
    }

}
