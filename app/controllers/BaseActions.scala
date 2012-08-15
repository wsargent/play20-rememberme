package controllers

import play.api.mvc._

import authentication.Context

/**
 * Some basic actions that expose the Context to the internal action...
 */
trait BaseActions {

  def Open(f: Context[AnyContent] => Result): Action[AnyContent] =
    Open(BodyParsers.parse.anyContent)(f)

  def Open[A](p: BodyParser[A])(f: Context[A] => Result): Action[A] =
    Action(p)(req => {
      f(reqToCtx(req))
    })

  protected def reqToCtx[A](req: Request[A]): Context[A] = req.asInstanceOf[Context[A]]

  protected def reqToCtx(req: RequestHeader): Context[_] = req.asInstanceOf[Context[_]]

}
