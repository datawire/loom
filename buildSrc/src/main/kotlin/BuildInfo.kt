class BuildInfo {

    fun resolveVersion(): String {
        val branch  = env("TRAVIS_BRANCH")?.replace("/", "_")?.toLowerCase()
        var version = if (branch in setOf("master", null)) "latest" else branch

        if (env("TRAVIS_TAG") != null) { version = env("TRAVIS_TAG") }

        return version!!
    }

    private fun env(name: String, defaultValue: String? = null) = System.getenv(name) ?: defaultValue
}