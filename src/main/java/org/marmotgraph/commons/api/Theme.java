package org.marmotgraph.commons.api;

import org.marmotgraph.commons.controller.ThemeController;
import org.marmotgraph.commons.models.FileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RequestMapping("${org.marmotgraph.api.root:}/theme")
@RestController
public class Theme {

    private final String tenant;
    private final String hostName;
    private final String apiVersion;
    private final ThemeController themeController;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final List<String> SUPPORTED_ASSETS = Arrays.asList("favicon", "background", "logo");

    public Theme(@Value("${org.marmotgraph.tenant:default}") String tenant, @Value("${org.marmotgraph.core.host}") String hostName, @Value("${org.marmotgraph.core.apiVersion:v3}") String apiVersion, ThemeController themeController) {
        this.tenant = tenant;
        this.hostName = hostName;
        this.apiVersion = apiVersion;
        this.themeController = themeController;
    }

    @GetMapping("{asset}")
    @ResponseBody
    public ResponseEntity<Resource> getAsset(@PathVariable("asset") String asset, @RequestParam(value = "darkMode", required = false) boolean darkMode) {
        if(SUPPORTED_ASSETS.contains(asset.toLowerCase())){
            return readAsset(asset, darkMode);
        }
        else{
            throw new IllegalArgumentException("Unsupported asset: " + asset);
        }
    }

    private ResponseEntity<Resource> readAsset(String asset, boolean darkMode) {
        FileResponse fileResponse = themeController.readAsset(buildUrl(asset, darkMode));
        if(fileResponse != null) {
            return new ResponseEntity<>(new ByteArrayResource(fileResponse.bytes()), CollectionUtils.toMultiValueMap(fileResponse.headers()), HttpStatus.OK);
        }
        return ResponseEntity.notFound().build();
    }

    private String buildUrl(String asset, boolean darkMode){
        return String.format("https://%s/%s/tenants/%s/theme/%s?darkMode=%s", hostName, apiVersion, tenant, asset, darkMode);
    }

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 */${org.marmotgraph.commons.assets.cacheRefreshInterval:10} * * * *")
    public void refreshAllCachesAtIntervals() {
        logger.info("Refreshing asset caches...");
        SUPPORTED_ASSETS.forEach(asset -> {
            themeController.refreshAsset(buildUrl(asset, true));
            themeController.refreshAsset(buildUrl(asset, false));
        }
        );
    }

}
