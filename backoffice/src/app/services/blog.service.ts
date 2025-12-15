import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Blog {
  id?: number;
  title: string;
  content: string;
  photo?: string;
  tags: string[];
  likes: number;
  authorId: number;
  authorName: string;
  authorRole: string;
  createdAt: string;
  updatedAt: string;
  commentCount: number;
}

export interface Comment {
  id: number;
  content: string;
  authorId: number;
  authorName: string;
  authorRole: string;
  blogId: number;
  createdAt: string;
  replies: Reply[];
  replyCount: number;
}

export interface Reply {
  id: number;
  content: string;
  authorId: number;
  authorName: string;
  authorRole: string;
  commentId: number;
  createdAt: string;
}

export interface CreateBlogRequest {
  title: string;
  content: string;
  photo?: string;
  tags: string[];
}

export interface CreateCommentRequest {
  content: string;
}

export interface CreateReplyRequest {
  content: string;
}

@Injectable({
  providedIn: 'root'
})
export class BlogService {
  private apiUrl = 'http://localhost:8090/api/blogs';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    });
  }

  // Blog CRUD operations
  createBlog(blog: CreateBlogRequest): Observable<Blog> {
    return this.http.post<Blog>(this.apiUrl, blog, { headers: this.getHeaders() });
  }

  getAllBlogs(): Observable<Blog[]> {
    return this.http.get<Blog[]>(this.apiUrl, { headers: this.getHeaders() });
  }

  getBlogById(id: number): Observable<Blog> {
    return this.http.get<Blog>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  updateBlog(id: number, blog: CreateBlogRequest): Observable<Blog> {
    return this.http.put<Blog>(`${this.apiUrl}/${id}`, blog, { headers: this.getHeaders() });
  }

  deleteBlog(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  likeBlog(id: number): Observable<Blog> {
    return this.http.post<Blog>(`${this.apiUrl}/${id}/like`, {}, { headers: this.getHeaders() });
  }

  // Comment operations
  addComment(blogId: number, comment: CreateCommentRequest): Observable<Comment> {
    return this.http.post<Comment>(`${this.apiUrl}/${blogId}/comments`, comment, { headers: this.getHeaders() });
  }

  getBlogComments(blogId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${this.apiUrl}/${blogId}/comments`, { headers: this.getHeaders() });
  }

  // Reply operations
  addReply(commentId: number, reply: CreateReplyRequest): Observable<Reply> {
    return this.http.post<Reply>(`${this.apiUrl}/comments/${commentId}/replies`, reply, { headers: this.getHeaders() });
  }
}
