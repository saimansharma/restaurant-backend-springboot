package com.restaurant.RMS.controller;

import com.restaurant.RMS.dto.WinesDTO;
import com.restaurant.RMS.entity.Wines;
import com.restaurant.RMS.service.WineService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class WinesRestController {

    public WineService wineService;

    public WinesRestController (WineService theWineService){
        wineService = theWineService;
    }

    @GetMapping("/wines")
    public List<Wines> getAllWines() {
        return wineService.getAllWines();
    }

    @GetMapping("/activewines")
    public List<Wines> getActiveWines(){
        return wineService.getActiveWines();
    }

    @GetMapping("/wines/{id}")
    public ResponseEntity<WinesDTO> getWineById(@PathVariable long id) {
        Wines wine = wineService.getWineById(id);

        if(wine == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        WinesDTO wineDTO = new WinesDTO();
        wineDTO.setId(wine.getId());
        wineDTO.setTitle(wine.getTitle());
        wineDTO.setPrice(wine.getPrice());
        wineDTO.setWineTags(wine.getWineTags());
        wineDTO.setStatus(wine.getStatus());
        wineDTO.setDisplayHomePage(wine.getDisplayHomePage());

        return new ResponseEntity<>(wineDTO, HttpStatus.OK);
    }

    @PostMapping("/wines")
    public Wines addWine(@RequestBody Wines theWine) {
        theWine.setId(0);
        Wines dbWine = wineService.saveWine(theWine);

        return dbWine;
    }

    @PutMapping("/wines")
    public ResponseEntity<Wines> updateWine(@RequestBody Wines theWine) {

        Wines dbWine = wineService.saveWine(theWine);

        return ResponseEntity.ok(dbWine);
    }

    @DeleteMapping("/wines/{id}")
    public String deleteWine(@PathVariable long id) {
        Wines tempWine = wineService.getWineById(id);

        if(tempWine == null) {
            throw new RuntimeException("Wine id not found- " + id);
        }

        wineService.deleteById(id);

        return "Deleted Wine id- " +id;
    }

}
