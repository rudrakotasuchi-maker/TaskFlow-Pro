
package com.taskflow.taskflowpro.impl;

import com.taskflow.taskflowpro.entity.Comment;
import com.taskflow.taskflowpro.repository.CommentRepository;
import com.taskflow.taskflowpro.service.CommentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public List<Comment> getCommentsByTask(Long taskId) {
        return commentRepository.findByTaskIdOrderByIdAsc(taskId);
    }
}

