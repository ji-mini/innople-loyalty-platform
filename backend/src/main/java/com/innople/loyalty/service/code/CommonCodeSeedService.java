package com.innople.loyalty.service.code;

public interface CommonCodeSeedService {
    SeedResult seedDefaultsForCurrentTenant();

    record SeedResult(
            int createdCount
    ) {
    }
}

