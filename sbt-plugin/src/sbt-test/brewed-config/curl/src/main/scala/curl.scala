import scala.scalanative.unsafe._

@link("curl")
@extern
object curl {
  def curl_version(): Ptr[CChar] = extern
}
