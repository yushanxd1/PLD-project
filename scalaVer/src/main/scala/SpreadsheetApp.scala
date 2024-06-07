import scala.collection.mutable
import scala.io.StdIn._
import scala.util.control.Breaks.{break, breakable}

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
    if (users.contains(name)) {
      println(s"用戶 '$name' 已存在")
      return
    }
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
    print("請輸入用戶名和表格名：")
    try {
      val Array(userName, sheetName) = readLine().split(" ")
      if (!users.contains(userName)) {
        println(s"用戶 '$userName' 不存在")
        return
      }
      sheets.get((userName, sheetName)) match {
        case Some(sheet) => printSheet(sheet)
        case None =>
          val sharedSheets = sheets.filter { case ((user, sheet), _) =>
            sheet == sheetName && users(user) != users(userName)
          }
          if (sharedSheets.isEmpty) {
            println(s"表格 '$sheetName' 不存在")
          } else if (
            sharedSheets.exists { case ((user, sheet), _) =>
              sheets((user, sheet)).isSharedWith(users(userName))
            }
          ) {
            sharedSheets.foreach { case ((user, sheet), _) =>
              println(s"表格 '$sheetName' 由 '$user' 分享給 '$userName'")
              printSheet(sheets((user, sheet)))
            }
          } else {
            println(s"表格 '$sheetName' 不存在")
          }

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
        val Array(col, row, value) = readLine().split(" ")
        try {
          col.toInt
          row.toInt
        } catch {
          case _: Throwable =>
            println("輸入格式錯誤")
            return
        }
        if (
          row.toInt >= sheet.cells.length || col.toInt >= sheet.cells(0).length
        ) {
          println("輸入超出範圍")
          return
        }
        val newValue = eval(value)
        sheet.cells(row.toInt)(col.toInt) = newValue
        printSheet(sheet)
      case Some(sheet) => println("此表格不可編輯")
      case None =>
        val sharedSheets = sheets.filter { case ((user, sheet), _) =>
          sheet == sheetName && users(user) != users(userName)
        }
        if (sharedSheets.isEmpty) {
          println(s"表格 '$sheetName' 不存在")
        }
        // use isshare with to check if the user can edit the sheet
        else if (
          sharedSheets.exists { case ((user, sheet), _) =>
            sheets((user, sheet)).isSharedWith(users(userName))
          }
        ) {
          breakable {
            sharedSheets.foreach { case ((user, sheet), _) =>
              println(s"表格 '$sheetName' 由 '$user' 分享給 '$userName'")
              val sharedSheet = sheets((user, sheet))
              printSheet(sharedSheet)
              print("請輸入行 列 新值：")
              val Array(col, row, value) = readLine().split(" ")
              try {
                col.toInt
                row.toInt
              } catch {
                case _: Throwable =>
                  println("輸入格式錯誤")
                  break
              }
              if (
                row.toInt >= sharedSheet.cells.length || col.toInt >= sharedSheet
                  .cells(0)
                  .length
              ) {
                println("輸入超出範圍")
                break
              }
              val newValue = eval(value)
              sharedSheet.cells(row.toInt)(col.toInt) = newValue
              printSheet(sharedSheet)
            }
          }
        }
    }
  }

  def changeSheetAccessRight(): Unit = {
    print("請輸入用戶名和表格名和權限類型 (ReadOnly / Editable)：")
    try {
      val Array(userName, sheetName, accessRightStr) = readLine().split(" ")
      sheets.get((userName, sheetName)) match {
        case Some(sheet) =>
          val accessRight =
            if (accessRightStr == "ReadOnly") ReadOnly else Editable
          sheet.accessRight = accessRight
          println(s"更改表格 '$sheetName' 的權限為 '$accessRightStr'")
        case None => println(s"表格 '$sheetName' 不存在")
      }
    } catch {
      case _: Throwable => println("輸入格式錯誤")
    }

  }

  def collaborateSheet(): Unit = {
    print("請輸入用戶名和表格名以及合作用戶名：")
    try {
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
    } catch {
      case _: Throwable => println("輸入格式錯誤")
    }

  }

  def eval(expression: String): Double = {
    val operators = expression.split("[0-9]+").filter(_.nonEmpty)
    val operands = expression.split("[+\\-*/]").map(_.toDouble)
    var result = operands(0)
    for (i <- 0 until operators.length) {
      operators(i) match {
        case "*" => result *= operands(i + 1)
        case "/" => result /= operands(i + 1)
        case _   =>
      }
    }
    for (i <- 0 until operators.length) {
      operators(i) match {
        case "+" => result += operands(i + 1)
        case "-" => result -= operands(i + 1)
        case _   =>
      }
    }
    result

  }

  def menu(): Unit = {
    println("---------------Menu---------------")
    println("1. Create a user")
    println("2. Create a sheet")
    println("3. Check a sheet")
    println("4. Change a value in a sheet")
    println("5. Change a sheet's access right")
    println("6. Collaborate with an other user")
    println("7. Exit")
    println("----------------------------------")
  }

  def mainLoop(): Unit = {
    while (true) {
      menu()
      val choice = readLine()
      choice match {
        case "1" => createUser()
        case "2" => createSheet()
        case "3" => checkSheet()
        case "4" => changeSheetValue()
        case "5" => changeSheetAccessRight()
        case "6" => collaborateSheet()
        case "7" => return
        case _   => println("無效選擇，請重新輸入")
      }
    }
  }

  mainLoop()
}
