package bot

import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.{FutureSttpClient, ScalajHttpClient}
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.models.{Message, User}
import com.softwaremill.sttp.SttpBackendOptions
import com.softwaremill.sttp.okhttp.{OkHttpBackend, OkHttpFutureBackend}
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}

import scala.collection.mutable
import scala.collection.mutable.Queue
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.Source
import com.softwaremill.sttp._
import com.softwaremill.sttp.json4s._
import kotlin.collections.EmptyList
import org.json4s.native.Serialization

import scala.util.Random

class BotStarter(override val client: RequestHandler[Future], val service: Service) extends TelegramBot
  with Polling
  with Commands[Future]{

  val messages = scala.collection.mutable.Map[String, mutable.MutableList[(String, String)]]()
    .withDefaultValue(mutable.MutableList())
  val registeredUsers = mutable.Set[User]()
  onCommand("/start") { implicit msg =>
    //msg.chat.id
    //msg.from
    msg.from match {
      case None => reply("Error").void
      case Some(x) => {
        registeredUsers += x
        reply(s"hi\nyour id: ${x.id}").void
      }
    }
  }

  onCommand("/users") {implicit msg =>
    var answer = ""
    registeredUsers.foreach {
      it =>
        answer += s"${it.firstName} ${it.lastName.getOrElse("")}, id: ${it.id}\n"
    }
    reply(answer).void
  }

  onCommand("/send") { implicit msg =>
    msg.from match {
      case None => reply("ERROR").void
      case (Some (x)) => withArgs { args =>
        val id = args.head
        if (!messages.contains(id)) {
          messages(args.seq.head) = mutable.MutableList()
        }
        messages(args.head) += (x.id.toString -> args.tail.foldLeft("")((acc, word) => acc + word + " "))
        reply("Message was sent").void
      }
    }
  }

  onCommand("/check") { implicit msg =>
    val res = msg.from match {
      case None => "ERROR"
      case (Some(x)) =>
        val answer = messages(x.id.toString).foldLeft("") {(acc, pair) =>
          acc + s"Message: ${pair._2} from: ${pair._1} \n"
        }
        messages(x.id.toString) = mutable.MutableList()
        answer
    }
    reply(res).void
  }

  def unwrapName(option: Option[String]): String = option match {
    case None => ""
    case Some(x) => x
  }

  onCommand("/iam") {implicit msg =>
    msg.from match {
      case None => reply("I don't know u((").void
      case Some(x) => reply(s"Hello, ${x.firstName} ${unwrapName(x.lastName)}").void
    }
  }

  onCommand("/cats") {implicit msg =>
    service.getCat().flatMap(reply(_)).void
  }

}


object BotStarter {
  def main(args: Array[String]): Unit = {
    // Рулит потоками
    implicit val ec: ExecutionContext = ExecutionContext.global
    implicit val backend: SttpBackend[Future, Nothing] = OkHttpFutureBackend(
      SttpBackendOptions.Default.socksProxy("ps8yglk.ddns.net", 11999)
    )

    val filename = "token.txt"
    val fileSource = Source.fromFile(filename)
    val token =  fileSource.mkString
    fileSource.close()

    val service: Service = new Service()
    val bot = new BotStarter(new FutureSttpClient(token), service)

    Await.result(bot.run(), Duration.Inf)
  }

}
