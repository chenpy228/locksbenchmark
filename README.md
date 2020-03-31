# locksbenchmark
[![Build Status](https://www.travis-ci.org/chenpy228/locksbenchmark.svg?branch=master)](https://www.travis-ci.org/chenpy228/locksbenchmark)
[![Coverage Status](https://coveralls.io/repos/github/chenpy228/locksbenchmark/badge.svg?branch=master)](https://coveralls.io/github/chenpy228/locksbenchmark?branch=master)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=locksbenchmark&metric=alert_status)](https://sonarcloud.io/dashboard?id=locksbenchmark)

Benchmark code of various locks in java8.

## How to start
Use `maven` to build. The specific commands are as follows:
``` shell
$ git clone git@github.com:chenpy228/locksbenchmark.git
$ cd locksbenchmark
$ mvn clean package
$ java -jar target/locksbenchmark-1.0.jar -h    # add -h to show help info
$ java -jar target/locksbenchmark-1.0.jar       # begin to test by using default parameter
```

## Principles
Since it's a synchronous test, of course, you need to use multithreading, and you'd better cover multiple scenarios. In summary, this test has the following key points:
- You can specify the number of reading and writing threads to simulate scenarios such as reading more and writing less, reading less and writing more, reading writing ratio, etc.
- By using the `CyclicBarrier` guarantee, all threads start timing at the moment when they start working. `CountDownLatch` ensures that as long as one thread reaches its goal, it will end the timing to ensure that the timing is as accurate as possible.
- For each synchronization mechanism, after multiple rounds of testing, the minimum value and maximum value are removed and then the average value is taken to avoid the singular value when the JVM is not preheated.
- The code in the synchronization code block is very simple, that is, accumulate the long value to a target value. If you need to test the new synchronization mechanism, you only need to extend the counter class, and the specific implementation can refer to the class `Raw`.

## License
Apache-2.0
