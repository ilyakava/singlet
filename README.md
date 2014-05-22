## [Singlet](https://en.wikipedia.org/wiki/Singlet_state)

An image converting utility.

### Performance

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

Latest Version (two instances of RemoteMaster each w/ 4 workers)
```
real    0m37.997s
```

### Usage

Open 3 terminal widows:

* In window 1, `cd singlet/RemoteMaster && sbt run`
* In window 2, `cd singlet/RemoteMaster2 && sbt run`
* In window 3, `cd singlet/LocalMaster`
* and then, within widow 3, type `sbt 'run "/Users/me/Pictures/photo.jpg" "/Users/me/Pictures/diff_photo.jpg"'` in the root directory to convert these two images in parallel.

### Development

#### Environment

requires Java JDK 7 for `java.nio`, get it [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)

#### About the Code

As of the latest version, the `RemoteMaster` and `RemoteMaster2` directories are identical (except for which port they mount on and their name when they are mounted). This repeated code will be deleted in a future version.

### References

* [Actors in general](http://www.reactive.io/tips/2014/03/28/getting-started-with-actor-based-programming-using-scala-and-akka/)
* [Remote actors](http://alvinalexander.com/scala/simple-akka-actors-remote-example)