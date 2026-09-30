package com.devstudy.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.devstudy.file.BoardFileStorage;
import com.devstudy.mapper.BoardFileMapper;
import com.devstudy.mapper.BoardMapper;
import com.devstudy.service.BoardService;
import com.devstudy.vo.BoardFileVO;
import com.devstudy.vo.BoardVO;
import com.devstudy.vo.FaqVO;
import com.devstudy.vo.NoticeVO;

@Service
public class BoardServiceImpl implements BoardService {

	private static final Logger LOG = Logger.getLogger(BoardServiceImpl.class.getName());

	private final BoardMapper boardMapper;
	private final BoardFileMapper boardFileMapper;
	private final BoardFileStorage boardFileStorage;

	public BoardServiceImpl(BoardMapper boardMapper, BoardFileMapper boardFileMapper,
			BoardFileStorage boardFileStorage) {

		this.boardMapper = boardMapper;
		this.boardFileMapper = boardFileMapper;
		this.boardFileStorage = boardFileStorage;
	}

	@Override
	@Transactional(readOnly = true)
	public List<BoardVO> selectBoardList(BoardVO vo) throws Exception {
		validateSearch(vo);
		return boardMapper.selectBoardList(vo);
	}

	@Override
	@Transactional(readOnly = true)
	public long selectBoardCount(BoardVO vo) throws Exception {
		validateSearch(vo);
		return boardMapper.selectBoardCount(vo);
	}

	// 게시글 상세
	@Override
	@Transactional(readOnly = true)
	public BoardVO selectBoard(BoardVO vo) throws Exception {
		validateBoardIdx(vo);
		return boardMapper.selectBoard(vo);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int insertBoard(BoardVO vo, Long loginMemberIdx, String loginRole) throws Exception {

		return saveBoard(vo, loginMemberIdx, loginRole);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int insertBoard(BoardVO vo, Long loginMemberIdx, String loginRole, List<MultipartFile> files)
			throws Exception {

		List<MultipartFile> attachments = new ArrayList<>();

		if (files != null) {
			for (MultipartFile file : files) {
				if (file == null) {
					continue;
				}

				String name = file.getOriginalFilename();

				// 파일 선택을 하지 않은 입력란
				if (file.isEmpty() && (name == null || name.isBlank())) {
					continue;
				}

				if (file.isEmpty()) {
					throw new IllegalArgumentException("빈 파일은 첨부할 수 없습니다.");
				}

				if (file.getSize() > 10L * 1024 * 1024) {
					throw new IllegalArgumentException("파일 하나의 크기는 10MB 이하여야 합니다.");
				}

				attachments.add(file);
			}
		}

		if (attachments.size() > 5) {
			throw new IllegalArgumentException("첨부파일은 최대 5개까지 등록할 수 있습니다.");
		}

		List<String> savedNames = new ArrayList<>();

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCompletion(int status) {
				if (status == STATUS_ROLLED_BACK) {
					removeStoredFiles(savedNames);
				}
			}
		});

		int result = saveBoard(vo, loginMemberIdx, loginRole);

		for (MultipartFile file : attachments) {
			BoardFileVO fileVO = boardFileStorage.store(file);

			savedNames.add(fileVO.getStoredName());
			fileVO.setBoardIdx(vo.getBoardIdx());

			if (boardFileMapper.insertBoardFile(fileVO) != 1) {
				throw new IllegalStateException("첨부파일 정보 저장에 실패했습니다.");
			}
		}

		return result;
	}

	private int saveBoard(BoardVO vo, Long loginMemberIdx, String loginRole) throws Exception {

		validateLogin(loginMemberIdx);
		validateBoardType(vo);

		if (vo.getBoardType() == 6 && !"ADMIN".equals(loginRole)) {
			throw new IllegalArgumentException("공지사항은 관리자만 작성할 수 있습니다.");
		}

		validateContent(vo);

		vo.setMemberIdx(loginMemberIdx);
		vo.setBoardIdx(null);

		int result = boardMapper.insertBoard(vo);

		if (result != 1 || vo.getBoardIdx() == null) {
			throw new IllegalStateException("게시글 저장에 실패했습니다.");
		}

		return result;
	}

	// 수정
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int updateBoard(BoardVO vo, Long loginMemberIdx) throws Exception {

		validateBoardIdx(vo);
		validateLogin(loginMemberIdx);

		BoardVO existing = boardMapper.selectBoardForUpdate(vo);

		if (existing == null || !loginMemberIdx.equals(existing.getMemberIdx())) {
			throw new IllegalArgumentException("게시글이 없거나 수정 권한이 없습니다.");
		}

		vo.setBoardType(existing.getBoardType());

		validateContent(vo);
		vo.setMemberIdx(loginMemberIdx);

		int updatedCount = boardMapper.updateBoard(vo);

		if (updatedCount == 0) {
			throw new IllegalArgumentException("게시글이 없거나 수정 권한이 없습니다.");
		}

		return updatedCount;
	}

