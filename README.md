example pipe service
===

pipe file upload from one service into another via a broker which allows monitoring of the transfer

```
+-----+       +------+        +-----+
| LHS | ----> | PIPE |  ----> | RHS |
+-----+       +------+        +-----+
```

- LHS: Source of File download (eg. S3, HTTP, etc)
- RHS: Destination of File upload as multi-part post

#### Goals
1. monitor a transfer that otherwise has no status
2. stream a file download into a multipart post upload

#### Examples
- Websocket
- File source
- File upload
- Multi-part post
- ActorPublisher

#### Next
- the pipe should be able to customize the source and dest requests based on the request it was called with
- proper error handling examples
- modify requests based on pipe configuration

#### Run

1. Update application.conf
 - `source.file` → file to transfer
 - use a large text file for most interesting results

2. Run `com.github.jw3.Boot`

3. `curl localhost:8082/slow/pipe`
- receive stream ID  ```{"id":"39f37e6"}```

4. Use stream ID to connect webhook to transfer
- ```ws://localhost:8082/hook/39f37e6```

#### More

See `Server` implementations in each package for details
