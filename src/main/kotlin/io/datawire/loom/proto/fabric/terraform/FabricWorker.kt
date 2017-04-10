package io.datawire.loom.proto.fabric.terraform

import io.datawire.loom.proto.fabric.FabricManager


class FabricWorker(private val broker: FabricManager) : Runnable {

    override fun run() {
        while(true) {
           broker.getTask().process()
        }
    }
}