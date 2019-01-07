package stroom.query.testing;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class DropwizardAppExtensionWithClientsSupport implements BeforeAllCallback, AfterAllCallback {
    private static Set<Field> findAnnotatedFields(Class<?> testClass, boolean isStaticMember) {
        final Set<Field> set = Arrays.stream(testClass.getDeclaredFields()).
                filter(m -> isStaticMember == Modifier.isStatic(m.getModifiers())).
                filter(m -> DropwizardAppExtensionWithClients.class.isAssignableFrom(m.getType())).
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
                ((DropwizardAppExtensionWithClients) get(member, null)).afterAll();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        try {
            for (Field member : findAnnotatedFields(extensionContext.getRequiredTestClass(), true)) {
                ((DropwizardAppExtensionWithClients) get(member, null)).beforeAll();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}