package com.technophobia.substeps.runner.description;

import com.technophobia.substeps.execution.node.IExecutionNode;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Description;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JunitLegacyDescriptionBuilderTest {

    private DescriptionBuilder descriptionBuilder;


    @Before
    public void initialise(){
        this.descriptionBuilder = new JunitLegacyDescriptionBuilder();
    }

    @Test
    @Ignore("This currently won't run, as the legacy description builder uses reflection to invoke a constructor that doesn't exist in junit 4.11")
    public void canCreateDescription(){

        final IExecutionNode node = mock(IExecutionNode.class);
        when(node.getDepth()).thenReturn(2);
        when(node.getDescription()).thenReturn("A description");

        Description description = descriptionBuilder.descriptionFor(node, new DescriptorStatus());
        assertThat(description.getDisplayName(), is("0-1: A description"));
    }
}
