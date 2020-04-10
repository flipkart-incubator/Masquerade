package com.flipkart.masquerade.processor;

import com.flipkart.masquerade.Configuration;
import com.flipkart.masquerade.rule.Rule;
import com.flipkart.masquerade.test.ConfigurationExtension;
import com.flipkart.masquerade.test.annotation.ConfigProvider;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.lang.model.element.Modifier;
import java.util.Optional;

import static com.flipkart.masquerade.util.Helper.getRuleInterface;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by shrey.garg on 29/05/17.
 */
@ExtendWith(ConfigurationExtension.class)
public class ReferenceMapProcessorTest {

    @Test
    public void addMap(@ConfigProvider Configuration configuration) {
        TypeSpec.Builder builder = TypeSpec.classBuilder("Cloak");

        ReferenceMapProcessor processor = new ReferenceMapProcessor(configuration, builder);
        for (Rule rule : configuration.getRules()) {
            processor.addMap(rule);
        }

        TypeSpec typeSpec = builder.build();
        assertEquals(configuration.getRules().size(), typeSpec.fieldSpecs.size());

        for (Rule rule : configuration.getRules()) {
            Optional<FieldSpec> fieldSpec = typeSpec.fieldSpecs.stream().filter(f -> f.name.equals(rule.getName())).findFirst();
            assertEquals(true, fieldSpec.isPresent());
            assertTrue(fieldSpec.get().hasModifier(Modifier.PRIVATE));
            assertTrue(fieldSpec.get().type instanceof ParameterizedTypeName);
            assertEquals("trunk.getTestOne()", fieldSpec.get().initializer.toString());

            ParameterizedTypeName typeName = (ParameterizedTypeName) fieldSpec.get().type;
            assertEquals(2, typeName.typeArguments.size());
            assertEquals(String.class.getName(), typeName.typeArguments.get(0).toString());
            assertEquals(getRuleInterface(configuration, rule).toString(), typeName.typeArguments.get(1).toString());
        }
    }

}