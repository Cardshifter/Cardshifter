-- Always name this function "startGame"
function startGame(game)
    print("test: " .. game:toString())

	local numPlayers = game:getPlayers():size()
	for i=0,numPlayers-1 do
		local player = game:getPlayer(i)
		print("Player: " .. player:toString())
		
	    player.data.life = 42
	    local field = game:createZone(player, "Battlefield")
	    player.data.battlefield = field
		
		for i=1,3 do
			local card = field:createCardOnBottom()
			card:addAction("Use", useAllowed, useCard)
	    end
	end 
end

function useAllowed(card)
    return true
end

function useCard(card)
    local owner = card:getOwner()
    local opp = owner:getNextPlayer()
    opp.data.life = opp.data.life - 1
    card:destroy()
end