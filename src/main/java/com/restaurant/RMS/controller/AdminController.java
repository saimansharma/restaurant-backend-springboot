package com.restaurant.RMS.controller;

import com.restaurant.RMS.dto.AdminReservationDTO;
import com.restaurant.RMS.entity.Awards;
import com.restaurant.RMS.entity.Cocktails;
import com.restaurant.RMS.entity.User;
import com.restaurant.RMS.entity.Wines;
import com.restaurant.RMS.repository.UserRepository;
import com.restaurant.RMS.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    AwardService awardService;

    @Autowired
    WineService wineService;

    @Autowired
    CocktailService cocktailService;

    @Autowired
    UserService userService;

    @Autowired
    AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute
    public void commonUser(Principal principal, Model model) {
        if (principal != null) {
            String email = principal.getName();
            User user = userRepository.findByEmail(email);
            model.addAttribute("user", user);
        }
    }

    @GetMapping("/dashboard")
    public String adminHome(){
        return "adminHome";
    }

    // Awards Section

    @GetMapping("/dashboard/awards")
    public String getAwards(Model model) {
        model.addAttribute("awards", awardService.getAllAwards());
        return "awards";
    }

    @GetMapping("dashboard/awards/add")
    public String addAward(Model model) {
        model.addAttribute("award", new Awards());
        return "awardsAdd";
    }

    @PostMapping("dashboard/awards/add")
    public String postAddAwards(@ModelAttribute("award") Awards awards, @RequestParam("awardImage") MultipartFile file, @RequestParam("imageUrl") String imageUrl) throws IOException {
        // Check if the award already exists by ID
        if (awards.getId() == 0 || !awardService.existsById(awards.getId())) {
            // If the ID is 0 or does not exist, save a new award
            awardService.saveAward(file, awards.getTitle(), awards.getSubtitle());
        } else {
            // If the ID exists, update the existing award
            awardService.updateAward(awards.getId(), file, awards.getTitle(), awards.getSubtitle());
        }
        return "redirect:/admin/dashboard/awards";
    }

    @GetMapping("dashboard/awards/update/{id}")
    public String updateAward(@PathVariable long id, Model model) {
        Awards award = awardService.getAwardById(id);
        model.addAttribute("award", award); // To retrieve from optional object, we use .get() method.
        return "awardsAdd"; //redirecting to add page along with the category model
    }

    @GetMapping("dashboard/awards/delete/{id}")
    public String deleteAward(@PathVariable long id) {
        awardService.deleteAwardById(id);
        return "redirect:/admin/dashboard/awards";
    }

    // Wines Section

    @GetMapping("/dashboard/wines")
    public String getWines(Model model) {
        model.addAttribute("wines", wineService.getAllWines());
        return "wines";
    }

    @GetMapping("/dashboard/wines/add")
    public String addWine(Model model) {
        Wines wine = new Wines();
        wine.setWineTags(new ArrayList<>());
        model.addAttribute("wine", wine);
        return "winesAdd";
    }

    @PostMapping("/dashboard/wines/add")
    public String addWine(@ModelAttribute("wine") Wines wine, @RequestParam("wineTags") List<String> wineTags) {
        wine.setWineTags(wineTags);
        wineService.saveWine(wine);
        return "redirect:/admin/dashboard/wines";
    }

    @GetMapping("/dashboard/wines/update/{id}")
    public String updateWine(@PathVariable long id, Model model) {
        Wines wine = wineService.getWineById(id);
        model.addAttribute("wine", wine);
        return "winesAdd";
    }

    @GetMapping("/dashboard/wines/delete/{id}")
    public String deleteWine(@PathVariable long id){
        wineService.deleteById(id);
        return "redirect:/admin/dashboard/wines";
    }

    // Cocktail Section

    @GetMapping("/dashboard/cocktails")
    public String getCocktails(Model model) {
        model.addAttribute("cocktails", cocktailService.getAllCocktails());
        return "cocktails";
    }

    @GetMapping("/dashboard/cocktails/add")
    public String addCocktail(Model model) {
        Cocktails cocktail = new Cocktails();
        cocktail.setCocktailTags(new ArrayList<>());
        model.addAttribute("cocktail", cocktail);
        return "cocktailsAdd";
    }

    @PostMapping("/dashboard/cocktails/add")
    public String addCocktail(@ModelAttribute("cocktail") Cocktails cocktail, @RequestParam("cocktailTags") List<String> cocktailTags) {
        cocktail.setCocktailTags(cocktailTags);
        cocktailService.saveCocktail(cocktail);
        return "redirect:/admin/dashboard/cocktails";
    }

    @GetMapping("/dashboard/cocktails/update/{id}")
    public String updateCocktail(@PathVariable long id, Model model) {
        Cocktails cocktail = cocktailService.getCocktailById(id);
        model.addAttribute("cocktail", cocktail);
        return "cocktailsAdd";
    }

    @GetMapping("/dashboard/cocktails/delete/{id}")
    public String deleteCocktail(@PathVariable long id){
        cocktailService.deleteById(id);
        return "redirect:/admin/dashboard/cocktails";
    }
    
    // User section

