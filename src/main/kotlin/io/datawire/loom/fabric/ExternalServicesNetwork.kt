package io.datawire.loom.fabric

/**
 * Operations module for defining how a Fabrics external service network is structured.
 *
 * @property name of the model.
 * @property module the source of the module.
 */
data class ExternalServicesNetworkModel(
    val name   : String,
    val module : String
)

/**
 * Fabric configuration.
 *
 * @property cidr the CIDR block address for the external services network
 */
data class ExternalServicesNetworkConfig(
    val cidr: String
)

/**
 * Merged configuration information about an External Services network setup.
 *
 * @property model the model behind this specification.
 * @property module the source to actualize the specification.
 * @property cidr the CIDR block address for the external services network
 */
data class ExternalServicesNetworkSpec(
    val model: String,
    val module: String,
    val cidr: String
)

fun assemble(model: ExternalServicesNetworkModel, config: ExternalServicesNetworkConfig): ExternalServicesNetworkSpec =
    ExternalServicesNetworkSpec(
        module = model.module,
        model  = model.name,
        cidr   = config.cidr
    )
