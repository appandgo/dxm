<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:url var="actionUrl" value="${url.base}${renderContext.mainResource.node.path}.html"/>
<form method="post" action="${actionUrl}">
    <c:forEach items="${param}" var="par">
        <c:if test="${fn:startsWith(par.key, 'src_')}">
            <c:forEach items="${par.value}" var="value">
                <input type="hidden" name="${par.key}" value="${value}"/>
            </c:forEach>
        </c:if>
    </c:forEach>
    <input type="hidden" name="jcrMethodToCall" value="get"/>
    <jsp:doBody/>
</form>