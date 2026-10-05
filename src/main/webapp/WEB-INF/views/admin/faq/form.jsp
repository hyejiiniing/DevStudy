<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>FAQ ${empty faqForm.faqIdx ? '등록' : '수정'} | DevStudy</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/css/header.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/css/board.css">
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp"/>

    <main class="board-page">
        <p class="eyebrow">DEVSTUDY / ADMIN</p>
        <h1>FAQ ${empty faqForm.faqIdx ? '등록' : '수정'}</h1>
        <p class="description">
            사용자가 궁금해할 질문과 답변을 작성해주세요.
        </p>

        <c:if test="${not empty errorMessage}">
            <div class="error" role="alert">
                <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <form class="card"
              action="${pageContext.request.contextPath}/board/admin/faq/save"
              method="post">

            <c:if test="${not empty faqForm.faqIdx}">
                <input type="hidden"
                       name="faqIdx"
                       value="${faqForm.faqIdx}">
            </c:if>

            <input type="hidden"
                   name="actionToken"
                   value="${sessionScope.faqActionToken}">

            <div class="field">
                <label for="question">질문</label>
                <input type="text"
                       id="question"
                       name="question"
                       maxlength="256"
                       required
                       placeholder="예: 첨부파일은 몇 개까지 등록할 수 있나요?"
                       value="<c:out value='${faqForm.question}'/>">
            </div>

            <div class="field">
                <label for="answer">답변</label>
                <textarea id="answer"
                          name="answer"
                          required
                          placeholder="질문에 대한 답변을 작성해주세요."><c:out value="${faqForm.answer}"/></textarea>
            </div>

            <div class="field">
                <label for="sortOrder">노출 순서</label>
                <input type="number"
                       id="sortOrder"
                       name="sortOrder"
                       min="0"
                       max="2147483647"
                       step="1"
                       required
                       value="<c:out value='${faqForm.sortOrder}'/>">

                <p class="hint">
                    작은 숫자부터 표시됩니다.
                    순서가 같으면 최근 등록한 FAQ가 먼저 나옵니다.
                </p>
            </div>

            <div class="field">
                <label for="isVisible">공개 여부</label>
                <select id="isVisible" name="isVisible">
                    <option value="1"
                        ${faqForm.isVisible == 1 ? 'selected' : ''}>
                        공개
                    </option>
                    <option value="0"
                        ${faqForm.isVisible == 0 ? 'selected' : ''}>
                        비공개
                    </option>
                </select>

                <p class="hint">
                    비공개 FAQ는 관리자 목록에서만 확인할 수 있습니다.
                </p>
            </div>

            <div class="actions">
                <a class="button"
                   href="${pageContext.request.contextPath}/board/admin/faq/list">
                    취소
                </a>

                <button class="button button-dark" type="submit">
                    ${empty faqForm.faqIdx ? '등록하기' : '수정 완료'} ↗
                </button>
            </div>
        </form>
    </main>
</body>
</html>