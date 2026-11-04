package com.algaworks.algashop.notifications.infrastructure.kafka;

import com.algaworks.algashop.notifications.application.email.TemplateRenderException;
import com.algaworks.algashop.notifications.domain.model.template.TemplateNotFoundException;
import jakarta.validation.ConstraintViolationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.mail.MailParseException;
import org.springframework.util.backoff.ExponentialBackOff;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {
    public static final String TYPE_ID_HEADER = "__TypeId__";
    public static final String MIN_INSYNC_REPLICAS_CONFIG = "min.insync.replicas";

    private static final String DLT_PREFIX = "notifications.dlt.";
    private static final int TOPIC_REPLICAS = 3;
    private static final int TOPIC_PARTITIONS = 3;
    private static final String RETENTION_30_DAYS = String.valueOf(Duration.ofDays(30).toMillis());

    public static final String EMAIL_COMMANDS_TOPIC = "notifications.email.commands";
    public static final String EMAIL_COMMANDS_CONSUMER_GROUP = "notifications.email-commands";

    @Bean
    public DefaultErrorHandler defaultErrorHandler(DeadLetterPublishingRecoverer recoverer) {
        long interval = 2000L; // 2s entre as tentativas
        double multiplier = 2;
        long maxRetries = 3L; // máximo de 2 tentativas
        long maxInterval = 8000L;

        ExponentialBackOff exponentialBackOff = new ExponentialBackOff(interval, multiplier);
        exponentialBackOff.setMaxInterval(maxInterval);
        exponentialBackOff.setMaxAttempts(maxRetries);

        DefaultErrorHandler defaultErrorHandler =
                new DefaultErrorHandler(recoverer, exponentialBackOff);
        defaultErrorHandler.addNotRetryableExceptions(
                TemplateNotFoundException.class,
                TemplateRenderException.class,
                ConstraintViolationException.class,
                MailParseException.class,
//                MailPreparationException.class, Impede retentativa quando o Mailpit está fora
                IllegalArgumentException.class);

        return defaultErrorHandler;
    }

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (consumerRecord, _) -> {
                            String dltTopic = DLT_PREFIX + consumerRecord.topic();
                            return new TopicPartition(dltTopic, consumerRecord.partition());
                        });

        recoverer.setLogRecoveryRecord(true);
        recoverer.setFailIfSendResultIsError(false);

        return recoverer;
    }

    @Bean
    public NewTopic emailCommandTopic() {
        return TopicBuilder.name(EMAIL_COMMANDS_TOPIC)
                .partitions(TOPIC_PARTITIONS)
                .replicas(TOPIC_REPLICAS)
                .configs(Map.of(MIN_INSYNC_REPLICAS_CONFIG, "2"))
                .build();
    }

    @Bean
    public NewTopic emailCommandsDlt() {
        return TopicBuilder.name(DLT_PREFIX + EMAIL_COMMANDS_TOPIC)
            .partitions(TOPIC_PARTITIONS)
            .replicas(TOPIC_REPLICAS)
            .configs(Map.of(MIN_INSYNC_REPLICAS_CONFIG, "2", "retention.ms", RETENTION_30_DAYS))
            .build();
    }
}
