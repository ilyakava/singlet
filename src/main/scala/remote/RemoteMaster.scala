package remote

import org.gm4java.engine.support.GMConnectionPoolConfig
import org.gm4java.engine.support.PooledGMService
import org.slf4j._

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

// import akka.actor.{ActorSystem, ActorLogging, Actor, Props}
import akka.actor._
import com.typesafe.config.ConfigFactory

import scala.collection.mutable.ArrayBuffer

// a request to resize an image
case class ImageResize(service: PooledGMService, srcPath: Path, maxLongSide: Int)
// a new image that has completed processing
case class AdditionalImage(newSrcPath: Path)

class ImageProcessor extends Actor with ActorLogging {
  def receive = {
    case ImageResize(service: PooledGMService, srcPath: Path, maxLongSide: Int) =>
      try {
        println("starting")
        val thumbnailPath = Files.createTempFile("thumbnail", ".jpg").toAbsolutePath()
        val imgIn: BufferedImage = ImageIO.read(srcPath.toFile())

        val scale = if (imgIn.getWidth() >= imgIn.getHeight()) {
          // horizontal or square image
          Math.min(maxLongSide, imgIn.getWidth()) / imgIn.getWidth().toDouble
        } else {
          // vertical image
          Math.min(maxLongSide, imgIn.getHeight()) / imgIn.getHeight().toDouble
        }

        service.execute("convert",
                   srcPath.toString(),
                   "-resize", Math.round(100 * scale) + "%",
                   thumbnailPath.toString())

        sender ! AdditionalImage(thumbnailPath)
      } catch { case ex: Exception =>
        println(ex.getMessage())
          throw new IllegalStateException(ex.getMessage(), ex)
    }
  }
}

class RemoteMaster(system: ActorSystem, numWorkers: Int) extends Actor with ActorLogging {
  val config = new GMConnectionPoolConfig()
  val service = new PooledGMService(config)
  // 4-5 workers seems to be the limit on my machine for a single JVM (`java.lang.OutOfMemoryError: Java heap space` otherwise)
  val workers = makeWorkers(numWorkers, system)

  var imgQueue = ArrayBuffer[String]()

  def receive = {
    case msg: String =>
      println(s"RemoteMaster received message '$msg'")
    case imagePaths: Array[String] =>
      println("Remote Master is queuing {} images", imagePaths.length)
      // The initialization case (LocalMaster sends array of image paths)
      for(path <- imagePaths) { imgQueue += path }

      for(worker <- workers) {
        val path = Paths.get(imgQueue.remove(0))
        worker ! ImageResize(service, path, 200)
      }
    case AdditionalImage(thumbnailPath: Path) =>
      log.info("Thumbnail was saved to {}", thumbnailPath)
      println(thumbnailPath)
      if(imgQueue.isEmpty) {
        context.system.shutdown()
      } else {
        val path = Paths.get(imgQueue.remove(0))
        sender ! ImageResize(service, path, 200)
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



