package com.profitera.services.business.notification;

import com.profitera.server.ServiceEngine;
import com.profitera.util.reflect.AccessDeniedException;
import com.profitera.util.reflect.InvocationTargetException;
import com.profitera.util.reflect.MethodNotFoundException;
import com.profitera.util.reflect.NoSuchClassException;
import com.profitera.util.reflect.ParameterMatchException;
import com.profitera.util.reflect.Reflect;

/**
 * @author jambu
 */
public class NotifierFactory {
  
  public static INotificationProcessor getNotificationProcessorInstance(String code) throws NotificationFailure {
    INotificationProcessor n = (INotificationProcessor) getInstance(code);
    n.setNotifierCode(code);
    // TODO: This impl should be dynamically loaded.
    n.setPropertyProvider(new ServiceEngineNotifierConfig());
    return n;
  }
  
  public static INotifier getNotifierInstance(String code) throws NotificationFailure {
    INotifier n = (INotifier) getInstance(code);
    n.setNotifierCode(code);
    // TODO: This impl should be dynamically loaded.
    n.setPropertyProvider(new ServiceEngineNotifierConfig());
    return n;
  }

  private static Object getInstance(String code) throws NotificationFailure {
    String className = getClassNameFromCode(code);
    if (className == null)
    	throw new NotificationFailure("No notifier class defined for notifier code " + code + "; there will no active notification agents for it.");
    	try {
			return Reflect.invokeConstructor(getClassNameFromCode(code), null, null);
		} catch (MethodNotFoundException e) {
			throw new NotificationFailure("Constructor was not found when attempting to instatiate notifier " + code + ".", e);
		} catch (AccessDeniedException e) {
			throw new NotificationFailure("Access denied while attempting to instatiate notifier " + code + ".", e);
		} catch (ParameterMatchException e) {
			throw new NotificationFailure("Constructor parameters do not match for notifier " + code + ".", e);
		} catch (InvocationTargetException e) {
			throw new NotificationFailure("Exception occured when invoking constructor of notifier " + code + ".", e);
		} catch (NoSuchClassException e) {
			throw new NotificationFailure("Implementation class for notifier " + code + " do not exist or not in classpath.", e);
		}
  }

  private static String getClassNameFromCode(String code) {
    String className = ServiceEngine.getProp(code + INotifier.SENDER_CLASS, null);
    if (className == null)
      className = ServiceEngine.getProp(code + INotificationProcessor.PROCESSOR_CLASS, null);
    return className;
  }
}