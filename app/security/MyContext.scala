package security

import models.User
import play.api.mvc.{Request, WrappedRequest}

/**
 * An example of a context class that wraps a request.  You should put your
 *
 */
case class MyContext[A](request: Request[A], me: Option[User]) extends WrappedRequest[A](request) with com.tersesystems.authentication.Context[A, User]
