package bot
import slick.jdbc.H2Profile.api._

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

// Definition of the COFFEES table
class Coffees(tag: Tag) extends Table[(String, Double)](tag, "COFFEES") {
  def name = column[String]("COF_NAME", O.PrimaryKey)
  def price = column[Double]("PRICE")
  def * = (name, price)

}
object SliceExample {

  val coffees = TableQuery[Coffees]

  def main(args: Array[String]): Unit = {
    val db = Database.forConfig("h2mem1")
    try {
      // DBIO - я в приницпе просто описываю какие-то вычисленияЮ но сами запросы я не запускаю
      // В Slique транзикции скрыты, сущность DBIO описывает все действия, которые нужно выполнить
      // внутри транзакции
      val transaction = for {
        _ <- coffees.schema.createIfNotExists
        _ <- coffees += ("latte", 120)
        _ <- coffees += ("mocco", 150)
        all <- coffees.result // SELECT * FROM COFFEE
        filtered <- coffees.filter(_.price > 100.0).result // SELECT * FROM ?? WHERE (predicat)
      } yield println(all)

      val resultFuture: Future[Unit] = db.run(transaction)
      Await.result(resultFuture, Duration.Inf)
      //lines.foreach(println)
    } finally db.close
  }
}
