package vn.com.fecredit.app.dto;

/**
 * Enum representing the types of objects that can be fetched from the API.
 * Based on the CommonAPIRequestAndResponse.puml diagram.
 */
public enum ObjectType {
    USER,
    ROLE,
    PERMISSION,
    PARTICIPANT,
    EVENT,
    EVENT_LOCATION,
    PROVINCE,
    REGION,
    REWARD,
    PARTICIPANT_EVENT,
    GOLDEN_HOUR,
    AUDIT_LOG,
    CONFIGURATION,
    WITHDRAWAL_REQUEST,
    REWARD_DISTRIBUTION
}
