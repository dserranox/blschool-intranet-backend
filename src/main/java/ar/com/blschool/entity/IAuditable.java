package ar.com.blschool.entity;

import java.time.LocalDateTime;
import java.util.Date;

public interface IAuditable {

    String getAudUsrIns();
    void setAudUsrIns(String var1);
    LocalDateTime getAudFechaIns();
    void setAudFechaIns(LocalDateTime var1);
    String getAudUsrUpd();
    void setAudUsrUpd(String var1);
    LocalDateTime getAudFechaUpd();
    void setAudFechaUpd(LocalDateTime var1);
}
