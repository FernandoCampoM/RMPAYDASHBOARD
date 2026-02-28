package com.retailmanager.rmpaydashboard.controller;

import com.retailmanager.rmpaydashboard.services.DTO.CloseShiftDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.retailmanager.rmpaydashboard.services.DTO.ShiftDTO;
import com.retailmanager.rmpaydashboard.services.services.ShiftService.IShiftService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@RestController
@RequestMapping("/api/shifts") // Usar /api/shifts como base para los endpoints de turnos
@Validated // Permite la validación a nivel de clase/método si no se usa @Valid en @RequestBody
public class ShiftController {

    @Autowired
    private IShiftService shiftService; // Inyecta la interfaz del servicio

    /**
     * Endpoint para crear un nuevo turno.
     * Recibe un ShiftDTO con la información del turno.
     *
     * @param shiftDTO El DTO del turno a crear. Debe pasar las validaciones de @Valid.
     * @return ResponseEntity con el ShiftDTO creado o un mensaje de error.
     * HTTP 200 OK si se crea exitosamente.
     * Posibles errores: 400 Bad Request (validación), 404 Not Found (usuario/terminal no existe),
     * 409 Conflict (turno abierto existente).
     */
    @PostMapping
    public ResponseEntity<?> createShift(@Valid @RequestBody ShiftDTO shiftDTO) {
        return shiftService.createShift(shiftDTO);
    }

    /**
     * Endpoint para actualizar un turno existente.
     * Requiere el shiftId en el ShiftDTO y utiliza el cuerpo de la solicitud para la actualización.
     *
     * @param shiftDTO El DTO del turno con la información actualizada. El 'shiftId' es obligatorio.
     * @return ResponseEntity con el ShiftDTO actualizado o un mensaje de error.
     * HTTP 200 OK si se actualiza exitosamente.
     * Posibles errores: 400 Bad Request (validación, shiftId nulo), 404 Not Found (turno no existe).
     */
    @PutMapping("/{shiftId}")
    public ResponseEntity<?> updateShift( @PathVariable String shiftId,@Valid @RequestBody ShiftDTO shiftDTO) {
        //shiftDTO.setShiftId(shiftId); // Asegura que el shiftId del DTO coincida con el de la ruta
        return shiftService.updateShift(shiftDTO);
    }

    @PutMapping("/close/wb/changeStatus")
    public ResponseEntity<?> updateStatusShift(
            @RequestParam String shiftId,
            @RequestParam String status) {
        return shiftService.updateStatusShift(shiftId, status);
    }

    /**
     * Endpoint para eliminar un turno por su ID.
     *
     * @param shiftId El ID del turno a eliminar, recibido como un parámetro de ruta.
     * @return ResponseEntity con un booleano (true/false) indicando el éxito de la eliminación.
     * HTTP 200 OK si se elimina exitosamente.
     * HTTP 404 Not Found si el turno no existe.
     */
    @DeleteMapping("/{shiftId}")
    public ResponseEntity<?> deleteShift(@PathVariable String shiftId) {
        return shiftService.deleteShift(shiftId);
    }

    /**
     * Endpoint para cerrar un turno existente o, en algunos casos, crear y cerrar un turno.
     * Si 'shiftId' se proporciona en el DTO, busca y cierra ese turno.
     * Si 'shiftId' es nulo, intenta crear un nuevo turno ya cerrado.
     *
     * @param shiftDTO El DTO con la información del turno a cerrar.
     * @return ResponseEntity con el ShiftDTO del turno cerrado o un mensaje de error.
     * HTTP 200 OK si se cierra exitosamente.
     * Posibles errores: 400 Bad Request (validación, campos nulos al cerrar),
     * 404 Not Found (turno, usuario o terminal no existe),
     * 409 Conflict (turno ya abierto para el usuario/terminal si se intenta crear/cerrar),
     * 409 Conflict (turno ya cerrado si se intenta cerrar uno ya cerrado).
     */
    @PutMapping("/close") // O @PostMapping si prefieres que sea idempotente
    public ResponseEntity<?> closeShift(@Valid @RequestBody ShiftDTO shiftDTO) {
        return shiftService.closeShift(shiftDTO);
    }

    @PutMapping("/close/wb") // O @PostMapping si prefieres que sea idempotente
    public ResponseEntity<?> closeShift(@Valid @RequestBody CloseShiftDTO closeShiftDTO) {
        return shiftService.closeShiftWeb(closeShiftDTO);
    }

    /**
     * Endpoint para obtener un turno por su ID.
     *
     * @param shiftId El ID del turno a buscar, recibido como un parámetro de ruta.
     * @return ResponseEntity con el ShiftDTO del turno encontrado.
     * HTTP 200 OK si se encuentra el turno.
     * HTTP 404 Not Found si el turno no existe.
     */
    @GetMapping("/{shiftId}")
    public ResponseEntity<?> getShiftById(@PathVariable String shiftId) {
        return shiftService.getShiftById(shiftId);
    }

    /**
     * Endpoint para obtener una lista paginada de turnos, con filtros opcionales.
     *
     * * @param businessId (Obligatorio) El ID del negocio al que pertenecen los turnos.
     * @param employeeId (Opcional) ID del empleado para filtrar turnos.
     * @param serialNumber (Opcional) Número de serie de la terminal para filtrar turnos.
     * @param startDate (Opcional) Fecha de inicio del rango (formato "yyyy-MM-dd").
     * @param endDate (Opcional) Fecha de fin del rango (formato "yyyy-MM-dd").
     * @param statusShiftBalance (Opcional) Estado del balance del turno para filtrar (true/false).
     * @param pageable Objeto Pageable para configurar la paginación (page, size, sort).
     * Ejemplo de llamada: /api/shifts?page=0&size=10&sort=startTime,desc
     * @return ResponseEntity con una página de ShiftDTOs que coinciden con los criterios.
     * HTTP 200 OK con la lista de turnos.
     * Posibles errores: 400 Bad Request (formato de fecha inválido),
     * 404 Not Found (empleado/terminal no existe si los IDs son proporcionados).
     */
    @GetMapping
    public ResponseEntity<?> getAllShiftsPageable(
        @RequestParam @NotNull(message = "Business ID cannot be null") 
        Long businessId, // Obligatorio y validado
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate,
            @RequestParam(required = false) Boolean statusShiftBalance,
            Pageable pageable) { // Spring Data JPA inyecta automáticamente el Pageable
        return shiftService.getAllShiftsPageable(businessId, employeeId, serialNumber, startDate, endDate, statusShiftBalance, pageable);
    }

    @GetMapping("/close/wb/all")
    public ResponseEntity<?> getAllShiftsSync(@RequestParam String terminalId) {
        return shiftService.getAllShiftsSync(terminalId);
    }

    @PutMapping("/close/wb/update")
    public ResponseEntity<?> updateShiftSync( @RequestBody ShiftDTO shiftDTO) {
        return shiftService.updateShiftSync(shiftDTO);
    }
}
