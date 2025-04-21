package ca.allanwang.game

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.protobuf.ProtoBuf

@OptIn(ExperimentalSerializationApi::class)
val ProtobufSerializer = ProtoBuf {
  encodeDefaults = true
  serializersModule = SerializersModule {
    polymorphic(Game::class) {
      defaultDeserializer { GameEmpty.serializer() }
    }
    polymorphic(GameClient::class) {
      defaultDeserializer { GameClientEmpty.serializer() }
    }
  }
}