package org.cameek.sshclient.stage

import javafx.stage.Stage
import org.springframework.context.ApplicationEvent

class StageReadyEvent(val stage: Stage) : ApplicationEvent(stage) {

}