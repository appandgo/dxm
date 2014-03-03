<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="function" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<%@ include file="../../getUser.jspf"%>

<template:addResources type="css" resources="admin-bootstrap.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>

<template:addCacheDependency node="${user}"/>

<jsp:useBean id="now" class="java.util.Date"/>

<jcr:nodeProperty node="${user}" name="j:publicProperties" var="publicProperties" />
<c:set var="publicPropertiesAsString" value=""/>
<c:forEach items="${publicProperties}" var="value">
    <c:set var="publicPropertiesAsString" value="${value.string} ${publicPropertiesAsString}"/>
</c:forEach>

<jcr:nodeProperty node="${user}" name="j:birthDate" var="birthDate"/>
<jcr:propertyInitializers node="${user}" name="j:gender" var="genderInit"/>
<jcr:propertyInitializers node="${user}" name="j:title" var="titleInit"/>

<template:addResources>
    <script type="text/javascript">
        $(document).ready(function(){

            $(".btnMoreAbout").click(function(){
                $(".aboutMeText").css( { height:"100%",maxHeight: "500px", overflow: "auto", paddingRight: "5px" }, { queue:false, duration:500 });
                $(".btnMoreAbout").hide();
                $(".btnLessAbout").show();
            });

            $(".btnLessAbout").click(function(){
                $(".aboutMeText").css( { height:"100px", overflow: "hidden" }, { queue:false, duration:500 });
                $(".btnLessAbout").hide();
                $(".btnMoreAbout").show();
            });

            $('#tabView a').click(function (e) {
                e.preventDefault();
                $(this).tab('show');
            })
        });
    </script>
</template:addResources>

<ul class="nav nav-tabs" id="tabView">
    <li class="active"><a href="#private">Private view</a></li>
    <li><a href="#public">Public view</a></li>
</ul>

