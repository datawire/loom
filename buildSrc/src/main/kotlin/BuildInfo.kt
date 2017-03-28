import org.ajoberstar.grgit.Grgit

class BuildInfo {
    fun resolveVersion(): String {
        val version = try {
            val git = Grgit.open()
            val branch = git.branch.current.name?.toString() ?: throw NullPointerException("Git branch is null")
            return when(branch) {
                "master" -> "latest"
                else     -> branch.replace(Regex(".*/"), "")
            }
        } catch (any: Throwable) {
            "latest"
        }

        return version
    }
}