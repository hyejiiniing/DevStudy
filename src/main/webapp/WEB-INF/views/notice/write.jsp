<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>공지사항 등록 | DevStudy</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/css/header.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/css/board.css">
</head>

<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp"/>

    <main class="board-page">
        <p class="eyebrow">DEVSTUDY / ADMIN</p>
        <h1>공지사항 등록</h1>
        <p class="description">
            새로운 소식과 서비스 이용 안내를 전해주세요.
        </p>

        <c:if test="${not empty errorMessage}">
            <div class="error" role="alert">
                <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <form class="card"
              action="${pageContext.request.contextPath}/board/notice/write"
              method="post">

            <input type="hidden"
                   name="writeToken"
                   value="${sessionScope.noticeWriteToken}">

            <div class="field">
                <label for="title">제목</label>
                <input type="text"
                       id="title"
                       name="title"
                       maxlength="256"
                       required
                       placeholder="공지사항 제목을 입력해주세요."
                       value="<c:out value='${noticeForm.title}'/>">
            </div>

            <div class="field">
                <label for="content">내용</label>
                <textarea id="content"
                          name="content"
                          required
                          placeholder="안내할 내용을 작성해주세요."><c:out value="${noticeForm.content}"/></textarea>
            </div>

            <div class="field">
                <label for="isPinned">상단 고정</label>
                <select id="isPinned" name="isPinned">
                    <option value="0"
                        ${noticeForm.isPinned == 0 ? 'selected' : ''}>
                        고정 안 함
                    </option>
                    <option value="1"
                        ${noticeForm.isPinned == 1 ? 'selected' : ''}>
                        상단 고정 · 필독
                    </option>
                </select>
            </div>

            <div class="field">
                <label for="isVisible">공개 여부</label>
                <select id="isVisible" name="isVisible">
                    <option value="1"
                        ${noticeForm.isVisible == 1 ? 'selected' : ''}>
                        공개
                    </option>
                    <option value="0"
                        ${noticeForm.isVisible == 0 ? 'selected' : ''}>
                        비공개
                    </option>
                </select>
                <p class="hint">
                    비공개 공지는 사용자 목록과 상세 화면에 표시되지 않습니다.
                </p>
            </div>

            <div class="actions">
                <a class="button"
                   href="${pageContext.request.contextPath}/board/notice/list">
                    취소
                </a>

                <button class="button button-dark" type="submit">
                    등록하기 ↗
                </button>
            </div>
        </form>
    </main>
</body>
</html>