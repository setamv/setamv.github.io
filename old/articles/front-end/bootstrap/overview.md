[Back](index.md)

# Bootstrap Overview

# Introduction

Bootstrap的基本介绍

# Content Catalogue <a id="≡">≡</a>

- [Mobild First](#MF)
- [Containers](#C)
    + [Differences between `.container` and `.container-fluid`](#C-D)


# Content 

## Mobild First <a id="MF">[≡](#≡)</a>

Bootstrap is mobile first. To ensure proper rendering and touch zooming, add the viewport meta tag to your <head>.
```
<meta name="viewport" content="width=device-width, initial-scale=1">
```

You can disable zooming capabilities on mobile devices by adding user-scalable=no to the viewport meta tag. This disables zooming, which results in your site feeling a bit more like a native application:     
```
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
```


## Containers <a id="C">[≡](#≡)</a>

Bootstrap requires a containing element to wrap site contents and house our grid system. You may choose one of two containers to use in your projects. Note that, due to padding and more, neither container is **_nestable_**.

Use `.container` for a responsive fixed width container.        
```
<div class="container">
  ...
</div>
```

Use `.container-fluid` for a full width container, spanning the entire width of your viewport.  
```
<div class="container-fluid">
  ...
</div>
```


### Differences between `.container` and `.container-fluid` <a id="C-D">[≡](#≡)</a>

The styles applied by `.container` are:         
```
@media (min-width: 1200px)
.container {
    width: 1170px;
}
@media (min-width: 768px)
.container {
    width: 750px;
}
.container {
    padding-right: 15px;
    padding-left: 15px;
    margin-right: auto;
    margin-left: auto;
}
```

The styles applied by `.container-fluid` are:       
```
.container-fluid {
    padding-right: 15px;
    padding-left: 15px;
    margin-right: auto;
    margin-left: auto;
}
```

可以看到，`.container`指定了不同视口(ViewPort)宽度下的容器宽度，例如，当视口宽度大于"1200px"时，容器的宽度被设定为了"1170px"；而当视口宽度在"768px"和"1200px"之间时，容器的宽度将被设置为"750px"。     
而`.container-fluid`并未动态指定容器的宽度，默认情况下其宽度就是父容器的宽度。    
所以，在一般情况下，`.container`的容器看上去会比`.container-fluid`的容器要窄一些。

注意，`.container`的宽度是根据**_视口_**的宽度来设定的，而不是父容器。所以，当给`.container`容器外面包裹一个宽度比较窄的父容器时，`.container`的宽度不会根据父容器的宽度而设定，它将仍然根据视口的宽度来设定。
如下的Html代码所示([完整的代码](demos/overview.html#demo3))：    
```
<div style="width: 400px; height:200px; margin:40px auto; border:solid 4px #AAA">           
    <div class="container" style="height:100px; margin-top:50px; border:solid 2px #888">
    
    </div>
</div>
```
其显示效果如下图所示：      
![图一](images/overview-1.png)

