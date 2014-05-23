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

### Usage

#### An example with 2 JVMs for Workers, each with 4 Actors within them

Open 3 terminal widows:

* In window 1, `cd singlet/RemoteMaster && sbt 'run "1" "4"'`
    * this is the 1st Remote worker manager
* In window 2, `cd singlet/RemoteMaster && sbt 'run "2" "4"'`
    * this is the 2nd Remote worker manager
* In window 3, `cd singlet/LocalMaster`
* and then, within widow 3, type `sbt 'run "2" "/Users/me/Pictures/photo.jpg" "/Users/me/Pictures/diff_photo.jpg"'` to convert these two images in parallel.
    * we are telling the local master that there are 2 remote worker managers

The example above can be done with more/less Remote worker managers, each with their own number of Actors (this is capped by how much memory you have availible for each JVM).

### Development

#### Environment

requires Java JDK 7 for `java.nio`, get it [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)

#### About the Code

As of the latest version, the `RemoteMaster` and `RemoteMaster2` directories are identical (except for which port they mount on and their name when they are mounted). This repeated code will be deleted in a future version.

### References

* [Actors in general](http://www.reactive.io/tips/2014/03/28/getting-started-with-actor-based-programming-using-scala-and-akka/)
* [Remote actors](http://alvinalexander.com/scala/simple-akka-actors-remote-example)