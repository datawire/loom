package io.datawire.loom.kops


data class EtcdMember(val name: String, val instanceGroup: String, val encryptedVolume: Boolean)
