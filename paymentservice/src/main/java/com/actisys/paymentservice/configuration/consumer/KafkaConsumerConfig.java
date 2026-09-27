package com.actisys.paymentservice.configuration.consumer;

import com.actisys.kafkacommunication.order.CreatePaymentEvent;
import com.actisys.kafkacommunication.replenishment.CreateReplenishmentEvent;
import com.actisys.kafkacommunication.wallet.RefundEvent;
import com.actisys.kafkacommunication.wallet.WithdrawEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${spring.kafka.consumer.group-id}")
  private String groupId;

  private Map<String, Object> baseProps() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.actisys.*");
    return props;
  }

  @Bean
  public ConsumerFactory<String, CreatePaymentEvent> createPaymentConsumerFactory() {
    Map<String, Object> props = baseProps();
    props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CreatePaymentEvent.class.getName());
    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, CreatePaymentEvent> createPaymentListenerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, CreatePaymentEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(createPaymentConsumerFactory());
    return factory;
  }

  @Bean
  public ConsumerFactory<String, CreateReplenishmentEvent> createReplenishmentConsumerFactory() {
    Map<String, Object> props = baseProps();
    props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CreateReplenishmentEvent.class.getName());
    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, CreateReplenishmentEvent> createReplenishmentListenerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, CreateReplenishmentEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(createReplenishmentConsumerFactory());
    return factory;
  }

  @Bean
  public ConsumerFactory<String, WithdrawEvent> withdrawConsumerFactory() {
    Map<String, Object> props = baseProps();
    props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, WithdrawEvent.class.getName());
    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, WithdrawEvent> withdrawListenerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, WithdrawEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(withdrawConsumerFactory());
    return factory;
  }

  @Bean
  public ConsumerFactory<String, RefundEvent> refundConsumerFactory() {
    Map<String, Object> props = baseProps();
    props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, RefundEvent.class.getName());
    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, RefundEvent> refundListenerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, RefundEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(refundConsumerFactory());
    return factory;
  }
}