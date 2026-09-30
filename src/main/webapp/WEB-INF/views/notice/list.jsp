<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>공지사항 | DevStudy</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/css/header.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/css/notice.css">
</head>

<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp"/>

    <main class="notice-page">
        <section class="notice-intro">
            <p class="notice-eyebrow">DEVSTUDY / NOTICE</p>
            <h1>공지사항<span>.</span></h1>
            <p>함께 알아두면 좋은 소식과 서비스 이용 안내를 전합니다.</p>
        </section>

        <c:if test="${not empty successMessage}">
            <div class="notice-message" role="status">
                <c:out value="${successMessage}"/>
            </div>
        </c:if>
        
        <c:if test="${not empty sessionScope.loginMemberIdx
              and sessionScope.loginRole == 'ADMIN'}">
		    <div class="notice-admin-actions">		    
		        <a class="notice-create-button"
				   href="${pageContext.request.contextPath}/board/notice/admin/list">
				    공지 관리
				</a>
		    </div>
		</c:if>

        <section class="notice-panel" aria-labelledby="notice-heading">
            <div class="notice-panel-heading">
                <h2 id="notice-heading">새로운 소식</h2>
                <span>최신 운영 안내를 확인하세요.</span>
            </div>

            <c:choose>
                <c:when test="${empty noticeList}">
                    <div class="notice-empty">
                        <span class="notice-empty-icon" aria-hidden="true">!</span>
                        <h3>아직 등록된 공지사항이 없어요.</h3>
                        <p>새로운 소식이 생기면 이곳에서 안내할게요.</p>
                    </div>
                </c:when>

                <c:otherwise>
                    <ul class="notice-list">
                        <c:forEach var="notice"
                                   items="${noticeList}"
                                   varStatus="status">

                            <c:url var="detailUrl" value="/board/notice/detail">
                                <c:param name="noticeIdx"
                                         value="${notice.noticeIdx}"/>
                            </c:url>

                            <li class="notice-item">
                                <a class="notice-link"
                                   href="<c:out value='${detailUrl}'/>">

                                    <div class="notice-number">
                                        <c:choose>
                                            <c:when test="${notice.isPinned == 1}">
                                                <span class="notice-badge">필독</span>
                                            </c:when>
                                            <c:otherwise>
                                                <c:out value="${status.count}"/>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <div class="notice-text">
                                        <span class="notice-label">운영 안내</span>
                                        <h3><c:out value="${notice.title}"/></h3>
                                    </div>

                                    <span class="notice-arrow" aria-hidden="true">
                                        ↗
                                    </span>
                                </a>
                            </li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </section>

        <div class="notice-bottom">
            <span>DevStudy와 함께 성장하는 공간을 만들어가요.</span>
            <a href="${pageContext.request.contextPath}/home">
                홈으로 돌아가기 →
            </a>
        </div>
    </main>
</body>
</html>
<style>
	.notice-admin-actions {
    gap: 8px;
}
</style>