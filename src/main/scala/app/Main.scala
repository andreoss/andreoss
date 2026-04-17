package app

import java.sql.Timestamp
import javax.sql.DataSource

import zio._

import zio.http._

import io.getquill._
import io.getquill.jdbczio.Quill
import org.postgresql.ds.PGSimpleDataSource

object Ctx extends PostgresZioJdbcContext(Literal)

import Ctx._

val pgUser = sys.env.getOrElse("POSTGRES_USER", "postgres")
val pgPass = sys.env.getOrElse("POSTGRES_PASSWORD", "postgres")
val pgHost = sys.env.getOrElse("POSTGRES_HOST", "localhost")
val pgDb   = sys.env.getOrElse("POSTGRES_DB", "postgres")
val pgUrl  = s"jdbc:postgresql://$pgHost:5432/$pgDb"

val dataSource: ZLayer[Any, Throwable, DataSource] = Quill.DataSource.fromDataSource {
  val ds = new PGSimpleDataSource()
  ds.setUrl(pgUrl)
  ds.setUser(pgUser)
  ds.setPassword(pgPass)
  ds
}

val handler = Handler.fromFunctionZIO[(Path, Request)] { (_, _) =>
  (for ts <- Ctx.run(quote(infix"SELECT current_timestamp".as[Query[Timestamp]])).map(_.head)
  yield Response.text(s"""{"status":"OK","version":"1.0.0","timestamp":"$ts"}"""))
    .catchAll(e => ZIO.succeed(Response.internalServerError(s"""{"error":"${e.getMessage}"""")))
}

val routes: Routes[DataSource, Response] = Routes.singleton(handler)

object Main extends ZIOAppDefault:
  def run = Server.serve(routes).provide(Server.default, dataSource)
