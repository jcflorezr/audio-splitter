package net.jcflorezr.transcriber.audio.transcriber.adapters.di.dao

import java.util.HashMap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Lazy
import org.springframework.data.cassandra.core.CassandraAdminOperations
import org.springframework.data.cassandra.core.CassandraOperations
import org.springframework.data.cassandra.core.cql.CqlIdentifier

@Configuration
@Lazy
@Import(value = [AudioTranscriberTestCassandraConfig::class])
open class AudioTranscriberCassandraDaoTestDI {

    @Autowired
    @Qualifier("audioTranscriberTestCassandraTemplate")
    protected lateinit var cassandraOperations: CassandraOperations
    @Autowired
    @Qualifier("audioTranscriberTestCassandraAdminTemplate")
    private lateinit var cassandraAdminOperations: CassandraAdminOperations

    fun createTable(tableName: String, tableEntityClass: Class<*>) {
        cassandraAdminOperations.createTable(true, CqlIdentifier.of(tableName), tableEntityClass, HashMap())
    }
}
