package controllers

import play.api.mvc._

import models.User
import security.MyContext

/**
 * Some basic actions that expose the Context to the internal action...
 */
trait BaseActions {

  def Open(f: MyContext[AnyContent] => Result): Action[AnyContent] =
    Open(BodyParsers.parse.anyContent)(f)

  def Open[A](p: BodyParser[A])(f: MyContext[A] => Result): Action[A] =
    Action(p)(req => {
      f(reqToCtx(req))
    })

  protected def reqToCtx[A](req: Request[A]): MyContext[A] = req.asInstanceOf[MyContext[A]]

  protected def reqToCtx(req: RequestHeader): MyContext[_] = req.asInstanceOf[MyContext[_]]

}
