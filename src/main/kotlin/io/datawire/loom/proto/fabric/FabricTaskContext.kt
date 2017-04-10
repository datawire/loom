package io.datawire.loom.proto.fabric

import io.datawire.loom.proto.core.ExternalTool
import io.datawire.loom.proto.model.Fabric
import io.datawire.loom.proto.model.FabricModel
import java.nio.file.Path


data class FabricTaskContext(
        val model     : FabricModel,
        val fabric    : Fabric,
        val manager   : FabricManager,
        val workspace : Path,
        val terraform : ExternalTool,
        val kops      : ExternalTool)