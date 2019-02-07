[Home](../index.md) << [Book List Index](index.md)

- [Linux Book List](book-list-linux.md)
    linux相关的书单

- [Database Book List](book-list-database.md)  
    数据库相关的书单

- [CSS Book List](book-list-css.md)
    CSS相关的书单

#### How to export bookmarks from pdf and translate bookmarks into markdown format?
1. 使用FreePic2Pdf软件将pdf的书签导出，操作为：
    1. 打开FreePic2Pdf软件，点击界面右下角的 “*更改PDF*”按钮
    2. 在弹出的界面选中 “*从PDF取书签*” Tab页，然后选择源PDF文件和导出的目标目录，并点击 “*开始*” 按钮即可。
2. 使用Sublime Text将步骤1中导出的书签转换成Markdown的格式，步骤如下：
    1. 将导出的标签文本使用Sublime Text打开，然后使用快捷键 Ctrl + H 打开Sublime Text的替换功能，使用快捷键 Alt + R 选中 “regular expression”选项
    2. 替换二级以下的目录（即导出的书签中以空格开始的那些书签行）：在查找栏中输入正则表达式：“^(\s+)(\w+)”，在替换栏中输入替换的正则表达式：“$1- $2”，然后点击 “*Replace ALl*” 即可。
    3. 替换一级目录（即导出的书签中无空格开始的那些书签行）：在查找栏中输入正则表达式：“^(?!=\s)(\w+)”，在替换栏中输入替换的正则表达式：“- $1”，然后点击 “Replace ALl” 即可。