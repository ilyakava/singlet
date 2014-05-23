## [Singlet](https://en.wikipedia.org/wiki/Singlet_state)

An image converting utility.

### Performance

All tests are run three times in a row, with the mediam time displayed here.

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

Latest Version (two instances of RemoteMaster each w/ 4 workers)
```
real    0m34.700s
```

#### 100 x 30MB images (total 3.09 GB)

Latest Version (2 instances of RemoteMaster each w/ 4 workers)
```
1m51.552s
```

Latest Version (3 instances of RemoteMaster each w/ 3 workers)
```
1m39.190s
```

Latest Version (4 instances of RemoteMaster each w/ 2 workers)
```
1m39.871s
```

Latest Version (4 instances of RemoteMaster each w/ 3 workers)
```
1m34.310s
```

Latest Version (4 instances of RemoteMaster 2 w/ 4 workers, 2 w/ 3)
(4 w /4 failed)
```
1m32.502s
```
#### Notes

Running gm in batch mode from the command line only spawns a single process. My code spawns 1 per actor.

### Usage

#### An example with 2 JVMs for Workers, each with 4 Actors within them

Open 3 terminal widows in the root directory of singlet:

* In window 1, `sbt 'run-main remote.Remote "1" "4"'`
    * this is the 1st Remote worker manager
* In window 2, `sbt 'run-main remote.Remote "2" "4"'`
    * this is the 2nd Remote worker manager
* In window 3, run `sbt 'run-main local.Local "2" "/Users/me/Pictures/photo.jpg" "/Users/me/Pictures/diff_photo.jpg"'` to convert these two images in parallel.
    * we are telling the local master that there are 2 remote worker managers

The example above can be done with more/less Remote worker managers, each with their own number of Actors (this is capped by how much memory you have availible for each JVM).

### Development

#### Environment

requires Java JDK 7 for `java.nio`, get it [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)

### References

* [Actors in general](http://www.reactive.io/tips/2014/03/28/getting-started-with-actor-based-programming-using-scala-and-akka/)
* [Remote actors](http://alvinalexander.com/scala/simple-akka-actors-remote-example)
