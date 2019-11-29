package dev.gavar.mojo.rc;

import dev.gavar.mojo.core.PropertySourceType;
import dev.gavar.mojo.core.UpperEnumConverter;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.configurator.BasicComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

@Component(role = ComponentConfigurator.class, hint = "basic")
public class MojoConfigurator extends BasicComponentConfigurator implements Initializable {

    @Override
    public void initialize() {
        converterLookup.registerConverter(new PropertyFile.Converter());
        converterLookup.registerConverter(new PropertyFileSet.Converter());
        converterLookup.registerConverter(new OutputInjection.Converter());
        converterLookup.registerConverter(new UpperEnumConverter(PropertySourceType.class));
    }
}
