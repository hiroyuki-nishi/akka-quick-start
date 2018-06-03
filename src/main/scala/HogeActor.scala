import akka.actor.Actor

class HogeActor extends Actor {
  val service: DynamoDBService = new DynamoDBService("http://localhost:9999", "ProdactionCatalog")

  override def receive: Receive = {
    case ("put", n: Int)  =>
      service.put(n, "HOGE")
      println(n)
    case ("delete", n: Int)  =>
      service.delete(n)
      println(n)
  }
}

