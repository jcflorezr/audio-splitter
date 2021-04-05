package net.jcflorezr.transcriber.core.config.db.cassandra

object AudioTranscriberIntegrationTestCassandraStartup : IntegrationTestCassandraStartup(
    startUpScriptName = "audio-transcriber-database-init-script", keyspaceName = "AUDIO_TRANSCRIBER"
) {
    init {
        System.setProperty("TRANSCRIBER_INTEGRATION_CASSANDRA_DB_KEYSPACE_NAME", "AUDIO_TRANSCRIBER")
    }
}
