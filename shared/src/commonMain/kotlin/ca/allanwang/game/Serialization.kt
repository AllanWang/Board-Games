package ca.allanwang.game

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf

@OptIn(ExperimentalSerializationApi::class)
val ProtobufSerializer = ProtoBuf {
  encodeDefaults = true
}