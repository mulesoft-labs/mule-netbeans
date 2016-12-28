/*
 * Copyright 2016 Facundo Lopez Kaufmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mule.tooling.netbeans.config.catalog;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor2;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.util.NbBundle.Messages;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
//@UserCatalog
@Messages({
    "MuleCatalog_name=Mule Catalog\n",
    "MuleCatalog_description=Catalog for Mule XML schemas"
})
public class MuleCatalog implements CatalogReader, CatalogDescriptor2, EntityResolver {


    private static final Logger LOGGER = Logger.getLogger(MuleCatalog.class.getName());
    private static final String MULE_SCHEMAS_PACKAGE = "nbres:/org/mule/tooling/netbeans/config/catalog/";
    private final List<SchemaInfo> schemas = new ArrayList<SchemaInfo>();

    public MuleCatalog() {
        initialize();
    }

    private void initialize() {
        schemas.add(new SchemaInfo("mule-3.7.3.xsd", MULE_SCHEMAS_PACKAGE, "http://www.mulesoft.org/schema/mule/core"));
        schemas.add(new SchemaInfo("mule-ee-3.7.3.xsd", MULE_SCHEMAS_PACKAGE, "http://www.mulesoft.org/schema/mule/ee/core"));
        schemas.add(new SchemaInfo("mule-domain-3.7.3.xsd", MULE_SCHEMAS_PACKAGE, "http://www.mulesoft.org/schema/mule/domain"));
        schemas.add(new SchemaInfo("mule-domain-ee-3.7.3.xsd", MULE_SCHEMAS_PACKAGE, "http://www.mulesoft.org/schema/mule/ee/domain"));
        schemas.add(new SchemaInfo("mule-schemadoc-3.7.3.xsd", MULE_SCHEMAS_PACKAGE, "http://www.mulesoft.org/schema/mule/schemadoc"));
        schemas.add(new SchemaInfo("mule-http-3.7.3.xsd", MULE_SCHEMAS_PACKAGE, "http://www.mulesoft.org/schema/mule/http"));
        schemas.add(new SchemaInfo("mule-https-3.7.3.xsd", MULE_SCHEMAS_PACKAGE, "http://www.mulesoft.org/schema/mule/https"));
        schemas.add(new SchemaInfo("mule-db-3.7.3.xsd", MULE_SCHEMAS_PACKAGE, "http://www.mulesoft.org/schema/mule/db"));
        schemas.add(new SchemaInfo("spring-beans-3.2.xsd", MULE_SCHEMAS_PACKAGE, "http://www.springframework.org/schema/beans"));
        schemas.add(new SchemaInfo("spring-context-3.2.xsd", MULE_SCHEMAS_PACKAGE, "http://www.springframework.org/schema/context"));
        schemas.add(new SchemaInfo("spring-security-3.1.xsd", MULE_SCHEMAS_PACKAGE, "http://www.springframework.org/schema/security"));
        schemas.add(new SchemaInfo("spring-util-3.2.xsd", MULE_SCHEMAS_PACKAGE, "http://www.springframework.org/schema/util"));
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        LOGGER.log(Level.INFO, "Trying to resolve entity: pid={0}, sid={1}", new Object[]{publicId, systemId});
        if (systemId == null) {
            return null;
        }
        for (SchemaInfo each : schemas) {
            if (systemId.startsWith(each.namespace)) {
                return new InputSource(each.getResourcePath());
            }
        }
        return null;
    }

    @Override
    public Iterator getPublicIDs() {
        List<String> result = new ArrayList<String>();
        for (SchemaInfo each : schemas) {
            result.add(each.getPublicId());
        }
        return result.iterator();
    }

    @Override
    public void refresh() {
    }

    @Override
    public String getSystemID(String publicId) {
        if (publicId == null) {
            return null;
        }
        LOGGER.log(Level.INFO, "Trying to resolve systemID: {0}", publicId);
        for (SchemaInfo each : schemas) {
            if (each.getPublicId().equals(publicId)) {
                return each.getResourcePath();
            }
        }
        return null;
    }

    @Override
    public String resolveURI(String string) {
        LOGGER.log(Level.INFO, "Trying to resolve URI: {0}", string);
//        if(string.startsWith("http://www.mulesoft.org/schema") && string.contains("/current/")) {
//            return string.replace("/current/", "/3.7/");
//        }
        return null;
    }

    @Override
    public String resolvePublic(String string) {
        return null;
    }

    @Override
    public void addCatalogListener(CatalogListener cl) {
    }

    @Override
    public void removeCatalogListener(CatalogListener cl) {
    }
//

    @Override
    public String getIconResource(int i) {
        return "com/mule/tooling/netbeans/xml/mule-type.png";
    }

    @Override
    public String getDisplayName() {
        return Bundle.MuleCatalog_name();
    }

    @Override
    public String getShortDescription() {
        return Bundle.MuleCatalog_description();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pl) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pl) {
    }

    /**
     * <i>copied from j2ee/persistence PersistenceCatalog</i>
     */
    private static class SchemaInfo {

        private final String schemaName;
        private final String resourcePath;
        private final String namespace;

        public SchemaInfo(String schemaName, String resourcePath, String namespace) {
            this.schemaName = schemaName;
            this.resourcePath = resourcePath + schemaName;
            this.namespace = namespace;
        }

        public String getResourcePath() {
            return resourcePath;
        }

        public String getSchemaName() {
            return schemaName;
        }

        public String getPublicId() {
            return "SCHEMA:" + namespace + "/" + schemaName; //NOI18N
        }
    }
}
