package ca.allanwang.game.coup

import ca.allanwang.game.coup.Card.*
import ca.allanwang.game.coup.Coup.CoupPlayerAction.*
import ca.allanwang.game.coup.CoupCardAction.*
import ca.allanwang.game.lobby.LobbyCode
import ca.allanwang.game.lobby.LobbyPlayer
import ca.allanwang.game.lobby.PlayerId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CoupActionTest {

    private fun createTestCoup(): Coup {
        val playerId1 = PlayerId("player1")
        val playerId2 = PlayerId("player2")
        
        val player1 = Player(
            lobbyPlayer = LobbyPlayer(id = playerId1, name = "Player 1"),
            gameInfo = GamePlayerInfo(
                coins = 10,
                cards = listOf(Duke, Assassin)
            )
        )
        
        val player2 = Player(
            lobbyPlayer = LobbyPlayer(id = playerId2, name = "Player 2"),
            gameInfo = GamePlayerInfo(
                coins = 5,
                cards = listOf(Contessa, Captain)
            )
        )
        
        return Coup(
            active = 0,
            code = LobbyCode("test"),
            players = listOf(player1, player2),
            deck = listOf(Ambassador, Duke, Captain, Contessa, Assassin),
            discard = emptyList(),
            playerAction = SelectingAction(options = CoupCardAction.entries.toList())
        )
    }
    
    @Test
    fun testIncomeMoveToNextPlayer() {
        val coup = createTestCoup()
        val player1 = coup.players[0]
        
        // Player 1 uses Income action
        val result = coup.act(Income, null)
        
        // Verify player got 1 coin
        assertEquals(player1.gameInfo.coins + 1, result.players[0].gameInfo.coins)
        
        // Verify turn moved to next player
        assertEquals(1, result.active)
        assertEquals(SelectingAction::class, result.playerAction::class)
    }
    
    @Test
    fun testAssassinateRequiresCost() {
        val coup = createTestCoup()
        val result = coup.copy(
            players = coup.players.map { 
                if (it.id == coup.players[0].id) it.updateGameInfo { copy(coins = 2) } else it
            }
        )
        
        // Player with only 2 coins attempts Assassinate (costs 3)
        val assassinateResult = result.act(Assassinate, coup.players[1].id)
        
        // Action should fail due to insufficient coins
        assertEquals(result, assassinateResult)
    }
    
    @Test
    fun testAssassinateDeductsCost() {
        val coup = createTestCoup()
        
        // Player with sufficient coins does Assassinate
        val assassinateResult = coup.act(Assassinate, coup.players[1].id)
        
        // Verify 3 coins were deducted
        assertEquals(coup.players[0].gameInfo.coins - 3, assassinateResult.players[0].gameInfo.coins)
        
        // Verify the action created a RequestingLoseCard state
        assertEquals(RequestingLoseCard::class, assassinateResult.playerAction::class)
    }
    
    @Test
    fun testCoupRequiresCost() {
        val coup = createTestCoup()
        val result = coup.copy(
            players = coup.players.map { 
                if (it.id == coup.players[0].id) it.updateGameInfo { copy(coins = 6) } else it
            }
        )
        
        // Player with only 6 coins attempts Coup (costs 7)
        val coupResult = result.act(CoupCardAction.Coup, coup.players[1].id)
        
        // Action should fail due to insufficient coins
        assertEquals(result, coupResult)
    }
    
    @Test
    fun testCoupDeductsCost() {
        val coup = createTestCoup()
        
        // Player with sufficient coins does Coup
        val coupResult = coup.act(CoupCardAction.Coup, coup.players[1].id)
        
        // Verify 7 coins were deducted
        assertEquals(coup.players[0].gameInfo.coins - 7, coupResult.players[0].gameInfo.coins)
        
        // Verify the action created a RequestingLoseCard state
        assertEquals(RequestingLoseCard::class, coupResult.playerAction::class)
    }
    
    @Test
    fun testStealActionTransfersCoins() {
        val coup = createTestCoup()
        
        // Player 1 steals from Player 2
        val stealResult = coup.act(Steal, coup.players[1].id)
        
        // Player 1 should get 2 coins and Player 2 should lose 2 coins
        assertEquals(coup.players[0].gameInfo.coins + 2, stealResult.players[0].gameInfo.coins)
        assertEquals(coup.players[1].gameInfo.coins - 2, stealResult.players[1].gameInfo.coins)
        
        // Turn should move to Player 2
        assertEquals(1, stealResult.active)
    }
    
    @Test
    fun testExchangeSetsUpCardSelection() {
        val coup = createTestCoup()
        
        // Player uses Exchange action
        val exchangeResult = coup.act(Exchange, null)
        
        // Should be in card selection state
        assertEquals(SelectingCard::class, exchangeResult.playerAction::class)
        
        // The deck should have 2 fewer cards
        assertEquals(coup.deck.size - 2, exchangeResult.deck.size)
        
        // Player should have their original cards plus 2 more from the deck to choose from
        val selectingCard = exchangeResult.playerAction as SelectingCard
        assertEquals(coup.players[0].gameInfo.cards.size + 2, selectingCard.cards.size)
        
        // Player should select the same number of cards they had originally
        assertEquals(coup.players[0].gameInfo.cards.size, selectingCard.count)
    }
}