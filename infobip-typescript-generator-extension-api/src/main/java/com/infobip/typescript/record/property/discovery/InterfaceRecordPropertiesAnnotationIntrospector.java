package com.infobip.typescript.record.property.discovery;

import tools.jackson.databind.PropertyName;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.*;

import java.util.stream.Stream;

public class InterfaceRecordPropertiesAnnotationIntrospector extends NopAnnotationIntrospector {

    @Override
    public PropertyName findNameForSerialization(MapperConfig<?> config, Annotated a) {
        if(!(a instanceof AnnotatedMember annotatedMember)) {
            return super.findNameForSerialization(config, a);
        }

        Class<?> declaringClass = annotatedMember.getDeclaringClass();

        if(!declaringClass.isInterface()) {
            return super.findNameForSerialization(config, a);
        }

        if(!declaringClass.isSealed()) {
            return super.findNameForSerialization(config, a);
        }

        var hasRecordGetter = Stream.of(declaringClass.getPermittedSubclasses())
                                    .filter(Class::isRecord)
                                    .flatMap(subclass -> Stream.of(subclass.getRecordComponents()))
                                    .anyMatch(component -> component.getName().equals(a.getName()));

        if(!hasRecordGetter) {
            return super.findNameForSerialization(config, a);
        }

        return new PropertyName(a.getName());
    }
}
