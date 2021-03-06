package eu.nimble.service.catalogue.config;

import eu.nimble.utility.validation.NimbleRole;

public class RoleConfig {
    public static final NimbleRole[] REQUIRED_ROLES_FOR_ADMIN_OPERATIONS =
            {NimbleRole.PLATFORM_MANAGER};
    public static final NimbleRole[] REQUIRED_ROLES_CATALOGUE =
            {NimbleRole.COMPANY_ADMIN,
                    NimbleRole.EXTERNAL_REPRESENTATIVE,
                    NimbleRole.LEGAL_REPRESENTATIVE,
                    NimbleRole.INITIAL_REPRESENTATIVE,
                    NimbleRole.PUBLISHER,
                    NimbleRole.EFACTORY_USER};
    public static final NimbleRole[] REQUIRED_ROLES_TO_EXPORT_CATALOGUE =
            {NimbleRole.COMPANY_ADMIN,
                    NimbleRole.EXTERNAL_REPRESENTATIVE,
                    NimbleRole.LEGAL_REPRESENTATIVE,
                    NimbleRole.INITIAL_REPRESENTATIVE,
                    NimbleRole.PUBLISHER,
                    NimbleRole.NIMBLE_DELETED_USER,
                    NimbleRole.EFACTORY_USER};
}
