package com.madsync.dtw.domain

import com.madsync.time.DateTime
import org.scalatest.{ FunSuite, Matchers }

class HammingSpaceTest extends FunSuite with Matchers {

  val now = DateTime.now()

  test("show metric counts the number of different positions") {
    HammingSpace.metric("abcd", "abcd") should be(0)
    HammingSpace.metric("abdd", "abcd") should be(1)
    HammingSpace.metric("abdc", "abcd") should be(2)

    HammingSpace.metric("dcba", "abcd") should be(4)
  }

  test("show coarsen concatenates all values in a coarsened region") {

    HammingSpace.coarsen(2).get(
      Seq(
        TimeSeriesElement(now, Some("ab")),
        TimeSeriesElement(now, Some("cd")),
        TimeSeriesElement(now, Some("zq")),
        TimeSeriesElement(now, Some("pfr")),
        TimeSeriesElement(now, Some("ab")))) should be(Seq(
        TimeSeriesElement(now, Some("abcd")),
        TimeSeriesElement(now, Some("zqpfr")),
        TimeSeriesElement(now, Some("ab"))))

  }

  test("show missing values in distance yield 0") {
    HammingSpace.distance(Some("abcd"), None) should be(0)
    HammingSpace.distance(None, Some("abcd")) should be(0)
    HammingSpace.distance(None, None) should be(0)
  }
}
