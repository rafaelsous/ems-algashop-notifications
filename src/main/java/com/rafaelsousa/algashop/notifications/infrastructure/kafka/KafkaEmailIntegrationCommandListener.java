package com.rafaelsousa.algashop.notifications.infrastructure.kafka;

import com.rafaelsousa.algashop.notifications.application.email.SendEmailApplicationService;
import com.rafaelsousa.algashop.notifications.application.email.command.SendEmailIntegrationCommand;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(
        id = KafkaConfig.EMAIL_COMMANDS_CONSUMER_GROUP,
        concurrency = "3",
        topics = KafkaConfig.EMAIL_COMMANDS_TOPIC)
public class KafkaEmailIntegrationCommandListener {
    private final SendEmailApplicationService sendEmailApplicationService;

    @KafkaHandler(isDefault = true)
    public void handle(
            @Payload Object command,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageKey,
            @Header(value = KafkaHeaders.OFFSET, required = false) Long offset) {
        log.warn(
                "Unsupported invoice command: type={} key={} offset={}",
                command.getClass().getSimpleName(),
                messageKey,
                offset);
        throw new IllegalArgumentException(
                "Unsupported invoice command type " + command.getClass().getSimpleName());
    }

    @KafkaHandler
    public void handle(
            @Payload @Valid SendEmailIntegrationCommand command,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageKey,
            @Header(value = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
            @Header(value = KafkaHeaders.OFFSET, required = false) Long offset) {
        log.info(
            "Received SendEmailCommand | templateId={} | key={} | partition={} | offset={}",
            command.getTemplateId(),
            messageKey,
            partition,
            offset);

        sendEmailApplicationService.send(command.toInput());
    }
}
