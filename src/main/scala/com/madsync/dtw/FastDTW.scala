package com.madsync.dtw

import com.madsync.dtw.domain.{ CostMatrix, RangeDiagonalConstraints, Space, TimeSeriesElement }

//import scala.annotation.tailrec

/**
 * An implementation of DTW that recursively evaluates a costmatrix from low resolution to high,
 * searching only within the results of the next highest resolution for an optimal path
 *
 * @param searchRadius
 * @param space
 * @tparam T
 */
class FastDTW[T](searchRadius: Int, space: Space[T]) {

  lazy val requiredSearchRaadius: Int = math.max(0, searchRadius)
  lazy val minTsSize: Int = requiredSearchRaadius + 2
  lazy val dtw: DTW[T] = new DTW(space)

  /**
   * Performs a FastDTW on two time series if the coarsen function exists on its space and the two sequences are larger
   * than the minimum search radius. Otherwise, defaults to DTW.
   *
   * @param left
   * @param right
   * @return
   */
  //@tailrec
  final def evaluate(left: Seq[TimeSeriesElement[T]], right: Seq[TimeSeriesElement[T]]): CostMatrix =
    (space.coarsen(left.length / 2), space.coarsen(right.length / 2), left.size > minTsSize && right.size > minTsSize) match {
      case (Some(coarsenLeft), Some(coarsenRight), true) =>
        val newLeft = coarsenLeft(left)
        val newRight = coarsenRight(right)
        dtw.costMatrix(left, right, RangeDiagonalConstraints.fromCostMatrix(evaluate(newLeft, newRight)))
      case _ => dtw.costMatrix(left, right)
    }
}
