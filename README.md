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

Examples:
* http://localhost:8080/web-stress-simulator-1.0.0/cpu?time=1000 - causes a request to last one second. During that time it will try to use 100% of a CPU core.
* http://localhost:8080/web-stress-simulator-1.0.0/memory?nbytes=10000000&time=5000 - causes a request to last five seconds. During that time 10MB of memory will be allocated
* http://localhost:8080/web-stress-simulator-1.0.0/delay?time=30000 - causes a request to last 30 seconds. During that time no CPU resources are spent, simulating slow or hung backend calls
* http://localhost:8080/web-stress-simulator-1.0.0/delay?time=3000&random=true - causes a request to last, randomically, from 0 to 3 seconds
* http://localhost:8080/web-stress-simulator-1.0.0/output?nbytes=3000 - causes 3KB of random text data to be returned from the server
* http://localhost:8080/web-stress-simulator-1.0.0/output?nbytes=3000&time=60000 - the same as above, but now it will generate 3KB of data with a data rate of 0.5b/s so that it will last 60 seconds to output the whole data. It's usefull to test network appliances under slow connections conditions
* http://localhost:8080/web-stress-simulator-1.0.0/delay?time=3000&random=true&http-status=500 - causes a request with random duration (0-3s) to return a response indicating an internal error
