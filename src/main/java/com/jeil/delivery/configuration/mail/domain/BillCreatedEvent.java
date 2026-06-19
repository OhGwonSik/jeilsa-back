package com.jeil.delivery.configuration.mail.domain;

import com.jeil.delivery.billing.domain.BillVO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BillCreatedEvent {
    private final BillVO billVO;
}
