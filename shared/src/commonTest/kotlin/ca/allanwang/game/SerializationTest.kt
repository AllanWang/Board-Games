import ca.allanwang.game.Game
import ca.allanwang.game.GameClient
import ca.allanwang.game.GameEmpty
import ca.allanwang.game.GameReducer
import ca.allanwang.game.GameStore
import ca.allanwang.game.ProtobufSerializer
import ca.allanwang.game.lobby.PlayerId
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalSerializationApi::class)
class SerializationTest {

   @Test
   fun test_serialization() {
     val serialized = ProtobufSerializer.encodeToByteArray(Game.serializer(), GameEmpty)
     val deserialized: GameEmpty = ProtobufSerializer.decodeFromByteArray(serialized)

     assertEquals(GameEmpty, deserialized)
   }

  @Test
  fun defaultGameClient_serialization() {
    val game = GameStore.initialState()
    val gameClient = GameReducer.playerState(state = game, playerId = PlayerId("test"))
    val serialized = ProtobufSerializer.encodeToByteArray(GameClient.serializer(), gameClient)
    val deserialized: GameClient = ProtobufSerializer.decodeFromByteArray(serialized)

    assertEquals(gameClient, deserialized)
  }
 }