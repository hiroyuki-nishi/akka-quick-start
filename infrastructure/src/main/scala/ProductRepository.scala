import QueryModel.Product
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

class ProductRepository() {
  private val Products = List(
    Product(1, "Cheesecake", "Tasty"),
    Product(2, "Health Potion", "+50 HP")
  )

  def product(id: String): Option[Product] =
    Products find (_.id == id)

  def products: List[Product] = Products
}

//object ProductRepository {
//  def createDatabase() = {
//    // TODO -nishi DynamoDBに置き換える予定
//    val db = Database.forConfig("h2mem")
//    Await.result(db.run(databaseSetup), 10 seconds)
//    new ProductRepository(db)
//  }
//}
