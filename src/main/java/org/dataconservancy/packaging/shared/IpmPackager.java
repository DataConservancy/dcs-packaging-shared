/*
 * Copyright 2016 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.packaging.shared;

import org.apache.jena.rdf.model.Model;

import org.dataconservancy.packaging.tool.api.Package;
import org.dataconservancy.packaging.tool.api.PackageGenerationService;
import org.dataconservancy.packaging.tool.impl.IpmRdfTransformService;
import org.dataconservancy.packaging.tool.model.BagItParameterNames;
import org.dataconservancy.packaging.tool.model.GeneralParameterNames;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.ParametersBuildException;
import org.dataconservancy.packaging.tool.model.PropertiesConfigurationParametersBuilder;
import org.dataconservancy.packaging.tool.model.RDFTransformException;
import org.dataconservancy.packaging.tool.model.ipm.Node;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

/**
 * Creates a DC package from a content provider, metadata and generation parameters.
 * @author Ben Trumbore (wbt3@cornell.edu).
 */
public class IpmPackager {

    private String packageLocation = System.getProperty("java.io.tmpdir");
    private String packageName = "MyPackage";

    private final ClassPathXmlApplicationContext cxt = new ClassPathXmlApplicationContext(
        "classpath*:org/dataconservancy/config/applicationContext.xml",
        "classpath*:org/dataconservancy/packaging/tool/ser/config/applicationContext.xml");

    /**
     * API call to create an instance of a packager.  The name and location of the packages
     * created by the packager can be changed before the package is built.
     */
    public IpmPackager() {
    }

    /**
     * API call to change the file location where a package will be created.
     * The location defaults to Java's temp IO folder.
     * @param packageLocation The existing folder where the package file will be created.
     */
    public void setPackageLocation(final String packageLocation) {
        this.packageLocation = packageLocation;
    }

    /**
     * API call to change the file name of the package that will be created.
     * The name defaults to "MyPackage".
     * @param packageName The root name of the package folder that will be created.
     */
    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    /**
     * API call for creating a package from the given content, metadata
     * and generation parameters.
     * @param contentProvider Source of domain objects and IpmTree content
     * @param metadataStream - A stream to metadata, as Java Properties.
     * @param paramsStream A stream to package generation parameters, as Java Properties.
     * @return A populated Package
     * @throws RuntimeException if the content cannot be processed successfully.
     */
    public Package buildPackage(final AbstractContentProvider contentProvider,
                                final InputStream metadataStream,
                                final InputStream paramsStream) {
        // Use try-with-resources here so the provider's close() is called afterwards.
        try (AbstractContentProvider provider = contentProvider) {
            // Create the state and populate it with the provided content
            final PackageState state = new PackageState();
            setDomainModel(state, contentProvider);
            setIpmModel(state, contentProvider);
            setMetadata(state, metadataStream);

            // Read the generation parameters, generate the package and return it.
            final PackageGenerationParameters params = getGenerationParameters(paramsStream);
            final PackageGenerationService generator = cxt.getBean(PackageGenerationService.class);
            return generator.generatePackage(state, params);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Assign a domain objects to the state.
     * @param state The state to which the domain objects is assigned.
     * @param contentProvider The source of the domain objects.
     */
    private void setDomainModel(final PackageState state, final AbstractContentProvider contentProvider) {
        final Model jenaModel = contentProvider.getDomainModel();
        state.setDomainObjectRDF(jenaModel);
    }

    /**
     * Assign an IPM model to the state.
     * @param state The state to which the IPM model is assigned.
     * @param contentProvider The source of the IPM model.
     * @throws RDFTransformException Thrown if the transformation from IPM to RDF fails.
     */
    private void setIpmModel(final PackageState state, final AbstractContentProvider contentProvider)
            throws RDFTransformException {
        final IpmRdfTransformService ipm2rdf = cxt.getBean(IpmRdfTransformService.class);
        final Node ipmTree = contentProvider.getIpmModel();
        final Model ipmRdfTree = ipm2rdf.transformToRDF(ipmTree);
        state.setPackageTree(ipmRdfTree);
    }

    /**
     * Assign metadata to the state.
     * @param state The state to which the metadata is assigned.
     * @param metadataStream A stream from which the metadata is read (as Java Properties).
     * @throws java.io.IOException Thrown if the Properties loading fails.
     */
    private void setMetadata(final PackageState state, final InputStream metadataStream) throws java.io.IOException {
        if (metadataStream == null) {
            return;
        }

        final LinkedHashMap<String, List<String>> metadata = new LinkedHashMap<>();
        final Properties props = new Properties();
        props.load(metadataStream);
        List<String> valueList;

        for (String key : props.stringPropertyNames()) {
            valueList = Arrays.asList(props.getProperty(key).trim().split("\\s*,\\s*"));
            // These required elements will be provided
            if (!key.equals(BagItParameterNames.BAGIT_PROFILE_ID) &&
                    !key.equals(BagItParameterNames.PACKAGE_MANIFEST)) {
                metadata.put(key, valueList);
            }
        }

        state.setPackageMetadataList(metadata);
    }

    /**
     * Read and return a set of package generation parameters.
     * @param paramsStream A stream from which the package generation parameters are read (as Java Properties).
     * @return The collection of parameters.
     * @throws ParametersBuildException Thrown if the parameter loading fails.
     */
    private PackageGenerationParameters getGenerationParameters(final InputStream paramsStream)
            throws ParametersBuildException {
        final PackageGenerationParameters params =
                new PropertiesConfigurationParametersBuilder().buildParameters(paramsStream);
        params.addParam(GeneralParameterNames.PACKAGE_LOCATION, packageLocation);
        params.addParam(GeneralParameterNames.PACKAGE_NAME, packageName);
        return params;
    }

}
