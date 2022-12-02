package io.fiter.ff4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.SneakyThrows;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.ff4j.FF4j;
import org.ff4j.audit.repository.EventRepository;
import org.ff4j.audit.repository.InMemoryEventRepository;
import org.ff4j.conf.FF4jConfiguration;
import org.ff4j.core.FeatureStore;
import org.ff4j.parser.yaml.YamlParser;
import org.ff4j.property.store.InMemoryPropertyStore;
import org.ff4j.property.store.PropertyStore;
import org.ff4j.store.InMemoryFeatureStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FF4JConfig {

    @Value("${fiter.features.ff4j}")
    private String ff4jFileName;

    @Autowired
    FineractProperties fineractProperties;

    @SneakyThrows
    @Bean
    public FF4j getFF4j() {

        final String templateRepository = fineractProperties.getContent().getFilesystem().getRootFolder() + File.separator + "ff4j"
                + File.separator + this.ff4jFileName;
        final File configFile = new File(templateRepository);
        InputStream targetStream = null;
        try {
            targetStream = new FileInputStream(configFile);
        } catch (IOException ex) {
            throw ex;
        }
        // We imported ff4j-config-yaml to have this
        FF4jConfiguration initConfig = new YamlParser().parseConfigurationFile(targetStream);
        // LOGGER.info("Default features have been loaded {}", initConfig.getFeatures().keySet());

        // 1. Define the store you want for Feature, Properties, Audit among 20 tech
        FeatureStore featureStore = new InMemoryFeatureStore(initConfig);
        PropertyStore propertyStore = new InMemoryPropertyStore(initConfig);
        EventRepository logsAudit = new InMemoryEventRepository();

        // 2. Build FF4j
        FF4j ff4jBean = new FF4j();
        ff4jBean.setPropertiesStore(propertyStore);
        ff4jBean.setFeatureStore(featureStore);
        ff4jBean.setEventRepository(logsAudit);

        // 3. Complete setup
        ff4jBean.setEnableAudit(false);
        ff4jBean.setAutocreate(true);
        return ff4jBean;
    }
}
