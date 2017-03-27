package io.datawire.loom.fabric

import io.datawire.loom.data.AwsS3Dao
import io.datawire.loom.model.Fabric
import java.util.concurrent.BlockingQueue


class FabricTaskManager(
        private val tasks: BlockingQueue<FabricTask>,
        private val fabrics: AwsS3Dao<Fabric>) {

    fun putTask(task: FabricTask) = tasks.put(task)
    fun getTask(): FabricTask     = tasks.take()

    fun updateFabric(fabric: Fabric) {
        fabrics.put(fabric.name, fabric)
    }
}