package com.common.auth.common.enums;

public enum PermissionHierarchyLevel {
    ADMIN(4, "ADMIN"),
    OWNER(3, "OWNER"),
    MANAGER(2, "MANAGER"),
    USER(1, "USER");

    private final int level;
    private final String name;

    PermissionHierarchyLevel(int level, String name) {
        this.level = level;
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public boolean isHigherThan(PermissionHierarchyLevel other) {
        return this.level > other.level;
    }

    public boolean isAtLeast(PermissionHierarchyLevel other) {
        return this.level >= other.level;
    }

    public boolean isLowerThan(PermissionHierarchyLevel other) {
        return this.level < other.level;
    }

    public static PermissionHierarchyLevel getHighest(PermissionHierarchyLevel... levels) {
        if (levels == null || levels.length == 0) {
            return null;
        }

        PermissionHierarchyLevel highest = levels[0];
        for (PermissionHierarchyLevel level : levels) {
            if (level != null && (highest == null || level.level > highest.level)) {
                highest = level;
            }
        }
        return highest;
    }
}
