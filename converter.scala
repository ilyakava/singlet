import org.gm4java.engine.support.GMConnectionPoolConfig
import org.gm4java.engine.support.PooledGMService
import org.slf4j._

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import akka.actor.{ActorSystem, ActorLogging, Actor, Props}

import scala.collection.mutable.ArrayBuffer

// a request to resize an image
case class ImageResize(service: PooledGMService, srcPath: Path, maxLongSide: Int)
// a new image that has completed processing
case class AdditionalImage(newSrcPath: Path)
case class WorkerReady()

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

class Master(var imagePaths: ArrayBuffer[String], service: PooledGMService) extends Actor with ActorLogging {
  def receive = {
    case WorkerReady =>
      val path = Paths.get(imagePaths.remove(0))
      sender ! ImageResize(service, path, 200)
    case AdditionalImage(thumbnailPath: Path) =>
      log.info("Thumbnail was saved to {}", thumbnailPath)
      println(thumbnailPath)
      if(imagePaths.isEmpty) {
        context.system.shutdown()
      } else {
        val path = Paths.get(imagePaths.remove(0))
        sender ! ImageResize(service, path, 200)
      }
  }
}

object Manager extends App {
  override def main(args: Array[String]) = {
    val config = new GMConnectionPoolConfig()
    val service = new PooledGMService(config)

    val system = ActorSystem("converters")

    var mutableArray = ArrayBuffer[String]()
    for(path <- args) { mutableArray += path }

    val master = system.actorOf(Props(new Master(mutableArray, service) ), "master")
    // 5 workers seems to be the limit on my machine (`java.lang.OutOfMemoryError: Java heap space` otherwise)
    // Is this a limitation of the number of cores?
    val worker1 = system.actorOf(Props( new ImageProcessor ), "worker1")
    val worker2 = system.actorOf(Props( new ImageProcessor ), "worker2")
    val worker3 = system.actorOf(Props( new ImageProcessor ), "worker3")
    val worker4 = system.actorOf(Props( new ImageProcessor ), "worker4")
    val worker5 = system.actorOf(Props( new ImageProcessor ), "worker5")

    master.tell(WorkerReady, worker1)
    master.tell(WorkerReady, worker2)
    master.tell(WorkerReady, worker3)
    master.tell(WorkerReady, worker4)
    master.tell(WorkerReady, worker5)

    system.awaitTermination()
  }
}
