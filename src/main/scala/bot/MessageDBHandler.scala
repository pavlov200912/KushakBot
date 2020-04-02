package bot

import slick.jdbc.H2Profile

import scala.collection.mutable
import slick.jdbc.H2Profile.api._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration


// TODO: Is this OK?
import scala.concurrent.ExecutionContext.Implicits.global
// Definition of the USERS table
class Messages(tag: Tag) extends Table[(Int, String, Int, Int)](tag, "MESSAGES") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def text = column[String]("TEXT")
  def sender_id = column[Int]("SENDER_ID")
  def receiver_id = column[Int]("RECEIVER_ID")
  def * = (id, text, sender_id, receiver_id)

    // TODO: Add foreign keys
  /*def sender = foreignKey("sender_fk", sender_id,
    TableQuery[Messages]) (_.id, onDelete=ForeignKeyAction.Cascade)

  def receiver = foreignKey("receiver_fk", receiver_id,
    TableQuery[Messages]) (_.id, onDelete=ForeignKeyAction.Cascade)*/
}


class MessageDBHandler(users: TableQuery[Users], messages: TableQuery[Messages]) {
  lazy val db : H2Profile.backend.Database = Database.forConfig("h2mem1")
  def init(): Future[Unit] = {
    db.run(messages.schema.createIfNotExists)
  }



  def sendMessage(senderId: String, recieverId: String, message: String): Future[Unit] = {
    val query = for {
      _ <- messages += (-1, message, senderId.toInt, recieverId.toInt)
    } yield()
    db.run(query)
  }

  def showMessages(id: String): Future[String] = {
    val idInt : Int = id.toInt
    val query = for {
      idMessages <- messages.filter(it => it.receiver_id === idInt).result
    } yield idMessages
    db.run(query).flatMap(seq => Future(
      seq.map(it => s"from: ${it._3} message: ${it._2}").mkString("\n"))
    )
  }
  def clearMessages(id: String): Future[Unit] = {
    // Drop all?
    db.run(messages.delete).flatMap(_ => Future())
  }
}
