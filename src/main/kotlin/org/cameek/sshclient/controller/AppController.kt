package org.cameek.sshclient.controller

import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import org.cameek.sshclient.bean.CmdStrIOE
//import net.rgielen.fxweaver.core.FxmlView
import org.cameek.sshclient.service.SshClientService
import org.cameek.sshclient.stream.SshClientShell
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
    lateinit var textAreaIn: TextArea

    @FXML
    lateinit var textAreaOut: TextArea

    @FXML
    lateinit var textAreaErr: TextArea

    @FXML
    lateinit var buttonSingle: Button

    @FXML
    lateinit var buttonMulti: Button

    @FXML
    lateinit var buttonMulti2: Button

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
                    "ls -all /tmp\nexit\n",
                    "ls -all /root\nexit\n",
                    "ls -all /home/resu\nexit\n",
                    "echo \"lqrtpb_2\" | sudo -S -k su - && sudo su\nls -all\nexit\nexit",
                    "sudo su -\nlqrtpb_2\n\n\n\nls -all /root\nexit\nexit\n"
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

        buttonMulti2.setOnAction { value ->
            run {
                log.info("Button 'Multi2' Clicked! ActionEvent: $value")
                textArea.appendText("Button 'Multi2' Clicked! ActionEvent: $value\n")

                val command = "ls -all\n"
                log.info("Calling SSH Client Service: $command")



                SshClientShell(
                    host = "127.0.0.1", port = 2231,
                    username = "jarosm3", password = "lqrtpb_2",
                    command = CmdStrIOE("ls -all")
                ).use {
                    sshClientShell ->
                        log.info("sdddddddddddddd")
                        val resultXXX = sshClientShell.processFlow()
                        log.info("processFlow returns: $resultXXX")

                       // log.info("Executing from AppController sshClientShell with command=${sshClientShell.command}")

                }


//                val result = sshClientService.remoteCommand("jarosm3", "lqrtpb_2",
//                    "127.0.0.1", 2231, 10, command
//                )
                log.info("Called  SSH Client Service: $command")

               // textArea.appendText(result)
            }
        }

        buttonCancel.setOnAction { value -> println("Button Cancel Clicked! ActionEvent: $value") }
    }

}
