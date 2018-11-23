import akka.http.scaladsl.model.StatusCode
import sangria.ast.Document
import sangria.execution.deferred.DeferredResolver
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.parser.{QueryParser, SyntaxError}
import sangria.parser.DeliveryScheme.Try

import scala.concurrent.{ExecutionContext, Future}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer

import scala.util.control.NonFatal
import scala.util.{Failure, Success}
import GraphQLRequestUnmarshaller._
import sangria.marshalling.sprayJson._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.{JsObject, JsString, JsValue}

object Server extends App {
  implicit val system = ActorSystem("sangria-server")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  def executeGraphQL(query: Document, operationName: Option[String], variables: JsObject)(implicit e: ExecutionContext): Future[(StatusCode, JsValue)] =
    Executor.execute(
      SchemaDefinitions.ArticleSchema,
      query,
      new ArticleRepository,
      variables = variables,
      operationName = operationName)
      .map(OK → _)
      .recover {
        case error: QueryAnalysisError ⇒ BadRequest → error.resolveError
        case error: ErrorWithResolver ⇒ InternalServerError → error.resolveError
      }


  def graphQLEndpoint(requestJson: JsValue) = {
    val JsObject(fields) = requestJson
    val JsString(query) = fields("query")
    val operation = fields.get("operationName") collect {
      case JsString(op) ⇒ op
    }
    val variables = fields.get("variables") match {
      case Some(obj: JsObject) ⇒ obj
      case _ ⇒ JsObject.empty
    }

    QueryParser.parse(query) match {
      case Success(queryAst) ⇒
        complete(executeGraphQL(queryAst, operation, variables))
      case Failure(error) ⇒
        // TODO -nishi 詳細なエラーをだす
        complete(BadRequest, JsObject("error" → JsString(error.getMessage)))
    }
  }

  val route: Route =
    (post & path("graphql")) {
      entity(as[JsValue]){ requestJson =>
        graphQLEndpoint(requestJson)
      }
    } ~ {
      getFromResource("assets/graphiql.html")
    }

  Http().bindAndHandle(route, "0.0.0.0", sys.props.get("http.port").fold(8080)(_.toInt))
}
