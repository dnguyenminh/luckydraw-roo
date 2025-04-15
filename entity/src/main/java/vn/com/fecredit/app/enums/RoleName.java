package vn.com.fecredit.app.enums;

public enum RoleName {
    ADMIN,
    MANAGER,
    OPERATOR,
    USER,
    GUEST;
    
    public static RoleName fromName(String name) {
        for (RoleName role : RoleName.values()) {
            if (role.name().equalsIgnoreCase(name)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role name: " + name);
    }
}