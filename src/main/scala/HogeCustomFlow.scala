import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}

class HogeCustomFlow extends GraphStage[FlowShape[Int, Int]] {
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

