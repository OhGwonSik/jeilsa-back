package com.common.auth.common.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.common.auth.audit.component.AuditTargetCache;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuditTargetScheduler {
    private final AuditTargetCache auditTargetCache;

    @Scheduled(cron = "0 0 * * * *")
    public void refreshAuditTargets() {
        auditTargetCache.refreshCache();
    }
}


