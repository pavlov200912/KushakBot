package bot

import com.softwaremill.sttp.{Response, SttpBackend}

import com.softwaremill.sttp.SttpBackend
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}

object RandomMock extends Randomizer {
  override def randomShuffle(list: List[Any]): Unit = list
}

class CatServiceTest extends AnyFlatSpec with Matchers with MockFactory {
  trait  mocks {
    implicit val ec: ExecutionContextExecutor = ExecutionContext.global
    implicit val sttpBackend: SttpBackend[Future, Nothing] = mock[SttpBackend[Future, Nothing]]
    val randomizer: RandomMock.type = RandomMock
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
