package vn.com.fecredit.app.service.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import vn.com.fecredit.app.entity.AuditLog;
import vn.com.fecredit.app.entity.BlacklistedToken;
import vn.com.fecredit.app.entity.Configuration;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.GoldenHour;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.entity.Permission;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.SpinHistory;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.service.dto.ObjectType;

/**
 * Factory component that provides related table information for entity types.
 * Used to determine which related tables should be shown when viewing an entity.
 */
@Component
public class RelatedTablesFactory {

    private final Map<Class<?>, List<String>> relatedTablesMap = new HashMap<>();
    private final Map<ObjectType, List<String>> objectTypeRelatedTablesMap = new HashMap<>();

    public RelatedTablesFactory() {
        initializeRelatedTablesMap();
        initializeObjectTypeRelatedTablesMap();
    }

    /**
     * Initialize class-based mappings based on entity relationships
     */
    private void initializeRelatedTablesMap() {
        // User related tables
        List<String> userRelatedTables = new ArrayList<>();
        userRelatedTables.add("Roles");
        userRelatedTables.add("AuditLogs");
        userRelatedTables.add("BlacklistedTokens");
        userRelatedTables.add("CreatedEvents");  // Events created by this user
        userRelatedTables.add("ModifiedEvents");  // Events modified by this user
        relatedTablesMap.put(User.class, userRelatedTables);
        
        // Event related tables
        List<String> eventRelatedTables = new ArrayList<>();
        eventRelatedTables.add("Locations");  // Corrected to match test expectations
        eventRelatedTables.add("ParticipantEvents");
        eventRelatedTables.add("Participants");
        eventRelatedTables.add("Rewards");
        eventRelatedTables.add("SpinHistories");
        eventRelatedTables.add("GoldenHours");
        relatedTablesMap.put(Event.class, eventRelatedTables);
        
        // Region related tables
        List<String> regionRelatedTables = new ArrayList<>();
        regionRelatedTables.add("Provinces");
        regionRelatedTables.add("EventLocations");
        regionRelatedTables.add("Participants");  // Participants from this region
        relatedTablesMap.put(Region.class, regionRelatedTables);
        
        // Province related tables
        List<String> provinceRelatedTables = new ArrayList<>();
        provinceRelatedTables.add("Participants");
        provinceRelatedTables.add("Region");  // Parent region
        provinceRelatedTables.add("EventLocations");  // Event locations in this province
        relatedTablesMap.put(Province.class, provinceRelatedTables);
        
        // EventLocation related tables
        List<String> locationRelatedTables = new ArrayList<>();
        locationRelatedTables.add("Event");  // Parent event
        locationRelatedTables.add("Region");  // Region this location belongs to
        locationRelatedTables.add("Province");  // Province this location belongs to
        locationRelatedTables.add("Rewards");
        locationRelatedTables.add("GoldenHours");
        locationRelatedTables.add("ParticipantEvents");
        locationRelatedTables.add("SpinHistories");  // All spins at this location
        relatedTablesMap.put(EventLocation.class, locationRelatedTables);
        
        // Participant related tables
        List<String> participantRelatedTables = new ArrayList<>();
        participantRelatedTables.add("ParticipantEvents");
        participantRelatedTables.add("SpinHistories");
        participantRelatedTables.add("Province");
        participantRelatedTables.add("WonRewards");  // Rewards won by this participant
        relatedTablesMap.put(Participant.class, participantRelatedTables);
        
        // ParticipantEvent related tables
        List<String> participantEventRelatedTables = new ArrayList<>();
        participantEventRelatedTables.add("SpinHistories");
        participantEventRelatedTables.add("Event");
        participantEventRelatedTables.add("EventLocation");
        participantEventRelatedTables.add("Participant");
        participantEventRelatedTables.add("WonRewards");  // Rewards won in this participation
        participantEventRelatedTables.add("GoldenHours");  // Golden hours active during participation
        relatedTablesMap.put(ParticipantEvent.class, participantEventRelatedTables);
        
        // Reward related tables
        List<String> rewardRelatedTables = new ArrayList<>();
        rewardRelatedTables.add("SpinHistories");  // Spins that won this reward
        rewardRelatedTables.add("EventLocation");  // Location this reward belongs to
        rewardRelatedTables.add("Event");  // Event this reward belongs to
        rewardRelatedTables.add("Winners");  // Participants who won this reward
        relatedTablesMap.put(Reward.class, rewardRelatedTables);
        
        // Role related tables
        List<String> roleRelatedTables = new ArrayList<>();
        roleRelatedTables.add("Users");  // Users with this role
        roleRelatedTables.add("Permissions");  // Permissions granted by this role
        relatedTablesMap.put(Role.class, roleRelatedTables);
        
        // Permission related tables
        List<String> permissionRelatedTables = new ArrayList<>();
        permissionRelatedTables.add("Roles");  // Roles that include this permission
        permissionRelatedTables.add("Users");  // Users who have this permission via roles
        relatedTablesMap.put(Permission.class, permissionRelatedTables);
        
        // SpinHistory related tables
        List<String> spinHistoryRelatedTables = new ArrayList<>();
        spinHistoryRelatedTables.add("ParticipantEvent");  // The participation record
        spinHistoryRelatedTables.add("Participant");  // The participant who spun
        spinHistoryRelatedTables.add("Reward");  // The reward won (if any)
        spinHistoryRelatedTables.add("GoldenHour");  // Active golden hour during spin (if any)
        spinHistoryRelatedTables.add("Event");  // The event context
        spinHistoryRelatedTables.add("EventLocation");  // The location where spin occurred
        relatedTablesMap.put(SpinHistory.class, spinHistoryRelatedTables);
        
        // GoldenHour related tables
        List<String> goldenHourRelatedTables = new ArrayList<>();
        goldenHourRelatedTables.add("EventLocation");  // Location this golden hour applies to
        goldenHourRelatedTables.add("Event");  // Event this golden hour is part of
        goldenHourRelatedTables.add("SpinHistories");  // Spins that occurred during this golden hour
        goldenHourRelatedTables.add("AffectedRewards");  // Rewards affected by this golden hour
        relatedTablesMap.put(GoldenHour.class, goldenHourRelatedTables);
        
        // AuditLog related tables
        List<String> auditLogRelatedTables = new ArrayList<>();
        auditLogRelatedTables.add("User");  // User who performed the action
        auditLogRelatedTables.add("RelatedEntity");  // Entity that was affected
        relatedTablesMap.put(AuditLog.class, auditLogRelatedTables);
        
        // BlacklistedToken related tables
        List<String> blacklistedTokenRelatedTables = new ArrayList<>();
        blacklistedTokenRelatedTables.add("User");  // User who owned the token
        blacklistedTokenRelatedTables.add("AuditLogs");  // Audit logs related to this token
        relatedTablesMap.put(BlacklistedToken.class, blacklistedTokenRelatedTables);
        
        // Configuration related tables
        List<String> configRelatedTables = new ArrayList<>();
        configRelatedTables.add("AuditLogs");  // Audit logs of configuration changes
        relatedTablesMap.put(Configuration.class, configRelatedTables);
    }

