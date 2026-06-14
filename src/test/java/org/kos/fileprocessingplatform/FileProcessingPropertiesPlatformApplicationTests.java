package org.kos.fileprocessingplatform;

import org.kos.fileprocessingplatform.handler.MqService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class FileProcessingPropertiesPlatformApplicationTests {

    @MockitoBean
    private MqService mqService;

    @Test
    void contextLoads() {
    }

}
