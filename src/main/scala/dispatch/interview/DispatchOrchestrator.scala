package dispatch.interview


import cats.effect.{IO, IOApp}
import com.comcast.ip4s.{Hostname, Port}
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.http4s.client.Client
import org.http4s.dsl.io.{->, /, POST, _}
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.{HttpRoutes, Request, Status, Uri}

case class Contact(email: String)
object Contact {
  implicit val codec: Codec[Contact] = deriveCodec[Contact]
}

case class Email(contact:Contact, content: String)
object Email {
  implicit val codec: Codec[Email] = deriveCodec[Email]
}

case class DispatchPayload(stationId: Int, timeInSeconds: Int, notifications: Option[List[Contact]] = None)
object DispatchPayload {
  implicit val codec: Codec[DispatchPayload] = deriveCodec[DispatchPayload]
}

class DispatchEndpoints(http: Client[IO], stationApiUrl: String) {
  def service: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case req@POST -> Root / "dispatchNow" =>
      req.as[DispatchPayload].flatMap { dp =>
        if (dp.timeInSeconds < 600) BadRequest("Asset has minimum dispatch of 600s")

        http.run(Request(POST, Uri.unsafeFromString(s"$stationApiUrl/dispatch/${dp.stationId}/${dp.timeInSeconds}"))).use { res =>
          res.status match {
            case Status.Ok => Ok(s"acknowledged")
          }
        }
      }
  }
}

object DispatchEndpoints extends IOApp.Simple {
  override def run: IO[Unit] =
    (for {
      client <- EmberClientBuilder.default[IO].build
      server <- EmberServerBuilder.default[IO]
        .withHost(Hostname.fromString("127.0.0.1").get)
        .withPort(Port.fromInt(8889).get)
        .withHttpApp(new DispatchEndpoints(client, "https://eu.httpbin.org/anything").service.orNotFound)
        .build
    } yield server).useForever
}
