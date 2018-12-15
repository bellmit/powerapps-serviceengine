/*
 * Created on Sep 27, 2003
 */
package com.profitera.services.system.dataaccess;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * This bad-boy is thread-safe so go crazy sharing it. There are no sync
 * methods, instead the sync is done on the internal map. This way when a
 * large table is being dumped into the cache it doesn't lock the whole object,
 * instead it just locks the cache for a single operation. 
 * @author jamison
 *
 */
public class FullTableCache {
    private Map caches = new HashMap();
    private Map methods = new HashMap();

    public FullTableCache() {
        this(new Class[0], new String[0]);
    }

    /**
     * This method DOES NOT tell you if there are cached values for a class,
     * just whether or not the class has been registered for cacheing
     * @param clazz
     * @return
     */
    public boolean isCached(Class clazz) {
        return methods.get(clazz) != null;
    }

    public FullTableCache(Class[] classes, String[] keyMethods) {
        if (classes.length != keyMethods.length) {
            throw new RuntimeException("Cache class count must be the same as the cache key method count");
        }
        for (int i = 0; i < classes.length; i++) {
            methods.put(classes[i], keyMethods[i]);
        }
        clearCache();
    }

    public void addCachedClass(Class clazz, String keyMethod) {
        synchronized (methods) {
            methods.put(clazz, keyMethod);
        }
    }

    /**
     * This method allows you to sneak in the values of your choice, 
     * rather than having the system do it for you, beware of cache
     * clearances though, because they will cause this cache to be
     * re-stocked in the conventional manner!
     * @param clazz
     * @param string
     * @param values
     */
    public void addCachedClass(Class clazz, String string, List values) {
        synchronized (caches) {
            addCachedClass(clazz, string);
            stockCache(clazz, values);
        }
    }

    /**
     * Clears the whole cache.
     */
    public void clearCache() {
        // Synchronize on caches when changing
        synchronized (caches) {
            caches = new HashMap();
        }
    }

    /**
     * Clears the cache for the selected objects only
     * @param clazz
     */
    public void clearCache(Class clazz) {
        //Synchronize on caches when changing
        synchronized (caches) {
            caches.put(clazz, null);
        }
    }

    private void stockCache(Class clazz) {
        Class clazz1 = clazz;
        Vector v = new QueryManager().getAll(clazz1);
        stockCache(clazz, v);
    }

    private void stockCache(Class clazz, List l) {
        Iterator i = l.iterator();
        Method method;
        Exception thrown;
        try {
            Map m = new HashMap(l.size());
            method = clazz.getMethod((String) methods.get(clazz), null);
            while (i.hasNext()) {
                Object val = i.next();
                Object returnVal = method.invoke(val, null);
                m.put(returnVal, val);
            }
            // Synchronize on caches when changing
            synchronized (caches) {
                caches.put(clazz, m);
            }
            return;
        } catch (SecurityException e) {
            thrown = e;
        } catch (NoSuchMethodException e) {
            thrown = e;
        } catch (IllegalArgumentException e) {
            thrown = e;
        } catch (IllegalAccessException e) {
            thrown = e;
        } catch (InvocationTargetException e) {
            thrown = e;
        }
        throw new RuntimeException("Failure invoking cache method " + methods.get(clazz), thrown);
    }

    /**
     * Retrieve an object from the cache based on the key presented,
     * the index is the index of the class/method passed into
     * the constructor, if the cache is not yet built it will query
     * ALL the objects out of that table to build the cache.
     * @param clazz
     * @param key
     * @return
     */
    public Object getObject(Class clazz, Object key) {
        Map m = (Map) caches.get(clazz);
        if (m == null) {
            stockCache(clazz);
            return getObject(clazz, key);
        }
        return m.get(key);
    }

    public Iterator getObjects(Class clazz) {
        Map m = (Map) caches.get(clazz);
        if (m == null) {
            stockCache(clazz);
            return getObjects(clazz);
        }
        return m.values().iterator();
    }
}
