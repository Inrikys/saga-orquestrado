package br.com.microservices.orchestrated.orchestratorservice.core.service;

import br.com.microservices.orchestrated.orchestratorservice.core.dto.Event;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.EEventSource;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics;
import br.com.microservices.orchestrated.orchestratorservice.core.producer.SagaOrchestratorProducer;
import br.com.microservices.orchestrated.orchestratorservice.core.saga.SagaExecutionController;
import br.com.microservices.orchestrated.orchestratorservice.core.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static br.com.microservices.orchestrated.orchestratorservice.core.enums.EEventSource.*;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus.*;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics.*;

@Slf4j
@Service
public class OrchestratorService {

    private final SagaOrchestratorProducer producer;
    private final JsonUtil jsonUtil;
    private final SagaExecutionController sagaExecutionController;

    public OrchestratorService(SagaOrchestratorProducer producer, JsonUtil jsonUtil, SagaExecutionController sagaExecutionController) {
        this.producer = producer;
        this.jsonUtil = jsonUtil;
        this.sagaExecutionController = sagaExecutionController;
    }

    public void startSaga(Event event) {
        event.setSource(ORCHESTRATOR);
        event.setStatus(SUCCESS);
        ETopics topic = getTopic(event);

        log.info("SAGA STARTED!");
        event.addHistory("Saga started!");
        sendToTopic(event, topic);
    }

    public void finishSagaSuccess(Event event) {
        event.setSource(ORCHESTRATOR);
        event.setStatus(SUCCESS);

        log.info("SAGA FINISHED SUCCESSFULLY FOR EVENT {}", event.getId());
        event.addHistory("Saga finished successfully!");
        sendToTopic(event, NOTIFY_ENDING);
    }

    public void finishSagaFail(Event event) {
        event.setSource(ORCHESTRATOR);
        event.setStatus(FAIL);

        log.info("SAGA FINISHED WITH ERRORS FOR EVENT {}", event.getId());
        event.addHistory("Saga finished with errors!");
        sendToTopic(event, NOTIFY_ENDING);
    }

    public void continueSaga(Event event) {
        ETopics topic = getTopic(event);
        log.info("SAGA CONTINUING FOR EVENT {}", event.getId());
        sendToTopic(event, topic);
    }

    private ETopics getTopic(Event event) {
        return sagaExecutionController.getNextTopic(event);
    }

    private void sendToTopic(Event event, ETopics topic) {
        producer.sendEvent(jsonUtil.toJson(event), topic.getTopic());
    }

}
