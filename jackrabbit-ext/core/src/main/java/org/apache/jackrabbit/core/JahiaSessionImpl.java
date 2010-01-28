package org.apache.jackrabbit.core;

import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.apache.jackrabbit.core.security.authentication.AuthContext;

import javax.jcr.AccessDeniedException;
import javax.jcr.RepositoryException;
import javax.security.auth.Subject;

/**
 * Jackrabbit XASession extension for jahia
 *
 */
public class JahiaSessionImpl extends XASessionImpl {
    private JahiaNodeTypeInstanceHandler myNtInstanceHandler;

    public JahiaSessionImpl(RepositoryImpl rep, AuthContext loginContext, WorkspaceConfig wspConfig) throws AccessDeniedException, RepositoryException {
        super(rep, loginContext, wspConfig);
        init();
    }

    public JahiaSessionImpl(RepositoryImpl rep, Subject subject, WorkspaceConfig wspConfig) throws AccessDeniedException, RepositoryException {
        super(rep, subject, wspConfig);
        init();
    }

    private void init() {
        myNtInstanceHandler = new JahiaNodeTypeInstanceHandler(userId);
    }

    @Override
    public JahiaNodeTypeInstanceHandler getNodeTypeInstanceHandler() {
        return myNtInstanceHandler;
    }
}