//    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/dashboard/admins")
    public String getAdmins(Model model) {
        model.addAttribute("admins", userService.getAdminUsers());
        return "admins";
    }

//    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/dashboard/admins/add")
    public String addAdmin(Model model) {
        User user = new User();
        model.addAttribute("admin", user);
        return "adminsAdd";
    }

//    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @PostMapping("/dashboard/admins/add")
    public String addAdmin(@ModelAttribute("admin") User user){
        userService.saveAdmin(user);
        return "redirect:/admin/dashboard/admins";
    }

//    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/dashboard/admins/update/{id}")
    public String updateUser(@PathVariable long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("admin", user);
        return "adminsAdd";
    }

//    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/dashboard/admins/delete/{id}")
    public String deleteUser(@PathVariable long id) {
        userService.deleteUserById(id);
        return "redirect:/admin/dashboard/admins";
    }

    //Reservations

    @GetMapping("/dashboard/reservations")
    public String getReservations(@RequestParam(name = "sort", required = false, defaultValue = "reservationDate, asc") String sort, Model model) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        String sortDirection = sortParams[1];

        List<AdminReservationDTO> reservations = adminService.getAllReservationsSorted(sortField, sortDirection);
        System.out.println(reservations);

        model.addAttribute("reservations", reservations);
        model.addAttribute("currentSort", sort);

        return "reservations";
    }

    @GetMapping("/dashboard/active-reservations")
    public String getActiveReservations(@RequestParam(name = "sort", required = false, defaultValue = "reservationDate, asc") String sort, Model model) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        String sortDirection = sortParams[1];

        List<AdminReservationDTO> reservations = adminService.getAllActiveReservationsSorted(sortField, sortDirection);
        System.out.println(reservations);

        model.addAttribute("reservations", reservations);
        model.addAttribute("currentSort", sort);

        return "activeReservation";
    }

    @GetMapping("/dashboard/active-reservations/search")
    public String searchReservation(@RequestParam("query") String query, Model model) {

        List<AdminReservationDTO> searchedReservations = adminService.searchActiveByNameOrPhone(query);

        model.addAttribute("reservations", searchedReservations);
        model.addAttribute("currentSort", ""); //clearing sort since it is a search result;

        return "activeReservation";
    }

    @GetMapping("/dashboard/active-reservations/fulfill/{id}")
    public String fulfillReservation(@PathVariable long id){
        adminService.fulfillReservation(id);
        return "redirect:/admin/dashboard/active-reservations";
    }

    @GetMapping("/dashboard/active-reservations/cancel/{id}")
    public String cancelReservation(@PathVariable long id){
        adminService.cancelReservation(id);
        return "redirect:/admin/dashboard/active-reservations";
    }

}
