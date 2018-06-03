import java.util

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document._
import com.amazonaws.services.dynamodbv2.model._

import scala.util.Try


class DynamoDBService(private val url: String, private val tableName: String) {
  val client = AmazonDynamoDBClientBuilder.standard().build()
  val dynamoDB = new DynamoDB(client)
  val table: Table = new DynamoDB(client).getTable(tableName)
  private val AttrId = "Id"
  private val AttrTitle= "Title"

  def apply(url: String, tableName: String): DynamoDBService = {
    new DynamoDBService(url, tableName)
  }

  def getTable(tableName: String): Try[Table] = Try {
    dynamoDB.getTable(tableName)
  }

  def createTable(tableName: String): Try[TableDescription] = Try {
    val attributeDefinitions = new util.ArrayList[AttributeDefinition]()
    attributeDefinitions.add(new AttributeDefinition().withAttributeName("Id").withAttributeType("N"))

    val keySchema = new util.ArrayList[KeySchemaElement]()
    keySchema.add(new KeySchemaElement().withAttributeName("Id").withKeyType(KeyType.HASH))

    val request = new CreateTableRequest()
      .withTableName(tableName)
      .withKeySchema(keySchema)
      .withAttributeDefinitions(attributeDefinitions)
      .withProvisionedThroughput(new ProvisionedThroughput()
        .withReadCapacityUnits(5L)
        .withWriteCapacityUnits(6L))

    val table = dynamoDB.createTable(request)
    table.waitForActive()
  }

  def updateTable(tableName: String): Try[TableDescription] =
    for {
      t <- getTable(tableName)
    } yield {
      t.updateTable(new ProvisionedThroughput()
        .withReadCapacityUnits(1L)
        .withWriteCapacityUnits(1L))
    }

  def deleteTable(tableName: String): Try[Unit] =
    for {
      t <- getTable(tableName)
    } yield {
      t.delete()
      t.waitForDelete()
    }

  def findAllTables: Try[TableCollection[ListTablesResult]] = Try(dynamoDB.listTables())

  def put(id: Int, title: String): Try[PutItemOutcome] = for {
    t <- getTable(tableName)
  } yield {
    val item = new Item().withPrimaryKey(AttrId, id)
      .withString(AttrTitle, title)
   t.putItem(item)
  }
}


object DynamoDBService {
}

