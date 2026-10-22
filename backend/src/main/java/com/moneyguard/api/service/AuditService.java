package com.moneyguard.api.service;

import com.moneyguard.api.domain.AuditEvent;
import com.moneyguard.api.repo.AuditRepo;
import com.moneyguard.api.util.Crypto;
import com.moneyguard.api.util.Json;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuditService {

    private final AuditRepo repo;
    private final Json json;

    public AuditService(AuditRepo repo, Json json) {
        this.repo = repo;
        this.json = json;
    }

    public void log(HttpServletRequest req, String actorUserId, String action, String entityType, String entityId, Map<String, Object> meta) {
        AuditEvent ev = new AuditEvent();
        ev.setId(Crypto.uuid());
        ev.setActorUserId(actorUserId);
        ev.setActorDeviceId(req.getHeader("X-Device-Id"));
        ev.setAction(action);
        ev.setEntityType(entityType);
        ev.setEntityId(entityId);
        ev.setIpAddress(req.getRemoteAddr());
        ev.setUserAgent(req.getHeader("User-Agent"));
        if (meta != null) ev.setMetadataJson(json.stringify(meta));
        repo.save(ev);
    }
}
