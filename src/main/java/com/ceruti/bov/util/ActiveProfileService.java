package com.ceruti.bov.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ActiveProfileService {

    private Environment env;

    @Autowired
    public ActiveProfileService(Environment env) {
        this.env = env;
    }

    public boolean isTestMode() {
        return profileIsActive("test");
    }

    public boolean profileIsActive(String profile) {
        String[] activeProfiles = env.getActiveProfiles();
        return activeProfiles != null && Arrays.stream(activeProfiles).anyMatch(activeProfile -> activeProfile.equals(profile));
    }

}
