package logic_core.infrastructure.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.persistence.EntityManagerFactory;
import logic_core.domain.service.LocalMediaStorageService;
import logic_core.domain.service.MediaStorageService;
import logic_core.infrastructure.media.MediaProperties;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "logic_core.infrastructure.repository"
)
@EntityScan(
        basePackages = "logic_core.infrastructure.persistence.entity"
)
public class AppConfig {

    // =====================================================================
    // Gson
    // =====================================================================

    @Bean
    public Gson gson() {
        return new GsonBuilder()
                .serializeNulls()
                .create();
    }

    // =====================================================================
    // Media storage (profile avatar/banner files)
    // =====================================================================

    @Bean
    public MediaProperties mediaProperties() {
        return new MediaProperties("data/media");
    }

    @Bean
    public MediaStorageService mediaStorageService(MediaProperties mediaProperties) {
        return new LocalMediaStorageService(mediaProperties);
    }
}
