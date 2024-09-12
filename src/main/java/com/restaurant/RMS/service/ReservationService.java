package com.restaurant.RMS.service;

import com.restaurant.RMS.dto.*;
import com.restaurant.RMS.entity.Reservation;
import com.restaurant.RMS.entity.RestaurantTable;
import com.restaurant.RMS.entity.TimeSlot;
import com.restaurant.RMS.entity.User;
import com.restaurant.RMS.repository.ReservationRepository;
import com.restaurant.RMS.repository.RestaurantTableRepository;
import com.restaurant.RMS.repository.TimeSlotRepository;
import com.restaurant.RMS.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RestaurantTableRepository restaurantTableRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    public ReservationResponseDTO checkAvailablity (ReservationRequestDTO reservationRequest){
        LocalDate requestedDate = reservationRequest.getRequestedDate(); // get requested date (2024-09-03)
        List<RestaurantTable> restaurantTables = restaurantTableRepository.findAll(); // fetch all tables
        List<TimeSlot> timeSlots = timeSlotRepository.findAll(); //fetch all time slots

        ReservationResponseDTO reservationResponse = new ReservationResponseDTO();
        reservationResponse.setDate(reservationRequest.getRequestedDate()); // set the date in response dto

        // Create a list to store table availability details
        List<RestaurantTableDTO> tableDTOs = new ArrayList<>();

        for(RestaurantTable table : restaurantTables) { // for each table in tables
            // Retrieve reservations for the current table on the requested date
            List<Reservation> reservations = reservationRepository.findByReservationDateAndRestaurantTable_IdAndStatus(requestedDate, table.getId(), "BOOKED"); // get all reservations for the table on the requested date.

            // Create a set to store reserved time slots for quick lookup
            Set<LocalTime> reservedTimeSlots = reservations.stream()
                    .map(reservation -> reservation.getTimeSlot().getTimeSlot())
                    .collect(Collectors.toSet()); // will store all the reserved time slots in a set for that table on the requested date.

            // Create the available slots list with status
            List<AvailableSlotDTO> availableSlots = timeSlots.stream().map(slot -> {
                AvailableSlotDTO availableSlot = new AvailableSlotDTO();
                availableSlot.setTimeSlot(slot.getTimeSlot());
                LocalDateTime currentTime = LocalDateTime.now();
                LocalDate dateRequested = requestedDate;
                LocalTime timeSlotRequested = slot.getTimeSlot();
                LocalDateTime dateTimeRequested = LocalDateTime.of(dateRequested, timeSlotRequested);
                if (dateTimeRequested.isBefore(currentTime)){
                    availableSlot.setStatus("disabled");
                }else {
                    availableSlot.setStatus(reservedTimeSlots.contains(slot.getTimeSlot())  ? "unavailable" : "available");
                }
                return availableSlot;
            }).collect(Collectors.toList());

            // Create and populate the table DTO
            RestaurantTableDTO tableDTO = new RestaurantTableDTO();
            tableDTO.setId(table.getId());
            tableDTO.setTableDesc(table.getTableDesc());
            tableDTO.setTableImg(table.getTableImg());
            tableDTO.setAvailableSlots(availableSlots);

            // Add the table DTO to the list
            tableDTOs.add(tableDTO);
        }

        // Set the list of table DTOs in the response
        reservationResponse.setRestaurantTables(tableDTOs);

        return reservationResponse;
    }

    public BookTableResponseDTO bookTable(BookTableRequestDTO bookTableRequest, String email) {
        User user = userRepository.findByEmail(email);
        RestaurantTable restaurantTable = restaurantTableRepository.findById(bookTableRequest.getTableId()).orElseThrow(() -> new RuntimeException("Table not Found"));
        TimeSlot timeSlot = timeSlotRepository.findByTimeSlot(bookTableRequest.getTimeSlot());

        if(user == null) {
            return new BookTableResponseDTO(404, "User not found");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setRestaurantTable(restaurantTable);
        reservation.setTimeSlot(timeSlot);
        reservation.setReservationDate(bookTableRequest.getReservationDate());
        reservation.setStatus("BOOKED");

        reservationRepository.save(reservation);

        sendEmail(reservation);

        return new BookTableResponseDTO(200, "Table Booked Successfully");
    }


    public void sendEmail(Reservation reservation) {

        String from = "smtprestaurant@gmail.com";
        String to = reservation.getUser().getEmail();
        String subject = "Reservation Confirmed";
        String content = "Dear [[name]],<br>" +
                "Thank you for your reservation at Gericht Restaurant. Your reservation details are provided below:<br>" +
                "<h3>Customer's name: [[name]]</h3>" +
                "<h3>Phone Number: [[phone]]</h3>" +
                "<h3>Reserved Table: [[table]]</h3>" +
                "<h3>Reservation Time: [[time]]</h3>" +
                "Thank you,<br>" +
                "Gericht Restaurant";

        try{

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setFrom(from, "Gericht Restaurant");
            helper.setTo(to);
            helper.setSubject(subject);

            int hour = reservation.getTimeSlot().getTimeSlot().getHour();  // Assuming getHour() returns the hour
            int minute = reservation.getTimeSlot().getTimeSlot().getMinute();  // Assuming getMinute() returns the minutes
            String time = LocalTime.of(hour, minute).toString();

            content = content.replace("[[name]]", reservation.getUser().getName());
            content = content.replace("[[phone]]", reservation.getUser().getMobileNo());
            content = content.replace("[[table]]", reservation.getRestaurantTable().getTableDesc());
            content = content.replace("[[time]]", time);

            helper.setText(content, true);

            mailSender.send(message);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void checkAndCancelExpiredReservations() {

        LocalDate todayDate = LocalDate.now();
        System.out.println(todayDate);
        LocalTime tenMinutesAgo = LocalTime.now().minusMinutes(10);
        System.out.println(tenMinutesAgo);
        List<Reservation> reservationsToCancel = reservationRepository
                .findByReservationDateBeforeOrReservationDateAndTimeSlot_TimeSlotBeforeAndStatusNot(todayDate, todayDate, tenMinutesAgo, "FULFILLED");
        System.out.println(reservationsToCancel);
        for (Reservation reservation : reservationsToCancel) {
            if (reservation.getReservationDate().isBefore(todayDate) ||
                    (reservation.getReservationDate().isEqual(todayDate) && reservation.getTimeSlot().getTimeSlot().isBefore(tenMinutesAgo))) {
                reservation.setStatus("CANCELED");
                reservationRepository.save(reservation);
            }
        }
    }

    public ViewReservationResponseDTO viewReservation(String email) {
        User user = userRepository.findByEmail(email);
        List<Reservation> reservations = reservationRepository.findByUser_IdAndStatus(user.getId(), "BOOKED");

        ViewReservationResponseDTO viewReservationResponse = new ViewReservationResponseDTO();
        List<ReservationDetailsDTO> reservationDetails = new ArrayList<>();

        for(Reservation reservation : reservations) {
            ReservationDetailsDTO reservationDetail = new ReservationDetailsDTO();
            reservationDetail.setReservationId(reservation.getId());
            reservationDetail.setReservationDate(reservation.getReservationDate());
            reservationDetail.setReservationTime(reservation.getTimeSlot().getTimeSlot());
            reservationDetail.setTableDesc(reservation.getRestaurantTable().getTableDesc());
            reservationDetail.setStatus(reservation.getStatus());

            reservationDetails.add(reservationDetail);
        }

        viewReservationResponse.setUserId(user.getId());
        viewReservationResponse.setReservationDetails(reservationDetails);

        return viewReservationResponse;
    }

    public ViewReservationResponseDTO viewHistory(String email) {
        User user = userRepository.findByEmail(email);

        List<Reservation> reservations = reservationRepository.findByUser_IdAndStatusNot(user.getId(), "BOOKED");

        ViewReservationResponseDTO viewReservationResponse = new ViewReservationResponseDTO();
        List<ReservationDetailsDTO> reservationDetails = new ArrayList<>();

        for(Reservation reservation : reservations) {
            ReservationDetailsDTO reservationHistory = new ReservationDetailsDTO();
            reservationHistory.setReservationId(reservation.getId());
            reservationHistory.setReservationDate(reservation.getReservationDate());
            reservationHistory.setReservationTime(reservation.getTimeSlot().getTimeSlot());
            reservationHistory.setTableDesc(reservation.getRestaurantTable().getTableDesc());
            reservationHistory.setStatus(reservation.getStatus());

            reservationDetails.add(reservationHistory);
        }

        viewReservationResponse.setUserId(user.getId());
        viewReservationResponse.setReservationDetails(reservationDetails);

        return viewReservationResponse;
    }

    public CancelBookingResponseDTO cancelBooking(CancelBookingRequestDTO cancelBookingRequestDTO) {

        Reservation reservation = reservationRepository.findByIdAndStatus(cancelBookingRequestDTO.getReservationId(),"BOOKED");

        if(reservation == null) {
            return new CancelBookingResponseDTO(404, "Reservation Not Found");
        }

        reservation.setStatus("CANCELED");
        reservationRepository.save(reservation);

        return new CancelBookingResponseDTO(200, "Reservation Cancelled Successfully");

    }


}

