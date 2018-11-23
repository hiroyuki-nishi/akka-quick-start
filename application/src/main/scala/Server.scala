import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import spray.json.JsValue

import Console._
import scala.concurrent.Await

object Server extends App {
  import scala.concurrent.duration._
  import actorSystem.dispatcher
  val PORT = 8080
  implicit val actorSystem = ActorSystem("graphql-server")
  implicit val materializer = ActorMaterializer()

  private def logger(message: String, color: String = GREEN): Unit = println(color + message)
  private def shutdown(): Unit = {
    logger("Terminating...", YELLOW)
    actorSystem.terminate()
    Await.result(actorSystem.whenTerminated, 30 seconds)
    logger("Terminated... Bye", YELLOW)
  }

  val route: Route =
    (post & path("graphql")) {
      // TODO -nishi 型指定の方法は？
      entity(as[JsValue]){ requestJson =>
        GraphQLServer.endpoint(requestJson)
      }
    } ~ {
      getFromResource("graphiql.html")
    }
  Http().bindAndHandle(route, "0.0.0.0", PORT)
  logger(s"open a browser with URL: http://localhost:$PORT")
  logger(s"or POST queries to http://localhost:$PORT/graphql")
  logger("Starting GRAPHQL server...")
  scala.sys.addShutdownHook(() -> shutdown())
}
