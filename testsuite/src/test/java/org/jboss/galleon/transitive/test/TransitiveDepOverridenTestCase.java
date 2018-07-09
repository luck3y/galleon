/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.galleon.transitive.test;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.runtime.ResolvedFeatureId;
import org.jboss.galleon.spec.FeatureParameterSpec;
import org.jboss.galleon.spec.FeatureSpec;
import org.jboss.galleon.state.ProvisionedFeaturePack;
import org.jboss.galleon.state.ProvisionedState;
import org.jboss.galleon.test.util.fs.state.DirState;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.MvnUniverse;
import org.jboss.galleon.universe.ProvisionFromUniverseTestBase;
import org.jboss.galleon.xml.ProvisionedConfigBuilder;
import org.jboss.galleon.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class TransitiveDepOverridenTestCase extends ProvisionFromUniverseTestBase {

    private FeaturePackLocation fp1Fpl;
    private FeaturePackLocation fp2Fpl;
    private FeaturePackLocation fp3_100_fpl;
    private FeaturePackLocation fp3_101_fpl;
    private FeaturePackLocation fp3_102_fpl;
    private FeaturePackLocation fp4Fpl;

    @Override
    protected void createProducers(MvnUniverse universe) throws ProvisioningException {
        universe.createProducer("producer1");
        universe.createProducer("producer2");
        universe.createProducer("producer3");
        universe.createProducer("producer4");
    }

    @Override
    protected void createFeaturePacks(FeaturePackCreator creator) throws ProvisioningException {

        fp1Fpl = newFpl("producer1", "1", "1.0.0.Final");
        fp2Fpl = newFpl("producer2", "1", "1.0.0.Final");
        fp3_100_fpl = newFpl("producer3", "1", "1.0.0.Final");
        fp3_101_fpl = newFpl("producer3", "1", "1.0.1.Final");
        fp3_102_fpl = newFpl("producer3", "1", "1.0.2.Final");
        fp4Fpl = newFpl("producer4", "1", "1.0.0.Final");

        creator.newFeaturePack()
            .setFPID(fp1Fpl.getFPID())
            .addDependency(fp3_100_fpl)
            .addSpec(FeatureSpec.builder("specA")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
            .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specA").setParam("p1", "1")).build())
            .newPackage("p1", true)
                .writeContent("fp1/p1.txt", "fp1");

        creator.newFeaturePack()
            .setFPID(fp2Fpl.getFPID())
            .addDependency(fp1Fpl)
            .addSpec(FeatureSpec.builder("specB")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
            .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specB").setParam("p1", "1"))
                .build())
            .newPackage("p1", true)
                .writeContent("fp2/p1.txt", "fp2");

        creator.newFeaturePack()
            .setFPID(fp3_100_fpl.getFPID())
            .addSpec(FeatureSpec.builder("specC")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
            .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specC").setParam("p1", "1"))
                .build())
            .newPackage("p1", true)
                .writeContent("fp3/p1.txt", "fp3 100");

        creator.newFeaturePack()
            .setFPID(fp3_101_fpl.getFPID())
            .addSpec(FeatureSpec.builder("specC")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
            .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specC").setParam("p1", "2"))
                .build())
            .newPackage("p1", true)
                .writeContent("fp3/p1.txt", "fp3 101");

        creator.newFeaturePack()
            .setFPID(fp3_102_fpl.getFPID())
            .addSpec(FeatureSpec.builder("specC")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
            .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specC").setParam("p1", "2"))
                .build())
            .newPackage("p1", true)
                .writeContent("fp3/p1.txt", "fp3 102");

        creator.newFeaturePack()
        .setFPID(fp4Fpl.getFPID())
        .addDependency(fp2Fpl).addDependency(FeaturePackConfig.forTransitiveDep(fp3_101_fpl))
            .addSpec(FeatureSpec.builder("specD")
                .addParam(FeatureParameterSpec.createId("p1"))
                .build())
            .addConfig(ConfigModel.builder("model1", "name1")
                .addFeature(new FeatureConfig("specD").setParam("p1", "1"))
                .build())
            .newPackage("p1", true)
                .writeContent("fp4/p1.txt", "fp4");

        creator.install();
    }

    @Override
    protected ProvisioningConfig provisioningConfig() throws ProvisioningException {
        return ProvisioningConfig.builder()
                .addFeaturePackDep(fp4Fpl)
                .addFeaturePackDep(fp3_102_fpl)
                .build();
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(fp1Fpl.getFPID())
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp2Fpl.getFPID())
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp4Fpl.getFPID())
                        .addPackage("p1")
                        .build())
                .addFeaturePack(ProvisionedFeaturePack.builder(fp3_102_fpl.getFPID())
                        .addPackage("p1")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .setModel("model1")
                        .setName("name1")
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(fp1Fpl.getFPID().getProducer(), "specA", "p1", "1")))
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(fp2Fpl.getFPID().getProducer(), "specB", "p1", "1")))
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(fp4Fpl.getFPID().getProducer(), "specD", "p1", "1")))
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(fp3_102_fpl.getFPID().getProducer(), "specC", "p1", "2")))
                        .build())
                .build();
    }

    @Override
    protected DirState provisionedHomeDir() {
        return newDirBuilder()
                .addFile("fp1/p1.txt", "fp1")
                .addFile("fp2/p1.txt", "fp2")
                .addFile("fp3/p1.txt", "fp3 102")
                .addFile("fp4/p1.txt", "fp4")
                .build();
    }
}