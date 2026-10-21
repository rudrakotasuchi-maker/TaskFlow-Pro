
package com.taskflow.taskflowpro.service;

import com.taskflow.taskflowpro.entity.Comment;

import java.util.List;

public interface CommentService {

    Comment saveComment(Comment comment);

    List<Comment> getCommentsByTask(Long taskId);

}

