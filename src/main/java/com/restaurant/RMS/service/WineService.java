package com.restaurant.RMS.service;

import com.restaurant.RMS.entity.Wines;
import com.restaurant.RMS.repository.WineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WineService {

    @Autowired
    public WineRepository wineRepository;

    public List<Wines> getAllWines(){
       return wineRepository.findAll();
    }

    public List<Wines> getActiveWines() {
        return wineRepository.findActiveWines();
    }

    public Wines getWineById(long id) {
        Optional<Wines> result = wineRepository.findById(id);

        Wines wine = null;
        if(result.isPresent()){
            wine = result.get();
        }
        else{
            throw new RuntimeException("Did not find the wine with id: "+ id);
        }
        return wine;
    }

    public Wines saveWine(Wines wine) {
        return wineRepository.save(wine);
    }

    public void deleteById(long id){
        wineRepository.deleteById(id);
    }

}
