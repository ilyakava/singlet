import org.gm4java.engine.support.GMConnectionPoolConfig
import org.gm4java.engine.support.PooledGMService
import org.slf4j._

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class GraphicsMagickResizer(maxLongSide: Integer) {

  def apply(service: PooledGMService, srcPath: Path) = {
    lazy val log = LoggerFactory.getLogger(classOf[GraphicsMagickResizer])

    try {
      println("starting")
      val thumbnailPath = Files.createTempFile("thumbnail", ".jpg").toAbsolutePath()
      val imgIn: BufferedImage = ImageIO.read(srcPath.toFile())

      val scale = if (imgIn.getWidth() >= imgIn.getHeight()) {
        // horizontal or square image
        Math.min(this.maxLongSide, imgIn.getWidth()) / imgIn.getWidth().toDouble
      } else {
        // vertical image
        Math.min(this.maxLongSide, imgIn.getHeight()) / imgIn.getHeight().toDouble
      }

      service.execute("convert",
                 srcPath.toString(),
                 "-resize", Math.round(100 * scale) + "%",
                 thumbnailPath.toString())

      log.info("Thumbnail was saved to {}", thumbnailPath)
      println(thumbnailPath)
    } catch { case ex: Exception =>
        throw new IllegalStateException(ex.getMessage(), ex)
    }
  }
}

object Converter {
  def main(args: Array[String]) = {
    val config = new GMConnectionPoolConfig()
    val service = new PooledGMService(config)

    val c = new GraphicsMagickResizer(200)
    for(path <- args) {
      c.apply(service, Paths.get(path))
    }
  }
}
