package dev.gavar.mojo.core;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.AbstractConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

public class UpperEnumConverter extends AbstractConfigurationConverter {

    private final Class<?> type;

    public UpperEnumConverter(Class<?> type) {
        this.type = type;
    }

    @Override
    public boolean canConvert(Class<?> type) {
        return this.type.equals(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object fromConfiguration(ConverterLookup lookup, PlexusConfiguration configuration,
                                    Class<?> type, Class<?> enclosingType, ClassLoader loader,
                                    ExpressionEvaluator evaluator, ConfigurationListener listener) throws ComponentConfigurationException {
        if (configuration.getChildCount() > 0)
            throw new ComponentConfigurationException("Basic element '" + configuration.getName()
                + "' must not contain child elements");

        Object value = fromExpression(configuration, evaluator);
        if (value instanceof String)
            try {
                value = Enum.valueOf((Class) type, ((String) value).toUpperCase());
            } catch (final RuntimeException e) {
                throw new ComponentConfigurationException("Cannot convert '" + value + "' to Enum", e);
            }

        return value;
    }
}
