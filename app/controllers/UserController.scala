package controllers

import javax.inject._
import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.i18n._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class UserController @Inject()(repo: UserData,
                                 cc: MessagesControllerComponents
                                )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  /**
    * The mapping for the person form.
    */
  val userForm: Form[CreateUserForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "phone" ->number,
      "email" -> email,
      "age" -> number.verifying(min(0), max(200))

    )(CreateUserForm.apply)(CreateUserForm.unapply)
  }


  def index = Action { implicit request =>
    Ok(views.html.index(userForm))
  }

  def addUser = Action.async { implicit request =>
    userForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.index(errorForm)))
      },
      //if there are no errors, we will create the user
      person => {
        repo.createUser(person.name, person.phone, person.email, person.age).map { _ =>
          Redirect(routes.UserController.index).flashing("success" -> "user has been created")
        }
      }
    )
  }

  def getUsers = Action.async { implicit request =>
    repo.show().map { people =>
      Ok(Json.toJson(people))
    }
  }
}

/**
  * The create person form.
  *
  * Generally for forms, you should define separate objects to your models, since forms very often need to present data
  * in a different way to your models.  In this case, it doesn't make sense to have an id parameter in the form, since
  * that is generated once it's created.
  */
case class CreateUserForm(name: String, phone: Int, email: String, age: Int)
