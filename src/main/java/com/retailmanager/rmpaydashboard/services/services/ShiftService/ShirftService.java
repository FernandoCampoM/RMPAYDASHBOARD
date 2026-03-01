package com.retailmanager.rmpaydashboard.services.services.ShiftService;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.DataInconsistencyException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadYaExisteException;
import com.retailmanager.rmpaydashboard.factory.ShiftFactory;
import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.SaleReport;
import com.retailmanager.rmpaydashboard.models.Shift;
import com.retailmanager.rmpaydashboard.models.Terminal;
import com.retailmanager.rmpaydashboard.models.UsersBusiness;
import com.retailmanager.rmpaydashboard.models.enums.SyncStatus;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.repositories.ShiftReporsitory;
import com.retailmanager.rmpaydashboard.repositories.TerminalRepository;
import com.retailmanager.rmpaydashboard.repositories.UsersAppRepository;
import com.retailmanager.rmpaydashboard.services.DTO.CloseShiftDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ShiftDTO;
import com.retailmanager.rmpaydashboard.utils.DateFormater;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;


@Service
public class ShirftService implements IShiftService {
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;
    @Autowired
    private TerminalRepository serviceDBTerminal;

    @Autowired
    private UsersAppRepository usersAppDBService;
    @Autowired
    private ShiftReporsitory serviceDBShift;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private ShiftFactory shiftFactory;

    /**
     * Creates a new shift or updates an existing open shift. This method ensures
     * that the user and terminal exist before creating or updating the shift.
     * If the shift already exists and is open, it updates its details and closes it.
     * Otherwise, it maps the provided ShiftDTO to a new Shift entity and saves it.
     * The operation is transactional to ensure atomicity.
     *
     * @param shiftDTO The data transfer object containing the shift details.
     * @return A ResponseEntity containing the created or updated ShiftDTO.
     * @throws EntidadNoExisteException If the user or terminal does not exist.
     * @throws EntidadYaExisteException If there is already an open shift for the user and terminal.
     */

