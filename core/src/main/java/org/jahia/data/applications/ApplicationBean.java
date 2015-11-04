/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.data.applications;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.pluto.container.om.portlet.UserAttribute;
import org.jahia.registries.ServicesRegistry;

/**
 * This object contains all the data relative to an application, notably the
 * context in which it should run, it's type (servlet or JSP) and additionally
 * information such as rights, etc...
 *
 * @author Serge Huber
 * @version 1.0
 */
public class ApplicationBean implements Serializable, Comparator<ApplicationBean>, Cloneable {

    private static final long serialVersionUID = -5886294839254670413L;

    private String ID;
    private String name;
    private String context;
    private boolean visible = false;
    private String description = "";
    private String type;
    private List<EntryPointInstance> entryPointInstances;
    private transient List<UserAttribute> userAttributes;

    /**
     * Basic constructor
     * @param ID
     * @param name
     * @param context
     * @param visible
     * @param desc
     * @param type
     */
    public ApplicationBean(String ID, String name,String context,boolean visible,String desc,String type) {
        setID(ID);
        this.name = name;
        this.context = context;
        this.visible = visible;
        this.description = desc;
        this.type = type;
    } // end constructor

    /**
     * accessor methods
     * {
     */
    public String getID() {
        return ID;
    }

    /**
     * Get name
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Get context
     *
     * @return
     */
    public String getContext() {
        return context;
    }

    /**
     * Get visible status
     *
     * @return
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Get description
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set id
     *
     * @param ID
     */
    public void setID(String ID) {
        this.ID = ID;
    }

    /**
     * Set name
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set visibility
     *
     * @param visible
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Set description
     *
     * @param descr
     */
    public void setDescription(String descr) {
        this.description = descr;
    }

    // end accessor methods

    /**
     * Compare methode
     *
     * @param c1
     * @param c2
     * @return
     * @throws ClassCastException
     */
    public int compare(ApplicationBean c1, ApplicationBean c2)
            throws ClassCastException {

        return (c1.getName().compareToIgnoreCase(c2.getName()));

    }

    /**
     * Get type
     *
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * Get all entrypoint definition
     *
     * @return
     */
    public List<EntryPointDefinition> getEntryPointDefinitions() {
        return ServicesRegistry.getInstance().getApplicationsManagerService().getAppEntryPointDefinitions(this);
    }

    /**
     * find EntryPointInstance by definition name
     *
     * @param definitionName
     * @return
     */
    public EntryPointDefinition getEntryPointDefinitionByName(String definitionName) {
        EntryPointDefinition entryPointDefinition = null;
        Iterator<EntryPointDefinition> entryPointDefinitions = getEntryPointDefinitions().iterator();
        while (entryPointDefinitions.hasNext()) {
            EntryPointDefinition curEntryPointDefinition =  entryPointDefinitions.next();
            if (curEntryPointDefinition.getName().equals(definitionName)) {
                entryPointDefinition = curEntryPointDefinition;
                break;
            }
        }
        return entryPointDefinition;
    }

    /**
     * Get entryPointInstance
     *
     * @return
     */
    public List<EntryPointInstance> getEntryPointInstances() {
        return entryPointInstances;
    }

    /**
     * Set entrypoint
     *
     * @param entryPointInstances
     */
    public void setEntryPointInstances(List<EntryPointInstance> entryPointInstances) {
        this.entryPointInstances = entryPointInstances;
    }

    /**
     * Clone current object
     *
     * @return
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException cnse) {
            // should never happen as we do support Cloneable in the interface
            return null;
        }
    }

    /**
     * eqiasl method
     *
     * @param o
     * @return
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ApplicationBean that = (ApplicationBean) o;

        return ID.equals(that.ID);

    }

    /**
     * return hashcode
     *
     * @return
     */
    public int hashCode() {
        return ID.hashCode();
    }

    /**
     * Get user attributes
     *
     * @return
     */
    public List<UserAttribute> getUserAttributes() {
        return userAttributes;
    }

    /**
     * Set user attributes
     *
     * @param userAttributes
     */
    public void setUserAttributes(List<UserAttribute> userAttributes) {
        this.userAttributes = userAttributes;
    }
}
