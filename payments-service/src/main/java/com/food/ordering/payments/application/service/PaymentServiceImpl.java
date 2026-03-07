package com.food.ordering.payments.application.service;

import com.food.ordering.payments.application.port.input.GetPaymentUseCase;
import com.food.ordering.payments.application.port.input.ProcessPaymentUseCase;
import com.food.ordering.payments.application.port.input.RefundPaymentUseCase;
import com.food.ordering.payments.application.port.input.dto.PaymentResponse;
import com.food.ordering.payments.application.port.input.dto.ProcessPaymentCommand;
import com.food.ordering.payments.application.port.output.PaymentEventPublisherPort;
import com.food.ordering.payments.application.port.output.PaymentRepositoryPort;
import com.food.ordering.payments.domain.exception.PaymentNotFoundException;
import com.food.ordering.payments.domain.exception.PaymentProcessingException;
import com.food.ordering.payments.domain.model.Payment;
import com.food.ordering.payments.domain.model.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class PaymentServiceImpl implements ProcessPaymentUseCase, GetPaymentUseCase, RefundPaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepositoryPort paymentRepository;
    private final PaymentEventPublisherPort eventPublisher;

    public PaymentServiceImpl(PaymentRepositoryPort paymentRepository,
                              PaymentEventPublisherPort eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PaymentResponse process(ProcessPaymentCommand command) {
        log.info("Processing payment for order: {}", command.orderId());

        Payment payment = new Payment();
        payment.setOrderId(command.orderId());
        payment.setUserId(command.userId());
        payment.setAmount(command.amount());
        payment.setPaymentMethod(command.paymentMethod());
        payment.setStatus(new PaymentStatus.Pending());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        try {
            // Transition to PROCESSING
            payment.transitionTo(new PaymentStatus.Processing());
            payment = paymentRepository.save(payment);

            // Simulate payment processing - generate transaction ID
            String transactionId = UUID.randomUUID().toString();
            payment.setTransactionId(transactionId);

            // Transition to COMPLETED
            payment.transitionTo(new PaymentStatus.Completed());
            payment = paymentRepository.save(payment);

            log.info("Payment completed for order: {} with transactionId: {}", command.orderId(), transactionId);
            eventPublisher.publishPaymentCompleted(payment);

        } catch (Exception e) {
            log.error("Payment failed for order: {}", command.orderId(), e);

            payment.setStatus(new PaymentStatus.Failed());
            payment.setUpdatedAt(LocalDateTime.now());
            payment = paymentRepository.save(payment);

            eventPublisher.publishPaymentFailed(payment);

            throw new PaymentProcessingException("Payment processing failed for order: " + command.orderId(), e);
        }

        return toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getById(Long id) {
        log.info("Fetching payment with id: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
        return toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getByOrderId(String orderId) {
        log.info("Fetching payment for order: {}", orderId);
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException(orderId));
        return toResponse(payment);
    }

    @Override
    public PaymentResponse refund(Long id) {
        log.info("Processing refund for payment id: {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        payment.transitionTo(new PaymentStatus.Refunded());
        payment = paymentRepository.save(payment);

        log.info("Payment {} refunded successfully", id);
        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getPaymentMethod().name(),
                payment.getStatus().name(),
                payment.getTransactionId(),
                payment.getCreatedAt()
        );
    }
}
