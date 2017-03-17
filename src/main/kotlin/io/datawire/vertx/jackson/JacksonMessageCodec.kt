/*
 * Copyright 2017 Datawire. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.datawire.vertx.jackson

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import io.vertx.core.json.Json
import kotlin.reflect.KClass


class JacksonMessageCodec<T: Any>(private val clazz: KClass<T>) : MessageCodec<T, T> {

    init {
        clazz.qualifiedName ?: throw IllegalStateException("class name is null")
    }

    override fun systemCodecID(): Byte = -1
    override fun name(): String = clazz.qualifiedName!!
    override fun transform(message: T?) = message

    override fun encodeToWire(buffer: Buffer?, message: T?) {
        val json = Json.mapper.writeValueAsString(message)
        buffer?.appendInt(json.toByteArray(Charsets.UTF_8).size)
        buffer?.appendString(json)
    }

    override fun decodeFromWire(bufferPos: Int, buffer: Buffer): T {
        val messageLength = buffer.getInt(bufferPos)
        val messageStartPos = bufferPos + 4 // Jump 4 because getInt() == 4 bytes
        val rawJson = buffer.getString(messageStartPos, messageStartPos + messageLength)
        return Json.mapper.readValue(rawJson, clazz.java)
    }
}