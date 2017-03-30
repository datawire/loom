package io.datawire.loom.internal

import spark.Request
import spark.Response
import spark.Spark


inline fun <reified T: Exception> exception(noinline handler: (ex: Exception, req: Request, res: Response) -> Unit) =
        Spark.exception(T::class.java, handler)