package com.madsync.dtw

import com.madsync.dtw.domain._
import com.madsync.dtw.domain.{ EuclideanSpace, HammingSpace, JaccardSpace, TimeSeriesElement, VectorValue }
import com.madsync.time.DateTime
import org.scalatest.{ FunSuite, Matchers }

class FastDTWTest extends FunSuite with Matchers {

  val now = DateTime.now

  test("show FastDTW achieves same result as DTW for a Euclidean space") {
    val dtw = new DTW(EuclideanSpace)
    val fdtw = new FastDTW(1, EuclideanSpace)

    val s1: Seq[Double] = Seq(1, 2, 3, 4, 4, 3, 4, 2, 1, 1)
    val s2: Seq[Double] = Seq(1, 2, 3, 3, 4, 3, 3, 2, 2, 1)

    val left = s1.map(v => TimeSeriesElement(now, Some(VectorValue(v))))
    val right = s2.map(v => TimeSeriesElement(now, Some(VectorValue(v))))

    fdtw.evaluate(left, right).optimalPath should be(dtw.costMatrix(left, right).optimalPath)
    fdtw.evaluate(left, right).optimalCost should be(dtw.costMatrix(left, right).optimalCost)
  }

  test("show FastDTW achieves same result as DTW for Hamming space") {
    val dtw = new DTW(HammingSpace)
    val fdtw = new FastDTW(1, HammingSpace)

    val s1: Seq[String] = Seq("a", "b", "c", "d", "d", "c", "d", "b", "a", "a")
    val s2: Seq[String] = Seq("a", "b", "c", "c", "d", "c", "c", "b", "b", "a")

    val left = s1.map(v => TimeSeriesElement(now, Some(v)))
    val right = s2.map(v => TimeSeriesElement(now, Some(v)))

    fdtw.evaluate(left, right).optimalPath should be(dtw.costMatrix(left, right).optimalPath)
    fdtw.evaluate(left, right).optimalCost should be(dtw.costMatrix(left, right).optimalCost)
  }

  test("show FastDTW achieves same result as DTW for Jaccard space") {
    val dtw = new DTW(JaccardSpace)
    val fdtw = new FastDTW(1, JaccardSpace)

    val s1: Seq[String] = Seq("a", "b", "c", "d", "d", "c", "d", "b", "a", "a")
    val s2: Seq[String] = Seq("a", "b", "c", "c", "d", "c", "c", "b", "b", "a")

    val left = s1.map(v => TimeSeriesElement(now, Some(v)))
    val right = s2.map(v => TimeSeriesElement(now, Some(v)))

    fdtw.evaluate(left, right).optimalPath should be(dtw.costMatrix(left, right).optimalPath)
    fdtw.evaluate(left, right).optimalCost should be(dtw.costMatrix(left, right).optimalCost)
  }

}
