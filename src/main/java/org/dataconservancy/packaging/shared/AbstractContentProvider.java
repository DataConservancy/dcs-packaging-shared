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
import org.dataconservancy.packaging.tool.model.ipm.Node;

/**
 * Base class for providing package content to the IpmPackager class.
 * These objects should be constructed in a try-with-resources so that
 * close() is called afterwards, allowing the class to clean up resources.
 * @author Ben Trumbore (wbt3@cornell.edu).
 */
public abstract class AbstractContentProvider implements AutoCloseable {

    /**
     * Returns a representation of the domain model for this content provider.
     * @return The Model representing the domain objects.
     */
    public abstract Model getDomainModel();

    /**
     * Returns a representation of the IPM model for this content provider.
     * @return The Node representing the root of the IPM Model.
     **/
    public abstract Node getIpmModel();

    /**
     * Called at the completion of a try-with-resources that constructs an instance
     * of a derived class, allowing the instance to clean up resources.
     */
    public void close() {
    }
}