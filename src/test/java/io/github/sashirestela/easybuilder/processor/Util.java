package io.github.sashirestela.easybuilder.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Util {

    private Util() {
    }

    public static Constructor<?> constructor(Class<?> clazz, Class<?>... parameterTypes)
            throws NoSuchMethodException, SecurityException {
        return clazz.getDeclaredConstructor(parameterTypes);
    }

    public static Object instance(Constructor<?> constructor, Object... arguments)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return constructor.newInstance(arguments);
    }

    public static Method method(Class<?> clazz, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException, SecurityException {
        return clazz.getDeclaredMethod(name, parameterTypes);
    }

    public static Object invoke(Method method, Object object, Object... arguments)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return method.invoke(object, arguments);
    }

}
