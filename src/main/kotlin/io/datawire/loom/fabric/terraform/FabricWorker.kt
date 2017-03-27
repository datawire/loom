package io.datawire.loom.fabric.terraform

import io.datawire.loom.fabric.FabricManager


class FabricWorker(private val broker: FabricManager) : Runnable {

    override fun run() {
        while(true) {
           broker.getTask().process()
        }
    }
}