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
package org.mule.tooling.netbeans.xml;

import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Facundo Lopez Kaufmann
 */
@ServiceProvider(service = Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class UserCatalogProcessor extends LayerGeneratingProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(UserCatalog.class.getCanonicalName());
    }

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(UserCatalog.class)) {
            if (e.getKind() != ElementKind.CLASS) {
                continue;
            }
            TypeElement clazz = (TypeElement) e;
            String name = processingEnv.getElementUtils().getBinaryName(clazz).toString();
            LayerBuilder.File f = layer(e).file("Plugins/XML/UserCatalogs/" + name.replace(".", "-") + ".instance");
            f.newvalue("instanceCreate", name);
            f.stringvalue("instanceOf", CatalogReader.class.getName());
            f.write();
        }
        return true;
    }
}
