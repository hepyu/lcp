package com.open.lcp.queue.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface KafkaMessageExecutor<K, V> {

	public void doMessage(ConsumerRecord<K, V> consumerRecord);
}
