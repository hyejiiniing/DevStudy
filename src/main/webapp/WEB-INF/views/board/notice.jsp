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
          href="${pageContext.request.contextPath}/resources/css/board.css">
</head>

<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp"/>

    <main class="board-page board-page--list">
        <section class="intro">
            <p class="eyebrow">DEVSTUDY / NOTICE</p>
            <h1>공지사항</h1>
            <p class="description">
                DevStudy의 새로운 소식과 이용 안내를 확인하세요.
            </p>
        </section>

        <c:if test="${not empty successMessage}">
            <div class="message" role="status">
                <c:out value="${successMessage}"/>
            </div>
        </c:if>

        <section class="board" aria-labelledby="notice-heading">
            <div class="board-heading">
                <h2 id="notice-heading">운영 소식</h2>
                <span>DevStudy Notice</span>
            </div>

            <c:choose>
                <c:when test="${empty noticeList}">
                    <div class="empty">
                        <div class="empty-mark" aria-hidden="true">!</div>
                        <h3>등록된 공지사항이 없습니다.</h3>
                        <p>새로운 소식이 있으면 알려드릴게요.</p>
                    </div>
                </c:when>

                <c:otherwise>
                    <div class="table-wrap">
                        <table aria-label="공지사항 목록">
                            <thead>
                                <tr>
                                    <th scope="col" class="number">번호</th>
                                    <th scope="col">제목</th>
                                    <th scope="col" class="views">조회</th>
                                </tr>
                            </thead>

                            <tbody>
                                <c:forEach var="notice"
                                           items="${noticeList}"
                                           varStatus="status">
                                    <tr>
                                        <td class="number">
                                            <c:out value="${status.count}"/>
                                        </td>

                                        <td>
                                            <c:if test="${notice.isPinned == 1}">
                                                <span class="category">고정</span>
                                            </c:if>

                                            <a class="post-title"
											   href="${pageContext.request.contextPath}/board/notice/detail?noticeIdx=${notice.noticeIdx}">
											    <c:out value="${notice.title}"/>
											</a>
                                        </td>

                                        <td class="views">
                                            <c:out value="${notice.viewCount}"/>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <a class="back-link"
           href="${pageContext.request.contextPath}/home">
            ← 홈으로 돌아가기
        </a>
    </main>
</body>
</html>