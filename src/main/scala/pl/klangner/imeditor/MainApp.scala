package pl.klangner.imeditor

import java.io.File
import javax.swing.JFileChooser

import scala.collection.mutable.ListBuffer
import scala.swing.BorderPanel.Position
import scala.swing._
import scala.swing.event.{ButtonClicked, SelectionChanged, EditDone}

/**
  * @author Krzysztof Langner on 2016-06-14.
  */
object MainApp extends SimpleSwingApplication {

  val loadFolderButton = new Button("Load")
  val imageList = new ListView(ListBuffer[String]("           "))
  val leftField = new TextField(5)
  val topField = new TextField(5)
  val rightField = new TextField(5)
  val bottomField = new TextField(5)
  val textField = new TextField(10)
  val imageCanvas = new Button("Image")
  val prevButton = new Button("<---")
  val posLabel = new Label("0/0")
  val nextButton = new Button("--->")

  def top = new MainFrame {
    title = "Image Metadata Editor"
    preferredSize = new Dimension(800, 600)

    contents = new BorderPanel {

      add(new FlowPanel(FlowPanel.Alignment.Left)(loadFolderButton), Position.North)
      add(new SplitPane(Orientation.Vertical, new ScrollPane(imageList), editorForm), Position.Center)
    }
    centerOnScreen()
  }

  val editorForm = new BorderPanel {
    val top = new FlowPanel(new Label(" Left = "), leftField, new Label(" Top = "), topField, new Label(" Right = "),
      rightField, new Label(" Bottom = "), bottomField, new Label(" Text = "), textField)
    add(top, Position.North)
    add(imageCanvas, Position.Center)
  }

  listenTo(loadFolderButton, leftField, topField, rightField, bottomField, imageList)
  reactions += {
    case ButtonClicked(`loadFolderButton`) => openFolderAction()
    case EditDone(`leftField`) => println("Left: " + leftField.text)
    case SelectionChanged(`imageList`) => println("Change image")
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
      val imgs = dir.listFiles.filter(_.isFile).filter(_.getName.endsWith(".png")).map(_.getName)
      imageList.listData = imgs
    }
  }
}