package bot

import scala.collection.mutable

class MessageHandler {
  val messages = scala.collection.mutable.Map[String, mutable.MutableList[(String, String)]]()
    .withDefaultValue(mutable.MutableList())
  def sendMessage(senderId: String, recieverId: String, message: String): Unit = {
    if (!messages.contains(recieverId)) {
      messages(recieverId) = mutable.MutableList()
    }
    messages(recieverId) += (senderId -> message)
  }

  def showMessages(id: String): String = messages(id).foldLeft("") { (acc, pair) =>
      acc + s"Message: ${pair._2} from: ${pair._1} \n"
    }
  def clearMessages(id: String): Unit = messages(id) = mutable.MutableList()
}
