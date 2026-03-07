package com.food.ordering.payments.infrastructure.adapter.output.mapper;

import com.food.ordering.payments.domain.model.Payment;
import com.food.ordering.payments.domain.model.PaymentMethod;
import com.food.ordering.payments.domain.model.PaymentStatus;
import com.food.ordering.payments.infrastructure.adapter.output.persistence.PaymentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "paymentMethod", source = "paymentMethod", qualifiedByName = "paymentMethodToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    PaymentEntity toEntity(Payment payment);

    @Mapping(target = "paymentMethod", source = "paymentMethod", qualifiedByName = "stringToPaymentMethod")
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    Payment toDomain(PaymentEntity entity);

    @Named("paymentMethodToString")
    default String paymentMethodToString(PaymentMethod paymentMethod) {
        return paymentMethod != null ? paymentMethod.name() : null;
    }

    @Named("stringToPaymentMethod")
    default PaymentMethod stringToPaymentMethod(String paymentMethod) {
        return paymentMethod != null ? PaymentMethod.valueOf(paymentMethod) : null;
    }

    @Named("statusToString")
    default String statusToString(PaymentStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("stringToStatus")
    default PaymentStatus stringToStatus(String status) {
        return status != null ? PaymentStatus.fromString(status) : null;
    }
}
