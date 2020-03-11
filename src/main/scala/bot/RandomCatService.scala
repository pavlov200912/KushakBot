package bot

import scala.concurrent.{Await, ExecutionContext, Future}
import com.softwaremill.sttp._
import com.softwaremill.sttp.json4s._
import org.json4s.native.Serialization
import scala.util.Random

case class Response(data: List[Data])
case class Data(link: String)

class Service(implicit val backend: SttpBackend[Future, Nothing],
              implicit val ec : ExecutionContext = ExecutionContext.global,
              implicit val serialization : Serialization.type = org.json4s.native.Serialization) {
  val request: RequestT[Id, Response, Nothing] = sttp
    .header("Authorization", "Client-ID 20ff243e5fe83b7")
    .get(uri"https://api.imgur.com/3/gallery/search?q=cats")
    .response(asJson[Response])

  def getCat(): Future[String] = {
    backend.send(request).map {
      response => Random.shuffle(response.unsafeBody.data).head.link
    }
  }
}
