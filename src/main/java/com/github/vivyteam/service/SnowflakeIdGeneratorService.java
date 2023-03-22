package com.github.vivyteam.service;

import com.github.vivyteam.helper.Base62;
import com.github.vivyteam.helper.CustomSnowflakeIdGenerator;
import com.github.vivyteam.service.contract.IdGeneratorInterface;
import org.springframework.stereotype.Service;

@Service
public class SnowflakeIdGeneratorService implements IdGeneratorInterface {
    private static final long WORKER_ID = 0;
    private static final long EPOCH = 1625014800000L; // Example epoch: July 1, 2021, 00:00:00
    private final CustomSnowflakeIdGenerator snowflakeIdGenerator = new CustomSnowflakeIdGenerator(WORKER_ID, EPOCH);

    @Override
    public String generateId() {
        long id = snowflakeIdGenerator.generateId();
        return Base62.encode(id);
    }
}
