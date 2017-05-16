package io.datawire.loom.fabric

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeType
import io.datawire.loom.core.aws.AwsCloud
import io.datawire.loom.core.validation.*


private val MODEL_NAME_REGEX = Regex("[a-z][a-z0-9_]{0,31}")

class FabricModelValidator(private val aws: AwsCloud) : Validator() {

  override fun validate(root: JsonNode) {
    val issues = mutableListOf<ValidationIssue>()

    root.matches(field("/name"), io.datawire.loom.fabric.MODEL_NAME_REGEX)?.let { issues += it }

    root.validate(
        field("/domain"),
        nullable = false,
        type     = JsonNodeType.STRING,
        check    = { aws.isOwnedRoute53Domain(textValue()) },
        failed   = issue("Unknown Domain", "Domain was not found in Amazon Route 53")
    )?.let { issues += it }

    root.validate(
        field("/sshPublicKey"),
        nullable = true,
        type     = JsonNodeType.STRING,
        check    = { true },
        failed   = issue("Value is Too Low", "Master node count is below allowed value: 0")
    )?.let { issues += it }

    root.validate(
        field("/region"),
        nullable = false,
        type     = JsonNodeType.STRING,
        check    = { !aws.isUsableRegion(textValue()) },
        failed   = issue("Invalid Cloud Region", "Cloud provider region is not valid or usable")
    )?.let { issues += it }

    root.validate(
        field("/masterNodes"),
        nullable = false,
        type     = JsonNodeType.OBJECT,
        check    = { true },
        failed   = issue("Invalid Node Type", "Master node type is not valid or usable")
    )?.let { issues += it }

    root.validate(
        field("/sshPublicKey"),
        nullable = true,
        type     = JsonNodeType.STRING,
        check    = { true },
        failed   = issue("Value is Too Low", "Master node count is below allowed value: 0")
    )?.let { issues += it }

    root.validate(
        field("/workerNodes"),
        nullable = false,
        type     = JsonNodeType.ARRAY,
        check    = { true },
        failed   = issue("Value is Too Low", "Master node count is below allowed value: 0")
    )?.let { issues += it }

    if (issues.isNotEmpty()) {
      throw ValidationException(issues)
    }
  }
}