web-stress-simulator
====================

JavaEE application for simulating various conditions of an application under stress (high load on cpus, high memory usage, high network throughput, hight backend delays, unusual HTTP return codes)

Install the package on any Servlet Container and perform HTTP GET calls like:
  * /web-stress-simulator-1.0.0/[operation]?[parameters]

Where:
[parameters] may be:
* "time": time in milliseconds - minimum time of the request
* "nbytes": number of bytes - memory allocation or output generation
* "random": true or false - evenly randomizes "time" and "nbytes" so that each request will have a different value whose max value are the parameters "time" and "nbytes"
* "log": outputs basic information to System.out on each request
* "cacheTTL": time in seconds - time in seconds for validity of the resource in client cache. This setups "Cache-Control" on HTTP response headers
* "http-status": http status code - http status code to be returned on the response for the request
[operation] may be:
* "cpu": use all available cpu resources for "time" milliseconds, so the Thread serving the request will cause a high load on the server (affects a single core per request). The server will be performing basic division and multiplication of double numbers in order to cause the high load.
* "memory": allocate "nbytes" on an internal array and wait for "time" milliseconds before releasing resources then return request info. A new array is allocated and filled with random values on each request.
* "delay": delay request for "time" milliseconds than return request info. This is based on Thread.sleep(time);
* "output": generates "nbytes" of text data as response. Mime type is "text/plain".
