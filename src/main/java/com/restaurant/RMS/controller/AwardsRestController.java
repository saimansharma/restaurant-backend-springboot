package com.restaurant.RMS.controller;

import com.restaurant.RMS.dto.AwardsDTO;
import com.restaurant.RMS.entity.Awards;
import com.restaurant.RMS.service.AwardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AwardsRestController {

    public AwardService awardService;

    public AwardsRestController(AwardService theAwardService) {
        awardService = theAwardService;
    }

    @GetMapping("/awards")
    public List<Awards> getAllAwards() {
       return awardService.getAllAwards();
    }

    @GetMapping("/awards/{id}")
    public ResponseEntity<AwardsDTO> getAwardById(@PathVariable long id){
        Awards award = awardService.getAwardById(id);
        if (award == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        AwardsDTO awardDTO = new AwardsDTO();
        awardDTO.setId(award.getId());
        awardDTO.setImageUrl(award.getImageUrl());
        awardDTO.setTitle(award.getTitle());
        awardDTO.setSubtitle(award.getSubtitle());
        return new ResponseEntity<>(awardDTO, HttpStatus.OK);
    }

    @PostMapping("/awards")
    public ResponseEntity<Awards> addAward(@RequestParam("imgUrl")MultipartFile file,
                                           @RequestParam("title") String title,
                                           @RequestParam("subtitle") String subtitle) {

       try {
           Awards newAward = awardService.saveAward(file, title, subtitle);
           return new ResponseEntity<>(newAward, HttpStatus.CREATED);
       } catch (IOException e) {
           return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }

    @PutMapping("/awards/{id}")
    public ResponseEntity<Awards> updateAward(@PathVariable long id,
                              @RequestParam("imgUrl")MultipartFile file,
                              @RequestParam("title") String title,
                              @RequestParam("subtitle") String subtitle) {
        try {
            Awards newAward = awardService.updateAward(id, file, title, subtitle);
            return new ResponseEntity<>(newAward, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/awards/{id}")
    public String deleteAward(@PathVariable long id) {
        Awards tempAward = awardService.getAwardById(id);

        if (tempAward == null) {
            throw new RuntimeException ("Award id not found - " + id);
        }

        awardService.deleteAwardById(id);

        return "Deleted award id - " + id;

    }
}
