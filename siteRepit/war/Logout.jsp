<%@ page import="ch.repit.rwt.server.audit.*" %>
<%@ page import="ch.repit.rwt.server.persistence.PMF" %>
<%@ page import="ch.repit.rwt.client.security.*" %>
<%@ page import="ch.repit.rwt.client.audit.*" %>
<%@ page import="com.google.appengine.api.users.*" %>
<%@ page import="javax.jdo.PersistenceManager" %>

<%
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    if (user != null) {

        Authorizer authorizer = (Authorizer)request.getSession().getAttribute(
            ch.repit.rwt.server.RwtRemoteServiceServlet.PRINCIPAL_SESSION_KEY);

        if (authorizer != null &&
            authorizer.getPrincipal() != null &&
            authorizer.getPrincipal().getUserRef() != null)
        {

            PersistenceManager pm = PMF.get().getPersistenceManager();
            try {
                AuditLog auditLog = new AuditLog(AuditLogDTO.AuditableAction.LOGOUT, authorizer.getPrincipal().getUserRef());
                AuditManager.get().writeAuditLog(pm, auditLog);
            } finally {
                pm.close();
            }
        }
    }

    response.sendRedirect(response.encodeRedirectURL((UserServiceFactory.getUserService().createLogoutURL("/Repit.jsp"))));
%>