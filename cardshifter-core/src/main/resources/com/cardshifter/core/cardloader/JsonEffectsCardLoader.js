var global = this;

//TODO refactor to create global objects in a method
function JSclass__com_cardshifter_modapi_base_ECSGame(javaObject) {
    this.javaObject = javaObject;
}

JSclass__com_cardshifter_modapi_base_ECSGame.prototype.__noSuchMethod__ = function() {
    throw new IllegalStateException("Method " + arguments[0] + " does not exist on JSclass__com_cardshifter_modapi_base_ECSGame");
}

function makeJSGame(javaGame) {
    return new JSclass__com_cardshifter_modapi_base_ECSGame(javaGame);
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

        var returnTypeName = normalizeGenericType(method.getGenericReturnType().toString());
        if (!global[returnTypeName]) {
            global[returnTypeName] = function(javaObject) {
                this.javaObject = javaObject;
            }
            global[returnTypeName].prototype.__noSuchMethod__ = (function(method) {
                return function() {
                    throw new IllegalStateException("Method " + arguments[0] + " does not exist on " + method.getGenericReturnType());
                }
            })(method);
        }

        var newFunction = (function(method, returnTypeName) {
            return function() {
                var argumentsArray = Array.prototype.slice.call(arguments);
                argumentsArray.unshift(this.javaObject);
                return new global[returnTypeName](method.invoke(null, Java.to(argumentsArray)));
            }
        })(method, returnTypeName);

        var genericParameterTypes = method.getGenericParameterTypes();
        if (genericParameterTypes.length == 0) {
            throw new IllegalStateException("Expected method parameters for method " + methodName + ": none were found");
        }

        var firstGenericParameterType = genericParameterTypes[0];
        var firstGenericParameterTypeName = normalizeGenericType(firstGenericParameterType.toString());
        if (!global[firstGenericParameterTypeName]) {
            global[firstGenericParameterTypeName] = function(javaObject) {
                this.javaObject = javaObject;
            }
            global[firstGenericParameterTypeName].prototype.__noSuchMethod__ = (function(firstGenericParameterType) {
                return function() {
                    throw new IllegalStateException("Method " + arguments[0] + " does not exist on " + firstGenericParameterType);
                }
            })(firstGenericParameterType);
        }
        global[firstGenericParameterTypeName].prototype[methodName] = newFunction;
    }
}

function normalizeGenericType(genericType) {
    return "JS" + genericType
        .replace(/ /g, "__")
        .replace(/\./g, "_")
        .replace(/[<>]/g, "___");
}