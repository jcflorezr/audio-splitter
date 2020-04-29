CREATE KEYSPACE IF NOT EXISTS AUDIO_SPLITTER WITH replication = { 'class': 'SimpleStrategy', 'replication_factor': '1' };
USE AUDIO_SPLITTER;