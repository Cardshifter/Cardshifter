local play = {}

function play.isAllowed(card)
	local currentPlayer = card:getGame():getCurrentPlayer()
	if card:getOwner() ~= currentPlayer then
		return false
	end
	if card:getZone() ~= currentPlayer.data.hand then
		return false
	end
	if card.data.manaCost > currentPlayer.data.mana then
		return false
	end
	return true
end

function play.perform(card)
  local owner = card:getOwner()
  card:moveToBottomOf(owner.data.battlefield)
	
  owner.data.mana = owner.data.mana - card.data.manaCost
end

return play