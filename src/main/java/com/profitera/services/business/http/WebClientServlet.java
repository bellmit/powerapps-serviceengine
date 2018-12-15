package com.profitera.services.business.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.xml.sax.SAXException;

import com.profitera.client.conf.ErrorMessageBuilder;
import com.profitera.descriptor.business.ServiceException;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.login.UserBean;
import com.profitera.descriptor.business.login.UserRoleBean;
import com.profitera.io.SerializableInputStreamWrapper;
import com.profitera.io.TransferWithBuffer;
import com.profitera.log.DefaultLogProvider;
import com.profitera.log.ILogProvider;
import com.profitera.services.business.http.impl.DocumentationStreamer;
import com.profitera.services.business.http.impl.ServletUtil;
import com.profitera.services.business.login.MapLoginService;
import com.profitera.services.business.login.ServerSession;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.Strings;
import com.profitera.util.io.FileUtil;
import com.profitera.webclient.IRenderingContext;
import com.profitera.webclient.data.Jsonifier;
import com.profitera.webclient.form.DefaultRenderingContext;
import com.profitera.webclient.form.IFormRenderer;
import com.profitera.webclient.form.IWebLanguage;
import com.profitera.webclient.form.WebClientLog;
import com.profitera.webclient.form.WebFieldValue;
import com.profitera.webclient.form.WebForm;
import com.profitera.webclient.form.WebFormCache;
import com.profitera.webclient.form.WebLanguageManager;

public class WebClientServlet extends HttpServlet {
  private static final String LOGIN_SERVICE = "LoginService";
  private static final String AUTH = "auth";
  private static final String DOCUMENT_UPLOAD = "document.upload";
  private static final String DOC_DOWNLOAD_EVENT = "document.download";
  private static final int LANG_COOKIE_EXPIRY = 60 * 60 * 24 * 365;
  private static final String LANG_COOKIE = "pwc_lang_override";
  private static final Charset UTF8 = Charset.forName("UTF-8");
  private static final long serialVersionUID = 1L;
  private static final int DEFAULT_UPLOAD_LIMIT_MB = 5;
  private final AtomicInteger formCounter = new AtomicInteger(0);
  private final WebFormCache cache;
  private ErrorMessageBuilder errorMessageBuilder;
  private final String errorMessagePath;
  private final WebLanguageManager lang;
  private final File style;
  private DefaultLogProvider log;

  public WebClientServlet(File formDirectory) {
    this.errorMessagePath = new File(formDirectory, "errormessages.xml").getAbsolutePath();
    lang = new WebLanguageManager(new File(formDirectory, "language.properties"));
    cache = new WebFormCache(formDirectory);
    style = new File(formDirectory, "style.css");
  }

