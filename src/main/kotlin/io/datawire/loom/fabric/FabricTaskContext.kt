package io.datawire.loom.fabric

import io.datawire.loom.core.ExternalTool
import io.datawire.loom.model.Fabric
import io.datawire.loom.model.FabricModel
import java.nio.file.Path


data class FabricTaskContext(
        val model     : FabricModel,
        val fabric    : Fabric,
        val manager   : FabricManager,
        val workspace : Path,
        val terraform : ExternalTool,
        val kops      : ExternalTool)