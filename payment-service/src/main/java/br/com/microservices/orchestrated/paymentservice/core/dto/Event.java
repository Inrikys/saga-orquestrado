package br.com.microservices.orchestrated.paymentservice.core.dto;

import br.com.microservices.orchestrated.paymentservice.core.enums.ESagaStatus;
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

    private static Double REDUCE_IDENTITY_SUM_VALUE = 0.0;

    private String id;
    private String transactionId;
    private String orderId;
    private Order payload;
    private String source;
    private ESagaStatus status;
    private List<History> eventHistory;
    private LocalDateTime createdAt;

    public double calculateAmount() {
        return payload.getProducts()
                .stream()
                .map(p -> p.getQuantity() * p.getProduct().getUnitValue())
                .reduce(REDUCE_IDENTITY_SUM_VALUE, Double::sum);
    }

    public int calculateTotalItems() {
        return payload.getProducts()
                .stream()
                .map(OrderProducts::getQuantity)
                .reduce(REDUCE_IDENTITY_SUM_VALUE.intValue(), Integer::sum);
    }

    public void setTotalAmountItems(Double totalAmount) {
        payload.setTotalAmount(totalAmount);
    }

    public void setTotalItems(int totalItems) {
        payload.setTotalItems(totalItems);
    }

    public void addHistorySuccess(String currentSource) {
        this.setStatus(ESagaStatus.SUCCESS);
        this.setSource(currentSource);

        addHistory(this, "Payment done successfully!");
    }

    public void addHistoryFail(String message, String currentSource) {
        this.setStatus(ESagaStatus.ROLLBACK_PENDING);
        this.setSource(currentSource);

        addHistory(this, "Fail while doing payment: " + message);
    }

    public void addHistoryRollback(String currentSource) {
        this.setStatus(ESagaStatus.FAIL);
        this.setSource(currentSource);
        addHistory(this, "Rollback executed on payment!");
    }

    private void addHistory(Event event, String message) {
        History history = History.builder()
                .source(event.getSource())
                .status(event.getStatus())
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
