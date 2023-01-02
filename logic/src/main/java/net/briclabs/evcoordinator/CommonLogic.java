package net.briclabs.evcoordinator;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class CommonLogic {

    /**
     * Standardized method for obtaining "now" as a {@link OffsetDateTime} in the {@link java.time.ZoneOffset#UTC}.
     *
     * @return "now" as a {@link OffsetDateTime} in the {@link java.time.ZoneOffset#UTC}.
     */
    protected static OffsetDateTime getNow() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

}
