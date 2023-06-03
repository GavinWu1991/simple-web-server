# Simple Web Server
![Coverage](.github/badges/jacoco.svg)

![Branches](.github/badges/branches.svg)

A simplex HTTP 1.0 Server implemented in Java for educational
purposes based on W3C specifications (http://www.w3.org/Protocols/):

* [W3](https://www.w3.org/Protocols/HTTP/AsImplemented.html) Hypertext Transfer Protocol -- HTTP/0.9
* [RFC 1945](http://www.ietf.org/rfc/rfc1945.txt) Hypertext Transfer Protocol -- HTTP/1.0
* [RFC 2616](http://www.ietf.org/rfc/rfc2616.txt) Hypertext Transfer Protocol -- HTTP/1.1
* [RFC 2617](http://www.ietf.org/rfc/rfc2617.txt) HTTP Authentication: Basic and Digest Access Authentication
* [RFC 6265](http://tools.ietf.org/html/rfc6265) HTTP State Management Mechanism (Cookies)

## Build
```
./gradlew jar 
```

## Run
```
java -cp build/libs/simple-web-server-1.0.jar liteweb.Server
```

## Performance test
```
bzt performance.yml
```

## Results

### Before
```
023-06-03 17:11:31,172 INFO Engine.final-stats] Test duration: 0:01:21
[2023-06-03 17:11:31,172 INFO Engine.final-stats] Samples count: 17060, 0.01% failures
[2023-06-03 17:11:31,173 INFO Engine.final-stats] Average times: total 0.230, latency 0.229, connect 0.065
[2023-06-03 17:11:31,174 INFO Engine.final-stats] Percentiles:
+---------------+---------------+
| Percentile, % | Resp. Time, s |
+---------------+---------------+
|           0.0 |         0.003 |
|          50.0 |         0.169 |
|          90.0 |         0.214 |
|          95.0 |         0.707 |
|          99.0 |         1.694 |
|          99.9 |         1.761 |
|         100.0 |         2.266 |
+---------------+---------------+
[2023-06-03 17:11:31,175 INFO Engine.final-stats] Request label stats:
+---------------------------------------------------------+--------+---------+--------+-------------------------------------------------------------------------------------------------------+
| label                                                   | status |    succ | avg_rt | error                                                                                                 |
+---------------------------------------------------------+--------+---------+--------+-------------------------------------------------------------------------------------------------------+
| http://127.0.0.1:8080                                   |   OK   | 100.00% |  0.233 |                                                                                                       |
| http://127.0.0.1:8080/performance.yml                   |   OK   | 100.00% |  0.227 |                                                                                                       |
| http://127.0.0.1:8080/readme.md                         |  FAIL  |  99.99% |  0.227 | Non HTTP response message: Connect to 127.0.0.1:8080 [/127.0.0.1] failed: Connection refused: connect |
| http://127.0.0.1:8080/src/main/java/liteweb/Server.java |   OK   | 100.00% |  0.233 |                                                                                                       |
+---------------------------------------------------------+--------+---------+--------+-------------------------------------------------------------------------------------------------------+
```

### After
```
[2023-06-03 18:26:17,538 INFO Engine.final-stats] Test duration: 0:01:15
[2023-06-03 18:26:17,538 INFO Engine.final-stats] Samples count: 129708, 0.00% failures
[2023-06-03 18:26:17,539 INFO Engine.final-stats] Average times: total 0.030, latency 0.030, connect 0.000
[2023-06-03 18:26:17,539 INFO Engine.final-stats] Percentiles:
+---------------+---------------+
| Percentile, % | Resp. Time, s |
+---------------+---------------+
|           0.0 |         0.003 |
|          50.0 |         0.031 |
|          90.0 |         0.035 |
|          95.0 |         0.037 |
|          99.0 |         0.061 |
|          99.9 |         0.125 |
|         100.0 |         0.185 |
+---------------+---------------+
[2023-06-03 18:26:17,540 INFO Engine.final-stats] Request label stats:
+---------------------------------------------------------+--------+---------+--------+-------+
| label                                                   | status |    succ | avg_rt | error |
+---------------------------------------------------------+--------+---------+--------+-------+
| http://127.0.0.1:8080                                   |   OK   | 100.00% |  0.030 |       |
| http://127.0.0.1:8080/performance.yml                   |   OK   | 100.00% |  0.030 |       |
| http://127.0.0.1:8080/readme.md                         |   OK   | 100.00% |  0.030 |       |
| http://127.0.0.1:8080/src/main/java/liteweb/Server.java |   OK   | 100.00% |  0.030 |       |
+---------------------------------------------------------+--------+---------+--------+-------+
```
