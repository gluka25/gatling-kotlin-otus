package otus

import io.gatling.javaapi.core.*

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.http.HttpDsl
import io.gatling.javaapi.http.HttpDsl.*
import java.time.Duration

class CountriesSimulation : Simulation()  {
    private val httpProtocol = HttpDsl.http
        .baseUrl("https://wft-geo-db.p.rapidapi.com")
        .header("x-rapidapi-key", "")
        .header("x-rapidapi-host", "wft-geo-db.p.rapidapi.com")

    private val scn = CoreDsl.scenario("Get countries")
        .exec(
            HttpDsl.http("request_get_countries")
            .get("/v1/geo/countries")
        )
        .pause(1)

    private val spbCityid = "Q656"
    private val radius = "100"
    private val scn2 = CoreDsl.scenario("Get cities near city")
        .exec(
            HttpDsl.http("request_get_cities_near_city")
                .get("/v1/geo/cities/$spbCityid/nearbyCities?radius=$radius")
        )
        .pause(5)

    init {
        setUp(
            scn.injectOpen(CoreDsl.constantUsersPerSec(2.0).during(Duration.ofSeconds(10))).protocols(httpProtocol)
            .throttle(
            reachRps(2).during(10),
            holdFor(Duration.ofSeconds(2)),
            jumpToRps(1),
            holdFor(Duration.ofSeconds(5))
            ),
            scn2.injectOpen(atOnceUsers(1)).protocols(httpProtocol)
        ).assertions(global().responseTime().max().lt(900))
    }
}