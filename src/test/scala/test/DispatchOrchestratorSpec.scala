package test

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.catsSyntaxOptionId
import dispatch.interview.{Contact, DispatchEndpoints, DispatchPayload, Email, IEmailService}
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.client.Client
import org.http4s.dsl.io.{->, /, POST, *}
import org.http4s.implicits.uri
import org.http4s.{HttpRoutes, Method, Request, Response}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers.shouldBe


class FakeEmailService extends IEmailService {

  var lastEmailRes : List[String] = Nil
  override def sendEmail(content: List[Email]) = {
    lastEmailRes = content.map(_ => "ok")
    IO.pure(content.map(_ => "ok"))
  }

}
class DispatchOrchestratorSpec extends AnyFunSpec {
  private def stubClient(stationResponse: IO[Response[IO]]): Client[IO] = Client.fromHttpApp {
    HttpRoutes.of[IO] {
      case POST       -> Root / "dispatch" / stationId / IntVar(durationInSeconds) => stationResponse
      case req @ POST -> Root / "email" => req.as[Email].flatMap(e => Ok(s"Email sent ok $e"))
    }.orNotFound
  }

  private val ok: IO[Response[IO]] = Ok(s"OK - station dispatched")

  private val fakeEmailService = new FakeEmailService()
  private val dispatchOk = new DispatchEndpoints(stubClient(ok), "", fakeEmailService)
  private val dispatchKO = new DispatchEndpoints(stubClient(BadRequest()), "", fakeEmailService)


  it("should dispatch when > min and < max dispatch time") {
    val request = Request[IO](Method.POST, uri"/dispatchNow").withEntity(DispatchPayload(100, 600))

    dispatchOk
      .service
      .orNotFound
      .run(request).unsafeRunSync().status shouldBe Ok
  }

  it("should not allow a dispatch less than minimum dispatch time (600)") {
    val request = Request[IO](Method.POST, uri"/dispatchNow").withEntity(DispatchPayload(100, 599))

    dispatchOk
      .service
      .orNotFound
      .run(request).unsafeRunSync().status shouldBe BadRequest
  }

  it("should return 400 when dipspach fails") {
    val request = Request[IO](Method.POST, uri"/dispatchNow").withEntity(DispatchPayload(100, 650))

    dispatchKO
      .service
      .orNotFound
      .run(request).unsafeRunSync().status shouldBe BadRequest
  }

  it("should not allow a dispatch less than max dispatch time (120m)") {
    val request = Request[IO](Method.POST, uri"/dispatchNow").withEntity(DispatchPayload(100, 120*60 + 1))

    dispatchOk
      .service
      .orNotFound
      .run(request).unsafeRunSync().status shouldBe BadRequest
  }

  it("should email optional contact on dispatch") {
    val request = Request[IO](Method.POST, uri"/dispatchNow")
      .withEntity(DispatchPayload(100, 700, List(Contact("sowen@kiwipowered.com")).some))

    dispatchOk
      .service
      .orNotFound
      .run(request).unsafeRunSync().status shouldBe Ok


    assert(fakeEmailService.lastEmailRes.length === 1)
    assert(fakeEmailService.lastEmailRes === "ok"::Nil)


  }


}
