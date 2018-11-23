//import sangria.parser.QueryParser
//import sangria.ast.Document
//import sangria.execution.{Executor, QueryAnalysisError}
//import spray.json.{JsObject, JsString, JsValue}
//
//import scala.util.{Failure, Success}
//import akka.http.scaladsl.model.StatusCodes._
//import akka.http.scaladsl.server.Directives._
//import akka.http.scaladsl.server._

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import sangria.ast.Document
import sangria.execution._
import sangria.marshalling.sprayJson._
import sangria.parser.QueryParser
import spray.json.{JsObject, JsString, JsValue}

import scala.concurrent.ExecutionContext
import scala.util.Failure

object GraphQLServer {
  // TODO -nishi DynamoDBにつなげる予定
  //  val repository = ProductRepository
  def endpoint(json: JsValue)(implicit e: ExecutionContext) = {
    val JsObject(fields) = json
    val JsString(query) = fields("query")
    val operation = fields.get("operationName") collect {
      case JsString(op) => op
    }
    val vars = fields.get("variables") match {
      case Some(obj: JsObject) => obj
      case _ => JsObject.empty
    }

    QueryParser.parse(query) match {
      case Success(queryAst) =>
        complete(excecuteGraphQLQuery(queryAsc, operation, vars))
      case Failure(error) =>
        complete(BadRequest, JsObject("error" -> JsString(error.getMessage)))
    }
  }

  def excecuteGraphQLQuery(query: Document, op: Option[String], vars: JsObject)(implicit e: ExecutionContext) =
    Executor.execute(schema, query, new ProductRepository, variables = vars, operationName = op)
    .map(OK -> _)
    .recover{
      case error: QueryAnalysisError => BadRequest -> error.resolveError
    }

}
