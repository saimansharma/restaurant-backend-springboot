package com.restaurant.RMS.service;

import com.restaurant.RMS.dto.AdminReservationDTO;
import com.restaurant.RMS.entity.Reservation;
import com.restaurant.RMS.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    public ReservationRepository reservationRepository;

    public List<AdminReservationDTO> getAllReservationsSorted(String sortField, String sortDirection) {

        Sort.Direction direction;

        try {
            // Convert sortDirection to uppercase to match enum values
            direction = Sort.Direction.valueOf(sortDirection.toUpperCase());
        } catch (IllegalArgumentException e) {
            // If invalid sort direction is passed, fallback to a default (e.g., ASC)
            direction = Sort.Direction.ASC;
        }


        Sort sort = Sort.by(direction, sortField);

        List<Reservation> allReservations =  reservationRepository.findAll(sort);

        List<AdminReservationDTO> adminReservations = new ArrayList<>();

        for (Reservation reservation : allReservations){
            AdminReservationDTO adminReservation = new AdminReservationDTO();
            adminReservation.setReservationId(reservation.getId());
            adminReservation.setTableDesc(reservation.getRestaurantTable().getTableDesc());
            adminReservation.setReservationDate(reservation.getReservationDate());
            adminReservation.setTimeSlot(reservation.getTimeSlot().getTimeSlot());
            adminReservation.setUsername(reservation.getUser().getName());
            adminReservation.setPhoneNo(reservation.getUser().getMobileNo());
            adminReservation.setStatus(reservation.getStatus());

            adminReservations.add(adminReservation);
        }

        return adminReservations;
    }

    public List<AdminReservationDTO> getAllActiveReservationsSorted(String sortField, String sortDirection) {

        Sort.Direction direction;

        try {
            // Convert sortDirection to uppercase to match enum values
            direction = Sort.Direction.valueOf(sortDirection.toUpperCase());
        } catch (IllegalArgumentException e) {
            // If invalid sort direction is passed, fallback to a default (e.g., ASC)
            direction = Sort.Direction.ASC;
        }


        Sort sort = Sort.by(direction, sortField);

        List<Reservation> allReservations =  reservationRepository.findAllByStatus("BOOKED",sort);

        List<AdminReservationDTO> adminReservations = new ArrayList<>();

        for (Reservation reservation : allReservations){
            AdminReservationDTO adminReservation = new AdminReservationDTO();
            adminReservation.setReservationId(reservation.getId());
            adminReservation.setTableDesc(reservation.getRestaurantTable().getTableDesc());
            adminReservation.setReservationDate(reservation.getReservationDate());
            adminReservation.setTimeSlot(reservation.getTimeSlot().getTimeSlot());
            adminReservation.setUsername(reservation.getUser().getName());
            adminReservation.setPhoneNo(reservation.getUser().getMobileNo());

            adminReservations.add(adminReservation);
        }

        return adminReservations;
    }

    public List<AdminReservationDTO> searchActiveByNameOrPhone(String query) {

        List<Reservation> searchedReservation = reservationRepository.findByStatusAndUser_NameContainingOrStatusAndUser_MobileNoContaining("BOOKED", query, "BOOKED", query);

        List<AdminReservationDTO> adminReservations = new ArrayList<>();

        for (Reservation reservation : searchedReservation){
            AdminReservationDTO adminReservation = new AdminReservationDTO();
            adminReservation.setReservationId(reservation.getId());
            adminReservation.setTableDesc(reservation.getRestaurantTable().getTableDesc());
            adminReservation.setReservationDate(reservation.getReservationDate());
            adminReservation.setTimeSlot(reservation.getTimeSlot().getTimeSlot());
            adminReservation.setUsername(reservation.getUser().getName());
            adminReservation.setPhoneNo(reservation.getUser().getMobileNo());

            adminReservations.add(adminReservation);
        }

        return adminReservations;
    }



    public void fulfillReservation(long id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);


        if(reservation.isPresent()){
            Reservation updatedReservation = reservation.get();
            updatedReservation.setStatus("FULFILLED");
            reservationRepository.save(updatedReservation);
        }
        else {
            System.out.println("Task Unsuccessful");
        }
    }

    public void cancelReservation(long id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);

        if(reservation.isPresent()){
            Reservation updatedReservation = reservation.get();
            updatedReservation.setStatus("CANCELED");
            reservationRepository.save(updatedReservation);
        }
        else {
            System.out.println("Task Unsuccessful");
        }
    }
}
