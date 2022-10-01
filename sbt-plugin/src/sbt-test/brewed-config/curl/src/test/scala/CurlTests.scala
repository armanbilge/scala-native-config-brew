import org.junit.Assert._
import org.junit.Test

import scala.scalanative.unsafe._

import curl._

class CurlTests {

  @Test def testVersion(): Unit = {
    val prefix = "libcurl/7."
    assertEquals(prefix, fromCString(curl_version()).take(prefix.length))
  }

}
