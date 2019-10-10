package com.madsync.dtw

import com.madsync.dtw.domain._
import com.madsync.dtw.domain.{ CostMatrix, EuclideanSpace, MatrixEntry, RangeDiagonalConstraints, TimeSeriesElement, UniformDiagonalConstraints, VectorValue }
import com.madsync.time.DateTime
import org.scalatest.Matchers

class DTWTest extends org.scalatest.FunSuite with Matchers {
  val now = DateTime.now
  def vectorify(s: Seq[Int]): Seq[TimeSeriesElement[VectorValue[Double]]] = s.map(v => TimeSeriesElement(now.plusMinutes(v), Some(VectorValue(v.toDouble))))

  test("test euclid sanity check") {
    val tv = VectorValue[Double](1, 2, 3)
    EuclideanSpace.metric(tv, tv) should be(BigDecimal(0))
  }

  test("show cost matrix handles empties gracefully") {
    new DTW(EuclideanSpace).costMatrix(Seq(), Seq()) should be(CostMatrix())
    new DTW(EuclideanSpace).costMatrix(Seq(), Seq()).optimalPath should be(Seq())
    new DTW(EuclideanSpace).costMatrix(Seq(), Seq()).optimalCost should be(0)
  }

  test("show cost matrix is diagonal for same vector") {
    val left: Seq[TimeSeriesElement[VectorValue[Double]]] = Seq(
      TimeSeriesElement(now, Some(VectorValue(1, 2, 3))),
      TimeSeriesElement(now.plus(1000), Some(VectorValue(4, 5, 6))),
      TimeSeriesElement(now.plus(2000), Some(VectorValue(7, 8, 9))))
    val right: Seq[TimeSeriesElement[VectorValue[Double]]] = left

    val v1 = EuclideanSpace.distance(left.head.v, left.drop(1).head.v)
    val v2 = EuclideanSpace.distance(left.head.v, left.drop(2).head.v) + v1

    new DTW(EuclideanSpace).costMatrix(left, right) should be(CostMatrix.fromValues(Seq(Seq(0D, v1, v2), Seq(v1, 0D, v1), Seq(v2, v1, 0D)): _*))
  }

  test("test asString") {
    CostMatrix.fromValues(Seq(Seq(0D, 3D, 5D), Seq(2D, 0D, 8D), Seq(4D, 6D, 0D)): _*).asString should be(
      """5 8 0
        |3 0 6
        |0 2 4""".stripMargin)
  }

  test("show cost matrix and optimal path are constructed properly for warped profile") {

    val s1: Seq[Double] = Seq(1, 2, 3, 4, 4, 3, 4, 2, 1, 1)
    val s2: Seq[Double] = Seq(1, 2, 3, 3, 4, 3, 4, 2, 2, 1)

    val left = s1.map(v => TimeSeriesElement(now, Some(VectorValue(v))))
    val right = s2.map(v => TimeSeriesElement(now, Some(VectorValue(v))))

    val matrix = new DTW(EuclideanSpace).costMatrix(left, right)
    println("Matrix was:")
    println(matrix.asString)

    matrix(0, 0).value should be(0)
    matrix(s1.length - 1, s2.length - 1).value should be(0)

    println("Path was:")
    matrix.optimalPath.map(println)
    /*
The optimalPath should be the 0s connected across this matrix

15 8 6 7 8 5 6 1 0 0
15 7 4 5 5 3 4 0 1 2
14 7 3 3 3 2 2 0 1 2
13 7 2 1 1 1 0 2 5 7
10 5 1 1 1 0 1 2 4 6
8  4 1 0 0 1 1 3 6 9
5  2 0 1 2 2 3 4 6 8
3  1 0 1 2 2 3 4 6 8
1  0 1 3 5 6 8 8 9 10
0  1 3 6 9 11 14 15 15 15
     */
    matrix.optimalCost should be(0D)
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
  }

  test("show nonzero cost paths work") {
    val s1: Seq[Double] = Seq(1, 2, 3, 4, 4, 3, 4, 2, 1, 1)
    val s2: Seq[Double] = Seq(1, 2, 3, 3, 4, 3, 3, 2, 2, 1)

    val left = s1.map(v => TimeSeriesElement(now, Some(VectorValue(v))))
    val right = s2.map(v => TimeSeriesElement(now, Some(VectorValue(v))))

    val matrix = new DTW(EuclideanSpace).costMatrix(left, right)
    println("Matrix was:")
    println(matrix.asString)
    println("Path was:")
    matrix.optimalPath.map(println)

    /*
14 7 5 6 7 4 5 2 1 1
14 6 3 4 5 2 3 1 2 3
13 6 2 3 4 1 2 1 2 3
12 6 1 2 2 0 1 2 4 6
10 5 1 1 1 0 1 2 4 6
8  4 1 0 0 1 1 3 6 9
5  2 0 1 2 2 3 4 6 8
3  1 0 1 2 2 3 4 6 8
1  0 1 3 5 6 8 8 9 10
0  1 3 6 9 11 14 15 15 15
     */
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
  }

