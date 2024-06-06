import scala.collection.mutable
import scala.io.StdIn._
// import scala.reflect.runtime.currentMirror
// import scala.tools.reflect.ToolBox

case class User(name: String)

case class Sheet(
    name: String,
    var accessRight: AccessRight = Editable,
    cells: Array[Array[Double]] = Array.fill(3, 3)(0.0)
) {
  private val sharedWith = mutable.Set[User]()

  def shareWith(user: User): Unit = {
    sharedWith += user
  }

  def isSharedWith(user: User): Boolean = {
    sharedWith.contains(user)
  }

  def canEdit(user: User): Boolean = {
    accessRight match {
      case Editable => true
      case ReadOnly => isSharedWith(user)
    }
  }
}

sealed trait AccessRight
case object Editable extends AccessRight
case object ReadOnly extends AccessRight

object SpreadsheetApp extends App {
  val users = mutable.Map[String, User]()
  val sheets = mutable.Map[(String, String), Sheet]()

  def createUser(): Unit = {
    print("請輸入用戶名：")
    val name = readLine()
    users += (name -> User(name))
    println(s"創建用戶 '$name'")
  }

  def createSheet(): Unit = {
    print("請輸入用戶名和表格名：")
    try {
      val Array(userName, sheetName) = readLine().split(" ")
      if (users.contains(userName)) {
        sheets += ((userName, sheetName) -> Sheet(sheetName))
        println(s"為 '$userName' 創建表格 '$sheetName'")
      } else {
        println(s"用戶 '$userName' 不存在")
      }
    } catch {
      case _: Throwable => println("輸入格式錯誤")
    }

  }

  def checkSheet(): Unit = {
    try {
      print("請輸入用戶名和表格名：")
      val Array(userName, sheetName) = readLine().split(" ")
      if (!users.contains(userName)) {
        println(s"用戶 '$userName' 不存在")
        return
      }
      sheets.get((userName, sheetName)) match {
        case Some(sheet) => printSheet(sheet)
        case None        => println(s"表格 '$sheetName' 不存在")
      }
    } catch {
      case _: Throwable => println("輸入格式錯誤")
    }

  }

  def printSheet(sheet: Sheet): Unit = {
    for (row <- sheet.cells) {
      println(row.mkString(", "))
    }
  }

  def changeSheetValue(): Unit = {
    print("請輸入用戶名和表格名：")
    val Array(userName, sheetName) = readLine().split(" ")
    if (!users.contains(userName)) {
      println(s"用戶 '$userName' 不存在")
      return
    }
    sheets.get((userName, sheetName)) match {
      case Some(sheet) if sheet.canEdit(users(userName)) =>
        printSheet(sheet)
        print("請輸入行 列 新值：")
        val Array(row, col, value) = readLine().split(" ")
        try {
          row.toInt
          col.toInt
        } catch {
          case _: Throwable =>
            println("輸入格式錯誤")
            return
        }
        val newValue = eval(value)
        sheet.cells(row.toInt)(col.toInt) = newValue
        printSheet(sheet)
      case Some(sheet) => println("此表格不可編輯")
      case None        => println(s"表格 '$sheetName' 不存在")
    }
  }

  def changeSheetAccessRight(): Unit = {
    print("請輸入用戶名和表格名和權限類型 (ReadOnly / Editable)：")
    val Array(userName, sheetName, accessRightStr) = readLine().split(" ")
    sheets.get((userName, sheetName)) match {
      case Some(sheet) =>
        val accessRight =
          if (accessRightStr == "ReadOnly") ReadOnly else Editable
        sheet.accessRight = accessRight
        println(s"更改表格 '$sheetName' 的權限為 '$accessRightStr'")
      case None => println(s"表格 '$sheetName' 不存在")
    }
  }

  def collaborateSheet(): Unit = {
    print("請輸入用戶名和表格名以及合作用戶名：")
    val Array(userName, sheetName, collaboratorName) = readLine().split(" ")
    (users.get(userName), users.get(collaboratorName)) match {
      case (Some(user), Some(collaborator)) =>
        sheets.get((userName, sheetName)) match {
          case Some(sheet) =>
            sheet.shareWith(collaborator)
            println(s"將 '$sheetName' 表格分享給 '$collaboratorName'")
          case None => println(s"表格 '$sheetName' 不存在")
        }
      case _ => println("用戶不存在")
    }
  }

  def eval(expression: String): Double = {
    expression.toDouble

  }

  def menu(): Unit = {
    println("---------------Menu---------------")
    println("1. Create a user")
    println("2. Create a sheet")
    println("3. Check a sheet")
    println("4. Change a value in a sheet")
    println("5. Change a sheet's access right")
    println("6. Collaborate with an other user")
    println("----------------------------------")
  }

  def mainLoop(): Unit = {
    while (true) {
      menu()
      val choice = readLine().toInt
      choice match {
        case 1 => createUser()
        case 2 => createSheet()
        case 3 => checkSheet()
        case 4 => changeSheetValue()
        case 5 => changeSheetAccessRight()
        case 6 => collaborateSheet()
        case _ => println("無效選擇，請重新輸入")
      }
    }
  }

  mainLoop()
}
