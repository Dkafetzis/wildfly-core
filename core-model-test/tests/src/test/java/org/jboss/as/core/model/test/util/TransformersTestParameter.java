/*
* JBoss, Home of Professional Open Source.
* Copyright 2012, Red Hat Middleware LLC, and individual contributors
* as indicated by the @author tags. See the copyright.txt file in the
* distribution for a full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.as.core.model.test.util;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.ModelVersion;
import org.jboss.as.core.model.test.ClassloaderParameter;
import org.jboss.as.model.test.ModelTestControllerVersion;
import org.jboss.as.version.Version;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class TransformersTestParameter extends ClassloaderParameter {

    private final ModelVersion modelVersion;
    private final ModelTestControllerVersion testControllerVersion;

    public TransformersTestParameter(ModelVersion modelVersion, ModelTestControllerVersion testControllerVersion) {
        this.modelVersion = modelVersion;
        this.testControllerVersion = testControllerVersion;
    }

    protected TransformersTestParameter(TransformersTestParameter delegate) {
        this(delegate.getModelVersion(), delegate.getTestControllerVersion());
    }

    public ModelVersion getModelVersion() {
        return modelVersion;
    }

    public ModelTestControllerVersion getTestControllerVersion() {
        return testControllerVersion;
    }

    public static List<TransformersTestParameter> setupVersions(){
        List<TransformersTestParameter> data = new ArrayList<TransformersTestParameter>();
        data.add(new TransformersTestParameter(ModelVersion.create(Version.MANAGEMENT_MAJOR_VERSION, Version.MANAGEMENT_MINOR_VERSION, Version.MANAGEMENT_MICRO_VERSION)
                , ModelTestControllerVersion.MASTER));

        //we only test EAP 7.4 and newer
        data.add(new TransformersTestParameter(ModelVersion.create(16, 0, 0), ModelTestControllerVersion.EAP_7_4_0));
        data.add(new TransformersTestParameter(ModelVersion.create(19, 0, 0), ModelTestControllerVersion.EAP_8_0_0));
        return data;
    }

    @Override
    public String toString() {
        return "TransformersTestParameters={modelVersion=" + modelVersion + "; testControllerVersion=" + testControllerVersion + "}";
    }


}
