package local

import akka.actor._
import com.typesafe.config.ConfigFactory

import helper._

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
      val groupedPaths = Helper.inGroups(paths, numRemoteMachines)
      for (i <- 0 to (numRemoteMachines - 1)) {
        remoteMasters(i) ! groupedPaths(i)
      }
    case msg: String =>
      println(s"LocalMaster received message: '$msg'")
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


