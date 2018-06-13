import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import akka.{Done, NotUsed}

import scala.concurrent.{ExecutionContextExecutor, Future}

class HogeFlow {
  implicit val system: ActorSystem = ActorSystem("HogeFlow")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val source: Source[Int, NotUsed] = Source(1 to 100)

  def run(): Unit = {
    val done: Future[Done] = source.runForeach(i ⇒ println(i))(materializer)
    done.onComplete(_ => system.terminate())
  }
}

class DynamoDbFlow {
  implicit val system: ActorSystem = ActorSystem("DynamoDbFlow")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val source: Source[Int, NotUsed] = Source(1 to 100)
  lazy val customeFlow: Flow[Int, Int, NotUsed] = Flow.fromGraph(new CustomStream())
  //TODO -nishi Future[Done]って何？
  val sink1: Sink[Int, Future[Done]] = Sink.foreach[Int](x => println(x))
  val sink2: Sink[Int, Future[Done]] = Sink.foreach[Int](x => println(x))
  val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val broadcast = b.add(Broadcast[Int](2))
    source ~> broadcast.in
//    broadcast.out(0) ~> Flow[Int].map(x => x) ~> sink1
    broadcast.out(0) ~> customeFlow ~> sink1
    broadcast.out(1) ~> Flow[Int].map(x => x) ~> sink1
    ClosedShape
  })

  def run(): Unit = g.run()
}

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}

class CustomStream extends GraphStage[FlowShape[Int, Int]] {
  val in = Inlet[Int]("CostomStream.in")
  val out = Outlet[Int]("CostomStream.out")

  override val shape = FlowShape.of(in, out)

  override def createLogic(attr: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val hoge = grab(in)
          println(in)
          push(out, hoge)
        }
      })
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }
}
