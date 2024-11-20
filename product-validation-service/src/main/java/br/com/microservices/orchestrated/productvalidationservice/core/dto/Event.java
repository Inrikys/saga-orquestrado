package br.com.microservices.orchestrated.productvalidationservice.core.dto;

import br.com.microservices.orchestrated.productvalidationservice.core.enums.ESagaStatus;
import br.com.microservices.orchestrated.productvalidationservice.core.repository.ProductRepository;
import br.com.microservices.orchestrated.productvalidationservice.core.repository.ValidationRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.security.oauthbearer.internals.secured.ValidateException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    private String id;
    private String transactionId;
    private String orderId;
    private Order payload;
    private String source;
    private ESagaStatus status;
    private List<History> eventHistory;
    private LocalDateTime createdAt;

    public void addToHistory(History history) {
        if (isEmpty(eventHistory)) {
            eventHistory = new ArrayList<>();
        }

        eventHistory.add(history);
    }

    public void validate(ValidationRepository validationRepository, ProductRepository productRepository) {

        validateProductsInformed();

        if (validationRepository.existsByOrderIdAndTransactionId(orderId, transactionId)) {
            throw new ValidateException("There's another transactionId for this validation.");
        }

        payload.getProducts().forEach(product -> {
                    validateProductInformed(product);
                    validateExistingProduct(product.getProduct().getCode(), productRepository);
                }
        );
    }

    private void validateProductsInformed() {
        if (isEmpty(payload) || isEmpty(payload.getProducts())) {
            throw new ValidateException("Product list is empty!");
        }

        if (isEmpty(payload.getId()) || isEmpty(payload.getTransactionId())) {
            throw new ValidateException("OrderID and TransactionID must be informed!");
        }
    }


    private void validateExistingProduct(String code, ProductRepository productRepository) {

        if (!productRepository.existsByCode(code)) {
            throw new ValidateException("Product does not exists in database!");
        }

    }

    private void validateProductInformed(OrderProducts orderProducts) {
        if (isEmpty(orderProducts.getProduct()) || isEmpty(orderProducts.getProduct().getCode())) {
            throw new ValidateException("Product must be informed!");
        }
    }

    public void handleSuccess(String currentSource) {
        this.setStatus(ESagaStatus.SUCCESS);
        this.setSource(currentSource);

        addHistory(this, "Products are validated successfully!");
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
}
