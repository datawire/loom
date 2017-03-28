import org.ajoberstar.grgit.Grgit

class BuildInfo {

    fun resolveVersion(): String {
        return when (System.getenv("TRAVIS")) {
            "true" -> handleTravisCi()
            else   -> handleLocal()
        }
    }

    fun handleTravisCi(): String {
        val branch = env("TRAVIS_BRANCH") ?: throw IllegalStateException("TRAVIS_BRANCH environment variable not set.")
        val tag    = env("TRAVIS_TAG")
        return tag ?: branch.replace(Regex(".*/"), "")
    }

    fun handleLocal(): String {
        return try {
            val git = Grgit.open()
            val branch = git.branch.current.name
            return when(branch) {
                "master" -> "latest"
                else     -> branch.replace(Regex(".*/"), "")
            }
        } catch (any: Throwable) {
            "latest"
        }
    }

    /**
     * Get an environment variable. Empty variables will be returned as null.
     */
    private fun env(name: String): String? = System.getenv(name)?.let { if (it != "") it else null }
}