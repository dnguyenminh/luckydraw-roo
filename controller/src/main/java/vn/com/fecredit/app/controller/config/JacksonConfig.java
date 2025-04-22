package vn.com.fecredit.app.controller.config;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import vn.com.fecredit.app.entity.*;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.build();
        
        // Register JavaTimeModule for proper date/time handling
        objectMapper.registerModule(new JavaTimeModule());
        
        // Disable features that cause problems
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        // Configure Jackson to handle circular references
        configureEntityMixIns(objectMapper);
        
        return objectMapper;
    }
    
    private void configureEntityMixIns(ObjectMapper objectMapper) {
        // Add mixins for all entities with circular references
        objectMapper.addMixIn(User.class, UserMixIn.class);
        objectMapper.addMixIn(Role.class, RoleMixIn.class);
        objectMapper.addMixIn(Permission.class, PermissionMixIn.class);
        objectMapper.addMixIn(Event.class, EntityMixIn.class);
        objectMapper.addMixIn(EventLocation.class, EntityMixIn.class);
        objectMapper.addMixIn(Reward.class, EntityMixIn.class);
        objectMapper.addMixIn(Participant.class, EntityMixIn.class);
        objectMapper.addMixIn(RewardEvent.class, EntityMixIn.class);
        objectMapper.addMixIn(SpinHistory.class, EntityMixIn.class);
        objectMapper.addMixIn(Province.class, EntityMixIn.class);
        objectMapper.addMixIn(Region.class, EntityMixIn.class);
    }

    // MixIn classes to handle circular references
    
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "roles"})
    abstract class UserMixIn {}
    
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "users", "permissions"})
    abstract class RoleMixIn {}
    
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "roles"})
    abstract class PermissionMixIn {}
    
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    abstract class EntityMixIn {}
}
