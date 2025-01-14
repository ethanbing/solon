package org.noear.solon.cloud.extend.kafka.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.cloud.CloudEventHandler;
import org.noear.solon.cloud.CloudProps;
import org.noear.solon.cloud.annotation.EventLevel;
import org.noear.solon.cloud.exception.CloudEventException;
import org.noear.solon.cloud.model.Event;
import org.noear.solon.cloud.service.CloudEventObserverManger;
import org.noear.solon.cloud.service.CloudEventServicePlus;
import org.noear.solon.core.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author noear
 * @since 1.3
 */
public class CloudEventServiceKafkaImp implements CloudEventServicePlus {
    static Logger log = LoggerFactory.getLogger(CloudEventServiceKafkaImp.class);


    private final CloudProps cloudProps;
    private KafkaProducer<String, String> producer;
    private KafkaConsumer<String, String> consumer;

    private long timeout;
    private String server;
    String eventChannelName;

    public CloudEventServiceKafkaImp(CloudProps cloudProps) {
        this.cloudProps = cloudProps;

        this.timeout = cloudProps.getEventPublishTimeout();
        this.server = cloudProps.getEventServer();
        this.eventChannelName = cloudProps.getEventChannel();
    }

    private synchronized void initProducer() {
        if (producer != null) {
            return;
        }

        Properties config = new Properties();

        config.put("bootstrap.servers", server);
        config.put("key.serializer", StringSerializer.class.getName());
        config.put("value.serializer", StringSerializer.class.getName());
        config.put("acks", "all");
        config.put("retries", 0);
        config.put("batch.size", 16384); //默认是16384Bytes，即16kB

        //绑定定制属性
        Properties props = cloudProps.getEventProducerProps();
        if (props.size() > 0) {
            props.forEach((k, v) -> {
                config.put(k, v);
            });
        }

        producer = new KafkaProducer<>(config);
    }

    private synchronized void initConsumer() {
        if (consumer != null) {
            return;
        }

        Properties config = new Properties();

        config.put("bootstrap.servers", server);
        config.put("key.deserializer", StringDeserializer.class.getName());
        config.put("value.deserializer", StringDeserializer.class.getName());
        config.put("group.id", Solon.cfg().appGroup() + "_" + Solon.cfg().appName());
        config.put("enable.auto.commit", "true");
        config.put("auto.commit.interval.ms", "1000");
        config.put("session.timeout.ms", "30000");
        config.put("max.poll.records", 100);
        config.put("auto.offset.reset", "earliest");

        //绑定定制属性
        Properties props = cloudProps.getEventConsumerProps();
        if (props.size() > 0) {
            props.forEach((k, v) -> {
                config.put(k, v);
            });
        }

        consumer = new KafkaConsumer<>(config);
    }


    @Override
    public boolean publish(Event event) throws CloudEventException {
        initProducer();

        if (Utils.isEmpty(event.key())) {
            event.key(Utils.guid());
        }

        Future<RecordMetadata> future = producer.send(new ProducerRecord<String, String>(event.topic(), event.key(), event.content()));
        if (timeout > 0 && event.qos() > 0) {
            try {
                future.get(timeout, TimeUnit.MICROSECONDS);
            } catch (Exception e) {
                throw new CloudEventException(e);
            }
        }

        return true;
    }

    CloudEventObserverManger observerManger = new CloudEventObserverManger();

    @Override
    public void attention(EventLevel level, String channel, String group, String topic, CloudEventHandler observer) {
        observerManger.add(topic, level, group, topic, observer);
    }

    public void subscribe() {
        try {
            initConsumer();

            //订阅
            if (observerManger.topicSize() > 0) {
                consumer.subscribe(observerManger.topicAll());
            }

            //开始拉取
            new Thread(this::subscribePull).start();
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private void subscribePull() {
        while (true) {
            boolean isOk = true;

            try {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                if (records.isEmpty()) {
                    Thread.sleep(100);
                    continue;
                }

                for (ConsumerRecord<String, String> record : records) {
                    Event event = new Event(record.topic(), record.value())
                            .key(record.key())
                            .channel(eventChannelName);

                    isOk = isOk && onReceive(event);
                }

                if (isOk) {
                    consumer.commitAsync();
                }
            } catch (EOFException e) {
                break;
            } catch (Throwable e) {
                EventBus.push(e);
            }
        }
    }

    /**
     * 处理接收事件
     */
    public boolean onReceive(Event event) throws Throwable {
        boolean isOk = true;
        CloudEventHandler handler = null;

        handler = observerManger.get(event.topic());
        if (handler != null) {
            isOk = handler.handler(event);
        } else {
            //只需要记录一下
            log.warn("There is no observer for this event topic[{}]", event.topic());
        }

        return isOk;
    }

    private String channel;
    private String group;

    @Override
    public String getChannel() {
        if (channel == null) {
            channel = cloudProps.getEventChannel();
        }
        return channel;
    }

    @Override
    public String getGroup() {
        if (group == null) {
            group = cloudProps.getEventGroup();
        }

        return group;
    }
}
