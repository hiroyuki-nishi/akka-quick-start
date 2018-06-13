import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import akka.{Done, NotUsed}
import scala.concurrent.{ExecutionContextExecutor, Future}


class DynamoDbFlow {
  implicit val system: ActorSystem = ActorSystem("DynamoDbFlow")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val source: Source[Int, NotUsed] = Source(1 to 100)
//  lazy val customeFlow: Flow[Int, Int, NotUsed] = Flow.fromGraph(new HogeCustomFlow())
  lazy val flow: Flow[Int, Int, NotUsed] = Flow.fromGraph(GraphDSL.create() { implicit b: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._

    val broadcast = b.add(Broadcast[Int](1))
    val merge = b.add(Merge[Int](1))

    broadcast.out(0).map(x => { println(x); x}) ~> merge.in(0)
    FlowShape(broadcast.in, merge.out)
  })
  val sink1: Sink[Int, Future[Done]] = Sink.foreach[Int](x => println(x))
  val sink2: Sink[Int, Future[Done]] = Sink.foreach[Int](x => println(x))
  val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val broadcast = b.add(Broadcast[Int](2))
    source ~> broadcast.in
//    broadcast.out(0) ~> Flow[Int].map(x => x) ~> sink1
    broadcast.out(0) ~> flow ~> sink1
//    broadcast.out(0) ~> customeFlow ~> sink1
    broadcast.out(1) ~> Flow[Int].map(x => x) ~> sink1
    ClosedShape
  })

  def run(): Unit = g.run()
}

