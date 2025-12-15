import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BlogService, Blog, Comment, CreateCommentRequest, CreateReplyRequest } from '../../../services/blog.service';

@Component({
  selector: 'app-blog-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './blog-detail.component.html',
  styleUrls: ['./blog-detail.component.scss']
})
export class BlogDetailComponent implements OnInit {
  blog?: Blog;
  comments: Comment[] = [];
  loading = false;
  commentsLoading = false;
  newComment = '';
  replyContent: { [commentId: number]: string } = {};
  showReplyForm: { [commentId: number]: boolean } = {};

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private blogService: BlogService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      const blogId = +params['id'];
      if (blogId) {
        this.loadBlog(blogId);
        this.loadComments(blogId);
      }
    });
  }

  loadBlog(id: number) {
    this.loading = true;
    this.blogService.getBlogById(id).subscribe({
      next: (blog) => {
        this.blog = blog;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading blog:', error);
        this.loading = false;
        this.router.navigate(['/blogs']);
      }
    });
  }

  loadComments(blogId: number) {
    this.commentsLoading = true;
    this.blogService.getBlogComments(blogId).subscribe({
      next: (comments) => {
        this.comments = comments;
        this.commentsLoading = false;
      },
      error: (error) => {
        console.error('Error loading comments:', error);
        this.commentsLoading = false;
      }
    });
  }

  likeBlog() {
    if (this.blog?.id) {
      this.blogService.likeBlog(this.blog.id).subscribe({
        next: (updatedBlog) => {
          this.blog = updatedBlog;
        },
        error: (error) => {
          console.error('Error liking blog:', error);
        }
      });
    }
  }

  addComment() {
    if (this.newComment.trim() && this.blog?.id) {
      const request: CreateCommentRequest = { content: this.newComment.trim() };
      
      this.blogService.addComment(this.blog.id, request).subscribe({
        next: (comment) => {
          this.comments.unshift(comment);
          this.newComment = '';
          if (this.blog) {
            this.blog.commentCount++;
          }
        },
        error: (error) => {
          console.error('Error adding comment:', error);
        }
      });
    }
  }

  toggleReplyForm(commentId: number) {
    this.showReplyForm[commentId] = !this.showReplyForm[commentId];
    if (!this.showReplyForm[commentId]) {
      this.replyContent[commentId] = '';
    }
  }

  addReply(commentId: number) {
    const content = this.replyContent[commentId];
    if (content?.trim()) {
      const request: CreateReplyRequest = { content: content.trim() };
      
      this.blogService.addReply(commentId, request).subscribe({
        next: (reply) => {
          const comment = this.comments.find(c => c.id === commentId);
          if (comment) {
            comment.replies.push(reply);
            comment.replyCount++;
          }
          this.replyContent[commentId] = '';
          this.showReplyForm[commentId] = false;
        },
        error: (error) => {
          console.error('Error adding reply:', error);
        }
      });
    }
  }

  editBlog() {
    if (this.blog?.id) {
      this.router.navigate(['/blogs', this.blog.id, 'edit']);
    }
  }

  deleteBlog() {
    if (this.blog?.id && confirm('Are you sure you want to delete this blog?')) {
      this.blogService.deleteBlog(this.blog.id).subscribe({
        next: () => {
          this.router.navigate(['/blogs']);
        },
        error: (error) => {
          console.error('Error deleting blog:', error);
        }
      });
    }
  }
}
