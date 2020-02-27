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

class BotStarter(override val client: RequestHandler[Future]) extends TelegramBot
  with Polling
  with Commands[Future]{

  val messages = scala.collection.mutable.Map[String, mutable.MutableList[(String, String)]]().withDefaultValue(mutable.MutableList())
  val registredUsers = mutable.Set[User]()
  onCommand("/start") { implicit msg =>
    //msg.chat.id
    //msg.from
    msg.from match {
      case None => ()
      case Some(x) => {
        registredUsers += x
      }
    }
    reply("hi").void
  }

  onCommand("/users") {implicit msg =>
    var answer = ""
    registredUsers.foreach {
      it =>
        answer += s"${it.firstName} ${unwrapName(it.lastName)}\n"
    }
    reply(answer).void
  }

  onCommand("/send") { implicit msg =>
    msg.from match {
      case None => reply("bbb").void
      case (Some (x)) => withArgs { args =>

        // += (x.id.toString() -> args.seq(1))
        messages(args.seq.head) += (x.id.toString -> args.seq(1))
        reply((args.seq(0)).mkString).void
      }
    }
  }

  def unwrapName(option: Option[String]) = option match {
    case None => ""
    case Some(x) => x
  }

  onCommand("/iam") {implicit msg =>
    msg.from match {
      case None => reply("I don't know u((").void
      case Some(x) => reply(s"Hello, ${x.firstName} ${unwrapName(x.lastName)}").void
    }
  }

}

object BotStarter {
  def main(args: Array[String]): Unit = {
    // Рулит потоками
    implicit val ec: ExecutionContext = ExecutionContext.global
    implicit val backend = OkHttpFutureBackend(
      SttpBackendOptions.Default.socksProxy("ps8yglk.ddns.net", 11999)
    )

    val token = "1032245037:AAFhT3mAz7PYFxOWiAqK7C8lgbF-Yq9D_MM"
    val bot = new BotStarter(new FutureSttpClient(token))
   Await.result(bot.run(), Duration.Inf)
  }
}