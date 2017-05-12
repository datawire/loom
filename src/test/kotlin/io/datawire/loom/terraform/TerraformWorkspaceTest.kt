package io.datawire.loom.terraform

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.assertj.core.api.Assertions.*


class TerraformWorkspaceTest {

  @get:Rule
  val workspaces = TemporaryFolder()

  @Test
  fun configureWorkspace() {
    val ws = TerraformWorkspace(terraformWorkspace("test-configure-workspace"))

    val backend  = createS3Backend("us-east-1", "test", "test")
    val provider = createAwsProvider("us-east-1")
    val template = terraformTemplate(
        modules = listOf(
            Module("foo", "/nonexistent/foo"),
            Module("bar", "/nonexistent/bar")
        )
    )

    ws.configureBackend(backend)
    ws.configureProvider(provider)
    ws.configureTemplate(template)

    assertThat(ws.fetchBackend()).isEqualTo(backend)
    assertThat(ws.fetchProvider()).isEqualTo(provider)
    assertThat(ws.fetchTemplate()).isEqualTo(template)
  }

  private fun terraformWorkspace(name: String) = workspaces.newFolder(name).toPath()
}