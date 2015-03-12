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

JSGame.prototype.getJavaObject = function() {
    return this.javaGame;
}

JSEntity.prototype.getJavaObject = function() {
    return this.javaEntity;
}

JSEntities.prototype.getJavaObject = function() {
    return this.javaEntities;
}

JSGame.prototype.__noSuchMethod__ = function() {
    var IllegalStateException = Java.type("java.lang.IllegalStateException");
    throw new IllegalStateException("Method " + arguments[0] + " does not exist on JSGame");
}

JSEntity.prototype.__noSuchMethod__ = function() {
    var IllegalStateException = Java.type("java.lang.IllegalStateException");
    throw new IllegalStateException("Method " + arguments[0] + " does not exist on JSEntity");
}

JSEntities.prototype.__noSuchMethod__ = function() {
    var IllegalStateException = Java.type("java.lang.IllegalStateException");
    throw new IllegalStateException("Method " + arguments[0] + " does not exist on JSEntities");
}

function loadDSL(dslClass) {
    var Modifier = Java.type("java.lang.reflect.Modifier");
    var IllegalStateException = Java.type("java.lang.IllegalStateException");

    var methods = dslClass.getDeclaredMethods();
    for (var i = 0; i < methods.length; i++) {
        var method = methods[i];
        if (!Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers()) || method.isSynthetic() || method.isBridge()) {
            continue;
        }
        var methodName = method.getName();
        var returnType = method.getReturnType();
        var returnTypeName = returnType.getTypeName();

        var newFunction;
        if (returnTypeName === "void") {
            newFunction = (function(method) {
                return function() {
                    var argumentsArray = Array.prototype.slice.call(arguments);
                    argumentsArray.unshift(this.getJavaObject());;
                    method.invoke(null, argumentsArray);
                }
            })(method);
        }
        else if (returnTypeName === "com.cardshifter.modapi.base.Entity") {
            newFunction = (function(method) {
                return function() {
                    var argumentsArray = Array.prototype.slice.call(arguments);
                    argumentsArray.unshift(this.getJavaObject());
                    return new JSEntity(method.invoke(null, argumentsArray));
                }
            })(method);
        }
        else if (returnTypeName === "java.util.List") {
            newFunction = (function(method) {
                return function() {
                    var argumentsArray = Array.prototype.slice.call(arguments);
                    argumentsArray.unshift(this.getJavaObject());
                    return new JSEntities(method.invoke(null, argumentsArray));
                }
            })(method);
        }
        else {
            throw new IllegalStateException("Unsupported return type for method " + methodName + ": " + returnTypeName);
        }

        var parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0) {
            throw new IllegalStateException("Expected method parameters for method " + methodName + ": none were found");
        }
        var firstParameterType = parameterTypes[0];
        var firstParameterTypeName = firstParameterType.getTypeName();
        if (firstParameterTypeName === "com.cardshifter.modapi.base.ECSGame") {
            JSGame.prototype[methodName] = newFunction;
        }
        else if (firstParameterTypeName === "com.cardshifter.modapi.base.Entity") {
            JSEntity.prototype[methodName] = newFunction;
        }
        else if (firstParameterTypeName === "java.util.List") {
            JSEntities.prototype[methodName] = newFunction;
        }
        else {
            throw new IllegalStateException("Unsupported first parameter type for method " + methodName + ": " + firstParameterTypeName);
        }
    }
}