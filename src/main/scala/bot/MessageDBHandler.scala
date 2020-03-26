package bot

import scala.collection.mutable
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
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
}


class MessageDBHandler {
  val messages = TableQuery[Messages]
  val db = Database.forConfig("h2mem1")
  Await.result(db.run(messages.schema.createIfNotExists), Duration.Inf)

  def sendMessage(senderId: String, recieverId: String, message: String): Unit = {
    val query = for {
      _ <- messages += (-1, message, senderId.toInt, recieverId.toInt)
    } yield()
    Await.result(db.run(query), Duration.Inf)
  }

  def showMessages(id: String): String = {
    val idInt : Int = id.toInt
    val query = for {
      idMessages <- messages.filter(it => it.receiver_id === idInt).result
    } yield idMessages
    val future = db.run(query)
    val seqMessages = Await.result(future, Duration.Inf)
    seqMessages.map(it => s"from: ${it._3} message: ${it._2}").mkString("\n")
  }
  def clearMessages(id: String): Unit = {
    // Drop all?
    val future = db.run(messages.delete)
    Await.result(future, Duration.Inf)
  }
}
