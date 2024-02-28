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
import cats.implicits._

case class Contact(email: String)
object Contact {
  given codec: Codec[Contact] = deriveCodec[Contact]
}

case class Email(contact:Contact, content: String)
object Email {
  given codec: Codec[Email] = deriveCodec[Email]
}

case class DispatchPayload(stationId: Int, timeInSeconds: Int, notifications: Option[List[Contact]] = None)
object DispatchPayload {
  given codec: Codec[DispatchPayload] = deriveCodec[DispatchPayload]
}


 trait IEmailService {
  def sendEmail(content: List[Email]):IO[List[String]]
}

class EmailService(http: Client[IO], stationApiUrl: String) extends IEmailService {


  override def sendEmail(content: List[Email]): IO[List[String]] = {
    content.map { obj =>
      http.expect[String](Request[IO](POST, Uri.unsafeFromString(s"$stationApiUrl/email")).withEntity(
        obj))
    }.sequence
  }
}
class DispatchEndpoints(http: Client[IO], 
                        stationApiUrl: String,
                         emailService:IEmailService) {

  def service: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case req@POST -> Root / "dispatchNow" =>
      req.as[DispatchPayload].flatMap { dp =>
        if (dp.timeInSeconds < 60 * 10 ) {
         BadRequest("Asset has minimum dispatch of 600s")
        }else if (dp.timeInSeconds > 60 * 120){
          BadRequest("Asset has max dispatch of 120m")
        }
        else {

          http.run(Request(POST, Uri.unsafeFromString(s"$stationApiUrl/dispatch/${dp.stationId}/${dp.timeInSeconds}")))
            .use { res =>
              res.status match {
                case Status.Ok => {

                  val emailObjects = dp.notifications.toList.flatMap(notOp => notOp.map{not =>
                    Email(contact = not, content = "dispach ok")
                  })



                  emailService.sendEmail(emailObjects).handleError {
                    case e:Exception => {
                      println(e)
                    }

                  }.flatMap(_ =>    {
                    Ok(s"acknowledged")
                  }
                  )
                }

                case Status.BadRequest => BadRequest("fail")
              }
            }

        }

      }

  }
}

object DispatchEndpoints extends IOApp.Simple {

  var emailService:IEmailService = null
  override def run: IO[Unit] =
    (for {
      client <- EmberClientBuilder.default[IO].build
      server <- EmberServerBuilder.default[IO]
        .withHost(Hostname.fromString("127.0.0.1").get)
        .withPort(Port.fromInt(8889).get)
        .withHttpApp(new DispatchEndpoints(client, "https://eu.httpbin.org/anything",
          null
        ).service.orNotFound)
        .build
    } yield server).useForever
}
