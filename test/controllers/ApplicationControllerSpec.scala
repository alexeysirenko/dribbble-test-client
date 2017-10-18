package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.ws.WSClient
import play.api.test._
import play.api.test.Helpers._

class ApplicationControllerSpec extends PlaySpec with GuiceOneServerPerSuite with Injecting {

  "test server logic" in {
    val wsClient = app.injector.instanceOf[WSClient]
    val myPublicAddress = s"localhost:$port"
    val username = "KreativaStudio"
    val url = s"http://$myPublicAddress/top10?login=$username"
    // await is from play.api.test.FutureAwaits
    val response = await(wsClient.url(url).get())

    response.status mustBe OK
  }

}
