import akka.actor.{ActorSystem, Props}
import com.amazonaws.services.dynamodbv2.model.TableDescription

import scala.util.Try

object Main extends App {
  val list = Seq.range(1, 100)
  val system = ActorSystem("HogeActorSystem")
  val hogeActor = system.actorOf(Props[HogeActor], "hogeActor")
  val service: DynamoDBService = new DynamoDBService("http://localhost:9999", "ProdactionCatalog")
  send(list)
//  delete(list)

  def updateTable(tableName: String): Try[TableDescription] = service.updateTable(tableName)
  def deleteTable(tableName: String): Try[Unit] = service.deleteTable(tableName)
  def findAllTables: Try[Unit] = for {
    result <- service.findAllTables
  } yield {
    while (result.iterator().hasNext) {
      println(result.iterator.next.getTableName)
    }
  }

  def send(data: Seq[Int]) = {
    println("send start!!")
    data.foreach(x => {
      hogeActor ! ("put", x)
    })
  }

  def delete(data: Seq[Int]) = {
    println("delete start!!")
    list.foreach(x => {
      hogeActor ! ("delete", x)
    })
  }
}


