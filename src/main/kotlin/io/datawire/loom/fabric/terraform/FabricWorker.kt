package io.datawire.loom.fabric.terraform

import io.datawire.loom.fabric.FabricManager
import io.datawire.loom.fabric.FabricTask


class FabricWorker(private val broker: FabricManager) : Runnable {

    override fun run() {
        while(true) {
            handleTask(broker.getTask())
        }
    }

    private fun handleTask(task: FabricTask) {
        task.process()
    }
}