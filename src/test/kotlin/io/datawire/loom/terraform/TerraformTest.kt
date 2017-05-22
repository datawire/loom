package io.datawire.loom.terraform

import io.datawire.loom.LoomTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestName
import org.assertj.core.api.Assertions.*


class TerraformTest : LoomTest() {

  @Rule
  @JvmField
  val workspaces = TemporaryFolder()

  @Rule
  @JvmField
  val testName = TestName()

//  @Test
//  fun simpleProject_initUsingLocalBackend_initializesTerraform() {
//    val workspace = TerraformWorkspace(workspaces.root.toPath())
//
//    workspace.configureBackend(createLocalBackend())
//    workspace.configureProvider(createAwsProvider("us-east-1"))
//    workspace.configureTemplate(terraformTemplate(
//        modules = listOf(
//            Module("random_name", javaClass.getResource("/fixtures/terraform-pet_name_module").toString(), emptyMap())
//        ),
//        outputs = listOf(OutputReference("random_name", "\${module.random_name.pet_name}"))
//    ))
//
//    val terraform = Terraform.newTerraform(homeDirectory, workspace)
//    terraform.init()
//
//    (terraform.plan() as? Difference)?.let {
//      terraform.apply(it)
//      val outputs = terraform.output()
//      assertThat(outputs.size).isEqualTo(1)
//      assertThat(outputs.hasOutput("random_name")).isTrue()
//      assertThat(outputs.getOutput("random_name")).isNotNull()
//    } ?: fail("Plan did not have any differences!")
//  }
//
//  @Test
//  fun newTerraform_returnsUsableTerraformInstance() {
//    val terraform = Terraform.newTerraform(homeDirectory, TerraformWorkspace(workspaces.root.toPath()))
//    assertThat(terraform.version()).matches("\\d+.\\d+.\\d+")
//  }
}