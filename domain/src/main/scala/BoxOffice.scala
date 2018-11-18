import BoxOffice._
import scala.concurrent.Future

import akka.actor._
import akka.util.Timeout

object BoxOffice {
  def props(implicit timeout: Timeout) = Props(new BoxOffice)
  def name = "boxOffice"

  case class CreateEvent(name: String, tickets: Int)
  case class GetEvent(name: String)
  case object GetEvents
  case class GetTickets(event: String, tickets: Int)
  case class CancelEvent(name: String)
  case class Event(name: String, tickets: Int)
  case class Events(events: Vector[Event])
  sealed trait EventResponse
  case class EventCreated(event: Event) extends EventResponse
  case object EventExists extends EventResponse
}

class BoxOffice(implicit val timeout: Timeout) extends Actor {
  import context._
  def createTicketSeller(name: String) = context.actorOf(TicketSeller.props(name), name)

  override def receive: Receive = {
    case CreateEvent(name, tickets) =>
      def create(): Unit = {
        val ticketSellerActor = createTicketSeller(name)
        val newTickets = (1 to tickets).map(TicketSeller.Ticket(_)).toVector
        ticketSellerActor ! TicketSeller.Add(newTickets)
        sender() ! EventCreated(Event(name, tickets))
      }
      context.child(name).fold(create())(_ => sender() ! EventExists)
      // TODO
      // sender() ! と to sender()は一緒？
    case GetTickets(event, tickets) =>
      def notFound() = sender() ! TicketSeller.Tickets(event)
      // 子アクター(TicketSeller Actor)に対してTicketSeller.Buy(tickets)
      // TODO -nishi forwadでなく sender() ! TicketSeller.Buy(tickets)でも同様？ 子アクターが直接値を返す場合にforward?
      def buy(child: ActorRef): Unit = child.forward(TicketSeller.Buy(tickets))
      context.child(event).fold(notFound())(buy)
    case GetEvents =>
      import akka.pattern.ask
      import akka.pattern.pipe

      // Futureには暗黙的にtimeoutが必要
      def getEvents: Iterable[Future[Option[Event]]] = context.children.map { child =>
        self.ask(GetEvent(child.path.name)).mapTo[Option[Event]]
      }

      def convertToEvents(f: Future[Iterable[Option[Event]]]): Future[Events] =
        f.map(_.flatten).map(l => Events(l.toVector))
      // pipeは処理の完了時に値をFutureで包んでアクターに送信する。
      // sender()はRestAPIになる。
      pipe(convertToEvents(Future.sequence(getEvents))) to sender()
    case GetEvent(event) =>
      def notFound(): Unit = sender() ! None
      def getEvent(child: ActorRef): Unit = child.forward(TicketSeller.GetEvent)
      context.child(event).fold(notFound())(getEvent)
    case CancelEvent(event) =>

//          case CancelEvent(event) =>
//      def notFound() = sender() ! None
//      def cancelEvent(child: ActorRef) = child forward TicketSeller.Cancel
//      context.child(event).fold(notFound())(cancelEvent)
  }
}
