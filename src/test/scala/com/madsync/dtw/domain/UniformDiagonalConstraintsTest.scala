package com.madsync.dtw.domain

import com.madsync.time.DateTime
import org.scalatest.{ FunSuite, Matchers }

class UniformDiagonalConstraintsTest extends FunSuite with Matchers {
  val now = DateTime.now
  def vectorify(s: Seq[Int]): Seq[TimeSeriesElement[VectorValue[Int]]] = s.map(v => TimeSeriesElement(now.plusMinutes(v), Some(VectorValue(v))))

  test("show constrain depends on indices of MatrixEntrys") {
    UniformDiagonalConstraints(2).constrain(Seq(MatrixEntry(3 -> 4, 1), MatrixEntry(3 -> 5, 1), MatrixEntry(3 -> 6, 1)), 3) should be(Seq(MatrixEntry(3 -> 4, 1), MatrixEntry(3 -> 5, 1)))
  }

  test("show mask depends on indices of raw arrays") {
    println(UniformDiagonalConstraints().columnRange(0, 9))
    UniformDiagonalConstraints().mask(vectorify(0 to 9), 0).map(_.v.get.head) should be(0 to 3)
    UniformDiagonalConstraints().mask(vectorify(0 to 9), 1).map(_.v.get.head) should be(0 to 4)
    UniformDiagonalConstraints().mask(vectorify(0 to 9), 2).map(_.v.get.head) should be(0 to 5)
    UniformDiagonalConstraints().mask(vectorify(0 to 9), 3).map(_.v.get.head) should be(0 to 6)
    UniformDiagonalConstraints().mask(vectorify(0 to 9), 4).map(_.v.get.head) should be(1 to 7)
    UniformDiagonalConstraints().mask(vectorify(0 to 9), 5).map(_.v.get.head) should be(2 to 8)
    UniformDiagonalConstraints().mask(vectorify(0 to 9), 9).map(_.v.get.head) should be(6 to 9)
  }

  test("show column range works as expected") {
    UniformDiagonalConstraints(1).columnRange(0, 4) should be(0 -> 1)
    UniformDiagonalConstraints(1).columnRange(1, 4) should be(0 -> 2)
    UniformDiagonalConstraints(1).columnRange(2, 4) should be(1 -> 3)
    UniformDiagonalConstraints(1).columnRange(3, 4) should be(2 -> 4)
    UniformDiagonalConstraints(1).columnRange(4, 4) should be(3 -> 4)
  }

}
