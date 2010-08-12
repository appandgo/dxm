/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.impl.vfs;

import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileObject;
import org.apache.log4j.Logger;

import javax.jcr.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 23, 2008
 * Time: 11:45:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class VFSRepositoryImpl implements Repository {
    
    private static final transient Logger logger = Logger.getLogger(VFSRepositoryImpl.class);
    
    private String root;
    private String rootPath;

    private FileSystemManager manager;

    private Map<String, Value> repositoryDescriptors = new HashMap<String, Value>();

    private void initDescriptors() {
        repositoryDescriptors.put(Repository.OPTION_ACCESS_CONTROL_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_ACTIVITIES_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_BASELINES_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_JOURNALED_OBSERVATION_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_LIFECYCLE_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_LOCKING_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_NODE_AND_PROPERTY_WITH_SAME_NAME_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_NODE_TYPE_MANAGEMENT_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_OBSERVATION_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_QUERY_SQL_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_RETENTION_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_SHAREABLE_NODES_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_SIMPLE_VERSIONING_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_TRANSACTIONS_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_UNFILED_CONTENT_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_UPDATE_MIXIN_NODE_TYPES_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_UPDATE_PRIMARY_NODE_TYPE_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_VERSIONING_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_WORKSPACE_MANAGEMENT_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_XML_EXPORT_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_XML_IMPORT_SUPPORTED, new VFSValueImpl(false));
    }

    public VFSRepositoryImpl(String root) {
        initDescriptors();
        this.root = root;

        try {
            manager = VFS.getManager();
            rootPath = getFile("/").getName().getPath();
        } catch (FileSystemException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String getRoot() {
        return root;
    }

    public String getRootPath() {
        return rootPath;
    }

    public String[] getDescriptorKeys() {
        return repositoryDescriptors.keySet().toArray(new String[repositoryDescriptors.size()]);
    }

    public String getDescriptor(String s) {
        Value descriptorValue = repositoryDescriptors.get(s);
        if (descriptorValue == null) {
            return null;
        }
        try {
            return descriptorValue.getString();
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }

    public Session login(Credentials credentials, String s) throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return new VFSSessionImpl(this, credentials);
    }

    public Session login(Credentials credentials) throws LoginException, RepositoryException {
        return new VFSSessionImpl(this, credentials);
    }

    public Session login(String s) throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return new VFSSessionImpl(this, null);
    }

    public Session login() throws LoginException, RepositoryException {
        return new VFSSessionImpl(this, null);
    }

    public FileObject getFile(String path) throws FileSystemException {
        return manager.resolveFile(root+path);
    }

    public boolean isStandardDescriptor(String key) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isSingleValueDescriptor(String key) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Value getDescriptorValue(String key) {
        return repositoryDescriptors.get(key);
    }

    public Value[] getDescriptorValues(String key) {
        return new Value[0];  //To change body of implemented methods use File | Settings | File Templates.
    }
}
