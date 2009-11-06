<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:remove var="currentList" scope="request"/>
<template:module node="${currentNode}" forcedTemplate="hidden.load" editable="false" />
<c:if test="${empty editable}">
    <c:set var="editable" value="false"/>
</c:if>

<c:forEach items="${currentList}" var="subchild">
    <p>
        <template:module node="${subchild}" template="${subNodesTemplate}" editable="${editable}" >
            <c:if test="${not empty forcedSkin}">
                <template:param name="forcedSkin" value="${forcedSkin}"/>
            </c:if>
            <c:if test="${not empty renderOptions}">
                <template:param name="renderOptions" value="${renderOptions}"/>
            </c:if>
        </template:module>
    </p>
</c:forEach>
