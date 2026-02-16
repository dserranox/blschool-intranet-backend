package ar.com.blschool.service;

import ar.com.blschool.entity.IAuditable;

import java.time.LocalDateTime;

public class BaseService {

    public BaseService() {}

    public void auditar(IAuditable domain, String userDomain) {
        if (domain.getAudFechaIns() == null && domain.getAudUsrIns() == null) {
            domain.setAudFechaIns(LocalDateTime.now());
            domain.setAudUsrIns(userDomain);
        }

        domain.setAudFechaUpd(LocalDateTime.now());
        domain.setAudUsrUpd(userDomain);
    }
}