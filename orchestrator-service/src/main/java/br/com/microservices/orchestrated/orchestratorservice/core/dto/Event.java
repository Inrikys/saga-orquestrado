package br.com.microservices.orchestrated.orchestratorservice.core.dto;

import br.com.microservices.orchestrated.orchestratorservice.core.enums.EEventSource;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    private static final String SAGA_LOG_ID = "ORDER ID: %s | TRANSACTION ID: %s | EVENT ID %s";

    private String id;
    private String transactionId;
    private String orderId;
    private Order payload;
    private EEventSource source;
    private ESagaStatus status;
    private List<History> eventHistory;
    private LocalDateTime createdAt;

    public String getSagaId() {
        return String.format(SAGA_LOG_ID,
                getPayload().getId(), getTransactionId(), getId());
    }

//    public void addHistorySuccess(String currentSource) {
//        this.setStatus(ESagaStatus.SUCCESS);
//        this.setSource(currentSource);
//
//        addHistory(this, "Inventory updated successfully!");
//    }
//
//    public void addHistoryFail(String message, String currentSource) {
//        this.setStatus(ESagaStatus.ROLLBACK_PENDING);
//        this.setSource(currentSource);
//
//        addHistory(this, "Fail while doing inventory update: " + message);
//    }
//
//    public void addHistoryRollback(String currentSource) {
//        this.setStatus(ESagaStatus.FAIL);
//        this.setSource(currentSource);
//        addHistory(this, "Rollback executed on inventory!");
//    }

    public void addHistory(String message) {
        History history = History.builder()
                .source(this.getSource())
                .status(this.getStatus())
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();

        event.addToHistory(history);
    }

    public void addToHistory(History history) {
        if (isEmpty(eventHistory)) {
            eventHistory = new ArrayList<>();
        }

        eventHistory.add(history);
    }

}
