-- Always name this function "startGame"
function startGame(game)
    print("test: " .. game:toString())
    players = game:getPlayers()
    print("test: " .. players:toString())
    first = players:get(0)
    print("test: " .. first:toString())

    first.data.life = 42
end