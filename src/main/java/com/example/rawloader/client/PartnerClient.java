package com.example.rawloader.client;

import com.example.rawloader.dto.PartnerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Calls Partner service to fetch partner details.
 * Make sure services.partner.url is set in application.yml
 */
@FeignClient(name = "partner-service", url = "${services.partner.url}", configuration = com.example.rawloader.config.FeignConfiguration.class)
public interface PartnerClient {
    @GetMapping("/api/partners/{partnerId}")
    PartnerDTO getPartner(@PathVariable("partnerId") Long partnerId);
}
