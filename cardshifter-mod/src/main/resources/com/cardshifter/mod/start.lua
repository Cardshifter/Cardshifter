-- Always name this function "startGame"
function startGame(game)
  local endturnAction = require "com/cardshifter/mod/actions/player/endturn"

	game:on('actionUsed', onActionUsed)
	game:on('turnStart', onTurnStart)

	local numPlayers = game:getPlayers():size()
	for i = 0, numPlayers - 1 do
		local player = game:getPlayer(i)
		print("Player: " .. player:toString())
		
		player:addAction("End Turn", endturnAction.isAllowed, endturnAction.perform)
		
	    player.data.life = 10
	    player.data.mana = 0
	    player.data.manaMax = 0
	    player.data.scrap = 0
	    player.data.cardType = 'Player'
	    local field = game:createZone(player, "Battlefield")
	    field:setGloballyKnown(true)
	    player.data.battlefield = field

	    local deck = game:createZone(player, "Deck")
	    player.data.deck = deck

	    local hand = game:createZone(player, "Hand")
	    hand:setKnown(player, true)
	    player.data.hand = hand
		
		for i = 1, 4 do
			local card
			for strength = 1, 5 do
				card = createCreature(deck, strength, strength, strength, 'B0T')
				if strength == 2 then
					card.data.health = card.data.health - 1
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
	    
		for i=1,5 do
			drawCard(player)
		end
	end
	
	-- Turn Start event is not called when starting game (player should not draw card), setup initial mana for first player
	firstPlayer = game:getFirstPlayer()
	firstPlayer.data.mana = 1
	firstPlayer.data.manaMax = 1
end

function createSpecialEnchantment(deck, strength, health, cost)
  local enchantspecialAction = require "com/cardshifter/mod/actions/card/enchantspecial"
  
    -- A special enchantment can only target a creature that has been enchanted already
	local card = deck:createCardOnBottom()
	card:addTargetAction("Enchant", enchantspecialAction.isAllowed, enchantspecialAction.perform, enchantspecialAction.isTargetAllowed)
	card.data.manaCost = 0
	card.data.scrapCost = cost
	card.data.enchStrength = strength
	card.data.enchHealth = health
	card.data.cardType = 'Enchantment'
	return card
end

function createEnchantment(deck, strength, health, cost)
  local enchantAction = require "com/cardshifter/mod/actions/card/enchant"
  
    -- Can only target creatureType == 'Bio'
	local card = deck:createCardOnBottom()
	card:addTargetAction("Enchant", enchantAction.isAllowed, enchantAction.perform, enchantAction.isTargetAllowed)
	card.data.manaCost = 0
	card.data.scrapCost = cost
	card.data.enchStrength = strength
	card.data.enchHealth = health
	card.data.cardType = 'Enchantment'
	return card
end

function createCreature(deck, cost, strength, health, creatureType)
  local playAction = require "com/cardshifter/mod/actions/card/play"
  local attackAction = require "com/cardshifter/mod/actions/card/attack"
  local scrapAction = require "com/cardshifter/mod/actions/card/scrap"
  
	local card = deck:createCardOnBottom()
	card:addAction("Play", playAction.isAllowed, playAction.perform)
	card:addTargetAction("Attack", attackAction.isAllowed, attackAction.perform, attackAction.isTargetAllowed)
	card:addAction("Scrap", scrapAction.isAllowed, scrapAction.perform)
	card.data.manaCost = cost
	card.data.strength = strength
	card.data.health = health
	card.data.enchantments = 0
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
	
	local field = player.data.battlefield
	local iterator = field:getCards():iterator()
	while iterator:hasNext() do
		local card = iterator:next()
		if card.data.sickness > 0 then
			card.data.sickness = card.data.sickness - 1
			print("Card on field now has sickness" .. card.data.sickness)
		end
		card.data.attacksAvailable = 1
	end
	
	if not drawCard(player) then
		player.data.life = player.data.life - 1
		if player.data.life <= 0 then
			player:getGame():gameOver()
		end
		print("(This is Lua) Deck is empty! One damage taken.")
	end
	player.data.manaMax = player.data.manaMax + 1
	player.data.mana = player.data.manaMax
end

function drawCard(player)
	if player.data.deck:isEmpty() then
    	return false
	end
	local card = player.data.deck:getTopCard()
	card:moveToBottomOf(player.data.hand)
	return true
end