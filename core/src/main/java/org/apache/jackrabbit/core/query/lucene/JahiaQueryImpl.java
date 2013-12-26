/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.apache.jackrabbit.core.query.lucene;

import org.apache.jackrabbit.core.query.PropertyTypeRegistry;
import org.apache.jackrabbit.core.query.lucene.constraint.Constraint;
import org.apache.jackrabbit.core.session.SessionContext;
import org.apache.jackrabbit.spi.commons.query.QueryNodeFactory;
import org.apache.lucene.analysis.Analyzer;

import javax.jcr.query.InvalidQueryException;

public class JahiaQueryImpl extends QueryImpl {

    private static final String JCR_LANGUAGE = "jcr:language='";
    private static final String JCR_SYSTEM = "jcr:system";
    public static final int LENGTH = "jcr:language='".length();
    private Constraint constraint = null;
    private String statement = null;    

    public JahiaQueryImpl(SessionContext sessionContext, SearchIndex index,
                          PropertyTypeRegistry propReg, String statement, String language,
                          QueryNodeFactory factory) throws InvalidQueryException {
        super(sessionContext, index, propReg, statement, language, factory);
        this.statement = statement;
    }

    public Constraint getConstraint() {
        return constraint;
    }

    public void setConstraint(Constraint constraint) {
        this.constraint = constraint;
    }

    @Override
    public boolean needsSystemTree() {
        return statement.contains(JCR_SYSTEM);
    }

    protected Analyzer getTextAnalyzer() {
        // extract language code from statement if available
        int langIndex = statement.indexOf(JCR_LANGUAGE);
        if (langIndex >= 0 && langIndex <= statement.length() - LENGTH - 2) {
            final int end = langIndex + LENGTH;
            final String lang = statement.substring(end, end + 2);
            return index.getAnalyzerRegistry().getAnalyzer(lang);
        }

        return index.getTextAnalyzer();
    }
}
