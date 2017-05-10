package io.datawire.loom.dev.auth

import spark.Filter
import spark.Request
import spark.Response
import spark.Spark.*
import java.util.*


class SingleUserAuthenticator(
    private val username: String,
    private val password: String
) : Filter {

  override fun handle(request: Request, response: Response) {
    val session = request.session()
    request.headers("Authorization")?.let {
      val decoded = String(Base64.getDecoder().decode(it.substringAfter("Basic").trim()), Charsets.UTF_8)
      val (username, password) = decoded.split(":")

      if (username == this.username && password == this.password) {
        session.attribute("user", User(username))
      } else {
        halt(401, "Not Authorized")
      }
    } ?: halt(401, "Not Authorized")
  }
}