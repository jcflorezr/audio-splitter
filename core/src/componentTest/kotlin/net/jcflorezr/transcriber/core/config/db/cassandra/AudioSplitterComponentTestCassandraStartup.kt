package net.jcflorezr.transcriber.core.config.db.cassandra

object AudioSplitterComponentTestCassandraStartup : ComponentTestCassandraStartup(
    startUpScriptName = "audio-splitter-database-init-script",
    keyspaceName = "AUDIO_SPLITTER"
) {
    init {
        System.setProperty("TRANSCRIBER_COMPONENT_CASSANDRA_DB_KEYSPACE_NAME", "AUDIO_SPLITTER")
    }
}
