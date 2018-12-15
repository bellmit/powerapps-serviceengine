package com.profitera.services.system.lookup;

import com.profitera.util.Utilities;

import java.util.HashMap;
import java.util.Map;

/**
 * This class acts as a registry for services, it is effectively a singleton
 * but designed to allow the injection of a new lookup mechanism in order
 * to allow for some flexibility, especially in testing.
 */
public class LookupManager {
    public static final Enum SYSTEM = new Enum("System");
    public static final Enum BUSINESS = new Enum("Business");
    private final Map<Enum, ServiceLookup> table;
    private static LookupManager lookup;

    public LookupManager() {
        table = new HashMap<>();
    }
    
    public static void setInstance(LookupManager lm) {
      lookup = lm;
  }

    public static LookupManager getInstance() {
        if (lookup == null) {
            lookup = new LookupManager();
            final ServiceLookup s = new ServiceLookup(Utilities.loadOrExit("SystemServices.properties"));
            final ServiceLookup b = new ServiceLookup(Utilities.loadOrExit("BusinessServices.properties"));
            lookup.table.put(SYSTEM, s);
            lookup.table.put(BUSINESS, b);
        }
        return lookup;
    }

    public ServiceLookup getLookup(final Enum type) {
        return table.get(type);
    }

    public Object getLookupItem(final Enum type, final String itemName) {
        final ServiceLookup lookup = (ServiceLookup) table.get(type);
        if (lookup == null) {
          throw new IllegalArgumentException(type + " lookup cannot be found");
        }
        return lookup.getService(itemName);
    }

    public final static class Enum {
        private String name;

        public Enum(String name) {
            this.name = name;
        }

        public boolean equals(Object o) {
            if (this == o) {
              return true;
            }
            if (!(o instanceof Enum)) {
              return false;
            }
            final Enum enumeration = (Enum) o;
            if (name != null ? !name.equals(enumeration.name) : enumeration.name != null){
              return false;
            }
            return true;
        }

        public int hashCode() {
            return (name != null ? name.hashCode() : 0);
        }

        public String toString() {
            return name;
        }
    }
}