package com.example.rawloader.dto;

import lombok.Data;

@Data
public class PartnerDTO {
    private Long id;
    private String partnerName;
    private String type;
    private String email;
    private String mobile;
    private String contactNumber;

    public void setStatus(String active) {
    }

    public void setName(String testPartner) {
    }
}
