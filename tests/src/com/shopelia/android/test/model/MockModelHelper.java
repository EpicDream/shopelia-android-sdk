package com.shopelia.android.test.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MockModelHelper {

    public static <T> T get(T instance, String name) {
        try {
            Method method = instance.getClass().getMethod(name);
            method.invoke(instance);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return instance;
    }

}
