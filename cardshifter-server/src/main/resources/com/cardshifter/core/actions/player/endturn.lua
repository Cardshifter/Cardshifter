local endturn = {}

function endturn.isAllowed(player)
	return player:getGame():getCurrentPlayer() == player
end

function endturn.perform(player)
	local game = player:getGame()
	game:nextTurn()
end

return endturn