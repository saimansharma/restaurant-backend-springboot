package com.restaurant.RMS.service;

import com.restaurant.RMS.dto.AwardsDTO;
import com.restaurant.RMS.entity.Awards;
import com.restaurant.RMS.repository.AwardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class AwardService {

    public static String uploadDir = System.getProperty("user.dir") + "/Backend/RMS/src/main/resources/static/awardImages";

    @Autowired
    public AwardRepository awardRepository;

    public boolean existsById(Long id) {
        return awardRepository.existsById(id);
    }


    public List<Awards> getAllAwards() {
       return awardRepository.findAll();
    }

    public Awards getAwardById(long id) {
        Optional<Awards> result = awardRepository.findById(id);
        Awards award = null;
        if(result.isPresent()) {
            award = result.get();
        }
        else {
            //we didn't find the employee
            throw new RuntimeException("Did not find award id - " + id);
        }
        return award;
    }

    public Awards saveAward(MultipartFile file, String title, String subtitle) throws IOException {
        Path filePath = Paths.get(uploadDir, file.getOriginalFilename());

        if (Files.notExists(filePath.getParent())) {
            Files.createDirectories(filePath.getParent());
            System.out.println("Directory created: " + filePath.getParent());
        }

        System.out.println("Saving file to: " + filePath.toString());

        Files.write(filePath, file.getBytes());
        System.out.println("File saved successfully");

        Awards award = new Awards();
        String imageUrl = "/awardImages/" + file.getOriginalFilename();
        award.setId(0);
        award.setImageUrl(imageUrl);
        award.setTitle(title);
        award.setSubtitle(subtitle);

        return awardRepository.save(award);
    }

    public Awards updateAward(long id, MultipartFile file, String title, String subtitle) throws IOException {
        Optional<Awards> optionalAward = awardRepository.findById(id);
        if (!optionalAward.isPresent()) {
            throw new IllegalArgumentException("Award not found with id: " + id);
        }

        Awards existingAward = optionalAward.get();
        // Delete the old file if a new file is uploaded
        if (file != null && !file.isEmpty()) {
            Path oldFilePath = Paths.get(uploadDir, existingAward.getImageUrl().substring(12)); // Adjust path based on your setup

            try {
                Files.deleteIfExists(oldFilePath);
                System.out.println("Old file deleted successfully: " + oldFilePath);
            } catch (IOException e) {
                System.err.println("Error deleting old file: " + oldFilePath);
                e.printStackTrace();
            }
        }

        // Save the new file
        Path newFilePath = Paths.get(uploadDir, file.getOriginalFilename());
        Files.write(newFilePath, file.getBytes());
        System.out.println("New file saved successfully: " + newFilePath);

        // Update the award details
        String imageUrl = "/awardImages/" + file.getOriginalFilename();
        existingAward.setImageUrl(imageUrl);
        existingAward.setTitle(title);
        existingAward.setSubtitle(subtitle);

        return awardRepository.save(existingAward);
    }

    public void deleteAwardById(long id) {
        Optional<Awards> optionalAward = awardRepository.findById(id);
        if (!optionalAward.isPresent()) {
            throw new IllegalArgumentException("Award not found with id: " + id);
        }

        Awards tempAward = optionalAward.get();
        // Delete the image file from file directory when the row is deleted
        Path filePath = Paths.get(uploadDir, tempAward.getImageUrl().substring(12)); // Adjust path based on your setup
            try {
                Files.deleteIfExists(filePath);
                System.out.println("Old file deleted successfully: " + filePath);
            } catch (IOException e) {
                System.err.println("Error deleting old file: " + filePath);
                e.printStackTrace();
            }
        awardRepository.deleteById(id);

    }



}
