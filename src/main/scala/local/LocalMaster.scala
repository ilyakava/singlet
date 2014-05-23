package local

import akka.actor._
import com.typesafe.config.ConfigFactory

object Local extends App {
  override def main(args: Array[String]) = {
    // LocalMaster needs to know how many machines there are. It makes assumptions
    // on the names and ports of where those machines are based on the number
    val numRemoteMachines = args.head.toInt
    val akkaConfig = ConfigFactory.load.getConfig("localsystem")
    val system = ActorSystem("localsystem", akkaConfig)
    val localMaster = system.actorOf(Props( new LocalMaster(numRemoteMachines) ), name = "LocalMaster")
    localMaster ! args.tail
  }
}

class LocalMaster(numRemoteMachines: Int) extends Actor {
  // register the location of the remote machines
  val remoteMasters = makeRemotes(numRemoteMachines)

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

  // make an array of remote systems
  def makeRemotes(n: Int) = {
    def go(acc: Array[ActorRef], n: Int): Array[ActorRef] = {
      if (0 == n)
        acc
      else {
        val nextLocation = "akka.tcp://remotesystem@127.0.0.1:"
          .concat((5150 + n).toString)
          .concat("/user/RemoteMaster")
          .concat(n.toString)
        val next: Array[ActorRef] = Array(context.actorFor(nextLocation))
        go(next ++ acc, n - 1)
      }
    }
    val empty: Array[ActorRef] = Array()
    go(empty, n)
  }
}


