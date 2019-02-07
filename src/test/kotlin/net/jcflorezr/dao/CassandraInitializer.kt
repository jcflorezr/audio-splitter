package net.jcflorezr.dao

import com.datastax.driver.core.Cluster
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.springframework.data.cassandra.core.CassandraAdminTemplate
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter
import org.springframework.data.cassandra.core.cql.CqlIdentifier
import java.util.HashMap

class CassandraInitializer : TestRule {

    companion object {
        private const val CONTACT_POINTS= "localhost"
        private const val PORT = 9142
        private const val KEYSPACE_CREATION_SCRIPT = "CREATE KEYSPACE IF NOT EXISTS AUDIO_SPLITTER WITH replication = { 'class': 'SimpleStrategy', 'replication_factor': '1' };"
        private const val KEYSPACE_ACTIVATION_SCRIPT = "USE AUDIO_SPLITTER;"
        private var cassandraAdminTemplate: CassandraAdminTemplate? = null
    }

    override fun apply(statement: Statement, description: Description): Statement {
        return object: Statement() {
            override fun evaluate() {
                // TODO: implement Logger
                println("Starting embedded Cassandra")
                EmbeddedCassandraServerHelper.startEmbeddedCassandra()
                val cluster = Cluster
                    .builder()
                    .addContactPoints(CONTACT_POINTS)
                    .withPort(PORT)
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
                    EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
                }
            }
        }
    }

    fun createTable(tableName: String, aClass: Class<*>) {
        cassandraAdminTemplate!!.createTable(true, CqlIdentifier.cqlId(tableName), aClass, HashMap())
    }

    fun dropTable(tableName: String) {
        cassandraAdminTemplate!!.dropTable(CqlIdentifier.cqlId(tableName))
    }

}