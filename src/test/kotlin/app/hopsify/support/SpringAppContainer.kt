package app.hopsify.support

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.PullPolicy

class SpringAppContainer<SELF : SpringAppContainer<SELF>>(dockerImageName: String, private val serverPort: Int = 8080) :
    GenericContainer<SELF>(dockerImageName) {
    private val logger: Logger = LoggerFactory.getLogger("spring-container:$serverPort")

    init {
        this.withImagePullPolicy(PullPolicy.defaultPolicy())
            .waitingFor(Wait.forHttp("/actuator/health"))
        this.withAccessToHost(true)
        this.withExposedPorts(serverPort)
        this.withLogConsumer(Slf4jLogConsumer(logger))

        // override default server port
        this.withEnv("SERVER_PORT", serverPort.toString())

        // set TestContainers host to get access to the test mail server implementation
        this.withEnv("SPRING_MAIL_HOST", "host.testcontainers.internal")

        // try to run the task every 100ms to have simultaneous task processing on both instances
        this.withEnv("TASKS_SUBSCRIPTION_NEWSLETTER_DELAY", "PT0.1S")
    }

    val serverPortMapping: Int get() = getMappedPort(serverPort)
}
