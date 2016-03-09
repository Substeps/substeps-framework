package com.technophobia.substeps.runner.description;

import com.technophobia.substeps.execution.node.IExecutionNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Junit411DescriptionBuilderTest {

    private DescriptionBuilder descriptionBuilder;


    @Before
    public void initialise() {
        this.descriptionBuilder = new Junit411DescriptionBuilder();
    }

    @Test
    public void canCreateDescription() {

        final IExecutionNode node = mock(IExecutionNode.class);
        when(node.getDepth()).thenReturn(2);
        when(node.getDescription()).thenReturn("A description");
        when(node.getFilename()).thenReturn("features.feature");
        when(node.getLineNumber()).thenReturn(32);

        Description description = descriptionBuilder.descriptionFor(node, new DescriptorStatus());
        assertThat(description.getDisplayName(), is("0-1: A description"));
    }
}
