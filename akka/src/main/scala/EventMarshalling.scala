import spray.json.DefaultJsonProtocol

case class EventDescription(tickets: Int) {
  require(tickets > 0)
}

case class TicketRequest(tickets: Int) {
  require(tickets > 0)
}

case class Error(message: String)

trait EventMarshalling extends DefaultJsonProtocol {
  import BoxOffice._
  import TicketSeller._

  implicit val eventDescriptionFormat = jsonFormat1(EventDescription)
  implicit val eventFormat = jsonFormat2(Event)
//  implicit val eventsFormat = jsonFormat1(Events)
  implicit val ticketRequestFormat = jsonFormat1(TicketRequest)
  implicit val ticketFormat = jsonFormat1(Ticket)
  implicit val ticketsFormat = jsonFormat2(Tickets)
  implicit val errorFormat = jsonFormat1(Error)
}
