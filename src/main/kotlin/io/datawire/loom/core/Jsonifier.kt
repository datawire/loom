package io.datawire.loom.core

import spark.ResponseTransformer


/**
 * Transform a non-null object into JSON.
 *
 * @property json the json serializer.
 */
class Jsonifier(private val json: Json = Json()) : ResponseTransformer {

  /**
   * Render the passed object as JSON. If the passed model is a null reference then no serialization is performed
   * because a null value becomes an HTTP 404 (NOT FOUND) in Spark framework which is the desired behavior for null
   * values.
   *
   * @param model the object to serialize as JSON.
   * @return the model serialized as JSON or null if the incoming model was null.
   */
  override fun render(model: Any?): String? = model?.let { json.write(it) }
}