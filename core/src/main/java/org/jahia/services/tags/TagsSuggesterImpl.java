/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.tags;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.jahia.services.content.*;
import org.jahia.services.query.QOMBuilder;
import org.jahia.services.query.QueryResultWrapper;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default TagSuggester implementation
 *
 * @author kevan
 */
public class TagsSuggesterImpl implements TagsSuggester{
    private boolean faceted = false;

    @Override
    public Map<String, Long> suggest(String input, String startPath, Long mincount, Long limit, Long offset,
                                     boolean sortByCount, JCRSessionWrapper sessionWrapper) throws RepositoryException {
        String converterdInput = TaggingService.getInstance().getTagHandler().execute(input);
        if(faceted){
            return facetedSuggestion(converterdInput, startPath, mincount, limit, offset, sortByCount, sessionWrapper);
        } else {
            return simpleSuggestion(converterdInput, startPath, limit, sessionWrapper);
        }
    }

    /**
     * Use a faceted query to suggest tags
     *
     * @param prefix the prefix to search on
     * @param startPath the path to start the search
     * @param mincount the minimum for a tag to be include in the result
     * @param limit the limit of tag returned
     * @param offset offset
     * @param sortByCount sort the map by count
     * @param sessionWrapper the session used to do the query
     * @return Map of tags retrieving the tag name and the count associate
     * @throws RepositoryException
     */
    protected Map<String, Long> facetedSuggestion (String prefix, String startPath, Long mincount, Long limit, Long offset,
                                               boolean sortByCount, JCRSessionWrapper sessionWrapper) throws RepositoryException {
        Map<String, Long> tagsMap = new LinkedHashMap<String, Long>();
        QueryManager queryManager = sessionWrapper.getWorkspace().getQueryManager();
        String searchPath = StringUtils.isEmpty(startPath) ? "/sites" : startPath;

        StringBuilder facet = new StringBuilder();
        facet.append("rep:facet(nodetype=jmix:tagged&key=j:tagList")
                .append(mincount != null ? "&facet.mincount=" + mincount.toString() : "")
                .append(limit != null ? "&facet.limit=" + limit.toString() : "")
                .append(offset != null ? "&facet.offset=" + offset.toString() : "")
                .append("&facet.sort=").append(String.valueOf(sortByCount))
                .append(StringUtils.isNotEmpty(prefix) ? "&facet.prefix=" + prefix : "")
                .append(")");

        QueryObjectModelFactory factory = queryManager.getQOMFactory();
        QOMBuilder qomBuilder = new QOMBuilder(factory, sessionWrapper.getValueFactory());

        qomBuilder.setSource(factory.selector("jmix:tagged", "tagged"));
        qomBuilder.andConstraint(factory.descendantNode("tagged", searchPath));
        qomBuilder.getColumns().add(factory.column("tagged", "j:tagList", facet.toString()));

        QueryObjectModel qom = qomBuilder.createQOM();
        QueryResultWrapper res = (QueryResultWrapper) qom.execute();

        if(res.getFacetField("j:tagList").getValues() != null){
            for(FacetField.Count count : res.getFacetField("j:tagList").getValues()){
                tagsMap.put(count.getName(), count.getCount());
            }
        }

        return tagsMap;
    }

    /**
     * Use a simple query to suggest tags
     *
     * @param term the term used to search tags
     * @param startPath the path to start the search
     * @param limit the limit of tag returned
     * @param sessionWrapper the session used to do the query
     * @return Map of tags retrieving the tag name
     * @throws RepositoryException
     */
    protected Map<String, Long> simpleSuggestion (final String term, String startPath, final Long limit, JCRSessionWrapper sessionWrapper) throws RepositoryException {
        // handle empty term
        if(StringUtils.isEmpty(term.trim())){
            return new HashMap<String, Long>();
        }

        QueryManager queryManager = sessionWrapper.getWorkspace().getQueryManager();
        String searchPath = StringUtils.isEmpty(startPath) ? "/sites" : startPath;
        Query query = queryManager.createQuery("select t.[j:tagList] from [jmix:tagged] as t where " +
                "isdescendantnode(t, [" + searchPath + "]) and t.[j:tagList] like '%" + JCRContentUtils.sqlEncode(term) + "%'", Query.JCR_SQL2);

        // use a scrollableQuery to iterate on contents, to avoid query returning too much nodes in one time
        ScrollableQuery scrollableQuery = new ScrollableQuery(100, query);
        return scrollableQuery.execute(new ScrollableCallback<Map<String, Long>>() {
            Map<String, Long> result = new LinkedHashMap<String, Long>();

            @Override
            boolean scroll() throws RepositoryException {
                NodeIterator nodeIterator = stepResult.getNodes();
                boolean limitReached = result.keySet().size() == limit;
                while (!limitReached && nodeIterator.hasNext()) {
                    JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodeIterator.next();
                    JCRValueWrapper[] tags = nodeWrapper.getProperty("j:tagList").getValues();
                    for (JCRValueWrapper tag : tags) {
                        String tagValue = tag.getString();
                        if (tagValue.contains(term)) {
                            if (result.keySet().size() < limit) {
                                result.put(tagValue, 0L);
                            }else {
                                // limit reached
                                limitReached = true;
                                break;
                            }
                        }
                    }
                }

                // continue until the limit is not reached
                return !limitReached;
            }

            @Override
            Map<String, Long> getResult() {
                return result;
            }
        });
    }

    public void setFaceted(boolean faceted) {
        this.faceted = faceted;
    }
}