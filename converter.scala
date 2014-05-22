import org.gm4java.engine.support.GMConnectionPoolConfig
import org.gm4java.engine.support.PooledGMService
import org.slf4j._

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import akka.actor.{ActorSystem, ActorLogging, Actor, Props}

import util.Random.nextInt

case class ImageResize(service: PooledGMService, srcPath: Path, maxLongSide: Int)

class ImageProcessor extends Actor with ActorLogging {
  // This annotation is annoying, but it does not prevent other cases from matching
  // I don't 100% understand the workaround: http://stackoverflow.com/questions/12869251/the-argument-types-of-an-anonymous-function-must-be-fully-known-sls-8-5
  def receive = {(req: ImageResize) =>
    req match {
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

          log.info("Thumbnail was saved to {}", thumbnailPath)
          println(thumbnailPath)
        } catch { case ex: Exception =>
          println(ex.getMessage())
            throw new IllegalStateException(ex.getMessage(), ex)
      }
    }
  }
}

object Manager extends App {
  override def main(args: Array[String]) = {
    val config = new GMConnectionPoolConfig()
    val service = new PooledGMService(config)

    val system = ActorSystem("converters")

    for(path <- args) {
      system.actorOf(Props( new ImageProcessor ), nextInt.toString) ! ImageResize(service, Paths.get(path), 200)
    }
    system.shutdown()
  }
}
