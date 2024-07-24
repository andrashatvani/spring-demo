package com.example.event

import com.example.event.BaseEvent.Companion.EVENT_TYPE_HEADER
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.utils.Utils
import org.apache.kafka.streams.*
import org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG
import org.apache.kafka.streams.StreamsConfig.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.processor.StateStore
import org.apache.kafka.streams.state.QueryableStoreType
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.apache.kafka.streams.test.TestRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.cloud.stream.binder.kafka.streams.properties.KafkaStreamsBinderConfigurationProperties
import org.springframework.cloud.stream.binder.kafka.streams.properties.KafkaStreamsExtendedBindingProperties
import org.springframework.cloud.stream.config.BindingServiceProperties
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ThreadLocalRandom

/**
 * override InteractiveQueryService bean in order to support access to queryableStore (key value stores).
 * KafkaStreams instance will not be fully started in test context, therefore stores will not be accessible.
 * See also https://docs.confluent.io/current/streams/developer-guide/test-streams.html
 */
@Suppress("UNCHECKED_CAST")
@Primary
@Component
class InteractiveQueryService : InteractiveQueryService(null, null) {

    private lateinit var topologyTestDriver: TopologyTestDriver

    override fun <T : Any?> getQueryableStore(storeName: String, storeType: QueryableStoreType<T>): T =
        topologyTestDriver.getKeyValueStore<Any, Any>(storeName) as T

    fun setTopologyTestDriver(topologyTestDriver: TopologyTestDriver) {
        this.topologyTestDriver = topologyTestDriver
    }

    fun <K, T> getStore(storeName: String): ReadOnlyKeyValueStore<K, T> =
        this.getQueryableStore(storeName, (QueryableStoreTypes.keyValueStore()))
}

abstract class AbstractStreamIntegrationTest {
    @Autowired
    protected lateinit var interactiveQueryService: InteractiveQueryService

    protected lateinit var testDriver: TopologyTestDriver

    @Autowired
    protected lateinit var kafkaStreamsBinderConfigurationProperties: KafkaStreamsBinderConfigurationProperties

    @Autowired
    protected lateinit var bindingServiceProperties: BindingServiceProperties

    @Autowired
    protected lateinit var kafkaStreamsExtendedBindingProperties: KafkaStreamsExtendedBindingProperties

    val defaultKeySerde by lazy {
        kafkaStreamsBinderConfigurationProperties.configuration["default.key.serde"]!!
    }
    val defaultValueSerde by lazy {
        kafkaStreamsBinderConfigurationProperties.configuration["default.value.serde"]!!
    }

    protected fun setupTestDriver(streamCreator: (StreamsBuilder) -> Unit) {
        val properties = Properties().apply {
            putAll(kafkaStreamsBinderConfigurationProperties.configuration)
            putIfAbsent(BOOTSTRAP_SERVERS_CONFIG, "dummy-bootstrap-host:0")
            putIfAbsent(
                APPLICATION_ID_CONFIG,
                "dummy-topology-test-driver-app-id-" + ThreadLocalRandom.current().nextInt()
            )
        }
        val streamsConfig = StreamsConfig(properties)
        val topologyConfig = TopologyConfig(streamsConfig)
        val streamsBuilder = StreamsBuilder(topologyConfig)
        streamCreator(streamsBuilder)
        val topology = streamsBuilder.build(properties)
        testDriver = TopologyTestDriver(topology, properties)

        (interactiveQueryService as com.example.event.InteractiveQueryService).setTopologyTestDriver(testDriver)
    }

    fun <T> String.toSerdeInstance(): Serde<T> = Utils.newInstance(this, Serde::class.java) as Serde<T>

    private fun <K> consumerKeySerde(bindingName: String) =
        (kafkaStreamsExtendedBindingProperties.bindings[bindingName]?.consumer?.keySerde ?: defaultKeySerde)
            .toSerdeInstance<K>()
            .apply { configure(kafkaStreamsBinderConfigurationProperties.configuration, true) }

