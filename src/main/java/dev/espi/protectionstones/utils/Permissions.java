package dev.espi.protectionstones.utils;

public final class Permissions {

    private Permissions() { }

    private static final String BASE = "protectionstones.";

    public static final String ADJACENT = BASE + ".adjacent.";

    /**
     * This permission allows users to override all ProtectionStones regions and use /ps admin and /ps reload.
     */
    public static final String ADMIN = BASE + "admin";

    /**
     * Allows players to use /ps buy and /ps sell.
     */
    public static final String BUY_SELL = BASE + "buysell";

    /**
     * Allows players the use of /ps count.
     */
    public static final String COUNT = BASE + "count";

    /**
     * Allows players the use of /ps count [player].
     */
    public static final String COUNT__OTHERS = COUNT + ".others";

    /**
     * Protect a region by placing a ProtectionStones block.
     */
    public static final String CREATE = BASE + "create";

    /**
     * Allow players to remove their own protected regions (block break).
     */
    public static final String DESTROY = BASE + "destroy";

    /**
     * Allows players to set their region flags.
     */
    public static final String FLAGS = BASE + "flags";

    /**
     * Set the permission to false for a flag (ex. protectionstones.flags.edit.tnt) so that
     * players cannot edit the flag with /ps flag even if it is on the "allowed_flags" list.
     */
    public static final String FLAGS__EDIT = FLAGS + ".edit.";

    /**
     * Allows players the use of /ps get.
     */
    public static final String GET = BASE + "get";

    /**
     * Allows players the use of /ps give (give protectionstones to others as admin).
     */
    public static final String GIVE = BASE + "give";

    /**
     * Allows players to hide their protectionstones block.
     */
    public static final String HIDE = BASE + "hide";

    /**
     * Access to the /ps home command.
     */
    public static final String HOME = BASE + "home";

    /**
     * Allows players the use of /ps info.
     */
    public static final String INFO = BASE + "info";

    /**
     * Allows players the use of /ps info in unowned regions.
     */
    public static final String INFO__OTHERS = INFO + ".others";

    public static final String LIMIT = BASE + ".limit.";

    /**
     * Allows players the use of /ps list.
     */
    public static final String LIST = BASE + "list";

    /**
     * Allows players the use of /ps list [player].
     */
    public static final String LIST__OTHERS = LIST + ".others";

    /**
     * Allows players to add or remove region members. Allows players to use /ps info members command.
     */
    public static final String MEMBERS = BASE + "members";

    /**
     * Allow players to merge their regions with other regions they own.
     */
    public static final String MERGE = BASE + "merge";

    /**
     * Access to the /ps name command.
     */
    public static final String NAME = BASE + "name";

    /**
     * Allows players to add or remove region owners. Allows players to use /ps info owners command.
     */
    public static final String OWNERS = BASE + "owners";

    /**
     * Allows players to set their region's priority.
     */
    public static final String PRIORITY = BASE + "priority";

    /**
     * Allows players to use the /ps region commands.
     */
    public static final String REGION = BASE + "region";

    /**
     * Allows players to use /ps rent.
     */
    public static final String RENT = BASE + "rent";

    public static final String RENT__LIMIT = RENT + ".limit.";

    /**
     * Access to the /ps sethome command.
     */
    public static final String SET_HOME = BASE + "sethome";

    /**
     * Allows access to /ps setparent.
     */
    public static final String SET_PARENT = BASE + "setparent";

    /**
     * Allow players to set their region to inherit properties from other regions they don't own.
     */
    public static final String SET_PARENT__OTHERS = SET_PARENT + ".others";

    /**
     * Allows players to override region permissions, and use ps commands without being the owner of a region.
     */
    public static final String SUPER_OWNER = BASE + "superowner";

    /**
     * 	Allows players to access /ps tax commands.
     */
    public static final String TAX = BASE + "tax";

    /**
     * Allows players to toggle ProtectionStones placement for their self.
     */
    public static final String TOGGLE = BASE + "toggle";

    /**
     * Access to /ps tp command.
     */
    public static final String TP = BASE + "tp";

    /**
     * Bypass prevent_teleport_in option in config.
     */
    public static final String TP__BYPASS_PREVENT = BASE + "bypassprevent";

    /**
     * Bypass the wait time set in the config for /ps home and /ps tp.
     */
    public static final String TP__BYPASS_WAIT = BASE + "bypasswait";

    /**
     * Allow players to unclaim their region using /ps unclaim.
     */
    public static final String UNCLAIM = BASE + "unclaim";

    /**
     * Allows players to unhide their protectionstones block.
     */
    public static final String UNHIDE = BASE + "unhide";

    /**
     * Allows players the use of /ps view.
     */
    public static final String VIEW = BASE + "view";

}
