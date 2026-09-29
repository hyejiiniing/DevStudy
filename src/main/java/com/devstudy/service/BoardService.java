package com.devstudy.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.devstudy.vo.BoardFileVO;
import com.devstudy.vo.BoardVO;
import com.devstudy.vo.FaqVO;
import com.devstudy.vo.NoticeVO;

public interface BoardService {

    public List<BoardVO> selectBoardList(BoardVO vo) throws Exception;
    public BoardVO selectBoard(BoardVO vo) throws Exception;
    public int insertBoard(
            BoardVO vo,
            Long loginMemberIdx,
            String loginRole) throws Exception;

    public int insertBoard(
            BoardVO vo,
            Long loginMemberIdx,
            String loginRole,
            List<MultipartFile> files) throws Exception;
    public int updateBoard(BoardVO vo, Long loginMemberIdx) throws Exception;
    public int deleteBoard(BoardVO vo, Long loginMemberIdx) throws Exception;
    public long selectBoardCount(BoardVO vo) throws Exception;
    public List<BoardFileVO> selectBoardFileList(
            BoardFileVO vo) throws Exception;
    public BoardFileVO selectBoardFile(
            BoardFileVO vo) throws Exception;
    public int updateBoard(
            BoardVO vo,
            Long loginMemberIdx,
            String loginRole,
            List<MultipartFile> files,
            List<Long> deleteFileIdxs) throws Exception;
    
    public List<FaqVO> selectFaqList() throws Exception;
    
    public List<NoticeVO> selectNoticeList() throws Exception;
	public NoticeVO selectNotice(NoticeVO vo) throws Exception;
	public int insertNotice(
	        NoticeVO vo,
	        Long loginMemberIdx,
	        String loginRole) throws Exception;
	
}