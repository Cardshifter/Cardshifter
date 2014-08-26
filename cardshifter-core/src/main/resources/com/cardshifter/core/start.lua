-- Always name this function "startGame"
function startGame(game)
    print("test: " .. game:toString())
    game:on('actionUsed', onActionUsed)
    game:on('turnStart', onTurnStart)

	local numPlayers = game:getPlayers():size()
	for i=0,numPlayers-1 do
		local player = game:getPlayer(i)
		print("Player: " .. player:toString())
		
		player:addAction("End Turn", allowNextTurn, endTurn)
		
	    player.data.life = 10
	    player.data.mana = 0
	    player.data.manaMax = 0
	    player.data.scrap = 0
	    local field = game:createZone(player, "Battlefield")
	    field:setGloballyKnown(true)
	    player.data.battlefield = field
		
	    local deck = game:createZone(player, "Deck")
	    player.data.deck = deck
		
	    local hand = game:createZone(player, "Hand")
	    hand:setKnown(player, true)
	    player.data.hand = hand
		
		for i=1,4 do
			local card
			for strength = 1,5 do
				card = createCreature(deck, strength, strength, strength, 'B0T')
				if strength == 2 then
					card.data.strength = card.data.strength + 1
				end
			end
			card = createCreature(deck, 5, 4, 4, 'Bio')
			card = createCreature(deck, 5, 5, 3, 'Bio')
			card = createCreature(deck, 5, 3, 5, 'Bio')
		
			card = createEnchantment(deck, 1, 0, 1)
			card = createEnchantment(deck, 0, 1, 1)
			card = createEnchantment(deck, 3, 0, 3)
			card = createEnchantment(deck, 0, 3, 3)
			card = createSpecialEnchantment(deck, 2, 2, 5)
	    end
	    
	    deck:shuffle()
	    
	    for i=1,4 do
		    drawCard(player)
	    end
	end
	
	-- Turn Start event is not called when starting game (player should not draw card), setup initial mana for first player
	firstPlayer = game:getFirstPlayer()
	firstPlayer.data.mana = 1
	firstPlayer.data.manaMax = 1
end

function createSpecialEnchantment(deck, strength, health, cost)
    -- A special enchantment can only target a creature that has been enchanted already
	local card = deck:createCardOnBottom()
	card:addAction("Enchant", enchAllowed, enchCard)
	card.data.manaCost = cost
	card.data.enchStrength = strength
	card.data.enchHealth = health
	card.data.cardType = 'Enchantment'
	return card
end

function createEnchantment(deck, strength, health, cost)
    -- Can only target creatureType == 'Bio'
	local card = deck:createCardOnBottom()
	card:addAction("Enchant", enchAllowed, enchCard)
	card.data.manaCost = 0
	card.data.scrapCost = cost
	card.data.enchStrength = strength
	card.data.enchHealth = health
	card.data.cardType = 'Enchantment'
	return card
end

function createCreature(deck, cost, strength, health, creatureType)
	local card = deck:createCardOnBottom()
	card:addAction("Play", playAllowed, playCard)
	card:addAction("Attack", attackAllowed, attackCard)
	card:addAction("Scrap", scrapAllowed, scrapCard)
	card.data.manaCost = cost
	card.data.strength = strength
	card.data.health = health
	card.data.enchantments = {}
	card.data.creatureType = creatureType
	card.data.cardType = 'Creature'
	card.data.sickness = 1
	card.data.attacksAvailable = 1
	return card
end

function onActionUsed(card, action)
	print("(This is Lua) Action Used! " .. card:toString() .. " with action " .. action:toString())
end

function onTurnStart(player, event)
	print("(This is Lua) Turn Start! " .. player:toString())
	if player.data.deck:isEmpty() then
		print("(This is Lua) Deck is empty!")
	end
	drawCard(player)
	player.data.manaMax = player.data.manaMax + 1
	player.data.mana = player.data.manaMax
end

function drawCard(player)
	local card = player.data.deck:getTopCard()
	card:moveToBottomOf(player.data.hand)
end

function playAllowed(card)
	local currPlayer = card:getGame():getCurrentPlayer()
	if card:getOwner() ~= currPlayer then
		return false
	end
	if card:getZone() ~= currPlayer.data.hand then
		return false
	end
	if card.data.manaCost > currPlayer.data.mana then
		return false
	end
	return true
end

function playCard(card)
    local owner = card:getOwner()
	card:moveToBottomOf(owner.data.battlefield)
end

function attackAllowed(card)
	local currPlayer = card:getGame():getCurrentPlayer()
	if card:getOwner() ~= currPlayer then
		return false
	end
	if card:getZone() ~= currPlayer.data.battlefield then
		return false
	end
end

function attackCard(card)
	return false
end

function enchAllowed(card)
	if not playAllowed(card) then
		return false
	end
	local currPlayer = card:getGame():getCurrentPlayer()
	if card.data.scrapCost < currPlayer.data.scrap then
		return false
	end
	return false
end

function enchCard(card)
	return false
end

function scrapAllowed(card)
	local currPlayer = card:getGame():getCurrentPlayer()
	if card:getOwner() ~= currPlayer then
		return false
	end
	if card:getZone() ~= currPlayer.data.battlefield then
		return false
	end
	return true
end

function scrapCard(card)
	local owner = card:getOwner()
	card:destroy()
	owner.data.scrap = owner.data.scrap + 1
end

function endTurn(player)
	local game = player:getGame()
	game:nextTurn()
end

function allowNextTurn(player)
	return player:getGame():getCurrentPlayer() == player
end