	// 게시글 및 첨부파일 삭제
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int deleteBoard(BoardVO vo, Long loginMemberIdx) throws Exception {

		validateBoardIdx(vo);
		validateLogin(loginMemberIdx);

		BoardVO board = boardMapper.selectBoard(vo);

		if (board == null || !loginMemberIdx.equals(board.getMemberIdx())) {
			throw new IllegalArgumentException("게시글이 없거나 삭제 권한이 없습니다.");
		}

		BoardFileVO condition = new BoardFileVO();
		condition.setBoardIdx(board.getBoardIdx());

		List<BoardFileVO> attachedFiles = boardFileMapper.selectBoardFileList(condition);

		List<String> storedNames = new ArrayList<>();

		for (BoardFileVO file : attachedFiles) {
			storedNames.add(file.getStoredName());
		}

		boardFileMapper.deleteBoardFiles(condition);

		vo.setMemberIdx(loginMemberIdx);
		int deletedCount = boardMapper.deleteBoard(vo);

		if (deletedCount != 1) {
			throw new IllegalArgumentException("게시글이 없거나 삭제 권한이 없습니다.");
		}

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				removeStoredFiles(storedNames);
			}
		});

		return deletedCount;
	}

	private void validateLogin(Long loginMemberIdx) {
		if (loginMemberIdx == null) {
			throw new IllegalArgumentException("로그인이 필요합니다.");
		}
	}

	private void validateBoardType(BoardVO vo) {
		if (vo == null || vo.getBoardType() == null || vo.getBoardType() < 1 || vo.getBoardType() > 6) {
			throw new IllegalArgumentException("올바르지 않은 게시판입니다.");
		}
	}

	private void validateBoardIdx(BoardVO vo) {
		if (vo == null || vo.getBoardIdx() == null || vo.getBoardIdx() <= 0) {
			throw new IllegalArgumentException("올바르지 않은 게시글 번호입니다.");
		}
	}

	private void validateSearch(BoardVO vo) {
		validateBoardType(vo);

		String searchType = vo.getSearchType();

		if (!"all".equals(searchType) && !"title".equals(searchType) && !"content".equals(searchType)) {
			throw new IllegalArgumentException("올바르지 않은 검색 조건입니다.");
		}

		String keyword = vo.getKeyword() == null ? "" : vo.getKeyword().strip();

		if (keyword.length() > 100) {
			throw new IllegalArgumentException("검색어는 100자 이내로 입력해주세요.");
		}

		vo.setKeyword(keyword);

		if (vo.getPage() < 1) {
			vo.setPage(1);
		}
	}

	private void validateContent(BoardVO vo) {
		String title = vo.getTitle() == null ? "" : vo.getTitle().strip();

		String content = vo.getContent() == null ? "" : vo.getContent();

		String category = vo.getCategory() == null ? "" : vo.getCategory().strip();

		if (title.isEmpty() || title.length() > 256) {
			throw new IllegalArgumentException("제목은 1~256자로 입력해주세요.");
		}

		if (content.isBlank()) {
			throw new IllegalArgumentException("내용을 입력해주세요.");
		}

		if (content.getBytes(StandardCharsets.UTF_8).length > 65535) {
			throw new IllegalArgumentException("본문이 너무 깁니다. 내용을 줄여주세요.");
		}

		if (category.length() > 256) {
			throw new IllegalArgumentException("분류는 256자 이내로 입력해주세요.");
		}

		vo.setTitle(title);
		vo.setContent(content);
		vo.setCategory(category.isEmpty() ? null : category);
	}

	private void removeStoredFiles(List<String> storedNames) {
		for (String storedName : storedNames) {
			try {
				boardFileStorage.delete(storedName);
			} catch (IOException | RuntimeException e) {
				LOG.log(Level.SEVERE, "첨부파일 정리 실패: " + storedName, e);
			}
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<BoardFileVO> selectBoardFileList(BoardFileVO vo) throws Exception {

		if (vo == null || vo.getBoardIdx() == null || vo.getBoardIdx() <= 0) {
			throw new IllegalArgumentException("올바르지 않은 게시글 번호입니다.");
		}

		return boardFileMapper.selectBoardFileList(vo);
	}

	@Override
	@Transactional(readOnly = true)
	public BoardFileVO selectBoardFile(BoardFileVO vo) throws Exception {

		if (vo == null || vo.getBoardIdx() == null || vo.getBoardIdx() <= 0 || vo.getFileIdx() == null
				|| vo.getFileIdx() <= 0) {
			throw new IllegalArgumentException("올바르지 않은 첨부파일 번호입니다.");
		}

		return boardFileMapper.selectBoardFile(vo);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int updateBoard(BoardVO vo, Long loginMemberIdx, String loginRole, List<MultipartFile> files,
			List<Long> deleteFileIdxs) throws Exception {

		validateBoardIdx(vo);
		validateLogin(loginMemberIdx);

		BoardVO board = boardMapper.selectBoardForUpdate(vo);

		if (board == null || !loginMemberIdx.equals(board.getMemberIdx())) {
			throw new IllegalArgumentException("게시글이 없거나 수정 권한이 없습니다.");
		}

		if (board.getBoardType() == 6 && !"ADMIN".equals(loginRole)) {
			throw new IllegalArgumentException("공지사항 변경 권한이 없습니다.");
		}

		if ((board.getBoardType() == 5 || board.getBoardType() == 6) && !"ADMIN".equals(loginRole)) {
			throw new IllegalArgumentException("FAQ와 공지사항 변경 권한이 없습니다.");
		}

		BoardFileVO condition = new BoardFileVO();
		condition.setBoardIdx(board.getBoardIdx());

		List<BoardFileVO> existingFiles = boardFileMapper.selectBoardFileList(condition);

		Set<Long> deleteIds = new HashSet<>();

		if (deleteFileIdxs != null) {
			deleteIds.addAll(deleteFileIdxs);
		}

		Set<Long> existingIds = new HashSet<>();

		for (BoardFileVO file : existingFiles) {
			existingIds.add(file.getFileIdx());
		}

		if (!existingIds.containsAll(deleteIds)) {
			throw new IllegalArgumentException("삭제할 첨부파일이 올바르지 않습니다.");
		}

		List<MultipartFile> newFiles = new ArrayList<>();

		if (files != null) {
			for (MultipartFile file : files) {
				if (file == null) {
					continue;
				}

				String name = file.getOriginalFilename();

				if (file.isEmpty() && (name == null || name.isBlank())) {
					continue;
				}

				if (file.isEmpty()) {
					throw new IllegalArgumentException("빈 파일은 첨부할 수 없습니다.");
				}

				if (file.getSize() > 10L * 1024 * 1024) {
					throw new IllegalArgumentException("파일 하나의 크기는 10MB 이하여야 합니다.");
				}

				newFiles.add(file);
			}
		}

		int finalFileCount = existingFiles.size() - deleteIds.size() + newFiles.size();

		if (finalFileCount > 5) {
			throw new IllegalArgumentException("기존 파일과 새 파일을 합쳐 최대 5개까지 첨부할 수 있습니다.");
		}

		List<String> newStoredNames = new ArrayList<>();
		List<String> oldStoredNames = new ArrayList<>();

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

			@Override
			public void afterCommit() {
				removeStoredFiles(oldStoredNames);
			}

			@Override
			public void afterCompletion(int status) {
				if (status == STATUS_ROLLED_BACK) {
					removeStoredFiles(newStoredNames);
				}
			}
		});

		int result = updateBoard(vo, loginMemberIdx);

		for (BoardFileVO file : existingFiles) {
			if (deleteIds.contains(file.getFileIdx())) {

				int deleted = boardFileMapper.deleteBoardFile(file);

				if (deleted != 1) {
					throw new IllegalStateException("첨부파일 정보 삭제에 실패했습니다.");
				}

				oldStoredNames.add(file.getStoredName());
			}
		}

		for (MultipartFile file : newFiles) {
			BoardFileVO savedFile = boardFileStorage.store(file);

			newStoredNames.add(savedFile.getStoredName());
			savedFile.setBoardIdx(board.getBoardIdx());

			if (boardFileMapper.insertBoardFile(savedFile) != 1) {
				throw new IllegalStateException("첨부파일 정보 저장에 실패했습니다.");
			}
		}

		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public List<FaqVO> selectFaqList() throws Exception {
		return boardMapper.selectFaqList();
	}

	// 공지사항
	@Override
	@Transactional(readOnly = true)
	public List<NoticeVO> selectNoticeList() throws Exception {
		return boardMapper.selectNoticeList();
	}

	@Override
	@Transactional(readOnly = true)
	public NoticeVO selectNotice(NoticeVO vo) throws Exception {

		if (vo == null || vo.getNoticeIdx() == null || vo.getNoticeIdx() <= 0) {
			throw new IllegalArgumentException("올바르지 않은 공지사항 번호입니다.");
		}

		return boardMapper.selectNotice(vo);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int insertNotice(NoticeVO vo, Long loginMemberIdx, String loginRole) throws Exception {

		if (loginMemberIdx == null || !"ADMIN".equals(loginRole)) {
			throw new IllegalArgumentException("관리자만 공지사항을 등록할 수 있습니다.");
		}

		if (vo == null) {
			throw new IllegalArgumentException("공지사항 정보를 입력해주세요.");
		}

		String title = vo.getTitle() == null ? "" : vo.getTitle().strip();

		String content = vo.getContent() == null ? "" : vo.getContent();

		if (title.isEmpty() || title.length() > 256) {
			throw new IllegalArgumentException("제목은 1~256자로 입력해주세요.");
		}

		if (content.isBlank()) {
			throw new IllegalArgumentException("내용을 입력해주세요.");
		}

		if (content.getBytes(StandardCharsets.UTF_8).length > 65535) {
			throw new IllegalArgumentException("본문이 너무 깁니다. 내용을 줄여주세요.");
		}

		if ((!Integer.valueOf(0).equals(vo.getIsPinned()) && !Integer.valueOf(1).equals(vo.getIsPinned()))
				|| (!Integer.valueOf(0).equals(vo.getIsVisible()) && !Integer.valueOf(1).equals(vo.getIsVisible()))) {
			throw new IllegalArgumentException("상단 고정 또는 공개 설정이 올바르지 않습니다.");
		}

		vo.setTitle(title);
		vo.setContent(content);
		vo.setMemberIdx(loginMemberIdx);
		vo.setNoticeIdx(null);

		int result = boardMapper.insertNotice(vo);

		if (result != 1 || vo.getNoticeIdx() == null) {
			throw new IllegalStateException("공지사항 등록에 실패했습니다.");
		}

		return result;
	}

	private void validateNoticeAdmin(Long loginMemberIdx, String loginRole) {

		if (loginMemberIdx == null || !"ADMIN".equals(loginRole)) {
			throw new IllegalArgumentException("관리자만 공지사항을 관리할 수 있습니다.");
		}
	}

	private void validateNoticeIdx(NoticeVO vo) {

		if (vo == null || vo.getNoticeIdx() == null || vo.getNoticeIdx() <= 0) {
			throw new IllegalArgumentException("올바르지 않은 공지사항 번호입니다.");
		}
	}

	private void validateNoticeForm(NoticeVO vo) {

		String title = vo.getTitle() == null ? "" : vo.getTitle().strip();

		String content = vo.getContent() == null ? "" : vo.getContent();

		if (title.isEmpty() || title.length() > 256) {
			throw new IllegalArgumentException("제목은 1~256자로 입력해주세요.");
		}

		if (content.isBlank()) {
			throw new IllegalArgumentException("내용을 입력해주세요.");
		}

		if (content.getBytes(StandardCharsets.UTF_8).length > 65535) {
			throw new IllegalArgumentException("본문이 너무 깁니다. 내용을 줄여주세요.");
		}

		if ((!Integer.valueOf(0).equals(vo.getIsPinned()) && !Integer.valueOf(1).equals(vo.getIsPinned()))
				|| (!Integer.valueOf(0).equals(vo.getIsVisible()) && !Integer.valueOf(1).equals(vo.getIsVisible()))) {
			throw new IllegalArgumentException("상단 고정 또는 공개 설정이 올바르지 않습니다.");
		}

		vo.setTitle(title);
		vo.setContent(content);
	}

	@Override
	@Transactional(readOnly = true)
	public List<NoticeVO> selectAdminNoticeList(Long loginMemberIdx, String loginRole) throws Exception {

		validateNoticeAdmin(loginMemberIdx, loginRole);

		return boardMapper.selectAdminNoticeList();
	}

	@Override
	@Transactional(readOnly = true)
	public NoticeVO selectAdminNotice(NoticeVO vo, Long loginMemberIdx, String loginRole) throws Exception {

		validateNoticeAdmin(loginMemberIdx, loginRole);
		validateNoticeIdx(vo);

		return boardMapper.selectAdminNotice(vo);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int updateNotice(NoticeVO vo, Long loginMemberIdx, String loginRole) throws Exception {

		validateNoticeAdmin(loginMemberIdx, loginRole);
		validateNoticeIdx(vo);
		validateNoticeForm(vo);

		if (boardMapper.selectAdminNotice(vo) == null) {
			throw new IllegalArgumentException("공지사항이 없거나 이미 삭제되었습니다.");
		}

		int result = boardMapper.updateNotice(vo);

		if (result == 0 && boardMapper.selectAdminNotice(vo) == null) {
			throw new IllegalArgumentException("공지사항이 없거나 이미 삭제되었습니다.");
		}

		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int deleteNotice(NoticeVO vo, Long loginMemberIdx, String loginRole) throws Exception {

		validateNoticeAdmin(loginMemberIdx, loginRole);
		validateNoticeIdx(vo);

		int result = boardMapper.deleteNotice(vo);

		if (result != 1) {
			throw new IllegalArgumentException("공지사항이 없거나 이미 삭제되었습니다.");
		}

		return result;
	}
}