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

  val messageHandler = new MessageHandler()
  val usersHandler = new UsersHandler()
  onCommand("/start") { implicit msg =>
    msg.from match {
      case None => reply("Error").void
      case Some(x) =>
        usersHandler.registerUser(x)
        reply(s"hi\nyour id: ${x.id}").void
    }
  }

  onCommand("/users") {implicit msg =>
    reply(usersHandler.showUsers()).void
  }

  onCommand("/send") { implicit msg =>
    msg.from match {
      case None => reply("ERROR").void
      case (Some (x)) => withArgs { args =>
        val id = args.head
        // TODO: Add function sendMessage in MessageHandler class
        messageHandler.sendMessage(id, x.id.toString, args.tail.foldLeft("")((acc, word) => acc + word + " "))
        reply("Message was sent").void
      }
    }
  }

  onCommand("/check") { implicit msg =>
    val res = msg.from match {
      case None => "ERROR"
      case (Some(x)) =>
        val ans = messageHandler.showMessages(x.id.toString)
        messageHandler.clearMessages(x.id.toString)
        ans
    }
    reply(res).void
  }

  onCommand("/iam") {implicit msg =>
    msg.from match {
      case None => reply("I don't know u((").void
      case Some(x) => reply(s"Hello, ${x.firstName} ${x.lastName.getOrElse("")}").void
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
