local scrap = {}

function scrap.isAllowed(card)
	local currentPlayer = card:getGame():getCurrentPlayer()
	if card:getOwner() ~= currentPlayer then
		return false
	end
	if card:getZone() ~= currentPlayer.data.battlefield then
		return false
	end
	return true
end

function scrap.perform(card)
	local owner = card:getOwner()
	card:destroy()
	owner.data.scrap = owner.data.scrap + 1
end

return scrap