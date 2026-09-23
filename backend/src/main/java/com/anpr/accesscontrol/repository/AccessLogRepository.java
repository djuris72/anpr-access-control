package com.anpr.accesscontrol.repository;

import com.anpr.accesscontrol.model.AccessLog;
import com.anpr.accesscontrol.model.enums.AccessStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

    // Za filtriranje istorije na admin panelu (npr. "pokazi sve odbijene danas")
    List<AccessLog> findByStatusAndTimestampBetween(
            AccessStatus status, LocalDateTime from, LocalDateTime to);

    List<AccessLog> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime from, LocalDateTime to);

    List<AccessLog> findTop50ByOrderByTimestampDesc();
}
