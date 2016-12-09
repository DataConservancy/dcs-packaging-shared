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

import org.apache.jena.rdf.model.*;
import org.dataconservancy.packaging.tool.model.ipm.Node;

/**
 * Base class for providing package content to the IpmPackager class.
 * Created by wbt on 11/30/16.
 */
abstract public class ContentProvider {
    abstract public Model getDomainModel();
    abstract public Node getIpmModel();
}