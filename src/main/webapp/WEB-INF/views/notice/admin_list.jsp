<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>공지사항 관리 | DevStudy</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/css/header.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/css/board.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/css/notice.css">
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp"/>

    <main class="board-page board-page--list">
        <section class="intro">
            <p class="eyebrow">DEVSTUDY / ADMIN</p>
            <h1>공지사항 관리</h1>
            <p class="description">
                공개·비공개 공지를 확인하고 내용을 관리하세요.
            </p>
        </section>

        <c:if test="${not empty successMessage}">
            <div class="message" role="status">
                <c:out value="${successMessage}"/>
            </div>
        </c:if>

        <c:if test="${not empty errorMessage}">
            <div class="error" role="alert">
                <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <div class="notice-admin-toolbar">
            <a class="button"
               href="${pageContext.request.contextPath}/board/notice/list">
                공개 목록
            </a>

            <a class="button button-dark"
               href="${pageContext.request.contextPath}/board/notice/write">
                공지 등록 ↗
            </a>
        </div>

        <section class="board" aria-label="공지사항 관리 목록">
            <c:choose>
                <c:when test="${empty noticeList}">
                    <div class="empty">
                        <h3>등록된 공지사항이 없습니다.</h3>
                        <p>첫 번째 공지사항을 등록해주세요.</p>
                    </div>
                </c:when>

                <c:otherwise>
                    <div class="table-wrap">
                        <table>
                            <thead>
                                <tr>
                                    <th scope="col">상태</th>
                                    <th scope="col">제목</th>
                                    <th scope="col">관리</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="notice" items="${noticeList}">
                                    <tr>
                                        <td>
                                            <c:choose>
                                                <c:when test="${notice.isVisible == 1}">
                                                    공개
                                                </c:when>
                                                <c:otherwise>
                                                    비공개
                                                </c:otherwise>
                                            </c:choose>
                                        </td>

                                        <td>
                                            <c:if test="${notice.isPinned == 1}">
                                                <span class="category">고정</span>
                                            </c:if>

                                            <a class="post-title"
                                               href="${pageContext.request.contextPath}/board/notice/update?noticeIdx=${notice.noticeIdx}">
                                                <c:out value="${notice.title}"/>
                                            </a>
                                        </td>

                                        <td>
                                            <div class="notice-manage-actions">
                                                <a class="button"
                                                   href="${pageContext.request.contextPath}/board/notice/update?noticeIdx=${notice.noticeIdx}">
                                                    수정
                                                </a>

                                                <form action="${pageContext.request.contextPath}/board/notice/delete"
                                                      method="post"
                                                      onsubmit="return confirm('이 공지사항을 삭제할까요? 삭제 후 복구할 수 없습니다.');">

                                                    <input type="hidden"
                                                           name="noticeIdx"
                                                           value="${notice.noticeIdx}">

                                                    <input type="hidden"
                                                           name="actionToken"
                                                           value="${sessionScope.noticeActionToken}">

                                                    <button class="button button-dark"
                                                            type="submit">
                                                        삭제
                                                    </button>
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </main>
</body>
</html>