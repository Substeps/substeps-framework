/*
 *	Copyright Technophobia Ltd 2012
 *
 *   This file is part of Substeps.
 *
 *    Substeps is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Substeps is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Substeps.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.technophobia.substeps.mojo.runner;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Ignore;

import com.technophobia.substeps.runner.SubstepsGlossaryMojo;

/**
 * @author ian
 * 
 */
@Ignore
public class MojoTest extends AbstractMojoTestCase {
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {

        // required for mojo lookups to work
        super.setUp();
    }


    /**
     * @throws Exception
     */
    public void testMojoGoal() throws Exception {
        final File testPom = new File(getBasedir(),
                "src/test/resources/basic-test-plugin-config.xml");

        final SubstepsGlossaryMojo mojo = (SubstepsGlossaryMojo) lookupMojo("integration-test", testPom);

        assertNotNull(mojo);
    }
}
