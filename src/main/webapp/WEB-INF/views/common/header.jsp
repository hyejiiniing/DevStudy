<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<header class="header">
    <div class="container header-inner">
        <a class="logo"
           href="${pageContext.request.contextPath}/home">
            <span class="logo-mark" aria-hidden="true">&lt;/&gt;</span>
            DevStudy
        </a>

        <nav class="nav" aria-label="메뉴">
            <a href="${pageContext.request.contextPath}/board/list?boardType=1">커뮤니티</a>
			<a href="${pageContext.request.contextPath}/board/list?boardType=2">AI</a>
			<a href="${pageContext.request.contextPath}/board/list?boardType=3">자격증</a>
			<a href="${pageContext.request.contextPath}/board/list?boardType=4">스터디</a>
			<a href="${pageContext.request.contextPath}/board/list?boardType=5">Q&amp;A</a>
			<a href="${pageContext.request.contextPath}/board/notice/list">공지사항</a>
        </nav>

        <div class="account">
            <c:choose>
                <c:when test="${not empty sessionScope.loginMemberIdx}">
                    <span class="account-name">
                        <strong>
                            <c:out value="${sessionScope.loginNickname}"/>
                        </strong>님
                    </span>
                    <form action="${pageContext.request.contextPath}/member/logout"
		                  method="post"
		                  style="margin: 0;">
		                <a class="button-light" href="${pageContext.request.contextPath}/member/signin">
		                	마이페이지
	                    </a>
		                <button type="submit" class="button-dark">
		                    로그아웃
		                </button>
		            </form>
                </c:when>
                <c:otherwise>
                    <a class="button-light" href="${pageContext.request.contextPath}/member/signin">
                        로그인
                    </a>
                    <a class="button-dark"
                       href="${pageContext.request.contextPath}/member/signup">
                        회원가입 ↗
                    </a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</header>