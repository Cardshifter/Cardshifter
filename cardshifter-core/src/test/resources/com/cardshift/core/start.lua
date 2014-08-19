-- Always name this function "startGame"
function startGame(game)
    print("test: " .. game:toString())
    first = game:getFirstPlayer()
    second = first:getNextPlayer()
    print("test: " .. first:toString())

    first.data.life = 42
    second.data.life = 42
    
    field1 = game:createZone(first, "Battlefield")
    field2 = game:createZone(second, "Battlefield")
    first.data.battlefield = field1
    second.data.battlefield = field2
    
    card = field1:createCardOnBottom()
    card:addAction("Use", useAllowed, useCard)
    field2:createCardOnBottom()
    
    field1:createCardOnBottom()
    field2:createCardOnBottom()
    
    field1:createCardOnBottom()
    field2:createCardOnBottom()
end

function useAllowed(card)
    return true
end

function useCard(card)
    owner = card:getOwner()
    opp = owner:getNextPlayer()
    opp.data.life = opp.data.life - 1;
end