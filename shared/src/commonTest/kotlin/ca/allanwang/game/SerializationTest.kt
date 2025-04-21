import ca.allanwang.game.Game
import ca.allanwang.game.GameEmpty
import ca.allanwang.game.ProtobufSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
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
 }