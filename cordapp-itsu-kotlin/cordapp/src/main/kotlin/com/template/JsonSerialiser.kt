package com.template

import com.google.gson.*
import net.corda.core.identity.Party
import java.lang.reflect.Type
import com.google.gson.GsonBuilder




class PartySerializer : JsonSerializer<Party> {
    override fun serialize(party: Party, type: Type, context: JsonSerializationContext): JsonElement {
        val result = JsonObject()
        result.add("name", JsonPrimitive(party.toString()))
        return result
    }
}
