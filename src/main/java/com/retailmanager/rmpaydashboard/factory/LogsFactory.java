package com.retailmanager.rmpaydashboard.factory;

import com.retailmanager.rmpaydashboard.models.Logs;
import com.retailmanager.rmpaydashboard.services.DTO.LogsDTO;
import com.retailmanager.rmpaydashboard.services.DTO.LogsResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LogsFactory {
    public Logs toEntity(LogsDTO dto) {
        return Optional.ofNullable(dto)
                .map(entity -> new Logs(
                        null,
                        entity.getLevel(),
                        entity.getTag(),
                        entity.getMessage(),
                        entity.getStacktrace(),
                        entity.getSerial(),
                        null,
                        null,
                        null
                )).orElse(null);
    }

    public LogsResponseDTO toResponseDTO(Logs entity) {

        return Optional.ofNullable(entity)
                .map(log -> new LogsResponseDTO(
                        log.getId(),
                        log.getLevel(),
                        log.getTag(),
                        log.getMessage(),
                        log.getStacktrace(),
                        log.getSerial(),
                        log.getCreatedAt()
                )).orElse(null);
    }

    public List<LogsResponseDTO> toResponseDTOList(List<Logs> logs) {
        return logs.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}
