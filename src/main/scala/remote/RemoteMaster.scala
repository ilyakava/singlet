package remote

import org.gm4java.engine.GMConnection
import org.gm4java.engine.support.SimpleGMService
import org.slf4j._

import java.nio.file.Files

import akka.actor.{ActorSystem, ActorLogging, Actor, Props, ActorRef}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable.ArrayBuffer

// a request to resize an image
case class ImageResize(srcPath: String, maxLongSide: Int)
// a new image that has completed processing
case class AdditionalImage(newSrcPath: String)

class ImageProcessor extends Actor with ActorLogging {
  val service = new SimpleGMService().getConnection()

  def receive = {
    case ImageResize(srcPath: String, maxLongSide: Int) =>
      try {
        println("starting")
        val thumbnailPath = Files.createTempFile("thumbnail", ".jpg").toString

        service.execute("convert",
                   srcPath,
                   "-scale",
                   maxLongSide + "x" + maxLongSide,
                   thumbnailPath)

        sender ! AdditionalImage(thumbnailPath)
      } catch { case ex: Exception =>
        println(ex.getMessage())
          throw new IllegalStateException(ex.getMessage(), ex)
    }
  }
}

class RemoteMaster(system: ActorSystem, numWorkers: Int) extends Actor with ActorLogging {
  // 4-5 workers seems to be the limit on my machine for a single JVM (`java.lang.OutOfMemoryError: Java heap space` otherwise)
  val workers = makeWorkers(numWorkers, system)

  var imgQueue = ArrayBuffer[String]()

  def receive = {
    case msg: String =>
      println(s"RemoteMaster received message '$msg'")
    case imagePaths: Array[String] =>
      // The initialization case (LocalMaster sends array of image paths)
      val numImages = imagePaths.length.toString
      println(s"Remote Master is queuing '$numImages' images")
      for(path <- imagePaths) { imgQueue += path }

      // don't try to queue all workers if few images are given
      for(worker <- workers) {
        if (!imgQueue.isEmpty) {
          val path = imgQueue.remove(0)
          worker ! ImageResize(path, 200)
        }
      }
    case AdditionalImage(thumbnailPath: String) =>
      log.info("Thumbnail was saved to {}", thumbnailPath)
      println(thumbnailPath)
      if(imgQueue.isEmpty) {
        context.system.shutdown()
      } else {
        val path = imgQueue.remove(0)
        sender ! ImageResize(path, 200)
      }
  }

  def makeWorkers(n: Int, system: ActorSystem) = {
    def go(acc: Array[ActorRef], n: Int): Array[ActorRef] = {
      if (0 == n)
        acc
      else {
        val next: Array[ActorRef] = Array(system.actorOf(Props[ImageProcessor], "worker".concat(n.toString)))
        go(next ++ acc, n - 1)
      }
    }
    val empty: Array[ActorRef] = Array()
    go(empty, n)
  }
}

object Remote extends App {
  override def main(args: Array[String]) {
    val masterNumber = args(0)
    val masterPort = 5150 + masterNumber.toInt
    val numWorkers = args(1).toInt

    // dynamically set the port of the remote master
    val customAkkaConfig = ConfigFactory.parseString("akka.remote.netty.tcp.port=".concat(masterPort.toString))
    val defaultAkkaConfig = ConfigFactory.load.getConfig("remotesystem")
    val akkaConfig = ConfigFactory.load(customAkkaConfig.withFallback(defaultAkkaConfig))

    val system = ActorSystem("remotesystem", akkaConfig)

    val master = system.actorOf(Props(new RemoteMaster(system, numWorkers) ), "RemoteMaster".concat(masterNumber))
    master ! "Live and breathe"
  }
}



