import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.{Done, NotUsed}
import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString

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

  def run(): Unit = source.runForeach(println)
}
