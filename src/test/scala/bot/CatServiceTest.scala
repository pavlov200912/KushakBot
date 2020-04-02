package bot

import com.bot4s.telegram.models.User
import com.softwaremill.sttp.{Response, SttpBackend}
import com.softwaremill.sttp.SttpBackend
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}

object RandomMock extends Randomizer {
  override def randomShuffle[T](list: List[T]) : List[T] = list
}

class CatServiceTest extends AnyFlatSpec with Matchers with MockFactory {
  trait  mocks {
    implicit val ec: ExecutionContextExecutor = ExecutionContext.global
    implicit val sttpBackend: SttpBackend[Future, Nothing] = mock[SttpBackend[Future, Nothing]]
    implicit val randomizer: RandomMock.type = RandomMock
    val service = new Service()
  }
  "ServiceRest" should "return cat link" in new mocks {
    (sttpBackend.send[Response] _).expects(*).returning(Future.successful(
      com.softwaremill.sttp.Response.ok(Response(List(Data(link = "www.cat.com"),
                                                      Data(link = "www.dog.com"),
                                                      Data(link = "www.pineapple.com")))
    )))

    val result: String = Await.result(service.getCat(), Duration.Inf)

    result shouldBe "www.cat.com"
  }
}

class UserHandlerTest extends AnyFlatSpec with Matchers {
  val userHandler = new UsersHandler()
  val users : List[User] = List(
    new User(1234, false, "Max", Some("Dog")),
    new User(2345, false, "Lucky", Some("Dog")),
    new User(3456, false, "Sophia", Some("Dog")),
    new User(4567, false,  "Coco", Some("Dog")),
    new User(5678, false,  "Musya")
  )
  users.foreach(user => userHandler.registerUser(user))

  userHandler.users shouldBe users.toSet
}

class MessageHandlerTest extends AnyFlatSpec with Matchers {
  val messageHandler = new MessageHandler()
  messageHandler.messages = mutable.Map(
    "1" -> mutable.MutableList("2"-> "aaa", "3" -> "bbb"),
    "2" -> mutable.MutableList("1" -> "ccc")
  )

  messageHandler.showMessages("1") shouldBe
    "Message: aaa from: 2\nMessage: bbb from: 3\n"


  messageHandler.clearMessages("1")
  messageHandler.clearMessages("2")

  messageHandler.messages shouldBe mutable.Map("1" -> mutable.MutableList(), "2" -> mutable.MutableList())


  messageHandler.sendMessage("2", "1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
  messageHandler.sendMessage("3", "1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
  messageHandler.sendMessage("1", "3", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")

  messageHandler.showMessages("1") shouldBe
    "Message: aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa from: 2\nMessage: aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa from: 3\n"
}
