package dev.gavar.mojo.rc;

import dev.gavar.mojo.core.PropertySourceType;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.composite.ObjectWithFieldsConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import java.util.Arrays;
import java.util.Properties;

import static dev.gavar.mojo.util.ArrayUtils.forEach;
import static dev.gavar.mojo.util.GenericUtils.arrify;
import static java.lang.String.format;

/**
 * Injects properties to the runtime object object evaluated from the expression.
 */
public class OutputInjection extends AbstractOutput {

    public OutputInjection(MavenProject project, ExpressionEvaluator evaluator) {
        this.project = project;
        this.evaluator = evaluator;
    }

    private final MavenProject project;
    private final ExpressionEvaluator evaluator;

    /**
     * Expressions to evaluate for resolving target object to inject properties into.
     * Note that this <i>must not</i> include the surrounding {@code ${...}}
     * @see <a href="https://maven.apache.org/plugins/maven-help-plugin/evaluate-mojo.html">Mojo Evaluate</a>
     */
    private String[] expressions;
    public void setExpression(String expression) { setExpressions(arrify(expression, String[]::new)); }
    public void setExpressions(String[] expressions) {
        forEach(expressions, OutputInjection::checkExpression);
        this.expressions = expressions;
    }

    /** Array of beans evaluated from the {@link #expressions}. */
    public Properties[] getBeans() {
        return Arrays.stream(expressions)
                .map(this::evaluate)
                .toArray(Properties[]::new);
    }

    /**
     * Whether to allow overwriting target property value.
     * {@code true} by default for {@link PropertySourceType#PROJECT} properties.
     * @see #shouldRewrite
     */
    private Boolean rewrite;
    public Boolean isRewrite() { return rewrite; }
    public void setRewrite(Boolean rewrite) { this.rewrite = rewrite; }

    /** Determine whether properties should be overwritten when outputs to the given target properties. */
    public boolean shouldRewrite(Properties target) {
        if (rewrite != null) return true;
        return target == project.getProperties();
    }

    private Properties evaluate(final String expression) {
        checkExpression(expression);

        final Object bean;
        try {
            bean = evaluator.evaluate("${" + expression + "}");
        } catch (ExpressionEvaluationException e) {
            throw new IllegalArgumentException("unable to evaluate expression: " + expression, e);
        }

        if (bean == null)
            throw new IllegalArgumentException(format("expression '%s' evaluated to a null value", expression));

        if (bean instanceof Properties)
            return (Properties) bean;

        throw new IllegalArgumentException(format(
                "expression '%s' evaluated to bean of type '%s' but expected to be '%s'",
                expression, bean.getClass(), Properties.class
        ));
    }

    private static void checkExpression(String expression) {
        checkNotContains(expression, "${", "expression");
        checkNotContains(expression, "@{", "expression");
    }

    private static void checkNotContains(String value, CharSequence s, String name) {
        if (value.contains(s))
            throw new IllegalArgumentException(format("%s '%s' should not contain '%s'", name, value, s));
    }

    static class Converter extends ObjectWithFieldsConverter {

        @Override
        public boolean canConvert(Class<?> type) {
            return OutputInjection.class.equals(type);
        }

        @Override
        public Object fromConfiguration(final ConverterLookup lookup, final PlexusConfiguration configuration,
                                        final Class<?> type, final Class<?> enclosingType, final ClassLoader loader,
                                        final ExpressionEvaluator evaluator, final ConfigurationListener listener) throws ComponentConfigurationException {
            final MavenProject project;
            try {
                project = (MavenProject) evaluator.evaluate("${project}");
            } catch (ExpressionEvaluationException e) {
                throw new ComponentConfigurationException(e);
            }

            final OutputInjection bean = new OutputInjection(project, evaluator);
            processConfiguration(lookup, bean, loader, configuration, evaluator, listener);
            return bean;
        }
    }
}
