<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <meta http-equiv="Content-Language" content="fr-ch">
    <meta http-equiv="Content-Author" content="Thomas Caprez">
    <meta name='gwt:property' content='locale=fr_CH'>

    <title>repit.ch - Société Coopérative le Répit</title>
    <link rel="stylesheet" type="text/css" href="/repit/rwt.css">
    <link rel="stylesheet" type="text/css" href="/repit/repit.css">
    <script type="text/javascript" src="/repit/scripts/grey.js">
      <!-- script included -->
    </script>
  </head>

  <body class="rwtBody">
<%
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    if (user != null) {
%>

    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>
    <script type="text/javascript" language="javascript" src="repit/repit.nocache.js"></script>
<%
    } else {
%>
    <br/><br/><br/><br/><br/><br/><br/><br/>
    <center>
      <table width='300px'>
        <tr><td class='rwtLoading-frame' colspan='3'>
          <img src="/repit/images/repit_logo3.png"/>
          <br/>
          <a href="<%= userService.createLoginURL(request.getRequestURI() + 
             ( (request.getQueryString()==null)?"?locale=fr":("?" + request.getQueryString()))) %>">Connexion</a>
        </td></tr>
<!--
        <tr>
          <td>about us...</td>
          <td>employement</td>
          <td>sources</td>
        </tr>
-->
      </table>
    </center>
<%
    }
%>

  </body>
</html>