  test("show unequal length paths work") {
    val s1: Seq[Double] = Seq(1, 2, 3, 4, 4, 3, 4, 2, 1, 1, 3, 1)
    val s2: Seq[Double] = Seq(1, 2, 3, 3, 4, 3, 3, 2, 2, 1)

    val left = s1.map(v => TimeSeriesElement(now, Some(VectorValue(v))))
    val right = s2.map(v => TimeSeriesElement(now, Some(VectorValue(v))))

    val matrix = new DTW(EuclideanSpace).costMatrix(left, right)
    println("Matrix was:")
    println(matrix.asString)
    println("Path was:")
    matrix.optimalPath.map(println)

    /*
14 7 5 6 7 4 5 2 1 1 3 3
14 6 3 4 5 2 3 1 2 3 4 5
13 6 2 3 4 1 2 1 2 3 4 5
12 6 1 2 2 0 1 2 4 6 6 8
10 5 1 1 1 0 1 2 4 6 6 8
8 4 1 0 0 1 1 3 6 9 9 11
5 2 0 1 2 2 3 4 6 8 8 10
3 1 0 1 2 2 3 4 6 8 8 10
1 0 1 3 5 6 8 8 9 10 11 12
0 1 3 6 9 11 14 15 15 15 17 17
     */
    matrix.optimalCost should be(3D)
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
      9 -> 9,
      10 -> 9,
      11 -> 9))
  }

  test("show restrictions are obeyed for square matrices") {

    val matrix31 = new DTW(EuclideanSpace).costMatrix(vectorify(Seq(0, 1, 2)), vectorify(Seq(0, 1, 2)), UniformDiagonalConstraints(1))
    println(matrix31.asString)
    matrix31 should be(CostMatrix(
      Seq(MatrixEntry(0 -> 0, 0), MatrixEntry(0 -> 1, 1)),
      Seq(MatrixEntry(1 -> 0, 1), MatrixEntry(1 -> 1, 0), MatrixEntry(1 -> 2, 1)),
      Seq(MatrixEntry(2 -> 1, 1), MatrixEntry(2 -> 2, 0))))

    val matrix51 = new DTW(EuclideanSpace).costMatrix(vectorify(0 to 4), vectorify(0 to 4), UniformDiagonalConstraints(1))
    println(matrix51.asString)
    matrix51 should be(CostMatrix(
      Seq(MatrixEntry(0 -> 0, 0), MatrixEntry(0 -> 1, 1)),
      Seq(MatrixEntry(1 -> 0, 1), MatrixEntry(1 -> 1, 0), MatrixEntry(1 -> 2, 1)),
      Seq(MatrixEntry(2 -> 1, 1), MatrixEntry(2 -> 2, 0), MatrixEntry(2 -> 3, 1)),
      Seq(MatrixEntry(3 -> 2, 1), MatrixEntry(3 -> 3, 0), MatrixEntry(3 -> 4, 1)),
      Seq(MatrixEntry(4 -> 3, 1), MatrixEntry(4 -> 4, 0))))

    val matrix52 = new DTW(EuclideanSpace).costMatrix(vectorify(0 to 4), vectorify(0 to 4), UniformDiagonalConstraints(2))
    println(matrix52.asString)
    matrix52 should be(CostMatrix(
      Seq(MatrixEntry(0 -> 0, 0), MatrixEntry(0 -> 1, 1), MatrixEntry(0 -> 2, 3)),
      Seq(MatrixEntry(1 -> 0, 1), MatrixEntry(1 -> 1, 0), MatrixEntry(1 -> 2, 1), MatrixEntry(1 -> 3, 3)),
      Seq(MatrixEntry(2 -> 0, 3), MatrixEntry(2 -> 1, 1), MatrixEntry(2 -> 2, 0), MatrixEntry(2 -> 3, 1), MatrixEntry(2 -> 4, 3)),
      Seq(MatrixEntry(3 -> 1, 3), MatrixEntry(3 -> 2, 1), MatrixEntry(3 -> 3, 0), MatrixEntry(3 -> 4, 1)),
      Seq(MatrixEntry(4 -> 2, 3), MatrixEntry(4 -> 3, 1), MatrixEntry(4 -> 4, 0))))
  }

  test("show restrictions are obeyed for nonsquare matrices") {

    var matrix31 = new DTW(EuclideanSpace).costMatrix(vectorify(Seq(0, 1, 2)), vectorify(Seq(0, 1, 2, 3)), UniformDiagonalConstraints(1))
    println(matrix31.asString)
    matrix31 should be(CostMatrix(
      Seq(MatrixEntry(0 -> 0, 0), MatrixEntry(0 -> 1, 1)),
      Seq(MatrixEntry(1 -> 0, 1), MatrixEntry(1 -> 1, 0), MatrixEntry(1 -> 2, 1)),
      Seq(MatrixEntry(2 -> 1, 1), MatrixEntry(2 -> 2, 0), MatrixEntry(2 -> 3, 1))))

    matrix31 = new DTW(EuclideanSpace).costMatrix(vectorify(Seq(0, 1, 2, 3)), vectorify(Seq(0, 1, 2)), UniformDiagonalConstraints(1))
    println(matrix31.asString)
    matrix31 should be(CostMatrix(
      Seq(MatrixEntry(0 -> 0, 0), MatrixEntry(0 -> 1, 1)),
      Seq(MatrixEntry(1 -> 0, 1), MatrixEntry(1 -> 1, 0), MatrixEntry(1 -> 2, 1)),
      Seq(MatrixEntry(2 -> 1, 1), MatrixEntry(2 -> 2, 0)),
      Seq(MatrixEntry(3 -> 2, 1))))

    var matrix51 = new DTW(EuclideanSpace).costMatrix(vectorify(0 to 4), vectorify(0 to 5), UniformDiagonalConstraints(1))
    println(matrix51.asString)
    matrix51 should be(CostMatrix(
      Seq(MatrixEntry(0 -> 0, 0), MatrixEntry(0 -> 1, 1)),
      Seq(MatrixEntry(1 -> 0, 1), MatrixEntry(1 -> 1, 0), MatrixEntry(1 -> 2, 1)),
      Seq(MatrixEntry(2 -> 1, 1), MatrixEntry(2 -> 2, 0), MatrixEntry(2 -> 3, 1)),
      Seq(MatrixEntry(3 -> 2, 1), MatrixEntry(3 -> 3, 0), MatrixEntry(3 -> 4, 1)),
      Seq(MatrixEntry(4 -> 3, 1), MatrixEntry(4 -> 4, 0), MatrixEntry(4 -> 5, 1))))

    matrix51 = new DTW(EuclideanSpace).costMatrix(vectorify(0 to 5), vectorify(0 to 4), UniformDiagonalConstraints(1))
    println(matrix51.asString)
    matrix51 should be(CostMatrix(
      Seq(MatrixEntry(0 -> 0, 0), MatrixEntry(0 -> 1, 1)),
      Seq(MatrixEntry(1 -> 0, 1), MatrixEntry(1 -> 1, 0), MatrixEntry(1 -> 2, 1)),
      Seq(MatrixEntry(2 -> 1, 1), MatrixEntry(2 -> 2, 0), MatrixEntry(2 -> 3, 1)),
      Seq(MatrixEntry(3 -> 2, 1), MatrixEntry(3 -> 3, 0), MatrixEntry(3 -> 4, 1)),
      Seq(MatrixEntry(4 -> 3, 1), MatrixEntry(4 -> 4, 0)),
      Seq(MatrixEntry(5 -> 4, 1))))

    var matrix52 = new DTW(EuclideanSpace).costMatrix(vectorify(0 to 4), vectorify(0 to 5), UniformDiagonalConstraints(2))
    println(matrix52.asString)
    matrix52 should be(CostMatrix(
      Seq(MatrixEntry(0 -> 0, 0), MatrixEntry(0 -> 1, 1), MatrixEntry(0 -> 2, 3)),
      Seq(MatrixEntry(1 -> 0, 1), MatrixEntry(1 -> 1, 0), MatrixEntry(1 -> 2, 1), MatrixEntry(1 -> 3, 3)),
      Seq(MatrixEntry(2 -> 0, 3), MatrixEntry(2 -> 1, 1), MatrixEntry(2 -> 2, 0), MatrixEntry(2 -> 3, 1), MatrixEntry(2 -> 4, 3)),
      Seq(MatrixEntry(3 -> 1, 3), MatrixEntry(3 -> 2, 1), MatrixEntry(3 -> 3, 0), MatrixEntry(3 -> 4, 1), MatrixEntry(3 -> 5, 3)),
      Seq(MatrixEntry(4 -> 2, 3), MatrixEntry(4 -> 3, 1), MatrixEntry(4 -> 4, 0), MatrixEntry(4 -> 5, 1))))

    matrix52 = new DTW(EuclideanSpace).costMatrix(vectorify(0 to 5), vectorify(0 to 4), UniformDiagonalConstraints(2))
    println(matrix52.asString)
    matrix52 should be(CostMatrix(
      Seq(MatrixEntry(0 -> 0, 0), MatrixEntry(0 -> 1, 1), MatrixEntry(0 -> 2, 3)),
      Seq(MatrixEntry(1 -> 0, 1), MatrixEntry(1 -> 1, 0), MatrixEntry(1 -> 2, 1), MatrixEntry(1 -> 3, 3)),
      Seq(MatrixEntry(2 -> 0, 3), MatrixEntry(2 -> 1, 1), MatrixEntry(2 -> 2, 0), MatrixEntry(2 -> 3, 1), MatrixEntry(2 -> 4, 3)),
      Seq(MatrixEntry(3 -> 1, 3), MatrixEntry(3 -> 2, 1), MatrixEntry(3 -> 3, 0), MatrixEntry(3 -> 4, 1)),
      Seq(MatrixEntry(4 -> 2, 3), MatrixEntry(4 -> 3, 1), MatrixEntry(4 -> 4, 0)),
      Seq(MatrixEntry(5 -> 3, 3), MatrixEntry(5 -> 4, 1))))
  }

  test("show cost matrix can calculate nondiagonal optimal path without constraints") {

    val s1: Seq[Double] = Seq(1, 2, 3, 4)
    val s2: Seq[Double] = Seq(1, 2, 2, 3, 4)

    val left = s1.map(v => TimeSeriesElement(now, Some(VectorValue(v))))
    val right = s2.map(v => TimeSeriesElement(now, Some(VectorValue(v))))

    val matrix = new DTW(EuclideanSpace).costMatrix(left, right)

    /*
    , RangeDiagonalConstraints(
      Map(0 -> (0 -> 2), 1 -> (0, 3), 2 -> (2 -> 4), 3 -> (3 -> 4))
    )
     */
    matrix.foreach(println)

    //7 3 1 0
    //4 1 0 1
    //2 0 1 3
    //1 0 1 3
    //0 1 3 6

    matrix should be(CostMatrix(
      Seq(MatrixEntry(0 -> 0, 0), MatrixEntry(0 -> 1, 1), MatrixEntry(0 -> 2, 2), MatrixEntry(0 -> 3, 4), MatrixEntry(0 -> 4, 7)),
      Seq(MatrixEntry(1 -> 0, 1), MatrixEntry(1 -> 1, 0), MatrixEntry(1 -> 2, 0), MatrixEntry(1 -> 3, 1), MatrixEntry(1 -> 4, 3)),
      Seq(MatrixEntry(2 -> 0, 3), MatrixEntry(2 -> 1, 1), MatrixEntry(2 -> 2, 1), MatrixEntry(2 -> 3, 0), MatrixEntry(2 -> 4, 1)),
      Seq(MatrixEntry(3 -> 0, 6), MatrixEntry(3 -> 1, 3), MatrixEntry(3 -> 2, 3), MatrixEntry(3 -> 3, 1), MatrixEntry(3 -> 4, 0))))

    matrix.optimalPath should be(Seq(
      0 -> 0, 1 -> 1, 1 -> 2, 2 -> 3, 3 -> 4))

  }

  test("show cost matrix can calculate nondiagonal optimal path with constraints") {
    val now = DateTime.now()
    val s1: Seq[Double] = Seq(1, 2, 3, 4)
    val s2: Seq[Double] = Seq(1, 2, 2, 3, 4)

    val left = s1.map(v => TimeSeriesElement(now, Some(VectorValue(v))))
    val right = s2.map(v => TimeSeriesElement(now, Some(VectorValue(v))))

    val matrix = new DTW(EuclideanSpace).costMatrix(left, right, RangeDiagonalConstraints(
      Map(0 -> (0 -> 1), 1 -> (0, 3), 2 -> (2 -> 4), 3 -> (3 -> 4))))

    /*
    , RangeDiagonalConstraints(
      Map(0 -> (0 -> 3), 1 -> (0, 3), 2 -> (2 -> 4), 3 -> (3 -> 4))
    )
     */
    matrix.foreach(println)

    //x x 1 0
    //x 1 0 1
    //x 0 1 x
    //1 0 x x
    //0 1 x x

    //    matrix should be(CostMatrix(
    //      Seq(MatrixEntry(0 -> 0, 0), MatrixEntry(0 -> 1, 1), MatrixEntry(0 -> 2, 2)),
    //      Seq(MatrixEntry(1 -> 0, 1), MatrixEntry(1 -> 1, 0), MatrixEntry(1 -> 2, 0), MatrixEntry(1 -> 3, 1)),
    //      Seq(                                                MatrixEntry(2 -> 2, 1), MatrixEntry(2 -> 3, 0), MatrixEntry(2 -> 4, 1)),
    //      Seq(                                                                        MatrixEntry(3 -> 3, 1), MatrixEntry(3 -> 4, 0))
    //    ))

    matrix.optimalPath should be(Seq(
      0 -> 0, 1 -> 1, 1 -> 2, 2 -> 3, 3 -> 4))
  }
}
