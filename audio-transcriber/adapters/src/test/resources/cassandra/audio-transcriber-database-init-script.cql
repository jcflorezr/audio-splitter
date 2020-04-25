CREATE KEYSPACE IF NOT EXISTS AUDIO_TRANSCRIBER WITH replication = { 'class': 'SimpleStrategy', 'replication_factor': '1' };
USE AUDIO_TRANSCRIBER;