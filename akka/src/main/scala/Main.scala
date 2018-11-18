import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

  object Main extends App with RequestTimeout {
    val config = ConfigFactory.load()
    val host = config.getString("http.host")
    val port = config.getInt("http.port")

    implicit val system = ActorSystem("my-system")
    // Dispatcher作成
    implicit val ec = system.dispatcher
    implicit val materialize = ActorMaterializer()
    //  // route定義
    val apiRoutes = new RestApi(system, requestTimeout(config)).routes
    // ServerBinding??
    // HTTPサーバーの起動
    val bindingFuture: Future[ServerBinding] = Http().bindAndHandle(apiRoutes, host, port)
    val log =  Logging(system.eventStream, "go-ticks")
    bindingFuture.map { serverBinding =>
      log.info(s"RestApi bound to ${serverBinding.localAddress}")
    }.onComplete {
      case Success(_) => log.info("Success to bind to {}:{}", host, port)
      case Failure(exception) => {
        log.error(exception, "Failed to bind to {}:{}!", host, port)
        print(exception)
        system.terminate()
      }
    }
}

trait RequestTimeout {
  import scala.concurrent.duration.Duration
  def requestTimeout(config: Config): Timeout = {
    val t = config.getString("akka.request-timeout")
    val d = Duration(t)
    FiniteDuration(d.length, d.unit)
  }
}
