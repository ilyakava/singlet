package local

import akka.actor._

object Local extends App {
  override def main(args: Array[String]) = {
    val system = ActorSystem("LocalSystem")
    val localMaster = system.actorOf(Props( new LocalMaster ), name = "LocalMaster")
    localMaster ! args
  }
}

class LocalMaster extends Actor {
  // create the remote actor
  val remoteMasters = List(
    context.actorFor("akka.tcp://RemoteSystem@127.0.0.1:5150/user/RemoteMaster"),
    context.actorFor("akka.tcp://RemoteSystem@127.0.0.1:5151/user/RemoteMaster2")
  )

  def receive = {
    case paths: Array[String] =>
      // split up among remoteMasters in the future
      val groupedPaths = groups(paths, remoteMasters.length)
      for (i <- 0 to (remoteMasters.length - 1)) {
        remoteMasters(i) ! groupedPaths(i)
      }
    case msg: String =>
      println(s"LocalMaster received message: '$msg'")
  }

  // splits an array into as equal groups as possible
  def groups[A](as: Array[A], n: Int) = {
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


