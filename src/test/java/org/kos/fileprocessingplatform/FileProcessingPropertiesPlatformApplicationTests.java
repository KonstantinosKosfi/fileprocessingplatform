package org.kos.fileprocessingplatform;

import org.kos.fileprocessingplatform.handler.MqService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:fileprocessing-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "app.folders.base=${java.io.tmpdir}/file-processing-platform-test",
        "app.folders.input=${app.folders.base}/data/input",
        "app.folders.output=${app.folders.base}/data/output",
        "app.folders.archive=${app.folders.base}/data/input/archive",
        "app.jwt.secret=test-secret-key-with-at-least-32-bytes",
        "app.jwt.expiration-ms=86400000",
        "app.jwt.issuer=file-processing-platform-test",
        "app.file-processing.max-file-size-mb=25",
        "app.file-processing.allowed-extensions[0]=xml",
        "app.mq.queue-in=TEST.QUEUE.IN",
        "app.mq.queue-out=TEST.QUEUE.OUT"
})
@ActiveProfiles("test")
class FileProcessingPropertiesPlatformApplicationTests {

    @MockitoBean
    private MqService mqService;

    @Test
    void contextLoads() {
    }

}
