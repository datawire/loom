package io.datawire.loom.proto.fabric.terraform


data class TfOutputValue(
        val sensitive: Boolean,
        val type: String,
        val value: Any) {

    fun asString() = value as? String
    fun asList()   = value as? List<*>
}