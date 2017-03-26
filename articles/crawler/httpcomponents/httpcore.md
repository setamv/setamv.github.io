[httpcomponents](../index.md)

### Httpcore

#### HTTP Messages

- Structure
    
    A HTTP message consists of a header and an optional body. 
    The message header of an HTTP request consists of a request line and a collection of header fields. 
    The message header of an HTTP response consists of a status line and a collection of header fields. 
    **All** HTTP messages **must** include the protocol version. Some HTTP messages can optionally enclose a content body.