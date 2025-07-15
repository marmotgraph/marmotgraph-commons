package org.marmotgraph.commons.api;

import org.marmotgraph.commons.controller.CoreController;
import org.marmotgraph.commons.models.FileResponse;
import org.marmotgraph.commons.models.ForbiddenException;
import org.marmotgraph.commons.models.UserRoles;
import org.marmotgraph.commons.service.UserClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;
import java.util.stream.Collectors;

@RequestMapping("${org.marmotgraph.api.root:}/")
@RestController
public class Theme {

    private final CoreController themeController;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final List<String> SUPPORTED_ASSETS = Arrays.asList("favicon", "background", "logo", "css");
    private final UserClient userClient;


    public Theme(CoreController themeController, UserClient userClient) {
        this.themeController = themeController;
        this.userClient = userClient;
    }



    @PutMapping("tenant/{tenant}")
    public void setTenantDynamically(@PathVariable("tenant") String tenant){
        UserRoles userRoles = userClient.getUserRoles();
        if(userRoles.isGlobalAdmin()) {
            themeController.setTenantDynamically(tenant);
        }
        else{
            throw new ForbiddenException("You are not allowed to set the tenant since you need to be a global administrator to do so.");
        }
    }



    @GetMapping("theme/{asset}")
    @ResponseBody
    public ResponseEntity<Resource> getAsset(@PathVariable("asset") String asset, @RequestParam(value = "darkMode", required = false) boolean darkMode) {
        if (SUPPORTED_ASSETS.contains(asset.toLowerCase())) {
            FileResponse fileResponse = themeController.readAsset(asset, darkMode);
            if (fileResponse != null) {
                Map<String, List<String>> headers = new HashMap<>(fileResponse.headers());
                Set<String> invalidKeys = headers.keySet().stream().filter(h -> h.startsWith(":")).collect(Collectors.toSet()); //We want to remove HTTP2 headers for now
                invalidKeys.forEach(headers::remove);
                return new ResponseEntity<>(new ByteArrayResource(fileResponse.bytes()), CollectionUtils.toMultiValueMap(headers), HttpStatus.OK);
            }
            return ResponseEntity.notFound().build();
        } else {
            throw new IllegalArgumentException("Unsupported asset: " + asset);
        }
    }


    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 */${org.marmotgraph.commons.assets.cacheRefreshInterval:10} * * * *")
    public void refreshAllCachesAtIntervals() {
        logger.info("Refreshing asset caches...");
        SUPPORTED_ASSETS.forEach(asset -> {
                    themeController.refreshAsset(asset, true);
                    themeController.refreshAsset(asset, false);

                }
        );
    }

}
