package com.restaurant.RMS.service;

import com.restaurant.RMS.entity.Cocktails;
import com.restaurant.RMS.entity.Wines;
import com.restaurant.RMS.repository.CocktailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CocktailService {

    @Autowired
    public CocktailRepository cocktailRepository;

    public List<Cocktails> getAllCocktails(){
        return cocktailRepository.findAll();
    }

    public List<Cocktails> getActiveCocktails() {
        return cocktailRepository.findActiveCocktails();
    }

    public Cocktails getCocktailById(long id) {
        Optional<Cocktails> result = cocktailRepository.findById(id);

        Cocktails cocktail = null;
        if(result.isPresent()){
            cocktail = result.get();
        }
        else{
            throw new RuntimeException("Did not find the cocktail with id: "+ id);
        }
        return cocktail;
    }

    public Cocktails saveCocktail(Cocktails cocktail) {
        return cocktailRepository.save(cocktail);
    }

    public void deleteById(long id){
        cocktailRepository.deleteById(id);
    }

}
