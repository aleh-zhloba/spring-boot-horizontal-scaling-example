package app.hopsify.support

import com.icegreen.greenmail.configuration.GreenMailConfiguration
import com.icegreen.greenmail.junit5.GreenMailExtension
import com.icegreen.greenmail.util.ServerSetupTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.messaging.converter.StringMessageConverter
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import org.testcontainers.Testcontainers
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import reactor.core.publisher.Flux
import java.lang.reflect.Type

abstract class AbstractMultiInstanceTest {
    companion object {
        // Postgres
        private const val POSTGRES_DB_NAME = "test"
        private const val POSTGRES_USERNAME = "postgres"
        private const val POSTGRES_PASSWORD = "postgres"
        private val network: Network = Network.newNetwork()
        private val postgresContainer: PostgreSQLContainer<*> =
            PostgreSQLContainer("postgres:16.0")
                .withDatabaseName(POSTGRES_DB_NAME)
                .withUsername(POSTGRES_USERNAME)
                .withPassword(POSTGRES_PASSWORD)
                .withNetwork(network)
                .withNetworkAliases("database")
        lateinit var jdbcTemplate: JdbcTemplate

        // Mail server
        @JvmField
        @RegisterExtension
        val greenMail: GreenMailExtension =
            GreenMailExtension(ServerSetupTest.SMTP)
                .withConfiguration(
                    GreenMailConfiguration.aConfig()
                        .withUser("greenmail", "greenmail")
                )
                .withPerMethodLifecycle(false)

        // Application containers
        private const val DOCKER_IMAGE_NAME = "hopsify-spring-horizontal-scaling:1.0-SNAPSHOT"
        private val firstInstance: SpringAppContainer<*> =
            SpringAppContainer(DOCKER_IMAGE_NAME, 8080)
                .withNetwork(network)
        private val secondInstance: SpringAppContainer<*> =
            SpringAppContainer(DOCKER_IMAGE_NAME, 9090)
                .withNetwork(network)

        // Http clients
        lateinit var firstInstanceClient: WebTestClient
        lateinit var secondInstanceClient: WebTestClient

        @Volatile
        private var roundRobinState: Int = 0

        @JvmStatic
        protected val roundRobinClient: WebTestClient
            get() =
                (if (roundRobinState == 0) firstInstanceClient else secondInstanceClient)
                    .also { roundRobinState = 1 - roundRobinState }

        // Stomp sessions
        private val stompClient =
            WebSocketStompClient(StandardWebSocketClient())
                .apply {
                    messageConverter = StringMessageConverter()
                }
        lateinit var firstInstanceStompSession: StompSession
        lateinit var secondInstanceStompSession: StompSession

        @JvmStatic
        @BeforeAll
        fun runContainers() {
            postgresContainer.start()
            val postgresDatasourcePropertiesEnv: Map<String, String> =
                mapOf(
                    "SPRING_DATASOURCE_URL" to "jdbc:postgresql://database:5432/${postgresContainer.databaseName}",
                    "SPRING_DATASOURCE_USERNAME" to postgresContainer.username,
                    "SPRING_DATASOURCE_PASSWORD" to postgresContainer.password
                )

            Testcontainers.exposeHostPorts(3025) // Green mail

            firstInstance.withEnv(postgresDatasourcePropertiesEnv).start()
            secondInstance.withEnv(postgresDatasourcePropertiesEnv).start()

            firstInstanceClient = firstInstance.webClient()
            secondInstanceClient = secondInstance.webClient()

            jdbcTemplate =
                JdbcTemplate(
                    DriverManagerDataSource(postgresContainer.getJdbcUrl(), POSTGRES_USERNAME, POSTGRES_PASSWORD).apply {
                        setDriverClassName("org.postgresql.Driver")
                    }
                )

            firstInstanceStompSession = firstInstance.stompSession()
            secondInstanceStompSession = secondInstance.stompSession()
        }

        protected fun SpringAppContainer<*>.webClient(): WebTestClient =
            WebTestClient.bindToServer()
                .baseUrl("http://localhost:${this.serverPortMapping}")
                .build()

        protected fun SpringAppContainer<*>.stompSession(): StompSession =
            stompClient.connectAsync(
                "ws://localhost:${this.serverPortMapping}/stomp",
                object : StompSessionHandlerAdapter() {}
            ).get()
    }

    protected fun waitSync() = Thread.sleep(100)

    protected fun StompSession.subscribe(destination: String): Flux<String> =
        Flux.create { sink ->
            try {
                this.subscribe(
                    destination,
                    object : StompFrameHandler {
                        override fun getPayloadType(headers: StompHeaders): Type {
                            return String::class.java
                        }

                        override fun handleFrame(
                            headers: StompHeaders,
                            payload: Any?
                        ) {
                            payload?.let { sink.next(it.toString()) }
                                ?: sink.error(RuntimeException("Empty message payload received"))
                        }
                    }
                )
            } catch (e: Exception) {
                sink.error(e)
            }
        }
}
