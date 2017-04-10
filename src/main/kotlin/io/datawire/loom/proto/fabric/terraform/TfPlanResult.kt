package io.datawire.loom.proto.fabric.terraform

import java.nio.file.Path


sealed class TfPlanResult(
        val code: Int,
        val destroy: Boolean
)

class NoDifferences(val plan: Path, destroy: Boolean) : TfPlanResult(0, destroy)
class PlanningError(destroy: Boolean) : TfPlanResult(1, destroy)
class Differences(val plan: Path, destroy: Boolean) : TfPlanResult(2, destroy)
