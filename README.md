## [Singlet](https://en.wikipedia.org/wiki/Singlet_state)

A proving ground for an image converting utility.

As of now, this project contains a script to run locally that scales images with the greatest possible throughput.

### Performance Tests

All tests are run three times in a row, with the median time displayed here.

#### 100 x 30MB images (total 3.09 GB)

**Latest Version** (2 instances of RemoteMaster, each w/ 4 workers)
```
0m29.069s
```
Wow, 3x improvement! What changed here was using the gm command `scale` instead of `resize` (at the suggestion of gm4java's author. The big difference is partly because the image no longer needs its dimensions checked since scale takes an output resolution while resize takes a percentage)

**Latest Version** (1 instance of RemoteMaster w/ 8 workers)
```
0m27.819s
```

---

Version: e1a34b9 (2 instances of RemoteMaster each w/ 4 workers)
```
1m51.552s
```

Version: e1a34b9 (3 instances of RemoteMaster each w/ 3 workers)
```
1m39.190s
```

Version: e1a34b9 (4 instances of RemoteMaster each w/ 2 workers)
```
1m39.871s
```

Version: e1a34b9 (4 instances of RemoteMaster each w/ 3 workers)
```
1m34.310s
```

Version: e1a34b9 (4 instances of RemoteMaster 2 w/ 4 workers, 2 w/ 3)
(4 w /4 failed)
```
1m32.502s
```

### Old tests

#### 37 x 30MB images (total 1.09 GB)
<sub>localdir `Documents/doublet/test_images/avg_size`</sub>

Running serially (version in the 2nd commit: e2b7af8)
```
real    1m58.674s
```

Old Parallel akka version with 5 workers (commit: ab97939):
```
real    0m43.117s
```
(with 4 workers was 2 seconds slower)

Version: e1a34b9 (two instances of RemoteMaster each w/ 4 workers)
```
real    0m34.700s
```

### Usage

The number of `ImageProcessor` workers that you should start (which each start 1 gm process) should equal the number of logical cores on your machine returned by: `Runtime.getRuntime.availableProcessors`.

Optimally, there should be as many gm processes started as there are effective cores in your computer. If you are on an intel chipset, it is likely that your processor is hyper-threaded, and the number of independent execution engines available is not equal to the number of physically distinct processors in your machine.

#### An example with 2 JVMs for Workers, each with 4 Actors within them

Open 3 terminal widows in the root directory of singlet:

* In window 1, `sbt 'run-main remote.Remote "1" "4"'`
    * this is the 1st Remote worker manager
* In window 2, `sbt 'run-main remote.Remote "2" "4"'`
    * this is the 2nd Remote worker manager
* In window 3, run `sbt 'run-main local.Local "2" "/Users/me/Pictures/photo.jpg" "/Users/me/Pictures/diff_photo.jpg"'` to convert these two images in parallel.
    * we are telling the local master that there are 2 remote worker managers

The example above can be done with more/less Remote worker managers, each with their own number of Actors (this is capped by how much memory you have available for each JVM).

### Development

#### Environment

requires Java JDK 7 for `java.nio`, get it [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)

### References

* [Actors in general](http://www.reactive.io/tips/2014/03/28/getting-started-with-actor-based-programming-using-scala-and-akka/)
* [Remote actors](http://alvinalexander.com/scala/simple-akka-actors-remote-example)
* [gm4java docs](http://sharneng.github.io/gm4java/doc/1.1.0/allclasses-noframe.html)

### Project Goals

- [ ] portability/modularity
    - [ ] providers
    - [ ] storage
- [ ] support custom processing (either natively or through pre/post-processing via a separate service)
    - [ ] *List of these is coming*
- [x] overall processing speed
- [x] parallelized processing of different versions
    - [x] scalability across a cluster is easily supported by the communication between Actors in different JVMs demonstrated by `LocalMaster` and `RemoteMaster`
- [ ]  prioritizing more immediately-needed versions
    - [ ] *available and coming soon* http://doc.akka.io/docs/akka/snapshot/scala/mailboxes.html
- [x] handle very large original files
    - [ ] *performance tests for these are coming*
- [x] handle wide variety of original formats
    - [x] [List of supported formats](http://www.graphicsmagick.org/formats.html) includes [animated GIFs](http://www.graphicsmagick.org/FAQ.html#how-do-i-create-a-gif-animation-sequence-to-display-within-firefox)
- [ ] preserve originals
- [ ] handle new kinds of image versions, either based on ad-hoc URLs, or by management of approved versions
    - [ ] would be easy as demonstrated in the `ImageResize` actor
- [ ] incremental backup (particularly of originals)
- [ ] good local/development workflow (it should at least work)
- [ ] stability: processing should recover, no matter how large or corrupt an original
    - [ ] On failure re-queuing of an image with actors is easy. Actors provide great encapsulation for individual image failures. *Demo of this is coming*
- [ ] feasible migration path for legacy images
