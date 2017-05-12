package io.datawire.loom.terraform

import io.datawire.loom.LoomTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestName
import java.nio.file.Files
import java.nio.file.Paths
import org.assertj.core.api.Assertions.*


class TerraformTest : LoomTest() {

  @Rule
  @JvmField
  val workspaces = TemporaryFolder()

  @Rule
  @JvmField
  val testName = TestName()

  @Test
  fun simpleProject_initUsingLocalBackend_initializesTerraform() {
    val workspace = TerraformWorkspace(workspaces.root.toPath())

    println(Files.exists(workspace.path))

    workspace.configureBackend(createLocalBackend())
    workspace.configureProvider(createAwsProvider("us-east-1"))
    workspace.configureTemplate(terraformTemplate(
        modules = listOf(
            Module("random_name", javaClass.getResource("/tf-random_name").toString(), emptyMap())
        )
    ))

    val terraform = Terraform(Paths.get("/home/plombardi/bin/terraform"), homeDirectory, workspace)
    terraform.init()

    (terraform.plan() as? Difference)?.let {
      terraform.apply(it)
      terraform.output()
    } ?: fail("Plan did not have any differences!")
  }
}