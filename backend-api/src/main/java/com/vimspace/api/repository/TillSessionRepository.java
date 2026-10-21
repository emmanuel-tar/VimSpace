package com.vimspace.api.repository;

import com.vimspace.api.entity.TillSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TillSessionRepository extends JpaRepository<TillSessionEntity, Long> {
    Optional<TillSessionEntity> findTopByTerminalIdAndStatusOrderByIdDesc(String terminalId, String status);

    List<TillSessionEntity> findByStatusOrderByIdDesc(String status);

    List<TillSessionEntity> findAllByOrderByIdDesc();
}

