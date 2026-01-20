package com.retailmanager.rmpaydashboard.services.services.ScheduleCalendar;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.models.ScheduleCalendar;
import com.retailmanager.rmpaydashboard.models.ScheduleDetailedCalendar;
import com.retailmanager.rmpaydashboard.models.ScheduleListDetailedCalendar;
import com.retailmanager.rmpaydashboard.models.UsersBusiness;
import com.retailmanager.rmpaydashboard.repositories.ScheduleCalendarRepository;
import com.retailmanager.rmpaydashboard.repositories.UsersAppRepository;
import com.retailmanager.rmpaydashboard.services.DTO.ScheduleCalendarDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ScheduleCalendarService implements IScheduleCalendarService {

    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;

    @Autowired
    private ScheduleCalendarRepository scheduleDBService;
    @Autowired
    private UsersAppRepository employeRepository;
    @Autowired
    private ScheduleCalendarRepository scheduleCalendarRepository;

    /**
     * Save the ScheduleCalendarDTO and return a ResponseEntity.
     *
     * @param prmSchedule the ScheduleCalendarDTO to be saved
     * @return a ResponseEntity containing the saved ScheduleCalendarDTO or an error message
     */
    @Override
    @Transactional
    public ResponseEntity<?> save(ScheduleCalendarDTO prmSchedule) {
        Long id = prmSchedule.getId();
        ScheduleCalendar objSchedule = this.mapper.map(prmSchedule, ScheduleCalendar.class);
        if (id == null) {
            UsersBusiness userBusiness = employeRepository.findById(prmSchedule.getEmployeeId()).orElse(null);
            if (userBusiness == null) {
                throw new EntidadNoExisteException("El UsersBusiness con userBusinessId " + prmSchedule.getEmployeeId() + " no existe en la Base de datos");
            }
            objSchedule.setEmployee(userBusiness);
            scheduleDBService.save(objSchedule);
            prmSchedule = this.mapper.map(objSchedule, ScheduleCalendarDTO.class);
            prmSchedule.setEmployeeId(objSchedule.getEmployee().getUserBusinessId());
            return new ResponseEntity<ScheduleCalendarDTO>(prmSchedule, HttpStatus.CREATED);
        } else {
            objSchedule = scheduleDBService.findById(id).orElse(null);
            if (objSchedule == null) {
                throw new EntidadNoExisteException("El ScheduleCalendar con id " + id + " no existe en la Base de datos");
            }
            objSchedule.setColor(prmSchedule.getColor());
            objSchedule.setDateEnd(prmSchedule.getDateEnd());
            objSchedule.setDateStart(prmSchedule.getDateStart());
            objSchedule.setTitle(prmSchedule.getTitle());

            scheduleDBService.save(objSchedule);
            prmSchedule = this.mapper.map(objSchedule, ScheduleCalendarDTO.class);
            prmSchedule.setEmployeeId(objSchedule.getEmployee().getUserBusinessId());
            return new ResponseEntity<ScheduleCalendarDTO>(prmSchedule, HttpStatus.OK);
        }
    }

    /**
     * Updates the ScheduleCalendarDTO and return a ResponseEntity.
     *
     * @param id          the ID of the ScheduleCalendarDTO to be updated
     * @param prmSchedule the ScheduleCalendarDTO to be updated
     * @return a ResponseEntity containing the updated ScheduleCalendarDTO or an error message
     */
    @Override
    @Transactional
    public ResponseEntity<?> update(Long id, ScheduleCalendar prmSchedule) {

        ScheduleCalendar objSchedule = scheduleDBService.findById(id).orElse(null);
        if (objSchedule == null) {
            throw new EntidadNoExisteException("El ScheduleCalendar con id " + id + " no existe en la Base de datos");
        }
        objSchedule.setColor(prmSchedule.getColor());
        objSchedule.setDateEnd(prmSchedule.getDateEnd());
        objSchedule.setDateStart(prmSchedule.getDateStart());
        objSchedule.setTitle(prmSchedule.getTitle());
        scheduleDBService.save(objSchedule);
        ScheduleCalendarDTO objScheduleCalendarDTO = this.mapper.map(objSchedule, ScheduleCalendarDTO.class);
        objScheduleCalendarDTO.setEmployeeId(objSchedule.getEmployee().getUserBusinessId());
        return new ResponseEntity<ScheduleCalendarDTO>(objScheduleCalendarDTO, HttpStatus.OK);
    }

    /**
     * Deletes the ScheduleCalendar with the given id.
     *
     * @param id the id of the ScheduleCalendar to be deleted
     * @return true if the ScheduleCalendar is deleted, false otherwise
     * @throws EntidadNoExisteException if the ScheduleCalendar with the given id does not exist
     */
    @Override
    @Transactional
    public boolean delete(Long id) {

        ScheduleCalendar objSchedule = scheduleDBService.findById(id).orElse(null);
        if (objSchedule == null) {
            throw new EntidadNoExisteException("El ScheduleCalendar con id " + id + " no existe en la Base de datos");
        }
        scheduleDBService.delete(objSchedule);
        return true;
    }

    /**
     * Finds a ScheduleCalendar by its ID and returns a ResponseEntity with ScheduleCalendarDTO if found,
     * otherwise throw an EntidadNoExisteException.
     *
     * @param serviceId the ID of the ScheduleCalendar to find
     * @return a ResponseEntity with ScheduleCalendarDTO if the ScheduleCalendar is found
     * or throw an EntidadNoExisteException if not found
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findById(Long serviceId) {
        ScheduleCalendar objSchedule = scheduleDBService.findById(serviceId).orElse(null);
        if (objSchedule == null) {
            throw new EntidadNoExisteException("El ScheduleCalendar con id " + serviceId + " no existe en la Base de datos");
        }
        ScheduleCalendarDTO objScheduleCalendarDTO = this.mapper.map(objSchedule, ScheduleCalendarDTO.class);
        objScheduleCalendarDTO.setEmployeeId(objSchedule.getEmployee().getUserBusinessId());
        return new ResponseEntity<ScheduleCalendarDTO>(objScheduleCalendarDTO, HttpStatus.OK);
    }

    /**
     * Retrieves all ScheduleCalendar entries for a given employee ID and returns a ResponseEntity
     * containing a list of ScheduleCalendarDTO objects.
     *
     * @param employeeId the ID of the employee for whom to retrieve ScheduleCalendar entries
     * @return a ResponseEntity containing a list of ScheduleCalendarDTO objects with HTTP status OK
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAll(Long employeeId) {

        Iterable<ScheduleCalendar> listScheduleCalendar = scheduleDBService.findByEmployeeId(employeeId);
        List<ScheduleCalendarDTO> listScheduleCalendarDTO = StreamSupport
                .stream(listScheduleCalendar.spliterator(), false) // Convierte el Iterable en un Stream
                .map(schedule -> mapper.map(schedule, ScheduleCalendarDTO.class)) // Mapea cada elemento
                .collect(Collectors.toList());
        for (ScheduleCalendarDTO s : listScheduleCalendarDTO) {
            s.setEmployeeId(employeeId);
        }
        return new ResponseEntity<Iterable<ScheduleCalendarDTO>>(listScheduleCalendarDTO, HttpStatus.OK);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAll(Long employeeId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        Iterable<ScheduleCalendar> listScheduleCalendar = scheduleDBService.findByEmployeeIdRange(employeeId, startDateTime, endDateTime);
        List<ScheduleCalendarDTO> listScheduleCalendarDTO = StreamSupport
                .stream(listScheduleCalendar.spliterator(), false) // Convierte el Iterable en un Stream
                .map(schedule -> mapper.map(schedule, ScheduleCalendarDTO.class)) // Mapea cada elemento
                .collect(Collectors.toList());
        for (ScheduleCalendarDTO s : listScheduleCalendarDTO) {
            s.setEmployeeId(employeeId);
        }
        return new ResponseEntity<Iterable<ScheduleCalendarDTO>>(listScheduleCalendarDTO, HttpStatus.OK);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getDetailedCalendar(Long employeeId) {
        Iterable<Object[]> listScheduleCalendar = scheduleDBService.getDetailedCalendar(employeeId);

        Map<String, List<Object[]>> weeks =
                StreamSupport.stream(listScheduleCalendar.spliterator(), false)
                        .collect(Collectors.groupingBy(
                                fila -> (String) fila[1],
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        List<ScheduleListDetailedCalendar> listDetailedCalendars = new ArrayList<>();

        weeks.forEach((week, lines) -> {
            List<ScheduleDetailedCalendar> detailedCalendars = new ArrayList<>();
            var scheduleListDetailedCalendar = new ScheduleListDetailedCalendar(
                    week,
                    detailedCalendars
            );

            for (Object[] line : lines) {
                scheduleListDetailedCalendar.detailedCalendars().add(
                        new ScheduleDetailedCalendar(
                                (Long) line[0],
                                (String) line[2],
                                (String) line[3],
                                ((java.sql.Timestamp) line[4]).toLocalDateTime(),
                                ((java.sql.Timestamp) line[5]).toLocalDateTime(),
                                (String) line[6],
                                (Long) line[7]
                        )
                );
            }
            listDetailedCalendars.add(scheduleListDetailedCalendar);
        });

        return new ResponseEntity<>(listDetailedCalendars, HttpStatus.OK);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllByBusinessId(Long prmBusinessId) {
        Iterable<ScheduleCalendar> listScheduleCalendar = scheduleDBService.findByBusinessId(prmBusinessId);
        List<ScheduleCalendarDTO> listScheduleCalendarDTO = StreamSupport
                .stream(listScheduleCalendar.spliterator(), false) // Convierte el Iterable en un Stream
                .map(schedule -> {
                    ScheduleCalendarDTO dto = mapper.map(schedule, ScheduleCalendarDTO.class);
                    dto.setEmployeeId(schedule.getEmployee() != null ? schedule.getEmployee().getUserBusinessId() : null);
                    return dto;
                })
                .collect(Collectors.toList());

        return new ResponseEntity<Iterable<ScheduleCalendarDTO>>(listScheduleCalendarDTO, HttpStatus.OK);
    }
}