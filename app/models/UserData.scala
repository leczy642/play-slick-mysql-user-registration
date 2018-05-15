package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * A repository for people.
  *
  * @param dbConfigProvider The Play db config provider. Play will inject this for you.
  */
@Singleton
class UserData @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import profile.api._

  /**
    * Here we define the table. It will have a name of people
    */
  private class UserTable(tag: Tag) extends Table[User](tag, "users") {

    /** The ID column, which is the primary key, and will auto increment */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    /** The name, phone, email and age column */
    def name = column[String]("name")

    def phone = column[Int]("phone")

    def email = column[String]("email")

    def age = column[Int]("age")


    /**
      * This is the tables default "projection".
      *
      * It defines how the columns are converted to and from the Person object.
      *
      * In this case, we are simply passing the id, name and page parameters to the Person case classes
      * apply and unapply methods.
      */
    def * = (id, name, phone, email, age) <> ((User.apply _).tupled, User.unapply)
  }

  private val allUsers = TableQuery[UserTable]

  def createUser(name: String, phone: Int, email: String, age: Int): Future[User] = db.run {
    (allUsers.map(p => (p.name, p.phone, p.email, p.age))

      //We return the id, so as to know which id was generated for each user, also we define a transformation for the
      //returned value which combines our parameters with the returned id
      returning allUsers.map (_.id)
      into ((namePhone, id) => User(id, namePhone._1, namePhone._2, namePhone._3, namePhone._4))

      //this is where we insert a user into the database
      ) += (name, phone, email, age)

  }

  // so now we list all users
  def show(): Future[Seq[User]] = db.run {
    allUsers.result
  }

  }