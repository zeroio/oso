package io.oso

import io.javaoperatorsdk.operator.Operator
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider
import org.http4k.config.Environment
import org.http4k.config.EnvironmentKey
import org.http4k.config.fromYaml
import org.http4k.core.Method.GET
import org.http4k.core.Method.PUT
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.k8s.Http4kK8sServer
import org.http4k.k8s.asK8sServer
import org.http4k.k8s.health.Health
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.SunHttpLoom
import java.security.Security

object App  {
    fun handleCreateUpdate(request: Request): Response {
        return Response(OK).body("Hello")
    }

    operator fun invoke(env: Environment): Http4kK8sServer  {
        val mainApp = routes(
            "/clusters" bind routes(
                "/{id:.*}" bind PUT to ::handleCreateUpdate
            )
        )

        val healthApp = Health(
            "/config" bind GET to { Response(OK).body(env.keys().toString()) },
            checks = emptyList()
        )

        return mainApp.asK8sServer({ port -> SunHttpLoom(port) }, env, healthApp)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        // Use Bouncy Castle FIPS provider as primary provider
        Security.insertProviderAt(BouncyCastleFipsProvider(), 1)

        // Read application configuration
        val defaultConfig = Environment.defaults(
            EnvironmentKey.k8s.SERVICE_PORT of 8000,
            EnvironmentKey.k8s.HEALTH_PORT of 8001,
        )
        val config = Environment.JVM_PROPERTIES overrides
                Environment.ENV overrides
                Environment.fromYaml("application.yml") overrides
                defaultConfig

        val operator = Operator()
        operator.register(ClusterReconciler())
        operator.start()
        App(config).start()
    }
}