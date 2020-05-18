package bot

import com.bot4s.telegram.models.User
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.{Future}

import scala.concurrent.ExecutionContext.Implicits.global


// Definition of the USERS table
class Users(tag: Tag) extends Table[(Int, String, String)](tag, "USERS") {
  def id = column[Int]("ID", O.PrimaryKey)
  def name = column[String]("NAME")
  def login = column[String]("LOGIN")
  def * = (id, name, login)
}



class UsersDBHandler(users: TableQuery[Users]){
  def getUserId(login: String): Future[Int] = {
    val transaction = for {
      id <- users.filter(_.login === login).map(_.id).result.head
    } yield id
    db.run(transaction)
  }


  val db : H2Profile.backend.Database =  Database.forConfig("h2mem1")
  def init(): Future[Unit] = {
    db.run(users.schema.createIfNotExists)
  }

  //val users = mutable.Set[User]()

  def showUsers(): Future[String] = {
    val transaction = for {
      all <- users.result
    } yield all
    val future: Future[Seq[(Int, String, String)]] = db.run(transaction)
    future.flatMap(seq => Future(seq.map(it => s"id: ${it._1} name: ${it._2}").mkString("\n")))
  }
  def registerUser(user: User): Future[Unit] = { //  users += user
    // TODO: remove extra " "
    val name = user.firstName + " " + user.lastName.getOrElse("")
    val transaction = for {
      _ <- users += (user.id, name, user.username.getOrElse(""))
    } yield ()
    db.run(transaction)
  }
}
