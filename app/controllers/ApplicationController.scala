package controllers

import play.api._
import play.api.mvc._

trait ApplicationController extends Controller {

  def index = Action {
    Ok(views.html.index("Image Processing & Classification Webservice Template"))
  }

}

object ApplicationController extends ApplicationController
