package bot

import com.bot4s.telegram.models.User
import slick.jdbc.H2Profile.api._

import scala.collection.mutable
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

// TODO: Is this OK?
import scala.concurrent.ExecutionContext.Implicits.global
// Definition of the USERS table
class Users(tag: Tag) extends Table[(Int, String, String)](tag, "USERS") {
  def id = column[Int]("ID", O.PrimaryKey)
  def name = column[String]("NAME")
  def login = column[String]("LOGIN")
  def * = (id, name, login)

}

class UsersDBHandler {
  //val users = mutable.Set[User]()

  val users = TableQuery[Users]
  val db = Database.forConfig("h2mem1")
  Await.result(db.run(users.schema.createIfNotExists), Duration.Inf)

  def showUsers(): String = {
    val transaction = for {
      all <- users.result
    } yield all
    val future: Future[Seq[(Int, String, String)]] = db.run(transaction)
    val usersSeq = Await.result(future, Duration.Inf)
    usersSeq.map(it => s"id: ${it._1} name: ${it._2}").mkString(sep="\n")
  }
  def registerUser(user: User): Unit = { //  users += user
    // TODO: remove extra " "
    val name = user.firstName + " " + user.lastName.getOrElse("")
    val transaction = for {
      _ <- users += (user.id, name, user.username.getOrElse(""))
    } yield ()
    Await.result(db.run(transaction), Duration.Inf)
  }
}
