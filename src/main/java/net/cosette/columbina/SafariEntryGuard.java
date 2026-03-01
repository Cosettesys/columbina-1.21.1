package net.cosette.columbina;

public class SafariEntryGuard {
    public static final ThreadLocal<Boolean> AUTHORIZED = ThreadLocal.withInitial(() -> false);
}