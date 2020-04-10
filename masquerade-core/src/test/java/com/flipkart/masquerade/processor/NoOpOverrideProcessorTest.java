package com.flipkart.masquerade.processor;

import com.flipkart.masquerade.Configuration;
import com.flipkart.masquerade.processor.type.NoOpOverrideProcessor;
import com.flipkart.masquerade.rule.Rule;
import com.flipkart.masquerade.test.ConfigurationExtension;
import com.flipkart.masquerade.test.annotation.ConfigProvider;
import com.flipkart.masquerade.util.Strings;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.flipkart.masquerade.util.Helper.getEntryClass;
import static com.flipkart.masquerade.util.Helper.getNoOpImplementationName;
import static com.flipkart.masquerade.util.Helper.getRepositoryClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by shrey.garg on 29/05/17.
 */
@ExtendWith(ConfigurationExtension.class)
public class NoOpOverrideProcessorTest {

    @Test
    public void createOverride(@ConfigProvider Configuration configuration) {
        NoOpOverrideProcessor processor = new NoOpOverrideProcessor(configuration, null);

        for (Rule rule : configuration.getRules()) {
            TypeSpec typeSpec = processor.createOverride(rule);
            assertEquals(getNoOpImplementationName(rule), typeSpec.name);
            assertEquals(1, typeSpec.methodSpecs.size());

            MethodSpec overriddenMethod = typeSpec.methodSpecs.get(0);
            assertEquals(Strings.INTERFACE_METHOD, overriddenMethod.name);
            assertEquals(4, overriddenMethod.parameters.size());
            assertEquals(Object.class.getName(), overriddenMethod.parameters.get(0).type.toString());
            assertEquals(rule.getEvaluatorClass().getName(), overriddenMethod.parameters.get(1).type.toString());
            assertEquals(getEntryClass(configuration).toString(), overriddenMethod.parameters.get(2).type.toString());
            assertEquals(getRepositoryClass(configuration).toString(), overriddenMethod.parameters.get(3).type.toString());
            assertTrue(overriddenMethod.code.isEmpty());
        }
    }

}