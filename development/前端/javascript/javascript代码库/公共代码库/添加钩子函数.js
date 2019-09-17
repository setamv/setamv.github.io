
/**
 * 为对象的方法添加钩子。如果钩子返回值 === false，将不再执行对象的原方法，并返回false;
 * @param target 目标对象。为object类型
 * @param methodName 需要添加钩子的方法。即为target[methodName]添加钩子
 * @param hook 钩子方法。如果该方法返回值 === false，将不再执行对象的原方法，并返回false;
 */ 
function addHook(target, methodName, hook) {
    if (!target || !target[methodName] || !hook || !isFunction(hook)) {
        return;
    }
    var originMethod = target[methodName];
    target[methodName] = function () {
        var argsArr = Array.prototype.slice.apply(arguments);
        try {
            var ret = hook.apply(this, argsArr);
            if (ret === false) {
                return false;
            }
        } catch (err) {
            if (console && console.log) {
                console.log("call hook failed. error: ")
                console.log(err);
            }
        }
        return originMethod.apply(this, argsArr);
    }
}