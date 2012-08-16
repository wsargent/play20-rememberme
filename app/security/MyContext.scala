package security

import models.User
import play.api.mvc.{Request, WrappedRequest}

/**
 * An example of a wrapped context.
 *
 * @author wsargent
 * @since 8/15/12
 */
case class MyContext[A](request: Request[A], me: Option[User]) extends WrappedRequest[A](request) with com.tersesystems.authentication.Context[A, User]
