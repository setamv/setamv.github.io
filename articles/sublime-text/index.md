### Sublime Text Articles Catalogues

#### Plugins

##### Markdown Plugins
- **Markdown Editing Plugin**     
    　　Markdown Editing Plugin是一个用于编辑Markdown的插件。

- **Markdown Preview Plugin**   
    　　Markdown Priview Plugin是一个预览Markdown的插件，在Sublime Text选择菜单Preferences–>Package Control（快捷键 ctrl+shift+p）中，输入 “*Markdown Preview*”，然后选中 “*Preview In Browser*”，即可在浏览器中查看当前文档的Markdown效果。      
    　　如果觉得上面的操作太麻烦，可以设置快捷键来快速查看：选择菜单 Preferences–>Key Bindings，在User一栏中，输入：
    ```
    [   
        {"keys":["alt+m"], "command":"markdown_preview", "args":{"target": "browser"}}
    ]
    ```
    即可将预览的快捷键设置为 “*alt+m*”


- **Table Editor Plugin**       
    Table Editor Plugin用于编辑表格，快捷方式如下：    

    |   **快捷键**    |                     **功能说明**                    |
    |-----------------|-----------------------------------------------------|
    | ctrl+k, enter   | 用于在当前行的下方插入一行，或用于快速格式化表格    |
    | alt+shift+right | 用于在当前列的前面插入一列                          |
    | alt+shift+left  | 在当前列的前面删除一列                              |
    | alt+right       | 将当前列与后面一列交换顺序                          |
    | alt+left        | 将当前列与前面一列交换顺序                          |
    | alt+shift+up    | 删除表格行中的鼠标所在的文本行                      |
    | alt+shift+down  | 在表格行的鼠标所在文本行上方插入一个文本行          |
    | alt+enter       | 将一行文本从光标处断行成两行文本                    |
    | ctrl+j          | alt+enter的反向操作，即将上下两行文本合并成一行文本 |

    注意：
    1. 目前MarkDown Preview插件在解析时，只有当表格内容行之间没有分割线，且只有标题行下有一条分割线时，才能正常的在HTML中展示为表格。如上面的快捷键表格所示。

