package redlightBack.Comment.Dto;

import redlightBack.Comment.Domain.Comment;

public record CreateReplyRequest(Comment parent) {
}
