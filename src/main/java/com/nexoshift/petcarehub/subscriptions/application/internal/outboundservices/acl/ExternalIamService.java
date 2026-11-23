package com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.acl;

import com.nexoshift.petcarehub.iam.interfaces.acl.IamContextFacade;
import org.springframework.stereotype.Service;

/**
 * Provides IAM information required by the Subscriptions bounded context.
 */
@Service("subscriptionsExternalIamService")
public class ExternalIamService {

    private final IamContextFacade iamContextFacade;

    public ExternalIamService(IamContextFacade iamContextFacade) {
        this.iamContextFacade = iamContextFacade;
    }

    public boolean existsActiveAccount(Long accountId) {
        return iamContextFacade.existsActiveAccount(accountId);
    }

    public boolean isAccountHolder(Long userId, Long accountId) {
        return iamContextFacade.isAccountHolder(userId, accountId);
    }

    public String fetchAccountTypeByAccountId(Long accountId) {
        return iamContextFacade.fetchAccountTypeByAccountId(accountId);
    }

    public Long fetchAccountIdByUserId(Long userId) {
        return iamContextFacade.fetchAccountIdByUserId(userId);
    }

    public String fetchEmailByUserId(Long userId) {
        return iamContextFacade.fetchEmailByUserId(userId);
    }

    public Long fetchAuthenticatedUserId() {
        return iamContextFacade.fetchAuthenticatedUserId();
    }
}