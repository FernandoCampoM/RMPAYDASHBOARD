package com.retailmanager.rmpaydashboard.repositories;

import com.retailmanager.rmpaydashboard.models.Logs;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LogsRepository extends CrudRepository<Logs, Long> {

    List<Logs> findAllByTerminal_TerminalIdOrderByCreatedAtDesc(String terminalId);

    void deleteAllByTerminal_TerminalId(String terminalId);
}