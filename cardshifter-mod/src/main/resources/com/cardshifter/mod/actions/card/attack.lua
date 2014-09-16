local attack = {}

function attack.isAllowed(card, action)
	local currentPlayer = card:getGame():getCurrentPlayer()
	if card:getOwner() ~= currentPlayer then
		return false
	end
	if card:getZone() ~= currentPlayer.data.battlefield then
		return false
	end
	if card.data.attacksAvailable <= 0 then
		return false
	end
	if card.data.sickness > 0 then
		return false
	end
	return true
end

function attack.perform(card, target, action)
	card.data.attacksAvailable = card.data.attacksAvailable - 1
	if target.data.cardType == 'Player' then
		target.data.life = target.data.life - card.data.strength
		if target.data.life <= 0 then
			card:getGame():gameOver()
		end
		return true
	end
	
	if target.data.health <= card.data.strength then
		local opp = target:getOwner()
		-- All units have trample
		-- Target health is negative so add that to opponent life
		local additionalDamage = card.data.strength - target.data.health
		
		opp.data.life = opp.data.life - additionalDamage
		if opp.data.life <= 0 then
			card:getGame():gameOver()
		end
		target:destroy()
	end
	
	if card.data.health <= target.data.strength then
		card:destroy()
	end

	return false
end

function attack.isTargetAllowed(card, target, action)
	local currentPlayer = card:getGame():getCurrentPlayer()
	local oppPlayer = currentPlayer:getNextPlayer()
	local oppBattlefield = oppPlayer.data.battlefield
	
	if oppBattlefield:isEmpty() then
		return target == oppPlayer
	end
	if target.data.cardType == 'Player' then
		return false
	end
	if target:getZone() ~= oppBattlefield then
		return false
	end
	print("Allowed to target unit in " .. target:getZone():toString() .. " Target is " .. target.data.cardType)
	return true
end

return attack