package org.substeps.glossary;

import com.technophobia.substeps.model.SubSteps;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * fake class
 */
@SubSteps.StepImplementations()
public class FakeSubsteps {

    /**
     *
     * another javadoc
     */
    public void nonStep(){
        // whatevs
    }

    // no docs
    @SubSteps.Step("FindById2 ([^\"]*)")
    public void findById2(final String id) {

    }

    /**
     * something without a location
     * @param id param
     * @org.substeps.step.example FindById3 username
     */
    @SubSteps.Step("FindById3 ([^\"]*)")
    public void findById3(final String id) {

    }

    /**
         * Find an element by it's ID
         *
         * @param id the id
         * @org.substeps.step.example FindById username
         * @org.substeps.step.section Location
         */
        @SubSteps.Step("FindById ([^\"]*)")
        public void findById(final String id) {

        }


        /**
         * Find an element by it's ID with
         * the specified timeout
         *
         * @param id      the id
         * @param timeout the timeout
         * @org.substeps.step.example FindByIdTimeout username timeout = 15 secs
         * @org.substeps.step.section Location
         */
        @SubSteps.Step("FindByIdTimeout ([^\"]*) timeout = ([^\"]*) secs")
        public void findById(final String id, final String timeout) {
        }



    /**
     * Finds a checkbox that is a child of the specified tag, that contains the
     * specified text; eg.
     * &lt;label&gt;<br/>
     * &lt;input type="checkbox" name="checkbox_name" value="yeah"/&gt;a checkbox &lt;span&gt;label&lt;/span&gt;<br/>
     * &lt;/label&gt;<br/>
     *
     * @param tag   the tag
     * @param label the checkbox label
     * @org.substeps.step.example FindCheckbox inside tag="label" with label="a checkbox label"
     * @org.substeps.step.section Location
     */
    @SubSteps.Step("FindCheckbox inside tag=\"?([^\"]*)\"? with label=\"([^\"]*)\"")
    public void findCheckBox(final String tag, final String label) {


    }


}
