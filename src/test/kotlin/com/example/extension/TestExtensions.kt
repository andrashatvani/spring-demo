package com.example.extension

import io.mockk.every
import org.apache.kafka.streams.state.QueryableStoreType
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService

inline fun <reified K : Any, reified V : Any> InteractiveQueryService.mockStore(storeName: String, store: ReadOnlyKeyValueStore<K, V>) = run {
    val interactiveQueryService = this
    every {
        interactiveQueryService.getQueryableStore(
            storeName,
            any<QueryableStoreType<ReadOnlyKeyValueStore<K, V>>>()
        )
    } returns store
}