    fun <V> consumerValueSerde(bindingName: String) =
        (kafkaStreamsExtendedBindingProperties.bindings[bindingName]?.consumer?.valueSerde ?: defaultValueSerde)
            .toSerdeInstance<V>()
            .apply { configure(kafkaStreamsBinderConfigurationProperties.configuration, false) }

    fun <K> producerKeySerde(bindingName: String) =
        (kafkaStreamsExtendedBindingProperties.bindings[bindingName]?.producer?.keySerde ?: defaultKeySerde)
            .toSerdeInstance<K>()
            .apply { configure(kafkaStreamsBinderConfigurationProperties.configuration, true) }

    fun <V> producerValueSerde(bindingName: String) =
        (kafkaStreamsExtendedBindingProperties.bindings[bindingName]?.consumer?.valueSerde ?: defaultValueSerde)
            .toSerdeInstance<V>()
            .apply { configure(kafkaStreamsBinderConfigurationProperties.configuration, false) }

    fun retrieveTopicNameForBinding(bindingName: String): String =
        bindingServiceProperties.bindings[bindingName]!!.destination

    fun <K, V> consumedBy(bindingName: String) =
        Consumed.with(consumerKeySerde<K>(bindingName), consumerValueSerde<V>(bindingName))

    fun <K, V> producedBy(bindingName: String) =
        Produced.with(producerKeySerde<K>(bindingName), producerValueSerde<V>(bindingName))

    fun <K, V, S : StateStore?> materializedAs(bindingName: String) =
        Materialized.`as`<K, V, S>(kafkaStreamsExtendedBindingProperties.bindings[bindingName]!!.consumer.materializedAs)

    inline fun <reified K, T> TopologyTestDriver.createInputTopic(bindingName: String): TestInputTopic<K, T> {
        val keySerializer =
            if (String::class.java == K::class.java) {
                defaultKeySerde.toSerdeInstance<String>().serializer()
            } else {
                DefaultJsonSerde<K>().serializer().forKeys()
            } as Serializer<K>
        return createInputTopic(
            retrieveTopicNameForBinding(bindingName),
            keySerializer,
            consumerValueSerde<T>(bindingName).serializer() as Serializer<T>
        )
    }

    inline fun <reified K, T> TopologyTestDriver.createOutputTopic(bindingName: String): TestOutputTopic<K, T> {
        val keyDeserializer =
            if (String::class.java == K::class.java) {
                defaultKeySerde.toSerdeInstance<String>().deserializer()
            } else {
                DefaultJsonSerde<K>().deserializer().forKeys().apply {
                    addTrustedPackages("*")
                }.forKeys()
            } as Deserializer<K>

        return createOutputTopic(
            retrieveTopicNameForBinding(bindingName),
            keyDeserializer,
            producerValueSerde<T>(bindingName).deserializer() as Deserializer<T>
        )
    }

    fun <V : BaseEvent<String>> TestInputTopic<String, V>.send(event: V) = send(event, event.messageKey)

    fun <K, V : BaseEvent<K>> TestInputTopic<K, V>.send(event: V, messageKey: K = event.messageKey) {
        val testRecord = TestRecord(messageKey, event)
        testRecord.headers.add(EVENT_TYPE_HEADER, event.eventType.toByteArray())
        this.pipeInput(testRecord)
    }

    fun <V : BaseEvent<*>> TestInputTopic<String, V>.sendTombstone(key: String, eventType: String) {
        val testRecord = TestRecord(key, null as V?)
        testRecord.headers.add(EVENT_TYPE_HEADER, eventType.toByteArray())
        this.pipeInput(testRecord)
    }

    fun <K, T> getStore(storeName: String): ReadOnlyKeyValueStore<K, T> =
        (interactiveQueryService as com.example.event.InteractiveQueryService).getStore(storeName)

}
