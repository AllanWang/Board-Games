package ca.allanwang.game

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.protobuf.ProtoBuf

@OptIn(ExperimentalSerializationApi::class)
val ProtobufSerializer = ProtoBuf {
  encodeDefaults = true
  serializersModule = SerializersModule {
    polymorphic(
      Game::class,
      GameEmpty::class,
      GameEmpty.serializer()
    )
    polymorphic(
      Game::class,
      GameLobby::class,
      GameLobby.serializer()
    )
    polymorphic(
      Game::class,
      GameCoup::class,
      GameCoup.serializer()
    )
    polymorphic(
      GameClient::class,
      GameClientEmpty::class,
      GameClientEmpty.serializer()
    )
    polymorphic(
      GameClient::class,
      GameClientLobby::class,
      GameClientLobby.serializer()
    )
    polymorphic(
      GameClient::class,
      GameClientCoup::class,
      GameClientCoup.serializer()
    )
    polymorphic(
      GameAction::class,
      GameActionLobby::class,
      GameActionLobby.serializer()
    )
    polymorphic(
      GameAction::class,
      GameActionCoup::class,
      GameActionCoup.serializer()
    )
  }
}