    /**
     * Initialize object type-based mappings
     */
    private void initializeObjectTypeRelatedTablesMap() {
        // User related tables
        List<String> userRelatedTables = new ArrayList<>();
        userRelatedTables.add("Roles");
        userRelatedTables.add("AuditLogs");
        userRelatedTables.add("BlacklistedTokens");
        objectTypeRelatedTablesMap.put(ObjectType.User, userRelatedTables);
        
        // Event related tables
        List<String> eventRelatedTables = new ArrayList<>();
        eventRelatedTables.add("Locations");
        eventRelatedTables.add("ParticipantEvents");
        eventRelatedTables.add("Participants");
        eventRelatedTables.add("Rewards");
        eventRelatedTables.add("SpinHistories");
        objectTypeRelatedTablesMap.put(ObjectType.Event, eventRelatedTables);
        
        // Region related tables
        List<String> regionRelatedTables = new ArrayList<>();
        regionRelatedTables.add("Provinces");
        regionRelatedTables.add("EventLocations");
        objectTypeRelatedTablesMap.put(ObjectType.Region, regionRelatedTables);
        
        // Province related tables
        List<String> provinceRelatedTables = new ArrayList<>();
        provinceRelatedTables.add("Participants");
        objectTypeRelatedTablesMap.put(ObjectType.Province, provinceRelatedTables);
        
        // EventLocation related tables
        List<String> locationRelatedTables = new ArrayList<>();
        locationRelatedTables.add("Rewards");
        locationRelatedTables.add("GoldenHours");
        locationRelatedTables.add("ParticipantEvents");
        objectTypeRelatedTablesMap.put(ObjectType.EventLocation, locationRelatedTables);
        
        // Participant related tables
        List<String> participantRelatedTables = new ArrayList<>();
        participantRelatedTables.add("ParticipantEvents");
        participantRelatedTables.add("SpinHistories");
        participantRelatedTables.add("Province");
        objectTypeRelatedTablesMap.put(ObjectType.Participant, participantRelatedTables);
        
        // ParticipantEvent related tables
        List<String> participantEventRelatedTables = new ArrayList<>();
        participantEventRelatedTables.add("SpinHistories");
        participantEventRelatedTables.add("Event");
        participantEventRelatedTables.add("EventLocation");
        participantEventRelatedTables.add("Participant");
        objectTypeRelatedTablesMap.put(ObjectType.ParticipantEvent, participantEventRelatedTables);
        
        // Reward related tables
        List<String> rewardRelatedTables = new ArrayList<>();
        rewardRelatedTables.add("SpinHistories");
        rewardRelatedTables.add("EventLocation");
        objectTypeRelatedTablesMap.put(ObjectType.Reward, rewardRelatedTables);
        
        // Role related tables
        List<String> roleRelatedTables = new ArrayList<>();
        roleRelatedTables.add("Users");
        roleRelatedTables.add("Permissions");
        objectTypeRelatedTablesMap.put(ObjectType.Role, roleRelatedTables);
        
        // Permission related tables
        List<String> permissionRelatedTables = new ArrayList<>();
        permissionRelatedTables.add("Roles");
        objectTypeRelatedTablesMap.put(ObjectType.Permission, permissionRelatedTables);
        
        // SpinHistory related tables
        List<String> spinHistoryRelatedTables = new ArrayList<>();
        spinHistoryRelatedTables.add("ParticipantEvent");
        spinHistoryRelatedTables.add("Reward");
        spinHistoryRelatedTables.add("GoldenHour");
        objectTypeRelatedTablesMap.put(ObjectType.SpinHistory, spinHistoryRelatedTables);
        
        // GoldenHour related tables
        List<String> goldenHourRelatedTables = new ArrayList<>();
        goldenHourRelatedTables.add("EventLocation");
        goldenHourRelatedTables.add("SpinHistories");
        objectTypeRelatedTablesMap.put(ObjectType.GoldenHour, goldenHourRelatedTables);
        
        // AuditLog related tables
        List<String> auditLogRelatedTables = new ArrayList<>();
        auditLogRelatedTables.add("User");
        objectTypeRelatedTablesMap.put(ObjectType.AuditLog, auditLogRelatedTables);
        
        // BlacklistedToken related tables
        List<String> blacklistedTokenRelatedTables = new ArrayList<>();
        blacklistedTokenRelatedTables.add("User");
        objectTypeRelatedTablesMap.put(ObjectType.BlacklistedToken, blacklistedTokenRelatedTables);
        
        // Configuration doesn't have direct related tables
        objectTypeRelatedTablesMap.put(ObjectType.Configuration, new ArrayList<>());
    }

    /**
     * Get list of related table names for an entity
     * @param entity the entity object
     * @return list of related table names
     */
    public List<String> getRelatedTables(Object entity) {
        if (entity == null) {
            return new ArrayList<>();
        }
        return relatedTablesMap.getOrDefault(entity.getClass(), new ArrayList<>());
    }
    
    /**
     * Get list of related table names for an entity type
     * @param objectType the entity type
     * @return list of related table names
     */
    public List<String> getRelatedTables(ObjectType objectType) {
        if (objectType == null) {
            return new ArrayList<>();
        }
        return objectTypeRelatedTablesMap.getOrDefault(objectType, new ArrayList<>());
    }
    
    /**
     * Check if an entity object has related tables
     * @param entity the entity object
     * @return true if it has related tables
     */
    public boolean hasRelatedTables(Object entity) {
        if (entity == null) {
            return false;
        }
        return relatedTablesMap.containsKey(entity.getClass());
    }
    
    /**
     * Check if an entity type has related tables
     * @param objectType the entity type
     * @return true if it has related tables
     */
    public boolean hasRelatedTables(ObjectType objectType) {
        if (objectType == null) {
            return false;
        }
        return objectTypeRelatedTablesMap.containsKey(objectType);
    }
}