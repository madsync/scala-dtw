package com.madsync.dtw

import com.madsync.dtw.domain.Direction.{ South, Southwest, West }
import com.madsync.dtw.domain.{ CostMatrix, MatrixEntry, TimeSeriesElement, VectorValue }
import com.madsync.dtw.domain.{ CostMatrix, EuclideanSpace, MatrixEntry, TimeSeriesElement, VectorValue }
import com.madsync.time.DateTime
import org.scalatest.{ FunSuite, Matchers }

class CostMatrixTest extends FunSuite with Matchers {

  test("test reduce prunes as expected") {

    //1 4 2 1
    //5 1 0 5
    //3 3 6 3
    //1 2 8 1
    val m = CostMatrix.fromValues(
      IndexedSeq(1D, 3D, 5D, 1D),
      IndexedSeq(2D, 3D, 1D, 4D),
      IndexedSeq(8D, 6D, 0D, 2D),
      IndexedSeq(1D, 3D, 5D, 1D))

    //5 1 0
    //3 3 6
    //1 2 8
    m.reduce(Southwest) should be(CostMatrix.fromValues(
      IndexedSeq(1D, 3D, 5D),
      IndexedSeq(2D, 3D, 1D),
      IndexedSeq(8D, 6D, 0D)))

    //5 1
    //3 3
    //1 2
    m.reduce(Southwest).reduce(West) should be(CostMatrix.fromValues(
      IndexedSeq(1D, 3D, 5D),
      IndexedSeq(2D, 3D, 1D)))

    //3 3
    //1 2
    m.reduce(Southwest).reduce(West).reduce(South) should be(CostMatrix.fromValues(
      IndexedSeq(1D, 3D),
      IndexedSeq(2D, 3D)))
  }

  test("test optimal path finds simple diagonal correctly") {
    val now = DateTime.now

    val left = Seq(
      TimeSeriesElement(now, Some(VectorValue(1D, 2D, 3D))),
      TimeSeriesElement(now.plus(1000), Some(VectorValue(4D, 5D, 6D))),
      TimeSeriesElement(now.plus(2000), Some(VectorValue(7D, 8D, 9D))))

    val v1 = EuclideanSpace.distance(left.head.v, left.drop(1).head.v)
    val v2 = EuclideanSpace.distance(left.head.v, left.drop(2).head.v) + v1

    val matrix = CostMatrix.fromValues(Seq(Seq(0D, v1, v2), Seq(v1, 0D, v1), Seq(v2, v1, 0D)): _*)
    matrix.optimalPath should be(Seq(0 -> 0, 1 -> 1, 2 -> 2))
    matrix.optimalCost should be(0)

    matrix.length should be(3)
    matrix(0).map(_.value) should be(Seq(BigDecimal(0), v1, v2))
    matrix(1).map(_.value) should be(Seq(v1, BigDecimal(0), v1))
    matrix(2).map(_.value) should be(Seq(v2, v1, BigDecimal(0)))
  }

  test("show reduce handles constrained matrices well") {
    val m = CostMatrix(
      Seq(MatrixEntry(0 -> 0, 0), MatrixEntry(0 -> 1, 1), MatrixEntry(0 -> 2, 2)),
      Seq(MatrixEntry(1 -> 0, 1), MatrixEntry(1 -> 1, 0), MatrixEntry(1 -> 2, 0), MatrixEntry(1 -> 3, 1)),
      Seq(MatrixEntry(2 -> 2, 1), MatrixEntry(2 -> 3, 0), MatrixEntry(2 -> 4, 1)),
      Seq(MatrixEntry(3 -> 3, 1), MatrixEntry(3 -> 4, 0)))

    m.reduce(Southwest) should be(CostMatrix(
      Seq(MatrixEntry(0 -> 0, 0), MatrixEntry(0 -> 1, 1), MatrixEntry(0 -> 2, 2)),
      Seq(MatrixEntry(1 -> 0, 1), MatrixEntry(1 -> 1, 0), MatrixEntry(1 -> 2, 0), MatrixEntry(1 -> 3, 1)),
      Seq(MatrixEntry(2 -> 2, 1), MatrixEntry(2 -> 3, 0))))

    m.reduce(West) should be(CostMatrix(
      Seq(MatrixEntry(0 -> 0, 0), MatrixEntry(0 -> 1, 1), MatrixEntry(0 -> 2, 2)),
      Seq(MatrixEntry(1 -> 0, 1), MatrixEntry(1 -> 1, 0), MatrixEntry(1 -> 2, 0), MatrixEntry(1 -> 3, 1)),
      Seq(MatrixEntry(2 -> 2, 1), MatrixEntry(2 -> 3, 0), MatrixEntry(2 -> 4, 1))))

    m.reduce(South) should be(CostMatrix(
      Seq(MatrixEntry(0 -> 0, 0), MatrixEntry(0 -> 1, 1), MatrixEntry(0 -> 2, 2)),
      Seq(MatrixEntry(1 -> 0, 1), MatrixEntry(1 -> 1, 0), MatrixEntry(1 -> 2, 0), MatrixEntry(1 -> 3, 1)),
      Seq(MatrixEntry(2 -> 2, 1), MatrixEntry(2 -> 3, 0)),
      Seq(MatrixEntry(3 -> 3, 1))))
  }

}
