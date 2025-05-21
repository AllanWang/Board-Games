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
    
    @Test
    fun testIncomeMoveToNextPlayer() {
        // Setup players
        val playerId1 = PlayerId("player1")
        val playerId2 = PlayerId("player2")
        
        val player1 = Player(
            lobbyPlayer = LobbyPlayer(id = playerId1, name = "Player 1"),
            gameInfo = GamePlayerInfo(
                coins = 2,
                cards = listOf(Duke, Assassin)
            )
        )
        
        val player2 = Player(
            lobbyPlayer = LobbyPlayer(id = playerId2, name = "Player 2"),
            gameInfo = GamePlayerInfo(
                coins = 3,
                cards = listOf(Contessa, Captain)
            )
        )
        
        val coup = Coup(
            active = 0,
            code = LobbyCode("test"),
            players = listOf(player1, player2),
            deck = listOf(Ambassador, Duke, Captain),
            discard = emptyList(),
            playerAction = SelectingAction(options = CoupCardAction.entries.toList())
        )
        
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
        // Setup players with insufficient coins for assassination
        val playerId1 = PlayerId("player1")
        val playerId2 = PlayerId("player2")
        
        val player1 = Player(
            lobbyPlayer = LobbyPlayer(id = playerId1, name = "Player 1"),
            gameInfo = GamePlayerInfo(
                coins = 2, // Not enough coins for Assassinate (costs 3)
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
        
        val coup = Coup(
            active = 0,
            code = LobbyCode("test"),
            players = listOf(player1, player2),
            deck = listOf(Ambassador, Duke, Captain),
            discard = emptyList(),
            playerAction = SelectingAction(options = CoupCardAction.entries.toList())
        )
        
        // Player with only 2 coins attempts Assassinate (costs 3)
        val assassinateResult = coup.act(Assassinate, player2.id)
        
        // Action should fail due to insufficient coins
        assertEquals(coup, assassinateResult)
    }
    
    @Test
    fun testAssassinateDeductsCost() {
        // Setup players with sufficient coins for assassination
        val playerId1 = PlayerId("player1")
        val playerId2 = PlayerId("player2")
        
        val player1 = Player(
            lobbyPlayer = LobbyPlayer(id = playerId1, name = "Player 1"),
            gameInfo = GamePlayerInfo(
                coins = 5, // Enough coins for Assassinate (costs 3)
                cards = listOf(Duke, Assassin)
            )
        )
        
        val player2 = Player(
            lobbyPlayer = LobbyPlayer(id = playerId2, name = "Player 2"),
            gameInfo = GamePlayerInfo(
                coins = 4,
                cards = listOf(Contessa, Captain)
            )
        )
        
        val coup = Coup(
            active = 0,
            code = LobbyCode("test"),
            players = listOf(player1, player2),
            deck = listOf(Ambassador, Duke, Captain),
            discard = emptyList(),
            playerAction = SelectingAction(options = CoupCardAction.entries.toList())
        )
        
        // Player with sufficient coins does Assassinate
        val assassinateResult = coup.act(Assassinate, player2.id)
        
        // Verify 3 coins were deducted
        assertEquals(player1.gameInfo.coins - 3, assassinateResult.players[0].gameInfo.coins)
        
        // Verify the action created a RequestingLoseCard state
        assertEquals(RequestingLoseCard::class, assassinateResult.playerAction::class)
    }
    
    @Test
    fun testCoupRequiresCost() {
        // Setup players with insufficient coins for coup
        val playerId1 = PlayerId("player1")
        val playerId2 = PlayerId("player2")
        
        val player1 = Player(
            lobbyPlayer = LobbyPlayer(id = playerId1, name = "Player 1"),
            gameInfo = GamePlayerInfo(
                coins = 6, // Not enough coins for Coup (costs 7)
                cards = listOf(Duke, Assassin)
            )
        )
        
        val player2 = Player(
            lobbyPlayer = LobbyPlayer(id = playerId2, name = "Player 2"),
            gameInfo = GamePlayerInfo(
                coins = 4,
                cards = listOf(Contessa, Captain)
            )
        )
        
        val coup = Coup(
            active = 0,
            code = LobbyCode("test"),
            players = listOf(player1, player2),
            deck = listOf(Ambassador, Duke, Captain),
            discard = emptyList(),
            playerAction = SelectingAction(options = CoupCardAction.entries.toList())
        )
        
        // Player with only 6 coins attempts Coup (costs 7)
        val coupResult = coup.act(CoupCardAction.Coup, player2.id)
        
        // Action should fail due to insufficient coins
        assertEquals(coup, coupResult)
    }
    
    @Test
    fun testCoupDeductsCost() {
        // Setup players with sufficient coins for coup
        val playerId1 = PlayerId("player1")
        val playerId2 = PlayerId("player2")
        
        val player1 = Player(
            lobbyPlayer = LobbyPlayer(id = playerId1, name = "Player 1"),
            gameInfo = GamePlayerInfo(
                coins = 10, // Enough coins for Coup (costs 7)
                cards = listOf(Duke, Assassin)
            )
        )
        
        val player2 = Player(
            lobbyPlayer = LobbyPlayer(id = playerId2, name = "Player 2"),
            gameInfo = GamePlayerInfo(
                coins = 4,
                cards = listOf(Contessa, Captain)
            )
        )
        
        val coup = Coup(
            active = 0,
            code = LobbyCode("test"),
            players = listOf(player1, player2),
            deck = listOf(Ambassador, Duke, Captain),
            discard = emptyList(),
            playerAction = SelectingAction(options = CoupCardAction.entries.toList())
        )
        
        // Player with sufficient coins does Coup
        val coupResult = coup.act(CoupCardAction.Coup, player2.id)
        
        // Verify 7 coins were deducted
        assertEquals(player1.gameInfo.coins - 7, coupResult.players[0].gameInfo.coins)
        
        // Verify the action created a RequestingLoseCard state
        assertEquals(RequestingLoseCard::class, coupResult.playerAction::class)
    }
    
    @Test
    fun testStealActionTransfersCoins() {
        // Setup players for testing steal action
        val playerId1 = PlayerId("player1")
        val playerId2 = PlayerId("player2")
        
        val player1 = Player(
            lobbyPlayer = LobbyPlayer(id = playerId1, name = "Player 1"),
            gameInfo = GamePlayerInfo(
                coins = 3,
                cards = listOf(Duke, Captain) // Captain needed for steal
            )
        )
        
        val player2 = Player(
            lobbyPlayer = LobbyPlayer(id = playerId2, name = "Player 2"),
            gameInfo = GamePlayerInfo(
                coins = 5, // Has coins to steal
                cards = listOf(Contessa, Assassin)
            )
        )
        
        val coup = Coup(
            active = 0,
            code = LobbyCode("test"),
            players = listOf(player1, player2),
            deck = listOf(Ambassador, Duke, Captain),
            discard = emptyList(),
            playerAction = SelectingAction(options = CoupCardAction.entries.toList())
        )
        
        // Player 1 steals from Player 2
        val stealResult = coup.act(Steal, player2.id)
        
        // Player 1 should get 2 coins and Player 2 should lose 2 coins
        assertEquals(player1.gameInfo.coins + 2, stealResult.players[0].gameInfo.coins)
        assertEquals(player2.gameInfo.coins - 2, stealResult.players[1].gameInfo.coins)
        
        // Turn should move to Player 2
        assertEquals(1, stealResult.active)
    }
    
    @Test
    fun testExchangeSetsUpCardSelection() {
        // Setup players for testing exchange action
        val playerId1 = PlayerId("player1")
        val playerId2 = PlayerId("player2")
        
        val player1 = Player(
            lobbyPlayer = LobbyPlayer(id = playerId1, name = "Player 1"),
            gameInfo = GamePlayerInfo(
                coins = 4,
                cards = listOf(Duke, Ambassador) // Ambassador needed for exchange
            )
        )
        
        val player2 = Player(
            lobbyPlayer = LobbyPlayer(id = playerId2, name = "Player 2"),
            gameInfo = GamePlayerInfo(
                coins = 3,
                cards = listOf(Contessa, Captain)
            )
        )
        
        val deck = listOf(Assassin, Contessa, Duke, Captain, Ambassador)
        
        val coup = Coup(
            active = 0,
            code = LobbyCode("test"),
            players = listOf(player1, player2),
            deck = deck,
            discard = emptyList(),
            playerAction = SelectingAction(options = CoupCardAction.entries.toList())
        )
        
        // Player uses Exchange action
        val exchangeResult = coup.act(Exchange, null)
        
        // Should be in card selection state
        assertEquals(SelectingCard::class, exchangeResult.playerAction::class)
        
        // The deck should have 2 fewer cards
        assertEquals(coup.deck.size - 2, exchangeResult.deck.size)
        
        // Player should have their original cards plus 2 more from the deck to choose from
        val selectingCard = exchangeResult.playerAction as SelectingCard
        assertEquals(player1.gameInfo.cards.size + 2, selectingCard.cards.size)
        
        // Player should select the same number of cards they had originally
        assertEquals(player1.gameInfo.cards.size, selectingCard.count)
    }
}