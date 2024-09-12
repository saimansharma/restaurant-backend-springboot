package com.restaurant.RMS.controller;

import com.restaurant.RMS.dto.CocktailsDTO;
import com.restaurant.RMS.dto.WinesDTO;
import com.restaurant.RMS.entity.Cocktails;
import com.restaurant.RMS.entity.Wines;
import com.restaurant.RMS.service.CocktailService;
import com.restaurant.RMS.service.WineService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CocktailsRestController {

    public CocktailService cocktailService;

    public CocktailsRestController (CocktailService theCocktailService){
        cocktailService = theCocktailService;
    }

    @GetMapping("/cocktails")
    public List<Cocktails> getAllCocktails() {
        return cocktailService.getAllCocktails();
    }

    @GetMapping("/activecocktails")
    public List<Cocktails> getActiveCocktails(){
        return cocktailService.getActiveCocktails();
    }

    @GetMapping("/cocktails/{id}")
    public ResponseEntity<CocktailsDTO> getCocktailById(@PathVariable long id) {
        Cocktails cocktail = cocktailService.getCocktailById(id);

        if(cocktail == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        CocktailsDTO cocktailDTO = new CocktailsDTO();
        cocktailDTO.setId(cocktail.getId());
        cocktailDTO.setTitle(cocktail.getTitle());
        cocktailDTO.setPrice(cocktail.getPrice());
        cocktailDTO.setCocktailTags(cocktail.getCocktailTags());
        cocktailDTO.setStatus(cocktail.getStatus());
        cocktailDTO.setDisplayHomePage(cocktail.getDisplayHomePage());

        return new ResponseEntity<>(cocktailDTO, HttpStatus.OK);
    }

    @PostMapping("/cocktails")
    public Cocktails addCocktail(@RequestBody Cocktails theCocktail) {
        theCocktail.setId(0);
        Cocktails dbCocktail = cocktailService.saveCocktail(theCocktail);

        return dbCocktail;
    }

    @PutMapping("/cocktails")
    public ResponseEntity<Cocktails> updateCocktail(@RequestBody Cocktails theCocktail) {

        Cocktails dbCocktail = cocktailService.saveCocktail(theCocktail);

        return ResponseEntity.ok(dbCocktail);
    }

    @DeleteMapping("/cocktails/{id}")
    public String deleteCocktail(@PathVariable long id) {
        Cocktails tempCocktail = cocktailService.getCocktailById(id);

        if(tempCocktail == null) {
            throw new RuntimeException("Cocktail id not found- " + id);
        }

        cocktailService.deleteById(id);

        return "Deleted Cocktail id- " +id;
    }

}
