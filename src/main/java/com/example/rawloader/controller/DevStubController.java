package com.example.rawloader.controller;

import com.example.rawloader.dto.PartnerDTO;
import com.example.rawloader.model.LoaderConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devstub/api")
@Profile("dev") // only active when spring.profiles.active=dev
@Slf4j
public class DevStubController {

    // Stub for PartnerClient
    @GetMapping("/partners/{partnerId}")
    public PartnerDTO getPartner(@PathVariable Long partnerId) {
        log.info("Stubbed Partner API called for ID: {}", partnerId);
        PartnerDTO partner = new PartnerDTO();
        partner.setId(partnerId);
        partner.setName("Test Partner");
        partner.setStatus("ACTIVE");
        return partner;
    }

    // Stub for ConfigClient (if needed)
    @GetMapping("/configs/{configId}")
    public LoaderConfig getConfig(@PathVariable String configId) {
        log.info("Stubbed Config API called for ID: {}", configId);
        LoaderConfig config = new LoaderConfig();
        config.setName(configId);
        return config;
    }
}
