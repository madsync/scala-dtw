# scala-dtw

[![Build Status](https://travis-ci.org/madsync/scala-dtw.svg?branch=master)](https://travis-ci.org/madsync/scala-dtw.svg?branch=master)
[![Coverage Status](https://coveralls.io/repos/github/madsync/scala-dtw/badge.svg)](https://coveralls.io/github/madsync/scala-dtw)

An implementation of the Dynamic Time Warping algorithm accepting an arbitrary sub-metric, done in idiomatic Scala

## DTW Algorithm

The Dynamic Time Warping algorithm is a method for mapping two sequences to one another in a more or less optimal manner. 
It will compare sequences of the same or differing length to find the matchup at which the distance between sequences is
minimized.

It does this by computing a cost matrix, which is NxM for two sequences of length N and M, and contains at any location
the sum of all the distances between elements over the path that leads to that cell. Note that this indicates the computational
complexity of the calculation is O(NM), which can be expensive in some applications (eg. DNA sequence traversals).

The nature of the distance being considered between any two elements is governed by a sub-metric or notion of a distance.
For the special case of a standard Euclidean distance, it is possible to improve computational complexity to an O(N)
computation through a method called FastDTW. FastDTW is also supported in this library.

###Fast DTW

FastDTW is a method of improving DTW calculation speed by computing a coarse-grained envelope to place bounds on the possible 
paths an optimal seqence matchup might take. It can be shown that the complexity of this method is O(N) on the grounds that
the higher level computation restricts considered cells to a neighborhood around the diagonal, traversal of which is O(N).

FastDTW is not perfectly accurate in the event a window iis introduced to limit its ability to traverse the cost matrix.

FastDTW may only be applied to sub-metrics that respect a renormalization symmetry, behaving qualitatively at coarsened
scales as they do at finer ones. Currently the only sub-metric supported that obeys this symmetry is
the Euclidean one.

## Usage

At present this library must be built as I haven't gotten around to adding it to Maven Central (TBD).

Here is an example usage from one of the tests, representing two sequences that had a nonzero optimal distance 
between them:

```scala
    val s1: Seq[Double] = Seq(1, 2, 3, 4, 4, 3, 4, 2, 1, 1)
    val s2: Seq[Double] = Seq(1, 2, 3, 3, 4, 3, 3, 2, 2, 1)

    val left = s1.map(v => TimeSeriesElement(now, Some(VectorValue(v))))
    val right = s2.map(v => TimeSeriesElement(now, Some(VectorValue(v))))

    val matrix = new DTW(EuclideanSpace).costMatrix(left, right)

    matrix.optimalCost should be(1D)
    matrix.optimalPath should be(Seq(
      0 -> 0,
      1 -> 1,
      2 -> 2,
      2 -> 3,
      3 -> 4,
      4 -> 4,
      5 -> 5,
      6 -> 6,
      7 -> 7,
      7 -> 8,
      8 -> 9,
      9 -> 9))
```

Usage for FastDTW is similar
```scala
    val dtw = new DTW(EuclideanSpace)
    val fdtw = new FastDTW(1, EuclideanSpace)

    val s1: Seq[Double] = Seq(1, 2, 3, 4, 4, 3, 4, 2, 1, 1)
    val s2: Seq[Double] = Seq(1, 2, 3, 3, 4, 3, 3, 2, 2, 1)
    
    val left = s1.map(v => TimeSeriesElement(now, Some(VectorValue(v))))
    val right = s2.map(v => TimeSeriesElement(now, Some(VectorValue(v))))

    val path = fdtw.evaluate(left, right).optimalPath
```

### Some Caveats

This library was written in idiomatic scala, ie. functional scala. As such it will incur significant performance penalties by the nature 
of immutable operations (the cost of creating and allocating memory for new objects per iteration). These penalties as compared 
with Java primitive-based implementations result in a factor of ~10x.

It is important to note, however, that writing a generalized library using Java primitives would end up comprising a great deal of code,
since only objects are supported in generic implementations in the JVM (Java boxed types come with a significant performance 
penalty as well).

Thus, for performance reasons, it would likely be a good idea to use a more traditional, side-effected approach in the core logic.
The advantage of such an approach would be that one ends up with a generic-compatible implementation only 10% slower than
using arrays of Java primitives (per independent testing) but with far cleaner code. 
