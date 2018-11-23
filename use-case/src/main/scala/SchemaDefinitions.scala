import sangria.schema._

object SchemaDefinitions {
  val ArticleType = ObjectType("Article", "記事",
    fields[ArticleRepository, Article](
      Field("id", StringType,
        Some("articleId"),
        resolve = _.value.id
      ),
      Field("title", StringType,
        Some("title 名"),
        resolve = _.value.title
      ),
      Field("author", OptionType(StringType),
        Some("author name"),
        resolve = _.value.author
      ),
      Field("tags", ListType(StringType),
        Some("tags name"),
        resolve = _.value.tags)
    ))
  // val Article = deriveObjectType[ArticleRepository, Article]
  val idArgument = Argument("id", StringType, description = "id")
  val QueryType = ObjectType(
    "Query", fields[ArticleRepository, Unit](
      Field("article", OptionType(ArticleType),
        arguments = idArgument :: Nil,
        resolve = c => c.ctx.findArticleById(c.arg(idArgument))),
      Field("articles", ListType(ArticleType),
        resolve = c => c.ctx.findAllArticles),
    ),
  )
  val ArticleSchema = Schema(QueryType)

  // TODO -nishi Relationとは？
  //  val product = Relation[Product, (Seq[CategoryId], Product), CategoryId]("product-category", _._1, _._2)
  //  val category = Relation[Category, (Seq[ProductId], Category), CategoryId]("category-product", _._1, _._2)


  //  val IdentifiableType = InterfaceType(
  //    "Identifiable",
  //    "Entity tha can be identified",
  //    fields[Unit, Identifiable](
  //      Field("id", IntType, resolve = _.value.id)
  //    )
  //  )
  //
  //  val ProductType = deriveObjectType[Unit, Product](
  //    Interfaces(IdentifiableType),
  //    IncludeMethods("picture")
  //  )
  //
  //  implicit val PictureType = ObjectType[Unit, Picture](
  //    "Picture",
  //    "Ther product picture",
  //    // Unit is Context Type: 全体を通して状態を保持しておきたいものを詰める場所？
  //    fields[Unit, Picture](
  //      Field("width", IntType, resolve = _.value.width),
  //      Field("height", IntType, resolve = _.value.height),
  //      Field("url", OptionType(StringType),
  //        description = Some("Picture CDN Url"),
  //        resolve = _.value.url
  //      )
  //    )
  //  )
  //
  ////  val Id = Argument("id", StringType)
  //
  //  val QueryType = ObjectType("Query", fields[ProductRepository, Unit](
  //    Field("product", OptionType(ProductType),
  //      description = Some("Returns a product with specific `id`. "),
  //      arguments = Id :: Nil,
  //      resolve = c => c.ctx.product(c arg Id)
  //    ),
  //
  //    Field("products", ListType(ProductType),
  //      description = Some("Returns a product with specific `id`. "),
  //      resolve = _.ctx.products
  //    )
  //  ))
}

