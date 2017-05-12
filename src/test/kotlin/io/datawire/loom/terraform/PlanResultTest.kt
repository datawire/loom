package io.datawire.loom.terraform

import org.junit.Test
import java.nio.file.Paths

import org.assertj.core.api.Assertions.*

class PlanResultTest {

  @Test
  fun fromExitCode_withExitCode0_returnsNoDifferences() {
    val res = fromExitCode(0, Paths.get("nonexistent-plan.out"))
    assertThat(res).isInstanceOf(NoDifference::class.java)
  }

  @Test
  fun fromExitCode_withExitCode1_returnsError() {
    val res = fromExitCode(1, Paths.get("nonexistent-plan.out"))
    assertThat(res).isInstanceOf(PlanError::class.java)
  }

  @Test
  fun fromExitCode_withExitCode2_returnsNoDifferences() {
    val res = fromExitCode(2, Paths.get("nonexistent-plan.out"))
    assertThat(res).isInstanceOf(Difference::class.java)
  }

  @Test
  fun fromExitCode_withInvalidExitCode_throwsIllegalArgumentException() {
    try {
      fromExitCode(33, Paths.get("nonexistent-plan.out"))
      failBecauseExceptionWasNotThrown(IllegalArgumentException::class.java)
    } catch (ex: IllegalArgumentException) {
      assertThat(ex).hasMessage("Invalid exit code (33) for `terraform plan -detailed-exitcode ...`")
    }
  }
}