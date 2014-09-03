-- Always name this function "startGame"
function startGame(game)
  local endturnAction = require "src/main/resources/com/cardshifter/core/actions/player/endturn"

  game:on('actionUsed', onActionUsed)
  game:on('turnStart', onTurnStart)

	local numPlayers = game:getPlayers():size()
	for i=0,numPlayers-1 do
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
  local enchantspecialAction = require "src/main/resources/com/cardshifter/core/actions/card/enchantspecial"
  
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
  local enchantAction = require "src/main/resources/com/cardshifter/core/actions/card/enchant"
  
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
  local playAction = require "src/main/resources/com/cardshifter/core/actions/card/play"
  local attackAction = require "src/main/resources/com/cardshifter/core/actions/card/attack"
  local scrapAction = require "src/main/resources/com/cardshifter/core/actions/card/scrap"
  
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
	if player.data.deck:isEmpty() then
		print("(This is Lua) Deck is empty!")
	end
	
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
	
	drawCard(player)
	player.data.manaMax = player.data.manaMax + 1
	player.data.mana = player.data.manaMax
end

function drawCard(player)
	local card = player.data.deck:getTopCard()
	card:moveToBottomOf(player.data.hand)
end

--function playAllowed(card)
--	local currPlayer = card:getGame():getCurrentPlayer()
--	if card:getOwner() ~= currPlayer then
--		return false
--	end
--	if card:getZone() ~= currPlayer.data.hand then
--		return false
--	end
--	if card.data.manaCost > currPlayer.data.mana then
--		return false
--	end
--	return true
--end
--
--function playCard(card)
--    local owner = card:getOwner()
--	card:moveToBottomOf(owner.data.battlefield)
--	
--	owner.data.mana = owner.data.mana - card.data.manaCost
--end

--function attackTargetAllowed(card, target, action)
--	local currPlayer = card:getGame():getCurrentPlayer()
--	local oppPlayer = currPlayer:getNextPlayer()
--	local oppBattlefield = oppPlayer.data.battlefield
--	
--	if oppBattlefield:isEmpty() then
--		return target == oppPlayer
--	end
--	if target.data.cardType == 'Player' then
--		return false
--	end
--	if target:getZone() ~= oppBattlefield then
--		return false
--	end
--	print("Allowed to target unit in " .. target:getZone():toString() .. " Target is " .. target.data.cardType)
--	return true
--end
--
--function attackAllowed(card, action)
--	local currPlayer = card:getGame():getCurrentPlayer()
--	if card:getOwner() ~= currPlayer then
--		return false
--	end
--	if card:getZone() ~= currPlayer.data.battlefield then
--		return false
--	end
--	if card.data.attacksAvailable <= 0 then
--		return false
--	end
--	if card.data.sickness > 0 then
--		return false
--	end
--	return true
--end
--
--function attackCard(card, target, action)
--	card.data.attacksAvailable = card.data.attacksAvailable - 1
--	if target.data.cardType == 'Player' then
--		target.data.life = target.data.life - card.data.strength
--		if target.data.life <= 0 then
--			card:getGame():gameOver()
--		end
--		return true
--	end
--	target.data.health = target.data.health - card.data.strength
--	card.data.health = card.data.health - target.data.strength
--	
--	if target.data.health <= 0 then
--		local opp = target:getOwner()
--		-- All units have trample
--		-- Target health is negative so add that to opponent life
--		opp.data.life = opp.data.life + target.data.health
--		if opp.data.life <= 0 then
--			card:getGame():gameOver()
--		end
--		target:destroy()
--	end
--	if card.data.health <= 0 then
--		card:destroy()
--	end
--
--	return false
--end

--function enchAllowed(card)
--	if not playAllowed(card) then
--		return false
--	end
--	local currPlayer = card:getGame():getCurrentPlayer()
--	if card.data.cardType ~= 'Enchantment' then
--		return false
--	end
--	if card.data.scrapCost > currPlayer.data.scrap then
--		return false
--	end
--	return true
--end
--
--function enchSpecialTargetAllowed(source, target, action)
--	if target.data.cardType ~= 'Creature' then
--		return false
--	end
----	if table.getn(target.data.enchantments) == 0 then
--	if target.data.enchantments <= 0 then
--		return false
--	end
--	return true
--end
--
--function enchTargetAllowed(source, target, action)
--	if target.data.cardType ~= 'Creature' then
--		return false
--	end
--	if target.data.creatureType ~= 'Bio' then
--		return false
--	end
--	return true
--end
--
--function enchCard(card, target, action)
--	target.data.enchantments = target.data.enchantments + 1
--	target.data.health = target.data.health + card.data.enchHealth
--	target.data.strength = target.data.strength + card.data.enchStrength
--	local owner = card:getOwner()
--	owner.data.scrap = owner.data.scrap - card.data.scrapCost 
--	card:destroy()
--end

--function scrapAllowed(card)
--	local currPlayer = card:getGame():getCurrentPlayer()
--	if card:getOwner() ~= currPlayer then
--		return false
--	end
--	if card:getZone() ~= currPlayer.data.battlefield then
--		return false
--	end
--	return true
--end
--
--function scrapCard(card)
--	local owner = card:getOwner()
--	card:destroy()
--	owner.data.scrap = owner.data.scrap + 1
--end

--function endTurn(player)
--	local game = player:getGame()
--	game:nextTurn()
--end
--
--function allowNextTurn(player)
--	return player:getGame():getCurrentPlayer() == player
--end