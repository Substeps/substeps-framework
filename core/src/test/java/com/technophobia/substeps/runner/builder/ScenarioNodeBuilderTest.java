package com.technophobia.substeps.runner.builder;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.technophobia.substeps.execution.node.BasicScenarioNode;
import com.technophobia.substeps.model.Background;
import com.technophobia.substeps.model.ExampleParameter;
import com.technophobia.substeps.model.Scenario;
import com.technophobia.substeps.model.Step;
import com.technophobia.substeps.runner.TestParameters;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.is;

/**
 * Created by ian on 15/04/16.
 */
public class ScenarioNodeBuilderTest {

    @Test
    public void testBasicScenarioBuilding(){

        Scenario scenario = new Scenario();

        scenario.setDescription("a test description with a <param>");

        Map parameters = ImmutableMap.of("param", "Parameter");
        ExampleParameter scenarioParameters = new ExampleParameter(5, parameters);

        TestParameters params = null;

        ScenarioNodeBuilder builder = new ScenarioNodeBuilder(params);

        BasicScenarioNode basicScenarioNode = builder.buildBasicScenarioNode(scenario, scenarioParameters, Collections.emptySet(), 1);

        Assert.assertThat(basicScenarioNode.getDescription() , is("a test description with a Parameter"));


        Scenario scenario2 = new Scenario();

        scenario2.setDescription("a test description without any parameters");

        BasicScenarioNode basicScenarioNode2 = builder.buildBasicScenarioNode(scenario2, scenarioParameters, Collections.emptySet(), 1);
        Assert.assertThat(basicScenarioNode2.getDescription() , is("a test description without any parameters"));

    }

}
