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
package org.jahia.services.render.filter;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.*;
import org.jahia.services.render.filter.cache.CacheKeyGenerator;
import org.jahia.services.render.filter.cache.PathCacheKeyPartGenerator;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import java.util.*;

/**
 * Aggregate render filter, in charge of aggregating fragment by resolving sub fragments
 *
 * Created by jkevan on 12/04/2016.
 */
public class AggregateFilter extends AbstractFilter {

    private static final Logger logger = LoggerFactory.getLogger(AggregateFilter.class);

    private static final String ESI_TAG_START = "<jahia_esi:include src=\"";
    private static final String ESI_TAG_END = "\"></jahia_esi:include>";
    private static final int ESI_TAG_END_LENGTH = ESI_TAG_END.length();

    private CacheKeyGenerator keyGenerator;

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {

        HttpServletRequest request = renderContext.getRequest();

        // Generates the key of the requested fragment. if we are currently aggregating a sub-fragment we already have the key
        // in the request, if not the KeyGenerator will create a key based on the request
        // (resource and context) and the cache properties. The generated key will contain temporary placeholders
        // that will be replaced to have the final key.
        String key = (String) request.getAttribute("aggregateFilter.aggregating");
        if (key != null) {
            request.removeAttribute("aggregateFilter.aggregating");
        } else {
            key = keyGenerator.generate(resource, renderContext, keyGenerator.getAttributesForKey(renderContext, resource));
        }

        if (request.getAttribute("aggregateFilter.rendering") != null) {
            request.setAttribute("aggregateFilter.rendering.submodule", key);
            return ESI_TAG_START + key + ESI_TAG_END;
        }

        logger.debug("Rendering node " + resource.getPath());

        request.setAttribute("aggregateFilter.rendering", key);
        request.setAttribute("aggregateFilter.rendering.time", System.currentTimeMillis());

        logger.debug("Aggregate filter for {}, key with placeholders: {}", resource.getPath(), key);

        return null;
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {

        HttpServletRequest request = renderContext.getRequest();

        if (request.getAttribute("aggregateFilter.rendering.submodule") != null) {
            request.removeAttribute("aggregateFilter.rendering.submodule");
            return previousOut;
        }

        String key = (String) request.getAttribute("aggregateFilter.rendering");
        logger.debug("Now aggregating subcontent for {}, key = {}", resource.getPath(), key);
        request.removeAttribute("aggregateFilter.rendering");
        return aggregateContent(previousOut, renderContext);
    }

    /**
     * Aggregate the content that are inside the cached fragment to get a full HTML content with all sub modules
     * embedded.
     *
     * @param content  The fragment, as it is stored in the cache
     * @param renderContext  The render context
     */
    protected String aggregateContent(String content, RenderContext renderContext) {
        int esiTagStartIndex = content.indexOf(ESI_TAG_START);
        if (esiTagStartIndex == -1) {
            return content;
        } else {
            StringBuilder sb = new StringBuilder(content);
            while (esiTagStartIndex != -1) {
                int esiTagEndIndex = sb.indexOf(ESI_TAG_END, esiTagStartIndex);
                if (esiTagEndIndex != -1) {
                    String key = sb.substring(esiTagStartIndex + ESI_TAG_START.length(), esiTagEndIndex);
                    try {
                        String replacement = generateContent(renderContext, key);
                        if (replacement == null) {
                            replacement = "";
                        }
                        sb.replace(esiTagStartIndex, esiTagEndIndex + ESI_TAG_END_LENGTH, replacement);
                        esiTagStartIndex = sb.indexOf(ESI_TAG_START, esiTagStartIndex + replacement.length());
                    } catch (RenderException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                } else {
                    // no closed esi end tag found
                    return sb.toString();
                }
            }
            return sb.toString();
        }
    }

    /**
     * Generates content for a sub fragment.
     *
     * @param renderContext  The render context
     * @param key       The cache key of the fragment to generate
     */
    protected String generateContent(RenderContext renderContext, String key) throws RenderException {

        try {

            // Parse the key to get all separate key attributes like node path and template
            Map<String, String> keyAttrbs = keyGenerator.parse(key);
            JCRSessionWrapper currentUserSession = JCRSessionFactory.getInstance().getCurrentUserSession(renderContext.getWorkspace(), LanguageCodeConverters.languageCodeToLocale(keyAttrbs.get("language")),
                    renderContext.getFallbackLocale());
            String path = StringUtils.replace(keyAttrbs.get("path"), PathCacheKeyPartGenerator.MAIN_RESOURCE_KEY, StringUtils.EMPTY);

            // create lazy resource
            Resource resource = new Resource(path, currentUserSession, keyAttrbs.get("templateType"), keyAttrbs.get("template"), keyAttrbs.get("context"));

            Map<String, Object> previous = keyGenerator.prepareContentForContentGeneration(keyAttrbs, resource, renderContext);

            // Store sub fragment key in attribute to use it in prepare() instead of re generating the sub fragment key
            renderContext.getRequest().setAttribute("aggregateFilter.aggregating", key);

            // Dispatch to the render service to generate the content
            String content = RenderService.getInstance().render(resource, renderContext);
            if (StringUtils.isBlank(content) && renderContext.getRedirect() == null) {
                logger.error("Empty generated content for key " + key);
            }

            keyGenerator.restoreContextAfterContentGeneration(keyAttrbs, resource, renderContext, previous);

            return content;

        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return StringUtils.EMPTY;
        }
    }


    public void setKeyGenerator(CacheKeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }
}