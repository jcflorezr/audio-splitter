package net.jcflorezr.dao

import com.datastax.driver.core.Cluster
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.springframework.data.cassandra.core.CassandraAdminTemplate
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter
import org.springframework.data.cassandra.core.cql.CqlIdentifier
import org.testcontainers.containers.CassandraContainer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.util.HashMap

class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)
class KCassandraContainer(imageName: String) : CassandraContainer<KCassandraContainer>(imageName)

class RedisInitializer : TestRule {

    companion object {
        private const val redisDockerImageName = "redis:5.0"
        private const val redisPort = 6379
        val redisDockerContainer: KGenericContainer = KGenericContainer(redisDockerImageName).withExposedPorts(redisPort)
    }

    override fun apply(statement: Statement, description: Description): Statement {
        return object: Statement() {
            override fun evaluate() {
                println("starting embedded Redis")
                redisDockerContainer.start()
                // Giving some time while the database is up
                Thread.sleep(1000L)
                try {
                    statement.evaluate()
                } finally {
                    println("stopping embedded Redis")
                    redisDockerContainer.stop()
                }
            }
        }
    }

}

class CassandraInitializer : TestRule {

    companion object {
        private const val cassandraDockerImageName = "cassandra:2.1"
        const val cassandraPort = 9042
        val cassandraDockerContainer: KCassandraContainer = KCassandraContainer(cassandraDockerImageName)
            .withExposedPorts(cassandraPort)
            .waitingFor(Wait.forListeningPort())
        private const val KEYSPACE_CREATION_SCRIPT = "CREATE KEYSPACE IF NOT EXISTS AUDIO_SPLITTER WITH replication = { 'class': 'SimpleStrategy', 'replication_factor': '1' };"
        private const val KEYSPACE_ACTIVATION_SCRIPT = "USE AUDIO_SPLITTER;"
        private var cassandraAdminTemplate: CassandraAdminTemplate? = null
    }

    override fun apply(statement: Statement, description: Description): Statement {
        return object: Statement() {
            override fun evaluate() {
                // TODO: implement Logger
                println("Starting embedded Cassandra")
                cassandraDockerContainer.start()
                val cluster = Cluster
                    .builder()
                    .addContactPoints(cassandraDockerContainer.containerIpAddress)
                    .withPort(cassandraDockerContainer.getMappedPort(cassandraPort))
                    .build()
                val session = cluster.connect()
                session.execute(KEYSPACE_CREATION_SCRIPT)
                session.execute(KEYSPACE_ACTIVATION_SCRIPT)
                cassandraAdminTemplate = CassandraAdminTemplate(session, MappingCassandraConverter())
                // Giving some time while the keyspace is created
                Thread.sleep(1000L)
                try {
                    statement.evaluate()
                } finally {
                    // TODO: implement Logger
                    println("Stopping embedded Cassandra")
                    /*
                    TODO: there is an exception thrown by cassandra driver when trying to
                        stop the database instance: java.io.IOException: Connection reset by peer
                     */
                    cassandraDockerContainer.stop()
                }
            }
        }
    }

    fun createTable(tableName: String, aClass: Class<*>) {
        cassandraAdminTemplate!!.createTable(true, CqlIdentifier.of(tableName), aClass, HashMap())
    }

    fun dropTable(tableName: String) {
        cassandraAdminTemplate!!.dropTable(CqlIdentifier.of(tableName))
    }

}