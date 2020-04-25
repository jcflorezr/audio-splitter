package net.jcflorezr.transcriber.audio.splitter.adapters.di.dao

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.cassandra.core.CassandraAdminOperations
import org.springframework.data.cassandra.core.CassandraOperations
import org.springframework.data.cassandra.core.cql.CqlIdentifier
import java.util.HashMap

@Configuration
@Import(value = [AudioSplitterTestCassandraConfig::class])
open class AudioSplitterCassandraDaoTestDI {

    @Autowired
    @Qualifier("audioSplitterTestCassandraTemplate")
    protected lateinit var cassandraOperations: CassandraOperations
    @Autowired
    @Qualifier("audioSplitterTestCassandraAdminTemplate")
    private lateinit var cassandraAdminOperations: CassandraAdminOperations

    fun createTable(tableName: String, tableEntityClass: Class<*>) {
        cassandraAdminOperations.createTable(true, CqlIdentifier.of(tableName), tableEntityClass, HashMap())
    }
}