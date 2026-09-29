<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title><c:out value="${notice.title}"/> | DevStudy</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/css/header.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/css/notice.css">
</head>

<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp"/>

    <main class="notice-page">
        <div class="notice-view-heading">
            <a class="notice-back"
               href="${pageContext.request.contextPath}/board/notice/list">
                ← 공지사항
            </a>

            <span class="notice-view-label">DEVSTUDY NOTICE</span>
        </div>

        <article class="notice-view"
                 aria-labelledby="notice-title">

            <div class="notice-view-top">
                <div class="notice-view-tags">
                    <span class="notice-view-category">운영 안내</span>

                    <c:if test="${notice.isPinned == 1}">
                        <span class="notice-badge">필독</span>
                    </c:if>
                </div>

                <h1 id="notice-title" class="notice-view-title">
                    <c:out value="${notice.title}"/>
                </h1>

                <div class="notice-view-meta">
                    <span class="notice-view-author">
                        <span class="notice-avatar" aria-hidden="true">D</span>
                        DevStudy 운영팀
                    </span>
                </div>
            </div>

            <div class="notice-view-content"><c:out value="${notice.content}"/></div>

            <div class="notice-view-signature">
                <strong>함께 배우고, 함께 성장해요.</strong>
                <span>DevStudy</span>
            </div>
        </article>

        <div class="notice-view-actions">
            <a class="notice-list-button"
               href="${pageContext.request.contextPath}/board/notice/list">
                <span>←</span>
                목록으로
            </a>
        </div>
    </main>
</body>
</html>