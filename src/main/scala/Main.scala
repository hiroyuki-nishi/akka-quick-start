import com.amazonaws.services.dynamodbv2.model.TableDescription

import scala.util.Try

object Main extends App {
  val service: DynamoDBService = new DynamoDBService("http://localhost:9999", "ProdactionCatalog")
  service.put(1, "HOGE")

  def updateTable(tableName: String): Try[TableDescription] = service.updateTable(tableName)
  def deleteTable(tableName: String): Try[Unit] = service.deleteTable(tableName)
  def findAllTables: Try[Unit] = for {
    result <- service.findAllTables
  } yield {
    while (result.iterator().hasNext) {
      println(result.iterator.next.getTableName)
    }
  }
}


