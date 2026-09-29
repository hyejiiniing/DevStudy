<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/resources/css/header.css">
<title><c:out value="${boardName}"/> | DevStudy</title>
</head>

<style>
    * { box-sizing: border-box; }

    body {
        margin: 0;
        background: #fafafa;
        color: #171717;
        font-family: -apple-system, BlinkMacSystemFont,
                     "Apple SD Gothic Neo", sans-serif;
    }

    a { color: inherit; text-decoration: none; }

    .container {
        width: min(1080px, calc(100% - 40px));
        margin: 0 auto;
    }

    header {
        background: #fff;
        border-bottom: 1px solid #e5e5e5;
    }

    .header-inner {
        min-height: 76px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 20px;
    }

    .logo {
        font-size: 23px;
        font-weight: 800;
        letter-spacing: -1px;
    }

    .account {
        font-size: 13px;
        overflow-wrap: anywhere;
    }

    .intro { padding: 56px 0 28px; }

    .eyebrow {
        margin: 0 0 16px;
        font-size: 10px;
        font-weight: 700;
        letter-spacing: 3px;
        color: #666;
    }

    h1 {
        margin: 0;
        font-size: 40px;
        letter-spacing: -2px;
    }

    .description {
        margin: 14px 0 0;
        color: #666;
        font-size: 14px;
        line-height: 1.8;
    }

    .tabs {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
        margin-bottom: 30px;
    }

    .tab {
        padding: 11px 17px;
        border: 1px solid #ddd;
        border-radius: 8px;
        background: #fff;
        font-size: 13px;
    }

    .tab:hover { background: #eee; }

    .tab.active {
        background: #171717;
        border-color: #171717;
        color: #fff;
    }

    .board {
        overflow: hidden;
        border: 1px solid #e3e3e3;
        border-radius: 16px;
        background: #fff;
    }

    .board-heading {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 16px;
        padding: 24px;
        border-bottom: 1px solid #eee;
    }

    .board-heading h2 {
        margin: 0;
        font-size: 16px;
    }

    .board-heading span {
        color: #707070;
        font-size: 12px;
    }

    .table-wrap { overflow-x: auto; }

    table {
        width: 100%;
        border-collapse: collapse;
        text-align: left;
        font-size: 13px;
    }

    th {
        padding: 15px 20px;
        color: #666;
        background: #fcfcfc;
        font-size: 12px;
        font-weight: 500;
        white-space: nowrap;
    }

    td {
        padding: 20px;
        border-top: 1px solid #eee;
    }

    .number, .views {
        width: 80px;
        color: #777;
    }

    .category {
        display: inline-block;
        margin-right: 8px;
        padding: 4px 7px;
        border: 1px solid #e3e3e3;
        border-radius: 5px;
        color: #666;
        font-size: 10px;
    }

    .post-title {
        font-weight: 600;
        overflow-wrap: anywhere;
    }

    .empty {
        padding: 76px 24px;
        text-align: center;
    }

    .empty-mark {
        display: grid;
        place-items: center;
        width: 52px;
        height: 52px;
        margin: 0 auto 20px;
        border: 1px solid #e3e3e3;
        border-radius: 14px;
        font-family: monospace;
        font-size: 20px;
    }

    .empty h3 {
        margin: 0 0 10px;
        font-size: 17px;
    }

    .empty p {
        margin: 0;
        color: #707070;
        font-size: 13px;
        line-height: 1.8;
    }

    .back-link {
        display: inline-block;
        margin: 24px 0 64px;
        color: #666;
        font-size: 13px;
    }
    
    .search-form {
	    display: flex;
	    flex-wrap: wrap;
	    align-items: center;
	    gap: 8px;
	    margin-bottom: 24px;
	}
	
	.search-form select,
	.search-form input {
	    min-height: 44px;
	    padding: 0 14px;
	    border: 1px solid #d9d9d9;
	    border-radius: 8px;
	    background: #fff;
	    color: #171717;
	    font: inherit;
	    font-size: 13px;
	}
	
	.search-form input {
	    flex: 1;
	    min-width: 160px;
	}
	
	.search-form button {
	    min-height: 44px;
	    padding: 0 22px;
	    border: 1px solid #171717;
	    border-radius: 8px;
	    background: #171717;
	    color: #fff;
	    font: inherit;
	    font-size: 13px;
	    cursor: pointer;
	}
	
	.search-form button:hover {
	    background: #383838;
	}
	
	.search-reset {
	    padding: 12px 8px;
	    color: #666;
	    font-size: 12px;
	    text-decoration: underline;
	    text-underline-offset: 4px;
	}
	
	.pagination {
	    display: flex;
	    justify-content: center;
	    flex-wrap: wrap;
	    gap: 6px;
	    margin: 28px 0;
	}
	
	.page-link {
	    display: inline-flex;
	    align-items: center;
	    justify-content: center;
	    min-width: 40px;
	    min-height: 40px;
	    padding: 8px;
	    border: 1px solid #e0e0e0;
	    border-radius: 8px;
	    background: #fff;
	    font-size: 13px;
	}
	
	a.page-link:hover {
	    background: #eee;
	}
	
	.page-link.active {
	    border-color: #171717;
	    background: #171717;
	    color: #fff;
	}
	
	.search-form input:focus-visible,
	.search-form select:focus-visible,
	.search-form button:focus-visible {
	    outline: 2px solid #171717;
	    outline-offset: 3px;
	}
	
	.sr-only {
	    position: absolute;
	    width: 1px;
	    height: 1px;
	    padding: 0;
	    margin: -1px;
	    overflow: hidden;
	    clip-path: inset(50%);
	    white-space: nowrap;
	    border: 0;
	}			
	
	    footer {
	        padding: 26px 20px;
	        border-top: 1px solid #e5e5e5;
	        color: #707070;
	        text-align: center;
	        font-size: 11px;
	        letter-spacing: 1px;
	    }
	
	    a:focus-visible {
	        outline: 2px solid #171717;
	        outline-offset: 4px;
	    }
	
	    @media (max-width: 600px) {
	        .intro { padding-top: 36px; }
	        h1 { font-size: 32px; }
	        .tab { padding: 10px 13px; }
	        th, td { padding: 14px 12px; }
	        .number { display: none; }
	    }
</style>
</head>

<body>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="container">
    <section class="intro">
        <p class="eyebrow">DEVSTUDY / BOARD</p>
        <h1><c:out value="${boardName}"/></h1>
        <p class="description">
            <c:choose>
                <c:when test="${boardType == 1}">
                    공부 기록부터 개발 일상까지, 편하게 이야기 나눠요.
                </c:when>
                <c:when test="${boardType == 2}">
                    직접 써본 AI 활용법과 새로운 시도를 공유해요.
                </c:when>
                <c:when test="${boardType == 3}">
                    자격증 준비 과정과 학습 노하우를 함께 나눠요.
                </c:when>
                <c:when test="${boardType == 4}">
                    같은 목표를 가진 스터디원과 프로젝트 팀원을 만나세요.
                </c:when>
                <c:when test="${boardType == 5}">
                    막히는 부분이 있다면 질문하고 함께 해결해요.
                </c:when>
                <c:otherwise>
                    DevStudy의 운영 안내와 새로운 소식을 확인하세요.
                </c:otherwise>
            </c:choose>
        </p>
    </section>

    <nav class="tabs" aria-label="게시판 선택">
        <a class="tab ${boardType == 1 ? 'active' : ''}"
           href="${pageContext.request.contextPath}/board/list?boardType=1">
            커뮤니티
        </a>
        <a class="tab ${boardType == 2 ? 'active' : ''}"
           href="${pageContext.request.contextPath}/board/list?boardType=2">
            AI
        </a>
        <a class="tab ${boardType == 3 ? 'active' : ''}"
           href="${pageContext.request.contextPath}/board/list?boardType=3">
            자격증
        </a>
        <a class="tab ${boardType == 4 ? 'active' : ''}"
           href="${pageContext.request.contextPath}/board/list?boardType=4">
            모임
        </a>
        <a class="tab ${boardType == 5 ? 'active' : ''}"
           href="${pageContext.request.contextPath}/board/list?boardType=5">
            Q&amp;A
        </a>
        <a class="tab ${boardType == 6 ? 'active' : ''}"
           href="${pageContext.request.contextPath}/board/notice">
            공지사항
        </a>
    </nav>
    
    <form class="search-form"
	      action="${pageContext.request.contextPath}/board/list"
	      method="get">
	
	    <input type="hidden" name="boardType" value="${boardType}">
	
	    <label class="sr-only" for="searchType">검색 범위</label>
	    <select id="searchType" name="searchType">
	        <option value="all"
	                ${searchType == 'all' ? 'selected' : ''}>
	            제목 + 내용
	        </option>
	        <option value="title"
	                ${searchType == 'title' ? 'selected' : ''}>
	            제목
	        </option>
	        <option value="content"
	                ${searchType == 'content' ? 'selected' : ''}>
	            내용
	        </option>
	    </select>
	
	    <label class="sr-only" for="keyword">검색어</label>
	    <input type="search" id="keyword" name="keyword"
	           maxlength="100"
	           value="<c:out value='${keyword}'/>"
	           placeholder="궁금한 이야기를 찾아보세요">
	
	    <button type="submit">검색</button>
	
	    <c:if test="${not empty keyword}">
	        <a class="search-reset"
	           href="${pageContext.request.contextPath}/board/list?boardType=${boardType}">
	            초기화
	        </a>
	    </c:if>
	</form>
    
    <c:if test="${not empty successMessage}">
	    <p role="status">
	        <c:out value="${successMessage}"/>
	    </p>
	</c:if>
	
	<c:if test="${not empty sessionScope.loginMemberIdx
	             and (boardType != 6 or sessionScope.loginRole == 'ADMIN')}">
	    <div style="display: flex; justify-content: flex-end; margin-bottom: 16px;">
	        <a class="tab active"
	           href="${pageContext.request.contextPath}/board/write?boardType=${boardType}">
	            글쓰기 ↗
	        </a>
	    </div>
	</c:if>

    <section class="board" aria-labelledby="list-title">
        <div class="board-heading">
            <h2 id="list-title">최근 게시글</h2>
            <span>
			    총 <c:out value="${totalCount}"/>개
			</span>
        </div>

        <c:choose>
            <c:when test="${empty boardList}">
                <div class="empty">
				    <div class="empty-mark" aria-hidden="true">{ }</div>
				
				    <c:choose>
				        <c:when test="${not empty keyword}">
				            <h3>검색 결과가 없어요.</h3>
				            <p>다른 검색어로 다시 찾아보세요.</p>
				        </c:when>
				        <c:otherwise>
				            <h3>아직 등록된 글이 없어요.</h3>
				            <p>첫 번째 이야기를 남겨보세요.</p>
				        </c:otherwise>
				    </c:choose>
				</div>
            </c:when>

            <c:otherwise>
                <div class="table-wrap">
                    <table aria-label="${boardName} 게시글 목록">
                        <thead>
                            <tr>
                                <th class="number" scope="col">번호</th>
                                <th scope="col">제목</th>
                                <th class="views" scope="col">조회</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="board" items="${boardList}" varStatus="status">
                                <tr>
						            <td class="number">
						                ${totalCount - (currentPage - 1) * 10 - status.index}
						            </td>
						
						            <td>
						                <c:if test="${not empty board.category}">
						                    <span class="category">
						                        <c:out value="${board.category}"/>
						                    </span>
						                </c:if>
                                        <a class="post-title"
										   href="${pageContext.request.contextPath}/board/detail?boardIdx=${board.boardIdx}">
										    <c:out value="${board.title}"/>
										</a>
                                    </td>
                                    <td class="views">
                                        <c:out value="${board.viewCount}"/>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
    
    <c:if test="${totalPages > 1}">
	    <nav class="pagination" aria-label="게시글 페이지">
	
	        <c:if test="${currentPage > 1}">
	            <c:url var="prevUrl" value="/board/list">
	                <c:param name="boardType" value="${boardType}"/>
	                <c:param name="searchType" value="${searchType}"/>
	                <c:param name="keyword" value="${keyword}"/>
	                <c:param name="page" value="${currentPage - 1}"/>
	            </c:url>
	
	            <a class="page-link"
	               href="<c:out value='${prevUrl}'/>"
	               aria-label="이전 페이지">←</a>
	        </c:if>
	
	        <c:forEach var="pageNo" begin="${startPage}" end="${endPage}">
	            <c:choose>
	                <c:when test="${pageNo == currentPage}">
	                    <span class="page-link active" aria-current="page">
	                        ${pageNo}
	                    </span>
	                </c:when>
	
	                <c:otherwise>
	                    <c:url var="pageUrl" value="/board/list">
	                        <c:param name="boardType" value="${boardType}"/>
	                        <c:param name="searchType" value="${searchType}"/>
	                        <c:param name="keyword" value="${keyword}"/>
	                        <c:param name="page" value="${pageNo}"/>
	                    </c:url>
	
	                    <a class="page-link"
	                       href="<c:out value='${pageUrl}'/>">
	                        ${pageNo}
	                    </a>
	                </c:otherwise>
	            </c:choose>
	        </c:forEach>
	
	        <c:if test="${currentPage < totalPages}">
	            <c:url var="nextUrl" value="/board/list">
	                <c:param name="boardType" value="${boardType}"/>
	                <c:param name="searchType" value="${searchType}"/>
	                <c:param name="keyword" value="${keyword}"/>
	                <c:param name="page" value="${currentPage + 1}"/>
	            </c:url>
	
	            <a class="page-link"
	               href="<c:out value='${nextUrl}'/>"
	               aria-label="다음 페이지">→</a>
	        </c:if>
	
	    </nav>
	</c:if>

    <a class="back-link"
       href="${pageContext.request.contextPath}/home">
        ← 홈으로 돌아가기
    </a>
</main>

<footer>DEVSTUDY · LEARN. BUILD. TOGETHER.</footer>
</body>
</html>