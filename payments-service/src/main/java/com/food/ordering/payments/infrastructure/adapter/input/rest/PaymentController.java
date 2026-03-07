package com.food.ordering.payments.infrastructure.adapter.input.rest;

import com.food.ordering.payments.application.port.input.GetPaymentUseCase;
import com.food.ordering.payments.application.port.input.ProcessPaymentUseCase;
import com.food.ordering.payments.application.port.input.RefundPaymentUseCase;
import com.food.ordering.payments.application.port.input.dto.PaymentResponse;
import com.food.ordering.payments.application.port.input.dto.ProcessPaymentCommand;
import com.food.ordering.payments.domain.model.PaymentMethod;
import com.food.ordering.payments.infrastructure.adapter.input.rest.dto.CreatePaymentRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final ProcessPaymentUseCase processPaymentUseCase;
    private final GetPaymentUseCase getPaymentUseCase;
    private final RefundPaymentUseCase refundPaymentUseCase;

    public PaymentController(ProcessPaymentUseCase processPaymentUseCase,
                             GetPaymentUseCase getPaymentUseCase,
                             RefundPaymentUseCase refundPaymentUseCase) {
        this.processPaymentUseCase = processPaymentUseCase;
        this.getPaymentUseCase = getPaymentUseCase;
        this.refundPaymentUseCase = refundPaymentUseCase;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody CreatePaymentRequest request) {
        log.info("Received payment request for order: {}", request.orderId());

        ProcessPaymentCommand command = new ProcessPaymentCommand(
                request.orderId(),
                request.userId(),
                request.amount(),
                PaymentMethod.valueOf(request.paymentMethod())
        );

        PaymentResponse response = processPaymentUseCase.process(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        log.info("Fetching payment with id: {}", id);
        PaymentResponse response = getPaymentUseCase.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable String orderId) {
        log.info("Fetching payment for order: {}", orderId);
        PaymentResponse response = getPaymentUseCase.getByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long id) {
        log.info("Received refund request for payment id: {}", id);
        PaymentResponse response = refundPaymentUseCase.refund(id);
        return ResponseEntity.ok(response);
    }
}
