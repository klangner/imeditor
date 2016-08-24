package pl.klangner.imeditor

import java.awt.{Color, Dimension}
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import scala.swing.{Graphics2D, Panel}

class ImagePanel extends Panel
{
  private var _imagePath = ""
  private var box = (0, 0, 0, 0)
  private var bufferedImage:BufferedImage = null

  def imagePath = _imagePath

  def imagePath_=(value:String) {
    _imagePath = value
    bufferedImage = ImageIO.read(new File(_imagePath))
    preferredSize = new Dimension(bufferedImage.getWidth, bufferedImage.getHeight)
    repaint()
  }

  def setBoxPosition(left: Int, top: Int, right: Int, bottom: Int): Unit = {
    if(left < right) {
      if (top < bottom) box = (left, top, right, bottom)
      else box = (left, bottom, right, top)
    } else {
      if (top < bottom) box = (right, top, left, bottom)
      else box = (right, bottom, left, top)

    }
    repaint()
  }

  override def paintComponent(g:Graphics2D) = {
    super.paintComponent(g)
    if (null != bufferedImage) {
      val w = bufferedImage.getWidth
      val h = bufferedImage.getHeight
      g.drawImage(bufferedImage, 0, 0, w, h, null)
      g.setColor(Color.red)
      g.drawRect(box._1, box._2, box._3-box._1, box._4-box._2)
    }
  }
}

object ImagePanel{
  def apply() = new ImagePanel()
}