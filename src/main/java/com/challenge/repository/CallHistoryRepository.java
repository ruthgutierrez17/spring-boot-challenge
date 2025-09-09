package com.challenge.repository;

import com.challenge.entity.CallHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para el historial de llamadas con soporte para Specifications
 */
@Repository
public interface CallHistoryRepository extends JpaRepository<CallHistory, Long>,
                                              JpaSpecificationExecutor<CallHistory> {
}
