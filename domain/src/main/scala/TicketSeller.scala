import akka.actor.{Actor, PoisonPill, Props}

object TicketSeller {
  def props(event: String) = Props(new TicketSeller(event))
  case class Add(tickets: Vector[Ticket])
  case class Buy(tickets: Int)
  case class Ticket(id: Int)
  case class Tickets(event: String, entries: Vector[Ticket] = Vector.empty[Ticket])
  case object GetEvent
  case object Cancel
}

class TicketSeller(event: String) extends Actor {
  import TicketSeller._

  var tickets = Vector.empty[Ticket]
  def receive = {
    case Add(newTickets) => tickets = tickets ++ newTickets
    case Buy(buyTickets) =>
      val takeTickets = tickets.take(buyTickets).toVector
      if (takeTickets.size >= buyTickets) {
        sender() ! Tickets(event, takeTickets)
        tickets = tickets.drop(buyTickets)
      } else {
        sender() ! Tickets(event)
      }
    case GetEvent => sender() ! Some(BoxOffice.Event(event, tickets.size))
    case Cancel =>
      sender() ! Some(BoxOffice.Event(event ,tickets.size))
      self ! PoisonPill
  }
}