<div class="tab-content">
    <div class="tab-pane active" id="private">
        <div class="alert alert-info">
            <div class="row-fluid">
                <jcr:nodeProperty var="picture" node="${user}" name="j:picture"/>
                <div class="span2">
                    <c:choose>
                        <c:when test="${empty picture}">
                            <img class="img-polaroid pull-left" src="<c:url value='${url.currentModule}/img/userbig.png'/>"
                                 alt="" border="0"/>
                        </c:when>
                        <c:otherwise>
                            <img class="img-polaroid pull-left" src="${picture.node.thumbnailUrls['avatar_120']}"
                                 alt="${fn:escapeXml(person)}"/>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="span10">
                    <h1>
                        <fmt:message key='jnt_user.j_about'/>
                    </h1>
                    <div class="aboutMeText lead" style="height: 100px; text-align: justify; overflow: hidden">
                        ${user.properties['j:about'].string}
                    </div>
                    <br />
                    <button class="btn btn-small btn-primary btnMoreAbout">
                        <fmt:message key='mySettings.readMore'/>
                    </button>
                    <button class="btn btn-small btn-primary hide btnLessAbout">
                        <fmt:message key='mySettings.readLess'/>
                    </button>
                </div>
            </div>
        </div>
        <div class="row-fluid">
            <div class="span2"></div>
            <div class="span8">
                <h2><i class="icon-user"></i>&nbsp;<fmt:message key='mySettings.name'/></h2>
                <div class="box-1">
                    <c:if test="${(not empty user.properties['j:title'].string)}">
                        <jcr:nodePropertyRenderer node="${user}" name="j:title" renderer="resourceBundle"/>&nbsp;
                    </c:if>
                    <c:if test="${(not empty user.properties['j:firstName'].string)}">
                        ${user.properties['j:firstName'].string}&nbsp;
                    </c:if>
                    <c:if test="${(not empty user.properties['j:lastName'].string)}">
                        ${user.properties['j:lastName'].string}&nbsp;
                    </c:if>
                </div>
                <h2><i class="icon-briefcase"></i>&nbsp;<fmt:message key='mySettings.profession'/></h2>
                <div class="box-1">
                    <c:if test="${(not empty user.properties['j:function'].string)}">
                        ${user.properties['j:function'].string}
                    </c:if>
                    &nbsp;<fmt:message key='mySettings.at'/>&nbsp;
                    <c:if test="${(not empty user.properties['j:organization'].string)}">
                        ${user.properties['j:organization'].string}
                    </c:if>
                </div>
                <h2><i class="icon-globe"></i>&nbsp;<fmt:message key='mySettings.social'/></h2>
                <div class="box-1">
                    <c:choose>
                        <c:when test="${(not empty user.properties['j:facebookID'].string)}">
                            <img src="<c:url value='${url.currentModule}/img/fb_logo_20_20.png' />"/>&nbsp;&nbsp;
                        </c:when>
                        <c:otherwise>
                            <img src="<c:url value='${url.currentModule}/img/fb_logo_off_20_20.png'/>"/>&nbsp;&nbsp;
                        </c:otherwise>
                    </c:choose>
                    <c:choose>
                        <c:when test="${(not empty user.properties['j:skypeID'].string)}">
                            <img src="<c:url value='${url.currentModule}/img/skype_logo_20_20.png' />"/>&nbsp;&nbsp;
                        </c:when>
                        <c:otherwise>
                            <img src="<c:url value='${url.currentModule}/img/skype_logo_off_20_20.png' />"/>&nbsp;&nbsp;
                        </c:otherwise>
                    </c:choose>
                    <c:choose>
                        <c:when test="${(not empty user.properties['j:twitterID'].string)}">
                            <img src="<c:url value='${url.currentModule}/img/twitter_logo_20_20.png' />"/>&nbsp;&nbsp;
                        </c:when>
                        <c:otherwise>
                            <img src="<c:url value='${url.currentModule}/img/twitter_logo_off_20_20.png' />"/>&nbsp;&nbsp;
                        </c:otherwise>
                    </c:choose>
                    <c:choose>
                        <c:when test="${(not empty user.properties['j:linkedinID'].string)}">
                            <img src="<c:url value='${url.currentModule}/img/in_logo_20_20.png' />"/>&nbsp;&nbsp;&nbsp;
                        </c:when>
                        <c:otherwise>
                            <img src="<c:url value='${url.currentModule}/img/in_logo_off_20_20.png' />"/>&nbsp;&nbsp;&nbsp;
                        </c:otherwise>
                    </c:choose>
                </div>
                <h2><i class="icon-envelope"></i>&nbsp;<fmt:message key='mySettings.address'/></h2>
                <div class="box-1">
                    <div class="row-fluid">
                        <div class="pull-left">
                            <div>
                                <strong><fmt:message key='jnt_user.j_email'/>&nbsp;:</strong>
                                <c:if test="${(not empty user.properties['j:email'].string)}">
                                    &nbsp;${user.properties['j:email'].string}
                                </c:if>
                            </div>
                            <div>
                                <strong><fmt:message key='jnt_user.j_phoneNumber'/>&nbsp;:</strong>
                                <c:if test="${(not empty user.properties['j:phoneNumber'].string)}">
                                    &nbsp;${user.properties['j:phoneNumber'].string}
                                </c:if>
                            </div>
                            <div>
                                <strong><fmt:message key='jnt_user.j_mobileNumber'/>&nbsp;:</strong>
                                <c:if test="${(not empty user.properties['j:mobileNumber'].string)}">
                                    &nbsp;${user.properties['j:mobileNumber'].string}
                                </c:if>
                            </div>
                        </div>
                        <div class="pull-right">
                            <div class="pull-left">
                                <strong><fmt:message key='jnt_user.j_address'/>&nbsp;:</strong>&nbsp;
                            </div>
                            <div class="pull-right">
                                <c:if test="${(not empty user.properties['j:address'].string)}">
                                    ${user.properties['j:address'].string}
                                </c:if>
                                <br />
                                <c:if test="${(not empty user.properties['j:zipCode'].string)}">
                                    ${user.properties['j:zipCode'].string}
                                </c:if>
                                <br />
                                <c:if test="${(not empty user.properties['j:city'].string)}">
                                    ${user.properties['j:city'].string}
                                </c:if>
                                <br />
                                <c:if test="${(not empty user.properties['j:country'].string)}">
                                    ${user.properties['j:country'].string}
                                </c:if>
                            </div>
                        </div>
                    </div>
                </div>
                <h2><i class="icon-info-sign"></i>&nbsp;<fmt:message key="jnt_user.j_gender.other"/></h2>
                <div class="box-1">
                    <jcr:nodeProperty node="${user}" name="j:birthDate" var="birthDate"/>
                    <div>
                        <strong><fmt:message key="jnt_user.age"/>&nbsp;:</strong>
                        <utility:dateDiff startDate="${birthDate.date.time}" endDate="${now}" format="years"/>&nbsp;<fmt:message key="jnt_user.profile.years"/>
                    </div>
                    <div>
                        <c:if test="${(not empty birthDate)}">
                            <fmt:formatDate value="${birthDate.date.time}" pattern="dd, MMMM yyyy" var="displayBirthDate"/>
                        </c:if>
                        <strong><fmt:message key="jnt_user.j_birthDate"/>&nbsp;:</strong>
                        &nbsp;${displayBirthDate}
                    </div>
                    <div>
                        <jcr:nodeProperty node="${user}" name="preferredLanguage" var="prefLang"/>
                        <c:set var="prefLang" value="${functions:toLocale(functions:default(prefLang.string, 'en'))}"/>
                        <strong><fmt:message key="jnt_user.preferredLanguage"/>&nbsp;:</strong>
                        &nbsp;${functions:displayLocaleNameWith(prefLang, prefLang)}
                    </div>
                </div>
            </div>
            <div class="span2"></div>
        </div>
    </div>
    <div class="tab-pane" id="public">
        <c:if test="${fn:contains(publicPropertiesAsString, 'j:picture') and fn:contains(publicPropertiesAsString, 'j:about')}">
            <div class="alert alert-info">
                <div class="row-fluid">
                    <jcr:nodeProperty var="picture" node="${user}" name="j:picture"/>
                    <div class="span2">
                        <c:choose>
                            <c:when test="${empty picture}">
                                <img class="img-polaroid pull-left" src="<c:url value='${url.currentModule}/img/userbig.png'/>"
                                     alt="" border="0"/>
                            </c:when>
                            <c:otherwise>
                                <img class="img-polaroid pull-left" src="${picture.node.thumbnailUrls['avatar_120']}"
                                     alt="${fn:escapeXml(person)}"/>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="span10">
                        <h1>
                            <fmt:message key='jnt_user.j_about'/>
                        </h1>
                        <div class="aboutMeText lead" style="height: 100px; text-align: justify; overflow: hidden">
                                ${user.properties['j:about'].string}
                        </div>
                        <br />
                        <button class="btn btn-small btn-primary btnMoreAbout">
                            <fmt:message key='mySettings.readMore'/>
                        </button>
                        <button class="btn btn-small btn-primary hide btnLessAbout">
                            <fmt:message key='mySettings.readLess'/>
                        </button>
                    </div>
                </div>
            </div>
        </c:if>
        <div class="row-fluid">
            <div class="span2">
                <c:if test="${fn:contains(publicPropertiesAsString, 'j:picture') and (not fn:contains(publicPropertiesAsString, 'j:about'))}">
                    <div class="alert alert-info" style="height: 74px">
                        <jcr:nodeProperty var="picture" node="${user}" name="j:picture"/>
                        <c:choose>
                            <c:when test="${empty picture}">
                                <img class="img-polaroid pull-left" src="<c:url value='${url.currentModule}/img/userbig.png'/>"
                                     alt="" border="0"/>
                            </c:when>
                            <c:otherwise>
                                <img class="img-polaroid pull-left" src="${picture.node.thumbnailUrls['avatar_120']}"
                                     alt="${fn:escapeXml(person)}"/>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:if>
            </div>
            <div class="span8">
                <c:if test="${(not fn:contains(publicPropertiesAsString, 'j:picture')) and fn:contains(publicPropertiesAsString, 'j:about')}">
                    <div class="alert alert-info">
                        <h1>
                            <fmt:message key='jnt_user.j_about'/>
                        </h1>
                        <div class="aboutMeText lead" style="height: 100px; text-align: justify; overflow: hidden">
                                ${user.properties['j:about'].string}
                        </div>
                        <br />
                        <button class="btn btn-small btn-primary btnMoreAbout">
                            <fmt:message key='mySettings.readMore'/>
                        </button>
                        <button class="btn btn-small btn-primary hide btnLessAbout">
                            <fmt:message key='mySettings.readLess'/>
                        </button>
                    </div>
                </c:if>
                <c:if test="${fn:contains(publicPropertiesAsString, 'j:title') or fn:contains(publicPropertiesAsString, 'j:firstName')
                                                        or fn:contains(publicPropertiesAsString, 'j:lastName') or fn:contains(publicPropertiesAsString, 'j:gender')}">
                    <h2><i class="icon-user"></i>&nbsp;<fmt:message key='mySettings.name'/></h2>
                    <div class="box-1">
                        <c:if test="${fn:contains(publicPropertiesAsString, 'j:title')}">
                            <c:if test="${(not empty user.properties['j:title'].string)}">
                                <jcr:nodePropertyRenderer node="${user}" name="j:title" renderer="resourceBundle"/>&nbsp;
                            </c:if>
                        </c:if>
                        <c:if test="${fn:contains(publicPropertiesAsString, 'j:firstName')}">
                            <c:if test="${(not empty user.properties['j:firstName'].string)}">
                                ${user.properties['j:firstName'].string}&nbsp;
                            </c:if>
                        </c:if>
                        <c:if test="${fn:contains(publicPropertiesAsString, 'j:lastName')}">
                            <c:if test="${(not empty user.properties['j:lastName'].string)}">
                                ${user.properties['j:lastName'].string}&nbsp;
                            </c:if>
                        </c:if>
                        <c:if test="${fn:contains(publicPropertiesAsString, 'j:gender')}">
                            <c:choose>
                                <c:when test="${fn:contains(publicPropertiesAsString, 'j:title') or fn:contains(publicPropertiesAsString, 'j:firstName')
                                                            or fn:contains(publicPropertiesAsString, 'j:lastName') and fn:contains(publicPropertiesAsString, 'j:gender')}">
                                    <div class="pull-right">
                                        <strong><fmt:message key='mySettings.gender'/>&nbsp;:</strong>
                                        <c:if test="${(not empty user.properties['j:gender'].string)}">
                                            &nbsp;${user.properties['j:gender'].string}
                                        </c:if>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <strong><fmt:message key='mySettings.gender'/>&nbsp;:</strong>
                                    <c:if test="${(not empty user.properties['j:gender'].string)}">
                                        &nbsp;${user.properties['j:gender'].string}
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                    </div>
                </c:if>
                <c:if test="${fn:contains(publicPropertiesAsString, 'j:function') or fn:contains(publicPropertiesAsString, 'j:organization')}">
                    <h2><i class="icon-briefcase"></i>&nbsp;<fmt:message key='mySettings.profession'/></h2>
                    <div class="box-1">
                        <c:if test="${fn:contains(publicPropertiesAsString, 'j:function')}">
                            <c:if test="${(not empty user.properties['j:function'].string)}">
                                ${user.properties['j:function'].string}
                            </c:if>
                        </c:if>
                        <c:if test="${fn:contains(publicPropertiesAsString, 'j:organization')}">
                            &nbsp;<fmt:message key='mySettings.at'/>&nbsp;
                            <c:if test="${(not empty user.properties['j:organization'].string)}">
                                ${user.properties['j:organization'].string}
                            </c:if>
                        </c:if>
                    </div>
                </c:if>
                <c:if test="${fn:contains(publicPropertiesAsString, 'j:facebookID') or fn:contains(publicPropertiesAsString, 'j:skypeID') or fn:contains(publicPropertiesAsString, 'j:twitterID') or fn:contains(publicPropertiesAsString, 'j:linkedinID')}">
                    <h2><i class="icon-globe"></i>&nbsp;<fmt:message key='mySettings.social'/></h2>
                    <div class="box-1">
                        <c:if test="${fn:contains(publicPropertiesAsString, 'j:facebookID')}">
                            <c:choose>
                                <c:when test="${(not empty user.properties['j:facebookID'].string)}">
                                    <img src="<c:url value='${url.currentModule}/img/fb_logo_20_20.png' />"/>&nbsp;&nbsp;
                                </c:when>
                                <c:otherwise>
                                    <img src="<c:url value='${url.currentModule}/img/fb_logo_off_20_20.png'/>"/>&nbsp;&nbsp;
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        <c:if test="${fn:contains(publicPropertiesAsString, 'j:skypeID')}">
                            <c:choose>
                                <c:when test="${(not empty user.properties['j:skypeID'].string)}">
                                    <img src="<c:url value='${url.currentModule}/img/skype_logo_20_20.png' />"/>&nbsp;&nbsp;
                                </c:when>
                                <c:otherwise>
                                    <img src="<c:url value='${url.currentModule}/img/skype_logo_off_20_20.png' />"/>&nbsp;&nbsp;
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        <c:if test="${fn:contains(publicPropertiesAsString, 'j:twitterID')}">
                            <c:choose>
                                <c:when test="${(not empty user.properties['j:twitterID'].string)}">
                                    <img src="<c:url value='${url.currentModule}/img/twitter_logo_20_20.png' />"/>&nbsp;&nbsp;
                                </c:when>
                                <c:otherwise>
                                    <img src="<c:url value='${url.currentModule}/img/twitter_logo_off_20_20.png' />"/>&nbsp;&nbsp;
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        <c:if test="${fn:contains(publicPropertiesAsString, 'j:linkedinID')}">
                            <c:choose>
                                <c:when test="${(not empty user.properties['j:linkedinID'].string)}">
                                    <img src="<c:url value='${url.currentModule}/img/in_logo_20_20.png' />"/>&nbsp;&nbsp;&nbsp;
                                </c:when>
                                <c:otherwise>
                                    <img src="<c:url value='${url.currentModule}/img/in_logo_off_20_20.png' />"/>&nbsp;&nbsp;&nbsp;
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                    </div>
                </c:if>
                <c:if test="${fn:contains(publicPropertiesAsString, 'j:phoneNumber') or fn:contains(publicPropertiesAsString, 'j:mobileNumber')
                                                        or fn:contains(publicPropertiesAsString, 'j:email') or fn:contains(publicPropertiesAsString, 'j:address')
                                                        or fn:contains(publicPropertiesAsString, 'j:zipCode') or fn:contains(publicPropertiesAsString, 'j:city')
                                                        or fn:contains(publicPropertiesAsString, 'j:country')}">
                    <h2><i class="icon-envelope"></i>&nbsp;<fmt:message key='mySettings.address'/></h2>
                    <div class="box-1">
                        <div class="row-fluid">
                            <div class="pull-left">
                                <c:if test="${fn:contains(publicPropertiesAsString, 'j:email')}">
                                    <div>
                                        <strong><fmt:message key='jnt_user.j_email'/>&nbsp;:</strong>
                                        <c:if test="${(not empty user.properties['j:email'].string)}">
                                            <c:if test="${(not empty user.properties['j:email'].string)}">
                                                &nbsp;${user.properties['j:email'].string}
                                            </c:if>
                                        </c:if>
                                    </div>
                                </c:if>
                                <c:if test="${fn:contains(publicPropertiesAsString, 'j:phoneNumber')}">
                                    <div>
                                        <strong><fmt:message key='jnt_user.j_phoneNumber'/>&nbsp;:</strong>
                                        <c:if test="${(not empty user.properties['j:phoneNumber'].string)}">
                                            <c:if test="${(not empty user.properties['j:phoneNumber'].string)}">
                                                &nbsp;${user.properties['j:phoneNumber'].string}
                                            </c:if>
                                        </c:if>
                                    </div>
                                </c:if>
                                <c:if test="${fn:contains(publicPropertiesAsString, 'j:mobileNumber')}">
                                    <div>
                                        <strong><fmt:message key='jnt_user.j_mobileNumber'/>&nbsp;:</strong>
                                        <c:if test="${(not empty user.properties['j:mobileNumber'].string)}">
                                            <c:if test="${(not empty user.properties['j:mobileNumber'].string)}">
                                                &nbsp;${user.properties['j:mobileNumber'].string}
                                            </c:if>
                                        </c:if>
                                    </div>
                                </c:if>
                            </div>
                            <c:if test="${fn:contains(publicPropertiesAsString, 'j:address') or fn:contains(publicPropertiesAsString, 'j:zipCode')
                                                                    or fn:contains(publicPropertiesAsString, 'j:city') or fn:contains(publicPropertiesAsString, 'j:country')}">
                                <div class="pull-right">
                                    <div class="pull-left">
                                        <strong><fmt:message key='jnt_user.j_address'/>&nbsp;:</strong>&nbsp;
                                        <div class="pull-right">
                                            <c:if test="${fn:contains(publicPropertiesAsString, 'j:address')}">
                                                <c:if test="${(not empty user.properties['j:address'].string)}">
                                                    ${user.properties['j:address'].string}
                                                </c:if>
                                            </c:if>
                                            <br />
                                            <c:if test="${fn:contains(publicPropertiesAsString, 'j:zipCode')}">
                                                <c:if test="${(not empty user.properties['j:zipCode'].string)}">
                                                    ${user.properties['j:zipCode'].string}
                                                </c:if>
                                            </c:if>
                                            <br />
                                            <c:if test="${fn:contains(publicPropertiesAsString, 'j:city')}">
                                                <c:if test="${(not empty user.properties['j:city'].string)}">
                                                    ${user.properties['j:city'].string}
                                                </c:if>
                                            </c:if>
                                            <br />
                                            <c:if test="${fn:contains(publicPropertiesAsString, 'j:country')}">
                                                <c:if test="${(not empty user.properties['j:country'].string)}">
                                                    ${user.properties['j:country'].string}
                                                </c:if>
                                            </c:if>
                                        </div>
                                    </div>
                                </div>
                            </c:if>
                        </div>
                    </div>
                </c:if>
                <c:if test="${fn:contains(publicPropertiesAsString, 'j:birthDate') or fn:contains(publicPropertiesAsString, 'preferredLanguage')}">
                    <h2><i class="icon-info-sign"></i>&nbsp;<fmt:message key="jnt_user.j_gender.other"/></h2>
                    <div class="box-1">
                        <c:if test="${fn:contains(publicPropertiesAsString, 'j:birthDate')}">
                            <jcr:nodeProperty node="${user}" name="j:birthDate" var="birthDate"/>
                            <div>
                                <strong><fmt:message key="jnt_user.age"/>&nbsp;:</strong>
                                <utility:dateDiff startDate="${birthDate.date.time}" endDate="${now}" format="years"/>&nbsp;<fmt:message key="jnt_user.profile.years"/>
                            </div>
                            <div>
                                <fmt:formatDate value="${birthDate.date.time}" pattern="dd, MMMM yyyy" var="displayBirthDate"/>
                                <strong><fmt:message key="jnt_user.j_birthDate"/>&nbsp;:</strong>
                                <c:if test="${not empty birthDate}">
                                    &nbsp;${displayBirthDate}
                                </c:if>
                            </div>
                        </c:if>
                        <c:if test="${fn:contains(publicPropertiesAsString, 'preferredLanguage')}">
                            <div>
                                <jcr:nodeProperty node="${user}" name="preferredLanguage" var="prefLang"/>
                                <c:set var="prefLang" value="${functions:toLocale(functions:default(prefLang.string, 'en'))}"/>
                                <c:if test="${(not empty user.properties['preferredLanguage'].string)}">
                                    <strong><fmt:message key="jnt_user.preferredLanguage"/>&nbsp;:</strong>
                                    &nbsp;${functions:displayLocaleNameWith(prefLang, prefLang)}
                                </c:if>
                            </div>
                        </c:if>
                    </div>
                </c:if>
            </div>
            <div class="span2"></div>
        </div>
    </div>
</div>