# ZooKeeper节点路径的规则
ZooKeeper的节点路径path必须满足一定的规则，如下：
1. path != null
2. path.length != 0
3. path不能以"/"开始
4. path不能以"/"结尾
5. path中不能包含连续的两个"/"
5. path中不能包含"/../"或"/.."（代表相对路径）
6. 不能包含以下字符c：
    c > '\u0000' && c <= '\u001f'
                || c >= '\u007f' && c <= '\u009F'
                || c >= '\ud800' && c <= '\uf8ff'
                || c >= '\ufff0' && c <= '\uffff'

ZooKeeper校验节点路径的代码在 `org.apache.zookeeper.common.PathUtils` 中，如下所示：
```
/**
* Validate the provided znode path string
* @param path znode path string
* @throws IllegalArgumentException if the path is invalid
*/
public static void validatePath(String path) throws IllegalArgumentException {
    if (path == null) {
        throw new IllegalArgumentException("Path cannot be null");
    }
    if (path.length() == 0) {
        throw new IllegalArgumentException("Path length must be > 0");
    }
    if (path.charAt(0) != '/') {
        throw new IllegalArgumentException("Path must start with / character");
    }
    if (path.length() == 1) { // done checking - it's the root
        return;
    }
    if (path.charAt(path.length() - 1) == '/') {
        throw new IllegalArgumentException("Path must not end with / character");
    }

    String reason = null;
    char lastc = '/';
    char[] chars = path.toCharArray();
    char c;
    for (int i = 1; i < chars.length; lastc = chars[i], i++) {
        c = chars[i];

        if (c == 0) {
            reason = "null character not allowed @" + i;
            break;
        } else if (c == '/' && lastc == '/') {
            reason = "empty node name specified @" + i;
            break;
        } else if (c == '.' && lastc == '.') {
            if (chars[i - 2] == '/' && ((i + 1 == chars.length) || chars[i + 1] == '/')) {
                reason = "relative paths not allowed @" + i;
                break;
            }
        } else if (c == '.') {
            if (chars[i - 1] == '/' && ((i + 1 == chars.length) || chars[i + 1] == '/')) {
                reason = "relative paths not allowed @" + i;
                break;
            }
        } else if (c > '\u0000' && c <= '\u001f'
                || c >= '\u007f' && c <= '\u009F'
                || c >= '\ud800' && c <= '\uf8ff'
                || c >= '\ufff0' && c <= '\uffff') {
            reason = "invalid character @" + i;
            break;
        }
    }

    if (reason != null) {
        throw new IllegalArgumentException("Invalid path string \"" + path + "\" caused by " + reason);
    }
}
```