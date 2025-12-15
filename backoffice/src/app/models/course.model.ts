export interface Course {
  id: number;
  title: string;
  description: string;
  shortDescription: string;
  thumbnailImage?: string;
  previewVideo?: string;
  price: number;
  level: CourseLevel;
  status: CourseStatus;
  category: string;
  language: string;
  createdAt: string;
  publishedAt?: string;
  teacherName: string;
  teacherId: number;
  moduleCount: number;
  lessonCount: number;
}

export enum CourseLevel {
  BEGINNER = 'BEGINNER',
  INTERMEDIATE = 'INTERMEDIATE',
  ADVANCED = 'ADVANCED'
}

export enum CourseStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
  ARCHIVED = 'ARCHIVED'
}

export interface CreateCourseRequest {
  title: string;
  description: string;
  shortDescription: string;
  price: number;
  level: CourseLevel;
  category: string;
  language: string;
}
