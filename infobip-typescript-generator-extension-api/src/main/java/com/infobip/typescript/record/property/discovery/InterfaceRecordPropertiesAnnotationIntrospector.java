package com.infobip.typescript.record.property.discovery;

import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.*;

import java.util.stream.Stream;

public class InterfaceRecordPropertiesAnnotationIntrospector extends NopAnnotationIntrospector {

    @Override
    public PropertyName findNameForSerialization(Annotated a) {
        if(!(a instanceof AnnotatedMember annotatedMember)) {
            return super.findNameForSerialization(a);
        }

        Class<?> declaringClass = annotatedMember.getDeclaringClass();

        if(!declaringClass.isInterface()) {
            return super.findNameForSerialization(a);
        }

        if(!declaringClass.isSealed()) {
            return super.findNameForSerialization(a);
        }

        var hasRecordGetter = Stream.of(declaringClass.getPermittedSubclasses())
                                    .filter(Class::isRecord)
                                    .flatMap(subclass -> Stream.of(subclass.getRecordComponents()))
                                    .anyMatch(component -> component.getName().equals(a.getName()));

        if(!hasRecordGetter) {
            return super.findNameForSerialization(a);
        }

        return new PropertyName(a.getName());
    }
}
