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
import slick.jdbc.H2Profile.api._

import scala.util.Random



class BotStarter(override val client: RequestHandler[Future], val service: Service,
                 val usersHandler: UsersDBHandler, val messageHandler: MessageDBHandler,
                 val statsHandler: StatsDBHandler) extends TelegramBot
  with Polling
  with Commands[Future]{

  onCommand("/start") { implicit msg =>

    msg.from match {
      case None => reply("Error").void
      case Some(x) =>
        usersHandler.registerUser(x).flatMap(_ =>
          reply(s"hi\nyour id: ${x.id}").void)
    }
  }

  onCommand("/users") {implicit msg =>
    usersHandler.showUsers().flatMap(reply(_).void)
  }

  onCommand("/send") { implicit msg =>
    msg.from match {
      case None => reply("ERROR").void
      case (Some (x)) => withArgs { args =>
        val id = args.head
        messageHandler.
          sendMessage(id, x.id.toString, args.tail.foldLeft("")((acc, word) => acc + word + " ")).
          flatMap(_ => reply("Message was sent").void)

      }
    }
  }

  onCommand("/check") { implicit msg =>
    msg.from match {
      case None => Future()
      case (Some(x)) =>
        messageHandler.showMessages(x.id.toString).flatMap(
          ans => messageHandler.clearMessages(x.id.toString).flatMap( _ =>
            reply(ans).void
          )
        )
    }
  }

  onCommand("/iam") {implicit msg =>
    msg.from match {
      case None => reply("I don't know u((").void
      case Some(x) => reply(s"Hello, ${x.firstName} ${x.lastName.getOrElse("")}").void
    }
  }

  onCommand("/cats") {implicit msg =>
    msg.from match {
      case None => reply("ERROR").void
      case Some(user) =>
        for {
          link <- service.getCat()
          - <- statsHandler.addCatLink(user.id, link)
          _ <- reply(link)
        }yield  ()
    }
  }

  onCommand("/stats") {implicit msg =>
    msg.from match {
      case None => reply("ERROR").void
      case (Some (x)) => withArgs { args =>
        if (args.isEmpty) {
          statsHandler.showStats(x.id).flatMap(reply(_).void)
        } else {
          val arg = args.head
          if (arg forall Character.isDigit) {
            // Assuming login can't be only from digits
            for {
              stats <- statsHandler.showStats(arg.toInt)
              _     <- reply(stats).void
            } yield ()
          } else {
            for {
              userId <- usersHandler.getUserId(arg)
              stats  <- statsHandler.showStats(userId)
              _      <- reply(stats).void
            } yield ()
          }
        }
      }
    }
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

    val users = TableQuery[Users]
    val messages = TableQuery[Messages]
    val stats = TableQuery[Stats]
    val usersHandler = new UsersDBHandler(users)
    val messageHandler = new MessageDBHandler(users, messages)
    val statsHandler = new StatsDBHandler(stats)
    val init = for {
      _ <- usersHandler.init()
      _ <- messageHandler.init()
      _ <- statsHandler.init()
      bot = new BotStarter(new FutureSttpClient(token), service,
        usersHandler, messageHandler, statsHandler)
      _ <- bot.run()
    } yield ()
    Await.result(init, Duration.Inf)
  }

}
