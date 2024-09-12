package com.restaurant.RMS.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AwardsDTO {

    private long id;
    private String imageUrl;
    private String title;
    private String subtitle;

//    private byte[] image;
}
