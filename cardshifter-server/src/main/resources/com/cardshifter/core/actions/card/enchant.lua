local enchant = {}

function enchant.isAllowed(card, action)
  local playAction = require "src/main/resources/com/cardshifter/core/actions/card/play"
  
	if not playAction.isAllowed(card) then
		return false
	end
	local currentPlayer = card:getGame():getCurrentPlayer()
	if card.data.cardType ~= 'Enchantment' then
		return false
	end
	if card.data.scrapCost > currentPlayer.data.scrap then
		return false
	end
	return true
end

function enchant.perform(card, target, action)
	target.data.enchantments = target.data.enchantments + 1
	target.data.health = target.data.health + card.data.enchHealth
	target.data.strength = target.data.strength + card.data.enchStrength
	local owner = card:getOwner()
	owner.data.scrap = owner.data.scrap - card.data.scrapCost 
	card:destroy()
end

function enchant.isTargetAllowed(card, target, action)
	if target.data.cardType ~= 'Creature' then
		return false
	end
	if target.data.creatureType ~= 'Bio' then
		return false
	end
	if target:getOwner() ~= card:getOwner() then
	  return false
	end
	if target:getZone() ~= card:getOwner().data.battlefield then
	  return false
	end
	return true
end

return enchant