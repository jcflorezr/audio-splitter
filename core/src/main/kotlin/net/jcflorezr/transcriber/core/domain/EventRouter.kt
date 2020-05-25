package net.jcflorezr.transcriber.core.domain

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object EventRouter {

    private const val AGGREGATE_FIELD = "aggregate"
    private const val AGGREGATE_CLASS_NAME_FIELD = "aggregateClassName"
    private const val EVENT_CLASS_NAME_FIELD = "eventClassName"

    private val mapper = ObjectMapper().registerKotlinModule()
    private val registeredEventHandlers = mutableMapOf<Class<*>, EventHandler<Event<AggregateRoot>>>()

    fun <T : Event<AggregateRoot>> register(eventClass: Class<T>, eventHandler: EventHandler<Event<AggregateRoot>>) {
        registeredEventHandlers[eventClass] = eventHandler
    }

    suspend fun route(eventRecord: JsonNode) =
        eventRecord.run {
            val aggregateRoot = withContext(Dispatchers.IO) {
                mapper.treeToValue(get(AGGREGATE_FIELD), Class.forName(get(AGGREGATE_CLASS_NAME_FIELD).asText()))
            }
            Class.forName(get(EVENT_CLASS_NAME_FIELD).asText())
                .getConstructor(aggregateRoot::class.java)
                .newInstance(aggregateRoot) as Event<AggregateRoot>
        }.let { event ->
            registeredEventHandlers[event::class.java]?.execute(event)
                ?: throw RuntimeException("No event handler found for event: ${event::class.java.name}")
                // TODO: create exception class for this
        }
}
