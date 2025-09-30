package com.example.rawloader.controller;

import com.example.rawloader.dto.PartnerDTO;
import com.example.rawloader.model.LoaderColumnDTO;
import com.example.rawloader.model.LoaderConfigDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devstub/api")
@Profile("dev") // only active in the 'dev' profile
@Slf4j
public class DevStubController {

    // ---- Partner stub: GET /devstub/api/partners/{partnerId}
    @GetMapping("/partners/{partnerId}")
    public PartnerDTO getPartner(@PathVariable Long partnerId) {
        log.info("Dev stub: GET partner {}", partnerId);

        PartnerDTO p = new PartnerDTO();
        p.setId(partnerId);
        // Use the actual field in your DTO (it's partnerName, not name)
        p.setPartnerName("Test Partner");
        // The rest are optional—set them only if they exist in your DTO
        try { p.setType("AGENCY"); } catch (Throwable ignored) {}
        try { p.setEmail("stub.partner@example.com"); } catch (Throwable ignored) {}
        try { p.setMobile("9999999999"); } catch (Throwable ignored) {}
        try { p.setContactNumber("011-12345678"); } catch (Throwable ignored) {}

        return p;
    }

    // ---- Config stub: GET /devstub/api/partners/{partnerId}/configs/{configId}
    @GetMapping("/partners/{partnerId}/configs/{configId}")
    public LoaderConfigDTO getConfig(@PathVariable Long partnerId, @PathVariable String configId) {
        log.info("Dev stub: GET config partnerId={}, configId={}", partnerId, configId);

        LoaderConfigDTO cfg = new LoaderConfigDTO();
        // Use your DTO field names
        try { cfg.setConfigId(configId); } catch (Throwable ignored) {}
        try { cfg.setPartnerId(partnerId); } catch (Throwable ignored) {}
        try { cfg.setName("MotorPolicy-v1"); } catch (Throwable ignored) {}
        try { cfg.setStatus("ACTIVE"); } catch (Throwable ignored) {}

        // Build column mappings using your LoaderColumnDTO
        LoaderColumnDTO c1 = new LoaderColumnDTO();
        c1.setHeader("Policy No");
        c1.setKey("policyId");
        c1.setType("string");
        c1.setRequired(true);

        LoaderColumnDTO c2 = new LoaderColumnDTO();
        c2.setHeader("Issue Date");
        c2.setKey("issueDate");
        c2.setType("date");
        c2.setRequired(true);
        c2.setFormat("dd/MM/yyyy");

        LoaderColumnDTO c3 = new LoaderColumnDTO();
        c3.setHeader("Premium");
        c3.setKey("premium");
        c3.setType("number");
        c3.setRequired(true);

        LoaderColumnDTO c4 = new LoaderColumnDTO();
        c4.setHeader("Product");
        c4.setKey("product");
        c4.setType("string");
        c4.setRequired(false);

        cfg.setColumnMappings(List.of(c1, c2, c3, c4));
        return cfg;
    }
}
