package pl.klangner.imeditor

import java.io.File
import javax.swing.JFileChooser

import scala.collection.mutable
import scala.swing.BorderPanel.Position
import scala.swing._
import scala.swing.event.{ButtonClicked, EditDone, MouseClicked}


case class ImageMetadata(left: Int, top: Int, right: Int, bottom: Int, plateText: String)

/**
  * @author Krzysztof Langner on 2016-06-14.
  */
object MainApp extends SimpleSwingApplication {

  val loadFolderButton = new Button("Load")
  val leftTopField = new TextField(10)
  val rightBottomField = new TextField(10)
  val plateTextField = new TextField(10)
  val imagePanel = new ImagePanel()
  val prevButton = new Button("<--")
  val posLabel = new Label("0/0")
  val nextButton = new Button("-->")

  // State
  val imageList = mutable.ArrayBuffer[File]()
  val metadataList = mutable.HashMap[String, ImageMetadata]()
  var currentImageIndex = 0

  def top = new MainFrame {
    title = "Image Metadata Editor"
    preferredSize = new Dimension(800, 600)

    contents = new BorderPanel {

      add(new FlowPanel(FlowPanel.Alignment.Left)(loadFolderButton), Position.North)
      add(editorForm, Position.Center)
      add(new FlowPanel(prevButton, posLabel, nextButton), Position.South)
    }
    centerOnScreen()
  }

  val editorForm = new BorderPanel {
    val top = new FlowPanel(FlowPanel.Alignment.Left)(
      new Label(" Left, Top: "), leftTopField,
      new Label(" Right, Bottom: "), rightBottomField,
      new Label(" Text: "), plateTextField)
    add(top, Position.North)
    add(new ScrollPane(imagePanel), Position.Center)
  }

  listenTo(loadFolderButton,
    leftTopField, rightBottomField, plateTextField,
    prevButton, nextButton, imagePanel.mouse.clicks)
  reactions += {
    case ButtonClicked(`loadFolderButton`) => openFolderAction()
    case EditDone(`leftTopField`) => updateMetadata()
    case EditDone(`rightBottomField`) => updateMetadata()
    case EditDone(`plateTextField`) => updateMetadata()
    case ButtonClicked(`prevButton`) => showImage(currentImageIndex-1)
    case ButtonClicked(`nextButton`) => showImage(currentImageIndex+1)
    case e: MouseClicked => imageClicked(e.point)
  }

  /** Open folder action. */
  def openFolderAction(): Unit = {
    selectFolder().foreach { path =>
      loadImageList(path)
    }
  }

  /** Show dialog box for selecting folder */
  def selectFolder(): Option[File] = {
    val chooser = new JFileChooser()
    chooser.setCurrentDirectory(new java.io.File("."))
    chooser.setDialogTitle("Select image folder")
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
    chooser.setAcceptAllFileFilterUsed(false)

    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) Some(chooser.getSelectedFile)
    else None
  }

  def loadImageList(dir: File): Unit = {
    if (dir.isDirectory){
      imageList.clear()
      imageList ++= dir.listFiles.filter(_.isFile).filter(imageFilter)
      metadataList.clear()
      showImage(0)
    }
  }

  def imageFilter(file: File): Boolean = file.getName.endsWith(".png") || file.getName.endsWith(".jpg")

  /** Show image */
  def showImage(index: Int): Unit = {
    if(index < imageList.size && index >= 0){
      currentImageIndex = index
      val meta = metadataList.getOrElse(imageList(index).getName, ImageMetadata(0, 0, 0, 0, ""))
      imagePanel.imagePath = imageList(index).getAbsolutePath
      imagePanel.setBoxPosition(meta.left, meta.top, meta.right, meta.bottom)
      posLabel.text = "%d/%d".format(currentImageIndex+1, imageList.size)
      leftTopField.text = "%d, %d".format(meta.left, meta.top)
      rightBottomField.text = "%d, %d".format(meta.right, meta.bottom)
      plateTextField.text = meta.plateText
      leftTopField.requestFocus()
    }
  }

  def imageClicked(pos: Point) : Unit = {
    if(leftTopField.hasFocus){
      leftTopField.text = "%d, %d".format(pos.x, pos.y)
    } else if(rightBottomField.hasFocus){
      rightBottomField.text = "%d, %d".format(pos.x, pos.y)
    }
    updateMetadata()
  }

  /** Save image metadata into the file */
  def updateMetadata() : Unit = {
    val lt = parsePos(leftTopField.text)
    val rb = parsePos(rightBottomField.text)
    imagePanel.setBoxPosition(lt._1, lt._2, rb._1, rb._2)
    if(currentImageIndex > -1 && currentImageIndex < imageList.size) {
      val meta = ImageMetadata(lt._1, lt._2, rb._1, rb._2, plateTextField.text)
      metadataList += (imageList(currentImageIndex).getName -> meta)
    }
  }

  def parsePos(str: String): (Int, Int) = {
    val tokens = str.split(",")
    if(tokens.size != 2) (0, 0)
    else (safeInt(tokens(0)), safeInt(tokens(1)))
  }

  def safeInt(str: String): Int = {
    try{
      str.trim.toInt
    } catch {
      case e: NumberFormatException => 0
    }
  }
}