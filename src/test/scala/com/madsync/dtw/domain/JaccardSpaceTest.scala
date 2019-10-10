package com.madsync.dtw.domain

import com.madsync.time.DateTime
import org.scalatest.{ FunSuite, Matchers }

class JaccardSpaceTest extends FunSuite with Matchers {
  val now = DateTime.now()
  test("test coarsen") {
    JaccardSpace.coarsen(2) should be(None)
  }

  test("test metric") {
    val a = "01256"
    val b = "0234579"

    JaccardSpace.metric(a, b) should be(1D / 3 +- 1E-6)
  }

  test("show missing values in distance yield 0") {
    JaccardSpace.distance(Some("abcd"), None) should be(0)
    JaccardSpace.distance(None, Some("abcd")) should be(0)
    JaccardSpace.distance(None, None) should be(0)
  }

}
