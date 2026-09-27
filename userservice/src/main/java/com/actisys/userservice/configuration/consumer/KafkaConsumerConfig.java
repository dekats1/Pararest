package com.actisys.userservice.configuration.consumer;

import com.actisys.kafkacommunication.wallet.CreateWalletEvent;
import com.actisys.kafkacommunication.wallet.RefundMoneyEvent;
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
  public ConsumerFactory<String, CreateWalletEvent> createWalletConsumerFactory() {
    Map<String, Object> props = baseProps();
    props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CreateWalletEvent.class.getName());
    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, CreateWalletEvent> createWalletListenerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, CreateWalletEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(createWalletConsumerFactory());
    return factory;
  }

  @Bean
  public ConsumerFactory<String, RefundMoneyEvent> createRefundConsumerFactory() {
    Map<String, Object> props = baseProps();
    props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, RefundMoneyEvent.class.getName());
    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, RefundMoneyEvent> createRefundListenerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, RefundMoneyEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(createRefundConsumerFactory());
    return factory;
  }
}