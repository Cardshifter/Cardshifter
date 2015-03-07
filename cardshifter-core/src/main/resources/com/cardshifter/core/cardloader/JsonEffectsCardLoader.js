function makeJSGame(javaGame) {
    return new JSGame(javaGame);
}

function JSGame(javaGame) {
    this.javaGame = javaGame;
}

function JSEntity(javaEntity) {
    this.javaEntity = javaEntity;
}

function JSEntities(javaEntities) {
    this.javaEntities = javaEntities;
}

function loadDSLManual(qualifiedDslClassName) {
    //manual -- should be automated

    var dslType = Java.type(qualifiedDslClassName);

    JSGame.prototype.opponent = function() {
        return new JSEntity(dslType.opponent(this.javaGame));
    }

    JSEntity.prototype.characters = function() {
        return new JSEntities(dslType.characters(this.javaEntity));
    }

    JSEntities.prototype.pickRandom = function(count) {
        return new JSEntities(dslType.pickRandom(this.javaEntities, count));
    }

    JSEntities.prototype.dealDamage = function(damage) {
        dslType.dealDamage(this.javaEntities, damage);
    }
}