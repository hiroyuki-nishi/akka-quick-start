import BoxOffice.GetTickets
import akka.actor.ActorRef
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class RestApi(system: ActorSystem, timeout: Timeout) extends RestRoutes {
  implicit val requestTimeout = timeout
  implicit def executionContext: ExecutionContextExecutor = system.dispatcher
  def createBoxOfficeActor: ActorRef = system.actorOf(BoxOffice.props, BoxOffice.name)
}

trait RestRoutes extends BoxOfficeApi with EventMarshalling {
  import akka.http.scaladsl.model.StatusCodes._

  def routes = eventRoute ~ eventsRoute
  def eventRoute =
    pathPrefix("events") {
      pathEndOrSingleSlash {
        get {
          // GET /events
          onSuccess(getEvents()) { events =>
            complete(OK)
          }
        }
      }
    }

  def eventsRoute =
    pathPrefix("events" / Segment) { event =>
      pathEndOrSingleSlash {
        post {
          entity(as[EventDescription]) { ed =>
            onSuccess(createEvent(event, ed.tickets)) {
              case BoxOffice.EventCreated(event) => complete(Created -> event.toString)
              case BoxOffice.EventExists =>  {
                val err = Error(s"$event event exists alredy.")
                complete(BadRequest -> err.toString)
              }
            }
          }
        } ~
        get {
          onSuccess(getEvents()) { events =>
            complete(OK, "")
          }
        } ~
        delete {
          onSuccess(cancelEvent(event)) {
            _.fold(complete(NotFound))(x => complete(OK))
          }
        }
      }
    }
}

trait BoxOfficeApi {
  import BoxOffice._

  // Restクラスからimplicitは提供される
  // ExecutionContextはFutureのために必要
  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout
  lazy val boxOffice = createBoxOfficeActor()

  def createBoxOfficeActor(): ActorRef
  // ask関数で子アクターのBoxOfficeActorのCreateEventがFutureで値を返してきたら
  // mapToでsealedTraitのEventResponse(EventCreated)を返す
  def createEvent(event: String, nrOfTickets: Int): Future[EventResponse] =
    boxOffice.ask(CreateEvent(event, nrOfTickets)).mapTo[EventResponse]
  def getEvents(): Future[Events] = boxOffice.ask(GetEvents).mapTo[Events]
  def getEvent(event: String) = boxOffice.ask(GetEvent).mapTo
  def getTickets(event: String, nrOfTickets: Int) = boxOffice.ask(GetTickets).mapTo[TicketSeller.Tickets]
  def cancelEvent(event: String) = boxOffice.ask(CancelEvent(event)).mapTo[Option[Event]]
}
