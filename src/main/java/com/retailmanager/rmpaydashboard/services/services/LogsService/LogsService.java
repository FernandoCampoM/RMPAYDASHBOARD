package com.retailmanager.rmpaydashboard.services.services.LogsService;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.factory.LogsFactory;
import com.retailmanager.rmpaydashboard.repositories.LogsRepository;
import com.retailmanager.rmpaydashboard.repositories.TerminalRepository;
import com.retailmanager.rmpaydashboard.services.DTO.LogsDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class LogsService implements ILogsService {

    private final TerminalRepository serviceDBTerminal;
    private final LogsFactory logsFactory;
    private final LogsRepository logsRepository;

    @Override
    public ResponseEntity<?> add(LogsDTO logsDTO) {

        if (!serviceDBTerminal.existsById(logsDTO.getTerminalId())) {
            throw new EntidadNoExisteException(
                    "El Terminal con id " + logsDTO.getTerminalId() + " no existe en la Base de datos"
            );
        }

        var terminal = serviceDBTerminal.findById(logsDTO.getTerminalId()).orElse(null);

        var logs = logsFactory.toEntity(logsDTO);

        logs.setTerminal(terminal);

        logsRepository.save(logs);

        return new ResponseEntity<>(true, HttpStatus.CREATED);
    }

    @Override
    @Transactional
    public ResponseEntity<?> removeAllByTerminalId(String terminalId) {

        if (!serviceDBTerminal.existsById(terminalId)) {
            throw new EntidadNoExisteException(
                    "El Terminal con id " + terminalId + " no existe en la Base de datos"
            );
        }

        logsRepository.deleteAllByTerminal_TerminalId(terminalId);

        return ResponseEntity.ok(true);
    }

    @Override
    public ResponseEntity<?> getAllByTerminalId(String terminalId) {

        if (!serviceDBTerminal.existsById(terminalId)) {
            throw new EntidadNoExisteException(
                    "El Terminal con id " + terminalId + " no existe en la Base de datos"
            );
        }

        var logs = logsRepository.findAllByTerminal_TerminalIdOrderByCreatedAtDesc(
                terminalId
        );

        var response = logsFactory.toResponseDTOList(logs);

        return ResponseEntity.ok(response);
    }
}