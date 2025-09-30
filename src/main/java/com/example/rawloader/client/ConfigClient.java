package com.example.rawloader.client;

import com.example.rawloader.model.LoaderConfigDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "config-service", url = "${services.config.url}", configuration = com.example.rawloader.config.FeignConfiguration.class)
public interface ConfigClient {
    @GetMapping("/api/partners/{partnerId}/configs/{configId}")
    LoaderConfigDTO getConfig(@PathVariable("partnerId") Long partnerId, @PathVariable("configId") String configId);
}
