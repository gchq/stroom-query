package stroom.query.testing;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This rule effectively builds an Authentication and Authorisation service for use in integration tests.
 * <p>
 * It provides methods for getting authenticated and unauthenticated users, it also gives tests the ability
 * to grant permissions to specific users.
 */
public class StroomAuthenticationExtensionSupport implements BeforeAllCallback, AfterAllCallback {
    private static Set<Field> findAnnotatedFields(Class<?> testClass, boolean isStaticMember) {
        final Set<Field> set = Arrays.stream(testClass.getDeclaredFields()).
                filter(m -> isStaticMember == Modifier.isStatic(m.getModifiers())).
                filter(m -> StroomAuthenticationExtension.class.isAssignableFrom(m.getType())).
                collect(Collectors.toSet());
        if (!testClass.getSuperclass().equals(Object.class)) {
            set.addAll(findAnnotatedFields(testClass.getSuperclass(), isStaticMember));
        }
        return set;
    }

    private static Object get(Field member, Object instance) throws IllegalAccessException {

        //Java 8 does not provide Field.canAccess()
        //if (!member.canAccess(instance)) {
        if (true){
            member.setAccessible(true);
        }

        return member.get(instance);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        try {
            for (Field member : findAnnotatedFields(extensionContext.getRequiredTestClass(), true)) {
                ((StroomAuthenticationExtension) get(member, null)).afterAll();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        try {
            for (Field member : findAnnotatedFields(extensionContext.getRequiredTestClass(), true)) {
                ((StroomAuthenticationExtension) get(member, null)).beforeAll();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
