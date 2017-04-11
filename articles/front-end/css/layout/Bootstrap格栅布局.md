[Back](index.md)

# Bootstrap格栅布局

# Introduction

使用Bootstrap格栅布局，可以很容易的实现一些移动端、PC端自适应的流式布局。
下面先介绍Bootstrap的格栅布局的基本内容，然后给出一些比较经典的应用案例。

# Content Catalogue <a id="≡">≡</a>

- [负边距在普通文档流中的作用和效果](#Tag1)
- [理解负边距的裁减效果](#Tag2)
- [左和右的负边距对元素宽度的影响](#Tag3)
    * [左和右的负边距对元素宽度的影响的应用](#Tag3-1)
- [负边距对绝对定位元素的影响](#Tag4)


# Content

## Mobild First

Bootstrap is mobile first. To ensure proper rendering and touch zooming, add the viewport meta tag to your <head>.
```
<meta name="viewport" content="width=device-width, initial-scale=1">
```

You can disable zooming capabilities on mobile devices by adding user-scalable=no to the viewport meta tag. This disables zooming, which results in your site feeling a bit more like a native application:     
```
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
```

