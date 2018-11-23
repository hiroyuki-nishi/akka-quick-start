import sangria.execution.deferred.{Relation, Relation, Fetcher, RelationIds}
import sangria.schema._

object SchemaDefinitions {
  import QueryModel._
  import sangria.macros.derive._

  // TODO -nishi Relationとは？
//  val product = Relation[Product, (Seq[CategoryId], Product), CategoryId]("product-category", _._1, _._2)
//  val category = Relation[Category, (Seq[ProductId], Category), CategoryId]("category-product", _._1, _._2)

  val IdentifiableType = InterfaceType(
    "Identifiable",
    "Entity tha can be identified",
    fields[Unit, Identifiable](
      Field("id", IntType, resolve = _.value.id)
    )
  )

  val ProductType = deriveObjectType[Unit, Product](
    Interfaces(IdentifiableType),
    IncludeMethods("picture")
  )

  implicit val PictureType = ObjectType[Unit, Picture](
    "Picture",
    "Ther product picture",
    // Unit is Context Type: 全体を通して状態を保持しておきたいものを詰める場所？
    fields[Unit, Picture](
      Field("width", IntType, resolve = _.value.width),
      Field("height", IntType, resolve = _.value.height),
      Field("url", OptionType(StringType),
        description = Some("Picture CDN Url"),
        resolve = _.value.url
      )
    )
  )

//  val Id = Argument("id", StringType)

  val QueryType = ObjectType("Query", fields[ProductRepository, Unit](
    Field("product", OptionType(ProductType),
      description = Some("Returns a product with specific `id`. "),
      arguments = Id :: Nil,
      resolve = c => c.ctx.product(c arg Id)
    ),

    Field("products", ListType(ProductType),
      description = Some("Returns a product with specific `id`. "),
      resolve = _.ctx.products
    )
  ))
}