    @Override
    @Transactional // Asegura que la operación sea atómica
    public ResponseEntity<?> createShift(ShiftDTO shiftDTO) {
        // 1. Validar existencia de usuario y terminal
        UsersBusiness userBusiness = usersAppDBService.findById(shiftDTO.userId())
                .orElseThrow(() -> new EntidadNoExisteException("User Business with ID " + shiftDTO.userId() + " does not exist."));

        // Asumo que Terminal tiene un método para buscar por deviceId (serial)
        Terminal terminal = serviceDBTerminal.findFirstBySerialAndBusiness(shiftDTO.deviceId(), userBusiness.getBusiness())
                .orElseThrow(() -> new EntidadNoExisteException("Terminal with device ID " + shiftDTO.deviceId() + " does not exist for Business " + userBusiness.getBusiness().getBusinessId()));
        if (!Objects.equals(terminal.getBusiness().getBusinessId(), userBusiness.getBusiness().getBusinessId())) {
            throw new DataInconsistencyException("El Empleado con id " + userBusiness.getUserBusinessId() + " y el terminal con id: " + terminal.getTerminalId() + " no pertenecen al mismo negocio");
        }
        // 2. Verificar si ya hay un turno abierto para este usuario y terminal
        Optional<Shift> existingOpenShift = serviceDBShift.findFirstByUserBusinessAndTerminal(userBusiness, terminal);
        if (existingOpenShift.isPresent() && existingOpenShift.get().isOpenShifBalance()) {
            throw new EntidadYaExisteException("There is already an open shift for employee user  ID " + shiftDTO.userId() + " and device whith serial ID " + shiftDTO.deviceId());
        }

        // 3. Mapear DTO a entidad
        Shift shift = convertToEntity(shiftDTO);

        // Setear las entidades completas
        shift.setUserBusiness(userBusiness);
        shift.setUserName(userBusiness.getUsername());
        shift.setTerminal(terminal);

        // El saleReport ya se mapea en convertToEntity, y la relación inversa se establece allí.

        // 4. Guardar el nuevo turno

        shift.setSyncStatus(SyncStatus.valueOf(shiftDTO.syncStatus()));
        shift.setLastSyncAt(Instant.now());
        Shift savedShift = serviceDBShift.save(shift);

        // 5. Retornar el DTO del turno creado
        ShiftDTO response = shiftFactory.toDTO(savedShift);
        //ShiftDTO response = new ShiftDTO(savedShift);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Updates an existing shift with the details provided in the ShiftDTO.
     * <p>
     * This method checks if the shift exists using the ID in the DTO. If the shift
     * does not exist, an exception is thrown. It then updates the updatable fields
     * of the existing shift entity using the DTO. Scalar fields are directly updated
     * using the ModelMapper. Complex fields, such as userBusinessId or terminalId,
     * require explicit handling if present in the DTO.
     * <p>
     * If the DTO contains a SaleReportDTO, this method either updates the existing
     * SaleReport or creates a new one if none exists. The inverse relationship is
     * also set. If the DTO does not include a SaleReport, and the entity has one,
     * it may be removed depending on business logic.
     * <p>
     * Finally, the updated shift is saved, and its DTO is returned.
     *
     * @param shiftDTO the DTO containing the updated shift details
     * @return a ResponseEntity containing the updated ShiftDTO or an error message
     * @throws IllegalArgumentException if the shift ID is null
     * @throws EntidadNoExisteException if the shift with the given ID does not exist
     */

    @Override
    @Transactional
    public ResponseEntity<?> updateShift(ShiftDTO shiftDTO) {
        // 1. Verificar si el turno existe
        String shiftId = shiftDTO.shiftId();
        if (shiftId == null) {
            throw new IllegalArgumentException("Shift ID cannot be null for update operation.");
        }
        Shift existingShift = serviceDBShift.findById(shiftId)
                .orElseThrow(() -> new EntidadNoExisteException("Shift with ID " + shiftId + " does not exist."));

        // 2. Mapear los campos actualizables del DTO a la entidad existente
        // ModelMapper puede manejar esto, pero para relaciones complejas o campos específicos,
        // es mejor hacerlo manualmente o con un mapper más customizado.
        existingShift.setBalanceFinal(shiftDTO.balanceFinal());
        existingShift.setCuadreFinal(shiftDTO.cuadreFinal());
        existingShift.setOpenShifBalance(shiftDTO.openShifBalance());
        existingShift.setEndTime(shiftDTO.endTime()); // Asegurarse de que se actualice la hora de cierre
        existingShift.setUserName(shiftDTO.userName());
        existingShift.setStartTime(shiftDTO.startTime()); // Asegurarse de que se actualice la hora de inicio
        existingShift.setBalanceInicial(shiftDTO.balanceInicial());


        // Asegurarse de que las relaciones no se sobrescriban incorrectamente si el DTO no las trae
        // Si el DTO no trae userBusinessId o terminalId, no deberíamos cambiarlos.
        // Si los trae, se deberían buscar y setear explícitamente como en createShift.

        // Si el DTO proporciona un SaleReportDTO, actualizar el SaleReport existente
        if (shiftDTO.saleReport() != null) {
            if (existingShift.getSaleReport() == null) {
                // Si no existía, crear uno nuevo y asociarlo
                SaleReport newSaleReport = mapper.map(shiftDTO.saleReport(), SaleReport.class);
                existingShift.setSaleReport(newSaleReport);
                newSaleReport.setShift(existingShift); // Establecer la relación inversa
            } else {
                // Si ya existía, actualizarlo con los datos del DTO
                mapper.map(shiftDTO.saleReport(), existingShift.getSaleReport());
            }
        } else {
            // Si el DTO no trae SaleReport, y la entidad lo tiene, podrías querer eliminarlo (orphanRemoval = true)
            // o dejarlo. Depende de la lógica de negocio.
            existingShift.setSaleReport(null); // Esto activaría orphanRemoval si está configurado
        }


        // El userName, startTime, endTime, balanceInicial, balanceFinal, cuadreFinal, statusShiftBalance
        // se actualizarán directamente por mapper.map(shiftDTO, existingShift);

        // 3. Guardar la entidad actualizada
        existingShift.setSyncStatus(SyncStatus.SYNCED);
        existingShift.setLastSyncAt(Instant.now());
        Shift updatedShift = serviceDBShift.save(existingShift);

        // 4. Retornar el DTO del turno actualizado
        ShiftDTO response = shiftFactory.toDTO(updatedShift);
        //ShiftDTO response = new ShiftDTO(updatedShift);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> updateStatusShift(String shiftId, String status) {
        boolean response = serviceDBShift.updateStatus(shiftId, SyncStatus.valueOf(status), Instant.now()) > 0;
        return new ResponseEntity<>(response, HttpStatus.OK);
       /* Shift shift = serviceDBShift.findById(shiftId)
                .orElseThrow(() -> new EntidadNoExisteException("Shift with ID " + shiftId + " does not exist."));
        shift.setSyncStatus(SyncStatus.valueOf(status));
        shift.setLastSyncAt(LocalDateTime.now());
        Shift updatedShift = serviceDBShift.save(shift);
        ShiftDTO response = new ShiftDTO(updatedShift);
        return new ResponseEntity<>(response, HttpStatus.OK);*/
    }


    /**
     * Elimina un turno por su ID.
     *
     * @param shiftId ID del turno a eliminar
     * @return ResponseEntity con un boolean que indica si el turno se eliminó (true) o no (false)
     * y un estado HTTP:
     * - 200 OK si se eliminó el turno
     * - 404 NOT_FOUND si no existe el turno con el ID proporcionado
     */

    @Override
    @Transactional
    public ResponseEntity<?> deleteShift(String shiftId) {
        // 1. Verificar si el turno existe
        Shift shiftToDelete = serviceDBShift.findById(shiftId)
                .orElse(null);
        if (shiftToDelete == null) {
            return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
        }

        // 2. Eliminar el turno
        serviceDBShift.delete(shiftToDelete);

        // 3. Retornar respuesta

        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    /**
     * Cierra un turno existente con el ID y detalles proporcionados en el ShiftDTO.
     * <p>
     * Si el turno existe y no está cerrado, se actualizarán sus campos con los del DTO.
     * Si el DTO proporciona un SaleReportDTO, se actualizará o creará el correspondiente SaleReport
     * en la entidad existente. Si no se proporciona, el SaleReport existente se dejará intacto.
     * <p>
     * Si no existe el turno con el ID proporcionado, se lanza una excepción.
     * <p>
     * Si el DTO no trae un shiftId, se asume que se quiere crear un nuevo turno cerrado con la
     * información proporcionada en el DTO. En este caso, se verificará la existencia del usuario
     * y terminal asociados y se lanzará una excepción si ya hay un turno abierto para ellos.
     *
     * @param shiftDTO el DTO con los detalles del turno a cerrar
     * @return un ResponseEntity con el DTO del turno cerrado o un mensaje de error
     * @throws EntidadNoExisteException si el turno no existe
     * @throws EntidadYaExisteException si ya hay un turno abierto para el usuario y terminal asociados
     * @throws IllegalArgumentException si el DTO no tiene los campos obligatorios
     * @throws IllegalStateException    si el turno ya está cerrado
     */
    @Override
    @Transactional
    public ResponseEntity<?> closeShift(ShiftDTO shiftDTO) {
        // Aquí la lógica es más compleja: el turno puede existir y estar abierto,
        // o el DTO podría traer toda la información para cerrar uno que no existe
        // o crear uno nuevo y cerrarlo.

        Shift shiftToClose;

        // Opción 1: El shiftDTO incluye un shiftId, buscar el turno existente
        if (shiftDTO.shiftId() != null) {
            if (shiftDTO.saleReport() == null) {
                throw new IllegalArgumentException("SaleReport cannot be null when closing a shift.");
            }
            if (shiftDTO.endTime() == null) {
                throw new IllegalArgumentException("End time cannot be null when closing a shift.");
            }
            if (shiftDTO.cuadreFinal() == null) {
                throw new IllegalArgumentException("Cuadre final cannot be null when closing a shift.");
            }
            if (shiftDTO.balanceFinal() == null) {
                throw new IllegalArgumentException("Balance final cannot be null when closing a shift.");
            }
            shiftToClose = serviceDBShift.findById(shiftDTO.shiftId())
                    .orElseThrow(() -> new EntidadNoExisteException("Shift with ID " + shiftDTO.shiftId() + " does not exist."));

            // Si el turno ya está cerrado, lanzar una excepción
            if (shiftToClose.getEndTime() != null && shiftToClose.isOpenShifBalance() == false) {
                throw new IllegalStateException("Shift with ID " + shiftDTO.shiftId() + " is already closed.");
            }

            // Actualizar los campos del turno existente con los del DTO
            shiftToClose.setBalanceFinal(shiftDTO.balanceFinal());
            shiftToClose.setCuadreFinal(shiftDTO.cuadreFinal());
            shiftToClose.setOpenShifBalance(shiftDTO.openShifBalance());
            shiftToClose.setEndTime(shiftDTO.endTime()); // Asegurarse de que se actualice la hora de cierre
            shiftToClose.setUserName(shiftDTO.userName());
            shiftToClose.setStartTime(shiftDTO.startTime()); // Asegurarse de que se actualice la hora de inicio
            shiftToClose.setBalanceInicial(shiftDTO.balanceInicial()); // Esto actualiza campos escalares


            // Si el DTO proporciona SaleReport, actualizar o crear
            if (shiftDTO.saleReport() != null) {
                if (shiftToClose.getSaleReport() == null) {
                    SaleReport newSaleReport = mapper.map(shiftDTO.saleReport(), SaleReport.class);
                    shiftToClose.setSaleReport(newSaleReport);
                    newSaleReport.setShift(shiftToClose);
                } else {
                    mapper.map(shiftDTO.saleReport(), shiftToClose.getSaleReport());
                }
            } else {
                // Si el DTO no trae SaleReport, pero la entidad tiene uno, puedes decidir si lo eliminas o no.
                // Aquí lo dejamos si existía, o lo creas si no existía (depende de si se puede cerrar un turno sin SaleReport).
                // Si debe haber un SaleReport al cerrar, deberías hacer que saleReport en ShiftDTO sea @NotNull para esta operación.
            }

        } else {
            // Opción 2: No hay shiftId, implica que el DTO trae toda la información para crear/cerrar un nuevo turno
            // Este caso es menos común para "cerrar", más para "abrir y cerrar inmediatamente" o "crear un turno cerrado".
            // Podrías decidir que esta operación SIEMPRE requiere un shiftId existente.
            // Para este ejemplo, lo manejaremos como una creación de un turno ya cerrado.

            // Validar existencia de usuario y terminal (como en createShift)
            UsersBusiness userBusiness = usersAppDBService.findById(shiftDTO.userId())
                    .orElseThrow(() -> new EntidadNoExisteException("User Business with ID " + shiftDTO.userId() + " does not exist."));

            Terminal terminal = serviceDBTerminal.findFirstBySerial(shiftDTO.deviceId())
                    .orElseThrow(() -> new EntidadNoExisteException("Terminal with serial ID " + shiftDTO.deviceId() + " does not exist."));

            // Verificar si ya hay un turno abierto para este usuario y terminal (prevención de duplicados)
            Optional<Shift> existingOpenShift = serviceDBShift.findFirstByUserBusinessAndTerminal(userBusiness, terminal);
            if (existingOpenShift.isPresent() && existingOpenShift.get().isOpenShifBalance()) {
                throw new EntidadYaExisteException("There is already an open shift for employee user ID " + shiftDTO.userId() + " and device whit serial ID " + shiftDTO.deviceId() + ". Please close it first or provide its ID to update.");
            }

            shiftToClose = convertToEntity(shiftDTO);
            shiftToClose.setShiftId(null); // Asegurar que sea nulo para nueva creación

            shiftToClose.setUserBusiness(userBusiness);
            shiftToClose.setTerminal(terminal);

            // SaleReport se mapea en convertToEntity
        }

        // Guardar el turno (ya sea actualizado o nuevo)
        shiftToClose.setSyncStatus(SyncStatus.SYNCED);
        shiftToClose.setLastSyncAt(Instant.now());

        Shift closedShift = serviceDBShift.save(shiftToClose);

        ShiftDTO response = shiftFactory.toDTO(closedShift);
        //ShiftDTO response = new ShiftDTO(closedShift);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> closeShiftWeb(CloseShiftDTO closeShiftDTO) {
        Shift shiftToClose;
        shiftToClose = serviceDBShift.findById(closeShiftDTO.getShiftId())
                .orElseThrow(() -> new EntidadNoExisteException("Shift with ID " + closeShiftDTO.getShiftId() + " does not exist."));

        var saleReport = serviceDBShift.findSaleReport(shiftToClose.getUserBusiness().getUserBusinessId(), shiftToClose.getShiftId(), Instant.now());
        shiftToClose.setSaleReport(new SaleReport(saleReport, shiftToClose));

        shiftToClose.setBalanceFinal(closeShiftDTO.getBalanceFinal());

        //finalSquare = currentCash - (entryCash + saleCash - refundsCash)
        BigDecimal currentCash = closeShiftDTO.getBalanceFinal();
        BigDecimal entryCash = shiftToClose.getBalanceInicial();
        BigDecimal saleCash = shiftToClose.getSaleReport().getSaleCash();
        BigDecimal refundsCash = shiftToClose.getSaleReport().getRefundCash();

        BigDecimal expectedValue = entryCash.add(saleCash).subtract(refundsCash);
        BigDecimal finalSquare = currentCash.subtract(expectedValue);

        shiftToClose.setCuadreFinal(finalSquare);
        shiftToClose.setOpenShifBalance(false);
        shiftToClose.setEndTime(Instant.now());

        /*
        var saleReport = serviceDBShift.findSaleReport(shiftToClose.getUserBusiness().getUserBusinessId(), shiftToClose.getShiftId());
        shiftToClose.setSaleReport(new SaleReport(
                null,
                saleReport.getSaleCash(),
                saleReport.getSaleCredit(),
                saleReport.getSaleDebit(),
                saleReport.getSaleATH(),
                saleReport.getRefundCash(),
                saleReport.getRefundCredit(),
                saleReport.getRefundDebit(),
                saleReport.getRefundATH(),
                saleReport.getStateTax(),
                saleReport.getCityTax(),
                saleReport.getReduceTax(),
                shiftToClose
        ));*/
        shiftToClose.setSyncStatus(SyncStatus.PENDING);
        Shift closedShift = serviceDBShift.save(shiftToClose);
        return getShiftById(closedShift.getShiftId());
    }


    /**
     * Retrieves a shift by its ID and returns a ResponseEntity containing the ShiftDTO.
     * If the shift is not found, an EntidadNoExisteException is thrown.
     *
     * @param shiftId the ID of the shift to retrieve
     * @return a ResponseEntity containing the ShiftDTO of the found shift
     * @throws EntidadNoExisteException if the shift with the specified ID does not exist
     */

    @Override
    public ResponseEntity<?> getShiftById(String shiftId) {
        // 1. Buscar el turno por ID
        Shift shift = serviceDBShift.findById(shiftId)
                .orElseThrow(() -> new EntidadNoExisteException("Shift with ID " + shiftId + " does not exist."));

        ShiftDTO response = shiftFactory.toDTO(shift);
        //ShiftDTO response = new ShiftDTO(shift);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Retrieves a paginated list of Shift entities that match the given criteria and returns a ResponseEntity containing the Page of ShiftDTOs.
     * The businessId is a mandatory filter for all queries.
     *
     * @param businessId      The ID of the business to which the shifts belong (mandatory).
     * @param employeeId      the ID of the employee user to filter by (optional).
     * @param serialNumber    the serial number of the terminal to filter by (optional).
     * @param startDate       the start date and time to filter by (inclusive, optional).
     * @param endDate         the end date and time to filter by (exclusive, optional).
     * @param openShifBalance the status of the shift balance to filter by (optional). // Vuelto a OpenShifBalance
     * @param pageable        the Pageable object containing pagination information.
     * @return a ResponseEntity containing the Page of ShiftDTOs matching the criteria.
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllShiftsPageable(Long businessId, // Nuevo parámetro obligatorio
                                                  Long employeeId,
                                                  String serialNumber,
                                                  Instant startDate,
                                                  Instant endDate,
                                                  Boolean openShifBalance, // Vuelto a OpenShifBalance
                                                  Pageable pageable) {
        // --- 1. Obtención de Entidades (Business, UsersBusiness y Terminal) ---
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new EntidadNoExisteException("Business with ID " + businessId + " does not exist."));

        UsersBusiness userBusiness = null;
        if (employeeId != null) {
            userBusiness = usersAppDBService.findById(employeeId)
                    .orElseThrow(() -> new EntidadNoExisteException("User Employee with ID " + employeeId + " does not exist."));
            // Opcional: Podrías añadir una validación aquí para asegurarte de que userBusiness.business() coincida con el 'business' proporcionado.
            // if (!userBusiness.business().equals(business)) {
            //     throw new IllegalArgumentException("User employee does not belong to the specified business.");
            // }
        }

        Terminal terminal = null;
        if (serialNumber != null && !serialNumber.isEmpty()) {
            terminal = serviceDBTerminal.findFirstBySerial(serialNumber)
                    .orElseThrow(() -> new EntidadNoExisteException("Terminal with serial " + serialNumber + " does not exist."));
            // Opcional: Validar que el terminal pertenezca al negocio.
            if (!terminal.getBusiness().equals(business)) {
                throw new IllegalArgumentException("Terminal does not belong to the specified business.");
            }
        }

        // --- 2. Parseo de Fechas ---
        Instant startDateTime = DateFormater.startOfDayUtc(startDate);
        Instant endDateTime = DateFormater.endOfDayUtc(endDate);
        /*String userTimeZone = "America/Puerto_Rico";
        OffsetDateTime startDateTimeZone = null;
        OffsetDateTime endDateTimeZone = null;


        try {
            ZoneId userZone = ZoneId.of(userTimeZone);
            ZoneId utc = ZoneId.of("UTC");

            if (startDate != null && !startDate.isEmpty()) {
                startDateTimeZone = LocalDate.parse(startDate)
                        .atStartOfDay(userZone)
                        .withZoneSameInstant(utc)
                        .toOffsetDateTime();
            }

            if (endDate != null && !endDate.isEmpty()) {
                endDateTimeZone = LocalDate.parse(endDate)
                        .atTime(23, 59, 59)
                        .atZone(userZone)
                        .withZoneSameInstant(utc)
                        .toOffsetDateTime();
            }
            nowDateTimeZone = LocalDate.now().atStartOfDay(userZone)
                    .withZoneSameInstant(utc)
                    .toOffsetDateTime();

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date format. Please use yyyy-MM-dd for startDate and endDate."
            );
        }
        LocalDateTime startDateTime = null;
        if (startDateTimeZone != null) {
            startDateTime = startDateTimeZone.toLocalDateTime();
        }

        LocalDateTime endDateTime = null;
        if (endDateTimeZone != null) {
            endDateTime = endDateTimeZone.toLocalDateTime();
        }*/

        /*LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        try {
            if (startDate != null && !startDate.isEmpty()) {
                startDateTime = LocalDate.parse(startDate).atStartOfDay();
                System.out.println("Parsed startDateTime: " + startDateTime); // Debugging line
                System.out.println("Date Start to parse: " + startDate); // Debugging line
            }
            if (endDate != null && !endDate.isEmpty()) {
                endDateTime = LocalDate.parse(endDate).atTime(23, 59, 59);
                System.out.println("Parsed endDateTime: " + endDateTime); // Debugging line
                System.out.println("Date End to parse: " + endDate); // Debugging line
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please useAPAC-MM-dd for startDate and endDate.");
        }*/

        Page<Shift> shiftsPage; // Ahora esperamos un objeto Page

        // --- 3. Lógica de Búsqueda Combinada con Paginación y openShifBalance ---
        // Todos los métodos de repositorio ahora deben comenzar con "findByTerminal_BusinessAnd..." o similar.
        // Dado que un Shift está relacionado con un Terminal, y un Terminal con un Business,
        // Spring Data JPA puede inferir la condición 'findByTerminal_Business'.

        // Se priorizan las combinaciones más completas
        if (userBusiness != null && terminal != null && startDateTime != null && endDateTime != null && openShifBalance != null) {
            shiftsPage = serviceDBShift.findByTerminal_BusinessAndUserBusinessAndTerminalAndStartTimeBetweenAndOpenShifBalance(business, userBusiness, terminal, startDateTime, endDateTime, openShifBalance, pageable);
        } else if (userBusiness != null && terminal != null && startDateTime != null && endDateTime != null) {
            shiftsPage = serviceDBShift.findByTerminal_BusinessAndUserBusinessAndTerminalAndStartTimeBetween(business, userBusiness, terminal, startDateTime, endDateTime, pageable);
        } else if (userBusiness != null && terminal != null && openShifBalance != null) {
            shiftsPage = serviceDBShift.findByTerminal_BusinessAndUserBusinessAndTerminalAndOpenShifBalance(business, userBusiness, terminal, openShifBalance, pageable);
        } else if (userBusiness != null && startDateTime != null && endDateTime != null && openShifBalance != null) {
            shiftsPage = serviceDBShift.findByTerminal_BusinessAndUserBusinessAndStartTimeBetweenAndOpenShifBalance(business, userBusiness, startDateTime, endDateTime, openShifBalance, pageable);
        } else if (terminal != null && startDateTime != null && endDateTime != null && openShifBalance != null) {
            shiftsPage = serviceDBShift.findByTerminal_BusinessAndTerminalAndStartTimeBetweenAndOpenShifBalance(business, terminal, startDateTime, endDateTime, openShifBalance, pageable);
        } else if (userBusiness != null && openShifBalance != null) {
            shiftsPage = serviceDBShift.findByTerminal_BusinessAndUserBusinessAndOpenShifBalance(business, userBusiness, openShifBalance, pageable);
        } else if (terminal != null && openShifBalance != null) {
            shiftsPage = serviceDBShift.findByTerminal_BusinessAndTerminalAndOpenShifBalance(business, terminal, openShifBalance, pageable);
        } else if (startDateTime != null && endDateTime != null && openShifBalance != null) {
            shiftsPage = serviceDBShift.findByTerminal_BusinessAndStartTimeBetweenAndOpenShifBalance(business, startDateTime, endDateTime, openShifBalance, pageable);
        } else if (userBusiness != null && terminal != null) {
            shiftsPage = serviceDBShift.findByTerminal_BusinessAndUserBusinessAndTerminal(business, userBusiness, terminal, pageable);
        } else if (userBusiness != null && startDateTime != null && endDateTime != null) {
            shiftsPage = serviceDBShift.findByTerminal_BusinessAndUserBusinessAndStartTimeBetween(business, userBusiness, startDateTime, endDateTime, pageable);
        } else if (terminal != null && startDateTime != null && endDateTime != null) {
            shiftsPage = serviceDBShift.findByTerminal_BusinessAndTerminalAndStartTimeBetween(business, terminal, startDateTime, endDateTime, pageable);
        } else if (userBusiness != null) {
            shiftsPage = serviceDBShift.findByTerminal_BusinessAndUserBusiness(business, userBusiness, pageable);
        } else if (terminal != null) {
            shiftsPage = serviceDBShift.findByTerminal_BusinessAndTerminal(business, terminal, pageable);
        } else if (startDateTime != null && endDateTime != null) {
            shiftsPage = serviceDBShift.findByTerminal_BusinessAndStartTimeBetween(business, startDateTime, endDateTime, pageable);
        } else if (openShifBalance != null) { // Caso base para solo openShifBalance
            shiftsPage = serviceDBShift.findByTerminal_BusinessAndOpenShifBalance(business, openShifBalance, pageable);
        } else {
            // Caso base: solo businessId y paginación
            shiftsPage = serviceDBShift.findByTerminal_Business(business, pageable);
        }

        Page<ShiftDTO> shiftDTOsPage = shiftsPage.map(shiftFactory::toDTO);
        if (openShifBalance != null && openShifBalance) {
            /*shiftDTOsPage.get().forEach(s -> {
                var now = Instant.now();
                var saleReport = serviceDBShift.findSaleReport(s.userId(), s.shiftId(), now);
                SaleReportDTO saleReportDTO = shiftFactory.toReportProjectionDTO(saleReport);
                s.withSaleReport(saleReportDTO);
                s.withSaleReport(shiftFactory.toReportProjectionDTO(saleReport));
            });*/

            var now = Instant.now();

            shiftDTOsPage = shiftDTOsPage.map(s -> {
                var saleReport = serviceDBShift.findSaleReport(s.userId(), s.shiftId(), now);
                var saleReportDTO = shiftFactory.toReportProjectionDTO(saleReport);
                return s.withSaleReport(saleReportDTO);
            });

        }

        return new ResponseEntity<>(shiftDTOsPage, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllShiftsSync(String terminalId) {
        var listShifts = serviceDBShift.findAllShift(terminalId);
        var listShiftsDto = listShifts.stream().map(shiftFactory::toDTO);
        return new ResponseEntity<>(listShiftsDto, HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateShiftSync(ShiftDTO shiftDTO) {
        Shift shiftToClose;
        shiftToClose = serviceDBShift.findById(shiftDTO.shiftId())
                .orElse(null);

        if (shiftToClose == null) return createShift(shiftDTO);

        if (shiftDTO.saleReport() == null) {
            var saleReport = serviceDBShift.findSaleReport(shiftToClose.getUserBusiness().getUserBusinessId(), shiftToClose.getShiftId(), shiftDTO.endTime());
            shiftToClose.setSaleReport(new SaleReport(saleReport, shiftToClose));
        } else {
            shiftToClose.setSaleReport(shiftFactory.toSaleReportEntity(shiftDTO.saleReport(), shiftToClose));
        }

        shiftToClose.setBalanceFinal(shiftDTO.balanceFinal());

        //finalSquare = currentCash - (entryCash + saleCash - refundsCash)
        BigDecimal currentCash = shiftDTO.balanceFinal();
        BigDecimal entryCash = shiftToClose.getBalanceInicial();
        BigDecimal saleCash = shiftToClose.getSaleReport().getSaleCash();
        BigDecimal refundsCash = shiftToClose.getSaleReport().getRefundCash();

        BigDecimal expectedValue = entryCash.add(saleCash).subtract(refundsCash);
        BigDecimal finalSquare = currentCash.subtract(expectedValue);

        shiftToClose.setCuadreFinal(finalSquare);
        shiftToClose.setOpenShifBalance(false);
        shiftToClose.setEndTime(shiftDTO.endTime());

        shiftToClose.setSyncStatus(SyncStatus.SYNCED);
        shiftToClose.setLastSyncAt(Instant.now());
        Shift closedShift = serviceDBShift.save(shiftToClose);
        ShiftDTO shiftDTO1 = shiftFactory.toDTO(closedShift);
        return new ResponseEntity<>(shiftDTO1, HttpStatus.OK);
    }

    /*
    public ResponseEntity<?> updateShiftSync(CloseShiftDTO closeShiftDTO) {
        Shift shiftToClose;
        shiftToClose = serviceDBShift.findById(closeShiftDTO.getShiftId())
                .orElseThrow(() -> new EntidadNoExisteException("Shift with ID " + closeShiftDTO.getShiftId() + " does not exist."));


        var saleReport = serviceDBShift.findSaleReport(shiftToClose.getUserBusiness().getUserBusinessId(), shiftToClose.getShiftId(),DateFormater.endOfDayUtc(closeShiftDTO.getEndTime()));
        shiftToClose.setSaleReport(new SaleReport(saleReport, shiftToClose));

        shiftToClose.setBalanceFinal(closeShiftDTO.getBalanceFinal());

        //finalSquare = currentCash - (entryCash + saleCash - refundsCash)
        BigDecimal currentCash = closeShiftDTO.getBalanceFinal();
        BigDecimal entryCash = shiftToClose.getBalanceInicial();
        BigDecimal saleCash = shiftToClose.getSaleReport().getSaleCash();
        BigDecimal refundsCash = shiftToClose.getSaleReport().getRefundCash();

        BigDecimal expectedValue = entryCash.add(saleCash).subtract(refundsCash);
        BigDecimal finalSquare = currentCash.subtract(expectedValue);

        shiftToClose.setCuadreFinal(finalSquare);
        shiftToClose.setOpenShifBalance(false);
        shiftToClose.setEndTime(closeShiftDTO.getEndTime());

        shiftToClose.setSyncStatus(SyncStatus.SYNCED);
        shiftToClose.setLastSyncAt(Instant.now());
        Shift closedShift = serviceDBShift.save(shiftToClose);
        return getShiftById(closedShift.getShiftId());
    }*/


    // Métodos para convertir DTO a Entidad y viceversa
    private Shift convertToEntity(ShiftDTO shiftDTO) {
        //Shift shift = mapper.map(shiftDTO, Shift.class);
        Shift shift = shiftFactory.toEntity(shiftDTO);
        shift.setOpenShifBalance(shiftDTO.openShifBalance());
        // ModelMapper no puede mapear automáticamente entidades relacionadas por ID
        // Debemos buscar y setear las entidades completas aquí
        if (shiftDTO.userId() != null) {
            UsersBusiness userBusiness = usersAppDBService.findById(shiftDTO.userId())
                    .orElseThrow(() -> new EntidadNoExisteException("User Employee with ID " + shiftDTO.userId() + " does not exist."));
            shift.setUserBusiness(userBusiness);
        }

        if (shiftDTO.deviceId() != null) {
            // Asumo que Terminal tiene un campo 'serialNumber' o 'deviceId' y un método para buscarlo
            // O que deviceId en el DTO es el ID de la Terminal. Si es el serial, ajusta el findBy
            Terminal terminal = serviceDBTerminal.findFirstBySerial(shiftDTO.deviceId()) // Asumo findBySerialNumber o findByDeviceId
                    .orElseThrow(() -> new EntidadNoExisteException("Terminal with serial ID " + shiftDTO.deviceId() + " does not exist."));
            shift.setTerminal(terminal);
        }

        // Mapear SaleReportDTO a SaleReport entity si existe
        if (shiftDTO.saleReport() != null) {
            SaleReport saleReport = mapper.map(shiftDTO.saleReport(), SaleReport.class);
            shift.setSaleReport(saleReport);
            saleReport.setShift(shift); // Establecer la relación inversa
        }


        return shift;
    }


}
