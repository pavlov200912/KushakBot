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

import scala.util.{Failure, Success}


object BotStarter {

  implicit val serialization = org.json4s.native.Serialization

  case class Response(data: List[Data])
  case class Data(title: String)

  // data[].images[].link

  def main(args: Array[String]): Unit = {
    // Рулит потоками
    implicit val ec: ExecutionContext = ExecutionContext.global
    implicit val backend: SttpBackend[Future, Nothing] = OkHttpFutureBackend(
      SttpBackendOptions.Default.socksProxy("ps8yglk.ddns.net", 11999)
    )

    val request: RequestT[Id, Response, Nothing] = sttp
      .header("Authorization", "Client-ID 20ff243e5fe83b7")
      .get(uri"https://api.imgur.com/3/gallery/search?q=cats")
      .response(asJson[Response])

    val res: Future[Unit] = backend.send(request).map {
      response => response.unsafeBody
    }.map(println)

    Await.ready(res, Duration.Inf)
  }

}