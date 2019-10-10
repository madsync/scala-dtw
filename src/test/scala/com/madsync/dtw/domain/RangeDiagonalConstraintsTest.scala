package com.madsync.dtw.domain

import com.madsync.dtw.DTW
import com.madsync.time.DateTime
import org.scalatest.{FunSuite, Matchers}

class RangeDiagonalConstraintsTest extends FunSuite with Matchers {

  test("show simplest cost matrix generalizes") {
    /*

    0 1
    1 0

    should provide

    x x 1 1
    x 1 1 1
    1 1 1 x
    1 1 x x

     */

    val cm1 = CostMatrix(
      Seq(MatrixEntry(0 -> 0, 0), MatrixEntry(0 -> 1, 1)),
      Seq(MatrixEntry(1 -> 0, 1), MatrixEntry(1 -> 1, 0))
    )

    RangeDiagonalConstraints.fromCostMatrix(cm1) should be(RangeDiagonalConstraints(
      Map(
        0 -> (0 -> 1),
        1 -> (0 -> 2),
        2 -> (1 -> 3),
        3 -> (2 -> 3)
      )
    ))

  }

  test("show from cost matrix correctly populates vertically off diagonal matrices") {
    val now = DateTime.now()
    val s1 = Seq(1D, 2D, 3D, 4D)
    val s2 = Seq(1D, 2D, 2D, 3D, 4D)

    val left = s1.map(v => TimeSeriesElement(now, Some(VectorValue(v))))
    val right = s2.map(v => TimeSeriesElement(now, Some(VectorValue(v))))

    val matrix = new DTW(EuclideanSpace).costMatrix(left, right, RangeDiagonalConstraints(
      Map(0 -> (0 -> 2), 1 -> (0, 3), 2 -> (2 -> 4), 3 -> (3 -> 4))
    ))

    /*
    , RangeDiagonalConstraints(
      Map(0 -> (0 -> 3), 1 -> (0, 3), 2 -> (2 -> 4), 3 -> (3 -> 4))
    )
     */
    matrix.foreach(println)

    //x x 1 0
    //x 1 0 1
    //2 0 1 x
    //1 0 x x
    //0 1 x x

    matrix should be(CostMatrix(
      Seq(MatrixEntry(0 -> 0, 0), MatrixEntry(0 -> 1, 1), MatrixEntry(0 -> 2, 2)),
      Seq(MatrixEntry(1 -> 0, 1), MatrixEntry(1 -> 1, 0), MatrixEntry(1 -> 2, 0), MatrixEntry(1 -> 3, 1)),
      Seq(                                                MatrixEntry(2 -> 2, 1), MatrixEntry(2 -> 3, 0), MatrixEntry(2 -> 4, 1)),
      Seq(                                                                        MatrixEntry(3 -> 3, 1), MatrixEntry(3 -> 4, 0))
    ))


    matrix.optimalPath should be(Seq(
      0 -> 0, 1 -> 1, 1 -> 2, 2 -> 3, 3 -> 4
    ))


    //x x x 1
    //x x 1 x
    //x 1 x x
    //x 1 x x
    //1 x x x

    /*

    //x x x x x x 1 1  9
      x x x x x 1 1 1  8
      x x x x 1 1 1 x  7
      x x x 1 1 1 x x  6
      x x 1 1 1 x x x  5
      x x 1 1 x x x x  4
      x x 1 1 x x x x  3
      x 1 1 1 x x x x  2
      1 1 1 x x x x x  1
      1 1 x x x x x x  0

      0 1 2 3 4 5 6 7

     */

    RangeDiagonalConstraints.fromCostMatrix(matrix) should be(RangeDiagonalConstraints(
      Map(
        0 -> (0 -> 1),
        1 -> (0 -> 2),
        2 -> (1 -> 5),
        3 -> (2 -> 6),
        4 -> (5 -> 7),
        5 -> (6 -> 8),
        6 -> (7 -> 9),
        7 -> (8 -> 9),
      )
    ))

  }

  test("show from cost matrix correctly populates horizontally off diagonal matrices") {
    val now = DateTime.now()
    val s1 = Seq(1D, 2D, 2D, 3D, 4D)
    val s2 = Seq(1D, 2D, 3D, 4D)

    val left = s1.map(v => TimeSeriesElement(now, Some(VectorValue(v))))
    val right = s2.map(v => TimeSeriesElement(now, Some(VectorValue(v))))

    val matrix = new DTW(EuclideanSpace).costMatrix(left, right, RangeDiagonalConstraints(
         Map(0 -> (0 -> 1), 1 -> (0 -> 2), 2 -> (0 -> 2), 3 -> (1 -> 3), 4 -> (2 -> 3))
    ))

    matrix.foreach(println)

/*

x x x 1 0
x 1 1 0 1
1 0 0 1 x
0 1 2 x x

 */

    matrix should be(CostMatrix(
      Seq(MatrixEntry(0 -> 0, 0), MatrixEntry(0 -> 1, 1)),
      Seq(MatrixEntry(1 -> 0, 1), MatrixEntry(1 -> 1, 0), MatrixEntry(1 -> 2, 1)),
      Seq(MatrixEntry(2 -> 0, 2), MatrixEntry(2 -> 1, 0), MatrixEntry(2 -> 2, 1)),
      Seq(                        MatrixEntry(3 -> 1, 1), MatrixEntry(3 -> 2, 0), MatrixEntry(3 -> 3, 1)),
      Seq(                                                MatrixEntry(4 -> 2, 1), MatrixEntry(4 -> 3, 0))
    ))
    matrix.optimalPath.foreach(println)

    matrix.optimalPath should be(Seq(
      0 -> 0, 1 -> 1, 2 -> 1, 3 -> 2, 4 -> 3
    ))

    /*

x x x x 0
x x x 0 x
x 0 0 x x
0 x x x x

x x x x x x x x 0 0
x x x x x x x 0 0 0
x x x x x x 0 0 0 x
x x x x x 0 0 0 x x
x x 0 0 0 0 0 x x x
x 0 0 0 0 0 x x x x
0 0 0 x x x x x x x
0 0 x x x x x x x x

0 1 2 3 4 5 6 7 8 9
 */

    RangeDiagonalConstraints.fromCostMatrix(matrix) should be(RangeDiagonalConstraints(
      Map(
        0 -> (0 -> 1),
        1 -> (0 -> 2),
        2 -> (1 -> 3),
        3 -> (2 -> 3),
        4 -> (2 -> 3),
        5 -> (2 -> 4),
        6 -> (3 -> 5),
        7 -> (4 -> 6),
        8 -> (5 -> 7),
        9 -> (6 -> 7)
      )
    ))

  }
}
