package net.jcflorezr.transcriber.core.config.db.cassandra

object AudioSplitterIntegrationTestCassandraStartup : IntegrationTestCassandraStartup(
    startUpScriptName = "audio-splitter-database-init-script", keyspaceName = "AUDIO_SPLITTER"
) {
    init {
        System.setProperty("TRANSCRIBER_INTEGRATION_CASSANDRA_DB_KEYSPACE_NAME", "AUDIO_SPLITTER")
    }
}
