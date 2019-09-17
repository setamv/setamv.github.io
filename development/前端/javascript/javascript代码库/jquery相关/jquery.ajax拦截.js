/**
 * 下面对jquery的ajax方法进行拦截，可以增加钩子，或对url进行预处理（比如增加公共的参数）
 */
addHook($, "ajaxSetup", function () {
    var settings = arguments[1];
    if (!settings) {
        return;
    }
    // 增加公共参数
    settings.url = appendUrlParam(settings.url, "uid", uid);
    // 拦截success 和 error方法，处理公共错误吗
    addHook(settings, 'success', function (data) {
        if (!processSaasCommError(data)) {
            return false;
        }
    });
    addHook(settings, 'error', function (data) {
        var response = null;
        if (data) {
            response = data.responseText && !isBlank(data.responseText) ? data.responseText : data.responseJSON;
            if (!processSaasCommError(response)) {
                return false;
            }
        }
    });
});

/**
 * 将key=value值对追加到url后面，作为参数
 * @param url
 * @param key
 * @param value
 * @returns 新的url
 */
function appendUrlParam(url, key, value) {
    if (isBlank(url) || isBlank(key)) {
        return url;
    }
    if (url.indexOf('?') != -1) {
        url += "&";
    } else {
        url += "?";
    }
    url += key + "=" + value;
    return url;
}

// 处理公共错误码
function processSaasCommError(data) {
    // 尝试解析错误信息
    var errorCode = null;
    var errorMessage = null;
    // 如果返回值为字符串，尝试将字符串转JSON
    if (data && typeof(data) == 'string') {
        try {
            var parsedData = eval('(' + data + ')');
            if (parsedData && parsedData["code"] && parsedData["code"] != '0000') {
                errorCode = parsedData["code"];
                errorMessage = parsedData["message"];
            }
        } catch (err) {
            if (console && console.log) {
                console.log(err);
            }
        }
    } else if (typeof(data) == "object" && data["code"] && data["code"] != '0000') {
        errorCode = data["code"];
        errorMessage = parsedData["message"];
    }
    if (!isBlank(errorCode)) {
        // 此处可以对特殊的错误码进行处理
        return false;
    }
    return true;
}

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