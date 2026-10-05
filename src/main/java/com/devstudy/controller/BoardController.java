package com.devstudy.controller;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.devstudy.file.BoardFileStorage;
import com.devstudy.service.BoardService;
import com.devstudy.vo.BoardFileVO;
import com.devstudy.vo.BoardVO;
import com.devstudy.vo.FaqVO;
import com.devstudy.vo.NoticeVO;

@Controller
@RequestMapping("/board")
public class BoardController {

	private final BoardService boardService;
	private final BoardFileStorage boardFileStorage;

	public BoardController(BoardService boardService, BoardFileStorage boardFileStorage) {

		this.boardService = boardService;
		this.boardFileStorage = boardFileStorage;
	}

	@GetMapping("/list")
	public String boardList(@RequestParam(defaultValue = "1") int boardType, @RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "all") String searchType, @RequestParam(defaultValue = "") String keyword,
			Model model) throws Exception {

		if (boardType < 1 || boardType > 6) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 게시판입니다.");
		}

		if (boardType == 5) {
			model.addAttribute("faqList", boardService.selectFaqList());
			return "board/faq";
		}

		if (boardType == 6) {
			model.addAttribute("noticeList", boardService.selectNoticeList());
			return "board/notice/list";
		}

		if (!"all".equals(searchType) && !"title".equals(searchType) && !"content".equals(searchType)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 검색 조건입니다.");
		}

		keyword = keyword.strip();

		if (keyword.length() > 100) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "검색어는 100자 이내로 입력해주세요.");
		}

		BoardVO vo = new BoardVO();
		vo.setBoardType(boardType);
		vo.setSearchType(searchType);
		vo.setKeyword(keyword);

		long totalCount = boardService.selectBoardCount(vo);

		long calculatedPages = totalCount / vo.getPageSize() + (totalCount % vo.getPageSize() == 0 ? 0 : 1);

		int totalPages = (int) Math.min(Integer.MAX_VALUE, Math.max(1L, calculatedPages));

		int currentPage = Math.min(Math.max(page, 1), totalPages);
		vo.setPage(currentPage);

		int startPage = ((currentPage - 1) / 5) * 5 + 1;
		int endPage = (int) Math.min((long) startPage + 4, totalPages);

		model.addAttribute("boardType", boardType);
		model.addAttribute("boardName", getBoardName(boardType));
		model.addAttribute("boardList", boardService.selectBoardList(vo));

		model.addAttribute("searchType", searchType);
		model.addAttribute("keyword", keyword);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("currentPage", currentPage);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);

		return "board/list";
	}

	@GetMapping("/write")
	public String writeForm(@RequestParam(defaultValue = "1") int boardType, HttpSession session, Model model) {

		if (session.getAttribute("loginMemberIdx") == null) {
			return "redirect:/member/signin";
		}

		checkWritePermission(boardType, session);

		if (session.getAttribute("boardWriteToken") == null) {
			session.setAttribute("boardWriteToken", UUID.randomUUID().toString());
		}

		model.addAttribute("boardType", boardType);
		model.addAttribute("boardName", getBoardName(boardType));

		return "board/write";
	}

	@PostMapping("/write")
	public String write(@RequestParam int boardType, @RequestParam(defaultValue = "") String title,
			@RequestParam(defaultValue = "") String content, @RequestParam(defaultValue = "") String category,
			@RequestParam(value = "files", required = false) List<MultipartFile> files,
			@RequestParam(value = "writeToken", required = false) String writeToken, HttpSession session, Model model,
			RedirectAttributes redirectAttributes) throws Exception {

		Long loginMemberIdx = (Long) session.getAttribute("loginMemberIdx");

		if (loginMemberIdx == null) {
			return "redirect:/member/signin";
		}

		checkWritePermission(boardType, session);

		String expectedToken = (String) session.getAttribute("boardWriteToken");

		if (expectedToken == null || !expectedToken.equals(writeToken)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "글쓰기 화면을 새로 열고 다시 시도해주세요.");
		}

		BoardVO vo = new BoardVO();
		vo.setBoardType(boardType);
		vo.setTitle(title);
		vo.setContent(content);
		vo.setCategory(category);

		try {
			boardService.insertBoard(vo, loginMemberIdx, (String) session.getAttribute("loginRole"), files);
		} catch (IllegalArgumentException e) {
			model.addAttribute("boardType", boardType);
			model.addAttribute("boardName", getBoardName(boardType));
			model.addAttribute("form", vo);
			model.addAttribute("errorMessage", e.getMessage());
			return "board/write";
		}

		session.removeAttribute("boardWriteToken");

		redirectAttributes.addFlashAttribute("successMessage", "게시글이 등록되었습니다.");

		return "redirect:/board/list?boardType=" + boardType;
	}

	private void checkWritePermission(int boardType, HttpSession session) {

		if (boardType < 1 || boardType > 6) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 게시판입니다.");
		}

		if (boardType == 6 && !"ADMIN".equals(session.getAttribute("loginRole"))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "공지사항은 관리자만 작성할 수 있습니다.");
		}
	}

	private String getBoardName(int boardType) {
		String[] names = { "", "커뮤니티", "AI", "자격증", "모임", "Q&A", "공지사항" };

		return names[boardType];
	}

	@GetMapping("/detail")
	public String detail(@RequestParam int boardIdx, HttpSession session, Model model) throws Exception {

		BoardVO board = findBoard(boardIdx);

		prepareBoardActionToken(session);

		model.addAttribute("board", board);
		model.addAttribute("boardName", getBoardName(board.getBoardType()));

		BoardFileVO condition = new BoardFileVO();
		condition.setBoardIdx(boardIdx);

		model.addAttribute("fileList", boardService.selectBoardFileList(condition));

		return "board/detail";
	}

	@GetMapping("/download")
	public ResponseEntity<Resource> download(@RequestParam int boardIdx, @RequestParam long fileIdx) throws Exception {

		if (boardIdx <= 0 || fileIdx <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 파일 요청입니다.");
		}

		findBoard(boardIdx);

		BoardFileVO condition = new BoardFileVO();
		condition.setBoardIdx(boardIdx);
		condition.setFileIdx(fileIdx);

		BoardFileVO file = boardService.selectBoardFile(condition);

		if (file == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "첨부파일을 찾을 수 없습니다.");
		}

		Path path = boardFileStorage.resolve(file.getStoredName());

		if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "저장된 파일을 찾을 수 없습니다.");
		}

		Resource resource = new UrlResource(path.toUri());

		String disposition = ContentDisposition.attachment().filename(file.getOriginalName(), StandardCharsets.UTF_8)
				.build().toString();

		return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).contentLength(Files.size(path))
				.header(HttpHeaders.CONTENT_DISPOSITION, disposition).header("X-Content-Type-Options", "nosniff")
				.body(resource);
	}

	@GetMapping("/update")
	public String updateForm(@RequestParam int boardIdx, HttpSession session, Model model) throws Exception {

		if (session.getAttribute("loginMemberIdx") == null) {
			return "redirect:/member/signin";
		}

		BoardVO board = findBoard(boardIdx);
		checkBoardOwner(board, session);

		prepareBoardActionToken(session);

		BoardFileVO condition = new BoardFileVO();
		condition.setBoardIdx(boardIdx);

		model.addAttribute("board", board);
		model.addAttribute("boardName", getBoardName(board.getBoardType()));
		model.addAttribute("fileList", boardService.selectBoardFileList(condition));

		return "board/update";
	}

	@PostMapping("/update")
	public String update(@RequestParam int boardIdx, @RequestParam(defaultValue = "") String title,
			@RequestParam(defaultValue = "") String content, @RequestParam(defaultValue = "") String category,
			@RequestParam(value = "files", required = false) List<MultipartFile> files,
			@RequestParam(value = "deleteFileIdxs", required = false) List<Long> deleteFileIdxs,
			@RequestParam(value = "actionToken", required = false) String actionToken, HttpSession session, Model model,
			RedirectAttributes redirectAttributes) throws Exception {

		if (session.getAttribute("loginMemberIdx") == null) {
			return "redirect:/member/signin";
		}

		checkBoardActionToken(session, actionToken);

		BoardVO board = findBoard(boardIdx);
		checkBoardOwner(board, session);

		board.setTitle(title);
		board.setContent(content);
		board.setCategory(category);

		try {
			boardService.updateBoard(board, (Long) session.getAttribute("loginMemberIdx"),
					(String) session.getAttribute("loginRole"), files, deleteFileIdxs);

		} catch (IllegalArgumentException e) {
			BoardFileVO condition = new BoardFileVO();
			condition.setBoardIdx(boardIdx);

			model.addAttribute("board", board);
			model.addAttribute("boardName", getBoardName(board.getBoardType()));
			model.addAttribute("errorMessage", e.getMessage());
			model.addAttribute("fileList", boardService.selectBoardFileList(condition));

			return "board/update";
		}

		redirectAttributes.addFlashAttribute("successMessage", "게시글이 수정되었습니다.");

		return "redirect:/board/detail?boardIdx=" + boardIdx;
	}

	@PostMapping("/delete")
	public String delete(@RequestParam int boardIdx,
			@RequestParam(value = "actionToken", required = false) String actionToken, HttpSession session,
			RedirectAttributes redirectAttributes) throws Exception {

		if (session.getAttribute("loginMemberIdx") == null) {
			return "redirect:/member/signin";
		}

		checkBoardActionToken(session, actionToken);

		BoardVO board = findBoard(boardIdx);
		checkBoardOwner(board, session);

		try {
			boardService.deleteBoard(board, (Long) session.getAttribute("loginMemberIdx"));
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
		}

		redirectAttributes.addFlashAttribute("successMessage", "게시글이 삭제되었습니다.");

		return "redirect:/board/list?boardType=" + board.getBoardType();
	}

	private BoardVO findBoard(int boardIdx) throws Exception {

		if (boardIdx <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 게시글 번호입니다.");
		}

		BoardVO condition = new BoardVO();
		condition.setBoardIdx(boardIdx);

		BoardVO board = boardService.selectBoard(condition);

		if (board == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
		}

		return board;
	}

	private void checkBoardOwner(BoardVO board, HttpSession session) {

		Long loginMemberIdx = (Long) session.getAttribute("loginMemberIdx");

		if (loginMemberIdx == null || !Objects.equals(loginMemberIdx, board.getMemberIdx())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 글만 변경할 수 있습니다.");
		}

		if (board.getBoardType() == 6 && !"ADMIN".equals(session.getAttribute("loginRole"))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "공지사항 변경 권한이 없습니다.");
		}
	}

	private void prepareBoardActionToken(HttpSession session) {

		if (session.getAttribute("boardActionToken") == null) {
			session.setAttribute("boardActionToken", UUID.randomUUID().toString());
		}
	}

	private void checkBoardActionToken(HttpSession session, String actionToken) {

		String expected = (String) session.getAttribute("boardActionToken");

		if (expected == null || !expected.equals(actionToken)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "화면을 새로 열고 다시 시도해주세요.");
		}
	}

