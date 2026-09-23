package com.anpr.accesscontrol.model.enums;

/**
 * Nivo pristupa dodeljen vozilu na whitelist-i.
 */
public enum AccessLevel {
    RESIDENT,   // stanar / stalni zaposleni - trajni pristup
    STAFF,      // osoblje / dostava - pristup u odredjeno vreme
    GUEST       // gost - privremeni pristup, obicno sa datumom isteka
}
