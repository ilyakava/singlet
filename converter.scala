import org.gm4java.engine.support.GMConnectionPoolConfig
import org.gm4java.engine.support.PooledGMService
import org.slf4j._

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class GraphicsMagickResizer(maxLongSide: Integer) {
  lazy val log = LoggerFactory.getLogger(classOf[GraphicsMagickResizer])

  def apply(srcPath: Path) {
    val config = new GMConnectionPoolConfig()
    val gm = new PooledGMService(config)

    try {
      val thumbnailPath = Files.createTempFile("thumbnail", ".jpg").toAbsolutePath()
      val imgIn: BufferedImage = ImageIO.read(srcPath.toFile())

      val scale = if (imgIn.getWidth() >= imgIn.getHeight()) {
        // horizontal or square image
        Math.min(this.maxLongSide, imgIn.getWidth()) / imgIn.getWidth().toDouble
      } else {
        // vertical image
        Math.min(this.maxLongSide, imgIn.getHeight()) / imgIn.getHeight().toDouble
      }

      gm.execute("convert",
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
    val c = new GraphicsMagickResizer(200)
    val path = Paths.get(args(0))
    c.apply(path)
  }
}
