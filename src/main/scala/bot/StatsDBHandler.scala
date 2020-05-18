package bot

import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.Future

case class StatsRow(id: Int, userId: Int, link: String)

import scala.concurrent.ExecutionContext.Implicits.global
class Stats (tag: Tag) extends Table[StatsRow](tag, "STATS") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def user_id = column[Int]("USER_ID")
  def link = column[String]("LINK")
  def * = (id, user_id, link) <> (StatsRow.tupled, StatsRow.unapply)
}


class StatsDBHandler(stats: TableQuery[Stats])
{
  val db : H2Profile.backend.Database =  Database.forConfig("h2mem1")
  def init(): Future[Unit] = {
    db.run(stats.schema.createIfNotExists)
  }
  def showStats(userId: Int): Future[String] = {
    val transaction = for {
      catStats <- stats.filter(_.user_id === userId).result
    } yield  catStats
    val future = db.run(transaction)
    future.flatMap(seq => Future(
      seq.map(it => s"link: [${it.link}]").mkString("\n")
    ))
  }
  def addCatLink(userId: Int, link: String): Future[Unit] = {
    val query = for {
      _ <- stats += StatsRow(-1, userId, link)
    } yield()
    db.run(query)
  }
}
