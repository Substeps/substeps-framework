package com.technophobia.substeps.runner.description;

import com.technophobia.substeps.execution.node.IExecutionNode;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class JunitVersionedDescriptionBuilder implements DescriptionBuilder {

    private final Logger log = LoggerFactory.getLogger(JunitVersionedDescriptionBuilder.class);

    private DescriptionBuilder descriptionBuilder = null;

    @Override
    public Description descriptionFor(IExecutionNode executionNode, DescriptorStatus descriptorStatus) {
        if (descriptionBuilder == null) {
            descriptionBuilder = initialiseDescriptionBuilder();
        }
        return descriptionBuilder.descriptionFor(executionNode, descriptorStatus);
    }

    private DescriptionBuilder initialiseDescriptionBuilder() {
        log.debug("Creating DescriptionBuilder");
        if (isJunit411()) {
            log.debug("Creating a Junit411DescriptionBuilder");
            return new Junit411DescriptionBuilder();
        }
        log.debug("Creating a JunitLegacyDescriptionBuilder");
        return new JunitLegacyDescriptionBuilder();
    }

    private boolean isJunit411() {
        try {
            Description.class.getMethod("createTestDescription", String.class, String.class, Serializable.class);
            log.debug("Junit 4.11 is on the classpath");
            return true;
        } catch (NoSuchMethodException e) {
            log.debug("Junit 4.11 is not on the classpath", e);
            return false;
        }
    }
}