//	@GetMapping("/faq/list")
//	public String adminFaqList(HttpSession session, Model model) throws Exception {
//
//		checkFaqAdmin(session);
//
//		model.addAttribute("faqList", boardService.selectFaqList());
//
//		return "/faq/list";
//	}

	private void checkFaqAdmin(HttpSession session) {

		if (session.getAttribute("loginMemberIdx") == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
		}

		if (!"ADMIN".equals(session.getAttribute("loginRole"))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
		}
	}
	
	@GetMapping("/admin/faq/list")
	public String adminFaqList(
	        HttpSession session,
	        Model model) throws Exception {

	    checkFaqAdmin(session);
	    prepareFaqActionToken(session);

	    model.addAttribute(
	            "faqList",
	            boardService.selectAdminFaqList(
	                    (Long) session.getAttribute("loginMemberIdx"),
	                    (String) session.getAttribute("loginRole")));

	    return "admin/faq/list";
	}
	
	private void prepareFaqActionToken(HttpSession session) {

	    if (session.getAttribute("faqActionToken") == null) {
	        session.setAttribute(
	                "faqActionToken", UUID.randomUUID().toString());
	    }
	}

	private void checkFaqActionToken(
	        HttpSession session, String actionToken) {

	    String expected =
	            (String) session.getAttribute("faqActionToken");

	    if (expected == null || !expected.equals(actionToken)) {
	        throw new ResponseStatusException(
	                HttpStatus.FORBIDDEN,
	                "화면을 새로 열고 다시 시도해주세요.");
	    }
	}

	@GetMapping("/admin/faq/write")
	public String adminFaqWrite(
	        HttpSession session,
	        Model model) {

	    checkFaqAdmin(session);
	    prepareFaqActionToken(session);

	    FaqVO form = new FaqVO();
	    form.setSortOrder(0);
	    form.setIsVisible(1);

	    model.addAttribute("faqForm", form);

	    return "admin/faq/form";
	}

	@GetMapping("/admin/faq/update")
	public String adminFaqUpdate(
	        @RequestParam("faqIdx") Long faqIdx,
	        HttpSession session,
	        Model model) throws Exception {

	    checkFaqAdmin(session);

	    if (faqIdx <= 0) {
	        throw new ResponseStatusException(
	                HttpStatus.BAD_REQUEST,
	                "올바르지 않은 FAQ 번호입니다.");
	    }

	    FaqVO condition = new FaqVO();
	    condition.setFaqIdx(faqIdx);

	    FaqVO faq = boardService.selectAdminFaq(
	            condition,
	            (Long) session.getAttribute("loginMemberIdx"),
	            (String) session.getAttribute("loginRole"));

	    if (faq == null) {
	        throw new ResponseStatusException(
	                HttpStatus.NOT_FOUND,
	                "FAQ를 찾을 수 없습니다.");
	    }

	    prepareFaqActionToken(session);
	    model.addAttribute("faqForm", faq);

	    return "admin/faq/form";
	}

	@PostMapping("/admin/faq/save")
	public String adminFaqSave(
	        @RequestParam(name = "faqIdx", required = false) Long faqIdx,
	        @RequestParam(name = "question", defaultValue = "") String question,
	        @RequestParam(name = "answer", defaultValue = "") String answer,
	        @RequestParam("sortOrder") int sortOrder,
	        @RequestParam("isVisible") int isVisible,
	        @RequestParam(name = "actionToken", required = false) String actionToken,
	        HttpSession session,
	        Model model,
	        RedirectAttributes redirectAttributes) throws Exception {

	    checkFaqAdmin(session);
	    checkFaqActionToken(session, actionToken);

	    boolean creating = faqIdx == null;

	    FaqVO vo = new FaqVO();
	    vo.setFaqIdx(faqIdx);
	    vo.setQuestion(question);
	    vo.setAnswer(answer);
	    vo.setSortOrder(sortOrder);
	    vo.setIsVisible(isVisible);

	    try {
	        boardService.saveFaq(
	                vo,
	                (Long) session.getAttribute("loginMemberIdx"),
	                (String) session.getAttribute("loginRole"));
	    } catch (IllegalArgumentException e) {
	        model.addAttribute("faqForm", vo);
	        model.addAttribute("errorMessage", e.getMessage());
	        return "admin/faq/form";
	    }

	    redirectAttributes.addFlashAttribute(
	            "successMessage",
	            creating ? "FAQ가 등록되었습니다." : "FAQ가 수정되었습니다.");

	    return "redirect:/board/admin/faq/list";
	}

	@PostMapping("/admin/faq/delete")
	public String adminFaqDelete(
	        @RequestParam("faqIdx") Long faqIdx,
	        @RequestParam(name = "actionToken", required = false) String actionToken,
	        HttpSession session,
	        RedirectAttributes redirectAttributes) throws Exception {

	    checkFaqAdmin(session);
	    checkFaqActionToken(session, actionToken);

	    FaqVO vo = new FaqVO();
	    vo.setFaqIdx(faqIdx);

	    try {
	        boardService.deleteFaq(
	                vo,
	                (Long) session.getAttribute("loginMemberIdx"),
	                (String) session.getAttribute("loginRole"));
	    } catch (IllegalArgumentException e) {
	        redirectAttributes.addFlashAttribute(
	                "errorMessage", e.getMessage());

	        return "redirect:/board/admin/faq/list";
	    }

	    redirectAttributes.addFlashAttribute(
	            "successMessage", "FAQ가 삭제되었습니다.");

	    return "redirect:/board/admin/faq/list";
	}

	@GetMapping("/notice/list")
	public String noticeList(Model model) throws Exception {

		model.addAttribute("noticeList", boardService.selectNoticeList());

		return "notice/list";
	}

	@GetMapping("/notice/detail")
	public String noticeDetail(@RequestParam("noticeIdx") Long noticeIdx, Model model) throws Exception {

		if (noticeIdx <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 공지사항 번호입니다.");
		}

		NoticeVO condition = new NoticeVO();
		condition.setNoticeIdx(noticeIdx);

		NoticeVO notice = boardService.selectNotice(condition);

		if (notice == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다.");
		}

		model.addAttribute("notice", notice);

		return "notice/detail";
	}

	private void checkNoticeAdmin(HttpSession session) {

		if (session.getAttribute("loginMemberIdx") == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
		}

		if (!"ADMIN".equals(session.getAttribute("loginRole"))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
		}
	}

	@GetMapping("/notice/write")
	public String noticeWriteForm(HttpSession session, Model model) {

		checkNoticeAdmin(session);

		if (session.getAttribute("noticeWriteToken") == null) {
			session.setAttribute("noticeWriteToken", UUID.randomUUID().toString());
		}

		NoticeVO form = new NoticeVO();
		form.setIsPinned(0);
		form.setIsVisible(1);

		model.addAttribute("noticeForm", form);

		return "notice/write";
	}

	@PostMapping("/notice/write")
	public String noticeWrite(@RequestParam(name = "title", defaultValue = "") String title,
			@RequestParam(name = "content", defaultValue = "") String content,
			@RequestParam(name = "isPinned", defaultValue = "0") int isPinned,
			@RequestParam(name = "isVisible") int isVisible,
			@RequestParam(name = "writeToken", required = false) String writeToken, HttpSession session, Model model,
			RedirectAttributes redirectAttributes) throws Exception {

		checkNoticeAdmin(session);

		String expectedToken = (String) session.getAttribute("noticeWriteToken");

		if (expectedToken == null || !expectedToken.equals(writeToken)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "등록 화면을 새로 열고 다시 시도해주세요.");
		}

		NoticeVO vo = new NoticeVO();
		vo.setTitle(title);
		vo.setContent(content);
		vo.setIsPinned(isPinned);
		vo.setIsVisible(isVisible);

		try {
			boardService.insertNotice(vo, (Long) session.getAttribute("loginMemberIdx"),
					(String) session.getAttribute("loginRole"));
		} catch (IllegalArgumentException e) {
			model.addAttribute("noticeForm", vo);
			model.addAttribute("errorMessage", e.getMessage());
			return "notice/write";
		}

		session.removeAttribute("noticeWriteToken");

		String message = isVisible == 1 ? "공지사항이 등록되었습니다." : "비공개로 저장되었습니다. 공개 목록에는 표시되지 않습니다.";

		redirectAttributes.addFlashAttribute("successMessage", message);

		return "redirect:/board/notice/list";
	}

	private void prepareNoticeActionToken(HttpSession session) {

		if (session.getAttribute("noticeActionToken") == null) {
			session.setAttribute("noticeActionToken", UUID.randomUUID().toString());
		}
	}

	private void checkNoticeActionToken(HttpSession session, String actionToken) {

		String expected = (String) session.getAttribute("noticeActionToken");

		if (expected == null || !expected.equals(actionToken)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "화면을 새로 열고 다시 시도해주세요.");
		}
	}

	@GetMapping("/notice/admin/list")
	public String noticeAdminList(HttpSession session, Model model) throws Exception {

		checkNoticeAdmin(session);
		prepareNoticeActionToken(session);

		model.addAttribute("noticeList", boardService.selectAdminNoticeList(
				(Long) session.getAttribute("loginMemberIdx"), (String) session.getAttribute("loginRole")));

		return "notice/admin-list";
	}

	@GetMapping("/notice/update")
	public String noticeUpdateForm(@RequestParam("noticeIdx") Long noticeIdx, HttpSession session, Model model)
			throws Exception {

		checkNoticeAdmin(session);

		if (noticeIdx <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 공지사항 번호입니다.");
		}

		NoticeVO condition = new NoticeVO();
		condition.setNoticeIdx(noticeIdx);

		NoticeVO notice = boardService.selectAdminNotice(condition, (Long) session.getAttribute("loginMemberIdx"),
				(String) session.getAttribute("loginRole"));

		if (notice == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다.");
		}

		prepareNoticeActionToken(session);
		model.addAttribute("noticeForm", notice);

		return "notice/update";
	}

	@PostMapping("/notice/update")
	public String noticeUpdate(@RequestParam("noticeIdx") Long noticeIdx,
			@RequestParam(name = "title", defaultValue = "") String title,
			@RequestParam(name = "content", defaultValue = "") String content, @RequestParam("isPinned") int isPinned,
			@RequestParam("isVisible") int isVisible,
			@RequestParam(name = "actionToken", required = false) String actionToken, HttpSession session, Model model,
			RedirectAttributes redirectAttributes) throws Exception {

		checkNoticeAdmin(session);
		checkNoticeActionToken(session, actionToken);

		NoticeVO vo = new NoticeVO();
		vo.setNoticeIdx(noticeIdx);
		vo.setTitle(title);
		vo.setContent(content);
		vo.setIsPinned(isPinned);
		vo.setIsVisible(isVisible);

		try {
			boardService.updateNotice(vo, (Long) session.getAttribute("loginMemberIdx"),
					(String) session.getAttribute("loginRole"));
		} catch (IllegalArgumentException e) {
			model.addAttribute("noticeForm", vo);
			model.addAttribute("errorMessage", e.getMessage());
			return "notice/update";
		}

		redirectAttributes.addFlashAttribute("successMessage", "공지사항이 수정되었습니다.");

		return "redirect:/board/notice/admin/list";
	}

	@PostMapping("/notice/delete")
	public String noticeDelete(@RequestParam("noticeIdx") Long noticeIdx,
			@RequestParam(name = "actionToken", required = false) String actionToken, HttpSession session,
			RedirectAttributes redirectAttributes) throws Exception {

		checkNoticeAdmin(session);
		checkNoticeActionToken(session, actionToken);

		NoticeVO vo = new NoticeVO();
		vo.setNoticeIdx(noticeIdx);

		try {
			boardService.deleteNotice(vo, (Long) session.getAttribute("loginMemberIdx"),
					(String) session.getAttribute("loginRole"));
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

			return "redirect:/board/notice/admin/list";
		}

		redirectAttributes.addFlashAttribute("successMessage", "공지사항이 삭제되었습니다.");

		return "redirect:/board/notice/admin/list";
	}
}