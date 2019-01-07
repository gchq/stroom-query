/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stroom.query.testing;

import org.junit.jupiter.api.extension.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class FifoLogbackExtensionSupport implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback, AfterEachCallback {
    private static Set<Field> findAnnotatedFields(Class<?> testClass, boolean isStaticMember) {
        final Set<Field> set = Arrays.stream(testClass.getDeclaredFields()).
                filter(m -> isStaticMember == Modifier.isStatic(m.getModifiers())).
                filter(m -> FifoLogbackExtension.class.isAssignableFrom(m.getType())).
                collect(Collectors.toSet());
        if (!testClass.getSuperclass().equals(Object.class)) {
            set.addAll(findAnnotatedFields(testClass.getSuperclass(), isStaticMember));
        }
        return set;
    }

    private static Object get(Field member, Object instance) throws IllegalAccessException {
        if (!member.canAccess(instance)) {
            member.setAccessible(true);
        }

        return member.get(instance);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        try {
            for (Field member : findAnnotatedFields(extensionContext.getRequiredTestClass(), true)) {
                ((FifoLogbackExtension) get(member, null)).after();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        final Object testInstance = extensionContext.getTestInstance()
                .orElseThrow(() -> new IllegalStateException("Unable to get the current test instance"));
        try {
            for (Field member : findAnnotatedFields(testInstance.getClass(), false)) {
                ((FifoLogbackExtension) get(member, testInstance)).after();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        try {
            for (Field member : findAnnotatedFields(extensionContext.getRequiredTestClass(), true)) {
                ((FifoLogbackExtension) get(member, null)).before();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        final Object testInstance = extensionContext.getTestInstance()
                .orElseThrow(() -> new IllegalStateException("Unable to get the current test instance"));
        try {
            for (Field member : findAnnotatedFields(testInstance.getClass(), false)) {
                ((FifoLogbackExtension) get(member, testInstance)).before();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
