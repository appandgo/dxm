/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.usermanager.jcr;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerRoutingService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerRoutingService;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Implementation of the JahiaGroup interface that uses the JCR API for storage
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 8 juil. 2009
 */
public class JCRGroup extends JahiaGroup {
    private transient static Logger logger = Logger.getLogger(JCRGroup.class);
    private String nodeUuid;
    private final JCRTemplate jcrTemplate;
    static final String J_HIDDEN = "j:hidden";
    private static final String PROVIDER_NAME = "jcr";

    public JCRGroup(Node nodeWrapper, JCRTemplate jcrTemplate, int siteID) {
        this.jcrTemplate = jcrTemplate;
        this.mSiteID = siteID;
        try {
            this.nodeUuid = nodeWrapper.getIdentifier();
            this.mGroupname = nodeWrapper.getName();
            this.mGroupKey = mGroupname + ":" + siteID;
            this.hidden = nodeWrapper.getProperty(J_HIDDEN).getBoolean();
            this.mMembers = getMembersMap(nodeWrapper);
        } catch (RepositoryException e) {
            logger.error("Error while accessing repository", e);
        }
    }

    /**
     * Get grp's properties list.
     *
     * @return Return a reference on the grp's properties list, or null if no
     *         property is present.
     */
    public Properties getProperties() {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Properties>() {
                 public Properties doInJCR(JCRSessionWrapper session) throws RepositoryException {
                     Properties properties = new Properties();
                     PropertyIterator iterator = getNode(session).getProperties();
                     for (; iterator.hasNext();) {
                         Property property = iterator.nextProperty();
                         if (!property.isMultiple()) {
                             properties.put(property.getName(), property.getString());
                         }
                     }
                     return properties;
                 }
            });
        } catch (RepositoryException e) {
            logger.error("Error while retrieving group properties", e);
        }
        return null;
    }

    /**
     * Retrieve the requested grp property.
     *
     * @param key Property's name.
     * @return Return the property's value of the specified key, or null if the
     *         property does not exist.
     */
    public String getProperty(final String key) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<String>() {
                 public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    return getNode(session).getProperty(key).getString();
                 }
            });
        } catch (PathNotFoundException pnfe) {
            // This is expected in the case the property doesn't exist in the repository. We will simply return null.
            return null;
        } catch (RepositoryException e) {
            logger.error("Error while retrieving group property " + key, e);
            return null;
        }
    }

    /**
     * Remove the specified property from the properties list.
     *
     * @param key Property's name.
     */
    public boolean removeProperty(final String key) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Node node = getNode(session);
                    Property property = node.getProperty(key);
                    if (property != null) {
                        session.checkout(node);
                        property.remove();
                        session.save();
                        return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
                }
            });
        } catch (RepositoryException e) {
            logger.warn("Error while removing property " + key, e);
        }
        return false;
    }

    /**
     * Add (or update if not already in the property list) a property key-value
     * pair in the grp's properties list.
     *
     * @param key   Property's name.
     * @param value Property's value.
     */
    public boolean setProperty(final String key, final String value) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Node node = getNode(session);
                    session.checkout(node);
                    node.setProperty(key, value);
                    session.save();
                    return Boolean.TRUE;
                }
            });
        } catch (RepositoryException e) {
            logger.warn("Error while setting property " + key + " with value " + value, e);
        }
        return false;
    }

    /**
     * Adds the specified member to the group.
     *
     * @param principal The principal to add to this group.
     * @return Return true if the member was successfully added, false if the
     *         principal was already a member.
     */
    public boolean addMember(final Principal principal) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRUser jcrUser = null;
                    if (principal instanceof JCRUser) {
                        jcrUser = (JCRUser) principal;
                    } else if (principal instanceof JahiaUser) {
                        jcrTemplate.getProvider("/").deployExternalUser(principal.getName(),
                                ((JahiaUser) principal).getProviderName());
                        jcrUser = (JCRUser) JCRUserManagerProvider.getInstance().lookupExternalUser(principal.getName());
                    }
                    if (jcrUser != null) {
                        Node node = getNode(session);
                        Node members = node.getNode("j:members");
                        if (!members.hasNode(principal.getName())) {
                            Node member = members.addNode(principal.getName(), Constants.JAHIANT_MEMBER);
                            member.setProperty("j:member", jcrUser.getNodeUuid());
                            session.save();
                        }
                        return true;
                    }
                    return false;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error while adding group member", e);
        }
        return false;
    }

    /**
     * Returns the group's home page id.
     * -1 : undefined
     *
     * @return int The group homepage id.
     */
    public int getHomepageID() {        
        return -1;
    }

    /**
     * Set the home page id.
     *
     * @param id the group homepage id.
     * @return false on error
     */
    public boolean setHomepageID(int id) {
        // TODO we will need to implement this if we want to support group homepages again.
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns a hashcode for this principal.
     *
     * @return A hashcode for this principal.
     */
    public int hashCode() {
        return nodeUuid.hashCode();
    }

    /**
     * Returns members of this group. If members were not loaded before,
     * forces loading.
     *
     * @return members of this group
     */
    protected Map<String, Principal> getMembersMap() {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Map<String, Principal>>() {
                public Map<String, Principal> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    final Node node = getNode(session);
                    return getMembersMap(node);
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error while retrieving group member map", e);
        }
        return new HashMap<String, Principal>();
    }

    private Map<String, Principal> getMembersMap(Node node) throws RepositoryException {
        Map<String, Principal> principalMap = new HashMap<String, Principal>();
        Node members = node.getNode("j:members");
        NodeIterator iterator = members.getNodes();
        while (iterator.hasNext()) {
            Node member = (Node) iterator.next();
            if (member.isNodeType(Constants.JAHIANT_MEMBER)) {
                JahiaUser jahiaUser = JahiaUserManagerRoutingService.getInstance().lookupUser(member.getName());
                if (jahiaUser != null) {
                    principalMap.put(member.getName(), jahiaUser);
                } else {
                    String s = member.getName().replace("___", ":");
                    JahiaGroup g = JahiaGroupManagerRoutingService.getInstance().lookupGroup(s);
                    if (g != null) {
                        principalMap.put(s, g);
                    } else {
                        logger.warn("Member '" + member.getName() + "' cannot be found for group '" + node.getName()
                                + "'");
                    }
                }
            }
        }
        return principalMap;
    }

    /**
     * Removes the specified member from the group.
     *
     * @param principal The principal to remove from this group.
     * @return Return true if the principal was removed, or false if the
     *         principal was not a member.
     */
    public boolean removeMember(final Principal principal) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Node group = getNode(session);
                    Node members = group.getNode("j:members");
                    if (principal instanceof JCRUser) {
                        JCRUser jcrUser = (JCRUser) principal;
                        return removeMember(session, members, jcrUser);
                    }
                    return Boolean.FALSE;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error while removing member", e);
        }
        return false;
    }

    private boolean removeMember(JCRSessionWrapper session, Node members, JCRUser jcrUser) throws RepositoryException {
        if (session.getWorkspace().getQueryManager() != null) {
            String query = "SELECT * FROM [jnt:member] as m where m.[j:member] = '" + jcrUser.getNodeUuid() + "' AND ISCHILDNODE(m, '" + members.getPath() + "') ORDER BY m.[j:nodename]";
            Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
            QueryResult qr = q.execute();
            NodeIterator nodes = qr.getNodes();
            while (nodes.hasNext()) {
                Node memberNode = nodes.nextNode();
                memberNode.remove();
            }
            session.save();
            return true;
        }
        return false;
    }

    /**
     * Returns a string representation of this group.
     *
     * @return A string representation of this group.
     */
    @Override
    public String toString() {
        StringBuffer output = new StringBuffer("Details of group [" + mGroupname + "] :\n");

        output.append("  - ID : ").append(getNodeUuid()).append("\n");

        output.append("  - properties :");

        Properties properties = getProperties();
        Iterator names = new EnumerationIterator(properties.propertyNames());
        String name;
        if (names.hasNext()) {
            output.append("\n");
            while (names.hasNext()) {
                name = (String) names.next();
                output.append("       ").append(name).append(" -> [").append(properties.getProperty(name)).append(
                        "]\n");
            }
        } else {
            output.append(" -no properties-\n");
        }

        // Add the user members useranames detail
        output.append("  - members : ");

        if (mMembers != null) {
            if (mMembers.size() > 0) {
                for (String member : mMembers.keySet()) {
                    output.append(member).append("/");
                }
            } else {
                output.append(" -no members-\n");
            }
        } else {
            output.append(" -preloading of members disabled-\n");
        }

        return output.toString();
    }

    /**
     * Get the name of the provider of this group.
     *
     * @return String representation of the name of the provider of this group
     */
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    private Node getNode(JCRSessionWrapper session) throws RepositoryException {
        return session.getNodeByUUID(nodeUuid);
    }

    public String getNodeUuid() {
        return nodeUuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JCRGroup jcrGroup = (JCRGroup) o;

        return nodeUuid.equals(jcrGroup.nodeUuid);

    }
}
