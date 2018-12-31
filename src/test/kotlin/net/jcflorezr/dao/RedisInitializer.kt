package net.jcflorezr.dao

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import redis.embedded.RedisServer

class RedisInitializer : TestRule {

    override fun apply(statement: Statement, description: Description): Statement {
        return object: Statement() {
            override fun evaluate() {
                println("starting embedded Redis")
                val port = 6379
                val redisServer = RedisServer(port)
                redisServer.start()
                // Giving some time while the database is up
                Thread.sleep(1000L)
                try {
                    statement.evaluate()
                } finally {
                    println("stopping embedded Redis")
                    redisServer.stop()
                }
            }
        }
    }

}