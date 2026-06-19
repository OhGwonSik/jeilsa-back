package com.jeil.delivery.configuration.mail.handler;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.jeil.delivery.billing.domain.BillVO;
import com.jeil.delivery.configuration.mail.domain.BillCreatedEvent;
import com.jeil.delivery.configuration.mail.service.MailService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BillMailEventHandler {

    private final MailService mailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleBillCreated(BillCreatedEvent event) {
        BillVO billVO = event.getBillVO();
        mailService.invoiceMailSend(billVO);
    }
}
