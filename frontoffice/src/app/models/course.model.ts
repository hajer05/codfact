export interface Course {
  id: number;
  title: string;
  description: string;
  shortDescription?: string;
  instructor?: string;
  teacherName?: string;
  price: number;
  level: CourseLevel;
  rating?: number;
  enrollmentCount?: number;
  studentsCount?: number;
  duration?: number;
  thumbnailImage?: string;
  previewVideo?: string;
  tags?: string[];
  category?: string;
  language?: string;
  moduleCount?: number;
  lessonCount?: number;
  modules?: CourseModule[];
}

export interface CourseModule {
  id?: number;
  title: string;
  description?: string;
  order?: number;
  lessons?: Lesson[];
}

export interface Lesson {
  id?: number;
  title: string;
  description?: string;
  duration?: number;
  videoDuration?: number;
  order?: number;
  orderIndex?: number;
  videoUrl?: string;
  video_url?: string;
  videoPath?: string;
  videoFileName?: string;
  thumbnail?: string;
  type?: 'video' | 'text' | 'quiz' | 'VIDEO' | 'TEXT' | 'QUIZ';
  content?: string;
  attachmentUrl?: string;
  attachmentFileName?: string;
  free?: boolean;
  createdAt?: string;
  publishedAt?: string;
  updatedAt?: string;
}

export enum CourseLevel {
  BEGINNER = 'BEGINNER',
  INTERMEDIATE = 'INTERMEDIATE',
  ADVANCED = 'ADVANCED'
}

export interface CourseFilter {
  search?: string;
  category?: string;
  level?: CourseLevel;
  priceRange?: {
    min: number;
    max: number;
  };
  rating?: number;
  isFree?: boolean;
  tags?: string[];
}
