## [Singlet](https://en.wikipedia.org/wiki/Singlet_state)

An image converting utility.

### Performance

#### 37 x 30MB images (total 1.09 GB)
<sub>localdir `Documents/doublet/test_images/avg_size`</sub>

Running serially (version in the 2nd commit: e2b7af8)
```
real    1m58.674s
user    1m8.443s
sys 0m1.145s
```

Parallel akka version with 5 workers:
```
real    0m43.117s
user    1m41.265s
sys 0m1.932s
```

### Usage

run `sbt 'run "/Users/me/Pictures/photo.jpg" "/Users/me/Pictures/diff_photo.jpg"'` in the root directory to convert the two images in parallel.

### Dev environment

requires Java JDK 7 for `java.nio`, get it [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
