package br.com.microservices.orchestrated.orchestratorservice.core.saga;

import br.com.microservices.orchestrated.orchestratorservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.orchestratorservice.core.dto.Event;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.EEventSource;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static br.com.microservices.orchestrated.orchestratorservice.core.saga.SagaHandler.*;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Component
public class SagaExecutionController {

    public ETopics getNextTopic(Event event) {

        if (isEmpty(event.getSource()) || isEmpty(event.getStatus())) {
            throw new ValidationException("Source and status must be informed.");
        }

        ETopics topic = findTopicsBySourceAndStatus(event);
        logCurrentSaga(event, topic);
        return topic;
    }

    private ETopics findTopicsBySourceAndStatus(Event event) {
        return (ETopics) Arrays.stream(SAGA_HANDLER)
                .filter(row -> isEventSourceAndStatusValid(event, row))
                .map(i -> i[TOPIC_INDEX])
                .findFirst()
                .orElseThrow(() -> new ValidationException("Topic not found!"));
    }

    private boolean isEventSourceAndStatusValid(Event event, Object[] row) {
        Object source = row[EVENT_SOURCE_INDEX];
        Object status = row[SAGA_STATUS_INDEX];

        return event.getSource().equals(source) && event.getStatus().equals(status);
    }

    private void logCurrentSaga(Event event, ETopics topic) {
        String sagaId = event.getSagaId();
        EEventSource source = event.getSource();

        switch (event.getStatus()) {
            case SUCCESS -> log.info("### CURRENT SAGA: {} | SUCCESS | NEXT TOPiC {} | {}",
                    source, topic, sagaId);
            case ROLLBACK_PENDING ->
                    log.info("### CURRENT SAGA: {} | SENDING TO ROLLBACK CURRENT SERVICE | NEXT TOPiC {} | {}",
                            source, topic, sagaId);
            case FAIL -> log.info("### CURRENT SAGA: {} | SENDING TO ROLLBACK PREVIOUS SERVICE | NEXT TOPiC {} | {}",
                    source, topic, sagaId);
        }
    }
}
