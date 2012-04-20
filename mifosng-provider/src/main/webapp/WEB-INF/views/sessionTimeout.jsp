<%@ page import="org.springframework.security.core.AuthenticationException" %>
<%@ page import="org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<sec:authorize ifAllGranted="ROLE_USER">
  <c:redirect url="index.jsp"/>
</sec:authorize>

<!DOCTYPE>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
  <title>Sparklr</title>
</head>

<body>

  <h1>Sparklr</h1>

  <div id="content">
    <c:if test="${!empty sessionScope.SPRING_SECURITY_LAST_EXCEPTION}">
      <div class="error">
      <h2>Woops!</h2>

       <p>Your login attempt was not successful. (<%= ((AuthenticationException) session.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY)).getMessage() %>)</p>
      </div>
    </c:if>
    <c:remove scope="session" var="SPRING_SECURITY_LAST_EXCEPTION"/>

    <sec:authorize ifNotGranted="ROLE_USER">
      <h2>Login</h2>

      <p>We've got a grand total of 2 users: marissa and paul. Go ahead and log in. Marissa's password is "koala" and Paul's password is "emu".</p>
      <form action="j_spring_security_check" method="POST">
        <p><label>Username: <input type='text' name='j_username' value="marissa"></label></p>
        <p><label>Password: <input type='text' name='j_password' value="koala"></label></p>
        
        <p><input name="login" value="Login" type="submit"></p>
      </form>
    </sec:authorize>
  </div>

</body>
</html>