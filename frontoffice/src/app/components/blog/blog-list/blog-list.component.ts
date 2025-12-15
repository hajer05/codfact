import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BlogService, Blog } from '../../../services/blog.service';

@Component({
  selector: 'app-blog-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './blog-list.component.html',
  styleUrls: ['./blog-list.component.scss']
})
export class BlogListComponent implements OnInit {
  blogs: Blog[] = [];
  loading = false;
  searchTerm = '';

  constructor(private blogService: BlogService) {}

  ngOnInit() {
    this.loadBlogs();
  }

  loadBlogs() {
    this.loading = true;
    this.blogService.getAllBlogs().subscribe({
      next: (blogs) => {
        this.blogs = blogs;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading blogs:', error);
        this.loading = false;
      }
    });
  }

  likeBlog(id: number) {
    this.blogService.likeBlog(id).subscribe({
      next: (updatedBlog) => {
        const index = this.blogs.findIndex(blog => blog.id === id);
        if (index !== -1) {
          this.blogs[index] = updatedBlog;
        }
      },
      error: (error) => {
        console.error('Error liking blog:', error);
      }
    });
  }

  get filteredBlogs() {
    if (!this.searchTerm) {
      return this.blogs;
    }
    return this.blogs.filter(blog => 
      blog.title.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      blog.content.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      blog.authorName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      blog.tags.some(tag => tag.toLowerCase().includes(this.searchTerm.toLowerCase()))
    );
  }
}
