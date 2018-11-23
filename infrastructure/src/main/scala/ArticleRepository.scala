class ArticleRepository() {
  def findArticleById(id: String): Option[Article] = ArticleRepository.articles.find(_.id == id)
  def findAllArticles = ArticleRepository.articles
}

object ArticleRepository {
  val articles = List(
    Article("1", "AWS", Some("hoge"), List("aws", "hoge")),
    Article("2", "AWS", Some("fuga"), List("azure", "fuga"))
  )
}

