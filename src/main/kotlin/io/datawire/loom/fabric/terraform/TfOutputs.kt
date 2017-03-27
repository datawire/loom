package io.datawire.loom.fabric.terraform


data class TfOutputs(private val map: Map<String, TfOutputValue>) {

    fun getOutputAsString(name: String): String {
        return map[name]?.asString()!!
    }

    fun getOutputAsList(name: String): List<String> {
        return ((map[name]?.asList()) ?: emptyList<String>()).filterNotNull().map(Any::toString)
    }
}