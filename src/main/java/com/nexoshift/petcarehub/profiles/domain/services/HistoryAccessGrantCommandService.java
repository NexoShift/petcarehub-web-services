package com.nexoshift.petcarehub.profiles.domain.services;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.HistoryAccessGrant;
import com.nexoshift.petcarehub.profiles.domain.model.commands.GrantHistoryAccessCommand;
import com.nexoshift.petcarehub.profiles.domain.model.commands.RevokeHistoryAccessCommand;

public interface HistoryAccessGrantCommandService {

    HistoryAccessGrant handle(GrantHistoryAccessCommand command);

    HistoryAccessGrant handle(RevokeHistoryAccessCommand command);
}
