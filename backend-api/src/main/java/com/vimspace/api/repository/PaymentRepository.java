package com.vimspace.api.repository;

import com.vimspace.api.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    /**
     * Total cash tendered by a sale that belongs to the given till session.
     * Method names are matched case-insensitively by the payments table.
     */
    @Query("select coalesce(sum(p.amount), 0) from PaymentEntity p " +
           "where p.sale.sessionId = :sessionId and lower(p.method) = 'cash'")
    BigDecimal sumCashBySessionId(@Param("sessionId") Long sessionId);

    /**
     * Total tendered (any method) for a till session.
     */
    @Query("select coalesce(sum(p.amount), 0) from PaymentEntity p where p.sale.sessionId = :sessionId")
    BigDecimal sumAllBySessionId(@Param("sessionId") Long sessionId);

    /**
     * Cash tendered for a till session broken down by payment method.
     */
    @Query("select p.method, sum(p.amount) from PaymentEntity p where p.sale.sessionId = :sessionId group by p.method")
    java.util.List<Object[]> totalsByMethodForSession(@Param("sessionId") Long sessionId);
}