  protected final void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      HttpSession session = req.getSession(false);
      if (req.getServletPath().equals("/logout")) {
        doLogout(req, resp, session);
      } else if (req.getServletPath().equals("/login")) {
        doLogin(req, resp, session);
      } else {
        // For now no posting without a session.
        if (getSession(req) == null) {
          resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          return;
        }
        if (req.getServletPath().startsWith("/event/")) {
          sendEvent(req, resp, req.getServletPath(), getSession(req), req.getRemoteAddr());
        } else if (req.getServletPath().endsWith("/upload") || req.getServletPath().endsWith("/upload/")) {
          handleUpload(req, resp);
        } else {
          // Now we are handling just any post request under the app context.
          // This will generally be form submission.
          resp.getOutputStream().write(
              ("Posted to " + req.getServletPath()).getBytes(UTF8));
          resp.setStatus(HttpURLConnection.HTTP_OK);
          /*Map<String, String[]> parameterMap = getParameterMap(req);
          for (Map.Entry<String, String[]> e : parameterMap.entrySet()) {
            System.out.println(e.getKey() + " "
                + Strings.getListString(e.getValue(), ", "));
          }*/
        }
      }
    } catch (ServiceException e) {
      sendErrorMessage(resp, e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, req);
    }
  }

  protected void handleUpload(HttpServletRequest req, HttpServletResponse resp) throws UnsupportedEncodingException,
      IOException, ServiceException {
    boolean isMultipart = ServletFileUpload.isMultipartContent(req);
    if (!isMultipart) {
      throw new IllegalStateException("Requests to '/upload/' must be multipart file uploads");
    }
    DiskFileItemFactory factory = new DiskFileItemFactory();
    factory.setSizeThreshold(getUploadMemorySizeLimit());
    ServletFileUpload upload = new ServletFileUpload(factory);
    upload.setSizeMax(getUploadSizeLimit());
    FileItemIterator items = null;
    try {
      Map<String, Object> arguments = new HashMap<String, Object>();
      Map<String, Object> fileDetails = new HashMap<String, Object>();
      items = getUploadItems(req, upload);
      while (items.hasNext()) {
        FileItemStream item = items.next();
        InputStream fieldStream = item.openStream();
        if (item.isFormField()) {
          arguments.put(item.getFieldName(), Streams.asString(fieldStream, UTF8.name()));
        } else {
          String fileName = item.getName();
          String contentType = item.getContentType();
          byte[] readEntireStream = FileUtil.readEntireStream(fieldStream, 1024*1024);
          fileDetails.put("FILE_CONTENT", new SerializableInputStreamWrapper(new ByteArrayInputStream(readEntireStream)));
          fileDetails.put("FILE_NAME", fileName);
          fileDetails.put("FILE_CONTENT_TYPE", contentType);
        }
      }
      WebForm f = getForm(req.getParameter("form"));
      arguments = f.normalizeTypes(arguments);
      arguments.putAll(fileDetails);
      Object o = fireServerEvent(DOCUMENT_UPLOAD, arguments,
          getSession(req), req.getRemoteAddr());
      sendJsonIfAccepted(req, resp, o, true);
      resp.setStatus(HttpURLConnection.HTTP_OK);
    } catch (FileUploadException e) {
      getLog().emit(WebClientLog.UPLOAD_ERROR, e, upload.getSizeMax()/1024/1024);
      throw new ServiceException("upload", new TransferObject(TransferObject.ERROR, e.getMessage()));
    } finally {
      try {
        while (items != null && items.hasNext()) {
          items.next();
        }
      } catch (FileUploadException e) { //NOPMD
        // Ignore this, we are just exhausting the stream.
      }
    }
  }

  private ILogProvider getLog() {
    if (log == null) {
      log = new DefaultLogProvider();
      log.register(new WebClientLog());
    }
    return log;
  }

  private FileItemIterator getUploadItems(HttpServletRequest req, ServletFileUpload upload) throws FileUploadException, IOException {
    return upload.getItemIterator(req);
  }

  private int getUploadMemorySizeLimit() {
    return 1024*1024*2; // 2MB
  }
  private int getUploadSizeLimit() {
    return 1024*1024*DEFAULT_UPLOAD_LIMIT_MB;
  }

  private void doLogout(HttpServletRequest req, HttpServletResponse resp,
      HttpSession session) throws IOException {
    logOffCurrentSession(req, session);
    String path = getServletContext().getContextPath() + "/";
    if (ServletUtil.acceptsJson(req)) {
      resp.getOutputStream().write(new Jsonifier().render(path).getBytes(UTF8));
      resp.setStatus(HttpURLConnection.HTTP_OK);
    } else {
      ServletUtil.redirectTo(resp, path);
    }
  }

  private void logOffCurrentSession(HttpServletRequest req, HttpSession session) {
    Long sessionId = getSession(req);
    if (sessionId != null) {
      ServerSession.THREAD_SESSION.set(sessionId);
      getLoginService().logoff(getUser(session), sessionId);
    }
  }

  private void doLogin(HttpServletRequest req, HttpServletResponse resp,
      HttpSession currentSession) throws IOException, ServiceException {
    HttpSession session;
    if (currentSession == null) {
      session = req.getSession();
    } else {
      session = currentSession;
    }
    // If there is an already logged-in session running, log it off.
    try { // Never fail to do this even if an error does occur.
      logOffCurrentSession(req, session);
    } catch (Throwable t) {
      getLog().emit(WebClientLog.LOGOUT_ERROR, t);
    }
    Map<String, String> values;
    boolean isJsonRequest = req.getContentType().equals("application/json");
    if (isJsonRequest) {
      values = getJsonArguments(req);
    } else {
      values = resolveFormTextValuesForLogin(req);
    }
    String user = Strings.coalesce(values.get("USER"), values.get("user"));
    String password = Strings.coalesce(values.get("PASSWORD"), values.get("password"));
    String role = Strings.coalesce(values.get("ROLE"), "*");
    TransferObject login = getLoginService().login(user, password);
    if (login.isFailed()) {
      session.setAttribute(AUTH, null);
      ServiceException se = new ServiceException(LOGIN_SERVICE, login);
      sendErrorMessage(resp, se, HttpURLConnection.HTTP_UNAUTHORIZED, req);
    } else {
      UserBean b = (UserBean) login.getBeanHolder();
      // Now we are logged in, but we need to set a role
      assignRole(role, b);
      session.setAttribute(AUTH, String.valueOf(b.getSession()));
      try {
        fireServerEvent("login.loggedin", null, b.getSession(), req.getRemoteAddr());
      } catch (RuntimeException e) {
        if (!e.getMessage().startsWith("No event file configured")) {
          logOffCurrentSession(req, session);
          ServiceException se = new ServiceException(LOGIN_SERVICE,
              new TransferObject(TransferObject.EXCEPTION, "UNEXPECTED_EXCEPTION"));
          sendErrorMessage(resp, se, HttpURLConnection.HTTP_UNAUTHORIZED, req);
        }
      }
      String path = calculateAfterLoginRedirect(req);
      if (ServletUtil.acceptsJson(req)) {
        resp.getOutputStream().write(new Jsonifier().render(path).getBytes(UTF8));
        resp.setStatus(HttpURLConnection.HTTP_OK);
      } else {
        ServletUtil.redirectTo(resp, path);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, String> getJsonArguments(HttpServletRequest req) throws IOException {
    Object readData = new Jsonifier().read(req.getInputStream());
    if (readData == null || readData instanceof Map) {
      return (Map<String, String>) readData;
    } else {
      throw new IOException("Expecting json dictionary for conversion to java.util.Map, found " + readData.getClass());
    }
  }

  private void assignRole(String role, UserBean b)
      throws ServiceException {
    List<UserRoleBean> roleBeans = b.getRoleBeans();
    if (roleBeans.isEmpty() || !role.equals("*")) {
      boolean isFound = false;
      for (UserRoleBean userRoleBean : roleBeans) {
        if (userRoleBean.getName().equals(role) || role.equals(userRoleBean.getRoleId().toString())) {
          isFound = true;
          getLoginService().setSessionRole(b.getSession(), userRoleBean.getRoleId().longValue());
          break;
        }
      }
      if (!isFound) {
        ServerSession.THREAD_SESSION.set(b.getSession());
        getLoginService().logoff(b.getUserId(), b.getSession());
        String code = "USER_ROLE_NOT_SET";
        if (!roleBeans.isEmpty()) {
          code = "INVALID_USER_ROLE_SELECTION";
        }
        throw new ServiceException(LOGIN_SERVICE, new TransferObject(TransferObject.ERROR, code));
      }
    } else {
      getLoginService().setSessionRole(b.getSession(), roleBeans.get(0).getRoleId().longValue());
    }
  }

  private String calculateAfterLoginRedirect(HttpServletRequest req) {
    String redirectTo = "/";
    String page = ServletUtil.getReferingLocalPage(req, this);
    if (page != null && !page.equals("") && !page.equals("login") && (page.startsWith("document/") || cache.isForm(page))) {
      redirectTo = "/" + ServletUtil.getLocalReferingAfterContext(req, this);
    }
    return getServletContext().getContextPath() + redirectTo;
  }

  private void sendErrorMessage(HttpServletResponse resp, ServiceException e, int status, HttpServletRequest req)
      throws IOException {
    resp.setStatus(status);
    OutputStream out = ServletUtil.wrapGzip(resp.getOutputStream(), req, resp);
    IWebLanguage l = getRequestLanguage(req);
    String message = getErrorMessageBuilder().getMessage(e);
    if (message != null) {
      message = l.getValue(message);
    }
    if (req != null && ServletUtil.acceptsJson(req)) {
      ServletUtil.jsonHeaders(resp);
      Map<String, String> m = new HashMap<String, String>();
      String title = getErrorMessageBuilder().getTitle(e);
      if (title != null) {
        title = l.getValue(title);
      }
      m.put("title", title);
      m.put("message", message);
      new Jsonifier().write(m, out);
    } else {
      ServletUtil.plainTextHeaders(resp);
      out.write(message.getBytes(UTF8));
    }
    out.close();
  }

  private static final Pattern EVENT_NAME = Pattern.compile("\\S+\\.\\S+");

  private void sendEvent(HttpServletRequest req, HttpServletResponse resp,
      String servletPath, Long session, String clientAddress)
      throws IOException, ServiceException {
    // Event urls are in the form /app/event/query.getcustomer?form=formX
    String eventName = servletPath.replace("/event/", "");
    if (!EVENT_NAME.matcher(eventName).matches()) {
      throw new IllegalArgumentException("Invalid event name '" + eventName
          + "' detected, must match " + EVENT_NAME.pattern());
    }
    String sourceForm = req.getParameter("form");
    if (sourceForm == null) {
      // Temp workaround, should have something in the event files that maps argument types
      sourceForm = eventName;
    }
    WebForm f = getForm(sourceForm);
    InputStream inputStream = req.getInputStream();
    Jsonifier j = new Jsonifier();
    Map<String, Object> jsonParams = getEventJsonParameters(eventName,
        j.read(inputStream));
    Object o = fireServerEvent(eventName, f.normalizeTypes(jsonParams),
        session, clientAddress);
    sendJsonIfAccepted(req, resp, o, false);
  }

  private void sendJsonIfAccepted(HttpServletRequest req, HttpServletResponse resp, Object o, boolean overrideAcceptsHeader) throws IOException {
    if (overrideAcceptsHeader || ServletUtil.acceptsJson(req)) {
      ServletUtil.jsonHeaders(resp);
      Jsonifier jsonifier = new Jsonifier();
      jsonifier.write(o, resp.getOutputStream());
      resp.setStatus(HttpURLConnection.HTTP_OK);
    }
  }

  private Object fireServerEvent(String eventName, Map<String, Object> params,
      Long sessionKey, String clientAddress) throws ServiceException {
    IMessageHandler h = (IMessageHandler) LookupManager.getInstance()
        .getLookupItem(LookupManager.SYSTEM, "MessageHandler");
    Map<String, Object> context = new HashMap<String, Object>();
    context.put("session", sessionKey);
    context.put("hostname", clientAddress);
    Object handleMessage = h.handleMessage(null, "EventService", "sendEvent",
        new Class[] {String.class, Map.class}, new Object[] {eventName, params}, context);
    TransferObject to = (TransferObject) handleMessage;
    if (to == null) {
      return null;
    }
    if (to.isFailed()) {
      throw new ServiceException("EventService", to);
    }
    return to.getBeanHolder();
  }

  private ErrorMessageBuilder getErrorMessageBuilder() {
    if (errorMessageBuilder == null) {
      String text;
      try {
        text = FileUtil.readEntireTextFile(new File(errorMessagePath), UTF8.name())
            .toString();
      } catch (IOException e) {
        text = "<messages/>";
      }
      errorMessageBuilder = new ErrorMessageBuilder(text);
    }
    return errorMessageBuilder;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getEventJsonParameters(String eventName,
      Object read) throws ServiceException {
    try {
    if (read == null) {
      return new HashMap<String, Object>();
    } else if (read instanceof List) {
      List<Map<String, Object>> l = (List<Map<String, Object>>) read;
      if (l.isEmpty()) {
        return new HashMap<String, Object>();
      }
      return (Map<String, Object>) l.get(0);
    }
    if (!(read instanceof Map)) {
      throw new IllegalArgumentException("Event " + eventName
          + " requested with invalid parameter type: " + read.getClass());
    }
    return (Map<String, Object>) read;
    } catch (IllegalArgumentException e) {
      throw new ServiceException(e.getMessage(), e);
    }
  }

  private WebForm getForm(String formName) throws IOException {
    try {
      return cache.getForm(formName);
    } catch (SAXException e) {
      throw new IOException(e);
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, String> resolveFormTextValuesForLogin(HttpServletRequest req) {
    Map<String, Object> parameterMap = req.getParameterMap();
    Map<String, String> values = new HashMap<String, String>();
    for (String k : parameterMap.keySet()) {
      if (k.startsWith("pwcf_")) {
        Matcher matcher = Pattern.compile("(pwcf[_1234567890]+__)(.+)")
            .matcher(k);
        if (matcher.matches()) {
          String fieldKey = matcher.group(2);
          String fieldInternalValue = req.getParameter(k);
          if (fieldInternalValue == null || fieldInternalValue.equals("")) {
            String formInputValue = req.getParameter(fieldKey + "__FC");
            if (formInputValue != null && !formInputValue.equals(fieldInternalValue)){
              fieldInternalValue = formInputValue;
            }
          }
          values.put(fieldKey, fieldInternalValue);
        }
      }
    }
    return values;
  }

  @Override
  protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      try {
        if (req.getServletPath().endsWith("/webform.js")) {
          ServletUtil.sendAsJavascriptFromClasspath("webform.js", req, resp);
          return;
        } else if (req.getServletPath().endsWith("/sprintf.js")) {
          ServletUtil.sendAsJavascriptFromClasspath("sprintf.js", req, resp);
          return;
        } else if (req.getServletPath().equals("/style.css")) {
          InputStream in = new FileInputStream(style);
          ServletUtil.sendAsCss(in, req, resp);
          return;
        } else if (req.getServletPath().startsWith("/js/")) {
          String formName = req.getServletPath().replace("/js/", "");
          WebForm form = getForm(formName);
          resp.setContentType(ServletUtil.JS_MIME);
          ServletUtil.nocache(resp);
          OutputStream o = ServletUtil.wrapGzip(resp.getOutputStream(), req, resp);
          form.renderFormJavascriptAsModule(getRequestLanguage(req), o);
          resp.setStatus(HttpURLConnection.HTTP_OK);
          o.close();
          return;
        } else if (req.getServletPath().equals("/lang")) {
          setLanguage(req, resp);
          return;
        } else if (req.getServletPath().equals("/docs")
            || req.getServletPath().startsWith("/docs/")) {
          new DocumentationStreamer().renderDocumentation(req, resp);
          resp.setStatus(HttpURLConnection.HTTP_OK);
          return;
        } else if (isFormJavascriptRequest(req)) {
          String formName = getFormName(req);
          InputStream in = new FileInputStream(cache.getExternalJavascriptForForm(formName));
          ServletUtil.sendAsJavascript(resp, in, req);
          return;
        }
        try {
          Long appSession = getSession(req);
          String servletPath = req.getServletPath();
          if (appSession == null) {
            if (isSubformRequest(req)) {
              resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
              renderLoginPage(resp, req);
              return;
            }
          } else if (servletPath.equals("/logout")) {
            renderLogoutPage(resp, req);
          } else if (servletPath.equals("/")) {
            renderHome(resp, req);
          } else if (req.getServletPath().startsWith("/document/")) {
            handleDocumentDownload(req, resp);
            return;
          } else if (servletPath.lastIndexOf('/') == 0) {
            String formName = getFormName(req);
            Map<String, Object> arguments = getAssociatedData(req, formName);
            renderForm(formName, resp, req, isSubformRequest(req), arguments, appSession);
          } else {
            renderRedirectToLogout(resp);
          }
        } catch (XMLStreamException e) {
          throw new IOException(e);
        }
      } catch (RuntimeException t) {
        if (t.getCause() instanceof ServiceException) {
          throw (ServiceException) t.getCause();
        }
        throw t;
      }
    } catch (ServiceException e) {
      sendErrorMessage(resp, e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, req);
    }
  }

  private boolean isFormJavascriptRequest(HttpServletRequest req) {
    String formName = getFormName(req);
    return cache.isForm(formName) && req.getServletPath().endsWith(".js");
  }

  private String getFormName(HttpServletRequest req) {
    String formName = req.getServletPath().replace("/", "");
    if (formName.endsWith(".js")) {
      return formName.substring(0, formName.length() - 3);
    }
    return formName;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private Map<String, Object> getAssociatedData(HttpServletRequest req, String formName) throws IOException {
    Object associatedData = parseAssociatedData(formName, req);
    if (associatedData instanceof List) {
      if (((List) associatedData).size() > 0) {
        // TODO: Warn
        return (Map<String, Object>) ((List) associatedData).get(0);
      } else {
        return null;
      }
    } else if (!(associatedData instanceof Map)) {
      // TODO: Warn
      return null;
    }
    return (Map<String, Object>) associatedData;
  }

  protected void setLanguage(HttpServletRequest req, HttpServletResponse resp) {
    String language = req.getQueryString();
    IWebLanguage selectedLanguage = lang.getLanguage(language);
    if (selectedLanguage == null) {
      ServletUtil.removeCookie(LANG_COOKIE, resp);
    } else {
      Cookie c = new Cookie(LANG_COOKIE, language);
      c.setMaxAge(LANG_COOKIE_EXPIRY);
      resp.addCookie(c);
    }
    ServletUtil.htmlHeader(resp);
    ServletUtil.redirectTo(resp, "home");
  }

  private void handleDocumentDownload(HttpServletRequest req, HttpServletResponse resp) throws ServiceException,
      IOException {
    String id = req.getServletPath().substring(req.getServletPath().lastIndexOf("/") + 1);
    String webForm = req.getParameter("WEB_FORM");
    String mimeType = req.getParameter("mime");
    String name = req.getParameter("name");
    if (mimeType == null) {
      mimeType = req.getHeader("Accept");
    }
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("MIME", mimeType);
    params.put("NAME", name);
    params.put("WEB_FORM", webForm);
    Long docId = Long.valueOf(id);
    params.put("DOCUMENT_ID", docId);
    Object eventResult = fireServerEvent(DOC_DOWNLOAD_EVENT, params, getSession(req), req.getRemoteAddr());
    Map<String, Object> docData = getMapFromEventResult(eventResult);
    // I expect a Map with some important keys set in it:
    try {
      InputStream s = (InputStream) docData.get("STREAM");
      String mime = (String) docData.get("MIME");
      if (mime != null) {
        ServletUtil.contentType(mime, resp);
      }
      boolean isInline = (Boolean) docData.get("INLINE");
      String fileName = (String) docData.get("NAME");
      if (isInline) {
        ServletUtil.inline(fileName, resp);
      } else {
        ServletUtil.attachment(fileName, resp);
      }
      resp.setStatus(HttpURLConnection.HTTP_OK);
      TransferWithBuffer twb = new TransferWithBuffer();
      twb.transfer(s, resp.getOutputStream(), new byte[ServletUtil.BUFFER_SIZE]);
      resp.getOutputStream().close();
    } catch (ClassCastException e) {
      String badType = "null";
      if (eventResult != null) {
        badType = eventResult.getClass().getName();
      }
      ServiceException se = new ServiceException(DOC_DOWNLOAD_EVENT + " event must return document stream data, not "
          + badType, e);
      sendErrorMessage(resp, se, HttpURLConnection.HTTP_INTERNAL_ERROR, req);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Map<String, Object> getMapFromEventResult(Object eventResult) {
    if (eventResult instanceof List && ((List) eventResult).size() > 0) {
      return (Map<String, Object>) ((List) eventResult).get(0);
    } else if (eventResult instanceof Map) {
    return (Map<String, Object>) eventResult;
    } else {
      return null;
    }
  }

  @SuppressWarnings("rawtypes")
  private Map<String, Object> parseAssociatedData(String formName, HttpServletRequest req) throws IOException {
    String parameter = req.getParameter("data");
    if (parameter == null || parameter.length() == 0) {
      return null;
    } else {
      Object associatedData = new Jsonifier().parse(parameter);
      if (associatedData instanceof List) {
        if (((List) associatedData).size() > 0) {
          // TODO: Warn
          associatedData = ((List) associatedData).get(0);
        } else {
          associatedData = null;
        }
      } else if (!(associatedData instanceof Map)) {
        // TODO: Warn
        associatedData = null;
      }
      @SuppressWarnings("unchecked")
      Map<String, Object> arguments = (Map<String, Object>) associatedData;
      WebForm form = getForm(formName);
      return form.normalizeTypes(arguments);
    }
  }

  private boolean isSubformRequest(HttpServletRequest req) {
    String parameter = req.getParameter("subform");
    return parameter != null && parameter.equals("true");
  }

  private void renderForm(String formName, HttpServletResponse resp, HttpServletRequest req,
      boolean isSubform, Map<String, Object> data, Long appSession) throws IOException, ServiceException {
      WebForm form = null;
    try {
     form = getForm(formName);
    } catch (IOException e) {
      OutputStream outputStream = ServletUtil.wrapGzip(resp.getOutputStream(), req, resp);
      ServletUtil.htmlHeader(resp);
      outputStream.write(("Unable to read configuration for form: " + formName + "<pre>\n").getBytes());
      e.printStackTrace(new PrintStream(outputStream));
      outputStream.close();
      resp.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
      return;
    }
    OutputStream outputStream = null;
    try {
      Map<String, WebFieldValue> params = form.asWebValues(data);
      DefaultRenderingContext context = buildContext(params, appSession, req.getRemoteAddr(), req);
      outputStream = ServletUtil.wrapGzip(resp.getOutputStream(), req, resp);
      ServletUtil.htmlHeader(resp);
      ServletUtil.nocache(resp);
      XMLOutputFactory fa = XMLOutputFactory.newInstance();
      XMLStreamWriter w = fa.createXMLStreamWriter(outputStream, "UTF-8");
      renderFormXML(formName, isSubform, context, w, req, resp);
      resp.setStatus(HttpURLConnection.HTTP_OK);
      return;
    } catch (FactoryConfigurationError e) {
      // Just creating the factory should not cause us any trouble, we just
      // rethrow since it is unlikely
      throw new IOException(e);
    } catch (Exception e) {
      resp.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
      renderHtmlUnexpectedException(e, outputStream);
    } finally {
      if (outputStream != null) {
        outputStream.close();
      }
    }
  }
  private void renderHtmlUnexpectedException(Exception e, OutputStream outputStream) throws UnsupportedEncodingException, IOException {
    if (outputStream != null) {
      outputStream.write(">>>>>>>><div id='fatal'>".getBytes(UTF8));
      ServiceException se = null;
      if (e instanceof ServiceException) {
        se = (ServiceException) e;
      } else if (e.getCause() instanceof ServiceException) {
        se = (ServiceException) e.getCause();
      }
      if (se != null) {
        outputStream.write(("<h1>" + getErrorMessageBuilder().getMessage(se) + "</h1>").getBytes(UTF8));
      }
      e.printStackTrace(new PrintStream(outputStream));
      outputStream.write("</div>".getBytes(UTF8));
    }
  }

  private void renderFormXML(String formName, boolean isSubform,
      IRenderingContext c, XMLStreamWriter w, HttpServletRequest req, HttpServletResponse resp)
  throws IOException, XMLStreamException, ServiceException {
    WebForm f = getForm(formName);
    Map<String, Object> result = fireFormServerEvent(formName, f, c);
    Map<String, WebFieldValue> fieldValues = f.asWebValues(result);
    final long renderStartTime = System.currentTimeMillis();
    boolean hasAssociatedJavascript = cache.getExternalJavascriptForForm(formName).exists();
    f.render(c.getSubcontext(formName, fieldValues), !isSubform, hasAssociatedJavascript, w);
    final long renderEndTime = System.currentTimeMillis();
    getLog().emit(WebClientLog.RENDER_TIME, (renderEndTime - renderStartTime), formName);
  }

  private Map<String, Object> fireFormServerEvent(String formName, WebForm f,
      IRenderingContext c)
      throws ServiceException {
    Map<String, Object> result = null;
    String assignedEvent = f.getAssignedEvent();
    if (assignedEvent == null) {
      result = f.asJavaValues(c.getFieldValues());
    } else {
      Map<String, Object> params = f.asJavaValues(c.getFieldValues());
      params.put("WEB_FORM", formName);
      Object o = fireServerEvent(assignedEvent, params, c.getAuthenticatedSessionId(), c.getClientAddress());
      result = getMapFromEventResult(o);
    }
    return result;
  }

  private DefaultRenderingContext buildContext(
      Map<String, WebFieldValue> parentValues, final Long appSession,
      final String clientAddress, final HttpServletRequest req) {
    IWebLanguage l = getRequestLanguage(req);
    IFormRenderer renderer = new IFormRenderer() {
      @Override
      public void render(String formName, XMLStreamWriter w, IRenderingContext parentContext) throws IOException {
        try {
          renderFormXML(formName, true, parentContext, w, null, null);
        } catch (XMLStreamException e) {
          throw new IOException(e);
        } catch (ServiceException e) {
          throw new IOException(e);
        }
      }
    };
    return new DefaultRenderingContext(formCounter.incrementAndGet(),
        parentValues, renderer, appSession, clientAddress, l) {
      @Override
      public IWebLanguage[] getAvailableLanguages() {
        return getFullLanguageList(req);
      }

      @Override
      public Map<String, WebFieldValue> marshallAsFormParameters(String formName, Map<String, Object> data)
       throws IOException {
        WebForm form = getForm(formName);
        return form.asWebValues(data);
      }
    };
  }

  private void renderHome(HttpServletResponse resp, HttpServletRequest req)
      throws IOException, ServiceException {
    renderForm("home", resp, req, false, null, getSession(req));
  }

  private void renderRedirectToLogout(HttpServletResponse resp) {
    ServletUtil.redirectTo(resp, getServletContext().getContextPath() + "/logout");
  }

  private Long getSession(HttpServletRequest req) {
    Long unvalidatedSessionKey = getUnvalidatedSessionKey(req);
    if (unvalidatedSessionKey == null) {
      return null;
    } else if (getLoginService().getSessionUser(unvalidatedSessionKey) == null) {
      return null;
    } else {
      return unvalidatedSessionKey;
    }
  }
  private Long getUnvalidatedSessionKey(HttpServletRequest req) {
    HttpSession session = req.getSession(false);
    if (session == null) {
      String authParameter = req.getParameter(AUTH);
      if (authParameter != null) {
        return Long.valueOf(authParameter);
      }
    } else {
      String sessionId = (String) session.getAttribute(AUTH);
      if (sessionId != null) {
        return Long.valueOf(sessionId);
      }
    }
    return null;
  }


  private String getUser(HttpSession session) {
    if (session == null) {
      return null;
    }
    String sessionId = (String) session.getAttribute(AUTH);
    return getLoginService().getSessionUser(Long.valueOf(sessionId));
  }

  private void renderLoginPage(HttpServletResponse resp, HttpServletRequest req)
      throws IOException, XMLStreamException, ServiceException {
    renderForm("login", resp, req, false, null, getSession(req));
  }

  private void renderLogoutPage(HttpServletResponse resp, HttpServletRequest req)
      throws IOException, XMLStreamException, ServiceException {
    renderForm("logout", resp, req, false, null, getSession(req));
  }

  private MapLoginService getLoginService() {
    Object d = LookupManager.getInstance().getLookupItem(
        LookupManager.BUSINESS, LOGIN_SERVICE);
    return (MapLoginService) d;
  }

  @SuppressWarnings("unchecked")
  private IWebLanguage getRequestLanguage(HttpServletRequest req) {
    String v = ServletUtil.findCookieValue(LANG_COOKIE, req);
    IWebLanguage l = lang.getLanguage(v);
    if (l == null) {
      l = lang.getFirstAvailable(req.getLocales());
    }
    if (l == null) {
      l = lang.getDefaultLanguage();
    }
    return l;
  }

  @SuppressWarnings("unchecked")
  private IWebLanguage[] getFullLanguageList(HttpServletRequest req) {
    return lang.getFullLanguageList(getRequestLanguage(req), req.getLocales());
  }

  @Override
  protected void doTrace(HttpServletRequest req, HttpServletResponse resp) 
  throws ServletException, IOException {
    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
    resp.getOutputStream().close();
  }
}
