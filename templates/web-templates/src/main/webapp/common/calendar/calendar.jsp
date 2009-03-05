<%--


    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ include file="../declarations.jspf" %>

<div class="box2 box2-style1"><!--start box 2 style1 -->
        <div class="box2-topright"> </div>
        <div class="box2-topleft"> </div>
        <h3 class="box2-header"><span><fmt:message key='statictitle.calendar.events'/></span></h3>
            <div class="box2-text">
                <p><fmt:message key='static.calendar.viewfromdate'/></p>
                <form action="" name="calendarStartDate" method="get">
                    <input type="hidden" name="startDate" id="startDate"/>
                </form>
            <!--start calendar-->
                <script type="text/javascript">
                    function setDate(date) {
                        document.calendarStartDate.startDate.value=date;
                        document.calendarStartDate.submit();
                    }
                </script>
                <%--
                this is GWT calendar
                <ui:calendar callback="setDate"/>
                --%>
                <script type="text/javascript">
                    jQuery(document).ready(function(jQuery) {
                        jQuery("#datepicker").datepicker({onSelect: function(dateText){setDate(dateText)},showButtonPanel: true, altField: '#startDate', altFormat: 'd/M/yy'},jQuery.datepicker.regional['${requestScope.currentRequest.locale}']);
                    });
                </script>
                <div id="datepicker"></div>
                
    <!--stop calendar-->
            </div>
        <div class="box2-bottomright"> </div>
        <div class="box2-bottomleft"> </div>
    <div class="clear"> </div>
</div>
<!--stop box 2 style 1-->