/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.modulemanager.persistence;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 * Responsible for parsing module bundle info from the provided resource.
 *
 * @author Ahmed Chaabni
 * @author Sergiy Shyrkov
 */
public final class PersistedBundleInfoBuilder {

    private static final Attributes.Name ATTR_BUNDLE_NAME = new Attributes.Name("Bundle-Name");

    private static final Attributes.Name ATTR_BUNDLE_VERSION = new Attributes.Name("Bundle-Version");

    private static final Attributes.Name ATTR_GROUP_ID = new Attributes.Name("Jahia-GroupId");

    private static final Attributes.Name ATTR_IMPL_TITLE = new Attributes.Name("Implementation-Title");

    private static final Attributes.Name ATTR_IMPL_VERSION = new Attributes.Name("Implementation-Version");

    private static final Attributes.Name ATTR_SYMBOLIC_NAME = new Attributes.Name("Bundle-SymbolicName");

    private static final Logger logger = LoggerFactory.getLogger(PersistedBundleInfoBuilder.class);

    public static PersistedBundle build(Resource resource) throws IOException {
        // populate data from manifest
        String groupId = null;
        String symbolicName = null;
        String version = null;
        String displayName = null;
        try (JarInputStream jarIs = new JarInputStream(resource.getInputStream())) {
            Manifest mf = jarIs.getManifest();
            if (mf != null) {
                Attributes attrs = mf.getMainAttributes();
                groupId = attrs.getValue(ATTR_GROUP_ID);
                symbolicName = attrs.getValue(ATTR_SYMBOLIC_NAME);
                version = StringUtils.defaultIfBlank(attrs.getValue(ATTR_BUNDLE_VERSION),
                        attrs.getValue(ATTR_IMPL_VERSION));
                displayName = StringUtils.defaultIfBlank(attrs.getValue(ATTR_IMPL_TITLE),
                        attrs.getValue(ATTR_BUNDLE_NAME));
            }
        }

        if (StringUtils.isBlank(symbolicName) || StringUtils.isBlank(version)) {
            // not a valid JAR or bundle information is missing -> we stop here
            logger.warn("Not a valid JAR or bundle information is missing for resource " + resource);
            return null;
        }

        PersistedBundle bundleInfo = new PersistedBundle(groupId, symbolicName, version);
        bundleInfo.setDisplayName(displayName);

        // calculate checksum
        bundleInfo.setChecksum(calculateDigest(resource));

        bundleInfo.setResource(resource);

        return bundleInfo;
    }

    private static String calculateDigest(Resource resource) throws IOException {
        try (DigestInputStream digestInputStream = toDigestInputStream(
                new BufferedInputStream(resource.getInputStream()))) {
            byte[] b = new byte[1024 * 8];
            int read = 0;
            while (read != -1) {
                read = digestInputStream.read(b);
            }

            return Hex.encodeHexString(digestInputStream.getMessageDigest().digest());
        }
    }

    private static DigestInputStream toDigestInputStream(InputStream is) {
        try {
            return new DigestInputStream(is, MessageDigest.getInstance("MD5"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private PersistedBundleInfoBuilder() {
        super();
    }

}
