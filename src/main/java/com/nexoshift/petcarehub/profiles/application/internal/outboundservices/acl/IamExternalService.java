package com.nexoshift.petcarehub.profiles.application.internal.outboundservices.acl;

import com.nexoshift.petcarehub.iam.interfaces.acl.IamContextFacade;
import org.springframework.stereotype.Service;

/**
 * IamExternal Service
 * <p>
 *     Provides IAM information and validation operations required by
 *     the Profile Bounded Context through the IAM Facade
 * </p>
 */
@Service
public class IamExternalService {

    private final IamContextFacade iamContextFacade;

    public IamExternalService(IamContextFacade iamContextFacade){
        this.iamContextFacade = iamContextFacade;
    }

    /**
     * Checks whether a user exists and is active.
     * @param userId The id of the user.
     * @return True if the user exists and is active, false otherwise.
     */
    public boolean existsActiveUser(Long userId){
        return iamContextFacade.existsActiveUser(userId);
    }

    /**
     * Checks whether an account exists and is active.
     * @param accountId The id of the account.
     * @return True if the account exists and is active, false otherwise.
     */
    public boolean existsActiveAccount(Long accountId) {
        return iamContextFacade.existsActiveAccount(accountId);
    }

    /**
     * Checks whether a user has the specified role.
     * @param userId The id of the user.
     * @param roleName The name of the role.
     * @return True if the user is active and has the role, false otherwise.
     */
    public boolean userHasRole(Long userId, String roleName) {
        return iamContextFacade.userHasRole(userId, roleName);
    }

    /**
     * Checks whether a user is the holder of an account.
     * @param userId The id of the user.
     * @param accountId The id of the account.
     * @return True if the user is the active holder, false otherwise.
     */
    public boolean isAccountHolder(Long userId, Long accountId) {
        return iamContextFacade.isAccountHolder(userId, accountId);
    }

    /**
     * Fetches the account id associated with a user.
     * @param userId The id of the user.
     * @return The account id, or 0 if the user does not exist.
     */
    public Long fetchAccountIdByUserId(Long userId) {
        return iamContextFacade.fetchAccountIdByUserId(userId);
    }

    /**
     * Fetches the account type of account.
     * @param accountId The id of the account.
     * @return The account type, or an empty string if it does not exist.
     */
    public String fetchAccountTypeByAccountId(Long accountId) {
        return iamContextFacade.fetchAccountTypeByAccountId(accountId);
    }

    /**
     * Fetches the email address associated with a user.
     * @param userId The id of the user.
     * @return The email address, or an empty string if the user does not exist.
     */
    public String fetchEmailByUserId(Long userId) {
        return iamContextFacade.fetchEmailByUserId(userId);
    }

    public Long fetchAuthenticatedUserId(){
        return iamContextFacade.fetchAuthenticatedUserId();
    }
}
