example pipe service
===

#### run
pipe file upload from one service into another

1. Update application.conf
 - `source.file` → file to transfer

2. Run `com.github.jw3.Boot`

3. `curl localhost:8082/pipe`

See `Server` implementations in each package for details


#### next
- the pipe should be able to customize the source and dest requests based on the request it was called with
- proper error handling examples
