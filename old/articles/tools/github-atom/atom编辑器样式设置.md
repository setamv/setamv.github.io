[Back](index.md)

# Introduction

Atom编辑器的样式设置。

# Content Catalogue <a id="≡">≡</a>

- [字体设置](#EFS)

# Content

## 字体设置 <a id="FS">[≡](#≡)</a>
修改Atom编辑器字体的方法为：    
1. 依次选择菜单项 "File\Stylesheet"，将打开Atom编辑器的样式设置文件。
2. 在样式设置文件的"atom-text-editor"一项中，添加"font-family"的设置，如下所示：
```
atom-text-editor {
  font-family: "YaHei Consolas Hybrid"
}
```

中英文推荐使用 "YaHei Consolas Hybrid" 字体，该字体系统默认未安装，需要先手动安装到系统。字体安装文件和安装方法，参见Eclipse中对该字体设置的描述：[YaHei Consolas Hybrid字体设置](../eclipse/eclipse开发环境配置.md#EFS-FS-YCH)