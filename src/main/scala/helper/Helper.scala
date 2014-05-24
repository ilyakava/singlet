package helper

object Helper {
  // splits an array into as equal groups as possible
  def inGroups[A](as: Array[A], n: Int) = {
    val rem = as.length % n
    val div = as.length / n
    def go(container: List[Array[A]], i: Int): List[Array[A]] = {
      if (i == 0) {
        val ii = (div + rem)
        go(List(as.slice(0, ii)), ii)
      } else if (i >= as.length) {
        container
      } else {
        val ii = i + div
        go(container ++ List(as.slice(i, ii)), ii)
      }
    }
    val empty: List[Array[A]] = List()
    go(empty, 0)
  }
}
