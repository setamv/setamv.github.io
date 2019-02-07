[httpcomponents](../index.md)

### Httpcore

#### HTTP Messages

- Structure
    
    A HTTP message consists of a header and an optional body. 
    The message header of an HTTP request consists of a request line and a collection of header fields. 
    The message header of an HTTP response consists of a status line and a collection of header fields. 
    **All** HTTP messages **must** include the protocol version. Some HTTP messages can optionally enclose a content body.

- Basic operations

    + HTTP request message
    
        ```
        GET /cgi/pin.php?r=46541711&s=0&p=gz-59ca0a16afe0df16f9193f508c78fb8d5dcb HTTP/1.1
        Host: login.sina.com.cn
        Connection: keep-alive
        Cache-Control: max-age=0
        Upgrade-Insecure-Requests: 1
        User-Agent: Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36
        Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8
        Accept-Encoding: gzip, deflate, sdch
        Accept-Language: zh-CN,zh;q=0.8
        Cookie: U_TRS1=0000004b.5eb5a16.58268655.5b85ef36; UOR=www.baidu.com,blog.sina.com.cn,; SINAGLOBAL=113.222.202.75_1478919765.288145; ULV=1478919669080:1:1:1::; vjuids=4f8f30900.158567b465c.0.0cd5fdfe42347; vjlast=1478919669.1490021880.11; SUB=_2AkMvj136f8NhqwJRmP4QxGnraIRzwg_EieKZ06whJRMyHRl-yD83qm1StRCRxcSjJZmTBsyRUsy5ItJ_wSEhrA..; SUBP=0033WrSXqPxfM72-Ws9jqgMF55529P9D9W5XJZ9HyoAvNlnPb1ib-eJo; Apache=222.244.126.239_1490277077.543323; ULOGIN_IMG=gz-59ca0a16afe0df16f9193f508c78fb8d5dcb
        ```

        * The first line of that message includes the method to apply to the resource, the identifier of the resource, and the protocol version in use.
        
        