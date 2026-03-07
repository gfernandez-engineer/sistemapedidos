package com.food.ordering.deliveries.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic deliveryAssignedTopic() {
        return TopicBuilder.name("delivery.assigned")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic deliveryStatusChangedTopic() {
        return TopicBuilder.name("delivery.status.changed")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
