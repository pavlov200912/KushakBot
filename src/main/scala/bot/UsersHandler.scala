package bot

import com.bot4s.telegram.models.User

import scala.collection.mutable


class UsersHandler {
  val users = mutable.Set[User]()
  def showUsers(): String = users.foldLeft(""){
    (acc, user) => acc + s"${user.firstName} ${user.lastName.getOrElse("")}, id: ${user.id}\n"
  }
  def registerUser(user: User): Unit = users += user
}
