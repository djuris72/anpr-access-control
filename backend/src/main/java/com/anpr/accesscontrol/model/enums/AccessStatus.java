package com.anpr.accesscontrol.model.enums;

/**
 * Ishod provere pristupa za detektovanu tablicu.
 */
public enum AccessStatus {
    GRANTED,        // tablica je na whitelist-i i ima vazeci pristup
    DENIED,         // tablica nije prepoznata kao ovlascena
    LOW_CONFIDENCE  // ML servis nije bio dovoljno siguran u ocitanu tablicu
}
