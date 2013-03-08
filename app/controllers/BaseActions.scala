package controllers

import play.api.mvc._
import play.api.mvc.Results._

import models.User
import security.{MyActionHandler, MyContext}

/**
 * Some basic actions that expose the Context to the internal action...
 */
trait BaseActions {

  def Open(f: MyContext[AnyContent] => Result): Action[AnyContent] =
    Open(BodyParsers.parse.anyContent)(f)

  def Open[A](p: BodyParser[A])(f: MyContext[A] => Result): Action[A] =
    MyActionHandler(p)(req => {
      val ctx = reqToCtx(req)
      f(ctx)
    })

  def Auth[A](p: BodyParser[A])(f: MyContext[A] => User => Result): Action[A] =
    MyActionHandler(p)(req => {
      val ctx = reqToCtx(req)
      ctx.me.map(me => f(ctx)(me)).getOrElse(AuthController.authenticationFailed(ctx))
    })

  def reqToCtx[A](req: Request[A]): MyContext[A] = req.asInstanceOf[MyContext[A]]
}
