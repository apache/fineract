<%@ page import="org.springframework.security.core.AuthenticationException" %>
<%@ page import="org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter" %>
<%@ taglib prefix="authz" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
  <title>MifosNg Platform: Access Confirmation</title>
</head>

<body>

  <h1>MifosNg</h1>

  <div id="content">

    <c:if test="${!empty sessionScope.SPRING_SECURITY_LAST_EXCEPTION}">
      <div class="error">
        <h2>Woops!</h2>

        <p>Access could not be granted. (<%= ((AuthenticationException) session.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY)).getMessage() %>)</p>
      </div>
    </c:if>
    <c:remove scope="session" var="SPRING_SECURITY_LAST_EXCEPTION"/>

      <h2>Please Confirm</h2>

      <p>You hereby authorize "<c:out value="${consumer.consumerName}"/>" to access the following resource:</p>

      <ul>
          <li><c:out value="${consumer.resourceName}"/> &mdash; <c:out value="${consumer.resourceDescription}"/></li>
      </ul>

      <form action="<c:url value="/oauth/authorize"/>" method="POST">
        <input name="requestToken" value="<c:out value="${oauth_token}"/>" type="hidden"/>
        <label><input name="authorize" value="Authorize" type="submit"></label>
      </form>
  </div>
</body>
</html>