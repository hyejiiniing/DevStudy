<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>FAQ 관리 | DevStudy</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/css/header.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/css/board.css">
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp"/>

    <main class="board-page board-page--list">
        <section class="intro">
            <p class="eyebrow">DEVSTUDY / ADMIN</p>
            <h1>FAQ 관리</h1>
            <p class="description">
                자주 묻는 질문과 답변, 노출 순서와 공개 여부를 관리하세요.
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

        <div class="write-actions">
            <a class="button button-dark"
               href="${pageContext.request.contextPath}/board/admin/faq/write">
                FAQ 등록 ↗
            </a>
        </div>

        <section class="board" aria-label="FAQ 관리 목록">
            <c:choose>
                <c:when test="${empty faqList}">
                    <div class="empty">
                        <h3>등록된 FAQ가 없습니다.</h3>
                        <p>첫 번째 질문과 답변을 등록해주세요.</p>
                    </div>
                </c:when>

                <c:otherwise>
                    <div class="table-wrap">
                        <table>
                            <thead>
                                <tr>
                                    <th scope="col">순서</th>
                                    <th scope="col">공개 여부</th>
                                    <th scope="col">질문</th>
                                    <th scope="col">관리</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="faq" items="${faqList}">
                                    <tr>
                                        <td>
                                            <c:out value="${faq.sortOrder}"/>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${faq.isVisible == 1}">
                                                    공개
                                                </c:when>
                                                <c:otherwise>
                                                    비공개
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <a class="post-title"
                                               href="${pageContext.request.contextPath}/board/admin/faq/update?faqIdx=${faq.faqIdx}">
                                                <c:out value="${faq.question}"/>
                                            </a>
                                        </td>
                                        <td>
                                            <div class="owner-actions">
                                                <a class="button"
                                                   href="${pageContext.request.contextPath}/board/admin/faq/update?faqIdx=${faq.faqIdx}">
                                                    수정
                                                </a>

                                                <form action="${pageContext.request.contextPath}/board/admin/faq/delete"
                                                      method="post"
                                                      onsubmit="return confirm('이 FAQ를 삭제할까요? 삭제 후 복구할 수 없습니다.');">

                                                    <input type="hidden"
                                                           name="faqIdx"
                                                           value="${faq.faqIdx}">

                                                    <input type="hidden"
                                                           name="actionToken"
                                                           value="${sessionScope.faqActionToken}">

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

        <a class="back-link"
           href="${pageContext.request.contextPath}/board/list?boardType=5">
            ← 공개 FAQ 보기
        </a>
    </main>
</body>
</html>