package com.technophobia.substeps.runner.description;

import com.technophobia.substeps.execution.node.IExecutionNode;
import org.junit.Assert;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class AbstractReflectiveDescriptionBuilder extends AbstractDescriptionBuilder {

    private final Logger log = LoggerFactory.getLogger(AbstractReflectiveDescriptionBuilder.class);

    public Description descriptionFor(IExecutionNode node, DescriptorStatus status) {
        Description newInstance = null;

        Constructor<Description> constructor;
        try {
            constructor = Description.class.getDeclaredConstructor(constructorParameterTypes());
            constructor.setAccessible(true);

            newInstance = constructor.newInstance(constructorArguments(node, status));
        } catch (final SecurityException e) {
            log.error(e.getMessage(), e);
        } catch (final NegativeArraySizeException e) {
            log.error(e.getMessage(), e);
        } catch (final NoSuchMethodException e) {
            log.error(e.getMessage(), e);
        } catch (final IllegalArgumentException e) {
            log.error(e.getMessage(), e);
        } catch (final InstantiationException e) {
            log.error(e.getMessage(), e);
        } catch (final IllegalAccessException e) {
            log.error(e.getMessage(), e);
        } catch (final InvocationTargetException e) {
            log.error(e.getMessage(), e);
        }

        Assert.assertNotNull(newInstance);
        return newInstance;
    }

    protected abstract Class<?>[] constructorParameterTypes();

    protected abstract Object[] constructorArguments(IExecutionNode node, DescriptorStatus status);
}
