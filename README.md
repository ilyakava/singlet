## Singlet

An image converting utility.

### Performance

```
Running 5 20MB images serially (version in the 2nd commit: e2b7af8)
real0m19.595s
user0m14.147s
sys0m0.508s
```

Same test with current parallel akka version:
```
real0m9.249s
user0m18.619s
sys0m0.734s
```

### Usage

run `sbt 'run "/Users/me/Pictures/photo.jpg" "/Users/me/Pictures/diff_photo.jpg"'` in the root directory

To convert the two images in parallel.

### Dev environment
requires Java JDK 7 for `java.nio`, get it [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
