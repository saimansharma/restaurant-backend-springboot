package com.restaurant.RMS.controller;

import com.restaurant.RMS.dto.*;
import com.restaurant.RMS.service.JWTUtils;
import com.restaurant.RMS.service.ReservationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth/user")
public class BookingRestController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private JWTUtils jwtUtils;

    @PostMapping("/check-availability")
    public ResponseEntity<ReservationResponseDTO> checkAvailability (@RequestBody ReservationRequestDTO request) {
        try {
            ReservationResponseDTO response = reservationService.checkAvailablity(request);

            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            throw  new RuntimeException("Internal Server Error");
        }
    }

    @PostMapping("/book-table")
    public ResponseEntity<BookTableResponseDTO> bookTable (@RequestBody BookTableRequestDTO bookingRequest, HttpServletRequest request) {
        try{
            String token = getTokenFromCookies(request.getCookies());

            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new BookTableResponseDTO(401, "Unauthorized"));
            }

            String email = jwtUtils.extractUsername(token);

            BookTableResponseDTO response = reservationService.bookTable(bookingRequest, email);

            return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new BookTableResponseDTO(500, "Internal Server Error"));
        }

    }

    @PostMapping("/view-booking")
    public ResponseEntity<ViewReservationResponseDTO> viewBooking(HttpServletRequest request) {
        try {
            String token = getTokenFromCookies(request.getCookies());
            if (token == null) {
                throw new RuntimeException("Unauthorized");
            }

            String email = jwtUtils.extractUsername(token);
            System.out.println("Extracted email: " + email);

            ViewReservationResponseDTO response = reservationService.viewReservation(email);
            System.out.println("Response: " + response);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();  // Log the exception details
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/view-history")
    public ResponseEntity<ViewReservationResponseDTO> viewHistory(HttpServletRequest request) {
        try {
            String token = getTokenFromCookies(request.getCookies());
            if (token == null) {
                throw new RuntimeException("Unauthorized");
            }

            String email = jwtUtils.extractUsername(token);
            System.out.println("Extracted email: " + email);

            ViewReservationResponseDTO response = reservationService.viewHistory(email);
            System.out.println("Response: " + response);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();  // Log the exception details
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/cancel-booking")
    public ResponseEntity<CancelBookingResponseDTO> cancelBooking(@RequestBody CancelBookingRequestDTO request) {
        try{
            CancelBookingResponseDTO response = reservationService.cancelBooking(request);

            return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new CancelBookingResponseDTO(500, "Internal Server Error"));
        }
    }

        private String getTokenFromCookies(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accessToken")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
